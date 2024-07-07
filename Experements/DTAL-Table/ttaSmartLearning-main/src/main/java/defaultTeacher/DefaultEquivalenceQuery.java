package defaultTeacher;

import com.microsoft.z3.*;
import dbm.ActionGuard;
import dbm.DBMUtil;
import dbm.TransitionState;
import com.microsoft.z3.*;
import dbm.Value;
import frame.EquivalenceQuery;
import frame.TwoClockTeacher;
import javafx.animation.Transition;
import lombok.Data;
import ta.*;
//import ta.ota.DOTA;
import ta.ota.LogicTimeWord;
import ta.ota.OTATranComparator;
import ta.twoClockTA.*;
import ta.ota.LogicTimedAction;
import ta.twoClockTA.TwoClockTA;
import timedAction.DelayTimedAction;
import timedAction.ResetDelayAction;
import timedWord.DelayTimeWord;
import timedWord.ResetDelayTimeWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

@Data
public class DefaultEquivalenceQuery implements EquivalenceQuery<TwoClockResetLogicTimeWord, TwoClockTA> {
    private TwoClockTA dtta;
    private int count;
    private int c;
    int dfs2c=0;
    public DefaultEquivalenceQuery(TwoClockTA dtta) {
        this.dtta = dtta;
    }
    int shortnumber=0;
    long reachtime=0;
    @Override

    public TwoClockResetLogicTimeWord findCounterExample(TwoClockTA hypothesis) {
        c++;
        count++;
        TA negDtta = TAUtil.negTA(dtta);
        TA negHypothesis = TAUtil.negTA(hypothesis);
        TA ta1 = TAUtil.parallelCombination(dtta, negHypothesis);
        TA ta2 = TAUtil.parallelCombination(negDtta, hypothesis);

        System.out.println("hypothesis location:" + hypothesis.getLocations().toString());
        System.out.println(hypothesis.getTransitions().size());
        System.out.println("hypothesis transition:");
        for (int i = 0; i < hypothesis.getTransitions().size(); i++) {
            System.out.println(hypothesis.getTransitions().get(i).toString());
        }
        long Start=System.currentTimeMillis();
        List<TransitionState> stateTrace1 = null;
        List<Set<Clock>> resets1 = null;
        Map<List<TransitionState>,List<Set<Clock>>> reachable=TAUtil.reachable(ta1);
        if(reachable!=null) {
            for (List<TransitionState> list : reachable.keySet()) {
                stateTrace1 = list;
            }
            for (Map.Entry<List<TransitionState>, List<Set<Clock>>> list : reachable.entrySet()) {
                resets1 = list.getValue();
            }
        }
        List<TransitionState> stateTrace2 = null;
        List<Set<Clock>> resets2 = null;
        Map<List<TransitionState>,List<Set<Clock>>> reachable2=TAUtil.reachable(ta2);
        long End=System.currentTimeMillis();
        reachtime+=End-Start;
        if(reachable2!=null) {
                    for (List<TransitionState> list : reachable2.keySet()) {
                stateTrace2 = list;
            }
            for (Map.Entry<List<TransitionState>, List<Set<Clock>>> list : reachable2.entrySet()) {
                resets2 = list.getValue();
            }
        }
//System.out.println("resets1:"+resets1);
        List<TransitionState> states = shortTrace(stateTrace1, stateTrace2);
        List<Set<Clock>> resets=shortResets(resets1, resets2);
//        System.out.println("resets1:"+resets);

        if (stateTrace2 == null) {

        }

        if (stateTrace1 == null) {
        }
        if (states == null) {
            return null;
        }
        TwoClockResetLogicTimeWord result = analyze(resets,states, hypothesis);
        if(result==null&&stateTrace1==null||result==null&&stateTrace2==null) {
            return null;
        }
        else if(result==null&&stateTrace1!=null&&stateTrace2!=null){
            if(shortnumber==2)
            { result = analyze(resets,stateTrace1, hypothesis);}
            else{
                result = analyze(resets,stateTrace2, hypothesis);
            }
            if(result!=null){
                System.out.println("Equivalence result:" + result.toString());
                return result;
            }
            else {
                return null;
            }
        }
        else {
            System.out.println("Equivalence result:" + result.toString());
            return result;}

    }

    @Override
    public int getCount() {
        return count;
    }

    public List<TransitionState> shortTrace(List<TransitionState> states1, List<TransitionState> states2) {

        if (null == states1 && null == states2) {
            return null;
        }
        if (null == states1) {

            shortnumber=2;
            return states2;
        }

        if (null == states2) {
            shortnumber=1;
            return states1;
        }
        if (states1.size() <= states2.size()) {
            shortnumber=1;
            return states1;
        } else {
            shortnumber=2;
            return states2;
        }
        //  return states1.size() <= states2.size() ? states1 : states2;
    }

    public List<Set<Clock>> shortResets(List<Set<Clock>> states1, List<Set<Clock>> states2) {

        if (null == states1 && null == states2) {
            return null;
        }
        if (null == states1) {
            shortnumber=2;
            return states2;
        }

        if (null == states2) {
            shortnumber=1;
            return states1;
        }
        if (states1.size() <= states2.size()) {
            shortnumber=1;
            return states1;
        } else {
            shortnumber=2;
            return states2;
        }
        //  return states1.size() <= states2.size() ? states1 : states2;
    }

    public  List<ActionGuardsSet> transferH(List<TransitionState> states) {
        //初始化区间范围
        List<ActionGuardsSet> actionGuards = new ArrayList<>();
        for (TransitionState state : states) {
            String action = state.getSymbol();
            List<TimeGuard> timeGuardList = new ArrayList<>();
            for (int i=0;i<dtta.getClockList().size();i++){
                Value lower = state.getDbm().getMatrix()[0][i+1+dtta.getClockList().size()];
                Value upper = state.getDbm().getMatrix()[i+1+dtta.getClockList().size()][0];
                TimeGuard guard = new TimeGuard(!lower.isEqual(), !upper.isEqual(), (-1) * lower.getValue(), upper.getValue());
                timeGuardList.add(guard);
            }
            ActionGuardsSet actionGuardsSet = new ActionGuardsSet(action, timeGuardList);
            actionGuards.add(actionGuardsSet);
        }
        return actionGuards;
    }

    public TwoClockResetLogicTimeWord analyze(List<Set<Clock>> resets,List<TransitionState> stateList, TwoClockTA hypothesis) {
        List<ActionGuardsSet> actionGuards = transfer(stateList);
        List<ActionGuardsSet> actionGuardsH = transferH(stateList);
        TwoClockLogicTimeWord logicTimeWord = TwoClockLogicTimeWord.emptyWord();
        TwoClockLogicTimeWord logicTimeWordH = TwoClockLogicTimeWord.emptyWord();
        TaLocation location=dtta.getInitLocation();
        resets=new ArrayList<>();
        for (int i=0;i<actionGuards.size();i++){
            List<TaTransition> transitions=dtta.getTransitions(location,actionGuards.get(i).getSymbol(),null);
            for (TaTransition transition:transitions){
                boolean eq=true;
                for (Clock clock:dtta.getClockList()){
                    if(transition.getLowerBound(clock)>actionGuards.get(i).getTimeGuards().get(dtta.getClockList().indexOf(clock)).getLowerBound()
                            ||transition.getLowerBound(clock)==actionGuards.get(i).getTimeGuards().get(dtta.getClockList().indexOf(clock)).getLowerBound()&&
                            transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock).isLowerBoundOpen()&&!actionGuards.get(i).getTimeGuards().get(dtta.getClockList().indexOf(clock)).isLowerBoundOpen()||
                            transition.getUpperBound(clock)<actionGuards.get(i).getTimeGuards().get(dtta.getClockList().indexOf(clock)).getUpperBound()
                            ||transition.getUpperBound(clock)==actionGuards.get(i).getTimeGuards().get(dtta.getClockList().indexOf(clock)).getUpperBound()&&
                            transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock).isUpperBoundOpen()&&!actionGuards.get(i).getTimeGuards().get(dtta.getClockList().indexOf(clock)).isUpperBoundOpen()){
                        eq=false;
                        break;
                    }
                }
                if(!eq){

                }
                else {
                    resets.add(transition.getResetClockSet());
                    location=transition.getTargetLocation();
                }
            }
        }

        for (ActionGuardsSet actionGuard : actionGuardsH) {
        }
        for (ActionGuardsSet actionGuard : actionGuards) {
            Map<Clock, Double> map = new HashMap<>();
            for (int i=0;i<dtta.getClockList().size();i++){
                map.put(dtta.getClockList().get(i), actionGuard.getTimeGuards().get(i).getLowerBound() * 1.0);
            }
            TwoClockLogicAction logicAction = new TwoClockLogicAction(
                    actionGuard.getSymbol(), map);
            List<TwoClockLogicAction> ActionList = new ArrayList<>();
            if (logicTimeWord.getTimedActions() != null) {
                ActionList.addAll(logicTimeWord.getTimedActions());
            }
            ActionList.add(logicAction);
            logicTimeWord.setTimedActions(ActionList);


            Map<Clock, Double> map2 = new HashMap<>();

            for (int i=0;i<hypothesis.getClockList().size();i++){
                map2.put(hypothesis.getClockList().get(i), actionGuard.getTimeGuards().get(i).getLowerBound() * 1.0);

            }
            TwoClockLogicAction logicActionH = new TwoClockLogicAction(
                    actionGuard.getSymbol(), map2);
            List<TwoClockLogicAction> ActionListH = new ArrayList<>();
            if (logicTimeWordH.getTimedActions() != null) {
                ActionListH.addAll(logicTimeWordH.getTimedActions());
            }
            ActionListH.add(logicActionH);
            logicTimeWordH.setTimedActions(ActionListH);
        }

        Clock x = dtta.getClockList().get(0);
        Clock Hx = hypothesis.getClockList().get(0);
        Clock y = dtta.getClockList().get(0);
        Clock Hy = hypothesis.getClockList().get(0);
        List<TimeGuard> timeGuardList = new ArrayList<>();
        for (int i = 0; i < actionGuards.size(); i++) {
            for (int m=0;m<dtta.getClockList().size();m++){
                timeGuardList.add(actionGuards.get(i).getTimeGuards().get(m));
            }
        }
        List<Double> newstring = new ArrayList<>();
        List<TwoClockResetLogicTimeWord> twoClockResetLogicTimeWords=new ArrayList<>();
        List<TwoClockResetLogicTimeWord> result=new ArrayList<>();
        dfs2c=0;

        int timeGuardListmax=0;
        for(int i = 0; i< timeGuardList.size(); i++) {
            if (timeGuardList.get(i).getUpperBound() != TimeGuard.MAX_TIME && timeGuardList.get(i).getUpperBound() > timeGuardListmax) {
                timeGuardListmax = timeGuardList.get(i).getUpperBound();
            }
            if (timeGuardList.get(i).getLowerBound() >= timeGuardListmax) {
                timeGuardListmax = timeGuardList.get(i).getLowerBound();
            }
        }
        timeGuardListmax=timeGuardListmax+2;
        List<TimeGuard> timeGuardList1 = new ArrayList<>();
