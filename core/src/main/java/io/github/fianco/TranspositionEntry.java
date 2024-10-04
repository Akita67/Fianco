package io.github.fianco;

import java.io.Serializable;

public class TranspositionEntry implements Serializable {
    private int evaluation;
    private boolean isAttack;
    private int startRow;
    private int startCol;
    private int endRow;
    private int endCol;
    private int depth;
    private int flag; // 0 = exact, 1 = lower bound, 2 = upper bound

    public TranspositionEntry(int evaluation, boolean isAttack, int startRow, int startCol, int endRow, int endCol, int depth, int flag) {
        this.evaluation = evaluation;
        this.isAttack = isAttack;
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.depth = depth;
        this.flag = flag;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public boolean isAttack() {return isAttack;}

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndCol() {
        return endCol;
    }
    public int getDepth(){return depth;}
    public int getFlag(){return flag;}

    @Override
    public String toString() {
        return "Evaluation: " + evaluation + ", Move: (" + startRow + ", " + startCol + ") -> (" + endRow + ", " + endCol + ")";
    }
}
