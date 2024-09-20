package io.github.fianco;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class Node {
    Node parent;
    List<Node> children;
    int[][] board;
    Move move;
    int wins;
    int visits;
    boolean isBlackNode;

    public Node(Node parent, int[][] board, Move move, boolean isBlackNode) {
        this.parent = parent;
        this.board = board;
        this.move = move;
        this.children = new ArrayList<>();
        this.wins = 0;
        this.visits = 0;
        this.isBlackNode = isBlackNode;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public boolean isTerminal() {
        // Add your terminal check logic here (e.g., game over)
        return false;
    }

    public int getResult() {
        // Add your logic to determine result of the game at this node (win/loss)
        return 0;
    }

    public Node getBestUCTChild() {
        Node bestNode = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Node child : children) {
            double uctValue = (child.wins / (double) child.visits) +
                Math.sqrt(2 * Math.log(this.visits) / child.visits);
            if (uctValue > bestValue) {
                bestValue = uctValue;
                bestNode = child;
            }
        }

        return bestNode;
    }

    public Node getRandomChild() {
        return children.get(new Random().nextInt(children.size()));
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public Node getBestChild() {
        Node bestNode = null;
        int maxVisits = Integer.MIN_VALUE;

        for (Node child : children) {
            if (child.visits > maxVisits) {
                maxVisits = child.visits;
                bestNode = child;
            }
        }

        return bestNode;
    }
}
