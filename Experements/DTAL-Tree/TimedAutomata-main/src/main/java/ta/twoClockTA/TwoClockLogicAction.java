package ta.twoClockTA;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.Clock;
import timedAction.TwoClockTimedAction;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoClockLogicAction extends TwoClockTimedAction {
    private String symbol;
    private Map<Clock, Double> clockValueMap;
    public void hello() {
        System.out.println("1");
    }
    public double getValue(Clock clock) {
        return clockValueMap.get(clock);
    }
    public double getValueClock(int i){
        return clockValueMap.get(i).doubleValue();
    }

}
