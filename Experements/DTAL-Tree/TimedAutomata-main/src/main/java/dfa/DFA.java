package dfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import ta.TaLocation;
import ta.TaTransition;
import ta.ota.OTATranComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DFA {
    private String name;
    private Set<String> sigma;
    private List<DfaLocation> locations;
    private List<DfaTransition> transitions;

    public int size() {
        return locations.size();
    }

    public boolean containsSymbol(String symbol) {
        return sigma.contains(symbol);
    }

    public List<DfaTransition> getTransitions(String symbol) {
        List<DfaTransition> transitions1 = new ArrayList<>();
        transitions.stream().forEach(e->{
            if (StringUtils.equals(e.getSymbol(),symbol)){
                transitions1.add(e);
            }
        });
        return transitions1;
    }

//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("{\n\t").append("\"sigma\":[");
//        for (String action : getSigma()) {
//            sb.append("\"" + action + "\",");
//        }
//        sb.deleteCharAt(sb.length() - 1).append("],\n\t").append("\"init\":");
//        String init = getInitLocation().getId() + "";
//        sb.append(init).append(",\n\t").append("\"name\":\"").append(getName()).append("\"\n\t");
//        sb.append("\"s\":[");
//        for (TaLocation l : getLocations()) {
//            sb.append(l.getId()).append(",");
//        }
//        sb.deleteCharAt(sb.length() - 1).append("]\n\t\"tran\":{\n");
//
//        getTransitions().sort(new OTATranComparator(clock));
//        for (int i = 0; i < getTransitions().size(); i++) {
//            TaTransition t = getTransitions().get(i);
//            String reset = t.getResetClockSet().contains(clock) ? "r" : "n";
//            sb.append("\t\t\"").append(i).append("\":[")
//                    .append(t.getSourceId()).append(",")
//                    .append("\"").append(t.getSymbol()).append("\",")
//                    .append("\"").append(t.getTimeGuard(clock)).append("\",")
//                    .append(t.getTargetId()).append(", ").append(reset).append("]").append(",\n");
//        }
//        sb.deleteCharAt(sb.length() - 2);
//        sb.append("\t},\n\t").append("\"accpted\":[");
//        for (TaLocation l : getAcceptedLocations()) {
//            sb.append(l.getId()).append(",");
//        }
//        sb.deleteCharAt(sb.length() - 1).append("]\n}");
//        return sb.toString();
//    }

    private DfaLocation getInitLocation() {
        for (DfaLocation l : locations) {
            if (l.isInit()) {
                return l;
            }
        }
        return null;
    }
}
