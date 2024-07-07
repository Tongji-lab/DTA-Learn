package ta.twoClockTA;
import ta.Clock;
import timedAction.TimedAction;
import timedAction.TwoClockTimedAction;

import java.util.Map;

public class TwoClockLogicTimedAction extends TwoClockTimedAction {
    public TwoClockLogicTimedAction(String symbol, Map<Clock, Double> clockValueMap) {
        super(symbol, clockValueMap);
    }
}
