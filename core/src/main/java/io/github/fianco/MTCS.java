package io.github.fianco;

import java.util.List;

public class MTCS extends Bot{

    public MTCS(boolean isBlack, int[][] board){
        super(isBlack, board);
    }

    public void calculate(BoardScreen boardScreen, int[][] board){

    }

    private Move runMCTS(BoardScreen boardScreen, int[][] board) {
        return null;
    }
    private Node selection(Node node) { // TODO Selection
        return null;
    }
    private Node expansion(Node node) { // TODO Add a child to the current node based on the possible moves
        return null;
    }
    private int simulation(Node node) { // TODO Play Random
        return 0;

    }
    private void backpropagation(Node node, int result) { // TODO Backpropagate the information about winning or losing to each node

    }
    private List<Move> getAllPossibleMoves(BoardScreen boardScreen) { // TODO get all the moves of a current boardstate
        return null;
    }

}
