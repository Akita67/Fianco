package io.github.fianco;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    /**
     * Represent the render of the board
     */
    public ShapeRenderer sr;
    /**
     * Represent the render of the board
     */
    public SpriteBatch mainBatch;
    /**
     * Put the font of our board
     */
    public BitmapFont font;

    @Override
    public void create() {
        mainBatch = new SpriteBatch();
        font = new BitmapFont();
        sr = new ShapeRenderer();
        this.setScreen(new FirstScreen(this));
    }
}
