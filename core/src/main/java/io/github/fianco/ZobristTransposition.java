package io.github.fianco;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ZobristTransposition {
    private long[][][] zobristTable; // Zobrist hashing table
    private long zobristHash; // Current hash for the board
    private Map<Long, Integer> transpositionTable; // Transposition table with Zobrist hash as key

    private static final int BLACK_PIECE = 2;
    private static final int WHITE_PIECE = 1;
    private static final int EMPTY = 0;

    public ZobristTransposition() {
        this.zobristTable = new long[9][9][3]; // 9x9 board, 3 states (empty, white, black)
        this.transpositionTable = new HashMap<>();
        initializeZobristTable();
    }

    // Method to initialize the Zobrist table with random values
    private void initializeZobristTable() {
        Random random = new Random();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                zobristTable[row][col][EMPTY] = random.nextLong();
                zobristTable[row][col][WHITE_PIECE] = random.nextLong();
                zobristTable[row][col][BLACK_PIECE] = random.nextLong();
            }
        }
    }

    // Method to compute the initial Zobrist hash of the board
    public long computeZobristHash(int[][] board) {
        long hash = 0L;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int piece = board[row][col];
                if (piece != EMPTY) {
                    hash ^= zobristTable[row][col][piece];
                }
            }
        }
        this.zobristHash = hash;
        return hash;
    }

    // Update the Zobrist hash when a move is made
    public void updateZobristHash(int row, int col, int oldPiece, int newPiece) {
        // XOR out the old piece at this position
        zobristHash ^= zobristTable[row][col][oldPiece];
        // XOR in the new piece at this position
        zobristHash ^= zobristTable[row][col][newPiece];
    }

    // Get the current Zobrist hash
    public long getZobristHash() {
        return zobristHash;
    }

    // Check if the current board state is already in the transposition table
    public boolean isInTranspositionTable() {
        return transpositionTable.containsKey(zobristHash);
    }

    // Get the evaluation from the transposition table
    public int getEvaluationFromTranspositionTable() {
        return transpositionTable.get(zobristHash);
    }

    // Store the evaluation in the transposition table
    public void storeEvaluationInTranspositionTable(int evaluation) {
        transpositionTable.put(zobristHash, evaluation);
    }

    // Method to reset or clear the transposition table (if needed)
    public void resetTranspositionTable() {
        transpositionTable.clear();
    }
    // Save the transposition table to a file
    public void saveTranspositionTable(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(transpositionTable);
            System.out.println("Transposition table saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving transposition table: " + e.getMessage());
        }
    }

    // Load the transposition table from a file
    @SuppressWarnings("unchecked")
    public void loadTranspositionTable(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            transpositionTable = (Map<Long, Integer>) ois.readObject();
            System.out.println("Transposition table loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading transposition table: " + e.getMessage());
        }
    }
}
