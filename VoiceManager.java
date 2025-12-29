package Experiment;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;

public class VoiceManager {
    private Socket socket;
    private DataOutputStream audioOut;
    private DataInputStream audioIn;
    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private boolean isTransmitting = false;
    private boolean isReceiving = true;
    private Thread transmitThread;
    private Thread receiveThread;

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            16000.0F,
            16,
            1,
            2,
            16000.0F,
            false
    );

    public VoiceManager(Socket socket) throws IOException {
        this.socket = socket;
        this.audioOut = new DataOutputStream(socket.getOutputStream());
        this.audioIn = new DataInputStream(socket.getInputStream());
        initializeAudio();
        startReceiving();
    }

    private void initializeAudio() {
        try {
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.err.println("Microphone not supported");
                return;
            }
            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(AUDIO_FORMAT);

            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(speakerInfo)) {
                System.err.println("Speakers not supported");
                return;
            }
            speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speakers.open(AUDIO_FORMAT);
            speakers.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // 按下按钮开始通信
    public void startTransmitting() {
        if (isTransmitting || microphone == null) return;

        isTransmitting = true;
        microphone.start();

        transmitThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            try {
                while (isTransmitting) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        audioOut.writeInt(bytesRead);
                        audioOut.write(buffer, 0, bytesRead);
                        audioOut.flush();
                    }
                }
            } catch (IOException e) {
                if (isTransmitting) {
                    e.printStackTrace();
                }
            }
        });
        transmitThread.start();
    }

    // 松开按钮结束通信
    public void stopTransmitting() {
        if (!isTransmitting) return;

        isTransmitting = false;
        if (microphone != null) {
            microphone.stop();
        }

        if (transmitThread != null) {
            try {
                transmitThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 语音通话
    private void startReceiving() {
        receiveThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            try {
                while (isReceiving) {
                    // Read the length of incoming audio data
                    int length = audioIn.readInt();
                    if (length > 0 && length <= buffer.length) {
                        // Read the audio data
                        int totalRead = 0;
                        while (totalRead < length) {
                            int bytesRead = audioIn.read(buffer, totalRead, length - totalRead);
                            if (bytesRead == -1) break;
                            totalRead += bytesRead;
                        }

                        // Play the audio
                        if (speakers != null && totalRead == length) {
                            speakers.write(buffer, 0, length);
                        }
                    }
                }
            } catch (IOException e) {
                if (isReceiving) {
                    e.printStackTrace();
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    // 关闭通讯
    public void close() {
        isReceiving = false;
        stopTransmitting();

        if (microphone != null) {
            microphone.close();
        }
        if (speakers != null) {
            speakers.close();
        }

        try {
            if (audioOut != null) audioOut.close();
            if (audioIn != null) audioIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}