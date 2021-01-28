package css.engine;

public abstract class BaseMEngine{
	protected float deltaTime;
	protected boolean isPlaying = false;
	protected boolean isPause = false;
	protected boolean isFinished = false;

	protected Object target;
	protected MEngineAccessor<Object> accessor;
	
	public void pause(){
		if (!isPlaying) {
			return;
		}
		
		isPlaying = false;
		isPause = true;
		
	}
	
	public void resume(){
		if (!isPause) {
			return;
		}
		
		isPause = false;
		isPlaying = true;
	}
	
	public void stop(){
		if (isFinished) {
			return;
		}
		
		isFinished = true;
		isPlaying = false;
		isPause = false;
	}

	public boolean isFinished(){
		return isFinished;
	}
	
	public abstract void update(float delta);
}
