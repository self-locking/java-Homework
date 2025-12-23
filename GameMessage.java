package Experiment;
import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        MOVE,           // 发送移动
        GAME_START,     // 游戏开始
        GAME_OVER,      // 游戏结束
        YOUR_TURN,      // 轮到你了
        INVALID_MOVE,   // 无效移动
        PLAYER_JOINED,   // 玩家加入
        SIDE_ASSIGNMENT // 用于告知客户端分配到的阵营
    }

    private MessageType type;
    private String fromPosition;  // 移动起点
    private String toPosition;    // 移动终点
    private String message;       // 附加信息
    private int playerSide;       // 1=红方, -1=黑方

    // Constructors
    public GameMessage(MessageType type) {
        this.type = type;
    }

    public GameMessage(MessageType type, String from, String to, int side) {
        this.type = type;
        this.fromPosition = from;
        this.toPosition = to;
        this.playerSide = side;
    }

    // Getters and Setters
    public MessageType getType() { return type; }
    public String getFromPosition() { return fromPosition; }
    public String getToPosition() { return toPosition; }
    public String getMessage() { return message; }
    public int getPlayerSide() { return playerSide; }
    public void setPlayerSide(int side) { this.playerSide = side; }

    public void setMessage(String message) { this.message = message; }
}