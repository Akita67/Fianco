package io.github.fianco;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Testing {
    private final int simulations = 10000000;
    private int gridSize = 9;
    private int[][] board = new int[gridSize][gridSize]; // Initialize the board
    private int countWinW = 0;
    private int countWinB = 0;
    private boolean blackWins = false;
    private boolean whiteWins = false;
    private boolean isBlackTurn = false;
    private Random random = new Random(); // For random bot moves

    /*
    1000000 164 secs
    Black wins: 514752
    White wins: 485248

    10000000 ~1640 secs
    Black wins: 5153373
    White wins: 4846627
     */


    public static void main(String[] args) {
        Testing testingInstance = new Testing();
        testingInstance.runSimulations();
    }

    // Run simulations and alternate between white and black turns
    private void runSimulations() {
        for (int i = 0; i < simulations; i++) {
            setInitialStonePositions();
            while (!gameOver()) {
                playGame();  // Alternate moves
                isBlackTurn = !isBlackTurn; // Switch turns
            }
            recordGameResult(); // Check winner and update counters
        }
        printResults(); // Print the number of wins for each side
    }

    private void setInitialStonePositions() {
        board = new int[gridSize][gridSize];
        // Set white stones in the first row
        for (int col = 0; col < gridSize; col++) {
            board[0][col] = 1; // 1 = white stone
        }
        board[1][1] = 1;
        board[1][7] = 1;
        board[2][2] = 1;
        board[2][6] = 1;
        board[3][3] = 1;
        board[3][5] = 1;

        // Set black stones in the last row
        for (int col = 0; col < gridSize; col++) {
            board[8][col] = 2; // 2 = black stone
        }
        board[7][1] = 2;
        board[7][7] = 2;
        board[6][2] = 2;
        board[6][6] = 2;
        board[5][3] = 2;
        board[5][5] = 2;
    }

    public void playGame() {
        if (isBlackTurn) {
            makeMove(true); // Black's move
        } else {
            makeMove(false); // White's move
        }
    }

    // Method to get all possible moves for the bot
    private List<Move> getAllPossibleMoves() {
        List<Move> moves = new ArrayList<>();

        // Loop through the board to find all possible moves for the bot's pieces
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (isBotPiece(row, col)) {
                    // Get all valid moves for this piece
                    List<Move> pieceMoves = getValidMovesForPiece(row, col);
                    moves.addAll(pieceMoves); // Add to the list of moves
                }
            }
        }

        return moves;
    }

    // Simulate a bot move (randomly pick a move)
    private void makeMove(boolean isBlack) {
        List<Move> possibleMoves = getAllPossibleMoves();
        possibleMoves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));
        if (!possibleMoves.isEmpty()) {
            if(possibleMoves.get(0).isAttackMove)
                executeMove(possibleMoves.get(0));
            else{
                Move chosenMove = possibleMoves.get(random.nextInt(possibleMoves.size())); // Pick a random move
                executeMove(chosenMove);
            }
        }
    }

    // Execute a move
    private void executeMove(Move move) {
        board[move.endRow][move.endCol] = board[move.startRow][move.startCol]; // Move the stone
        board[move.startRow][move.startCol] = 0; // Clear the previous spot
    }

    // Check if the stone belongs to the bot
    private boolean isBotPiece(int row, int col) {
        return (isBlackTurn && board[row][col] == 2) || (!isBlackTurn && board[row][col] == 1);
    }

    // Get all valid moves for a specific piece (regular move or attack)
    private List<Move> getValidMovesForPiece(int row, int col) {
        List<Move> validMoves = new ArrayList<>();

        // Try moving in all four directions (forward for white, backward for black, left and right)
        if (isBlackTurn && row > 0)
            addMoveIfValid(validMoves, row, col, row - 1, col); // Down
        if (!isBlackTurn && row < 8)
            addMoveIfValid(validMoves, row, col, row + 1, col); // Forward
        if (col > 0)
            addMoveIfValid(validMoves, row, col, row, col - 1); // Left
        if (col < 8)
            addMoveIfValid(validMoves, row, col, row, col + 1); // Right

        // Check for attack moves
        List<Move> getAttackMoves = checkForCapturesSpec(board, !isBlackTurn, row, col);
        if (!getAttackMoves.isEmpty()) {
            validMoves.addAll(getAttackMoves); // Add any valid attack moves
        }

        return validMoves;
    }

    // Check if a move is valid and add it to the list
    private void addMoveIfValid(List<Move> validMoves, int startRow, int startCol, int endRow, int endCol) {
        if (board[endRow][endCol] == 0) {
            validMoves.add(new Move(startRow, startCol, endRow, endCol, false));
        }
    }
    protected List<Move> checkForCapturesSpec(int [][]board, boolean player1, int row, int col) {
        //When a player is on the wall the other player needs to attack is a bug

        int playerStone = player1 ? 1 : 2; // 1 for white, 2 for black
        int opponentStone = player1 ? 2 : 1; // Opponent's stone
        List<Move> getAttackMoves = new ArrayList<>();

        // Loop through the board to check for capture possibilities
        if (board[row][col] == playerStone) {
            // Check diagonally left and right for opponent stones

            // Check diagonally left
            if (playerStone == 1) { // White moves upwards
                // Check diagonally left
                if (col>1 && row < 7 && board[row + 1][col - 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for white moving up)
                    if (board[row + 2][col - 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row+2, col-2, true));

                    }
                }
                // Check diagonally right
                if (col < board[row].length - 2 && row < 7 && board[row + 1][col + 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for white moving up)
                    if (board[row + 2][col + 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row+2, col+2, true));
                    }
                }
            }


            // Similar logic for black moving down (opposite direction)
            if (playerStone == 2) { // Black moves downwards
                // Check diagonally left
                if (col > 1 && row > 1 && board[row - 1][col - 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for black moving down)
                    if (board[row - 2][col - 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row-2, col-2, true));
                    }
                }

                // Check diagonally right
                if (col < board[col].length - 2 && row > 1 && board[row - 1][col + 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for black moving down)
                    if (board[row - 2][col + 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row-2, col+2, true));
                    }
                }
            }
        }
        return getAttackMoves;

    }


    public boolean gameOver(){
        // Check if a black stone has reached row 0 (top)
        for (int i = 0; i < board[0].length; i++) {
            if (board[0][i] == 2) {
                blackWins = true;
                return true;  // Exit the loop if a black stone has reached row 0
            }
        }

        // Check if a white stone has reached row 8 (bottom)
        for (int i = 0; i < board[8].length; i++) {
            if (board[8][i] == 1) {
                whiteWins = true;
                return true;  // Exit the loop if a white stone has reached row 8
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

    // Record game result after game over
    private void recordGameResult() {

        if (blackWins) countWinB++;
        if (whiteWins) countWinW++;
        resetBoolean();

    }

    // Print simulation results
    private void printResults() {
        System.out.println("Black wins: " + countWinB);
        System.out.println("White wins: " + countWinW);
    }
    private void resetBoolean(){
        blackWins = false;
        whiteWins = false;
    }
}
