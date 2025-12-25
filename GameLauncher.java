package Experiment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private static void initUI(JFrame frame, ChessBoard board, VoiceManager voiceManager) {
        // 右侧面板 - 整体布局
        JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
        sidePanel.setPreferredSize(new Dimension(300, 600));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 顶部控制面板(语音+重新开始)
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // 语音通话区域
        JPanel voicePanel = new JPanel(new BorderLayout(5, 5));
        voicePanel.setBorder(BorderFactory.createTitledBorder("语音通话"));
        voicePanel.setPreferredSize(new Dimension(280, 100));

        // 创建自定义的语音按钮
        JButton voiceButton = new JButton("按住说话");
        voiceButton.setPreferredSize(new Dimension(260, 50));
        voiceButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        voiceButton.setBackground(new Color(76, 175, 80));
        voiceButton.setForeground(Color.WHITE);
        voiceButton.setFocusPainted(false);
        voiceButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 142, 60), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // 鼠标按下 - 开始说话
        voiceButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (voiceManager != null) {
                    voiceManager.startTransmitting();
                    voiceButton.setBackground(new Color(244, 67, 54));
                    voiceButton.setText("正在说话...");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (voiceManager != null) {
                    voiceManager.stopTransmitting();
                    voiceButton.setBackground(new Color(76, 175, 80));
                    voiceButton.setText("按住说话");
                }
            }
        });

        voicePanel.add(voiceButton, BorderLayout.CENTER);

        // 重新开始按钮区域
        JPanel restartPanel = new JPanel(new BorderLayout(5, 5));
        restartPanel.setBorder(BorderFactory.createTitledBorder("游戏控制"));
        restartPanel.setPreferredSize(new Dimension(280, 70));

        JButton restartButton = new JButton("重新开始");
        restartButton.setPreferredSize(new Dimension(260, 35));
        restartButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        restartButton.setBackground(new Color(33, 150, 243));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 118, 210), 2),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        restartButton.addActionListener(e -> {
            board.requestRestart();
        });

        restartPanel.add(restartButton, BorderLayout.CENTER);

        // 将语音和重新开始按钮放入顶部面板
        topPanel.add(voicePanel, BorderLayout.NORTH);
        topPanel.add(restartPanel, BorderLayout.SOUTH);

        // 文字聊天区域
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createTitledBorder("文字聊天 (回车发送)"));

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        board.setChatArea(chatArea);

        JTextField chatInput = new JTextField();
        chatInput.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        chatInput.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty() && board.networkManager != null) {
                GameMessage chatMsg = new GameMessage(GameMessage.MessageType.CHAT);
                chatMsg.setMessage(text);
                board.networkManager.sendMessage(chatMsg);
                board.appendChat(text, true);
                chatInput.setText("");
            }
        });

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(280, 350));

        chatPanel.add(chatScroll, BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);

        // 组装右侧面板
        sidePanel.add(topPanel, BorderLayout.NORTH);
        sidePanel.add(chatPanel, BorderLayout.CENTER);

        // 主窗口布局
        frame.setLayout(new BorderLayout());
        frame.add(board, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 关闭窗口时清理语音资源
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (voiceManager != null) {
                    voiceManager.close();
                }
            }
        });

        frame.setVisible(true);
    }

    // 启动服务器
    private static void startAsServer() {
        try {
            JFrame frame = new JFrame("等待玩家连接...");
            ChessBoard board = new ChessBoard();

            // 先用null初始化UI，等连接后再设置voiceManager
            board.setGameFrame(frame);

            ServerSocket serverSocket = new ServerSocket(1234);

            // 为语音通信创建单独的端口
            ServerSocket voiceServerSocket = new ServerSocket(1235);

            Socket gameSocket = serverSocket.accept();
            NetworkManager nm = new NetworkManager(gameSocket, board);

            // 等待语音连接
            Socket voiceSocket = voiceServerSocket.accept();
            VoiceManager voiceManager = new VoiceManager(voiceSocket);

            // 现在初始化UI
            initUI(frame, board, voiceManager);

            int serverSide = Math.random() > 0.5 ? 1 : -1;
            board.isNetworkGame = true;
            board.networkManager = nm;
            board.resetAndStart(serverSide);

            GameMessage msg = new GameMessage(GameMessage.MessageType.SIDE_ASSIGNMENT);
            msg.setPlayerSide(-serverSide);
            nm.sendMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 启动客户端
    private static void startAsClient() {
        String ip = JOptionPane.showInputDialog("请输入服务器IP:", "localhost");
        if (ip == null) return;
        try {
            JFrame frame = new JFrame("正在连接服务器...");
            ChessBoard board = new ChessBoard();

            board.setGameFrame(frame);

            // 连接游戏socket
            Socket gameSocket = new Socket(ip, 1234);
            NetworkManager nm = new NetworkManager(gameSocket, board);

            // 连接语音socket
            Socket voiceSocket = new Socket(ip, 1235);
            VoiceManager voiceManager = new VoiceManager(voiceSocket);

            // 初始化UI
            initUI(frame, board, voiceManager);

            board.isNetworkGame = true;
            board.networkManager = nm;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "连接失败: " + e.getMessage());
        }
    }
}