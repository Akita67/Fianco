package io.github.fianco;

import java.util.ArrayList;
import java.util.List;

public class NegaMaxBot extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private boolean blackWins = false;
    private boolean whiteWins = false;

    public NegaMaxBot(boolean isBlack, int[][] board, int depthLimit) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        Move bestMove = negamax(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
        System.out.println("best move " + bestMove.startRow + " " + bestMove.startCol + " to " + bestMove.endRow + " " + bestMove.endCol + " " + bestMove.evaluation);

        if (bestMove != null) {
            // Execute the move
            if (bestMove.isAttackMove) {
                boardScreen.botAttackStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            } else {
                boardScreen.botMoveStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            }
        }
    }
    private Move negamax(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, int max) {
        if (depth == 0 || isGameOver(board)) {
            int evaluation = evaluateBoard(board); // Evaluate the board at the leaf node
            return new Move(-1,-1,-1,-1,false,max * evaluation); // Return the evaluation wrapped in a Move object
        }

        List<Move> moves = getAllPossibleMoves(boardScreen, max == 1); // Color == 1 for current player, -1 for opponent
        moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));

        if (moves.get(0).isAttackMove) {
            moves.removeIf(move -> !move.isAttackMove);
        }

        Move bestMove = null;
        int maxEval = Integer.MIN_VALUE;

        for (Move move : moves) {
            makeMove(board, move, isBlack && max == 1 || !isBlack && max == -1); // if true move is black, else white

            Move resultMove = negamax(boardScreen, board, depth - 1, -beta, -alpha, -max); // Negate alpha, beta, and color

            if (-resultMove.evaluation > maxEval) {
                maxEval = -resultMove.evaluation;
                bestMove = move.clone();
                bestMove.evaluation = maxEval;
            }

            // Undo the move
            undoMove(board, move, isBlack && max == 1 || !isBlack && max == -1);

            alpha = Math.max(alpha, maxEval);
            if (alpha >= beta) {
                break; // Beta cut-off, no need to explore further if a winning move is found
            }
        }

        if (bestMove == null) {
            bestMove = moves.get(0); // Fallback in case no best move was found
        }
        //System.out.println(bestMove.startRow + " " + bestMove.startCol + " eval : " + bestMove.evaluation);
        return bestMove;
    }

    public List<Move> getAllPossibleMoves(BoardScreen boardScreen, boolean maximization){
        List<Move> moves = new ArrayList<>();
        if(maximization){ // Bot moves
            if(isBlack){
                for (int row = 0; row < boardScreen.gridSize; row++) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isBotPiece(row, col)) {
                            List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, isBlack);
                            moves.addAll(pieceMoves); // Add to the list of moves
                        }
                    }
                }
            }else{
                for (int row = 8; row >= 0; row--) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isBotPiece(row, col)) {
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
                        if (isOpponentPiece(row, col)){
                            List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, !isBlack);
                            moves.addAll(pieceMoves); // Add to the list of moves
                        }
                    }
                }
            }else{
                for (int row = 8; row >= 0; row--) {
                    for (int col = 0; col < boardScreen.gridSize; col++) {
                        // Get all valid moves for this piece
                        if (isOpponentPiece(row, col)) {
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
    public void undoMove(int[][] board, Move move, boolean isBlacky){
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

    // Method to evaluate the board: positive for bot, negative for opponent
    private int evaluateBoard(int[][] board) {
        int score = 0;

        // Weights for each component of the evaluation function
        int distanceWeight = 5;
        int controlWeight = 3;
        int pieceValue = 15;
        for (int col = 0; col < board[8].length; col++) {
            if (board[8][col] == 1) { // If white reaches the last row
                if(isBlack)
                    return -10000; // Assign a high score for winning
                else
                    return 10000;
            }
            else if (board[0][col] == 2) { // If black reaches the last row
                if(isBlack)
                    return 10000; // Assign a high score for winning
                else
                    return -10000;
            }
        }

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (isBotPiece(row, col)) {
                    if(isBlack)
                        score += pieceValue + distanceWeight * (7 - row); // Example add: + (7 - row);
                    else
                        score += pieceValue + distanceWeight * row;

                    // 2. Control of the Board (central rows/columns are more valuable)
                    score += controlWeight * centralityValue(row, col);

                } else if (isOpponentPiece(row, col)) {
                    if(!isBlack)
                        score -= (pieceValue + distanceWeight * (7 - row)); // Example add: + row;
                    else
                        score -= (pieceValue + distanceWeight * row);

                    // 2. Control of the Board (central rows/columns are more valuable)
                    score -= controlWeight * centralityValue(row, col);
                }
            }
        }
        return score;
    }
    private int centralityValue(int row, int col) {
        // Higher values for positions near the center (4, 4)
        return 4 - Math.abs(4 - row) + 4 - Math.abs(4 - col);
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
