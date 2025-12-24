package Experiment;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ChessBoard extends Canvas{
    boolean isNetworkGame = false;  // 是否是网络游戏
    int mySide = 0;                 // 我的阵营 (1=红, -1=黑)
    NetworkManager networkManager;   // 网络管理器
    final int boardX = 100;
    final int boardY = 60;
    final int distance = 90; // 格子间距
    Piece[][] pieces = new Piece[9][10];
    private Piece selectedPiece = null;
    private String selectedPosition = null;
    private int round = 1;  // 回合变化
    private BufferedImage boardImage;
    private boolean gameOver = false; // 游戏是否结束
    private boolean eated = false;
    private boolean isReady = false;
    private JTextArea chatArea;
    private JFrame gameFrame; // 保存frame引用

    // 普通构造
    public ChessBoard(){
        setPreferredSize(new Dimension(1100, 1100));
        setBackground(new Color(210, 180, 140));
        initializePieces();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameOver) {
                    handleMouseClick(e);
                }
            }
        });
        createBoardImage();
    }

    // 网络游戏构造函数
    public ChessBoard(int playerSide, NetworkManager netManager) {
        this();  // 调用原来的构造函数
        this.isNetworkGame = true;
        this.mySide = playerSide;
        this.networkManager = netManager;
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        g2.drawImage(boardImage, 0, 0, this);
        drawPieces(g2);
        drawText(g2);

        if (!gameOver && isInCheck(round)) {
            drawCheckWarning(g2);
        }
        if (!gameOver && eated) {
            drawEat(g2);
            eated = false;
        }

    }

    private void drawBoard(Graphics2D g2) {
        g2.setColor(new Color(120, 70, 20));
        g2.drawRect(boardX, boardY, 8*distance, 9*distance);
        for(int i = 1; i < 8; i++) {
            g2.drawLine(boardX+i*distance, boardY,boardX+i*distance, boardY+4*distance);
            g2.drawLine(boardX+i*distance, boardY+5*distance,boardX+i*distance, boardY+9*distance);
        }
        for(int i = 1; i < 9; i++) {
            g2.drawLine(boardX, boardY+i*distance,boardX+8*distance, boardY+i*distance);
        }
        g2.drawLine(boardX+3*distance, boardY,boardX+5*distance, boardY+2*distance);
        g2.drawLine(boardX+3*distance, boardY+2*distance,boardX+5*distance, boardY);
        g2.drawLine(boardX+3*distance, boardY+7*distance,boardX+5*distance, boardY+9*distance);
        g2.drawLine(boardX+3*distance, boardY+9*distance,boardX+5*distance, boardY+7*distance);

        g2.setColor(new Color(40,40,40));
        g2.setFont(new Font("宋体",Font.BOLD, 60));
        g2.drawString("楚河     汉界", boardX+160, boardY+430);
        g2.setFont(new Font("宋体",Font.BOLD, 20));

        String[] num1 = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
        for(int i = 0; i < 9; i++) {
            g2.drawString(num1[i], boardX-6+i*distance, boardY-5);
            g2.drawString(num1[i], boardX-6+i*distance, boardY-5+840);
        }
        String[] num2 = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for(int i = 0; i < 10; i++) {
            g2.drawString(num2[i], boardX-30, boardY+5+i*distance);
            g2.drawString(num2[i], boardX+730, boardY+5+i*distance);
        }
    }

    private void drawPieces(Graphics2D g2) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (pieces[i][j] != null) pieces[i][j].paint(g2);
            }
        }
    }

    private void drawText(Graphics2D g2) {
        if (gameOver) {
            g2.setFont(new Font("宋体",Font.BOLD, 40));
            g2.setColor(Color.RED);
            g2.drawString("游戏结束!", 880, 200);
        } else {
            String roundName = (round == 1) ? "红" : "黑";
            g2.setFont(new Font("宋体",Font.BOLD, 30));
            g2.setColor(Color.BLACK);
            g2.drawString("当前回合："+roundName, 900, 200);
        }
    }

    // 启动游戏
    public void resetAndStart(int side) {
        this.isReady = true;    // 激活棋盘
        this.round = 1;         // 重置为红方先走
        this.gameOver = false;  // 重置游戏状态
        this.setMySide(side);   // 设置当前玩家的阵营
        initializePieces();     // 重新初始化棋子位置（可选，看你是否需要开局重置）
        repaint();              // 刷新显示
    }

    // 提醒"将军"
    private void drawCheckWarning(Graphics2D g2) {
        g2.setColor(new Color(180, 30, 30));
        g2.setFont(new Font("宋体",Font.BOLD, 60));
        g2.drawString("将军！", 920, 400);
    }

    // 提醒"吃"
    private void drawEat(Graphics2D g2) {
        g2.setColor(new Color(180, 30, 30));
        g2.setFont(new Font("宋体",Font.BOLD, 60));
        g2.drawString("吃！", 920, 300);
    }

    private void initializePieces() {
        // 红方
        pieces[0][9] = new Piece("A9", 0, "车");
        pieces[1][9] = new Piece("B9", 0, "马");
        pieces[2][9] = new Piece("C9", 0, "相");
        pieces[3][9] = new Piece("D9", 0, "士");
        pieces[4][9] = new Piece("E9", 0, "帅");
        pieces[5][9] = new Piece("F9", 0, "士");
        pieces[6][9] = new Piece("G9", 0, "相");
        pieces[7][9] = new Piece("H9", 0, "马");
        pieces[8][9] = new Piece("I9", 0, "车");
        pieces[1][7] = new Piece("B7", 0, "炮");
        pieces[7][7] = new Piece("H7", 0, "炮");
        pieces[0][6] = new Piece("A6", 0, "兵");
        pieces[2][6] = new Piece("C6", 0, "兵");
        pieces[4][6] = new Piece("E6", 0, "兵");
        pieces[6][6] = new Piece("G6", 0, "兵");
        pieces[8][6] = new Piece("I6", 0, "兵");
        // 黑方
        pieces[0][0] = new Piece("A0", 1, "車");
        pieces[1][0] = new Piece("B0", 1, "馬");
        pieces[2][0] = new Piece("C0", 1, "象");
        pieces[3][0] = new Piece("D0", 1, "仕");
        pieces[4][0] = new Piece("E0", 1, "将");
        pieces[5][0] = new Piece("F0", 1, "仕");
        pieces[6][0] = new Piece("G0", 1, "象");
        pieces[7][0] = new Piece("H0", 1, "馬");
        pieces[8][0] = new Piece("I0", 1, "車");
        pieces[1][2] = new Piece("B2", 1, "砲");
        pieces[7][2] = new Piece("H2", 1, "砲");
        pieces[0][3] = new Piece("A3", 1, "卒");
        pieces[2][3] = new Piece("C3", 1, "卒");
        pieces[4][3] = new Piece("E3", 1, "卒");
        pieces[6][3] = new Piece("G3", 1, "卒");
        pieces[8][3] = new Piece("I3", 1, "卒");
    }

    // 检测点击
    private void handleMouseClick(MouseEvent e) {
        if (!isReady) {
            return;
        }

        if (isNetworkGame && round != mySide) {
            JOptionPane.showMessageDialog(this, "不是你的回合", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clickX = e.getX();
        int clickY = e.getY();
        String position = getBoardPosition(clickX, clickY);

        if (position != null) {
            Piece clickedPiece = getPieceAtPosition(position);

            if (selectedPiece == null) {
                if (clickedPiece != null && clickedPiece.getType() == round) {
                    selectedPiece = clickedPiece;
                    selectedPosition = position;
                    selectedPiece.setSelected(true);
                    repaint();
                }
            } else {
                String fromPos = selectedPosition;
                boolean moved = movePiece(selectedPiece, position);

                selectedPiece.setSelected(false);
                // 暂存当前选中的棋子信息用于网络同步，然后清空引用
                Piece lastSelected = selectedPiece;
                selectedPiece = null;
                selectedPosition = null;

                if (moved) {
                    // 修改点：使用统一的 sendMessage 接口
                    if (isNetworkGame && networkManager != null) {
                        GameMessage moveMsg = new GameMessage(
                                GameMessage.MessageType.MOVE,
                                fromPos,
                                position,
                                mySide
                        );
                        networkManager.sendMessage(moveMsg);
                    }

                    round = -round;
                    checkWinCondition(); // 每次移动后检测胜负
                }
                repaint();
            }
        }
    }

    // 获取点击位置
    private String getBoardPosition(int x, int y) {
        float relativeX = x - boardX;
        float relativeY = y - boardY;

        int col = Math.round(relativeX / distance);
        int row = Math.round(relativeY / distance);

        if (col < 0 || col > 8 || row < 0 || row > 9) {
            return null;
        }

        char colChar = (char)('A' + col);
        String rowStr = String.valueOf(row);

        return colChar + rowStr;
    }

    private Piece getPieceAtPosition(String position) {
        int col = position.charAt(0) - 'A';
        int row = Integer.parseInt(position.substring(1));
        return pieces[col][row];
    }

    // 远程结束游戏的方法
    public void endGameRemote(String msg) {
        if (this.gameOver) return; // 如果本地已经判定结束，不再处理
        this.gameOver = true;
        repaint(); // 确保 UI 更新显示"游戏结束"
        JOptionPane.showMessageDialog(this, msg);
    }

    // 判断胜负
    private void checkWinCondition() {
        // 如果游戏已经标记为结束，直接跳过，防止重复弹窗
        if (this.gameOver) {
            return;
        }

        boolean redKingAlive = false;
        boolean blackKingAlive = false;

        // 遍历棋盘寻找将和帅
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (pieces[i][j] != null) {
                    if (pieces[i][j].getName().equals("帅")) redKingAlive = true;
                    if (pieces[i][j].getName().equals("将")) blackKingAlive = true;
                }
            }
        }

        // 判定胜负
        if (!redKingAlive || !blackKingAlive) {
            this.gameOver = true; // 立即加锁
            String winMsg = !redKingAlive ? "黑方获胜！" : "红方获胜！";

            // 关键修复：只有当是"我的回合"且我赢了时，才通知对方
            // 这样可以避免接收方在处理 MOVE 消息时又反过来发送一遍 GAME_OVER
            if (isNetworkGame && networkManager != null && round == -mySide) {
                GameMessage msg = new GameMessage(GameMessage.MessageType.GAME_OVER);
                msg.setMessage(winMsg);
                networkManager.sendMessage(msg);
            }

            JOptionPane.showMessageDialog(this, winMsg);
        }
    }

    // 检测是否可以将军
    private boolean isInCheck(int side) {
        int generalCol = -1, generalRow = -1;

        if (side == 1) { // 红方
            for (int col = 3; col <= 5; col++) {
                for (int row = 7; row <= 9; row++) {
                    if (pieces[col][row] != null) {
                        if (pieces[col][row].getName().equals("帅")){
                            generalRow = row;
                            generalCol = col;
                            break;
                        }
                    }
                }
            }
        }
        else { // 黑方
            for (int col = 3; col <= 5; col++) {
                for (int row = 0; row <= 2; row++) {
                    if (pieces[col][row] != null) {
                        if (pieces[col][row].getName().equals("将")){
                            generalRow = row;
                            generalCol = col;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (pieces[i][j] != null && pieces[i][j].getType() == -side) {
                    if (canPieceAttack(pieces[i][j], i, j, generalCol, generalRow)) return true;
                }
            }
        }
        return false;
    }

    // 同上辅助检测
    private boolean canPieceAttack(Piece piece, int fromCol, int fromRow, int targetCol, int targetRow) {
        int colDiff = targetCol - fromCol;
        int rowDiff = targetRow - fromRow;

        if (colDiff == 0 && rowDiff == 0) {
            return false;
        }

        switch (piece.getName()) {
            case "车":
            case "車":
                // 直线移动
                if (colDiff != 0 && rowDiff != 0) {
                    return false;
                }
                if (colDiff == 0) {
                    int startRow = Math.min(fromRow, targetRow);
                    int endRow = Math.max(fromRow, targetRow);
                    for (int r = startRow + 1; r < endRow; r++) {
                        if (pieces[fromCol][r] != null) {
                            return false;
                        }
                    }
                } else {
                    int startCol = Math.min(fromCol, targetCol);
                    int endCol = Math.max(fromCol, targetCol);
                    for (int c = startCol + 1; c < endCol; c++) {
                        if (pieces[c][fromRow] != null) {
                            return false;
                        }
                    }
                }
                return true;

            case "马":
            case "馬":
                // 日字移动
                if ((Math.abs(colDiff) == 2 && Math.abs(rowDiff) == 1) ||
                        (Math.abs(colDiff) == 1 && Math.abs(rowDiff) == 2)) {
                    int blockCol = fromCol;
                    int blockRow = fromRow;
                    if (Math.abs(colDiff) == 2) {
                        blockCol = fromCol + (colDiff > 0 ? 1 : -1);
                    } else {
                        blockRow = fromRow + (rowDiff > 0 ? 1 : -1);
                    }
                    return pieces[blockCol][blockRow] == null;
                }
                return false;

            case "炮":
            case "砲":
                // 吃子需要隔一个棋子
                if (colDiff != 0 && rowDiff != 0) {
                    return false;
                }
                int piecesBetween = 0;
                if (colDiff == 0) {
                    int startRow = Math.min(fromRow, targetRow);
                    int endRow = Math.max(fromRow, targetRow);
                    for (int r = startRow + 1; r < endRow; r++) {
                        if (pieces[fromCol][r] != null) {
                            piecesBetween++;
                        }
                    }
                } else {
                    int startCol = Math.min(fromCol, targetCol);
                    int endCol = Math.max(fromCol, targetCol);
                    for (int c = startCol + 1; c < endCol; c++) {
                        if (pieces[c][fromRow] != null) {
                            piecesBetween++;
                        }
                    }
                }
                // 炮攻击将军时，中间必须有一个棋子
                return piecesBetween == 1;

            case "兵":
                // 红兵向前（row减小），过河后可以左右
                if (fromRow > 4) { // 未过河
                    return false;
                } else { // 已过河
                    return (rowDiff == -1 && colDiff == 0) || (rowDiff == 0 && Math.abs(colDiff) == 1);
                }

            case "卒":
                // 黑卒向前（row增大），过河后可以左右
                if (fromRow < 5) { // 未过河
                    return false;
                } else { // 已过河
                    return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && Math.abs(colDiff) == 1);
                }

            default:
                return false;
        }
    }

    private Boolean movePiece(Piece selectedPiece, String position) {
        int oldCol = selectedPosition.charAt(0) - 'A';
        int oldRow = Integer.parseInt(selectedPosition.substring(1));
        int col = position.charAt(0) - 'A';
        int row = Integer.parseInt(position.substring(1));

        Piece targetPiece = pieces[col][row];
        if (targetPiece != null && targetPiece.getType() == selectedPiece.getType()) {
            return false;
        }

        int colDiff = col - oldCol;
        int rowDiff = row - oldRow;

        if (colDiff == 0 && rowDiff == 0) {return false;}

        switch (selectedPiece.getName()) {
            case "兵":
                if (oldRow > 4) {
                    if (rowDiff == -1 && colDiff == 0) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                } else {
                    if ((rowDiff == -1 && colDiff == 0) ||
                            (rowDiff == 0 && Math.abs(colDiff) == 1)) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                }
                break;

            case "卒":
                if (oldRow < 5) {
                    if (rowDiff == 1 && colDiff == 0) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                } else {
                    if ((rowDiff == 1 && colDiff == 0) ||
                            (rowDiff == 0 && Math.abs(colDiff) == 1)) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                }
                break;

            case "马":
            case "馬":
                if ((Math.abs(colDiff) == 2 && Math.abs(rowDiff) == 1) ||
                        (Math.abs(colDiff) == 1 && Math.abs(rowDiff) == 2)) {

                    int blockCol = oldCol;
                    int blockRow = oldRow;

                    if (Math.abs(colDiff) == 2) {
                        blockCol = oldCol + (colDiff > 0 ? 1 : -1);
                    } else {
                        blockRow = oldRow + (rowDiff > 0 ? 1 : -1);
                    }

                    if (pieces[blockCol][blockRow] == null) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                }
                break;

            case "炮":
            case "砲":
                if (colDiff != 0 && rowDiff != 0) {
                    break;
                }

                int piecesBetween = 0;

                if (colDiff == 0) {
                    int startRow = Math.min(oldRow, row);
                    int endRow = Math.max(oldRow, row);
                    for (int r = startRow + 1; r < endRow; r++) {
                        if (pieces[oldCol][r] != null) {
                            piecesBetween++;
                        }
                    }
                } else {
                    int startCol = Math.min(oldCol, col);
                    int endCol = Math.max(oldCol, col);
                    for (int c = startCol + 1; c < endCol; c++) {
                        if (pieces[c][oldRow] != null) {
                            piecesBetween++;
                        }
                    }
                }

                if ((targetPiece == null && piecesBetween == 0) ||
                        (targetPiece != null && piecesBetween == 1)) {
                    if (pieces[col][row] != null) {eated = true;}
                    pieces[col][row] = pieces[oldCol][oldRow];
                    pieces[oldCol][oldRow] = null;
                    selectedPiece.setPosition(position);
                    return true;
                }
                break;

            case "车":
            case "車":
                if (colDiff != 0 && rowDiff != 0) {
                    break;
                }

                boolean pathClear = true;
                if (colDiff == 0) {
                    int startRow = Math.min(oldRow, row);
                    int endRow = Math.max(oldRow, row);
                    for (int r = startRow + 1; r < endRow; r++) {
                        if (pieces[oldCol][r] != null) {
                            pathClear = false;
                            break;
                        }
                    }
                } else {
                    int startCol = Math.min(oldCol, col);
                    int endCol = Math.max(oldCol, col);
                    for (int c = startCol + 1; c < endCol; c++) {
                        if (pieces[c][oldRow] != null) {
                            pathClear = false;
                            break;
                        }
                    }
                }

                if (pathClear) {
                    if (pieces[col][row] != null) {eated = true;}
                    pieces[col][row] = pieces[oldCol][oldRow];
                    pieces[oldCol][oldRow] = null;
                    selectedPiece.setPosition(position);
                    return true;
                }
                break;

            case "相":
            case "象":
                if (Math.abs(colDiff) == 2 && Math.abs(rowDiff) == 2) {

                    if (selectedPiece.getName().equals("相")) {
                        if (row < 5) {
                            break;
                        }
                    } else {
                        if (row > 4) {
                            break;
                        }
                    }

                    int centerCol = oldCol + colDiff / 2;
                    int centerRow = oldRow + rowDiff / 2;

                    if (pieces[centerCol][centerRow] == null) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                }
                break;

            case "士":
            case "仕":
                if (Math.abs(colDiff) == 1 && Math.abs(rowDiff) == 1) {

                    if (selectedPiece.getName().equals("士")) {
                        if (col >= 3 && col <= 5 && row >= 7 && row <= 9) {
                            if (pieces[col][row] != null) {eated = true;}
                            pieces[col][row] = pieces[oldCol][oldRow];
                            pieces[oldCol][oldRow] = null;
                            selectedPiece.setPosition(position);
                            return true;
                        }
                    } else {
                        if (col >= 3 && col <= 5 && row >= 0 && row <= 2) {
                            if (pieces[col][row] != null) {eated = true;}
                            pieces[col][row] = pieces[oldCol][oldRow];
                            pieces[oldCol][oldRow] = null;
                            selectedPiece.setPosition(position);
                            return true;
                        }
                    }
                }
                break;

            case "帅":
            case "将":
                boolean inPalace = false;
                if (selectedPiece.getName().equals("帅")) {
                    inPalace = (col >= 3 && col <= 5 && row >= 7 && row <= 9);
                } else {
                    inPalace = (col >= 3 && col <= 5 && row >= 0 && row <= 2);
                }

                if (!inPalace) {
                    break;
                }

                if ((Math.abs(colDiff) == 1 && rowDiff == 0) ||
                        (colDiff == 0 && Math.abs(rowDiff) == 1)) {

                    if (isValidGeneralMove(col, row, selectedPiece)) {
                        if (pieces[col][row] != null) {eated = true;}
                        pieces[col][row] = pieces[oldCol][oldRow];
                        pieces[oldCol][oldRow] = null;
                        selectedPiece.setPosition(position);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    // 规定将帅不能见面
    private boolean isValidGeneralMove(int targetCol, int targetRow, Piece movingGeneral) {
        int oldCol = selectedPosition.charAt(0) - 'A';
        int oldRow = Integer.parseInt(selectedPosition.substring(1));

        Piece tempTarget = pieces[targetCol][targetRow];
        pieces[targetCol][targetRow] = movingGeneral;
        pieces[oldCol][oldRow] = null;

        int redGeneralCol = -1, redGeneralRow = -1;
        int blackGeneralCol = -1, blackGeneralRow = -1;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 10; j++) {
                if (pieces[i][j] != null) {
                    if (pieces[i][j].getName().equals("帅")) {
                        redGeneralCol = i;
                        redGeneralRow = j;
                    } else if (pieces[i][j].getName().equals("将")) {
                        blackGeneralCol = i;
                        blackGeneralRow = j;
                    }
                }
            }
        }

        boolean valid = true;

        if (redGeneralCol == blackGeneralCol) {
            int startRow = Math.min(redGeneralRow, blackGeneralRow);
            int endRow = Math.max(redGeneralRow, blackGeneralRow);
            boolean hasBlocker = false;

            for (int r = startRow + 1; r < endRow; r++) {
                if (pieces[redGeneralCol][r] != null) {
                    hasBlocker = true;
                    break;
                }
            }

            if (!hasBlocker) {
                valid = false;
            }
        }

        // 检测完后交换回去
        pieces[oldCol][oldRow] = movingGeneral;
        pieces[targetCol][targetRow] = tempTarget;

        return valid;
    }

    private void createBoardImage() {
        boardImage = new BufferedImage(900, 900, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = boardImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawBoard(g2);

        g2.dispose();
    }

    // 接收对手的移动
    public void receiveOpponentMove(String fromPos, String toPos) {
        Piece piece = getPieceAtPosition(fromPos);
        if (piece != null) {
            // 关键：模拟选中的上下文，否则 movePiece 内部会报空指针
            this.selectedPosition = fromPos;

            // 执行移动逻辑
            boolean moved = movePiece(piece, toPos);

            if (moved) {
                round = -round; // 切换到我的回合
                checkWinCondition(); // 对方走完也可能导致我输掉，需要检测
            }

            this.selectedPosition = null;
            repaint();
        }
    }

    // 设置游戏窗口引用
    public void setGameFrame(JFrame frame) {
        this.gameFrame = frame;
    }

    // 确保阵营安全
    public void setMySide(int side) {
        this.mySide = side;
        // 使用保存的frame引用直接更新标题
        if (gameFrame != null) {
            String role = (side == 1) ? "红方" : "黑方";
            EventQueue.invokeLater(() -> {
                gameFrame.setTitle("中国象棋联网版 - 你是：" + role);
            });
        }
    }

    // 设置聊天区
    public void setChatArea(JTextArea chatArea) {
        this.chatArea = chatArea;
    }

    // 聊天功能
    public void appendChat(String text, boolean isMe) {
        if (chatArea != null) {
            String name = isMe ? "【我】" : "【对方】";
            chatArea.append(name + ": " + text + "\n");
            // 自动滚动
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }
}