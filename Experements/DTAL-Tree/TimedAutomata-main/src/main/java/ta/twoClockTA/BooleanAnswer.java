package ta.twoClockTA;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BooleanAnswer implements Answer {
    private List<Resets> resets;
    private boolean accept;

}
