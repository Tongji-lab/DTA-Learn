package classificationTree.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import timedWord.TimedWord;
import timedWord.TwoClockTimedWord;
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class Node<T extends TwoClockTimedWord> {
    private T word;

    public boolean isLeaf(){
        return this instanceof LeafNode;
    }

    public boolean isInnerNode(){
        return this instanceof InnerNode;
    }
}
