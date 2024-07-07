package classificationTree.node;

import lombok.Data;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
@Data
public class LeafNode extends Node<TwoClockResetLogicTimeWord>{
    private boolean init;
    private boolean accpted;
    private InnerNode preNode;

    public LeafNode(TwoClockResetLogicTimeWord word) {
        super(word);
    }
public TwoClockResetLogicTimeWord getword(){
        return super.getWord();
}
    public LeafNode(TwoClockResetLogicTimeWord word, boolean init, boolean accpted) {
        super(word);
        this.init = init;
        this.accpted = accpted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeafNode)) return false;
        if (!super.equals(o)) return false;

        LeafNode leafNode = (LeafNode) o;

        if (init != leafNode.init) return false;
        return accpted == leafNode.accpted;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (init ? 1 : 0);
        result = 31 * result + (accpted ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
