package ta.twoClockTA;

import ta.Clock;
import ta.twoClockTA.Region;
import java.util.Map;

public class RegionTwoClockLogicAction{

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
    public void setSymbol(String symbol){
        this.symbol=symbol;
    }
    public void setRegion(Region region){
        this.region=region;
    }

//    public double getValue(Clock clock) {
//        return clockValueMap.get(clock);
//    }
//    public double getValueClock(int i){
//        return clockValueMap.get(i).doubleValue();
//    }

}