//        for (int i = 0; i< timeGuardList.size();i++) {
//            if (timeGuardList.get(i).getUpperBound() == TimeGuard.MAX_TIME) {
//                TimeGuard timeGuard = new TimeGuard(timeGuardList.get(i).isLowerBoundOpen(), true, timeGuardList.get(i).getLowerBound(), timeGuardListmax);
//                timeGuardList1.add(timeGuard);
//            } else {
//                timeGuardList1.add(timeGuardList.get(i));
//            }
//        }
        for (int i = 0; i< timeGuardList.size();i++) {
            if (timeGuardList.get(i).getUpperBound() >timeGuardList.get(i).getLowerBound()+5) {
                TimeGuard timeGuard = new TimeGuard(timeGuardList.get(i).isLowerBoundOpen(), true, timeGuardList.get(i).getLowerBound(), timeGuardList.get(i).getLowerBound()+5);
                timeGuardList1.add(timeGuard);
            } else {
                timeGuardList1.add(timeGuardList.get(i));
            }
        }
        System.out.println(timeGuardList1);
        //    result= dfs2(timeGuardList1, 0, timeGuardList.size(), newstring, logicTimeWord, logicTimeWordH, x, y, Hx, Hy,hypothesis,twoClockResetLogicTimeWords);
        TwoClockResetLogicTimeWord timeWord = dfs3(actionGuards);
        if(timeWord!=null&&timeWord.getTimedActions()!=null){
            TwoClockResetLogicTimeWord check=analyzethree(TwoClockResetLogicTimeWord.logicTimeWord(timeWord), logicTimeWordH, hypothesis);
            if(check==null||check.getTimedActions()==null){
                result=null;
            }
            else {
                result.add(timeWord);
            }
        }
        else {
            timeWord = dfs3more(actionGuards);
            if(timeWord!=null&&timeWord.getTimedActions()!=null){
                TwoClockResetLogicTimeWord check=analyzethree(TwoClockResetLogicTimeWord.logicTimeWord(timeWord), logicTimeWordH, hypothesis);
                if(check==null||check.getTimedActions()==null){
                    result=null;
                }
                else {
                    result.add(timeWord);
                }
            }
        }
        if(result==null||result.size()==0) {

            double[] delays=z33(resets,actionGuards);
            List<ResetDelayAction> delayTimedActions=new ArrayList<>();
            for (int i=0;i<actionGuards.size();i++){
                ResetDelayAction delayTimedAction= new ResetDelayAction(actionGuards.get(i).getSymbol(),delays[i],resets.get(i));
                delayTimedAction.setValue(delays[i]);
                delayTimedAction.setSymbol(actionGuards.get(i).getSymbol());
                delayTimedAction.setReset(resets.get(i));
                delayTimedActions.add(delayTimedAction);
            }
            ResetDelayTimeWord delayTimeWord=new ResetDelayTimeWord(delayTimedActions);
            System.out.println(delayTimeWord);
            System.out.println(result);
            if(result==null){
                result=new ArrayList<>();
            }
            result.add(DelaytoLogic(delayTimeWord));
            if((result!=null&&result.size()!=0)){
                return  result.get(0);
            }
//            long START=System.currentTimeMillis();
//            result= dfs2(START,timeGuardList1, 0, timeGuardList.size(), newstring, logicTimeWord, logicTimeWordH, x, y, Hx, Hy,hypothesis,twoClockResetLogicTimeWords);


            if (result != null&&result.size()!=0) {
                return result.get(0);
            }
            else {
                double[] doubles=new double[1];
                newstring=new ArrayList<>();
                twoClockResetLogicTimeWords=new ArrayList<>();
                System.out.println("actionGuards:"+actionGuards);
                int[] min=new int[actionGuards.size()];
                int[] max=new int[actionGuards.size()];
                min[0]=actionGuards.get(0).getTimeGuards().get(0).getLowerBound();
                max[0]=actionGuards.get(0).getTimeGuards().get(0).getUpperBound();
                for (int i=1;i<actionGuards.size();i++){
                    if(resets.get(i-1).contains(dtta.getClockList().get(0))){
                        min[i]=actionGuards.get(i).getTimeGuards().get(0).getLowerBound();
                        if(actionGuards.get(i).getTimeGuards().get(0).getUpperBound()-actionGuards.get(i).getTimeGuards().get(0).getLowerBound()>5){
                            max[i]=min[i]+5;
                        }
                        else {
                            max[i]=actionGuards.get(i).getTimeGuards().get(0).getUpperBound();}

                    }
                    else {
                        min[i]=0;
                        // max[i]=actionGuards.get(i).getTimeGuards().get(0).getUpperBound();
                        max[i]=actionGuards.get(i).getTimeGuards().get(0).getLowerBound()+5;
                    }
                }
                for (int i=0;i<min.length;i++){
                    System.out.println("min:"+min[i]);}
                for (int i=0;i<max.length;i++){
                    System.out.println("max:"+max[i]);}
                result=findbydelay2(min,max,timeGuardList1,doubles,TimeGuard.MAX_TIME,actionGuards,actionGuardsH, 0, actionGuards.size(), newstring, logicTimeWord, logicTimeWordH, x, y, Hx, Hy, hypothesis, twoClockResetLogicTimeWords);
                return result.get(0);
            }
        }
        if(result!=null&&result.size()!=0){
            return result.get(0);
        }
        else {
            for (int i = 0; i < actionGuards.size(); i++) {
                HashMap<Clock,Double> map=new HashMap<>();
                map.put(x,(double)actionGuards.get(i).getTimeGuards().get(0).getLowerBound());
                map.put(y,(double)actionGuards.get(i).getTimeGuards().get(1).getLowerBound());
                logicTimeWord.get(i).setClockValueMap(map);
                HashMap<Clock,Double> map2=new HashMap<>();
                map2.put(Hx,(double)actionGuards.get(i).getTimeGuards().get(0).getLowerBound());
                map2.put(Hy,(double)actionGuards.get(i).getTimeGuards().get(1).getLowerBound());
                logicTimeWordH.get(i).setClockValueMap(map2);
            }
            return null;
        }
    }
    public List<TwoClockResetLogicTimeWord> findbydelay2(int[] min,int[] max2,List<TimeGuard> timeGuardList1,double[] delay,int max,List<ActionGuardsSet> actionGuards,List<ActionGuardsSet> actionGuardsH,int index, int size, List<Double> newstring,TwoClockLogicTimeWord logicTimeWord,TwoClockLogicTimeWord logicTimeWordH,Clock x,Clock y,Clock Hx,Clock Hy, TwoClockTA hypothesis,List<TwoClockResetLogicTimeWord> twoClockResetLogicTimeWord){
        if(index==size-1){
            {
                BigDecimal b1=new BigDecimal(0.0);
                BigDecimal b2=new BigDecimal(Double.toString(0.5));;
                for (double value1 = min[index/dtta.getClockList().size()];value1<max2[index/dtta.getClockList().size()] ; value1 =b1.add(b2).doubleValue()) {

                    newstring.add(value1);
                    List<DelayTimedAction> actions=new ArrayList<>();
                    for (int i=0;i<actionGuards.size();i++){
                        DelayTimedAction action=new DelayTimedAction(actionGuards.get(i).getSymbol(),newstring.get(i));
                        action.setSymbol(actionGuards.get(i).getSymbol());
                        action.setValue(newstring.get(i));
                        actions.add(action);
                    }
                    DelayTimeWord delayTimeWord=new DelayTimeWord(actions);
                    //  System.out.println("delayTimeWord:"+delayTimeWord);
                    BooleanAnswer answer1 = dtta.reach(delayTimeWord);
                    BooleanAnswer answer2 = hypothesis.reach(delayTimeWord);
                    if(answer1!=null&&answer2!=null&&answer1.isAccept()!=answer2.isAccept()) {
                        TwoClockLogicTimeWord logictimeword2 = new TwoClockLogicTimeWord(null);
                        boolean satisfy = true;
                        Map<Clock, Double> clockDoubleMap = new HashMap<>();
                        Map<Clock, Double> clockDoubleMapH = new HashMap<>();
                        for (Clock clock : dtta.getClockList()) {
                            BigDecimal bigDecimal = new BigDecimal(Double.toString(0.0));
                            BigDecimal bigDecimal2 = new BigDecimal(Double.toString(newstring.get(0)));
                            clockDoubleMap.put(clock, bigDecimal.add(bigDecimal2).doubleValue());
                            clockDoubleMapH.put(clock, bigDecimal.add(bigDecimal2).doubleValue());
                        }

                        for (int m = 0; m < dtta.getClockList().size(); m++) {
                            if (clockDoubleMap.get(dtta.getClockList().get(m)) < actionGuards.get(0).getTimeGuards().get(m).getLowerBound() ||
                                    clockDoubleMap.get(dtta.getClockList().get(m)) > actionGuards.get(0).getTimeGuards().get(m).getUpperBound() ||
                                    clockDoubleMap.get(dtta.getClockList().get(m)) == actionGuards.get(0).getTimeGuards().get(m).getLowerBound() &&
                                            actionGuards.get(0).getTimeGuards().get(m).isLowerBoundOpen() ||
                                    clockDoubleMap.get(dtta.getClockList().get(m)) == actionGuards.get(0).getTimeGuards().get(m).getUpperBound() &&
                                            actionGuards.get(0).getTimeGuards().get(m).isUpperBoundOpen()) {
                                satisfy = false;
                                break;
                            }
                        }

                        List<TwoClockLogicAction> actions2 = new ArrayList<>();
                        TwoClockLogicAction action = new TwoClockLogicAction(actionGuards.get(0).getSymbol(), clockDoubleMap);
                        action.setSymbol(actionGuards.get(0).getSymbol());
                        action.setClockValueMap(clockDoubleMap);
                        actions2.add(action);
                        logictimeword2.setTimedActions(actions2);
                        TwoClockResetLogicTimeWord resetLogicTimeWord = dtta.transferResetbyteacher(logictimeword2);
                        for (int i = 1; i < actionGuards.size(); i++) {
                            clockDoubleMap = new HashMap<>();
                            for (Clock clock : dtta.getClockList()) {
                                if (resetLogicTimeWord.getLastResetAction().isReset(clock)) {
                                    BigDecimal bigDecimal = new BigDecimal(Double.toString(0.0));
                                    BigDecimal bigDecimal2 = new BigDecimal(Double.toString(newstring.get(i)));
                                    clockDoubleMap.put(clock, bigDecimal2.add(bigDecimal).doubleValue());
                                } else {
                                    BigDecimal bigDecimal = new BigDecimal(Double.toString(resetLogicTimeWord.getLastResetAction().getValue(clock)));
                                    BigDecimal bigDecimal2 = new BigDecimal(Double.toString(newstring.get(i)));
                                    clockDoubleMap.put(clock, bigDecimal2.add(bigDecimal).doubleValue());
                                }

                            }

                            for (int m = 0; m < dtta.getClockList().size(); m++) {
                                if (clockDoubleMap.get(dtta.getClockList().get(m)) < actionGuards.get(i).getTimeGuards().get(m).getLowerBound() ||
                                        clockDoubleMap.get(dtta.getClockList().get(m)) > actionGuards.get(i).getTimeGuards().get(m).getUpperBound() ||
                                        clockDoubleMap.get(dtta.getClockList().get(m)) == actionGuards.get(i).getTimeGuards().get(m).getLowerBound() &&
                                                actionGuards.get(i).getTimeGuards().get(m).isLowerBoundOpen() ||
                                        clockDoubleMap.get(dtta.getClockList().get(m)) == actionGuards.get(i).getTimeGuards().get(m).getUpperBound() &&
                                                actionGuards.get(i).getTimeGuards().get(m).isUpperBoundOpen()) {
                                    satisfy = false;
                                    break;
                                }
                            }
                            TwoClockLogicAction action2 = new TwoClockLogicAction(actionGuards.get(i).getSymbol(), clockDoubleMap);
                            action2.setSymbol(actionGuards.get(i).getSymbol());
                            action2.setClockValueMap(clockDoubleMap);
                            actions2.add(action2);
                            logictimeword2 = new TwoClockLogicTimeWord(actions2);
                            logictimeword2.setTimedActions(actions2);
                            resetLogicTimeWord=dtta.transferResetbyteacher(logictimeword2);
                        }
                        if (satisfy) {
                            twoClockResetLogicTimeWord.add(dtta.transferResetbyteacher(logictimeword2));
                            return twoClockResetLogicTimeWord;
                        }
                    }
                    newstring.remove(index);
                    b1 = new BigDecimal(Double.toString(value1));
                    b2 = new BigDecimal(Double.toString(0.5));
                }
            }


        }
        else {
            {
                BigDecimal b1=new BigDecimal(0.0);
                BigDecimal b2=new BigDecimal(Double.toString(0.5));
//                for (double value1 = b1.add(b2).doubleValue(); value1<max; value1 =b1.add(b2).doubleValue()) {
                for (double value1 = min[index/dtta.getClockList().size()]; value1<max2[index/dtta.getClockList().size()] ; value1 =b1.add(b2).doubleValue()) {

                    newstring.add(value1);

                    findbydelay2(min,max2,timeGuardList1,delay,max,actionGuards,actionGuardsH, index+1, size, newstring,logicTimeWord,logicTimeWordH,x,y,Hx,Hy,hypothesis,twoClockResetLogicTimeWord);
                    if(twoClockResetLogicTimeWord.size()!=0){
                        System.out.println(twoClockResetLogicTimeWord);
                        return twoClockResetLogicTimeWord;
                    }
                    newstring.remove(index);
                    b1 = new BigDecimal(Double.toString(value1));
                    b2 = new BigDecimal(Double.toString(0.5));
                }
            }
        }
        if(twoClockResetLogicTimeWord.size()!=0) {
            return twoClockResetLogicTimeWord;
        }
        else {
            return null;
        }
    }

    public TwoClockResetLogicTimeWord dfs3(List<ActionGuardsSet> actionGuards) {
        TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=null;
        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
        TwoClockResetLogicTimeWord prefix=new TwoClockResetLogicTimeWord(null);

        Map<Clock, Double> map = new HashMap<>();
        for (int j = 0; j < dtta.getClockList().size(); j++) {
            if (!actionGuards.get(0).getTimeGuards().get(j).isLowerBoundOpen()) {
                map.put(dtta.getClockList().get(j), Double.valueOf(actionGuards.get(0).getTimeGuards().get(j).getLowerBound()));
            } else {
                BigDecimal b1 = new BigDecimal(Double.toString(actionGuards.get(0).getTimeGuards().get(j).getLowerBound()));
                BigDecimal b2 = new BigDecimal(Double.toString(0.1));
                map.put(dtta.getClockList().get(j), b1.add(b2).doubleValue());
            }
        }
        TwoClockLogicTimeWord timeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> actions=new ArrayList<>();
        TwoClockLogicAction action=new TwoClockLogicAction();
        action.setClockValueMap(map);
        action.setSymbol(actionGuards.get(0).getSymbol());
        actions.add(action);
        timeWord.setTimedActions(actions);
        prefix=dtta.transferResetbyteacher(timeWord);
        double[] difference = new double[dtta.getClockList().size()];
        if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
            TwoClockResetLogicAction twoClockLogicAction = prefix.get(prefix.size() - 1);
            for (int p = 0; p < dtta.getClockList().size(); p++) {
                if (!twoClockLogicAction.isReset(dtta.getClockList().get(p))) {
                    difference[p] = twoClockLogicAction.getValue(dtta.getClockList().get(p));
                } else {
                    difference[p] = 0d;
                }

            }
        }
        if(actionGuards.size()>1) {
            for (int i = 1; i < actionGuards.size(); i++) {
                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0) {
                    twoClockResetLogicTimeWord.setTimedActions(null);
                }

                //    try {
                StringBuilder c = new StringBuilder();
                double diff=0;
                double lowera1=0;
                double lowera2=0;
                double uppera1=0;
                double lowerc1=0;
                double lowerc2=0;
                double upperc1=0;
                for (int i1 = 0; i1 < dtta.getClockList().size() - 1; i1++) {
                    c.append(dtta.getClockList().get(i1).getName());
                    c.append(",");
                }
                c.append(dtta.getClockList().get(dtta.getClockList().size() - 1).getName());
                StringBuilder e = new StringBuilder();

//                    for (int k = 0; k < dtta.getClockList().size(); k++) {
//                        if (actionGuards.get(i).getTimeGuards().get(k).isLowerBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + ">" + actionGuards.get(i).getTimeGuards().get(k).getLowerBound());
//                            e.append(",");
//                        }
//                        if (!actionGuards.get(i).getTimeGuards().get(k).isLowerBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + ">=" + actionGuards.get(i).getTimeGuards().get(k).getLowerBound());
//                            e.append(",");
//                        }
//                        if (actionGuards.get(i).getTimeGuards().get(k).isUpperBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + "<" + actionGuards.get(i).getTimeGuards().get(k).getUpperBound());
//                            e.append(",");
//                        }
//                        if (!actionGuards.get(i).getTimeGuards().get(k).isUpperBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + "<=" + actionGuards.get(i).getTimeGuards().get(k).getUpperBound());
//                            e.append(",");
//                        }
//                        e.append(dtta.getClockList().get(k).getName() + ">=" + difference[k]);
//                        e.append(",");
//                    }


                if (actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + ">" + actionGuards.get(i).getTimeGuards().get(0).getLowerBound());
                    e.append(",");
                    lowera1=actionGuards.get(i).getTimeGuards().get(0).getLowerBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + ">=" + actionGuards.get(i).getTimeGuards().get(0).getLowerBound());
                    e.append(",");
                    lowera1=actionGuards.get(i).getTimeGuards().get(0).getLowerBound();
                }
                if (actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + "<" + actionGuards.get(i).getTimeGuards().get(0).getUpperBound());
                    e.append(",");
                    uppera1=actionGuards.get(i).getTimeGuards().get(0).getUpperBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + "<=" + actionGuards.get(i).getTimeGuards().get(0).getUpperBound());
                    e.append(",");
                    uppera1=actionGuards.get(i).getTimeGuards().get(0).getUpperBound();
                }
                e.append(dtta.getClockList().get(0).getName() + ">=" + difference[0]);
                lowera2=difference[0];
                e.append(",");

                if (actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + ">" + actionGuards.get(i).getTimeGuards().get(1).getLowerBound());
                    e.append(",");
                    lowerc1=actionGuards.get(i).getTimeGuards().get(1).getLowerBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + ">=" + actionGuards.get(i).getTimeGuards().get(1).getLowerBound());
                    e.append(",");
                    lowerc1=actionGuards.get(i).getTimeGuards().get(1).getLowerBound();
                }
                if (actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + "<" + actionGuards.get(i).getTimeGuards().get(1).getUpperBound());
                    e.append(",");
                    upperc1=actionGuards.get(i).getTimeGuards().get(1).getUpperBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + "<=" + actionGuards.get(i).getTimeGuards().get(1).getUpperBound());
                    e.append(",");
                    upperc1=actionGuards.get(i).getTimeGuards().get(1).getUpperBound();
                }
                e.append(dtta.getClockList().get(1).getName() + ">=" + difference[1]);
                lowerc2=difference[1];
                e.append(",");

                for (int m = 0; m < dtta.getClockList().size() - 1; m++) {
                    for (int j = m + 1; j < dtta.getClockList().size(); j++) {
                        BigDecimal b1 = new BigDecimal(Double.toString(difference[m]));
                        BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));
                        e.append(dtta.getClockList().get(m).getName() + "-" + dtta.getClockList().get(j).getName() + "==" + b1.subtract(b2).doubleValue());
                        e.append(",");
                        diff=b1.subtract(b2).doubleValue();
                    }
                }
                e.deleteCharAt(e.lastIndexOf(","));
                System.out.println(e.toString());
