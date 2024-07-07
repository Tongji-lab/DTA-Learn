package defaultTeacher;
import ta.Clock;
import frame.Teacher;
import lombok.Data;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
import timedWord.TimedWord;
import ta.twoClockTA.TwoClockTA;
import java.util.HashMap;
import java.util.Map;
import ta.twoClockTA.*;
import frame.TwoClockTeacher;
@Data
public class DefaultTeacher implements TwoClockTeacher<TwoClockResetLogicTimeWord, TwoClockResetLogicTimeWord, TwoClockTA, TwoClockLogicTimeWord> {
    public TwoClockTA dtta;
    private DefaultTTAMembership membership;
    private DefaultEquivalenceQuery equivalenceQuery;
    private Map<TwoClockLogicTimeWord, TwoClockResetLogicTimeWord> map = new HashMap<>();
    private Map<TwoClockResetLogicTimeWord, BooleanAnswer> map1 = new HashMap<>();
    int countmember;
    int counttran;
    public DefaultTeacher(TwoClockTA dtta) {
        this.dtta = dtta;
        membership = new DefaultTTAMembership(dtta);
        equivalenceQuery = new DefaultEquivalenceQuery(dtta);
    }

    @Override
    public boolean membership(TwoClockResetLogicTimeWord timedWord,Integer cs) {
        countmember=countmember+1;
        return membership.answer(timedWord,cs);
    }

    @Override
    public TwoClockResetLogicTimeWord equivalence(TwoClockTA hypothesis) {
        System.out.println("countmember:"+countmember);
        System.out.println("counttran:"+counttran);
        return equivalenceQuery.findCounterExample(hypothesis);
    }

    @Override
    public TwoClockResetLogicTimeWord transferWord(TwoClockTA ta,TwoClockLogicTimeWord timeWord) {
        TwoClockResetLogicTimeWord resetLogicTimeWord = ta.transferResetbyteacher(timeWord);
        map.put(timeWord,resetLogicTimeWord);
        return resetLogicTimeWord;
    }
    public TwoClockResetLogicTimeWord transferWord(TwoClockLogicTimeWord timeWord) {
        counttran=counttran+1;
        System.out.println(timeWord);
        TwoClockResetLogicTimeWord resetLogicTimeWord = dtta.transferResetbyteacher(timeWord);
        map.put(timeWord,resetLogicTimeWord);
        return resetLogicTimeWord;
    }



}
