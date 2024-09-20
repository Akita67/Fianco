package io.github.fianco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MTCS extends Bot{
    private Random random;
    private List<Move> possibleMoves;
    private List<Move> possibleAttack;
    private int iterations;  // Number of iterations to run MCTS
    private boolean blackWins = false;
    private boolean whiteWins = false;

    public MTCS(boolean isBlack, int[][] board, int iterations){
        super(isBlack, board);
        this.iterations = iterations;
        random = new Random();
    }

    public void calculate(BoardScreen boardScreen, int[][] board){
        possibleAttack = new ArrayList<>();

        // Run the MCTS to determine the best move
        Move bestMove = runMCTS(boardScreen, board);
        System.out.println("blabla");
        System.out.println(bestMove.startRow + " " + bestMove.startCol);

        if (bestMove != null) {
            // Execute the best move
            if (bestMove.isAttackMove()) {
                boardScreen.botAttackStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            } else {
                boardScreen.botMoveStone(bestMove.startRow, bestMove.startCol, bestMove.endRow, bestMove.endCol);
            }
        }
    }

    private Move runMCTS(BoardScreen boardScreen, int[][] board) {
        Node rootNode = new Node(null, board, null, isBlack);

        // Run MCTS iterations
        for (int i = 0; i < iterations; i++) {
            Node selectedNode = selection(rootNode);
            if (!selectedNode.isTerminal()) {
                Node expandedNode = expansion(selectedNode,boardScreen);
                int simulationResult = simulation(expandedNode,boardScreen);
                backpropagation(expandedNode, simulationResult);
            } else {
                backpropagation(selectedNode, selectedNode.getResult());
            }
        }

        // Choose the child with the highest visit count
        return rootNode.getBestChild().move;
    }
    private Node selection(Node node) {
        while (!node.isLeaf()) {
            node = node.getBestUCTChild();
        }
        return node;
    }
    private Node expansion(Node node, BoardScreen boardScreen) {
        List<Move> possibleMoves = getAllPossibleMoves(boardScreen, node.board, node.isBlackNode);
        for(Move move : possibleMoves){
            System.out.println(move.startRow + " " + move.startCol);
        }
        System.out.println();
        possibleMoves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));
        if(possibleMoves.get(0).isAttackMove){
            // remove all the moves with isAttackMove false
            possibleMoves.removeIf(move -> !move.isAttackMove);
        }
        for (Move move : possibleMoves) {
            int[][] newBoard = makeMove(node.board, move, !node.isBlackNode); // Simulate the move TODO alternate isBlack and not
            node.addChild(new Node(node, newBoard, move, !node.isBlackNode));
        }
        return node.getRandomChild();
    }
    private int simulation(Node node, BoardScreen boardScreen) {
        int[][] simulationBoard = deepCopyBoard(node.board);  // Make a copy of the board
        boolean currentPlayerIsBot = node.isBlackNode;
        blackWins = false;
        whiteWins = false;

        while (!isGameOver(simulationBoard)) {
            List<Move> possibleMoves = getAllPossibleMoves(boardScreen, simulationBoard, currentPlayerIsBot);
            possibleMoves.sort((Move m1, Move m2) -> Boolean.compare(m2.isAttackMove(), m1.isAttackMove()));
            if(possibleMoves.get(0).isAttackMove){
                // remove all the moves with isAttackMove false
                possibleMoves.removeIf(move -> !move.isAttackMove);
            }
            if (possibleMoves.isEmpty()) {
                break;
            }
            // TODO does not end the simulation
            Move randomMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            //System.out.println(randomMove.startRow + " " + randomMove.startCol + " " + randomMove.endRow + " " + randomMove.endCol);
            simulationBoard = makeMove(simulationBoard, randomMove, currentPlayerIsBot);
            currentPlayerIsBot = !currentPlayerIsBot;  // Switch turns
        }
        System.out.println("a game has ended");

        // Return the result of the simulation (1 for win, 0 for loss, 0.5 for draw)
        return getWinner() ? 1 : 0;

    }
    private void backpropagation(Node node, int result) {
        while (node != null) {
            node.visits++;
            node.wins += result;
            result = 1 - result;  // Flip the result as we go back up the tree
            node = node.parent;
        }
    }
    private List<Move> getAllPossibleMoves(BoardScreen boardScreen, int[][] currentboard, Boolean isBlackNode) {
        List<Move> moves = new ArrayList<>();

        // Loop through the board to find all possible moves for the bot's pieces
        if(isBlackNode == isBlack) {
            for (int row = 0; row < boardScreen.gridSize; row++) {
                for (int col = 0; col < boardScreen.gridSize; col++) {
                    if (isBotPiece(row, col)) {
                        // Get all valid moves for this piece
                        List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, currentboard, isBlack);
                        moves.addAll(pieceMoves); // Add to the list of moves
                    }
                }
            }
        }else {
            for (int row = 0; row < boardScreen.gridSize; row++) {
                for (int col = 0; col < boardScreen.gridSize; col++) {
                    if (isOpponentPiece(row, col)) {
                        // Get all valid moves for this piece
                        List<Move> pieceMoves = getValidMovesForPiece(boardScreen, row, col, currentboard, !isBlack);
                        moves.addAll(pieceMoves); // Add to the list of moves
                    }
                }
            }
        }

        return moves;
    }
    // Check if the stone belongs to the bot
    private boolean isBotPiece(int row, int col) {
        return (isBlack && board[row][col] == 2) || (!isBlack && board[row][col] == 1);
    }
    private boolean isOpponentPiece(int row, int col) {
        return (!isBlack && board[row][col] == 2) || (isBlack && board[row][col] == 1);
    }
    private List<Move> getValidMovesForPiece(BoardScreen boardScreen, int row, int col, int[][]currentboard, boolean isBlack) {
        List<Move> validMoves = new ArrayList<>();

        // Try moving in all four directions (forward for white,backward for black, left and right) if allowed
        if(isBlack && row>0)
            addMoveIfValid(currentboard, validMoves, row, col, row - 1, col); // Down
        if(!isBlack && row<8)
            addMoveIfValid(currentboard, validMoves, row, col, row + 1, col); // Forward
        if(col>0)
            addMoveIfValid(currentboard, validMoves, row, col, row, col - 1); // Left
        if(col<8)
            addMoveIfValid(currentboard, validMoves, row, col, row, col + 1); // Right

        // Check for attack moves
        List<Move> getAttackMoves = boardScreen.checkForCapturesSpec(currentboard,!isBlack,row,col);
        //System.out.println(getAttackMoves.size());
        if (getAttackMoves.size()!=0) {
            validMoves.addAll(getAttackMoves); // Add any valid attack moves
        }

        return validMoves;
    }
    private void addMoveIfValid(int[][] board, List<Move> validMoves, int startRow, int startCol, int endRow, int endCol) {
        if (board[endRow][endCol] == 0) {
            validMoves.add(new Move(startRow, startCol, endRow, endCol, false));
        }
    }
    public int[][] makeMove(int[][] board, Move move, boolean isBlack){
        int [][] newboard = deepCopyBoard(board);
        if(move.isAttackMove()){
            if(isBlack){
                if(move.endCol>move.startCol)// Black attack to the right
                    newboard[move.startRow-1][move.startCol+1] = 0;
                else // Black attack to the left
                    newboard[move.startRow-1][move.startCol-1] = 0;
            }else{
                if(move.endCol>move.startCol)// White attack to the right
                    newboard[move.startRow+1][move.startCol+1] = 0;
                else // White attack to the left
                    newboard[move.startRow+1][move.startCol-1] = 0;
            }

        }
        newboard[move.endRow][move.endCol] = newboard[move.startRow][move.startCol];
        newboard[move.startRow][move.startCol] = 0;
        return newboard;
    }
    private int[][] deepCopyBoard(int[][] originalBoard) {
        int[][] copy = new int[originalBoard.length][originalBoard[0].length];
        for (int i = 0; i < originalBoard.length; i++) {
            System.arraycopy(originalBoard[i], 0, copy[i], 0, originalBoard[i].length);
        }
        return copy;
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

}