//                    String[] aa = new String[]{"python", "D:\\ExpressionZ3Solver.py", c.toString(), e.toString()};
//                    Process proc = Runtime.getRuntime().exec(aa);// 执行py文件
//
//                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//                    String line = null;
//                    while ((line = in.readLine()) != null) {
//                        System.out.println("有输出");
//                        System.out.println(line);
//                        if (line.equals("[unsat]")) {
//                            return null;
//                        } else {
//                        }
//                        String[] strs = line.split(",");
//                        String[] s1 = new String[strs.length - 1];
//                        for (int i1 = 1; i1 < strs.length; i1++) {
//                            s1[i1 - 1] = strs[i1];
//                        }
//                        for (int i1=0;i1<s1.length;i1++){
//                            System.out.println("s1[i1]:"+s1[i1]);
//                        }
//                        String[] s2 = new String[s1.length];
//                        for (int i1 = 0; i1 < s1.length; i1++) {
//                            String[] a = s1[i1].split(":");
//                            s2[i1] = a[1];
//                        }
//                        for (int i1=0;i1<s2.length;i1++){
//                            System.out.println("s2[i1]:"+s2[i1]);
//                        }
//
//
//                        String[] s5 = new String[s1.length];
//                        for (int i1 = 0; i1 < s1.length; i1++) {
//                            String[] a = s1[i1].split(":");
//                            s5[i1] = a[0].substring(a[0].lastIndexOf("'")-1,a[0].lastIndexOf("'"));
//                        }
//                        for (int i1=0;i1<s5.length;i1++){
//                            System.out.println("s5[i1]:"+s5[i1]);
//                        }
//
//
//                        String[] s3 = new String[s2.length];
//                        for (int i1 = 0; i1 < s2.length; i1++) {
//                            s3[i1] = s2[i1].substring(s2[i1].indexOf("'") + 1, s2[i1].lastIndexOf("'"));
//                        }
//                        for (int i1=0;i1<s3.length;i1++){
//                            System.out.println("s3[i1]:"+s3[i1]);
//                        }
//                        Double[] values = new Double[s3.length];
//                        for (int i1 = 0; i1 < values.length; i1++) {
//
//                            if (s3[i1].contains("/")) {
//                                String a = s3[i1].split("/")[0];
//                                String b = s3[i1].split("/")[1];
//                                values[i1] = Double.valueOf(a) / Double.valueOf(b);
//                            } else {
//                                values[i1] = Double.valueOf(s3[i1]);
//                            }
//                        }
//                        for (int i1=0;i1<values.length;i1++){
//                            System.out.println("values[i1]:"+values[i1]);
//                        }
//                        for (int m = 0; m < strs.length; m++) {
//                            strs[m] = strs[m].replaceAll("\\s*", "");
//                        }
//
//
//                        Map<Clock, Double> map2 = new HashMap<>();
//                        for (int i1=0;i1<values.length;i1++){
//                            System.out.println(values[i1]);
//                        }
//                        for (int i1 = 0; i1 < s5.length; i1++) {
//                            map2.put(dtta.getClockList().get(Integer.valueOf(s5[i1]).intValue()), values[i1]);
//                        }
                double[] results=z3( diff, lowera1, lowera2, uppera1, lowerc1, lowerc2, upperc1,actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen(),actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen(),actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen(),actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen());
                if(results==null){
                    return null;
                }
                Map<Clock, Double> map2 = new HashMap<>();
                for (int i1 = 0; i1 < dtta.getClockList().size(); i1++) {
                    map2.put(dtta.getClockList().get(i1), results[i1]);
                }
                TwoClockLogicAction twoClockLogicAction1 = new TwoClockLogicAction(actionGuards.get(i).getSymbol(), map2);
                twoClockLogicActionList.add(twoClockLogicAction1);
                twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);

                TwoClockLogicTimeWord logicTimeWord2 = new TwoClockLogicTimeWord(null);
                if (prefix != null && prefix.getTimedActions() != null && prefix.getTimedActions().size() != 0) {
                    logicTimeWord2 = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
                    logicTimeWord2.setTimedActions(TwoClockResetLogicTimeWord.logicTimeWord(prefix).getTimedActions());
                    logicTimeWord2 = TwoClockLogicTimeWord.concat(logicTimeWord2, twoClockLogicTimeWord);
                } else {
                    logicTimeWord2 = twoClockLogicTimeWord;
                }
                TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1 = dtta.transferResetbyteacher(logicTimeWord2);
                twoClockResetLogicTimeWord = twoClockResetLogicTimeWord1;
                twoClockResetLogicTimeWord.setTimedActions(twoClockResetLogicTimeWord1.getTimedActions());
                for (int p = 0; p < dtta.getClockList().size(); p++) {
                    if (!twoClockResetLogicTimeWord1.getLastResetAction().isReset(dtta.getClockList().get(p))) {
                        difference[p] = twoClockResetLogicTimeWord1.getLastResetAction().getValue(dtta.getClockList().get(p));
                    } else {
                        difference[p] = 0d;
                    }
                }
                //  }
