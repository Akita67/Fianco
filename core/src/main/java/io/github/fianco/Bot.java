package io.github.fianco;


public abstract class Bot {
    protected long runtime;
    protected boolean isBlack;
    protected int [][] board;

    /**
     * constructor for initialising the runtime of the bot.
     */
    public Bot(boolean isBlack, int [][]board) {
        this.runtime=0;
        this.isBlack = isBlack;
        this.board = board;
    }


    /**
     * @param boardScreen
     * This is the general method for performing a move for a bot.
     */
    public void execMove(BoardScreen boardScreen, int [][]board) {
        this.board = board;
        runtime=0;
        long startTime = System.nanoTime();
        calculate(boardScreen,board);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        runtime += duration/1000;
    }

    /**
     * @param boardScreen
     * To be implemented by the class who extends this one affects the field in some way.
     */

    public void calculate(BoardScreen boardScreen, int[][] board) {}


    /**
     * @return runtime of the bot
     */
    public long getRuntime() {
        return runtime;
    }
    public void changeSide(){}
}
