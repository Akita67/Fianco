package io.github.fianco;

import java.util.*;

public class BotLogic {
    private boolean isBlack;
    private boolean blackWins = false;
    private boolean whiteWins = false;
    private int [][] board;

    public BotLogic(boolean isBlack){
        this.isBlack = isBlack;
    }
    // Method to evaluate the board: positive for bot, negative for opponent
    protected int evaluateBoard(int[][] board) {
        this.board = board;
        int score = 0;

        // Weights for each component of the evaluation function
        int distanceWeight = 1;
        int pieceValue = 10;

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (isBotPiece(row, col, board)) {
                    if(isBlack)
                        score += pieceValue + distanceWeight * (7 - row); // Example add: + (7 - row);
                    else
                        score += pieceValue + distanceWeight * row;

                } else if (isOpponentPiece(row, col, board)) {
                    if(!isBlack)
                        score -= (pieceValue + distanceWeight * (7 - row)); // Example add: + row;
                    else
                        score -= (pieceValue + distanceWeight * row);
                }
            }
        }
        return score;
    }

    // Check if the stone belongs to the bot
    private boolean isBotPiece(int row, int col, int[][]board) {
        return (isBlack && board[row][col] == 2) || (!isBlack && board[row][col] == 1);
    }

    // Check if the stone belongs to the opponent
    private boolean isOpponentPiece(int row, int col, int[][]board) {
        return (!isBlack && board[row][col] == 2) || (isBlack && board[row][col] == 1);
    }
    public void makeMove(int[][] board, Move move, boolean isBlack){
        this.board = board;
        if(move.isAttackMove()){
            if(isBlack){
                if(move.endCol>move.startCol)// Black attack to the right
                    board[move.startRow-1][move.startCol+1] = 0;
                else // Black attack to the left
                    board[move.startRow-1][move.startCol-1] = 0;
            }else{
                if(move.endCol>move.startCol)// White attack to the right
                    board[move.startRow+1][move.startCol+1] = 0;
                else // White attack to the left
                    board[move.startRow+1][move.startCol-1] = 0;
            }

        }
        board[move.endRow][move.endCol] = board[move.startRow][move.startCol];
        board[move.startRow][move.startCol] = 0;
    }
    public void undoMove(int[][] board, Move move, boolean isBlacky){
        this.board = board;
        if(move.isAttackMove()){
            if(isBlacky){
                if(move.endCol>move.startCol)// Black attacked to the right
                    board[move.startRow-1][move.startCol+1] = 1;
                else // Black attacked to the left
                    board[move.startRow-1][move.startCol-1] = 1;
            }else{
                if(move.endCol>move.startCol)// White attacked to the right
                    board[move.startRow+1][move.startCol+1] = 2;
                else // White attacked to the left
                    board[move.startRow+1][move.startCol-1] = 2;
            }

        }
        board[move.startRow][move.startCol] = board[move.endRow][move.endCol];
        board[move.endRow][move.endCol] = 0;
    }
    public List<Move> getAllPossibleMoves(int [][] board, BoardScreen boardScreen, boolean maximization){
        this.board = board;
        List<Move> moves = new ArrayList<>();
        if(maximization){ // Bot moves
            if(isBlack){
                for (int row = 0; row < boardScreen.gridSize; row++) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isBotPiece(row, col, board)) {
                            List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, isBlack);
                            moves.addAll(pieceMoves); // Add to the list of moves
                        }
                    }
                }
            }else{
                for (int row = 8; row >= 0; row--) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isBotPiece(row, col, board)) {
                            List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, isBlack);
                            moves.addAll(pieceMoves); // Add to the list of moves
                        }
                    }
                }
            }
        }else{ // Opponent moves
            if(!isBlack){
                for (int row = 0; row < boardScreen.gridSize; row++) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isOpponentPiece(row, col, board)){
                            List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, !isBlack);
                            moves.addAll(pieceMoves); // Add to the list of moves
                        }
                    }
                }
            }else{
                for (int row = 8; row >= 0; row--) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isOpponentPiece(row, col, board)) {
                            List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, !isBlack);
                            moves.addAll(pieceMoves); // Add to the list of moves
                        }
                    }
                }
            }
        }

        return moves;
    }
    private List<Move> getValidMovesForPiece(BoardScreen boardScreen, int row, int col, boolean playAsBlack) {
        List<Move> validMoves = new ArrayList<>();

        // Try moving in all four directions (forward for white,backward for black, left and right) if allowed
        if(playAsBlack && row>0)
            addMoveIfValid(validMoves, row, col, row - 1, col); // Down
        if(!playAsBlack && row<8)
            addMoveIfValid(validMoves, row, col, row + 1, col); // Forward
        if(col>0)
            addMoveIfValid(validMoves, row, col, row, col - 1); // Left
        if(col<8)
            addMoveIfValid(validMoves, row, col, row, col + 1); // Right

        // Check for attack moves
        AddAttackToList(boardScreen.checkForCapturesSpec(board,!playAsBlack,row,col),validMoves);

        return validMoves;
    }
    private void addMoveIfValid(List<Move> validMoves, int startRow, int startCol, int endRow, int endCol) {
        if (board[endRow][endCol] == 0) {
            validMoves.add(new Move(startRow, startCol, endRow, endCol, false, 0));
        }
    }
    private void AddAttackToList(List<Move> attacks, List<Move> validMoves) {
        for(Move attack : attacks){
            validMoves.add(new Move(attack.startRow, attack.startCol, attack.endRow, attack.endCol, true, 0));
        }
    }
    // Method to check if the game is over
    protected boolean isGameOver(int[][] board, BoardScreen boardScreen, int max) {
        this.board = board;
        // Check if a black stone has reached row 0 (top)
        for (int i = 0; i < board[0].length; i++) {
            if (board[0][i] == 2) {
                this.blackWins = true;
                return true;
            }
        }
        // Check if a white stone has reached row 8 (bottom)
        for (int i = 0; i < board[8].length; i++) {
            if (board[8][i] == 1) {
                this.whiteWins = true;
                return true;
            }
        }
        // Check if still are any white or black stones
        int countW = 0;
        int countB = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == 1)
                    countW++;
                else if(board[i][j] == 2)
                    countB++;
                else if (countB!=0 && countW!=0) {
                    break;
                }
            }
        }
        if(countW==0){
            this.blackWins = true;
            return true;
        } else if (countB==0) {
            this.whiteWins = true;
            return true;
        }
        List<Move> moves = getAllPossibleMoves(board, boardScreen, max == 1); // Color == 1 for current player, -1 for opponent
        if(moves.isEmpty()){
            if((max==1 && isBlack) || (max==-1 && !isBlack)){
                this.whiteWins=true;
            }else{
                this.blackWins=true;
            }
            return true;
        }

        return false;
    }

    public boolean didBlackWin(){
        return blackWins;
    }
    public boolean didWhiteWin(){
        return whiteWins;
    }
    public void setWinsToFalse(){
        blackWins = false;
        whiteWins = false;
    }
}
