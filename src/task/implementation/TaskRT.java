package task.implementation;

import java.util.Comparator;

/**
 * Created by Bartosz on 07.01.2018.
 */
public class TaskRT extends TaskAbstract {

    private boolean isHard; // true if hard, false if soft
    private int arrayToSort[];
    private int laxity = 0;

//    public TaskRT(boolean isHard, int deadline, int arr[]) {
    public TaskRT(Object data) {
//        this.isHard = isHard;
//        this.deadline = deadline;
        this.arrayToSort = (int[]) data;
    }

    @Override
    public Object solve() {
        int n = this.arrayToSort.length;
        int tmp;
        for(int i = 0; i < n ; i++){
            for(int j = 1; j < (n-i); j++){
                if(arrayToSort[j-1] > arrayToSort[j]){
                    tmp = arrayToSort[j-1];
                    arrayToSort[j-1] = arrayToSort[j];
                    arrayToSort[j] = tmp;
                }
            }
        }
        return arrayToSort;
    }

    public int getLaxity() {
        return laxity;
    }

    public void setLaxity(int laxity) {
        this.laxity = laxity;
    }

    public static class laxityComparator implements Comparator<TaskRT>{
        @Override
        public int compare(TaskRT t1, TaskRT t2) {
            return t1.getLaxity() - t2.getLaxity();
        }
    }
}