//                    in.close();
//                    proc.waitFor();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                {
                }

            }
        }
        else {
            return prefix;
        }
        return twoClockResetLogicTimeWord;
    }

    public TwoClockResetLogicTimeWord dfs3more(List<ActionGuardsSet> actionGuards) {
        TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=null;
        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
        TwoClockResetLogicTimeWord prefix=new TwoClockResetLogicTimeWord(null);

        Map<Clock, Double> map = new HashMap<>();
        for (int j = 0; j < dtta.getClockList().size(); j++) {
            if (!actionGuards.get(0).getTimeGuards().get(j).isLowerBoundOpen()) {
                map.put(dtta.getClockList().get(j), Double.valueOf(actionGuards.get(0).getTimeGuards().get(j).getLowerBound()));
            } else {
                BigDecimal b1 = new BigDecimal(Double.toString(actionGuards.get(0).getTimeGuards().get(j).getLowerBound()));
                BigDecimal b2 = new BigDecimal(Double.toString(1.0));
                map.put(dtta.getClockList().get(j), b1.add(b2).doubleValue());
            }
        }
        TwoClockLogicTimeWord timeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> actions=new ArrayList<>();
        TwoClockLogicAction action=new TwoClockLogicAction();
        action.setClockValueMap(map);
        action.setSymbol(actionGuards.get(0).getSymbol());
        actions.add(action);
        timeWord.setTimedActions(actions);
        prefix=dtta.transferResetbyteacher(timeWord);
        double[] difference = new double[dtta.getClockList().size()];
        if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
            TwoClockResetLogicAction twoClockLogicAction = prefix.get(prefix.size() - 1);
            for (int p = 0; p < dtta.getClockList().size(); p++) {
                if (!twoClockLogicAction.isReset(dtta.getClockList().get(p))) {
                    difference[p] = twoClockLogicAction.getValue(dtta.getClockList().get(p));
                } else {
                    difference[p] = 0d;
                }

            }
        }
        if(actionGuards.size()>1) {
            for (int i = 1; i < actionGuards.size(); i++) {
                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0) {
                    twoClockResetLogicTimeWord.setTimedActions(null);
                }

                //    try {
                StringBuilder c = new StringBuilder();
                double diff=0;
                double lowera1=0;
                double lowera2=0;
                double uppera1=0;
                double lowerc1=0;
                double lowerc2=0;
                double upperc1=0;
                for (int i1 = 0; i1 < dtta.getClockList().size() - 1; i1++) {
                    c.append(dtta.getClockList().get(i1).getName());
                    c.append(",");
                }
                c.append(dtta.getClockList().get(dtta.getClockList().size() - 1).getName());
                StringBuilder e = new StringBuilder();

//                    for (int k = 0; k < dtta.getClockList().size(); k++) {
//                        if (actionGuards.get(i).getTimeGuards().get(k).isLowerBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + ">" + actionGuards.get(i).getTimeGuards().get(k).getLowerBound());
//                            e.append(",");
//                        }
//                        if (!actionGuards.get(i).getTimeGuards().get(k).isLowerBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + ">=" + actionGuards.get(i).getTimeGuards().get(k).getLowerBound());
//                            e.append(",");
//                        }
//                        if (actionGuards.get(i).getTimeGuards().get(k).isUpperBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + "<" + actionGuards.get(i).getTimeGuards().get(k).getUpperBound());
//                            e.append(",");
//                        }
//                        if (!actionGuards.get(i).getTimeGuards().get(k).isUpperBoundOpen()) {
//                            e.append(dtta.getClockList().get(k).getName() + "<=" + actionGuards.get(i).getTimeGuards().get(k).getUpperBound());
//                            e.append(",");
//                        }
//                        e.append(dtta.getClockList().get(k).getName() + ">=" + difference[k]);
//                        e.append(",");
//                    }


                if (actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + ">" + actionGuards.get(i).getTimeGuards().get(0).getLowerBound());
                    e.append(",");
                    lowera1=actionGuards.get(i).getTimeGuards().get(0).getLowerBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + ">=" + actionGuards.get(i).getTimeGuards().get(0).getLowerBound());
                    e.append(",");
                    lowera1=actionGuards.get(i).getTimeGuards().get(0).getLowerBound();
                }
                if (actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + "<" + actionGuards.get(i).getTimeGuards().get(0).getUpperBound());
                    e.append(",");
                    uppera1=actionGuards.get(i).getTimeGuards().get(0).getUpperBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(0).getName() + "<=" + actionGuards.get(i).getTimeGuards().get(0).getUpperBound());
                    e.append(",");
                    uppera1=actionGuards.get(i).getTimeGuards().get(0).getUpperBound();
                }
                e.append(dtta.getClockList().get(0).getName() + ">=" + difference[0]);
                lowera2=difference[0];
                e.append(",");

                if (actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + ">" + actionGuards.get(i).getTimeGuards().get(1).getLowerBound());
                    e.append(",");
                    lowerc1=actionGuards.get(i).getTimeGuards().get(1).getLowerBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + ">=" + actionGuards.get(i).getTimeGuards().get(1).getLowerBound());
                    e.append(",");
                    lowerc1=actionGuards.get(i).getTimeGuards().get(1).getLowerBound();
                }
                if (actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + "<" + actionGuards.get(i).getTimeGuards().get(1).getUpperBound());
                    e.append(",");
                    upperc1=actionGuards.get(i).getTimeGuards().get(1).getUpperBound();
                }
                if (!actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen()) {
                    e.append(dtta.getClockList().get(1).getName() + "<=" + actionGuards.get(i).getTimeGuards().get(1).getUpperBound());
                    e.append(",");
                    upperc1=actionGuards.get(i).getTimeGuards().get(1).getUpperBound();
                }
                e.append(dtta.getClockList().get(1).getName() + ">=" + difference[1]);
                lowerc2=difference[1];
                e.append(",");

                for (int m = 0; m < dtta.getClockList().size() - 1; m++) {
                    for (int j = m + 1; j < dtta.getClockList().size(); j++) {
                        BigDecimal b1 = new BigDecimal(Double.toString(difference[m]));
                        BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));
                        e.append(dtta.getClockList().get(m).getName() + "-" + dtta.getClockList().get(j).getName() + "==" + b1.subtract(b2).doubleValue());
                        e.append(",");
                        diff=b1.subtract(b2).doubleValue();
                    }
                }
                e.deleteCharAt(e.lastIndexOf(","));
                System.out.println(e.toString());
