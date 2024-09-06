package io.github.fianco;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameLogic {
    private final Main game;
    private int[][] board;
    public BitmapFont font;
    private boolean blackWins = false;  // Track if black has won
    private boolean whiteWins = false;  // Track if white has won
    private GlyphLayout layout;
    BoardScreen boardScreen;

    public GameLogic(Main game){
        this.game = game;
        initialize();
    }

    public void initialize(){
        font = new BitmapFont(); // Create font for displaying text
        font.setColor(Color.BLACK); // Set font color
        layout = new GlyphLayout(); // Initialize layout to center text
    }

    public void winCondition(BoardScreen boardScreen, int[][] board, SpriteBatch batch){
        this.board = board;

        // Check if a black stone has reached row 0 (top)
        for (int i = 0; i < board[0].length; i++) {
            if (board[0][i] == 2) {
                blackWins = true;
                break;  // Exit the loop if a black stone has reached row 0
            }
        }

        // Check if a white stone has reached row 8 (bottom)
        for (int i = 0; i < board[8].length; i++) {
            if (board[8][i] == 1) {
                whiteWins = true;
                break;  // Exit the loop if a white stone has reached row 8
            }
        }

        // Render win message if a player has won

        if (blackWins) {
            if(boardScreen.isPlayer1White)
                boardScreen.addPlayer2Score();
            else
                boardScreen.addPlayer1Score();
            resetBoard();
            boardScreen.resetGrid();
            boardScreen.changePlayer1Side();
        } else if (whiteWins) {
            if(boardScreen.isPlayer1White)
                boardScreen.addPlayer1Score();
            else
                boardScreen.addPlayer2Score();
            resetBoard();
            boardScreen.resetGrid();
            boardScreen.changePlayer1Side();
        }
    }

    private void renderWinMessage(SpriteBatch batch, String message) {
        layout.setText(font, message); // Prepare the text layout
        System.out.println(layout.width);
        System.out.println(layout.height);

        // Center the message on the screen
        float x = 600;
        float y = 500;

        // Draw the message on the screen
        game.mainBatch.begin();
        font.draw(batch, layout, x, y);
        game.mainBatch.end();
    }
    // Method to reset the board state
    public void resetBoard() {
        // Reset the win conditions
        blackWins = false;
        whiteWins = false;
    }
    public void finished(int num1, int num2){
        if(num1>=2){
            game.setScreen(new CongratulationsScreen("Congratulations! Player 1 won the game!"));
        }
        else if(num2>=2){
            game.setScreen(new CongratulationsScreen("Congratulations! Player 2 won the game!"));
        }
    }
}
