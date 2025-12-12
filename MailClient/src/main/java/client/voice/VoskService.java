package client.voice;

import org.vosk.Model;
import org.vosk.Recognizer;

public class VoskService {

    private Recognizer recognizer;
    private Model model;
    private final VoiceInputListener listener;
    private volatile boolean running = false;

    public VoskService(VoiceInputListener listener) {
        this.listener = listener;
    }

    public void loadModel(String path) throws Exception {
        listener.onStatus("🔄 Đang load model…");
        model = new Model(path);
        listener.onStatus("🟢 Model loaded.");
    }

    public void startRecognition(AudioCapture capture) throws Exception {
        // --- ensure recognizer created AFTER capture started to mirror test flow ---
        // start capture first so convertedStream is ready
        capture.start();
        // small pause to let stream warm up (very short)
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        recognizer = new Recognizer(model, 16000);
        listener.onStatus("🎤 Đang nghe…");

        running = true;

        byte[] buffer = capture.createBuffer();

        Thread thread = new Thread(() -> {
            try {
                System.out.println("VoskService: recognition thread started, buffer size=" + buffer.length);
                while (running) {
                    int n = capture.read(buffer);
                    System.out.println("VoskService: read n = " + n);

                    if (n <= 0) {
                        // nếu chưa có dữ liệu thì đợi một chút (tránh busy-loop)
                        try { Thread.sleep(30); } catch (InterruptedException ignored) {}
                        continue;
                    }

                    boolean isFinal = recognizer.acceptWaveForm(buffer, n);

                    if (isFinal) {
                        String result = recognizer.getFinalResult();
                        System.out.println("VoskService: RAW_FINAL = " + result);
                        listener.onFinal(parse(result));
                    } else {
                        String partial = recognizer.getPartialResult();
                        System.out.println("VoskService: RAW_PARTIAL = " + partial);
                        listener.onPartial(parse(partial));
                    }
                }
            } catch (Exception e) {
                // log và báo lại cho listener
                e.printStackTrace();
                listener.onError(e);
            }
        });

        thread.setDaemon(true);
        thread.start();
    }


//    public void startRecognition(AudioCapture capture) throws Exception {
//        recognizer = new Recognizer(model, 16000);
//        listener.onStatus("🎤 Đang nghe…");
//
//        running = true;
//
//        // mở microphone
//        capture.start();
//
//        byte[] buffer = capture.createBuffer();
//
//        // tạo 1 thread đọc âm thanh liên tục
//        Thread thread = new Thread(() -> {
//            try {
//                while (running) {
//                    int n = capture.read(buffer);
//                    if (n <= 0) continue;
//
//                    boolean isFinal = recognizer.acceptWaveForm(buffer, n);
//
//                    if (isFinal) {
//                        String result = recognizer.getFinalResult();
//                        listener.onFinal(parse(result));
//                    } else {
//                        String partial = recognizer.getPartialResult();
//                        listener.onPartial(parse(partial));
//                    }
//                }
//            } catch (Exception e) {
//                listener.onError(e);
//            }
//        });
//
//        thread.setDaemon(true);
//        thread.start();
//    }

    public void stopRecognition() {
        running = false;
        listener.onStatus("⏹ Đã dừng.");

        try {
            if (recognizer != null) {
                recognizer.close();
                recognizer = null;
            }
        } catch (Exception ignored) {}
    }

//    private String parse(String json) {
//        int i = json.indexOf(":\"");
//        int j = json.lastIndexOf("\"");
//        if (i == -1 || j == -1) return "";
//        return json.substring(i + 2, j);
//    }
private String parse(String json) {
    if (json == null) return "";
    var m = java.util.regex.Pattern.compile("\"(partial|text)\"\\s*:\\s*\"(.*?)\"")
            .matcher(json);
    if (m.find()) {
        return m.group(2); // chuỗi recognized
    }
    return "";
}

}