//                    String[] aa = new String[]{"python", "D:\\ExpressionZ3Solver.py", c.toString(), e.toString()};
//                    Process proc = Runtime.getRuntime().exec(aa);// 执行py文件
//
//                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//                    String line = null;
//                    while ((line = in.readLine()) != null) {
//                        System.out.println("有输出");
//                        System.out.println(line);
//                        if (line.equals("[unsat]")) {
//                            return null;
//                        } else {
//                        }
//                        String[] strs = line.split(",");
//                        String[] s1 = new String[strs.length - 1];
//                        for (int i1 = 1; i1 < strs.length; i1++) {
//                            s1[i1 - 1] = strs[i1];
//                        }
//                        for (int i1=0;i1<s1.length;i1++){
//                            System.out.println("s1[i1]:"+s1[i1]);
//                        }
//                        String[] s2 = new String[s1.length];
//                        for (int i1 = 0; i1 < s1.length; i1++) {
//                            String[] a = s1[i1].split(":");
//                            s2[i1] = a[1];
//                        }
//                        for (int i1=0;i1<s2.length;i1++){
//                            System.out.println("s2[i1]:"+s2[i1]);
//                        }
//
//
//                        String[] s5 = new String[s1.length];
//                        for (int i1 = 0; i1 < s1.length; i1++) {
//                            String[] a = s1[i1].split(":");
//                            s5[i1] = a[0].substring(a[0].lastIndexOf("'")-1,a[0].lastIndexOf("'"));
//                        }
//                        for (int i1=0;i1<s5.length;i1++){
//                            System.out.println("s5[i1]:"+s5[i1]);
//                        }
//
//
//                        String[] s3 = new String[s2.length];
//                        for (int i1 = 0; i1 < s2.length; i1++) {
//                            s3[i1] = s2[i1].substring(s2[i1].indexOf("'") + 1, s2[i1].lastIndexOf("'"));
//                        }
//                        for (int i1=0;i1<s3.length;i1++){
//                            System.out.println("s3[i1]:"+s3[i1]);
//                        }
//                        Double[] values = new Double[s3.length];
//                        for (int i1 = 0; i1 < values.length; i1++) {
//
//                            if (s3[i1].contains("/")) {
//                                String a = s3[i1].split("/")[0];
//                                String b = s3[i1].split("/")[1];
//                                values[i1] = Double.valueOf(a) / Double.valueOf(b);
//                            } else {
//                                values[i1] = Double.valueOf(s3[i1]);
//                            }
//                        }
//                        for (int i1=0;i1<values.length;i1++){
//                            System.out.println("values[i1]:"+values[i1]);
//                        }
//                        for (int m = 0; m < strs.length; m++) {
//                            strs[m] = strs[m].replaceAll("\\s*", "");
//                        }
//
//
//                        Map<Clock, Double> map2 = new HashMap<>();
//                        for (int i1=0;i1<values.length;i1++){
//                            System.out.println(values[i1]);
//                        }
//                        for (int i1 = 0; i1 < s5.length; i1++) {
//                            map2.put(dtta.getClockList().get(Integer.valueOf(s5[i1]).intValue()), values[i1]);
//                        }
                double[] results=z3( diff, lowera1, lowera2, uppera1, lowerc1, lowerc2, upperc1,actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen(),actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen(),actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen(),actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen());
                if(results==null){
                    return null;
                }
                Map<Clock, Double> map2 = new HashMap<>();
                for (int i1 = 0; i1 < dtta.getClockList().size(); i1++) {
                    map2.put(dtta.getClockList().get(i1), results[i1]);
                }
                TwoClockLogicAction twoClockLogicAction1 = new TwoClockLogicAction(actionGuards.get(i).getSymbol(), map2);
                twoClockLogicActionList.add(twoClockLogicAction1);
                twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);

                TwoClockLogicTimeWord logicTimeWord2 = new TwoClockLogicTimeWord(null);
                if (prefix != null && prefix.getTimedActions() != null && prefix.getTimedActions().size() != 0) {
                    logicTimeWord2 = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
                    logicTimeWord2.setTimedActions(TwoClockResetLogicTimeWord.logicTimeWord(prefix).getTimedActions());
                    logicTimeWord2 = TwoClockLogicTimeWord.concat(logicTimeWord2, twoClockLogicTimeWord);
                } else {
                    logicTimeWord2 = twoClockLogicTimeWord;
                }
                TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1 = dtta.transferResetbyteacher(logicTimeWord2);
                twoClockResetLogicTimeWord = twoClockResetLogicTimeWord1;
                twoClockResetLogicTimeWord.setTimedActions(twoClockResetLogicTimeWord1.getTimedActions());
                for (int p = 0; p < dtta.getClockList().size(); p++) {
                    if (!twoClockResetLogicTimeWord1.getLastResetAction().isReset(dtta.getClockList().get(p))) {
                        difference[p] = twoClockResetLogicTimeWord1.getLastResetAction().getValue(dtta.getClockList().get(p));
                    } else {
                        difference[p] = 0d;
                    }
                }
                //  }
//                    in.close();
//                    proc.waitFor();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                {
                }

            }
        }
        else {
            return prefix;
        }
        return twoClockResetLogicTimeWord;
    }

    public List<TwoClockResetLogicTimeWord> dfs2(long START,List<TimeGuard> timeGuardList,int index, int size, List<Double> newstring,TwoClockLogicTimeWord logicTimeWord,TwoClockLogicTimeWord logicTimeWordH,Clock x,Clock y,Clock Hx,Clock Hy, TwoClockTA hypothesis,List<TwoClockResetLogicTimeWord> twoClockResetLogicTimeWord){
        long END=System.currentTimeMillis();
        if(END-START>2000){
            return null;
        }
        dfs2c=dfs2c+1;
        if(index==size-1){
            if(timeGuardList.get(index).isPass(timeGuardList.get(index).getLowerBound())) {
                BigDecimal b1;
                BigDecimal b2;
                for (double value1 = timeGuardList.get(index).getLowerBound(); timeGuardList.get(index).isPass(value1); value1 = b1.add(b2).doubleValue()) {
                    newstring.add(value1);
                    Map<Clock, Double> map1 = new HashMap<>();
                    map1.put(dtta.getClockList().get(index % dtta.getClockList().size()),newstring.get(index));
                    for (int i1=0;i1<dtta.getClockList().size();i1++){
                        if(i1!=index % dtta.getClockList().size()){
                            map1.put(dtta.getClockList().get(i1), logicTimeWord.get(index / dtta.getClockList().size()).getValue(dtta.getClockList().get(i1)));
                        }
                    }
                    logicTimeWord.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    logicTimeWordH.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    //    System.out.println("logicTimeWord:"+logicTimeWord);
                    TwoClockResetLogicTimeWord timeWord=analyzethree(logicTimeWord, logicTimeWordH, hypothesis);
                    //    System.out.println("timeword:"+timeWord);
                    if (timeWord == null) {
                    } else {
                        twoClockResetLogicTimeWord.add(timeWord);
                        return twoClockResetLogicTimeWord;
                    }

                    newstring.remove(index);
                    b1 = new BigDecimal(Double.toString(value1));
                    b2 = new BigDecimal(Double.toString(0.05));
                }
            }
            else {
                BigDecimal b1=new BigDecimal(Double.toString(timeGuardList.get(index).getLowerBound()));
                BigDecimal b2=new BigDecimal(Double.toString(0.05));;
                for (double value1 = b1.add(b2).doubleValue(); timeGuardList.get(index).isPass(value1); value1 =b1.add(b2).doubleValue()) {
                    newstring.add(value1);
                    Map<Clock, Double> map1 = new HashMap<>();
                    map1.put(dtta.getClockList().get(index % dtta.getClockList().size()),newstring.get(index));
                    for (int i1=0;i1<dtta.getClockList().size();i1++){
                        if(i1!=index % dtta.getClockList().size()){
                            map1.put(dtta.getClockList().get(i1), logicTimeWord.get(index / dtta.getClockList().size()).getValue(dtta.getClockList().get(i1)));
                        }
                    }
                    logicTimeWord.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    logicTimeWordH.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    //    System.out.println("logicTimeWord2:"+logicTimeWord);
                    TwoClockResetLogicTimeWord timeWord=analyzethree(logicTimeWord, logicTimeWordH, hypothesis);
                    // System.out.println("timeword:"+timeWord);
                    if (timeWord == null) {
                    } else {
                        twoClockResetLogicTimeWord.add(timeWord);
                        System.out.println("dfs2c:"+dfs2c);
                        return twoClockResetLogicTimeWord;
                    }
                    newstring.remove(index);
                    b1 = new BigDecimal(Double.toString(value1));
                    b2 = new BigDecimal(Double.toString(0.05));
                }
            }
        }
        else {
            if(timeGuardList.get(index).isPass(timeGuardList.get(index).getLowerBound())) {
                BigDecimal b1;
                BigDecimal b2;
                for (double value1 = timeGuardList.get(index).getLowerBound(); timeGuardList.get(index).isPass(value1); value1 = b1.add(b2).doubleValue()) {
                    newstring.add(value1);
                    Map<Clock, Double> map1 = new HashMap<>();

                    map1.put(dtta.getClockList().get(index % dtta.getClockList().size()),newstring.get(index));
                    for (int i1=0;i1<dtta.getClockList().size();i1++){
                        if(i1!=index % dtta.getClockList().size()){
                            map1.put(dtta.getClockList().get(i1), logicTimeWord.get(index / dtta.getClockList().size()).getValue(dtta.getClockList().get(i1)));
                        }
                    }
                    logicTimeWord.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    logicTimeWordH.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    dfs2(START,timeGuardList, index+1, size, newstring,logicTimeWord,logicTimeWordH,x,y,Hx,Hy,hypothesis,twoClockResetLogicTimeWord);
//                    if(twoClockResetLogicTimeWord.size()!=0||dfs2c>10000){
                    if(twoClockResetLogicTimeWord.size()!=0){
                        System.out.println("dfs2c:"+dfs2c);
                        return twoClockResetLogicTimeWord;
                    }
                    newstring.remove(index);
                    b1 = new BigDecimal(Double.toString(value1));
                    b2 = new BigDecimal(Double.toString(0.05));
                }
            }
            else {
                BigDecimal b1=new BigDecimal(Double.toString(timeGuardList.get(index).getLowerBound()));
                BigDecimal b2=new BigDecimal(Double.toString(0.05));;
                for (double value1 = b1.add(b2).doubleValue(); timeGuardList.get(index).isPass(value1); value1 =b1.add(b2).doubleValue()) {
                    newstring.add(value1);
                    Map<Clock, Double> map1 = new HashMap<>();

                    map1.put(dtta.getClockList().get(index % dtta.getClockList().size()),newstring.get(index));
                    for (int i1=0;i1<dtta.getClockList().size();i1++){
                        if(i1!=index % dtta.getClockList().size()){
                            map1.put(dtta.getClockList().get(i1), logicTimeWord.get(index / dtta.getClockList().size()).getValue(dtta.getClockList().get(i1)));
                        }
                    }
                    logicTimeWord.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    logicTimeWordH.getTimedActions().get((int) (index / dtta.getClockList().size())).setClockValueMap(map1);
                    dfs2(START,timeGuardList, index+1, size, newstring,logicTimeWord,logicTimeWordH,x,y,Hx,Hy,hypothesis,twoClockResetLogicTimeWord);
//                    if(twoClockResetLogicTimeWord.size()!=0||dfs2c>10000){
                    if(twoClockResetLogicTimeWord.size()!=0){
                        System.out.println("dfs2c:"+dfs2c);
                        return twoClockResetLogicTimeWord;
                    }
                    newstring.remove(index);
                    b1 = new BigDecimal(Double.toString(value1));
                    b2 = new BigDecimal(Double.toString(0.05));
                }
            }
        }
        if(twoClockResetLogicTimeWord.size()!=0) {
            System.out.println("dfs2c:"+dfs2c);
            return twoClockResetLogicTimeWord;
        }
        else {
            return null;
        }
    }

    public DelayTimeWord LogictoDelay2(TwoClockResetLogicTimeWord timeWord){
        List<DelayTimedAction> delayTimedActions = new ArrayList<>();
        for (Clock clock : dtta.getClockList()) {
            BigDecimal b3 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(0).getValue(clock)));
            BigDecimal b4 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(0).getValue(dtta.getClockList().get(0))));
