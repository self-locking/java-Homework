package Experiment;

import java.io.*;
import java.net.*;

public class NetworkManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ChessBoard board;
    private boolean isRunning = true;

    // 客户端Socket构造
    public NetworkManager(String serverIP, int port, ChessBoard board) throws IOException {
        this.board = board;
        this.socket = new Socket(serverIP, port);
        setupStreams();
        startListening();
    }

     // 服务端Socket构造
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

    // 将GameMessage对象发送给对方
    public void sendMessage(GameMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 创建一个后台线程持续监听输入流，异步处理收到的消息，不阻塞主线程
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

    // 根据消息类型调用ChessBoard对应方法
    private void handleMessage(GameMessage msg) {
        switch (msg.getType()) {
            case MOVE:
                board.receiveOpponentMove(msg.getFromPosition(), msg.getToPosition());
                break;
            case SIDE_ASSIGNMENT:
                board.resetAndStart(msg.getPlayerSide());
                break;
            case CHAT:
                board.appendChat(msg.getMessage(), false);
                break;
            case GAME_OVER:
                break;
        }
    }
}