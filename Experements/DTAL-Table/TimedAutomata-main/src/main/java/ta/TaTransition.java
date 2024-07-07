package ta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaTransition {
    private TaLocation sourceLocation;
    private TaLocation targetLocation;
    private String symbol;
    private EdgeTimeGuard clockTimeGuardMap;
    private Set<Clock> resetClockSet;


    public TimeGuard getTimeGuard(Clock clock){
        return clockTimeGuardMap.clockTimeGuardMap.get(clock);
    }
    public EdgeTimeGuard getEdgeTimeGuard(){
        return this.clockTimeGuardMap;
    }
    public boolean isReset(Clock clock){
        return resetClockSet.contains(clock);
    }

//    public boolean isPass(String symbol, Map<Clock, Double> clockValueMap){
//        if(StringUtils.equals(symbol, this.symbol)){
//            for(Clock clock: clockValueMap.keySet()){
//                double value = clockValueMap.get(clock);
//                TimeGuard timeGuard = getTimeGuard(clock);
//                if (!timeGuard.isPass(value)){
//                    return false;
//                }
//            }
//            BigDecimal c1 = new BigDecimal(Double.toString(clockValueMap.get(clockValueMap.get(0))));
//            BigDecimal c2 = new BigDecimal(Double.toString(clockValueMap.get(clockValueMap.get(1))));
//            double diff=c1.subtract(c2).doubleValue();
//            if(clockTimeGuardMap.getLargerclock()&&clockTimeGuardMap.isHaveupperdiff()&&!clockTimeGuardMap.isIsequalupper()&&diff*100<clockTimeGuardMap.getDifferencexyupper()*100
//            ||clockTimeGuardMap.getLargerclock()&&clockTimeGuardMap.isHaveupperdiff()&&clockTimeGuardMap.isIsequalupper()&&diff*100<=clockTimeGuardMap.getDifferencexyupper()*100
//            ||clockTimeGuardMap.getLargerclock()&&clockTimeGuardMap.isHavelowerdiff()&&!clockTimeGuardMap.isHavelowerdiff()&&diff*100>clockTimeGuardMap.getDifferencexylower()*100
//                    ||clockTimeGuardMap.getLargerclock()&&clockTimeGuardMap.isHavelowerdiff()&&clockTimeGuardMap.isHavelowerdiff()&&diff*100>=clockTimeGuardMap.getDifferencexylower()*100){
//                return false;
//            }
//            return true;
//        }
//        return false;
//    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(sourceLocation.getId()).append(", ").append(symbol).append(",");
        for(Map.Entry<Clock, TimeGuard> entry: clockTimeGuardMap.clockTimeGuardMap.entrySet()){
            sb.append(entry.getKey().getName()).append("-").append(entry.getValue()).append(" & ");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
      //  sb.append(clockTimeGuardMap.clockTimeGuardMap.toString());
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards());
        if(clockTimeGuardMap.getDifferenceEdgeTimeGuards().size()!=0){
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff());
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards().get(0).getDifferencexylower());
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards().get(0).isIsequallower());
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff());
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards().get(0).getDifferencexyupper());
        sb.append(", ").append(clockTimeGuardMap.getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff());}
//        sb.append(", ").append(clockTimeGuardMap.getLargerclock());
//        sb.append(", ").append(clockTimeGuardMap.isHavelowerdiff());
//        sb.append(", ").append(clockTimeGuardMap.getDifferencexylower());
//        sb.append(", ").append(clockTimeGuardMap.isIsequallower());
//        sb.append(", ").append(clockTimeGuardMap.isHaveupperdiff());
//        sb.append(", ").append(clockTimeGuardMap.getDifferencexyupper());
//        sb.append(", ").append(clockTimeGuardMap.isIsequalupper());
        sb.append(", ").append(targetLocation.getId());
        sb.append(", ").append(resetClockSet).append("]");
        return sb.toString();
    }

    public String getSourceId() {
        return sourceLocation.getId();
    }

    public String getTargetId(){
        return targetLocation.getId();
    }

    public int getLowerBound(Clock clock) {
        return getTimeGuard(clock).getLowerBound();
    }

    public int getUpperBound(Clock clock) {
        return getTimeGuard(clock).getUpperBound();
    }
}
