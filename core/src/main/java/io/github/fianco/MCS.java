package io.github.fianco;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MCS extends Bot {
    private Random random;
    private List<Move> possibleMoves;
    private List<Move> possibleAttack;
    private int iterations;  // Number of iterations to run MCS
    private int numThreads;  // Number of threads for parallelism
    private boolean blackWins = false;
    private boolean whiteWins = false;

    public MCS(boolean isBlack, int[][] board, int iterations, int numThreads) {
        super(isBlack, board);
        this.iterations = iterations;
        this.numThreads = numThreads;
        random = new Random();
    }

    public void calculate(BoardScreen boardScreen, int[][] board) {
        BotLogic botLogic = new BotLogic(isBlack);
        possibleAttack = new ArrayList<>();

        // Run MCS to evaluate different evaluation functions
        List<EvaluationFunction> evalFunctions = getEvaluationFunctions();  // Define multiple evaluation functions
        EvaluationFunction bestEvalFunction = runMCS(boardScreen, board, botLogic, evalFunctions);

        System.out.println("Best Evaluation Function: " + bestEvalFunction.getName());

        // Optionally, after finding the best evaluation function, use it for your move decision
        Move bestMove = botLogic.getBestMoveUsingEvaluation(board, bestEvalFunction);
        System.out.println("Best move according to " + bestEvalFunction.getName() + ": " + bestMove.startRow + " " + bestMove.startCol);
    }

    private EvaluationFunction runMCS(BoardScreen boardScreen, int[][] board, BotLogic botLogic, List<EvaluationFunction> evalFunctions) {
        int bestPerformance = Integer.MIN_VALUE;
        EvaluationFunction bestEvalFunction = null;

        for (EvaluationFunction evalFunc : evalFunctions) {
            int totalPerformance = 0;

            for (int i = 0; i < iterations; i++) {
                System.out.println(i);
                int result = runParallelSimulations(boardScreen, board, botLogic, evalFunc);  // Pass evalFunc
                totalPerformance += result;
            }

            System.out.println("Performance of " + evalFunc.getName() + ": " + totalPerformance);

            if (totalPerformance > bestPerformance) {
                bestPerformance = totalPerformance;
                bestEvalFunction = evalFunc;
            }
        }

        return bestEvalFunction;  // Return the best-performing evaluation function
    }

    private int runParallelSimulations(BoardScreen boardScreen, int[][] board, BotLogic botLogic, EvaluationFunction evalFunc) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> futures = new ArrayList<>();

        // Submit simulations to run in parallel
        for (int i = 0; i < numThreads; i++) {
            Future<Integer> future = executor.submit(() -> simulation(boardScreen, board, botLogic, evalFunc));  // Pass evalFunc
            futures.add(future);
        }

        int totalResult = 0;
        // Collect results
        for (Future<Integer> future : futures) {
            try {
                totalResult += future.get();  // Get the result of the simulation
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return totalResult;
    }

    private int simulation(BoardScreen boardScreen, int[][] board, BotLogic botLogic, EvaluationFunction evalFunc) {
        int[][] simulationBoard = deepCopyBoard(board);
        blackWins = false;
        whiteWins = false;
        boolean current = isBlack;
        boolean max = true;

        while (!isGameOver(simulationBoard)) {
            List<Move> possibleMoves = botLogic.getAllPossibleMoves(simulationBoard, boardScreen, max);
            if (possibleMoves.isEmpty()) {
                break;
            }
            possibleMoves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));
            if(possibleMoves.get(0).isAttackMove){
                // remove all the moves with isAttackMove false
                possibleMoves.removeIf(move -> !move.isAttackMove);
            }

            int [][] finalsimulationBoard = simulationBoard;
            possibleMoves.sort((Move m1, Move m2) -> Double.compare(evalFunc.evaluate(botLogic.makeMoveInt(finalsimulationBoard, m2,isBlack)), evalFunc.evaluate(botLogic.makeMoveInt(finalsimulationBoard, m1,isBlack))));

            // Pick the best move according to the evaluation function
            Move bestMove = possibleMoves.get(0);
            simulationBoard = botLogic.makeMoveInt(simulationBoard, bestMove, isBlack);
            isBlack = !isBlack;
            max = !max;
        }
        isBlack = current;
        // Return the result of the simulation
        return getWinner() ? 1 : 0;
    }

    private int[][] deepCopyBoard(int[][] originalBoard) {
        int[][] copy = new int[originalBoard.length][originalBoard[0].length];
        for (int i = 0; i < originalBoard.length; i++) {
            System.arraycopy(originalBoard[i], 0, copy[i], 0, originalBoard[i].length);
        }
        return copy;
    }
    private List<EvaluationFunction> getEvaluationFunctions() {
        List<EvaluationFunction> evalFunctions = new ArrayList<>();
        evalFunctions.add(new SimpleEvaluationFunction());
        evalFunctions.add(new AdvancedEvaluationFunction());
        // Add more evaluation functions here
        return evalFunctions;
    }
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
    private boolean getWinner(){
        if(isBlack && blackWins || !isBlack && whiteWins){
            return true;
        }else{
            return false;
        }
    }
    // Evaluation Function Interface
    interface EvaluationFunction {
        double evaluate(int[][] board);
        String getName();
    }

    // Example: Simple Evaluation Function
    public class SimpleEvaluationFunction implements EvaluationFunction {
        public double evaluate(int[][] board) {
            int score = 0;

            int pieceValue = 10;

            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    if (isBotPiece(row, col, board)) {
                        if(isBlack)
                            score += pieceValue; // Example add: + (7 - row);
                        else
                            score += pieceValue;

                    } else if (isOpponentPiece(row, col, board)) {
                        if(!isBlack)
                            score -= (pieceValue); // Example add: + row;
                        else
                            score -= (pieceValue);
                    }
                }
            }
            return score;
        }

        public String getName() {
            return "Simple Evaluation";
        }
    }

    // Example: Advanced Evaluation Function
    public class AdvancedEvaluationFunction implements EvaluationFunction {
        public double evaluate(int[][] board) {
            int score = 0;

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

        public String getName() {
            return "Advanced Evaluation";
        }
    }
    // Check if the stone belongs to the bot
    private boolean isBotPiece(int row, int col, int[][]board) {
        return (isBlack && board[row][col] == 2) || (!isBlack && board[row][col] == 1);
    }

    // Check if the stone belongs to the opponent
    private boolean isOpponentPiece(int row, int col, int[][]board) {
        return (!isBlack && board[row][col] == 2) || (isBlack && board[row][col] == 1);
    }
    public void changeSide(){
        isBlack = !isBlack;
    }
}
