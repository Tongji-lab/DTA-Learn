package ta.ota;

import timedWord.TimedWord;

import java.util.ArrayList;
import java.util.List;

//只针对OTA
public class LogicTimeWord extends TimedWord<LogicTimedAction> {

    public LogicTimeWord(List<LogicTimedAction> timedActions) {
        super(timedActions);
    }

    public LogicTimeWord concat(LogicTimeWord logicTimeWord){
        List<LogicTimedAction> logicActionList = new ArrayList<>();
        logicActionList.addAll(getTimedActions());
        logicActionList.addAll(logicTimeWord.getTimedActions());
        return new LogicTimeWord(logicActionList);
    }

    public static LogicTimeWord emptyWord() {
        return new LogicTimeWord(new ArrayList<LogicTimedAction>());
    }

    @Override
    public LogicTimeWord subWord(int fromIndex, int toIndex) {
        try{
            List<LogicTimedAction> subList = getTimedActions().subList(fromIndex,toIndex);
            return new LogicTimeWord(subList);
        }catch (Exception e){
            return emptyWord();
        }
    }

    @Override
    public LogicTimeWord concat(LogicTimedAction timedAction){
        List<LogicTimedAction> timedActions1 = new ArrayList<>();
        timedActions1.addAll(getTimedActions());
        timedActions1.add(timedAction);
        return new LogicTimeWord(timedActions1);
    }
}
