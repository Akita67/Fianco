package io.github.fianco;

import java.io.Serializable;

public class TranspositionEntry implements Serializable {
    private int evaluation;
    private boolean isAttack;
    private int startRow;
    private int startCol;
    private int endRow;
    private int endCol;

    public TranspositionEntry(int evaluation, boolean isAttack, int startRow, int startCol, int endRow, int endCol) {
        this.evaluation = evaluation;
        this.isAttack = isAttack;
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
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

    @Override
    public String toString() {
        return "Evaluation: " + evaluation + ", Move: (" + startRow + ", " + startCol + ") -> (" + endRow + ", " + endCol + ")";
    }
}
