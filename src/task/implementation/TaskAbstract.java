package task.implementation;

import java.io.Serializable;

/**
 * Created by Bartosz on 07.01.2018.
 */

/**
 *  Main class to for polimorphism
 */
public abstract class TaskAbstract implements Serializable{
    int deadline;
    public abstract Object solve();

}
