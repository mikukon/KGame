package css.engine;

public class MMoveEngine extends BaseMEngine{
	private int type;
	private MLoaction orgLocaion;
	private float durition;
	private float time;
	private boolean bFirstStart = true;
	private int speedX;
	private int speedY;
	private int accelerateX;
	private int accelerateY;
	
	private int targetX;
	private int targetY;
	
	public MMoveEngine() {
		
	}
	
	public MMoveEngine registerAccessor(MEngineAccessor<?> accessor){
		this.accessor = (MEngineAccessor<Object>) accessor;
		return this;
	}
	
	public MMoveEngine type(int type){
		this.type = type;
		return this;
	}
	
	public MMoveEngine durition(float s){
		this.durition = s;
		return this;
	}
	
	public MMoveEngine attach(Object object){
		target = object;
		return this;
	}
	
	public void detach(){
		target = null;
	}
	
	public MMoveEngine setSpeed(int speedX, int speedY) {
		this.speedX = speedX;
		this.speedY = speedY;
		return this;
	}
	
	public MMoveEngine setAccelerate(int accelerateX, int accelerateY) {
		this.accelerateX = accelerateX;
		this.accelerateY = accelerateY;
		return this;
	}
	
	public int getCurrnetSpeed(int type){
		if (type == 0) {
			return (int) (speedX + accelerateX * time);			
		}else{
			return (int) (speedY + accelerateY * time);
		}
	}
	
	public MMoveEngine start(MEngineManager manager){
		if (isPlaying) {
			return this;	
		}
		
		isPlaying = true;
		
		manager.start(this);
		
		return this;
	}
	
	public MMoveEngine setTarget(int x, int y) {
		targetX = x;
		targetY = y;
		return this;
	}
	
	@Override
	public void update(float delta) {
		if (accessor == null || !isPlaying) {
			return;
		}
		
		time += delta;
		
		if (bFirstStart) {
			bFirstStart = false;
			float [] orgs = new float[2];
			accessor.getValues(target, type, orgs);
			orgLocaion = new MLoaction();
			orgLocaion.x = orgs[0];
			orgLocaion.y = orgs[1];
		}
		
		float [] beforeVals = new float[5];
		float [] afterVals = new float[5];
		
		accessor.getValues(target, type, beforeVals);
		
		if (accelerateX < 0 && getCurrnetSpeed(0) <= 0) {
			afterVals[0] = beforeVals[0];			
		}else{
			afterVals[0] = orgLocaion.x + (speedX * time + 0.5f * accelerateX * time * time);
		}
		
		if (accelerateY < 0 && getCurrnetSpeed(1) <= 0) {
			afterVals[1] = beforeVals[1];
		}else {
			afterVals[1] = orgLocaion.y + (speedY * time + 0.5f * accelerateY * time * time);			
		}

		if (targetX != 0) {
			if (targetX >= orgLocaion.x) {
				if (afterVals[0] >= targetX) {
					afterVals[0] = targetX;
				}
			}else{
				if (afterVals[0] < targetX) {
					afterVals[0] = targetX;
				}
			}
		}
		
		if (targetY != 0) {	
			if (targetY >= orgLocaion.y) {
				if (afterVals[1] >= targetY) {
					afterVals[1] = targetY;
				}
			}else{
				if (afterVals[1] < targetY) {
					afterVals[1] = targetY;
				}
			}
		}
		
		accessor.setValues(target, type, afterVals);
		
		boolean bFinished = false;
		if (targetX !=0 || targetY != 0) {
			boolean bXFinished = true;
			boolean bYFinished = true;
			if (targetX != 0) {
				if (targetX != afterVals[0]) {
					bXFinished = false;
				}
			}
			
			if (targetY != 0) {	
				if (targetY != afterVals[0]) {
					bYFinished = false;
				}
			}			
			
			if (bXFinished && bYFinished) {
				bFinished = true;
			}
		}
		
		if (getCurrnetSpeed(0) <= 0 && getCurrnetSpeed(1) <= 0) {
			bFinished  = true;
		}
		
		if (durition > 0 && time >= durition) {
			bFinished = true;
		}
		
		if (bFinished) {
			System.out.println("finished!!!");
			stop();
		}
	}
}
