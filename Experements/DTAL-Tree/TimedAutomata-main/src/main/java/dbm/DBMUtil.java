package dbm;


import ta.TimeGuard;

import java.util.ArrayList;
import java.util.List;

public final class DBMUtil {

    public static List<ActionGuard> transfer(List<TransitionState> states){
        //初始化区间范围
        List<ActionGuard> actionGuards = new ArrayList<>();

        for(TransitionState state : states){
            String action = state.getSymbol();
            Value lower = state.getDbm().getMatrix()[0][1];
            Value upper = state.getDbm().getMatrix()[1][0];
            Value lower2 = state.getDbm().getMatrix()[0][2];
            Value upper2 = state.getDbm().getMatrix()[2][0];
            List<TimeGuard> guards = new ArrayList<>();
            //guards.add(!lower.isEqual(),!upper.isEqual(),(-1)*lower.getValue(),upper.getValue());
            guards.add(new TimeGuard(!lower.isEqual(),!upper.isEqual(),(-1)*lower.getValue(),upper.getValue()));
            guards.add(new TimeGuard(!lower.isEqual(),!upper.isEqual(),(-1)*lower2.getValue(),upper2.getValue()));
           // TimeGuard guard = new TimeGuard(!lower.isEqual(), !upper.isEqual(),
             //       (-1)*lower.getValue(),upper.getValue());
            ActionGuard actionGuard = new ActionGuard(action, guards);
            actionGuards.add(actionGuard);
        }

        return actionGuards;
    }
}
