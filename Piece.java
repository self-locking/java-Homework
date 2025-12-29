package Experiment;
import java.awt.*;

public class Piece extends Canvas{
    private String POSITION;        // 棋子逻辑位置
    private int x, y;               // 棋子像素位置
    final private Color pieceColor; // 棋子颜色
    private final int type;         // 棋子颜色类型
    final private String name;      // 用于棋子的绘画和像素坐标计算
    private boolean selected;       // 是否被选中
    final int distance = 90;
    final int X=100, Y=60;
    final int R = 40;

    public Piece(String position, int color, String str3) {
        POSITION = position;
        x = X + (position.charAt(0) - 'A') * distance;
        y = Y + (Integer.parseInt(position.substring(1))) * distance;
        selected = false;
        if(color == 0) {
            //设置红方
            pieceColor = new Color(200, 0 , 0);
            type = 1;
        }
        else {
            //设置黑方
            pieceColor = new Color(40, 40 , 40);
            type = -1;
        }
        name = str3;
    }

    // 用于悔棋时保存被吃棋子的副本
    public Piece(Piece other) {
        if (other != null) {
            this.POSITION = other.POSITION;
            this.x = other.x;
            this.y = other.y;
            this.pieceColor = other.pieceColor;
            this.type = other.type;
            this.name = other.name;
            this.selected = false;
        } else {
            this.POSITION = null;
            this.x = 0;
            this.y = 0;
            this.pieceColor = null;
            this.type = 0;
            this.name = null;
            this.selected = false;
        }
    }

    // 棋子底色和轮廓绘画
    public void paint(Graphics2D g2) {
        g2.setColor(new Color(255,230,150));
        g2.fillOval(x-R, y-R,2*R,2*R);
        g2.setColor(new Color(255,240,150));
        g2.fillOval(x-(R-5), y-(R-5),2*(R-5),2*(R-5));
        g2.setColor(new Color(40,40,40));
        g2.drawOval(x-R, y-R,2*R,2*R);
        g2.drawOval(x-(R-5), y-(R-5),2*(R-5),2*(R-5));

        // 如果被选中，绘制选中特效
        if(selected) {
            Stroke oldStroke = g2.getStroke();
            g2.setColor(Color.YELLOW); // 外圈颜色，可以换成发光效果的颜色
            g2.setStroke(new BasicStroke(4)); // 边框粗细
            g2.drawOval(x-R-5, y-R-5, 2*(R+5), 2*(R+5)); // 外圈比棋子大一点
            g2.setStroke(oldStroke);
        }

        //绘制文字
        g2.setColor(pieceColor);
        g2.setFont(new Font("宋体",Font.BOLD, 50));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        int textHeight = fm.getAscent();
        g2.drawString(name, x - textWidth/2, y + textHeight/2);
    }

    // 更新棋子位置
    public void setPosition(String position) {
        POSITION = position;
        x = X + (position.charAt(0) - 'A') * distance;
        y = Y + (Integer.parseInt(position.substring(1))) * distance;
    }

    // 获取棋子类型
    public String getName() {return name;}

    // 设置是否被选中
    public void setSelected(boolean selected) {this.selected = selected;}

    // 返回棋子颜色类型
    public int getType() {return type;}
}
