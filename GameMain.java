package Experiment;

import javax.swing.*;
import java.awt.*;

public class GameMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 创建主窗口
            JFrame frame = new JFrame("中国象棋单机版");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 创建棋盘
            ChessBoard chessBoard = new ChessBoard();
            chessBoard.isNetworkGame = false; // 单机模式

            // 设置窗口布局
            frame.setLayout(new BorderLayout());

            // 棋盘放在中间
            frame.add(chessBoard, BorderLayout.CENTER);

            // 创建按钮面板，放右边
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // 垂直排列按钮
            JButton resetButton = new JButton("重新开始");

            // 给按钮绑定事件
            resetButton.addActionListener(e -> chessBoard.resetAndStart(1));

            buttonPanel.add(resetButton);
            buttonPanel.add(Box.createVerticalGlue()); // 让按钮靠上显示

            // 将按钮面板添加到右侧
            frame.add(buttonPanel, BorderLayout.EAST);

            // 设置窗口属性
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);

            // 启动游戏
            chessBoard.resetAndStart(1);
            chessBoard.setGameFrame(frame);

            frame.setVisible(true);
        });
    }
}