//            System.out.println(b3.subtract(b4).doubleValue());
            if (b3.subtract(b4).doubleValue() == 0.0) {

            } else {
                return null;
            }
        }
        //     System.out.println("合法");
        DelayTimedAction delayTimedAction = new DelayTimedAction(timeWord.getTimedActions().get(0).getSymbol(), timeWord.getTimedActions().get(0).getValue(dtta.getClockList().get(0)));
        delayTimedActions.add(delayTimedAction);
        for (int i1 = 0; i1 < timeWord.getTimedActions().size()-1; i1++) {
            double defference;

            BigDecimal b1 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1).getValue(dtta.getClockList().get(0))));
            BigDecimal b2 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1 + 1).getValue(dtta.getClockList().get(0))));
            if(timeWord.getTimedActions().get(i1).isReset(dtta.getClockList().get(0))){
                defference=timeWord.getTimedActions().get(i1+1).getValue(dtta.getClockList().get(0));
            }
            else {
                defference=b2.subtract(b1).doubleValue();
            }

            for (int m=1;m<dtta.getClockList().size();m++) {
                double defferenceeach;

                BigDecimal b3 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1).getValue(dtta.getClockList().get(m))));
                BigDecimal b4 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1 + 1).getValue(dtta.getClockList().get(m))));
                if(timeWord.getTimedActions().get(i1).isReset(dtta.getClockList().get(m))){
                    defferenceeach=timeWord.getTimedActions().get(i1+1).getValue(dtta.getClockList().get(m));
                }
                else {
                    defferenceeach=b4.subtract(b3).doubleValue();
                }
                BigDecimal b5 = new BigDecimal(Double.toString(defference));
                BigDecimal b6 = new BigDecimal(Double.toString(defferenceeach));

                if (b5.subtract(b6).doubleValue() == 0.0) {

                } else {
                    return null;
                }
            }
            DelayTimedAction delayTimedAction2 = new DelayTimedAction(timeWord.getTimedActions().get(i1 + 1).getSymbol(), defference);
            delayTimedActions.add(delayTimedAction2);
        }
        DelayTimeWord twoClockTimedWord = new DelayTimeWord(delayTimedActions);
        return twoClockTimedWord;
    }

    public TwoClockResetLogicTimeWord analyzethree(TwoClockLogicTimeWord logicTimeWord,TwoClockLogicTimeWord logicTimeWordH,TwoClockTA hypothesis){
        TwoClockResetLogicTimeWord resetLogicTimeWord1 = dtta.transferResetbyteacher(logicTimeWord);
        DelayTimeWord delayTimeWord=LogictoDelay2(resetLogicTimeWord1);
        if(delayTimeWord==null){
            return null;
        }
        BooleanAnswer answer1=dtta.reach(delayTimeWord);
        BooleanAnswer answer2=hypothesis.reach(delayTimeWord);
        System.out.println(answer1);
        System.out.println(answer2);
        if(answer1.isAccept()!=answer2.isAccept()){
            return resetLogicTimeWord1;
        }
        return null;

    }
    public static void completeTwoClock(TwoClockTA dtta) {

//        Clock clock1 = dtta.getClockList().get(0);
//        Clock clock2 = dtta.getClockList().get(1);
        List<TaTransition> transitionList = dtta.getTransitions();
        List<TaTransition> complementaryTranList = new ArrayList<>();
        List<TaLocation> locationList = dtta.getLocations();
        Set<String> sigma = dtta.getSigma();

        TaLocation sink = new TaLocation(String.valueOf(dtta.size() + 1), "sink", false, false);
        for (TaLocation location : locationList) {
            for (String symbol : sigma) {
                List<TaTransition> transitions = dtta.getTransitions(location, symbol, null);
                if (transitions.isEmpty()) {
                    Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                    for (int i=0;i<dtta.getClockList().size();i++){
                        clockTimeGuardMap.put(dtta.getClockList().get(i),new TimeGuard("[0,+)"));
                    }
//                    clockTimeGuardMap.put(clock1, new TimeGuard("[0,+)"));
//                    clockTimeGuardMap.put(clock2, new TimeGuard("[0,+)"));
                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                    Set<Clock> resetClocks = new HashSet<>();
                    for (int i=0;i<dtta.getClockList().size();i++) {
                /*        resetClocks.add(clock1);
                        resetClocks.add(clock2);*/
                        resetClocks.add(dtta.getClockList().get(i));
                    }
                    TaTransition transition = TaTransition.builder()
                            .sourceLocation(location)
                            .targetLocation(sink)
                            .symbol(symbol)
                            .resetClockSet(resetClocks)
                            .clockTimeGuardMap(edgeTimeGuard)
                            .build();
                    complementaryTranList.add(transition);
                    continue;
                }
//                List<TimeGuard> timeGuardList1 = obtainGuardList(transitions, clock1);
//                List<TimeGuard> timeGuardList2 = obtainGuardList(transitions, clock2);
                //     System.out.println(transitions.get(0));
//                complementaryTranList.addAll(complementary(transitions, sink, clock1, clock2));
                complementaryTranList.addAll(complementary(dtta,transitions, sink, dtta.getClockList()));
                //  }
            }
        }
        if (complementaryTranList.isEmpty()) {
            return;
        }

        Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
        clockTimeGuardMap.put(dtta.getClockList().get(0), new TimeGuard("[0,+)"));
        // clockTimeGuardMap.put(clock2, new TimeGuard("[0,+)"));
        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
        edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
        Set<Clock> resetClocks = new HashSet<>();
        resetClocks.add(dtta.getClockList().get(0));
        //    resetClocks.add(clock2);
        for (String symbol : sigma) {
            TaTransition transition = TaTransition.builder()
                    .sourceLocation(sink)
                    .targetLocation(sink)
                    .symbol(symbol)
                    .resetClockSet(resetClocks)
                    .clockTimeGuardMap(edgeTimeGuard)
                    .build();
            complementaryTranList.add(transition);
        }

        transitionList.addAll(complementaryTranList);
        locationList.add(sink);
//        transitionList.sort(new OTATranComparator(clock1));
//        transitionList.sort(new OTATranComparator(clock2));
        for (TaLocation location : locationList) {
            for (String symbol : sigma) {
                List<TaTransition> transitions = dtta.getTransitions(location, symbol, sink);
                for (TaTransition transition : transitions) {
                    List<Clock> clockList = dtta.getClockList();
                    for (Clock clock : clockList)
//                        if (transition.getTimeGuard(clock) == null&&!transition.getEdgeTimeGuard().getDifferenceEdgeTimeGuards()) {
                        if (transition.getTimeGuard(clock) == null) {
//                            transition.getClockTimeGuardMap().put(clock, new TimeGuard("[0,+)"));
                            transition.getClockTimeGuardMap().getClockTimeGuardMap().put(clock, new TimeGuard("[0,+)"));
                            transition.getResetClockSet().add(clock);
                        }
                }
            }
        }
    }
    public static double[] sort(double X[]) {
        for(int i = 0;i < X.length;i++){
            for(int j = 0;j < X.length-i-1;j++){
                if(X[j] > X[j+1]){
                    double t = X[j];
                    X[j] = X[j+1];
                    X[j+1] = t;
                }
            }
        }
        return  X;
    }
    public static double[] delete(double X[],int j) {
        for (int i=j;i<X.length-1;i++){
            X[i]=X[i+1];
        }
        return X;
    }
    private static List<TaTransition> complementary(TwoClockTA dtta, List<TaTransition> transitionList, TaLocation targetLocation, List<Clock> clockList) {
        //System.out.println(transitionList);
        //   List<TaTransition> complementaryTranList=new ArrayList<>();
        List<TaTransition> transitionList1=new ArrayList<>();
        for (int i=0;i<transitionList.size();i++){
            List<Map<Clock,TimeGuard>> mapList=new ArrayList<>();
            for (int j=0;j<transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().size();j++){
                if(transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).getLowerBound()==0&&transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).isLowerBoundClose()){

                }
                else {
                    TimeGuard timeGuard = new TimeGuard(false,!transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).isLowerBoundOpen(),0,transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).getLowerBound());
                    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
                    clockTimeGuardMap.put(clockList.get(j), timeGuard);
                    mapList.add(clockTimeGuardMap);
                }
                if(transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).getUpperBound()==TimeGuard.MAX_TIME){

                }
                else {
                    TimeGuard timeGuard = new TimeGuard(!transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).isUpperBoundOpen(),true,transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(dtta.getClockList().get(j)).getUpperBound(),TimeGuard.MAX_TIME);
                    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
                    clockTimeGuardMap.put(clockList.get(j), timeGuard);
                    mapList.add(clockTimeGuardMap);
                }
            }
            if(transitionList1.size()!=0){
                List<TaTransition> transitionList2=new ArrayList<>();
                for (int p=0;p<transitionList1.size();p++){
                    System.out.println("transitionList1:"+transitionList1.get(p));
                    System.out.println("mapList:"+mapList);
                    for (int k=0;k<mapList.size();k++) {
                        EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
                        Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
                        for (Clock clock:transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().keySet()){
                            clockTimeGuardMap.put(clock,transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock));}
                        for (Clock clock:mapList.get(k).keySet()){
                            boolean sameclock=false;
                            for (Clock clock1:transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().keySet()){
                                if(clock1.equals(clock)){
                                    sameclock=true;
                                    int lower;
                                    boolean loweropen;
                                    if(mapList.get(k).get(clock).getLowerBound()<transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound()||
                                            mapList.get(k).get(clock).getLowerBound()==transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound()&&mapList.get(k).get(clock).isLowerBoundClose()&&transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isLowerBoundOpen()){
                                        lower=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound();
                                        loweropen=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isLowerBoundOpen();
                                        System.out.println("lower:"+lower);
                                    }
                                    else {
                                        lower=mapList.get(k).get(clock).getLowerBound();
                                        loweropen=mapList.get(k).get(clock).isLowerBoundOpen();
                                        System.out.println("lower2:"+lower);
                                    }

                                    int upper;
                                    boolean upperopen;
                                    if(mapList.get(k).get(clock).getUpperBound()>transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound()||
                                            mapList.get(k).get(clock).getUpperBound()==transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound()&&mapList.get(k).get(clock).isUpperBoundClose()&&transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isUpperBoundOpen()){
                                        upper=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound();
                                        upperopen=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isUpperBoundOpen();
                                        System.out.println("upper:"+upper);
                                    }
                                    else {
                                        upper=mapList.get(k).get(clock).getUpperBound();
                                        upperopen=mapList.get(k).get(clock).isUpperBoundOpen();
                                        System.out.println("upper2:"+upper);
                                    }
                                    TimeGuard timeGuard=new TimeGuard(loweropen,upperopen,lower,upper);
                                    clockTimeGuardMap.put(clock,timeGuard);
                                }
                            }
                            if(!sameclock){
                                clockTimeGuardMap.put(clock,mapList.get(k).get(clock));
                            }
                        }
                        edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                        Set<Clock> clockSet = new HashSet<>();
                        for (int n = 0; n < clockList.size(); n++) {
                            clockSet.add(clockList.get(n));
                        }
                        TaTransition taTransition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, clockSet);
                        transitionList2.add(taTransition);
                    }
                }
                transitionList1.clear();
                transitionList1=transitionList2;
            }
            else {
                for (int k=0;k<mapList.size();k++) {
                    EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
                    edgeTimeGuard.setClockTimeGuardMap(mapList.get(k));
                    Set<Clock> clockSet = new HashSet<>();
                    for (int n = 0; n < clockList.size(); n++) {
                        clockSet.add(clockList.get(n));
                    }
                    TaTransition taTransition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, clockSet);
                    System.out.println("taTransition4:"+taTransition);
                    transitionList1.add(taTransition);
                }


            }
        }
