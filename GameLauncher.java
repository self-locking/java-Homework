package Experiment;

import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameLauncher {
    public static void main(String[] args) {
        String[] options = {"作为服务器启动 (等待连接)", "作为客户端连接 (输入IP)"};
        int choice = JOptionPane.showOptionDialog(null, "请选择运行模式", "中国象棋联网对战",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            startAsServer();
        } else if (choice == 1) {
            startAsClient();
        }
    }

    private static void startAsServer() {
        try {
            int port = 8888;
            ServerSocket serverSocket = new ServerSocket(port);

            JFrame frame = new JFrame("中国象棋 - 等待玩家连接...");
            ChessBoard board = new ChessBoard();
            frame.add(board);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 阻塞等待连接
            Socket socket = serverSocket.accept();
            NetworkManager nm = new NetworkManager(socket, board);

            // 核心逻辑：服务器生成随机阵营
            int serverSide = Math.random() > 0.5 ? 1 : -1;
            board.isNetworkGame = true;
            board.networkManager = nm;
            board.resetAndStart(serverSide); // 激活棋盘

            // 同步给客户端
            GameMessage msg = new GameMessage(GameMessage.MessageType.SIDE_ASSIGNMENT);
            msg.setPlayerSide(-serverSide);
            nm.sendMessage(msg);

            frame.setTitle("中国象棋(服务器) - " + (serverSide == 1 ? "红方" : "黑方"));
            JOptionPane.showMessageDialog(frame, "玩家已连接！你是" + (serverSide == 1 ? "红方" : "黑方"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startAsClient() {
        String ip = JOptionPane.showInputDialog("请输入服务器IP地址:", "localhost");
        if (ip == null) return;

        try {
            JFrame frame = new JFrame("中国象棋 - 正在连接...");
            ChessBoard board = new ChessBoard();
            frame.add(board);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 连接服务器
            NetworkManager nm = new NetworkManager(ip, 8888, board);
            board.isNetworkGame = true;
            board.networkManager = nm;

            // 客户端此时依然处于 isWaiting 状态，直到收到服务器的 SIDE_ASSIGNMENT 消息

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "连接失败: " + e.getMessage());
        }
    }
}