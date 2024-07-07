package ta.ota;

import lombok.AllArgsConstructor;
import lombok.Data;
import ta.Clock;
import ta.TaTransition;

import java.util.Comparator;

@Data
@AllArgsConstructor
public class OTATranComparator implements Comparator<TaTransition> {

    private Clock clock;

    @Override
    public int compare(TaTransition o1, TaTransition o2) {
        int var1 = o1.getSourceId().compareTo(o2.getSourceId());
        if (var1 != 0){
            return var1;
        }
        int var2 = o1.getSymbol().compareTo(o2.getSymbol());
        if(var2 != 0){
            return var2;
        }
        int var3 = o1.getLowerBound(clock) - o2.getLowerBound(clock);
        if(var3 !=0){
            return var3;
        }
        int var4 = o1.getUpperBound(clock) - o2.getUpperBound(clock);
        if(var4 != 0){
            return var4;
        }
        return -1;
    }
}
