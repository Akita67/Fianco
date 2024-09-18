package io.github.fianco;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaBot extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private boolean isBlackMax;

    public AlphaBetaBot(boolean isBlack, int[][] board, int depthLimit) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        if(isBlack){
            isBlackMax=true;
        }else{
            isBlackMax=false;
        }
        Move bestMove = alphaBeta(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
        System.out.println("I found the best move");
        System.out.println(bestMove.startRow + " " + bestMove.startCol);

        if (bestMove != null) {
            // Execute the move
            if (bestMove.isAttackMove) {
                boardScreen.botAttackStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            } else {
                boardScreen.botMoveStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            }
        }
    }

    // Alpha-Beta pruning algorithm to find the best move
    private Move alphaBeta(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || isGameOver(board)) {
            int evaluation = evaluateBoard(board); // Evaluate the board at the leaf node
            return new Move(-1,-1,-1,-1,false,evaluation); // Return the evaluation wrapped in a Move object
        }

        List<Move> moves = getAllPossibleMoves(boardScreen, maximizingPlayer);
        // Sort the list by isAttackMove first (true before false)
        moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));
        if(moves.get(0).isAttackMove){
            // remove all the moves with isAttackMove false
            moves.removeIf(move -> !move.isAttackMove);
        }

        for(Move move : moves){
            System.out.println(move.startRow + " " + move.startCol);
        }
        System.out.println();


        Move bestMove = null;

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (Move move : moves) {
                // Simulate the move
                makeMove(board, move, isBlack);
                Move resultMove = alphaBeta(boardScreen, board, depth - 1, alpha, beta, false);

                if (resultMove.evaluation > maxEval) {
                    maxEval = resultMove.evaluation;
                    bestMove = move;
                    bestMove.evaluation = maxEval;
                }

                // Undo the move
                undoMove(board, move, isBlack);

                alpha = Math.max(alpha, resultMove.evaluation);
                if (beta <= alpha) {
                    break; // Beta cut-off
                }
            }

        } else {
            int minEval = Integer.MAX_VALUE;

            for (Move move : moves) {
                // Simulate the move
                makeMove(board, move, !isBlack);
                Move resultMove = alphaBeta(boardScreen, board, depth - 1, alpha, beta, true);

                if (resultMove.evaluation < minEval) {
                    minEval = resultMove.evaluation;
                    bestMove = move;
                    bestMove.evaluation = minEval;
                }

                // Undo the move
                undoMove(board, move, !isBlack);

                beta = Math.min(beta, resultMove.evaluation);
                if (beta <= alpha) {
                    break; // Alpha cut-off
                }
            }
        }

        return bestMove;
    }
    public List<Move> getAllPossibleMoves(BoardScreen boardScreen, boolean maximization){
        List<Move> moves = new ArrayList<>();
        if(maximization){ // Bot moves
            for (int row = 0; row < boardScreen.gridSize; row++) {
                for (int col = 0; col < boardScreen.gridSize; col++) {
                    // Get all valid moves for this piece
                    if (isBotPiece(row, col)) {
                        List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, isBlack);
                        moves.addAll(pieceMoves); // Add to the list of moves
                    }
                }
            }
        }else{ // Opponent moves
            for (int row = 0; row < boardScreen.gridSize; row++) {
                for (int col = 0; col < boardScreen.gridSize; col++) {
                    // Get all valid moves for this piece
                    if (isOpponentPiece(row, col)){
                        List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, !isBlack);
                        moves.addAll(pieceMoves); // Add to the list of moves
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
            addMoveIfValid(boardScreen, validMoves, row, col, row - 1, col); // Down
        if(!playAsBlack && row<8)
            addMoveIfValid(boardScreen, validMoves, row, col, row + 1, col); // Forward
        if(col>0)
            addMoveIfValid(boardScreen, validMoves, row, col, row, col - 1); // Left
        if(col<8)
            addMoveIfValid(boardScreen, validMoves, row, col, row, col + 1); // Right

        // Check for attack moves
        AddAttackToList(boardScreen.checkForCapturesSpec(board,!playAsBlack,row,col),validMoves);

        return validMoves;
    }
    private void addMoveIfValid(BoardScreen boardScreen, List<Move> validMoves, int startRow, int startCol, int endRow, int endCol) {
        if (board[endRow][endCol] == 0) {
            validMoves.add(new Move(startRow, startCol, endRow, endCol, false, 0));
        }
    }
    private void AddAttackToList(List<Move> attacks, List<Move> validMoves) {
        for(Move attack : attacks){
            validMoves.add(new Move(attack.startRow, attack.startCol, attack.endRow, attack.endCol, true, 0));
        }
    }
    public void makeMove(int[][] board, Move move, boolean isBlack){
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
    public void undoMove(int[][] board, Move move, boolean isBlackMax){
        if(move.isAttackMove()){
            if(isBlackMax){
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

    // Method to evaluate the board: positive for bot, negative for opponent
    private int evaluateBoard(int[][] board) {
        int score = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (isBotPiece(row, col)) {
                    score += 10 ; // Example add: + (7 - row);
                } else if (isOpponentPiece(row, col)) {
                    score -= 10 ; // Example add: + row;
                }
            }
        }
        return score;
    }

    // Check if the stone belongs to the bot
    private boolean isBotPiece(int row, int col) {
        return (isBlack && board[row][col] == 2) || (!isBlack && board[row][col] == 1);
    }

    // Check if the stone belongs to the opponent
    private boolean isOpponentPiece(int row, int col) {
        return (!isBlack && board[row][col] == 2) || (isBlack && board[row][col] == 1);
    }

    // Method to check if the game is over
    private boolean isGameOver(int[][] board) {
        this.board = board;
        boolean blackWins = false;
        boolean whiteWins = false;

        // Check if a black stone has reached row 0 (top)
        for (int i = 0; i < board[0].length; i++) {
            if (board[0][i] == 2) {
                blackWins = true;
                return true;
            }
        }

        // Check if a white stone has reached row 8 (bottom)
        for (int i = 0; i < board[8].length; i++) {
            if (board[8][i] == 1) {
                whiteWins = true;
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
                else if(countW!=0 && countB!=0)
                    break;
            }
        }
        if(countW==0){
            blackWins = true;
            return true;
        } else if (countB==0) {
            whiteWins = true;
            return true;
        }
        return false;
    }
    public void changeSide(){
        isBlack = !isBlack;
    }
}
