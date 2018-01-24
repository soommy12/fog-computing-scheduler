package task.implementation;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Bartosz on 07.01.2018.
 */
public class TaskN extends TaskAbstract {

    private Pair range;

    public TaskN(Pair range) {
        this.deadline = 0;
        this.range = range;
    }

    //min max
    @Override
    public Object solve() {
        return ThreadLocalRandom.current().nextInt(range.getMin(), range.getMax() + 1);
    }
}
