package ta.twoClockTA;

import ta.Clock;
import ta.twoClockTA.Region;
import timedAction.TwoClockTimedAction;

import java.util.Map;

public class RegionTwoClockLogicAction extends TwoClockTimedAction {
    private String symbol;
    private Region region;
    public RegionTwoClockLogicAction(String symbol,Region region){
        this.symbol=symbol;
        this.region=region;
    }
    public String getSymbol(){
        return symbol;
    }
    public Region getRegion(){
        return region;
    }
//    public double getValue(Clock clock) {
//        return clockValueMap.get(clock);
//    }
//    public double getValueClock(int i){
//        return clockValueMap.get(i).doubleValue();
//    }

}
