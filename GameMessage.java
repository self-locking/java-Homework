package Experiment;
import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        MOVE,              // 棋子移动
        GAME_OVER,         // 游戏结束
        SIDE_ASSIGNMENT,   // 阵营分配
        CHAT               // 聊天
    }

    private MessageType type;
    private String fromPosition;
    private String toPosition;
    private String message;
    private int playerSide;

    public GameMessage(MessageType type) {
        this.type = type;
    }

    public GameMessage(MessageType type, String from, String to, int side) {
        this.type = type;
        this.fromPosition = from;
        this.toPosition = to;
        this.playerSide = side;
    }

    public MessageType getType() { return type; }
    public String getFromPosition() { return fromPosition; }
    public String getToPosition() { return toPosition; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getPlayerSide() { return playerSide; }
    public void setPlayerSide(int side) { this.playerSide = side; }
}