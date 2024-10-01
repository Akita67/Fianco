package io.github.fianco;

import java.util.ArrayList;
import java.util.List;

public class IterativeDeepening extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private final long timeConstraint;

    public IterativeDeepening(boolean isBlack, int[][] board, int depthLimit, long timeConstraint) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
        this.timeConstraint = timeConstraint;
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        BotLogic botLogic = new BotLogic(isBlack);
        Move bestMove = iterativeDeepening(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, timeConstraint, botLogic);
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
    private Move iterativeDeepening(BoardScreen boardScreen, int[][] board, int maxDepth, int alpha, int beta, int max, long timeLimitMillis, BotLogic botLogic) {
        Move bestMove = null;
        // Capture the start time of the search
        long startTime = System.currentTimeMillis();

        // Iteratively increase the depth from 1 up to the maximum depth limit
        for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
            // Check the elapsed time
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= timeLimitMillis) {
                System.out.println("Time limit reached. Stopping search at depth: " + (currentDepth - 1));
                break;
            }

            System.out.println("Searching at depth: " + currentDepth);

            // If you only have one move and it's an attack move you should not look further
            List<Move> moves = botLogic.getAllPossibleMoves(board,boardScreen, max == 1);
            moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));

            if(currentDepth==1 && moves.size() == 1){
                bestMove = moves.get(0);
                break;
            }
            else if (moves.get(0).isAttackMove) {
                moves.removeIf(move -> !move.isAttackMove);
            }
            if(currentDepth==1 && moves.size()==1){
                bestMove = moves.get(0);
                break;
            }

            // Perform a negamax search with the current depth limit
            Move currentBestMove = negamax(boardScreen, board, currentDepth, alpha, beta, max, botLogic);

            if (currentBestMove != null) {
                bestMove = currentBestMove; // Update the best move found so far

                // If we find a winning move, break out of the loop early
                if (Math.abs(bestMove.evaluation) == 10000) {
                    break;
                }
            }
        }

        return bestMove;
    }

    private Move negamax(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, int max, BotLogic botLogic) {
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
            botLogic.makeMove(board, move, isBlack && max == 1 || !isBlack && max == -1); // if true move is black, else white

            Move resultMove = negamax(boardScreen, board, depth - 1, -beta, -alpha, -max, botLogic); // Negate alpha, beta, and color

            if (-resultMove.evaluation > maxEval) {
                maxEval = -resultMove.evaluation;
                bestMove = move.clone();
                bestMove.evaluation = maxEval;
            }

            // Undo the move
            botLogic.undoMove(board, move, isBlack && max == 1 || !isBlack && max == -1);

            alpha = Math.max(alpha, maxEval);
            if (alpha >= beta) {
                break; // Beta cut-off, no need to explore further if a winning move is found
            }
        }

        if (bestMove == null) {
            bestMove = moves.get(0); // Fallback in case no best move was found
        }

        return bestMove;
    }
    public void changeSide(){
        isBlack = !isBlack;
    }
}
