package io.github.fianco;

import java.util.List;

public class NegaMaxBotWindow extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private int delta;

    public NegaMaxBotWindow(boolean isBlack, int[][] board, int depthLimit, int delta) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
        this.delta = delta;
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        BotLogic botLogic = new BotLogic(isBlack);
        Move bestMove = null;
        List<Move> moves = botLogic.getAllPossibleMoves(board, boardScreen, true);
        moves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));

        if (moves.size() == 1) {
            bestMove = moves.get(0);
        } else if (moves.get(0).isAttackMove) {
            moves.removeIf(move -> !move.isAttackMove);
            if (moves.size() == 1) {
                bestMove = moves.get(0);
            } else {
                int guess = 0; // Initial guess for the aspiration window
                bestMove = negamaxWithWindowing(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, botLogic, guess);
            }
        } else {
            int guess = 0; // Initial guess for the aspiration window
            bestMove = negamaxWithWindowing(boardScreen, board, depthLimit, Integer.MIN_VALUE, Integer.MAX_VALUE, 1, botLogic, guess);
        }

        System.out.println("best move " + bestMove.startRow + " " + bestMove.startCol + " to " + bestMove.endRow + " " + bestMove.endCol + " " + bestMove.evaluation);

        if (bestMove != null) {
            // Execute the move
            if (bestMove.isAttackMove) {
                boardScreen.botAttackStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            } else {
                boardScreen.botMoveStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            }
        } else {
            boardScreen.cantMove(this.isBlack);
        }
    }

    // Negamax with aspiration windowing
    private Move negamaxWithWindowing(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, int max, BotLogic botLogic, int guess) {
        // First search with a window around the guess
        int aspirationAlpha = guess - delta;
        int aspirationBeta = guess + delta;

        Move bestMove = negamax(boardScreen, board, depth, aspirationAlpha, aspirationBeta, max, botLogic);

        // If the result is outside the window, perform a re-search with a wider window
        if (bestMove.evaluation <= aspirationAlpha) {
            // Fail low - search again with a smaller window
            bestMove = negamax(boardScreen, board, depth, Integer.MIN_VALUE, aspirationAlpha, max, botLogic);
        } else if (bestMove.evaluation >= aspirationBeta) {
            // Fail high - search again with a larger window
            bestMove = negamax(boardScreen, board, depth, aspirationBeta, Integer.MAX_VALUE, max, botLogic);
        }

        return bestMove;
    }

    // The standard negamax function
    private Move negamax(BoardScreen boardScreen, int[][] board, int depth, int alpha, int beta, int max, BotLogic botLogic) {
        if (depth == 0 || botLogic.isGameOver(board, boardScreen, max)) {
            int evaluation;
            if ((botLogic.didBlackWin() && isBlack) || (botLogic.didWhiteWin() && !isBlack)) {
                evaluation = 10000;
            } else if ((botLogic.didBlackWin() && !isBlack) || (botLogic.didWhiteWin() && isBlack)) {
                evaluation = -10000;
            } else {
                evaluation = botLogic.evaluateBoard(board); // Evaluate the board at the leaf node
            }
            botLogic.setWinsToFalse();

            return new Move(-1, -1, -1, -1, false, max * evaluation); // Return the evaluation wrapped in a Move object
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

    public void changeSide() {
        isBlack = !isBlack;
    }
}
