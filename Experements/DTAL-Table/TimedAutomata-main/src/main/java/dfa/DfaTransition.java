package dfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.Clock;
import ta.TimeGuard;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DfaTransition {
    private DfaLocation sourceLocation;
    private DfaLocation targetLocation;
    private String symbol;

    public String getSourceId() {
        return sourceLocation.getId();
    }

    public String getTargetId(){
        return targetLocation.getId();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(sourceLocation.getId()).append(", ").append(symbol);
        sb.append(", ").append(targetLocation.getId()).append("]");
        return sb.toString();
    }
}
