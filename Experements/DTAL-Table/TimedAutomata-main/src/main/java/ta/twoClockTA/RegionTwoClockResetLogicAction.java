package ta.twoClockTA;

import ta.Clock;

import java.util.Map;
import java.util.Set;

public class RegionTwoClockResetLogicAction {

    private String symbol;
    private Set<Clock> resetClockSet;
    private Region region;

    public boolean isReset(Clock clock){
        return resetClockSet.contains(clock);
    }

    public Region getRegion() {
        return region;
    }

    public String getSymbol() {
        return symbol;
    }
    public Set<Clock> getResetClockSet() {
        return resetClockSet;
    }
    public RegionTwoClockResetLogicAction(String symbol,Set<Clock> resetClockSet,Region region){
        this.region=region;
        this.symbol=symbol;
        this.resetClockSet=resetClockSet;

    }

}
