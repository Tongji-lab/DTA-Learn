package ta;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaLocation implements Cloneable{
    private String id;
    private String name;
    private boolean init;
    private boolean accept;

    public TaLocation(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public TaLocation copy() {
        return new TaLocation(id,name,init,accept);
    }

}
