package Experiment;

import javax.swing.*;
import java.awt.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameLauncher {
    public static void main(String[] args) {
        String[] options = {"作为服务器启动", "作为客户端连接"};
        int choice = JOptionPane.showOptionDialog(null, "请选择运行模式", "中国象棋联网对战",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) startAsServer();
        else if (choice == 1) startAsClient();
    }

    // 统筹 UI 布局的方法
    private static void initUI(JFrame frame, ChessBoard board) {
        // 右侧聊天面板
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(300, 600));
        sidePanel.setBorder(BorderFactory.createTitledBorder("文字聊天 (回车发送)"));

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        board.setChatArea(chatArea); // 关联board

        JTextField chatInput = new JTextField();
        chatInput.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty() && board.networkManager != null) {
                GameMessage chatMsg = new GameMessage(GameMessage.MessageType.CHAT);
                chatMsg.setMessage(text);
                board.networkManager.sendMessage(chatMsg);  // 对方信息
                board.appendChat(text, true);         // 己方信息
                chatInput.setText("");                     // 清空输入框
            }
        });

        sidePanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        sidePanel.add(chatInput, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout());
        frame.add(board, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // 启动服务器
    private static void startAsServer() {
        try {
            JFrame frame = new JFrame("等待玩家连接...");
            ChessBoard board = new ChessBoard();
            initUI(frame, board);

            // 先保存frame引用到board，这样setMySide可以找到它
            board.setGameFrame(frame);

            ServerSocket serverSocket = new ServerSocket(1234);
            Socket socket = serverSocket.accept();
            NetworkManager nm = new NetworkManager(socket, board);

            int serverSide = Math.random() > 0.5 ? 1 : -1;
            board.isNetworkGame = true;
            board.networkManager = nm;
            board.resetAndStart(serverSide);

            GameMessage msg = new GameMessage(GameMessage.MessageType.SIDE_ASSIGNMENT);
            msg.setPlayerSide(-serverSide);
            nm.sendMessage(msg);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // 启动客户端
    private static void startAsClient() {
        String ip = JOptionPane.showInputDialog("请输入服务器IP:", "localhost");
        if (ip == null) return;
        try {
            JFrame frame = new JFrame("正在连接服务器...");
            ChessBoard board = new ChessBoard();
            initUI(frame, board);

            // 先保存frame引用到board
            board.setGameFrame(frame);

            NetworkManager nm = new NetworkManager(ip, 1234, board);
            board.isNetworkGame = true;
            board.networkManager = nm;
        } catch (Exception e) { e.printStackTrace(); }
    }
}