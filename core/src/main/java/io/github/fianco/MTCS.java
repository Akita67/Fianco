package io.github.fianco;

import java.util.List;

public class MTCS extends Bot{

    public MTCS(boolean isBlack, int[][] board){
        super(isBlack, board);
    }

    public void calculate(BoardScreen boardScreen, int[][] board){

    }

    private Move runMCTS(BoardScreen boardScreen, int[][] board) {

    }
    private Node selection(Node node) { // TODO Selection

    }
    private Node expansion(Node node) { // TODO Add a child to the current node based on the possible moves

    }
    private int simulation(Node node) { // TODO Play Random


    }
    private void backpropagation(Node node, int result) { // TODO Backpropagate the information about winning or losing to each node

    }
    private List<Move> getAllPossibleMoves(BoardScreen boardScreen) { // TODO get all the moves of a current boardstate

    }

}
