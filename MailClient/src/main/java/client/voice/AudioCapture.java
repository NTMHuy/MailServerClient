package client.voice;

import javax.sound.sampled.*;

public class AudioCapture {

    private TargetDataLine line;
    private AudioInputStream convertedStream;

    // Format Vosk yêu cầu
    private final AudioFormat targetFormat = new AudioFormat(
            16000,
            16,
            1,
            true,
            false
    );

    public AudioFormat getFormat() {
        return targetFormat;
    }

    public void start() throws LineUnavailableException {

        // Lấy format mặc định của micro (KHÔNG ép format)
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        for (Mixer.Info mi : mixers) {
            System.out.println("Mic: " + mi.getName());
        }

        AudioFormat defaultFormat = new AudioFormat(
                48000, 16, 2, true, false     // phần lớn micro hiện nay dùng 48000Hz
        );

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, defaultFormat);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Không mở được microphone!");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(defaultFormat);
        line.start();

        AudioInputStream rawStream = new AudioInputStream(line);

        // convert sang 16kHz mono cho Vosk
        convertedStream = AudioSystem.getAudioInputStream(targetFormat, rawStream);
    }

    public int read(byte[] buffer) {
        try {
            return convertedStream.read(buffer, 0, buffer.length);
        } catch (Exception e) {
            System.out.println("Audio read error: " + e.getMessage());
            return -1;
        }
    }

    public void stop() {
        try {
            if (line != null) {
                line.stop();
                line.close();
            }
        } catch (Exception ignored) {}
    }

    public byte[] createBuffer() {
        return new byte[4096];
    }
}
