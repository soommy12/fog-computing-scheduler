package task.implementation;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Bartosz on 20.01.2018.
 */
public class Task implements Serializable {

    private int laxity;

    public Task(){
    }

    public int getLaxity() {
        return laxity;
    }

    public void setLaxity(int laxity) {
        this.laxity = laxity;
    }

    public static class LaxityComparator implements Comparator<Task>{
        @Override
        public int compare(Task t1, Task t2) {
            return t1.getLaxity() - t2.getLaxity();
        }
    }
}
