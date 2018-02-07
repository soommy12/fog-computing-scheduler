package task.implementation;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Bartosz on 20.01.2018.
 */
public class Task implements Serializable {

    private int laxity;
    private int id;
    private long arrivalt;

    public Task(){
    }

    public int getLaxity() {
        return laxity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getArrivalt() {
        return arrivalt;
    }

    public void setArrivalt(long arrivalt) {
        this.arrivalt = arrivalt;
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
