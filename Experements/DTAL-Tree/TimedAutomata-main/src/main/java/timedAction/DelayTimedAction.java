package timedAction;

import lombok.AllArgsConstructor;
import lombok.Data;

public class DelayTimedAction extends TimedAction{

    public DelayTimedAction(String symbol, Double value) {
        super(symbol, value);
    }
}
