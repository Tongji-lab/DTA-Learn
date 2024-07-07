package frame;

import ta.TA;
import timedWord.TimedWord;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
//public interface EquivalenceQuery<T extends TimedWord, R extends TA> {
public interface EquivalenceQuery<T extends TwoClockResetLogicTimeWord, R extends TA> {
    T findCounterExample(R hypothesis);

    int getCount();
}
