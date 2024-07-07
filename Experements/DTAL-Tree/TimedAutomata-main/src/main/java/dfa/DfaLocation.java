package dfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DfaLocation {
    private String id;
    private String name;
    private boolean init;
    private boolean accept;

    @Override
    public String toString() {
        return "{" +
                 name +
                '}';
    }
}
