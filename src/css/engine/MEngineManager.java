package css.engine;

import java.util.ArrayList;
import java.util.List;


public class MEngineManager {
	private final List<BaseMEngine> objects = new ArrayList<BaseMEngine>();
	private Object LK = new Object();
	public void start(BaseMEngine o) {
		synchronized (LK) {
			if (o != null && !objects.contains(o)) {
				objects.add(o);
			}			
		}
	}
	
	public void update(float delta) {
		synchronized (LK) {
			List<BaseMEngine> finishedObjs = new ArrayList<BaseMEngine>();
			for (BaseMEngine object : objects) {
				if (object.isFinished) {
					finishedObjs.add(object);
				}
			}

			objects.removeAll(finishedObjs);

			List<BaseMEngine> objs = new ArrayList<BaseMEngine>();
			objs.addAll(objects);

			for (BaseMEngine object : objs) {
				object.update(delta);
			}	
		}
	}
}
