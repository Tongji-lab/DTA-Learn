package classificationTree.node;

import lombok.Data;
import ta.twoClockTA.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ta.twoClockTA.RegionTwoClockLogicTimedWord;

@Data
public class InnerNode extends Node<RegionTwoClockLogicTimedWord> {
    private InnerNode preNode;
    private Map<BooleanAnswer, Node> keyChildMap = new HashMap<>();

    public InnerNode(RegionTwoClockLogicTimedWord word) {
        super(word);
    }
    public void add(BooleanAnswer key, Node node) {
        keyChildMap.put(key, node);
    }
    public Node getChild(BooleanAnswer key) {
        Node node = keyChildMap.get(key);
        return node;
    }
    public List<Node> getChildList() {
        return new ArrayList<>(keyChildMap.values());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
