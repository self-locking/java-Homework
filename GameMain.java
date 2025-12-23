package Experiment;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameMain {

    public static void main(String[] args) {
        Frame f = new Frame("棋盘");
        f.pack();
        f.setBackground(new Color(210, 180, 140));
        ChessBoard chessboard = new ChessBoard();
        f.add(chessboard);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }
}
