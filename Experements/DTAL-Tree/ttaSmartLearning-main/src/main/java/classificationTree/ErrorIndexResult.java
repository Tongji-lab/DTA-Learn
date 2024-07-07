package classificationTree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorIndexResult {
    private int index;
    private ErrorEnum errorEnum;
}
