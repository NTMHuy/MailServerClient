package client.voice;

public interface VoiceInputListener {
    void onPartial(String text);
    void onFinal(String text);
    void onError(Exception e);
    void onStatus(String status);
}
