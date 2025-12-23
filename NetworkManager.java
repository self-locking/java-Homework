package Experiment;

import java.io.*;
import java.net.*;

public class NetworkManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ChessBoard board;
    private boolean isRunning = true;

    public NetworkManager(String serverIP, int port, ChessBoard board) throws IOException {
        this.board = board;
        this.socket = new Socket(serverIP, port);
        setupStreams();
        startListening();
    }

    public NetworkManager(Socket socket, ChessBoard board) throws IOException {
        this.board = board;
        this.socket = socket;
        setupStreams();
        startListening();
    }

    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendMessage(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Object obj = in.readObject();
                    if (obj instanceof GameMessage) {
                        handleMessage((GameMessage) obj);
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }

    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case MOVE:
                board.receiveOpponentMove(msg.getFromPosition(), msg.getToPosition());
                break;
            case SIDE_ASSIGNMENT:
                board.resetAndStart(msg.getPlayerSide());
                break;
            case CHAT:
                // 核心：收到聊天消息后调用棋盘的显示方法
                board.appendChat(msg.getMessage(), false);
                break;
            case GAME_OVER:
                // board.endGameRemote(msg.getMessage()); // 如果你有这个方法
                break;
        }
    }
}