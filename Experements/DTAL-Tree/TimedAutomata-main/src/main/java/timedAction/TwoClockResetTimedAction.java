package timedAction;

import lombok.Data;
import lombok.Getter;
import ta.Clock;
import java.util.Set;
import java.util.Objects;
import java.util.Map;
public class TwoClockResetTimedAction extends TwoClockTimedAction {
    @Getter
    private Set<Clock> resetClockSet;
   // @Getter
   // private boolean reset2;
   // private String symbol;
    // private Map<Clock, Double> clockValueMap;

    public TwoClockResetTimedAction(String symbol, Set<Clock> resetClockSet, Map<Clock, Double> clockValueMap) {
        super(symbol,clockValueMap);
        this.resetClockSet = resetClockSet;
    }

   /* @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(")
                .append(getSymbol())
                .append(",")
                .append(getValue1())
                .append(",")
                .append(getValue2())
                .append(",")
                .append(isReset1()?"r":"n")
                .append(",")
                .append(isReset2()?"r":"n")
                .append(")");
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ResetTimedAction)) return false;
        if (!super.equals(o)) return false;

        ResetTimedAction that = (ResetTimedAction) o;

        return isReset1() == that.isReset();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isReset1() ? 1 : 0);
        return result;
    }*/
}
