package client.voice;

import client.voice.AudioCapture;
import client.voice.VoiceInputListener;
import client.voice.VoskService;

import java.util.function.Consumer;

public class VoiceManager implements VoiceInputListener {

    private static VoiceManager instance;
    private VoskService vosk;
    private AudioCapture capture;

    private Consumer<String> finalCallback;     // UI nhận text cuối
    private Consumer<String> partialCallback;   // UI muốn hiển thị partial
    private Consumer<String> statusCallback;    // UI hiển thị trạng thái

    private boolean loaded = false;

    public static VoiceManager get() {
        if (instance == null) instance = new VoiceManager();
        return instance;
    }

    private VoiceManager() {}

    // Gọi 1 lần khi load app
    public void loadModel(String path) throws Exception {
        vosk = new VoskService(this);
        vosk.loadModel(path);
        capture = new AudioCapture();
        loaded = true;
    }

    public void startListening(Consumer<String> finalCallback) {
        startListening(finalCallback, null, null);
    }

    public void startListening(Consumer<String> finalCallback,
                               Consumer<String> partialCallback,
                               Consumer<String> statusCallback) {
        this.finalCallback = finalCallback;
        this.partialCallback = partialCallback;
        this.statusCallback = statusCallback;

        if (!loaded) {
            if (statusCallback != null)
                statusCallback.accept("❌ Model chưa được load!");
            return;
        }

        new Thread(() -> {
            try {
                vosk.startRecognition(capture);
            } catch (Exception e) {
                onError(e);
            }
        }).start();
    }

    public void stopListening() {
        try {
            vosk.stopRecognition();
        } catch (Exception e) {
            onError(e);
        }
    }

    // CALLBACK từ VoskService
    @Override
    public void onPartial(String text) {
        if (partialCallback != null) partialCallback.accept(text);
    }

    @Override
    public void onFinal(String text) {
        if (finalCallback != null) finalCallback.accept(text);
    }

    @Override
    public void onError(Exception e) {
        if (statusCallback != null)
            statusCallback.accept("❌ Lỗi: " + e.getMessage());
    }

    @Override
    public void onStatus(String status) {
        if (statusCallback != null)
            statusCallback.accept(status);
    }
}
