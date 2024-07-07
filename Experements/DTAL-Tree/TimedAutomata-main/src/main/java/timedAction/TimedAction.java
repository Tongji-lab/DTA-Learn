package timedAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class TimedAction {
    private String symbol;
    private Double value;

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(")
                .append(getSymbol())
                .append(",")
                .append(getValue())
                .append(")");
        return stringBuilder.toString();
    }
}
