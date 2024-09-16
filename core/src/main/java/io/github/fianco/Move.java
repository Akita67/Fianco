package io.github.fianco;

public class Move {
    public int startRow, startCol;
    public int endRow, endCol;
    public boolean isAttackMove;
    public int evaluation;

    public Move(int startRow, int startCol, int endRow, int endCol, boolean isAttackMove) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.isAttackMove = isAttackMove;
        this.evaluation = 0;
    }
    // Constructor for evaluation (dummy move with evaluation)
    public Move(int startRow, int startCol, int endRow, int endCol, boolean isAttackMove, int evaluation) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.isAttackMove = isAttackMove;
        this.evaluation = evaluation; // Attach evaluation score
    }

    public boolean isAttackMove(){
        return this.isAttackMove;
    }
}
