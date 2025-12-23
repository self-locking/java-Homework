package Experiment;
import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        MOVE,
        GAME_START,
        GAME_OVER,
        YOUR_TURN,
        INVALID_MOVE,
        PLAYER_JOINED,
        SIDE_ASSIGNMENT,
        CHAT // 新增聊天类型
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