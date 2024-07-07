package timedAction;

import lombok.Data;
import lombok.Getter;
import ta.Clock;

import java.util.Objects;
import java.util.Set;


public class ResetTimedAction extends TimedAction {
    @Getter
    private Set<Clock> reset;

    public ResetTimedAction(String symbol, Double value, Set<Clock> reset) {
        super(symbol, value);
        this.reset = reset;
    }
    public void setReset(Set<Clock> reset){
        this.reset=reset;
    }
    public Set<Clock> getReset(){
        return this.reset;
    }
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(")
                .append(getSymbol())
                .append(",")
                .append(getValue())
                .append(",")
                .append(getReset())
                .append(")");
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ResetTimedAction)) return false;
        if (!super.equals(o)) return false;

        ResetTimedAction that = (ResetTimedAction) o;

        return getReset() == that.getReset();
    }

    //   @Override
//    public int hashCode() {
//        int result = super.hashCode();
//        result = 31 * result + (isReset() ? 1 : 0);
//        return result;
//    }
}
