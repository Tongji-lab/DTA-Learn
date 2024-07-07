package ta;
import java.util.ArrayList;
import java.util.List;

public class TimeGuardUtil {

    //求2个timeGuard的交集
    public static TimeGuard intersection(TimeGuard guard1, TimeGuard guard2){

        int lowBound,upperBound = 0;
        boolean lowerBoundOpen,upperBoundOpen;

        int guard1LowerBound = guard1.getLowerBound();
        int guard2LowerBound = guard2.getLowerBound();
        int guard1UpperBound = guard1.getUpperBound();
        int guard2UpperBound = guard2.getUpperBound();
        boolean guard1lowerBoundOpen = guard1.isLowerBoundOpen();
        boolean guard2lowerBoundOpen = guard2.isLowerBoundOpen();
        boolean guard1UpperBoundOpen = guard1.isUpperBoundOpen();
        boolean guard2UpperBoundOpen = guard2.isUpperBoundOpen();

        if(guard1LowerBound < guard2LowerBound){
            lowBound = guard1LowerBound;
            lowerBoundOpen = guard1lowerBoundOpen;
        }
        else if(guard1LowerBound == guard2LowerBound){
            lowBound = guard1LowerBound;
            lowerBoundOpen = guard1lowerBoundOpen || guard2lowerBoundOpen;
        }else {
            lowBound = guard2LowerBound;
            lowerBoundOpen = guard2lowerBoundOpen;
        }

        if(guard1UpperBound > guard2UpperBound){
            upperBound = guard1UpperBound;
            upperBoundOpen = guard1UpperBoundOpen;
        }else if(guard1UpperBound == guard2UpperBound){
            upperBound = guard1UpperBound;
            upperBoundOpen = guard1UpperBoundOpen || guard2UpperBoundOpen;
        }else {
            upperBound = guard2UpperBound;
            upperBoundOpen = guard2UpperBoundOpen;
        }

        if (lowBound > upperBound){
            return null;
        }
        if (lowBound == upperBound && (lowerBoundOpen || upperBoundOpen)){
            return null;
        }
        return new TimeGuard(lowerBoundOpen,upperBoundOpen,lowBound,upperBound);
    }


    //求一组互不包含的guard的补
    public static List<TimeGuard> complementary(List<TimeGuard> guardList){
        List<TimeGuard> complementaryList = new ArrayList<>();

        if(guardList.isEmpty()){
            TimeGuard guard = new TimeGuard(false,false,0, TimeGuard.MAX_TIME);
            complementaryList.add(guard);
            return complementaryList;
        }

        guardList.sort(new TimeGuardComparator());

        TimeGuard pre = guardList.get(0);
        if( pre.getLowerBound() != 0 || pre.isLowerBoundOpen() ){
            TimeGuard guard = new TimeGuard(false, pre.isLowerBoundClose(),0,pre.getLowerBound());
            complementaryList.add(guard);
        }

        for(int i = 1; i < guardList.size(); i++){
            TimeGuard current = guardList.get(i);
            if(pre.getUpperBound() != current.getLowerBound() || (pre.isUpperBoundOpen() && current.isLowerBoundOpen())){
                TimeGuard guard = new TimeGuard(pre.isUpperBoundClose(), current.isLowerBoundClose(),
                        pre.getUpperBound() , current.getLowerBound());
                complementaryList.add(guard);
            }
            pre = current;
        }

        if(pre.getUpperBound() != TimeGuard.MAX_TIME ){
            TimeGuard guard = new TimeGuard(pre.isUpperBoundClose(),true,
                    pre.getUpperBound() , TimeGuard.MAX_TIME);
            complementaryList.add(guard);
        }

        return complementaryList;
    }


}
