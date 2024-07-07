package frame;

import timedWord.TimedWord;

public interface Membership<T extends TimedWord, R> {
    R answer(T timedWord);

    int getCount();
}
