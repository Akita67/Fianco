package io.github.fianco;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ZobristTransposition implements Serializable {
    private long[][][] zobristTable; // Zobrist hashing table
    private long zobristHash; // Current hash for the board
    private Map<Long, TranspositionEntry> transpositionTable; // Transposition table with Zobrist hash as key

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
    public void updateZobristHash(int startRow, int startCol, int endRow, int endCol, int piece) {
        // XOR out the old piece at the start position
        zobristHash ^= zobristTable[startRow][startCol][piece];
        // XOR in the empty square at the start position
        zobristHash ^= zobristTable[startRow][startCol][EMPTY];

        // XOR out the piece at the end position (should be empty)
        zobristHash ^= zobristTable[endRow][endCol][EMPTY];
        // XOR in the new piece at the end position
        zobristHash ^= zobristTable[endRow][endCol][piece];
    }
    public void updateZobristHashForAttack(int startRow, int startCol, int endRow, int endCol, int attackingPiece) {
        // Regular move: XOR out the attacking piece from its start position
        zobristHash ^= zobristTable[startRow][startCol][attackingPiece];
        // XOR in the empty space at the start position
        zobristHash ^= zobristTable[startRow][startCol][EMPTY];

        // Regular move: XOR out the empty square from the end position
        zobristHash ^= zobristTable[endRow][endCol][EMPTY];
        // XOR in the attacking piece at the end position
        zobristHash ^= zobristTable[endRow][endCol][attackingPiece];

        // Captured piece: XOR out the captured piece from the board
        int capturedRow = (startRow + endRow) / 2; // Midpoint between start and end for checkers attack
        int capturedCol = (startCol + endCol) / 2;

        zobristHash ^= zobristTable[capturedRow][capturedCol][attackingPiece==WHITE_PIECE?2:1];
        // XOR in an empty square where the captured piece was
        zobristHash ^= zobristTable[capturedRow][capturedCol][EMPTY];
    }
    public void undoZobristHashForAttack(int startRow, int startCol, int endRow, int endCol, int attackingPiece) {
        // Reverse the regular move portion (same as undoing a regular move)
        zobristHash ^= zobristTable[endRow][endCol][attackingPiece];
        zobristHash ^= zobristTable[endRow][endCol][EMPTY];

        zobristHash ^= zobristTable[startRow][startCol][EMPTY];
        zobristHash ^= zobristTable[startRow][startCol][attackingPiece];

        // Restore the captured piece
        int capturedRow = (startRow + endRow) / 2; // Midpoint for the captured piece
        int capturedCol = (startCol + endCol) / 2;

        // XOR in the captured piece at its original position
        zobristHash ^= zobristTable[capturedRow][capturedCol][EMPTY]; // XOR out the empty
        zobristHash ^= zobristTable[capturedRow][capturedCol][attackingPiece==WHITE_PIECE?2:1]; // XOR in the captured piece
    }

    // Get the current Zobrist hash
    public long getZobristHash() {
        return zobristHash;
    }

    // Check if the current board state is already in the transposition table
    public boolean isInTranspositionTable() {
        return transpositionTable.containsKey(zobristHash);
    }

    // Get the TranspositionEntry (which includes evaluation and move information) from the table
    public TranspositionEntry getEntryFromTranspositionTable() {
        return transpositionTable.get(zobristHash);
    }

    // Store the evaluation and move in the transposition table
    public void storeEntryInTranspositionTable(int evaluation, boolean isAttack, int startRow, int startCol, int endRow, int endCol) {
        TranspositionEntry entry = new TranspositionEntry(evaluation, isAttack, startRow, startCol, endRow, endCol);
        transpositionTable.put(zobristHash, entry);
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
            transpositionTable = (Map<Long, TranspositionEntry>) ois.readObject();
            System.out.println("Transposition table loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading transposition table: " + e.getMessage());
        }
    }
}
