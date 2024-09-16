package io.github.fianco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomBot extends Bot {
    private Random random;
    private List<Move> possibleMoves;
    private List<Move> possibleAttack;

    public RandomBot(boolean isBlack, int[][] board) {
        super(isBlack,board); // Pass the bot's color to the parent Bot class
        random = new Random();
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        //boardScreen.printBoard(board);
        // Get the list of all possible moves for this bot
        possibleAttack = new ArrayList<>();
        possibleMoves = getAllPossibleMoves(boardScreen);

        if (possibleMoves.isEmpty()) {
            // No valid moves, return or handle accordingly
            return;
        }

        // Randomly select a move from the list
        if(!possibleAttack.isEmpty()){
            System.out.println("should attack");
            Move selectedMove = possibleAttack.get(random.nextInt(possibleAttack.size()));
            boardScreen.botAttackStone(selectedMove.startRow, selectedMove.startCol, selectedMove.endRow, selectedMove.endCol);
        }else{
            Move selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            boardScreen.botMoveStone(selectedMove.startRow, selectedMove.startCol, selectedMove.endRow, selectedMove.endCol);
        }
    }

    // Method to get all possible moves for the bot
    private List<Move> getAllPossibleMoves(BoardScreen boardScreen) {
        List<Move> moves = new ArrayList<>();

        // Loop through the board to find all possible moves for the bot's pieces
        for (int row = 0; row < boardScreen.gridSize; row++) {
            for (int col = 0; col < boardScreen.gridSize; col++) {
                if (isBotPiece(row, col)) {
                    // Get all valid moves for this piece
                    List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col);
                    moves.addAll(pieceMoves); // Add to the list of moves
                }
            }
        }

        return moves;
    }

    // Check if the stone belongs to the bot
    private boolean isBotPiece(int row, int col) {
        return (isBlack && board[row][col] == 2) || (!isBlack && board[row][col] == 1);
    }

    // Get all valid moves for a specific piece (regular move or attack)
    private List<Move> getValidMovesForPiece(BoardScreen boardScreen, int row, int col) {
        List<Move> validMoves = new ArrayList<>();

        // Try moving in all four directions (forward for white,backward for black, left and right) if allowed
        if(isBlack && row>0)
            addMoveIfValid(boardScreen, validMoves, row, col, row - 1, col); // Down
        if(!isBlack && row<8)
            addMoveIfValid(boardScreen, validMoves, row, col, row + 1, col); // Forward
        if(col>0)
            addMoveIfValid(boardScreen, validMoves, row, col, row, col - 1); // Left
        if(col<8)
            addMoveIfValid(boardScreen, validMoves, row, col, row, col + 1); // Right

        // Check for attack moves
        List<Move> getAttackMoves = boardScreen.checkForCapturesSpec(board,!isBlack,row,col);
        //System.out.println(getAttackMoves.size());
        if (getAttackMoves.size()!=0) {
            possibleAttack.addAll(getAttackMoves); // Add any valid attack moves
        }

        return validMoves;
    }

    // Check if a move is valid and add it to the list
    private void addMoveIfValid(BoardScreen boardScreen, List<Move> validMoves, int startRow, int startCol, int endRow, int endCol) {
        if (board[endRow][endCol] == 0) {
            validMoves.add(new Move(startRow, startCol, endRow, endCol, false));
        }
    }
    public void changeSide(){
        isBlack = !isBlack;
    }
}
