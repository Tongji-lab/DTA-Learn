package frame;

import ta.ota.DOTA;
import ta.twoClockTA.TwoClockTA;
import timedWord.TimedWord;
import timedWord.TwoClockTimedWord;

public interface TwoClockLearner<T extends TwoClockTimedWord> {
    //生命周期方法

    //初始化
    void init();

    //学习
    void learn();

    //对反例进行处理
    void refine(T counterExample);

    boolean check(T counterExample);

    //构造假设自动机
    TwoClockTA buildHypothesis();

    //获取最终结果自动机
    TwoClockTA getFinalHypothesis();

    void show();

}
