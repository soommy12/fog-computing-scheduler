package task.implementation;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Bartosz on 20.01.2018.
 */
public class Task implements Serializable {

    private int laxity;
    private int deadline;
    private boolean isHard;
    private Object data;

    public Task(Object data) {
        this.data = data;
    }
    public Task(){

    }

    public int getLaxity() {
        return laxity;
    }

    public void setLaxity(int laxity) {
        this.laxity = laxity;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public Object getData() {
        return data;
    }

    public boolean isHard() {
        return isHard;
    }

    public void setHard(boolean hard) {
        isHard = hard;
    }

    public static class LaxityComparator implements Comparator<Task>{
        @Override
        public int compare(Task t1, Task t2) {
            return t1.getLaxity() - t2.getLaxity();
        }
    }
}
