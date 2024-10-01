package io.github.fianco;

import java.util.ArrayList;
import java.util.List;

public class NegaMaxBotTT extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private ZobristTransposition zobristTransposition; // Instance of ZobristTransposition


    public NegaMaxBotTT(boolean isBlack, int[][] board, int depthLimit) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
        this.zobristTransposition = new ZobristTransposition(); // Initialize ZobristTransposition
        this.zobristTransposition.computeZobristHash(board); // Compute initial Zobrist hash for the current board
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        BotLogic botLogic = new BotLogic(isBlack);
        loadTranspositionTable("transposition_table.ser");
        Move bestMove = negamax(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, botLogic);
        System.out.println("best move " + bestMove.startRow + " " + bestMove.startCol + " to " + bestMove.endRow + " " + bestMove.endCol + " " + bestMove.evaluation);

        if (bestMove != null) {
            // Execute the move
            if (bestMove.isAttackMove) {
                boardScreen.botAttackStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            } else {
                boardScreen.botMoveStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            }
        }
        saveTranspositionTable("transposition_table.ser");
    }
    private Move negamax(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, int max, BotLogic botLogic) {
        if (zobristTransposition.isInTranspositionTable()) {
            TranspositionEntry entry = zobristTransposition.getEntryFromTranspositionTable();
            return new Move(entry.getStartRow(), entry.getStartCol(), entry.getEndRow(), entry.getEndCol(), entry.isAttack(), entry.getEvaluation());
        }

        if (depth == 0 || botLogic.isGameOver(board,boardScreen,max)) {
            int evaluation;
            if((botLogic.didBlackWin() && isBlack) || (botLogic.didWhiteWin() && !isBlack)){
                evaluation = 10000;
            } else if ((botLogic.didBlackWin() && !isBlack) || (botLogic.didWhiteWin() && isBlack)) {
                evaluation = -10000;
            }else {
                evaluation = botLogic.evaluateBoard(board); // Evaluate the board at the leaf node
            }
            botLogic.setWinsToFalse();

            return new Move(-1,-1,-1,-1,false,max * evaluation); // Return the evaluation wrapped in a Move object
        }

        List<Move> moves = botLogic.getAllPossibleMoves(board, boardScreen, max == 1); // Color == 1 for current player, -1 for opponent
        moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));

        if (moves.get(0).isAttackMove) {
            moves.removeIf(move -> !move.isAttackMove);
        }

        Move bestMove = null;
        int maxEval = Integer.MIN_VALUE;

        for (Move move : moves) {
            // Before making a move, save the previous Zobrist hash
            long prevZobristHash = zobristTransposition.getZobristHash();

            // Update Zobrist hash and make the move on the board
            if(move.isAttackMove())
                zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol, board[move.startRow][move.startCol]);
            else
                zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol, board[move.startRow][move.startCol]); // Piece attack

            botLogic.makeMove(board, move, isBlack && max == 1 || !isBlack && max == -1); // if true move is black, else white

            Move resultMove = negamax(boardScreen, board, depth - 1, -beta, -alpha, -max, botLogic); // Negate alpha, beta, and color

            if (-resultMove.evaluation > maxEval) {
                maxEval = -resultMove.evaluation;
                bestMove = move.clone();
                bestMove.evaluation = maxEval;
            }

            // Undo the move
            botLogic.undoMove(board, move, isBlack && max == 1 || !isBlack && max == -1);

            if(move.isAttackMove())
                zobristTransposition.undoZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol, board[move.startRow][move.startCol]);
            else
                zobristTransposition.updateZobristHash(move.endRow, move.endCol, move.startRow, move.startCol, board[move.startRow][move.startCol]); // Piece attack

            alpha = Math.max(alpha, maxEval);
            if (alpha >= beta) {
                break; // Beta cut-off, no need to explore further if a winning move is found
            }
        }

        if (bestMove == null) {
            bestMove = moves.get(0); // Fallback in case no best move was found
        }
        // Store the result in the transposition table
        zobristTransposition.storeEntryInTranspositionTable(bestMove.evaluation, bestMove.isAttackMove, bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
        return bestMove;
    }
    // Save the transposition table to a file
    public void saveTranspositionTable(String filename) {
        zobristTransposition.saveTranspositionTable(filename);
    }

    // Load the transposition table from a file
    public void loadTranspositionTable(String filename) {
        zobristTransposition.loadTranspositionTable(filename);
    }

    public void changeSide(){
        isBlack = !isBlack;
    }
}
