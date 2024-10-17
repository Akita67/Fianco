package io.github.fianco;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaBotTT extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private ZobristTransposition zobristTransposition; // Instance of ZobristTransposition
    private int flag = 0;

    public AlphaBetaBotTT(boolean isBlack, int[][] board, int depthLimit) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
        this.zobristTransposition = new ZobristTransposition(); // Initialize ZobristTransposition
        this.zobristTransposition.resetTranspositionTable();
        this.zobristTransposition.computeZobristHash(board); // Compute initial Zobrist hash for the current board
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        this.zobristTransposition.computeZobristHash(board);
        BotLogic botLogic = new BotLogic(isBlack);
        Move bestMove = null;
        List<Move> moves = botLogic.getAllPossibleMoves(board, boardScreen, true);
        moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));

        if(moves.size() == 1){
            bestMove = moves.get(0);
        }
        else if (moves.get(0).isAttackMove) {
            moves.removeIf(move -> !move.isAttackMove);
            if(moves.size()==1){
                bestMove = moves.get(0);
            }else{
                loadTranspositionTable("transposition_table.ser");
                bestMove = alphaBeta(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, true, botLogic);
            }
        }else{
            loadTranspositionTable("transposition_table.ser");
            bestMove = alphaBeta(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, true, botLogic);
        }
        System.out.println("best move " + bestMove.startRow + " " + bestMove.startCol + " to " + bestMove.endRow + " " + bestMove.endCol + " " + bestMove.evaluation);


        if (bestMove != null) {
            // Execute the move
            if (bestMove.isAttackMove) {
                boardScreen.botAttackStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            } else {
                boardScreen.botMoveStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            }
            System.out.println(bestMove.evaluation);
        }
        saveTranspositionTable("transposition_table.ser");
    }
    // Alpha-Beta pruning algorithm to find the best move
    private Move alphaBeta(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, boolean maximizingPlayer, BotLogic botLogic) {
        if (zobristTransposition.isInTranspositionTable()) { // && zobristTransposition.getEntryFromTranspositionTable().getDepth() >= depth
            //System.out.println("this is the depth " + depth);
            TranspositionEntry entry = zobristTransposition.getEntryFromTranspositionTable();
            //System.out.println("this is evaluation " + entry.getEvaluation());
            return new Move(entry.getStartRow(), entry.getStartCol(), entry.getEndRow(), entry.getEndCol(), entry.isAttack(), entry.getEvaluation());
        }

        if (depth == 0 || botLogic.isGameOver(board,boardScreen,maximizingPlayer?1:-1)) {
            int evaluation;
            if((botLogic.didWhiteWin() && !isBlack) || (botLogic.didBlackWin() && isBlack)){
                evaluation = 10000;
            } else if((botLogic.didWhiteWin() && isBlack) || (botLogic.didBlackWin() && !isBlack)){
                evaluation = -10000;
            }else{
                evaluation = botLogic.evaluateBoard(board); // Evaluate the board at the leaf node
            }
            botLogic.setWinsToFalse();
            return new Move(-1,-1,-1,-1,false,evaluation); // Return the evaluation wrapped in a Move object
        }

        List<Move> moves = botLogic.getAllPossibleMoves(board, boardScreen, maximizingPlayer);
        // Sort the list by isAttackMove first (true before false)
        moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));
        if(moves.get(0).isAttackMove){
            // remove all the moves with isAttackMove false
            moves.removeIf(move -> !move.isAttackMove);
        }
        Move bestMove = null;

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;

            for (Move move : moves) {
                // Simulate the move
                botLogic.makeMove(board, move, isBlack);
                if(move.isAttackMove)
                    zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);
                else
                    zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);

                Move resultMove = alphaBeta(boardScreen, board, depth - 1, alpha, beta, false, botLogic);
                zobristTransposition.storeEntryInTranspositionTable(resultMove.evaluation, move.isAttackMove, move.startRow, move.startCol, move.endRow, move.endCol, depth, flag);
                if (resultMove.evaluation > maxEval) {
                    maxEval = resultMove.evaluation;
                    bestMove = move;
                    bestMove.evaluation = maxEval;
                }

                // Undo the move
                botLogic.undoMove(board, move, isBlack);
                if(move.isAttackMove)
                    zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);
                else
                    zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);

                alpha = Math.max(alpha, resultMove.evaluation);
                if (beta <= alpha) {
                    flag = 1; // Fail-high result implies a lower bound
                    break; // Beta cut-off
                }
            }

        } else {
            int minEval = Integer.MAX_VALUE;

            for (Move move : moves) {
                // Simulate the move
                botLogic.makeMove(board, move, !isBlack);
                if(move.isAttackMove)
                    zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);
                else
                    zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);

                Move resultMove = alphaBeta(boardScreen, board, depth - 1, alpha, beta, true, botLogic);
                zobristTransposition.storeEntryInTranspositionTable(resultMove.evaluation, move.isAttackMove, move.startRow, move.startCol, move.endRow, move.endCol, depth, flag);

                if (resultMove.evaluation < minEval) {
                    minEval = resultMove.evaluation;
                    bestMove = move;
                    bestMove.evaluation = minEval;
                }

                // Undo the move
                botLogic.undoMove(board, move, !isBlack);
                if(move.isAttackMove)
                    zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);
                else
                    zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);

                beta = Math.min(beta, resultMove.evaluation);
                if (beta <= alpha) {
                    flag = 2; // Fail-low result implies an upper bound
                    break; // Alpha cut-off
                }
            }
        }
        flag = 0;
        return bestMove;
    }
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
