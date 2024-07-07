package ta;

import java.util.Comparator;

public class TimeGuardComparator implements Comparator<TimeGuard>  {
    @Override
    public int compare(TimeGuard o1, TimeGuard o2) {
        int val1 = o1.getLowerBound() - o2.getLowerBound();
        if (val1 != 0){
            return val1;
        }
        return o1.isUpperBoundClose()?-1:1;
    }

}

