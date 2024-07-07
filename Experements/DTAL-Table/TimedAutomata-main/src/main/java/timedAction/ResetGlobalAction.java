package timedAction;

import ta.Clock;

import java.util.Set;

public class ResetGlobalAction extends ResetTimedAction {

    public ResetGlobalAction(String symbol, Double value, Set<Clock> reset) {
        super(symbol, value, reset);
    }
}
