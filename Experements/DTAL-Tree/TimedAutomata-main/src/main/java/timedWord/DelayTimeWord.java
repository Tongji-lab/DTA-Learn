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

    @Override
    public DelayTimeWord concat(DelayTimedAction timedAction) {
        List<DelayTimedAction> timedActions1 = new ArrayList<>();
        timedActions1.add(timedAction);
        return new DelayTimeWord(timedActions1);
    }

}