//        return complementaryTranList;
        List<TaTransition> delete=new ArrayList<>();
//        for (TaTransition transition:transitionList1){
//            if(transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound()>transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getUpperBound()||
//            transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound()==transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getUpperBound()&&
//                    transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen()||
//                    transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound()==transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getUpperBound()&&
//                            transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).isUpperBoundOpen()){
//                delete.add(transition);
//            }
//        }
        transitionList1.removeAll(delete);
        return   transitionList1;
    }


    private static List<TimeGuard> obtainGuardList(List<TaTransition> transitionList, Clock clock) {
        List<TimeGuard> timeGuardList = new ArrayList<>();
        for (TaTransition transition : transitionList) {
            timeGuardList.add(transition.getTimeGuard(clock));
        }
        return timeGuardList;
    }

    public  List<ActionGuardsSet> transfer(List<TransitionState> states) {
        //初始化区间范围
        List<ActionGuardsSet> actionGuards = new ArrayList<>();
        for (TransitionState state : states) {
            String action = state.getSymbol();
            List<TimeGuard> timeGuardList = new ArrayList<>();
            for (int i=0;i<dtta.getClockList().size();i++){
                Value lower = state.getDbm().getMatrix()[0][i+1];
                Value upper = state.getDbm().getMatrix()[i+1][0];
                TimeGuard guard = new TimeGuard(!lower.isEqual(), !upper.isEqual(), (-1) * lower.getValue(), upper.getValue());
                timeGuardList.add(guard);
            }
            ActionGuardsSet actionGuardsSet = new ActionGuardsSet(action, timeGuardList);
            actionGuards.add(actionGuardsSet);
        }
        return actionGuards;
    }
    public  double[] z3(double diff,double lowera1,double lowera2,double uppera1,double lowerc1,double lowerc2,double upperc1,boolean alo,boolean auo,boolean clo,boolean cuo) {
        System.out.println("h w");
        System.out.println(diff);
        System.out.println(lowera1);
        System.out.println(lowera2);
        System.out.println(uppera1);
        System.out.println(lowerc1);
        System.out.println(lowerc2);
        System.out.println(upperc1);
        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);
        Solver s = ctx.mkSolver();
        RealExpr a = ctx.mkRealConst("a");
        RealExpr c = ctx.mkRealConst("c");
        ArithExpr left = ctx.mkSub(a, c);
        String s2=String.valueOf( (int)(diff*1000));
        BoolExpr equation = ctx.mkEq(left, ctx.mkReal(s2.concat("/1000")));
        s.add(equation);
        if(alo){
            String s1=String.valueOf( (int)(lowera1*1000));
            s.add(ctx.mkGt(a,  ctx.mkReal(s1.concat("/1000"))));
        }
        else {
            String s1=String.valueOf( (int)(lowera1*1000));
            s.add(ctx.mkGe(a,  ctx.mkReal(s1.concat("/1000"))));
        }
        String s3=String.valueOf( (int)(lowera2*1000));
        s.add(ctx.mkGe(a,  ctx.mkReal(s3.concat("/1000"))));
        if(auo){
            String s1=String.valueOf( (int)(uppera1*1000));
            s.add(ctx.mkGt(  ctx.mkReal(s1.concat("/1000")),a));
        }
        else {
            String s1=String.valueOf( (int)(uppera1*1000));
            s.add(ctx.mkGe(  ctx.mkReal(s1.concat("/1000")),a));
        }
        if(clo){
            String s1=String.valueOf( (int)(lowerc1*1000));
            s.add(ctx.mkGt(c,  ctx.mkReal(s1.concat("/1000"))));
        }
        else {
            String s1=String.valueOf( (int)(lowerc1*1000));
            s.add(ctx.mkGe(c,  ctx.mkReal(s1.concat("/1000"))));
        }
        String s4=String.valueOf( (int)(lowerc2*1000));
        s.add(ctx.mkGe(c,  ctx.mkReal(s4.concat("/1000"))));
        if(cuo) {
            String s1=String.valueOf( (int)(upperc1*1000));
            s.add(ctx.mkGt( ctx.mkReal(s1.concat("/1000")),c));
        }
        else {
            String s1=String.valueOf( (int)(upperc1*1000));
            s.add(ctx.mkGe(  ctx.mkReal(s1.concat("/1000")),c));
        }
        Status result = s.check();
        if (result == Status.SATISFIABLE){
            System.out.println("model for: x + y*c*e = d + a, a > 0");
            System.out.print(s.getModel());
            System.out.println(a.toString());
            Expr a_value = s.getModel().evaluate(a, false);
            Expr c_value = s.getModel().evaluate(c, false);
            double[] results=new double[2];
            if (a_value.toString().contains("/")) {
                String d = a_value.toString().split("/")[0];
                String b = a_value.toString().split("/")[1];
                results[0] = Double.valueOf(d) / Double.valueOf(b);
            } else {
                results[0] = Double.valueOf(a_value.toString());
            }
            if (c_value.toString().contains("/")) {
                String d = c_value.toString().split("/")[0];
                String b = c_value.toString().split("/")[1];
                results[1] = Double.valueOf(d) / Double.valueOf(b);
            } else {
                results[1] = Double.valueOf(c_value.toString());
            }
            return results;

        }
        else if(result == Status.UNSATISFIABLE)
            System.out.println("unsat");
        return null;
