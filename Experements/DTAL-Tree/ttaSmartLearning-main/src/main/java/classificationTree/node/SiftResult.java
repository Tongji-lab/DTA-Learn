package classificationTree.node;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiftResult {
    private LeafNode leafNode;
    private boolean completeOperation;
}
