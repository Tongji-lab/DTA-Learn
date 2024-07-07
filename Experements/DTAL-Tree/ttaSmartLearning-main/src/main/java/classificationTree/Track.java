package classificationTree;


import classificationTree.node.LeafNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import ta.twoClockTA.TwoClockResetLogicAction;
@Data
@AllArgsConstructor
public class Track {

    private LeafNode source;
    private LeafNode target;
    private TwoClockResetLogicAction action;

    @Override
    public int hashCode(){
        return source.hashCode()+action.hashCode();
    }

    @Override
    public boolean equals(Object o){
        Track guard = (Track)o;
        boolean var1 = source.equals(guard.source);
        boolean var3 = action.equals(guard.action);
        return var1 && var3;
    }

    @Override
    public String toString() {
        return "Track{" +
                "source=" + source +
                ", target=" + target +
                ", word=" + action +
                '}';
    }
}
