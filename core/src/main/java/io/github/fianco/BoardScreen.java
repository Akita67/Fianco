package io.github.fianco;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.InputAdapter;

public class BoardScreen extends InputAdapter implements Screen {

    private Main game;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout;
    private Texture blackStoneTexture;
    private Texture whiteStoneTexture;

    private final int gridSize = 9;  // 9x9 grid
    private final int cellSize = 60; // Size of each cell in pixels
    private boolean[][] cellClicked; // Tracks clicked cells
    private int[][] board; // Tracks where stones are placed (0 = empty, 1 = white, 2 = black)

    private int selectedRow = -1; // Track the selected stone's row
    private int selectedCol = -1; // Track the selected stone's column
    private boolean isBlackTurn = false; // Track whose turn it is (alternating between white and black stones)

    public BoardScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1000, 600); // Set your camera dimensions
        shapeRenderer = new ShapeRenderer();

        cellClicked = new boolean[gridSize][gridSize]; // Initialize cell click tracking
        board = new int[gridSize][gridSize]; // Initialize the board

        font = new BitmapFont(); // Default LibGDX font
        layout = new GlyphLayout(); // Used to position text

        // Load stone textures
        blackStoneTexture = new Texture(Gdx.files.internal("black_stone.png"));
        whiteStoneTexture = new Texture(Gdx.files.internal("white_stone.png"));

        // Set initial positions for the stones
        setInitialStonePositions();

        // Set this screen as the input processor to capture clicks
        Gdx.input.setInputProcessor(this);
    }

    private void setInitialStonePositions() {
        // Set white stones in the first row
        for (int col = 0; col < gridSize; col++) {
            board[0][col] = 1; // 1 = white stone
        }
        board[1][1]=1;board[1][7]=1;board[2][2]=1;board[2][6]=1;board[3][3]=1;board[3][5]=1;

        // Set black stones in the last row
        for (int col = 0; col < gridSize; col++) {
            board[8][col] = 2; // 2 = black stone
        }
        board[7][1]=2;board[7][7]=2;board[6][2]=2;board[6][6]=2;board[5][3]=2;board[5][5]=2;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.mainBatch.setProjectionMatrix(camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        makeGrid();
    }

    private String getChessNotation(int row, int col) {
        char file = (char) ('a' + col); // 'a' to 'i'
        int rank = 1 + row; // 1 to 9
        return "" + file + rank;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));

        int col = (int) (worldCoordinates.x / cellSize);
        int row = (int) (worldCoordinates.y / cellSize);

        if ( (row >= 0 && row < gridSize && col >= 0 && col < gridSize)) {
            if (selectedRow == -1 && selectedCol == -1) {
                // Select a stone depending on the player turn
                if (board[row][col] == 1 && !isBlackTurn) {
                    selectedRow = row;
                    selectedCol = col;
                } else if (board[row][col] == 2 && isBlackTurn) {
                    selectedRow = row;
                    selectedCol = col;
                }
            } else {
                // Attempt to move the stone if a stone is already selected
                moveStone(row, col);
            }
        }
        System.out.println("This is selected row " + selectedRow);
        System.out.println("This is  row " + row);

        return true; // Return true to indicate the event was handled
    }

    private void moveStone(int row, int col) {
        // Check if the move is valid (forward, left, right, and only by one cell)
        if(board[row][col] == 0){
            System.out.println("helooo");
            if ( ((!isBlackTurn) && (row - selectedRow == 1 && col == selectedCol)) || ((isBlackTurn) && (selectedRow - row == 1 && col == selectedCol)) || // Forward
                (Math.abs(col - selectedCol) == 1 && row == selectedRow) ) {  // Left or right

                // Move the stone
                board[row][col] = board[selectedRow][selectedCol];
                board[selectedRow][selectedCol] = 0;

                // Reset the selection
                selectedRow = -1;
                selectedCol = -1;

                // Change turns
                isBlackTurn = !isBlackTurn;

            }
        }
    }

    public void makeGrid(){
        // Draw the grid lines
        shapeRenderer.setColor(0, 0, 0, 1); // Grid color (black)
        for (int i = 0; i <= gridSize; i++) {
            // Draw vertical lines
            shapeRenderer.line(i * cellSize, 0, i * cellSize, gridSize * cellSize);
            // Draw horizontal lines
            shapeRenderer.line(0, i * cellSize, gridSize * cellSize, i * cellSize);
        }

        shapeRenderer.end();

        // Render the chess notations
        game.mainBatch.begin();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                String notation = getChessNotation(row, col);
                layout.setText(font, notation);

                // Center the text in the cell
                float x = col * cellSize + (cellSize - layout.width) / 2;
                float y = row * cellSize + (cellSize + layout.height) / 2;

                font.draw(game.mainBatch, notation, x, y);

                // Draw stones based on the board state
                if (board[row][col] == 1) { // White stone
                    game.mainBatch.draw(whiteStoneTexture, col * cellSize, row * cellSize, cellSize, cellSize);
                } else if (board[row][col] == 2) { // Black stone
                    game.mainBatch.draw(blackStoneTexture, col * cellSize, row * cellSize, cellSize, cellSize);
                }
            }
        }
        game.mainBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void show() {
        // Initialization code if needed
    }

    @Override
    public void hide() {
        // Dispose resources when hidden
    }

    @Override
    public void pause() {
        // Pause logic if needed
    }

    @Override
    public void resume() {
        // Resume logic if needed
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        blackStoneTexture.dispose();
        whiteStoneTexture.dispose();
    }
}
