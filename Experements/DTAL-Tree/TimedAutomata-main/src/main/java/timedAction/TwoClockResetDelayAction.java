package timedAction;

import ta.Clock;

import java.util.Map;
import java.util.Set;

public class TwoClockResetDelayAction  extends TwoClockResetTimedAction{
    public TwoClockResetDelayAction(String symbol, Set<Clock> resetClockSet, Map<Clock, Double> clockValueMap)
    {
        super(symbol, resetClockSet, clockValueMap);
    }
}
