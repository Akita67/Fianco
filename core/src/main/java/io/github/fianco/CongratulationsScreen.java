package io.github.fianco;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class CongratulationsScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont font;
    private String message;

    private Array<Texture> gifFrames; // Array to hold GIF frames
    private float frameDuration = 0.1f; // Duration for each frame (in seconds)
    private float frameTimer = 0f; // Timer to keep track of the frame time
    private int currentFrameIndex = 0; // Current frame index

    public CongratulationsScreen(String message) {
        this.message = message;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(3);

        // Load the GIF frames from the assets folder
        gifFrames = new Array<>();
        for (int i = 1; i <= 3; i++) { // Assuming 10 frames for the GIF
            gifFrames.add(new Texture(Gdx.files.internal("you-won-youwin-" + i + ".png"))); // Load each frame: you-won-youwin-1.png, you-won-youwin-2.png, etc.
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update frame timer
        frameTimer += delta;
        if (frameTimer >= frameDuration) {
            frameTimer = 0f;
            currentFrameIndex = (currentFrameIndex + 1) % gifFrames.size; // Loop through frames
        }

        // Start rendering
        batch.begin();

        // Draw the current GIF frame
        Texture currentFrame = gifFrames.get(currentFrameIndex);
        batch.draw(currentFrame, 50, 110, 500, 500); // Draw GIF frames in the center (adjust size as needed)

        // Draw the congratulations message
        font.setColor(Color.PINK);
        font.draw(batch, message, 250, 400);

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
        for (Texture frame : gifFrames) {
            frame.dispose(); // Dispose of each frame
        }
    }
}
