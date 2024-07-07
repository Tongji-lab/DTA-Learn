package ta.twoClockTA;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.Clock;
import timedAction.TimedAction;
import timedAction.TimedAction;
import java.util.Map;
import java.util.Set;
//import timedAction.ResetTimedAction;
import timedAction.TwoClockResetTimedAction;
@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class TwoClockResetLogicAction extends TwoClockResetTimedAction{
    private String symbol;
    private Set<Clock> resetClockSet;
    private Map<Clock, Double> clockValueMap;
    private Map<Double,Double> TwoClockValue;

    public boolean isReset(Clock clock){
        return resetClockSet.contains(clock);
    }

    public double getValue(Clock clock) {
        return clockValueMap.get(clock);
    }

    public TwoClockResetLogicAction(String symbol,Set<Clock> resetClockSet,Map<Clock,Double> clockValueMap){
        super(symbol,resetClockSet,clockValueMap);

    }
//    public TwoClockLogicTimedAction logicTimedAction(){
//        return new TwoClockLogicTimedAction(getSymbol(),clockValueMap);
//    }
    public TwoClockLogicAction logicTimedAction(){
        return new TwoClockLogicAction(getSymbol(),clockValueMap);
    }
}
