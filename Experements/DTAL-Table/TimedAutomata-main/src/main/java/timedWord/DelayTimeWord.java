package timedWord;

import timedAction.DelayTimedAction;

import java.util.ArrayList;
import java.util.List;

public class DelayTimeWord extends TimedWord<DelayTimedAction> {

    public DelayTimeWord(List<DelayTimedAction> timedActions) {
        super(timedActions);
    }

    public static DelayTimeWord emptyWord() {
        return new DelayTimeWord(new ArrayList<DelayTimedAction>());
    }

    @Override
    public DelayTimeWord subWord(int fromIndex, int toIndex) {
        try {
            List<DelayTimedAction> subList = getTimedActions().subList(fromIndex, toIndex);
            return new DelayTimeWord(subList);
        } catch (Exception e) {
            return emptyWord();
        }
    }
    //    public String toString(){
//        StringBuilder sb = new StringBuilder();
//        for (DelayTimedAction timedAction : timedActions){
//            sb.append(timedAction);
//        }
//        return sb.toString();
//    }
    @Override
    public DelayTimeWord concat(DelayTimedAction timedAction) {
        List<DelayTimedAction> timedActions1 = new ArrayList<>();
        timedActions1.add(timedAction);
        return new DelayTimeWord(timedActions1);
    }

}
