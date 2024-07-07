package dbm;

import lombok.Data;
import ta.TaLocation;

@Data
public class LocationState extends BaseState {
    private TaLocation location;
    private TransitionState preState;

    public LocationState(DBM dbm, TaLocation location) {
        super(dbm);
        this.location = location;
    }

    public LocationState(DBM dbm, TaLocation location, TransitionState preState) {
        super(dbm);
        this.location = location;
        this.preState = preState;
    }

    public boolean include(LocationState state){
        if(state.getLocation() != getLocation()){
            return false;
        }
        if(!getDbm().include(state.getDbm())){
            return false;
        }
        return true;
    }
}
