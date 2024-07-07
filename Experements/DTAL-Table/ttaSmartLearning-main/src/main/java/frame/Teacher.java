package frame;

import ta.TA;
import timedWord.TimedWord;

public interface Teacher<T extends TimedWord, R extends TimedWord, M extends TA, W extends TimedWord> {
    boolean membership(T timedWord);
    R equivalence(M hypothesis);
    T transferWord(W timeWord);
}
