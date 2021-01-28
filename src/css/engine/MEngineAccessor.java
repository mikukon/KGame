package css.engine;

public interface MEngineAccessor<T> {
	
	public int getValues(T target, int tweenType, float[] returnValues);
	
	public void setValues(T target, int tweenType, float[] newValues);
}
