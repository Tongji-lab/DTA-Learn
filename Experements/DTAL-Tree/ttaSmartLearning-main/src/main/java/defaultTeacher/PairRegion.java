package defaultTeacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import frame.Pair;
import ta.twoClockTA.RegionTwoClockLogicTimedWord;
import ta.twoClockTA.TwoClockLogicTimeWord;
import ta.twoClockTA.TwoClockLogicTimeWordTwoClockLogicTimeWord;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
@Data
@AllArgsConstructor
public class PairRegion implements Pair {
    public TwoClockResetLogicTimeWord prefix;//前缀是逻辑的，之后要改成delay
    public RegionTwoClockLogicTimedWord suffix;
    public TwoClockLogicTimeWordTwoClockLogicTimeWord timeWord() {
        TwoClockLogicTimeWord pre = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
        return RegionTwoClockLogicTimedWord.concatRegion(pre,suffix);
    }
//    public PairRegion(TwoClockResetLogicTimeWord prefix,RegionTwoClockLogicTimedWord suffix){
//        this.prefix=prefix;
//        this.suffix=suffix;
//    }

    public TwoClockResetLogicTimeWord getPrefix(){
        return this.prefix;
    }
    public RegionTwoClockLogicTimedWord getSuffix(){
        return this.suffix;
    }
}
