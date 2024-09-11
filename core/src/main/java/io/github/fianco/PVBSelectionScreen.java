package io.github.fianco;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PVBSelectionScreen implements Screen {
    Main game;
    Skin menuSkin;
    Stage stage;
    SelectBox<String> position;
    SelectBox<String> bots1;
    TextButton confirm;
    int index;

    public PVBSelectionScreen(Main game) {
        super();
        this.game = game;
        stage = new Stage(new FillViewport(1280, 720));
        menuSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        bots1 = new SelectBox<String>(menuSkin);
        bots1.setItems("Random bot", "FitnessGroup bot", "MaxN Paranoid", "OneLookAhead bot", "Depth Limiting Tree bot","MCTS", "Predict bot (supervised learning)", "BOB (Double DQN)");
        bots1.setPosition(500, 350);
        bots1.setSize(200, 50);

        position = new SelectBox<String>(menuSkin);
        position.setItems("Be Player 1", "Be Player 2");
        position.setPosition(200, 350);
        position.setSize(200, 50);

        confirm = new TextButton("Confirm", menuSkin);
        confirm.setColor(Color.BLACK);
        confirm.setPosition(500, 300);

        stage.addActor(bots1);
        stage.addActor(position);
        stage.addActor(confirm);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.90f, 1.00f, 1.00f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        bots1.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });
        position.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

            }
        });

        confirm.addListener(new ClickListener() {
            public void touchUp(InputEvent e, float x, float y, int point, int button) {
                index = bots1.getSelectedIndex();
                if(position.getSelectedIndex()==0){
                    game.setScreen(new BoardScreen(game,false,true,0,index));
                }else{
                    game.setScreen(new BoardScreen(game,true,false,index,0));
                }
            }
        });

    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
    }
}
