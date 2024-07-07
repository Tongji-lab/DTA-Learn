package timedWord;

import timedAction.ResetDelayAction;

import java.util.ArrayList;
import java.util.List;

public class ResetDelayTimeWord extends TimedWord<ResetDelayAction>{

    public ResetDelayTimeWord() {
        super(new ArrayList<ResetDelayAction>());
    }

    public ResetDelayTimeWord(List<ResetDelayAction> timedActions) {
        super(timedActions);
    }

    public static ResetDelayTimeWord emptyWord() {
        return new ResetDelayTimeWord();
    }

    @Override
    public ResetDelayTimeWord subWord(int fromIndex, int toIndex) {
        try{
            List<ResetDelayAction> subList = getTimedActions().subList(fromIndex,toIndex);
            return new ResetDelayTimeWord(subList);
        }catch (Exception e){
            return emptyWord();
        }
    }

    @Override
    public ResetDelayTimeWord concat(ResetDelayAction timedAction){
        List<ResetDelayAction> timedActions1 = new ArrayList<>();
        timedActions1.add(timedAction);
        return new ResetDelayTimeWord(timedActions1);
    }


}
