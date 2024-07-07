package timedAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.Clock;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TwoClockTimedAction {
    private String symbol;
    //private Double value1;
    //private Double value2;
    private Map<Clock, Double> clockValueMap;

   // @Override
    /*public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(")
                .append(getSymbol())
                .append(",")
                .append(getValue1())
                .append(",")
                .append(getValue2())
                .append(")");
        return stringBuilder.toString();
    }*/
}