package timedAction;

import ta.Clock;

import java.util.Set;

public class ResetDelayAction extends ResetTimedAction{

    public ResetDelayAction(String symbol, Double value, Set<Clock> reset) {
        super(symbol, value, reset);
    }

}