package ta.twoClockTA;

import lombok.Data;
import ta.ota.LogicTimeWord;
import ta.ota.LogicTimedAction;
import timedWord.TimedWord;
import timedAction.TwoClockResetTimedAction;
import timedWord.TwoClockTimedWord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class TwoClockResetLogicTimeWord extends TwoClockTimedWord<TwoClockResetLogicAction> {

    private List<TwoClockResetLogicAction> timedActions;

   /*public TwoClockResetLogicTimeWord() {
        timedActions = new ArrayList<>();
    }*/

    public TwoClockResetLogicTimeWord(List<TwoClockResetLogicAction> timedActions) {
        //this.timedActions = timedActions;
        super(timedActions);
    }

    public int size(){
        return timedActions.size();
    }

    public TwoClockResetLogicAction get(int i){
        return timedActions.get(i);
    }

    public Set<TwoClockResetLogicTimeWord> getAllPrefixes() {
        Set<TwoClockResetLogicTimeWord> prefixes = new HashSet<>();
        List<TwoClockResetLogicAction> actionList = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            actionList.add(get(i));
     //       System.out.println("actionList:"+actionList.toString());
            TwoClockResetLogicTimeWord prefixWord = new TwoClockResetLogicTimeWord(new ArrayList<>(actionList));
            prefixWord.setTimedActions(new ArrayList<>(actionList));
      //      System.out.println("prefixWord:"+prefixWord.toString());
            prefixes.add(prefixWord);
      //      System.out.println("prefixes:"+prefixes.toString());
        }
        return prefixes;
    }

    public TwoClockResetLogicTimeWord concat(TwoClockResetLogicTimeWord resetLogicTimeWord) {
        List<TwoClockResetLogicAction> resetLogicActionList = new ArrayList<>();
        resetLogicActionList.addAll(getTimedActions());
        resetLogicActionList.addAll(resetLogicTimeWord.getTimedActions());
        return new TwoClockResetLogicTimeWord(resetLogicActionList);
    }

    public static TwoClockLogicTimeWord logicTimeWord(TwoClockResetLogicTimeWord resetLogicTimeWord) {
//        System.out.println("开始执行logicTimedWord函数");
//        System.out.println("resetLogicTimeWord:"+resetLogicTimeWord);
        List<TwoClockLogicAction> logicTimedActions = new ArrayList<>();
//        if(resetLogicTimeWord.getTimedActions()!=null){
        for(TwoClockResetLogicAction resetlogicactions:resetLogicTimeWord.getTimedActions()) {
            TwoClockLogicAction logicTimedAction = new TwoClockLogicAction(resetlogicactions.getSymbol(), resetlogicactions.getClockValueMap());
            logicTimedActions.add(logicTimedAction);
        }
      //  System.out.println("logicTimedActions为："+logicTimedActions);
        TwoClockLogicTimeWord logicTimeWord=new TwoClockLogicTimeWord(logicTimedActions);
        logicTimeWord.setTimedActions(logicTimedActions);
     //   System.out.println("logicTimeWord为:"+logicTimeWord);
//        return new TwoClockLogicTimeWord(logicTimedActions);//}
       return logicTimeWord;
//        else {
//            System.out.println("执行了为空的函数");
//            return null;
//        }
    }

    public TwoClockLogicAction getLastLogicAction() {
        TwoClockResetLogicAction resetAction = getTimedActions().get(size() - 1);
        return new TwoClockLogicAction(resetAction.getSymbol(),resetAction.getClockValueMap());
    }

    public TwoClockResetLogicAction getLastResetAction() {
        return getTimedActions().get(size() - 1);
    }

    public static TwoClockResetLogicTimeWord emptyWord() {
        return new TwoClockResetLogicTimeWord(new ArrayList<TwoClockResetLogicAction>());
    }
    @Override
    public TwoClockResetLogicTimeWord subWord(int fromIndex, int toIndex) {
        try {
            List<TwoClockResetLogicAction> subList = getTimedActions().subList(fromIndex, toIndex);
           // System.out.println("subList"+subList);
            TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=new TwoClockResetLogicTimeWord(subList);
            twoClockResetLogicTimeWord.setTimedActions(subList);
            return twoClockResetLogicTimeWord;}

        catch (Exception e) {
            return emptyWord();
        }
    }

    public TwoClockResetLogicTimeWord concat(TwoClockResetLogicAction timedAction) {
        List<TwoClockResetLogicAction> resetLogicActionList = new ArrayList<>();
        if(getTimedActions()==null){

        }
        else {
            resetLogicActionList.addAll(getTimedActions());
        }
        resetLogicActionList.add(timedAction);
        return new TwoClockResetLogicTimeWord(resetLogicActionList);
    }


    public TwoClockLogicTimeWord concat2(TwoClockLogicTimeWord logicTimeWord, TwoClockLogicAction timedAction) {
        List<TwoClockLogicAction> resetLogicActionList = new ArrayList<>();
    //    System.out.println(getTimedActions());
        if (logicTimeWord.getTimedActions() == null) {
        } else {
            List<TwoClockLogicAction> LogicActions = logicTimeWord.getTimedActions();
            for (TwoClockLogicAction LogicAction : LogicActions) {
                //resetLogicActionList.addAll(getTimedActions());
                resetLogicActionList.add(LogicAction);
            }
            resetLogicActionList.add(timedAction);
        }
        return new TwoClockLogicTimeWord(resetLogicActionList);
    }



}
