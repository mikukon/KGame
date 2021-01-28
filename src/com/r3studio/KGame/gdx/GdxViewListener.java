package com.r3studio.KGame.gdx;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.yiqiding.ktvbox.widget.KGDafengView.PrepareLis;

import css.engine.MEngineManager;

public class GdxViewListener implements ApplicationListener
{
	private int SCREEN_WIDTH = 1280;
	private int SCREEN_HEIGHT = 720;
	public static float DELTA_DISTANCE_SPEED = 0.15f;
	private Stage stage;
	private MEngineManager engineManager = new MEngineManager();
	private GdxDaFenView daFenView;

	public GdxViewListener(int width, int height)
	{
		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;
		DELTA_DISTANCE_SPEED *= (SCREEN_WIDTH / 1280.0);
	}
	
	@Override
	public void create() {
		stage = new Stage();
	}

	@Override
	public void dispose() {
		if(stage != null){
			stage.dispose();
			stage = null;
		}
	}

	@Override
	public void pause() {

	}

	@Override
	public void render() {
	    Gdx.gl20.glViewport( 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT );
	    Gdx.gl20.glClearColor( 0, 0, 0, 0 );
	    Gdx.gl20.glClear( GL20.GL_COLOR_BUFFER_BIT );

	    float delta = Gdx.graphics.getDeltaTime();
	    
	    engineManager.update(delta);

	    if(stage != null){
	    	stage.act(delta);
	        stage.draw();
	    }
	}

	@Override
	public void resize(int arg0, int arg1) {

	}

	@Override
	public void resume() {

	}
	
	public void prepare(final float [] dataArr, final int minPitch, final int maxPitch){
		Gdx.app.postRunnable(new Runnable() {
			
			@Override
			public void run() {
				if (daFenView != null) {
					daFenView.remove();
				}
				
				daFenView = new GdxDaFenView(engineManager);
				stage.addActor(daFenView);
				daFenView.prepare(dataArr, minPitch, maxPitch);
				pls.onPrepared(null, null);
			}
		});
	}
	
	public void start(){
		System.out.println("start");
		if (daFenView != null) {
			daFenView.start();
		}
	}
	
	public void updateCurPitch(Long time, int pitch){
		if (daFenView != null) {
			daFenView.updateCurPitch(time, pitch);			
		}
	}
	
	public void stop(){
		System.out.println("stop");
		if (daFenView != null) {
			daFenView.stop();
		}
	}
	
	public void exit() {
		System.out.println("exit");
		Gdx.app.postRunnable(new Runnable() {
			
			@Override
			public void run() {
				if (daFenView != null) {
					daFenView.remove();
					daFenView.dispose();
					daFenView = null;			
				}				
			}
		});
	}
	
	PrepareLis pls;

	public void setPrepareLis(PrepareLis l) {
		pls = l;
	}
}
