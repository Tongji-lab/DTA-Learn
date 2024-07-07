package defaultTeacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import ta.TimeGuard;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ActionGuardsSet {
    private String symbol;
    private List<TimeGuard> timeGuards;

    public List<Double> getLowerValue(){
        List<Double> lowerValues = new ArrayList<>();
        for (TimeGuard timeGuard :
                timeGuards) {
            if (timeGuard.isLowerBoundClose()) {
                lowerValues.add(timeGuard.getLowerBound() * 1.0);
            }
            else {
                lowerValues.add(timeGuard.getLowerBound() + 0.5);
            }
        }
//        if (timeGuard.isLowerBoundClose()){
//            return timeGuard.getLowerBound()*1.0;
//        }
//        return timeGuard.getLowerBound()+0.5;
        return lowerValues;
    }
}
