package frame;

import ta.TA;
import ta.twoClockTA.BooleanAnswer;
import timedWord.TwoClockTimedWord;
public interface TwoClockTeacher<T extends TwoClockTimedWord, R extends TwoClockTimedWord, M extends TA, W extends TwoClockTimedWord> {
    boolean membership(T timedWord,Integer cs);
    R equivalence(M hypothesis);
    T transferWord(M ta,W timeWord);
    T transferWord(W timeWord);
}
