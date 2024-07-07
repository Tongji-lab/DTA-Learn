package frame;

import ta.twoClockTA.BooleanAnswer;
import timedWord.TwoClockTimedWord;

import ta.Clock;

public interface TwoClockMembership<T extends TwoClockTimedWord> {
    boolean answer(T timedWord,Integer cs);

    int getCount();
}
