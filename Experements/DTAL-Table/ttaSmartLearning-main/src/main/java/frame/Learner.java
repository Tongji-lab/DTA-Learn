package frame;

import ta.TA;
import ta.ota.DOTA;
import timedWord.TimedWord;

public interface Learner<T extends TimedWord> {
    //生命周期方法

    //初始化
    void init();

    //学习
    void learn();

    //对反例进行处理
    void refine(T counterExample);

    boolean check(T counterExample);

    //构造假设自动机
    DOTA buildHypothesis();

    //获取最终结果自动机
    DOTA getFinalHypothesis();

    void show();

}
