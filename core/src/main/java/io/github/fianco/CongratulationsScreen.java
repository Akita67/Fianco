package io.github.fianco;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class CongratulationsScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private String message;

    public CongratulationsScreen(String message) {
        this.message = message;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(3);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        font.setColor(Color.PINK);
        font.draw(batch, message, 200, 400);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
