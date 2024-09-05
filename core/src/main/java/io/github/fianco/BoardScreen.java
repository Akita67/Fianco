package io.github.fianco;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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


    public BoardScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1000, 600); // Set your camera dimensions
        shapeRenderer = new ShapeRenderer();

        cellClicked = new boolean[gridSize][gridSize]; // Initialize cell click tracking

        font = new BitmapFont(); // Default LibGDX font
        layout = new GlyphLayout(); // Used to position text

        // Load stone textures
        blackStoneTexture = new Texture(Gdx.files.internal("black_stone.png"));
        whiteStoneTexture = new Texture(Gdx.files.internal("white_stone.png"));


        // Set this screen as the input processor to capture clicks
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.mainBatch.setProjectionMatrix(camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the cells, highlight clicked ones
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (cellClicked[row][col]) {
                    shapeRenderer.setColor(Color.BLUE); // Highlight clicked cells (blue)
                } else {
                    shapeRenderer.setColor(Color.GRAY); // Default cell color (white)
                }
                shapeRenderer.rect(col * cellSize, row * cellSize, cellSize, cellSize);
            }
        }

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

        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            cellClicked[row][col] = !cellClicked[row][col]; // Toggle cell click state
        }

        return true; // Return true to indicate the event was handled
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

                // Draw stones
                if (row == 8 || (row == 7 && ( col == 1 || col == 7 )) || (row == 6 && ( col == 2 || col == 6 )) || (row == 5 && ( col == 3 || col == 5 )) ) { // 9th row (index 8) for black stones
                    game.mainBatch.draw(blackStoneTexture, col * cellSize, row * cellSize, cellSize, cellSize);
                } else if (row == 0 || (row == 1 && ( col == 1 || col == 7 )) || (row == 2 && ( col == 2 || col == 6 )) || (row == 3 && ( col == 3 || col == 5 )) ) { // 1st row (index 0) for white stones
                    game.mainBatch.draw(whiteStoneTexture, col * cellSize, row * cellSize, cellSize, cellSize);
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
