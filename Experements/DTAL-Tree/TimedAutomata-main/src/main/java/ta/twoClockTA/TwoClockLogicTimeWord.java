package ta.twoClockTA;

import lombok.Data;
import timedWord.TimedWord;
import timedWord.TwoClockTimedWord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import timedWord.TimedWord;
import timedWord.TwoClockTimedWord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class TwoClockLogicTimeWord  extends TwoClockTimedWord<TwoClockLogicAction> {

    private List<TwoClockLogicAction> timedActions;
//    public void setTimedActions(List<TwoClockLogicAction> timedActions){
//        this.timedActions=timedActions;
//    }
    //public TwoClockLogicTimeWord() {
    //  timedActions = new ArrayList<>();
    //}

    public TwoClockLogicTimeWord(List<TwoClockLogicAction> timedActions) {
        // this.timedActions = timedActions;
        super(timedActions);
    }

    public int size() {
        return timedActions.size();
    }

    public TwoClockLogicAction get(int i) {
        return timedActions.get(i);
    }

    public Set<TwoClockLogicTimeWord> getAllPrefixes() {
        Set<TwoClockLogicTimeWord> prefixes = new HashSet<>();
        List<TwoClockLogicAction> actionList = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            actionList.add(get(i));
            TwoClockLogicTimeWord prefixWord = new TwoClockLogicTimeWord(new ArrayList<>(actionList));
            prefixes.add(prefixWord);
        }
        return prefixes;
    }

    public static TwoClockLogicTimeWord concat(TwoClockLogicTimeWord timedword1, TwoClockLogicTimeWord timedword2) {
      //  System.out.println("TwoClockLogicTimeWord函数中timedword1"+timedword1);
     //   System.out.println("TwoClockLogicTimeWord函数中timedword2"+timedword2);
        List<TwoClockLogicAction> resetLogicActionList = new ArrayList();
        if (timedword1.getTimedActions() == null) {
            resetLogicActionList.addAll(timedword2.getTimedActions());
        } else {
            resetLogicActionList.addAll(timedword1.getTimedActions());
      //      System.out.println("连接完前缀的resetLogicActionList:"+resetLogicActionList);
            if(timedword2.getTimedActions()!=null) {
                resetLogicActionList.addAll(timedword2.getTimedActions());
       //         System.out.println("连接完后缀的resetLogicActionList:"+resetLogicActionList);
            }
            else {
                TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(resetLogicActionList);
                twoClockLogicTimeWord.setTimedActions(resetLogicActionList);
                return twoClockLogicTimeWord;
            }
        }
        if(timedword1.getTimedActions() == null&&timedword2.getTimedActions()==null){
            return emptyWord();
        }

        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(resetLogicActionList);
        twoClockLogicTimeWord.setTimedActions(resetLogicActionList);
        return twoClockLogicTimeWord;
    }


    public TwoClockLogicAction getLastLogicAction() {
        TwoClockLogicAction logicAction = getTimedActions().get(size() - 1);
        return logicAction;
    }
/*
    public static TwoClockLogicTimeWord emptyWord() {
        return new TwoClockLogicTimeWord();
    }
*/

    public static TwoClockLogicTimeWord emptyWord() {
        // return new TwoClockLogicTimeWord();
        return new TwoClockLogicTimeWord(new ArrayList<TwoClockLogicAction>());
    }

    @Override
    public TwoClockLogicTimeWord subWord(int fromIndex, int toIndex) {
        try {
            System.out.println(getTimedActions());
            List<TwoClockLogicAction> subList = getTimedActions().subList(fromIndex, toIndex);
            TwoClockLogicTimeWord timeWord=new TwoClockLogicTimeWord(null);
            timeWord.setTimedActions(subList);
//            return new TwoClockLogicTimeWord(subList);
            return timeWord;
        } catch (Exception e) {
            return emptyWord();
        }
    }

    @Override
    public TwoClockLogicTimeWord concat(TwoClockLogicAction timedAction) {
        List<TwoClockLogicAction> resetLogicActionList = new ArrayList<>();
    //    System.out.println(getTimedActions());
        if (getTimedActions() == null) {

        } else {
            resetLogicActionList.addAll(getTimedActions());
          //  System.out.println("concact函数中前半截"+resetLogicActionList);
        }
        resetLogicActionList.add(timedAction);
     //   System.out.println("concact函数中加完后半截"+resetLogicActionList);
        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(resetLogicActionList);
        twoClockLogicTimeWord.setTimedActions(resetLogicActionList);
    //    System.out.println("concact函数中twoClockLogicTimeWord"+twoClockLogicTimeWord);
        return twoClockLogicTimeWord;
    }

    public TwoClockLogicTimeWord concat2(TwoClockLogicTimeWord logicTimeWord, TwoClockLogicAction timedAction) {
        List<TwoClockLogicAction> resetLogicActionList = logicTimeWord.getTimedActions();
//        System.out.println(logicTimeWord.toString());
//        System.out.println(timedAction.toString());
//        System.out.println(logicTimeWord.getTimedActions());
        if (resetLogicActionList == null) {
        } else {
        }
//            List<TwoClockLogicAction> LogicActions = logicTimeWord.getTimedActions();
//            for (TwoClockLogicAction LogicAction : LogicActions) {
//                //resetLogicActionList.addAll(getTimedActions());
                resetLogicActionList.add(timedAction);

//            resetLogicActionList.add(timedAction);

        return new TwoClockLogicTimeWord(resetLogicActionList);
    }


}
