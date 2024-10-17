package io.github.fianco;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ZobristTransposition implements Serializable {

    private static final int MAX_ENTRIES = 500000; // number of elements * size of each element => 243 * 8 bytes = 1944 bytes
    // 1gm ram / 1944 bytes = ~0,56 millions
    private long[][][] zobristTable; // Zobrist hashing table
    private long zobristHash; // Current hash for the board
    private Map<Long, TranspositionEntry> transpositionTable; // Transposition table with Zobrist hash as key

    private static final int BLACK_PIECE = 2;
    private static final int WHITE_PIECE = 1;
    private static final int EMPTY = 0;
    private long playerHash;

    public ZobristTransposition() {
        this.zobristTable = new long[9][9][3]; // 9x9 board, 3 states (empty, white, black)
        this.transpositionTable = new HashMap<>();
        initializeZobristTable();
    }

    // Method to initialize the Zobrist table with random values
    private void initializeZobristTable() {
        Random random = new Random();
        playerHash = random.nextLong();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                zobristTable[row][col][WHITE_PIECE] = random.nextLong();
                zobristTable[row][col][BLACK_PIECE] = random.nextLong();
            }
        }
    }
    // Manually manage the size of the transposition table
    private void ensureCapacity() {
        if (transpositionTable.size() > MAX_ENTRIES) {
            Long keyToRemove = null;
            int minDepth = 50; // Initialize with the maximum possible integer value
            int value;
            int count = 0;

            // Iterate through the transposition table to find the entry with the lowest depth
            for (Map.Entry<Long, TranspositionEntry> entry : transpositionTable.entrySet()) {
                TranspositionEntry transpositionEntry = entry.getValue();
                value = transpositionEntry.getDepth();
                count++;
                if (value < minDepth) {
                    minDepth = value;
                    keyToRemove = entry.getKey(); // Store the key of the entry with the lowest depth
                }
                if(count>=10){
                    break;
                }
            }

            // Remove the entry with the lowest depth
            if (keyToRemove != null) {
                transpositionTable.remove(keyToRemove);
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
        // XOR in the new piece at the end position
        zobristHash ^= zobristTable[endRow][endCol][piece];
        zobristHash ^= playerHash; // switch who is playing
    }
    public void updateZobristHashForAttack(int startRow, int startCol, int endRow, int endCol, int attackingPiece) {
        // Regular move: XOR out the attacking piece from its start position
        zobristHash ^= zobristTable[startRow][startCol][attackingPiece];
        // XOR in the attacking piece at the end position
        zobristHash ^= zobristTable[endRow][endCol][attackingPiece];

        // Captured piece: XOR out the captured piece from the board
        int capturedRow = (startRow + endRow) / 2; // Midpoint between start and end for checkers attack
        int capturedCol = (startCol + endCol) / 2;

        zobristHash ^= zobristTable[capturedRow][capturedCol][attackingPiece==WHITE_PIECE?2:1];
        zobristHash ^= playerHash; // switch who is playing
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
    public void storeEntryInTranspositionTable(int evaluation, boolean isAttack, int startRow, int startCol, int endRow, int endCol, int depth, int flag) {
        TranspositionEntry entry = new TranspositionEntry(evaluation, isAttack, startRow, startCol, endRow, endCol, depth, flag);
        ensureCapacity(); // Ensure we don't exceed the limit
        //System.out.println(transpositionTable.size());
        transpositionTable.put(zobristHash, entry);
    }

    // Method to reset or clear the transposition table (if needed)
    public void resetTranspositionTable() {
        // Check if the file "transposition_table.ser" exists
        File file = new File("transposition_table.ser");
        if (file.exists()) {
            // Delete the file if it exists
            if (file.delete()) {
                System.out.println("Transposition table file deleted successfully.");
            } else {
                System.err.println("Failed to delete transposition table file.");
            }
        }

        // Clear the transposition table
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