//        else
//            System.out.println("unknow");
//        return null;
    }
    public  double[] z33(List<Set<Clock>> resets,List<ActionGuardsSet> actionGuards) {
        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);
        Solver s = ctx.mkSolver();
        RealExpr[] t=new RealExpr[actionGuards.size()];
        for (int i=0;i<actionGuards.size();i++){
            t[i]=ctx.mkRealConst("t".concat(String.valueOf(i)));
        }
        RealExpr[] x=new RealExpr[actionGuards.size()];
        for (int i=0;i<actionGuards.size();i++){
            x[i]=ctx.mkRealConst("x".concat(String.valueOf(i)));
        }
        RealExpr[] y=new RealExpr[actionGuards.size()];
        for (int i=0;i<actionGuards.size();i++){
            y[i]=ctx.mkRealConst("y".concat(String.valueOf(i)));
        }
        for (int i=0;i<actionGuards.size();i++){
            s.add(ctx.mkGe(t[i],ctx.mkReal(0)));
            s.add(ctx.mkGe(x[i],ctx.mkReal(0)));
            s.add(ctx.mkGe(y[i],ctx.mkReal(0)));
        }
        s.add(ctx.mkEq(x[0],ctx.mkReal(0)));
        s.add(ctx.mkEq(y[0],ctx.mkReal(0)));
        for (int i=0;i<actionGuards.size();i++){
            if(actionGuards.get(i).getTimeGuards().get(0).isLowerBoundOpen()){
                s.add(ctx.mkGt(ctx.mkAdd(t[i],x[i]),ctx.mkReal(actionGuards.get(i).getTimeGuards().get(0).getLowerBound())));
            }
            else {
                s.add(ctx.mkGe(ctx.mkAdd(t[i],x[i]),ctx.mkReal(actionGuards.get(i).getTimeGuards().get(0).getLowerBound())));
            }
            if(actionGuards.get(i).getTimeGuards().get(0).isUpperBoundOpen()){
                s.add(ctx.mkGt(ctx.mkReal(actionGuards.get(i).getTimeGuards().get(0).getUpperBound()),ctx.mkAdd(t[i],x[i])));
            }
            else {
                s.add(ctx.mkGe(ctx.mkReal(actionGuards.get(i).getTimeGuards().get(0).getUpperBound()),ctx.mkAdd(t[i],x[i])));
            }
            if(i!=actionGuards.size()-1) {
                if (!resets.get(i).contains(dtta.getClockList().get(0))) {
                    s.add(ctx.mkEq(ctx.mkAdd(t[i],x[i]),x[i+1]));
                }
                else {
                    s.add(ctx.mkEq(ctx.mkReal(0),x[i+1]));
                }
            }
        }

        for (int i=0;i<actionGuards.size();i++){
            if(actionGuards.get(i).getTimeGuards().get(1).isLowerBoundOpen()){
                s.add(ctx.mkGt(ctx.mkAdd(t[i],y[i]),ctx.mkReal(actionGuards.get(i).getTimeGuards().get(1).getLowerBound())));
            }
            else {
                s.add(ctx.mkGe(ctx.mkAdd(t[i],y[i]),ctx.mkReal(actionGuards.get(i).getTimeGuards().get(1).getLowerBound())));
            }
            if(actionGuards.get(i).getTimeGuards().get(1).isUpperBoundOpen()){
                s.add(ctx.mkGt(ctx.mkReal(actionGuards.get(i).getTimeGuards().get(1).getUpperBound()),ctx.mkAdd(t[i],y[i])));
            }
            else {
                s.add(ctx.mkGe(ctx.mkReal(actionGuards.get(i).getTimeGuards().get(1).getUpperBound()),ctx.mkAdd(t[i],y[i])));
            }
            if(i!=actionGuards.size()-1) {
                if (!resets.get(i).contains(dtta.getClockList().get(1))) {
                    s.add(ctx.mkEq(ctx.mkAdd(t[i],y[i]),y[i+1]));
                }
                else {
                    s.add(ctx.mkEq(ctx.mkReal(0),y[i+1]));
                }
            }
        }
        System.out.println(s.toString());
//
//        double[] clockvalue=new double[dtta.getClockList().size()];
//        for (int i=0;i<actionGuards.size();i++){
//            s.add(ctx.mkGe(t[i],ctx.mkReal(0)));
//            for (int m=0;m<dtta.getClockList().size();m++){
//                double results=0.0;
//                for (int k=0;k<i;k++){
//                    if(resets.get(k).contains(dtta.getClockList().get(m))){
//
//                    }
//                    else {
//                        BigDecimal bigDecimal=new BigDecimal(Double.toString(results));
//                        double dd=0.0;
//                        if (t[k].toString().contains("/")) {
//                            String d = t[k].toString().split("/")[0];
//                            String b = t[k].toString().split("/")[1];
//                            dd = Double.valueOf(d) / Double.valueOf(b);
//                        } else {
//                            System.out.println(t[k].toString());
//                            dd = Double.valueOf(t[k].toString());
//                        }
//                        BigDecimal bigDecimal2=new BigDecimal(Double.toString(dd));
//                        results=bigDecimal.add(bigDecimal2).doubleValue();
//                    }
//                }
//
//                BigDecimal bigDecimal1=new BigDecimal(Double.toString(results));
//                BigDecimal bigDecimal2=new BigDecimal(Double.toString(actionGuards.get(i).getTimeGuards().get(m).getLowerBound()));
//                Double diffl=bigDecimal2.subtract(bigDecimal1).doubleValue();
//                BigDecimal bigDecimal3=new BigDecimal(Double.toString(actionGuards.get(i).getTimeGuards().get(m).getUpperBound()));
//                Double diffu=bigDecimal3.subtract(bigDecimal1).doubleValue();
//                if(actionGuards.get(i).getTimeGuards().get(m).isLowerBoundOpen()){
//                    String s2=String.valueOf( (int)(diffl*1000));
//                    s.add(ctx.mkGt(t[i],  ctx.mkReal(s2.concat("/1000"))));
//                }
//                else {
//                    String s2=String.valueOf( (int)(diffl*1000));
//                    s.add(ctx.mkGe(t[i],  ctx.mkReal(s2.concat("/1000"))));
//                }
//                if(actionGuards.get(i).getTimeGuards().get(m).isUpperBoundOpen()){
//                    String s2=String.valueOf( (int)(diffu*1000));
//                    s.add(ctx.mkGt(  ctx.mkReal(s2.concat("/1000")),t[i]));
//                }
//                else {
//                    String s2=String.valueOf( (int)(diffu*1000));
//                    s.add(ctx.mkGe(  ctx.mkReal(s2.concat("/1000")),t[i]));
//                }
//            }
//            s.add(ctx.mkGe(t[i],ctx.mkReal(0)));
//        }
        Status result = s.check();
        if (result == Status.SATISFIABLE){
            System.out.println("model for: x + y*c*e = d + a, a > 0");
            System.out.print(s.getModel());
            double[] results=new double[actionGuards.size()];
            for (int i=0;i<actionGuards.size();i++){
                Expr a_value = s.getModel().evaluate(t[i], false);
                if (a_value.toString().contains("/")) {
                    String d = a_value.toString().split("/")[0];
                    String b = a_value.toString().split("/")[1];
                    results[i] = Double.valueOf(d) / Double.valueOf(b);
                } else {
                    results[i] = Double.valueOf(a_value.toString());
                }
            }
            //  System.out.println("results:"+results[0]+results[1]);
            return results;

        }
        else if(result == Status.UNSATISFIABLE)
            System.out.println("unsat");
        return null;
//        else
//            System.out.println("unknow");
//        return null;
    }
    public TwoClockResetLogicTimeWord DelaytoLogic(ResetDelayTimeWord resetDelayTimeWord){
        TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=new TwoClockResetLogicTimeWord(null);
        List<TwoClockResetLogicAction> twoClockResetTimedActions=new ArrayList<>();
        Map<Clock,Double> map=new HashMap<>();
        for (Clock clock:dtta.getClockList()){
            map.put(clock,resetDelayTimeWord.getTimedActions().get(0).getValue());
        }
        TwoClockResetLogicAction timedAction=new TwoClockResetLogicAction(resetDelayTimeWord.getTimedActions().get(0).getSymbol(),resetDelayTimeWord.getTimedActions().get(0).getReset(),map);
        timedAction.setSymbol(resetDelayTimeWord.getTimedActions().get(0).getSymbol());
        timedAction.setClockValueMap(map);
        timedAction.setResetClockSet(resetDelayTimeWord.getTimedActions().get(0).getReset());
        twoClockResetTimedActions.add(timedAction);
        for (int i=1;i<resetDelayTimeWord.size();i++){
            Map<Clock,Double> map2=new HashMap<>();
            for (Clock clock:dtta.getClockList()){
                if(!resetDelayTimeWord.getTimedActions().get(i-1).getReset().contains(clock)){
                    BigDecimal b1 = new BigDecimal(Double.toString(twoClockResetTimedActions.get(twoClockResetTimedActions.size()-1).getValue(clock)));
                    BigDecimal b2 = new BigDecimal(Double.toString(resetDelayTimeWord.getTimedActions().get(i).getValue()));

                    map2.put(clock,b1.add(b2).doubleValue());
                }
                else {
                    map2.put(clock,resetDelayTimeWord.getTimedActions().get(i).getValue());
                }
            }
            TwoClockResetLogicAction timedAction2=new TwoClockResetLogicAction(resetDelayTimeWord.getTimedActions().get(i).getSymbol(),resetDelayTimeWord.getTimedActions().get(i).getReset(),map2);
            timedAction2.setSymbol(resetDelayTimeWord.getTimedActions().get(i).getSymbol());
            timedAction2.setClockValueMap(map2);
            timedAction2.setResetClockSet(resetDelayTimeWord.getTimedActions().get(i).getReset());
            twoClockResetTimedActions.add(timedAction2);
        }
        twoClockResetLogicTimeWord.setTimedActions(twoClockResetTimedActions);
        return twoClockResetLogicTimeWord;
    }
}