package ta.ota;

import lombok.Builder;
import lombok.Data;
import ta.*;

import java.util.*;

/**
 * DOTA是TA的一个子集，它只有一个时钟，且是确定性的
 */
@Data
public class DOTA extends TA {

    private Clock clock;

    public DOTA(String name, Set<String> sigma, List<TaLocation> locations, List<TaTransition> transitions, Clock clock) {
        super(name, new ArrayList<>(), sigma, locations, transitions);
        getClockList().add(clock);
        this.clock = clock;
    }


    //给定一个重置逻辑时间字，DOTA最多一个路径
//    public TaLocation reach(ResetLogicTimeWord resetLogicTimeWord) {
//
//        TaLocation location = getInitLocation();
//
//        //存储当前时钟值,初始化0
//        double value = 0.0;
//        Map<Clock, Double> clockDoubleMap = new HashMap<>();
//        clockDoubleMap.put(clock, 0.0);
//
//        List<ResetLogicAction> actions = resetLogicTimeWord.getTimedActions();
//
//        flag:
//        for (ResetLogicAction action : actions) {
//            List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
//            for (TaTransition transition : transitions) {
//                if (transition.isReset(clock) != action.isReset()) {
//                    continue;
//                }
//
//                TimeGuard timeGuard = transition.getTimeGuard(clock);
//
//                if (timeGuard.isPass(action.getValue())) {
//                    location = transition.getTargetLocation();
//                    continue flag;
//                }
//
//            }
//            return null;
//        }
//        return location;
//    }

    //给定一个逻辑时间字，DOTA最多一个路径
    public TaLocation reach(LogicTimeWord logicTimeWord) {

        TaLocation location = getInitLocation();

        //存储当前时钟值,初始化0
        double value = 0.0;
        Map<Clock, Double> clockDoubleMap = new HashMap<>();
        clockDoubleMap.put(clock, 0.0);

        List<LogicTimedAction> actions = logicTimeWord.getTimedActions();

        flag:
        for (LogicTimedAction action : actions) {
            List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
            for (TaTransition transition : transitions) {
                TimeGuard timeGuard = transition.getTimeGuard(clock);
                if (timeGuard.isPass(action.getValue())) {
                    location = transition.getTargetLocation();
                    continue flag;
                }
            }
            return null;
        }
        return location;
    }

//    public ResetLogicTimeWord transferReset(LogicTimeWord logicTimeWord) {
//        TaLocation location = getInitLocation();
//        //存储当前时钟值,初始化0
//        double value = 0.0;
//        Map<Clock, Double> clockDoubleMap = new HashMap<>();
//        clockDoubleMap.put(clock, 0.0);
//
//        List<LogicTimedAction> actions = logicTimeWord.getTimedActions();
//        List<ResetLogicAction> resetActions = new ArrayList<>();
//        boolean end = false;
//        flag:
//        for (LogicTimedAction action : actions) {
//            if (!end) {
//                List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
//                for (TaTransition transition : transitions) {
//                    TimeGuard timeGuard = transition.getTimeGuard(clock);
//                    if (timeGuard.isPass(action.getValue())) {
//                        location = transition.getTargetLocation();
//                        ResetLogicAction resetLogicAction = new ResetLogicAction(
//                                action.getSymbol(), action.getValue(), transition.isReset(clock));
//                        resetActions.add(resetLogicAction);
//                        continue flag;
//                    }
//                }
//                end = true;
//            }
//            if (end) {
//                ResetLogicAction resetLogicAction = new ResetLogicAction(
//                        action.getSymbol(), action.getValue(), true);
//                resetActions.add(resetLogicAction);
//            }
//        }
//        return new ResetLogicTimeWord(resetActions);
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\t").append("\"sigma\":[");
        for (String action : getSigma()) {
            sb.append("\"" + action + "\",");
        }
        sb.deleteCharAt(sb.length() - 1).append("],\n\t").append("\"init\":");
        String init = getInitLocation().getId() + "";
        sb.append(init).append(",\n\t").append("\"name\":\"").append(getName()).append("\"\n\t");
        sb.append("\"s\":[");
        for (TaLocation l : getLocations()) {
            sb.append(l.getId()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("]\n\t\"tran\":{\n");

        getTransitions().sort(new OTATranComparator(clock));
        for (int i = 0; i < getTransitions().size(); i++) {
            TaTransition t = getTransitions().get(i);
            String reset = t.getResetClockSet().contains(clock) ? "r" : "n";
            sb.append("\t\t\"").append(i).append("\":[")
                    .append(t.getSourceId()).append(",")
                    .append("\"").append(t.getSymbol()).append("\",")
                    .append("\"").append(t.getTimeGuard(clock)).append("\",")
                    .append(t.getTargetId()).append(", ").append(reset).append("]").append(",\n");

        }
        sb.deleteCharAt(sb.length() - 2);
        sb.append("\t},\n\t").append("\"accpted\":[");
        for (TaLocation l : getAcceptedLocations()) {
            sb.append(l.getId()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("]\n}");
        return sb.toString();
    }


}
