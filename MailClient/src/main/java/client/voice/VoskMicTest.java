package client.voice;

import org.vosk.Model;
import org.vosk.Recognizer;

public class VoskMicTest {

    public static void main(String[] args) {
        try {
            // 1. Load model
            System.out.println("Đang load model...");
            Model model = new Model("D:\\tai_lieu_k5\\lap_trinh_mang\\MailClient\\src\\main\\resources\\models\\vosk-model-vn-0.4");
            System.out.println("Model load xong!");

            // 2. Chuẩn bị micro
            AudioCapture capture = new AudioCapture();
            capture.start();
            System.out.println("Micro STARTED!");

            // 3. Khởi tạo recognizer
            Recognizer recognizer = new Recognizer(model, 16000);

            byte[] buffer = new byte[4096];

            System.out.println("=== BẮT ĐẦU THU ÂM TRONG 7 GIÂY ===");

            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < 7000) {

                int n = capture.read(buffer);

                if (n > 0) {
                    boolean isFinal = recognizer.acceptWaveForm(buffer, n);

                    if (isFinal) {
                        System.out.println("[FINAL] " + recognizer.getFinalResult());
                    } else {
                        System.out.println("[PARTIAL] " + recognizer.getPartialResult());
                    }
                } else {
                    System.out.println("Không đọc được dữ liệu từ micro!");
                }
            }

            System.out.println("=== KẾT THÚC THU ÂM ===");

            capture.stop();
            recognizer.close();
            model.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
