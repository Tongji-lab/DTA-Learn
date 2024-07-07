package timedWord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import timedAction.TimedAction;

import java.util.*;

@AllArgsConstructor
public abstract class TimedWord<T extends TimedAction> {

    @Getter
    private List<T> timedActions;

    public int size(){
        return timedActions.size();
    }

    public T get(int i){
        return timedActions.get(i);
    }

    public boolean isEmpty(){
        return timedActions.isEmpty();
    }

    public abstract TimedWord subWord( int fromIndex, int toIndex);

    public abstract TimedWord concat(T timedAction);



    @Override
    public String toString(){
        if (isEmpty()){
            return "empty";
        }
        StringBuilder sb = new StringBuilder();
        for (TimedAction timedAction : timedActions){
            sb.append(timedAction);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimedWord)) return false;
        TimedWord<?> timedWord = (TimedWord<?>) o;
        return getTimedActions().equals(timedWord.getTimedActions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimedActions());
    }
}
