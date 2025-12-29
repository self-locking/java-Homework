package Experiment;
import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fromPosition;
    private String toPosition;
    private Piece capturedPiece;  // 被吃掉的棋子（如果有）
    private int movingSide;       // 移动方（1=红，-1=黑）

    public Move(String from, String to, Piece captured, int side) {
        this.fromPosition = from;
        this.toPosition = to;
        this.capturedPiece = captured;
        this.movingSide = side;
    }

    public String getFromPosition() { return fromPosition; }
    public String getToPosition() { return toPosition; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public int getMovingSide() { return movingSide; }
}