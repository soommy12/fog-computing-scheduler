package task.implementation;

/**
 * Created by Bartosz on 07.01.2018.
 */
public class Pair {

    private int min;
    private int max;

    public Pair(int min, int max) {
        if(min > max){
            int tmp = min;
            min = max;
            max = tmp;
        }
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
