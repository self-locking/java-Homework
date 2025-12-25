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
            16000.0F,  // Sample rate
            16,        // Sample size in bits
            1,         // Channels (mono)
            2,         // Frame size
            16000.0F,  // Frame rate
            false      // Big endian
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
            // Initialize microphone
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.err.println("Microphone not supported");
                return;
            }
            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(AUDIO_FORMAT);

            // Initialize speakers
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

    // Start transmitting audio when button is pressed
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
                        // Send audio data length first, then the data
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

    // Stop transmitting audio when button is released
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

    // Continuously receive and play audio from the other player
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

    // Clean up resources
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