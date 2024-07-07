package defaultTeacher;

import frame.Membership;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.Clock;
import ta.TaLocation;
import ta.ota.DOTA;
import ta.twoClockTA.BooleanAnswer;
import ta.twoClockTA.Resets;
import ta.twoClockTA.TwoClockTA;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
import frame.TwoClockMembership;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DefaultTTAMembership implements TwoClockMembership<TwoClockResetLogicTimeWord>
{
    private TwoClockTA dtta;
    private int count;

    public DefaultTTAMembership(TwoClockTA dtta) {
        this.dtta = dtta;
    }

    @Override
    public boolean answer(TwoClockResetLogicTimeWord timedWord,Integer csize) {
        count++;
        BooleanAnswer booleanAnswer;
        if(timedWord==null||timedWord.getTimedActions()==null||timedWord.getTimedActions().size()==0){
            return dtta.getInitLocation().isAccept();
        }
        else {
            booleanAnswer = dtta.reach(timedWord);
            if (booleanAnswer == null) {
                return false;
            }
            else {
                return booleanAnswer.isAccept();
            }
        }
    }

    @Override
    public int getCount() {
        return count;
    }
}