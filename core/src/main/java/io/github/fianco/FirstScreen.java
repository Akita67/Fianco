package io.github.fianco;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private Main game;
    private boolean click = false, clickAI = false, clickBVB = false;
    private Stage stage;
    private Texture fiancoSymbol;
    private Skin menuSkin;
    private TextButton PVP, PVAI, BVB;

    public FirstScreen(Main game){
        super();
        this.game = game;
        stage = new Stage(new FillViewport(1280, 720));
        menuSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        fiancoSymbol = new Texture(Gdx.files.internal("fianco_cell.png"));
        PVP = new TextButton("Play: 2 Player", menuSkin);
        PVP.setColor(Color.BLACK);
        PVP.setPosition(325, 170);
        PVP.setSize(200, 100);

        PVAI = new TextButton("Play: AI", menuSkin);
        PVAI.setColor(Color.BLACK);
        PVAI.setPosition(525, 170);
        PVAI.setSize(200, 100);

        BVB = new TextButton("Play: Bot vs Bot", menuSkin);
        BVB.setColor(Color.BLACK);
        BVB.setPosition(725, 170);
        BVB.setSize(200, 100);

        stage.addActor(BVB);
        stage.addActor(PVP);
        stage.addActor(PVAI);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.90f, 1.00f, 1.00f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        PVP.addListener(new ClickListener() {
            public void touchUp(InputEvent e, float x, float y, int point, int button) {
                click = true;
            }
        });

        if (click) {
            this.dispose();
            game.setScreen(new BoardScreen(game));
        }
        PVAI.addListener(new ClickListener() {
            public void touchUp(InputEvent e, float x, float y, int point, int button) {
                clickAI = true;
            }
        });

        if (clickAI) {
            this.dispose();
            game.setScreen(new PVBSelectionScreen(game));
        }
        BVB.addListener(new ClickListener() {
            public void touchUp(InputEvent e, float x, float y, int point, int button) {
                clickBVB = true;
            }
        });

        if (clickBVB) { // Bot vs Bot
            this.dispose();
            game.setScreen(new BVBSelectionScreen(game, true, true));
        }

        stage.act(delta);
        stage.draw();

        game.sr.begin(ShapeRenderer.ShapeType.Filled);
        game.mainBatch.begin();

        game.font.setColor(Color.BLACK);
        game.font.getData().setScale(3, 3);

        game.font.getData().setScale(3);
        game.font.draw(game.mainBatch, "Fianco : Java edition", 430, 350);
        game.mainBatch.draw(fiancoSymbol, 525, 375, 225f, 225f);

        game.mainBatch.end();
        game.sr.end();
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
