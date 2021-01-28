package css.engine;

public class MEngine extends BaseMEngine{
	private int type;
	private float [] orgValuses;
	private float [] toValuses;
	private float durition;
	private int repeatCount;
	private float time;
	private boolean bFirstStart = true;
	private float speed;
	private int accelerate;
	private float delayed;
	private OnEndListener listener;
	
	public MEngine() {
		
	}
	
	public interface OnEndListener{
		public void end(MEngine engine);
	}
	
	public MEngine setOnEndListener(OnEndListener listener) {
		this.listener = listener;
		return this;
	}
	
	public MEngine registerAccessor(MEngineAccessor<?> accessor){
		this.accessor = (MEngineAccessor<Object>) accessor;
		return this;
	}
	
	public MEngine type(int type){
		this.type = type;
		return this;
	}
	
	public MEngine to(float ... values){
		this.orgValuses = new float[values.length];
		this.toValuses = new float[values.length];
		for (int i = 0; i < values.length; i++) {
			this.toValuses[i] = values[i];
		}
		
		return this;
	}
	
	public MEngine durition(float s){
		this.durition = s;
		return this;
	}
	
	public MEngine repeat(int count){
		this.repeatCount = count;
		return this;
	}
	
	public MEngine attach(Object object){
		target = object;
		return this;
	}
	
	public void detach(){
		target = null;
	}
	
	public MEngine speed(float speed) {
		this.speed = speed;
		return this;
	}
	
	public MEngine accelerate(int accelerate) {
		this.accelerate = accelerate;
		return this;
	}

	public MEngine delay(float ms){
		this.delayed = ms;
		return this;
	}
	
	public int getCurrnetSpeed(){
		return (int) (speed + accelerate * time);
	}
	
	public MEngine start(MEngineManager manager){
		if (isPlaying) {
			return this;	
		}
		
		isPlaying = true;
		
		manager.start(this);
		
		return this;
	}
	
	public Object getTarget(){
		return target;
	}
	
	@Override
	public void update(float delta) {
		if (accessor == null || !isPlaying) {
			return;
		}
		
		time += delta;
		if (delayed != 0){
			if (time < delayed) {
				return;
			}else{
				delayed = 0;
				time = 0;
			}
		}

		if (bFirstStart) {
			bFirstStart = false;
			accessor.getValues(target, type, orgValuses);
		}
		
		float [] beforeVals = new float[5];
		float [] afterVals = new float[5];
		
		accessor.getValues(target, type, beforeVals);

		for (int i = 0; i < toValuses.length; i++) {
			if (toValuses[i] != beforeVals[i]) {
				if (accelerate == 0 && speed == 0) {
//					afterVals[i] = beforeVals[i] + ((toValuses[i] - orgValuses[i]) * delta / durition);
					afterVals[i] = orgValuses[i] + ((toValuses[i] - orgValuses[i]) * time / durition);
				}else {
					afterVals[i] = orgValuses[i] + (speed * time + 0.5f * accelerate * time * time);
				}
				
				if (toValuses[i] > orgValuses[i]) {
					if (afterVals[i] > toValuses[i]) {
						afterVals[i] = toValuses[i];
					}
				}else{
					if (afterVals[i] < toValuses[i]) {
						afterVals[i] = toValuses[i];
					}
				}
			}else {
				afterVals[i] = toValuses[i];
			}
		}

		accessor.setValues(target, type, afterVals);
		
		boolean bFinish = true;
		for (int i = 0; i < toValuses.length; i++) {
			if (toValuses[i] != afterVals[i]) {
				bFinish = false;
				break;
			}
		}
		
		if (bFinish) {
			if (repeatCount == -1){
				time = 0;
				accessor.setValues(target, type, orgValuses);
			} else if (repeatCount > 0) {
				repeatCount--;
				time = 0;
				accessor.setValues(target, type, orgValuses);
			}else{
				stop();
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (listener != null){
			listener.end(this);
		}
	}
}
