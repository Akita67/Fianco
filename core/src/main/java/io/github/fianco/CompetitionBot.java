package io.github.fianco;

import java.util.List;

public class CompetitionBot extends Bot {
    private int depthLimit; // Depth limit for alpha-beta pruning
    private ZobristTransposition zobristTransposition; // Instance of ZobristTransposition
    private final long timeConstraint;
    private int flag = 0;

    public CompetitionBot(boolean isBlack, int[][] board, int depthLimit, long timeConstraint) {
        super(isBlack, board);
        this.depthLimit = depthLimit; // Set the maximum depth for the search
        this.timeConstraint = timeConstraint;
        this.zobristTransposition = new ZobristTransposition(); // Initialize ZobristTransposition
        this.zobristTransposition.resetTranspositionTable();
        this.zobristTransposition.computeZobristHash(board); // Compute initial Zobrist hash for the current board
    }

    // Main method for the bot to make its move
    public void calculate(BoardScreen boardScreen, int[][] board) {
        this.zobristTransposition.computeZobristHash(board);
        BotLogic botLogic = new BotLogic(isBlack);
        loadTranspositionTable("transposition_table.ser");
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
        saveTranspositionTable("transposition_table.ser");
    }
    private Move iterativeDeepening(BoardScreen boardScreen, int[][] board, int maxDepth, int alpha, int beta, int max, long timeLimitMillis, BotLogic botLogic) {
        Move bestMove = null;
        // Capture the start time of the search
        long startTime = System.currentTimeMillis();

        // Iteratively increase the depth from 1 up to the maximum depth limit
        for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth+=1) {
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
        if (zobristTransposition.isInTranspositionTable() && zobristTransposition.getEntryFromTranspositionTable().getDepth() >= depth) { //
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
            if(suicideMove(board,move.endRow,move.endCol, max)) // suicide moves
                continue;
            botLogic.makeMove(board, move, isBlack && max == 1 || !isBlack && max == -1); // if true move is black, else white
            if(move.isAttackMove)
                zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);
            else
                zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);

            Move resultMove = negamax(boardScreen, board, depth - 1, -beta, -alpha, -max, botLogic); // Negate alpha, beta, and color
            zobristTransposition.storeEntryInTranspositionTable(-resultMove.evaluation, move.isAttackMove, move.startRow, move.startCol, move.endRow, move.endCol, depth, flag);

            if (-resultMove.evaluation > maxEval) {
                maxEval = -resultMove.evaluation;
                bestMove = move.clone();
                bestMove.evaluation = maxEval;
            }

            // Undo the move
            botLogic.undoMove(board, move, isBlack && max == 1 || !isBlack && max == -1);

            if(move.isAttackMove)
                zobristTransposition.updateZobristHashForAttack(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);
            else
                zobristTransposition.updateZobristHash(move.startRow, move.startCol, move.endRow, move.endCol,board[move.endRow][move.endCol]);

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

    public boolean suicideMove(int [][]board, int row, int col, int max){
        if(row!=0 && row!=board.length-1 && col!=0 && col!=board.length-1)
            if((max==1 && isBlack) || (max==-1 && !isBlack)) // Bot black plays
                if((board[row-1][col-1] == 1 && board[row+1][col+1] == 0) || (board[row-1][col+1] == 1 && board[row+1][col-1] == 0))
                    return true;
            else if((max==1 && !isBlack) || (max==-1 && isBlack)) // Bot white plays
                if((board[row+1][col-1] == 2 && board[row-1][col+1] == 0) || (board[row+1][col+1] == 2 && board[row-1][col-1] == 0))
                    return true;
        return false;
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
