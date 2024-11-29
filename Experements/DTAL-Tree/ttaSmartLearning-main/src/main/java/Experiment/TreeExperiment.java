package Experiment;
import classificationTree.ClassificationTree;
import dbm.ActionGuard;
import dbm.DBMUtil;
import dbm.TransitionState;
import dbm.Value;
import frame.EquivalenceQuery;
import lombok.Data;
import ta.*;
//import ta.ota.DOTA;
import ta.ota.LogicTimeWord;
import ta.ota.OTATranComparator;
import ta.twoClockTA.TwoClockLogicTimeWord;
import ta.ota.LogicTimedAction;
import ta.twoClockTA.TwoClockLogicAction;
import ta.twoClockTA.TwoClockResetLogicAction;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
import ta.twoClockTA.TwoClockTA;
import ta.twoClockTA.TwoClockTA;
import defaultTeacher.DefaultEquivalenceQuery;
import defaultTeacher.DefaultTeacher;
import ta.TimeGuard;
import ta.ota.DOTA;
import ta.ota.DOTAUtil;
import ta.twoClockTA.TwoClockTA;
import java.io.IOException;
import ta.twoClockTA.*;
import ta.Clock;

import java.math.BigDecimal;
import java.util.*;
import ta.twoClockTA.TwoClockTAUtil;
import timedAction.DelayTimedAction;
import timedWord.DelayTimeWord;

public class TreeExperiment {
    public static void main(String[] args) throws IOException {

        int counter = 0;

        List<TwoClockTA> twoClockTAS = new ArrayList<>();

            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-1.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-2.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-3.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-4.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-5.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-6.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-7.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-8.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-9.json"));
//            twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-10.json"));
////
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-10.json"));
////
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-10.json"));
////
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-10.json"));
////
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-10.json"));
//
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-10.json"));
//
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-10.json"));
//
//
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-10.json"));
//
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-10.json"));
//
//
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-1.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-2.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-3.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-4.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-5.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-6.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-7.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-8.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-9.json"));
//        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-10.json"));


        for (TwoClockTA twoclockta : twoClockTAS) {
            counter=0;
            System.out.println("Target DTA:" + twoclockta.getLocations());
            for (int i = 0; i < twoclockta.getTransitions().size(); i++) {
                System.out.println(twoclockta.getTransitions().get(i));
            }
            DefaultEquivalenceQuery.completeTwoClock(twoclockta);
            System.out.println("Complete Target DTA:" + twoclockta.getLocations());
            for (int i = 0; i < twoclockta.getTransitions().size(); i++) {
                System.out.println(twoclockta.getTransitions().get(i));
            }
            DefaultTeacher teacher = new DefaultTeacher(twoclockta);
            int k = twoclockta.getK();
            ClassificationTree classificationTree = new ClassificationTree("h", twoclockta.getSigma(), teacher, twoclockta.getClockList(), k);
            // TwoClockObservationTable observationTable = new TwoClockObservationTable("h", twoclockta.getSigma(), teacher);

            long start = System.currentTimeMillis();
            //自定义学习流程
            //1、观察表初始化
            classificationTree.init();

            //2、开始学习
            classificationTree.learn();
            classificationTree.show();

            //3、生成假设
            TwoClockTA hypothesis = classificationTree.buildHypothesis();
            System.out.println(hypothesis);
            //4、等价判断
            TwoClockResetLogicTimeWord ce = null;
            ce = teacher.equivalence(hypothesis);
            counter++;
            boolean reuse = false;
            while (null != ce) {
                for (int i = 0; i < twoclockta.getTransitions().size(); i++) {
                    System.out.println(twoclockta.getTransitions().get(i));
                }
                //System.out.println("ctx:"+ce.toString());
                classificationTree.refine(ce, reuse);
                // System.out.println("refine结束");
                classificationTree.show();
                hypothesis = classificationTree.buildHypothesis();

                List<DelayTimedAction> delayTimedActions = new ArrayList<>();
                for (Clock clock : twoclockta.getClockList()) {
                    BigDecimal b3 = new BigDecimal(Double.toString(ce.getTimedActions().get(0).getValue(clock)));
                    BigDecimal b4 = new BigDecimal(Double.toString(ce.getTimedActions().get(0).getValue(twoclockta.getClockList().get(0))));
                }
                DelayTimedAction delayTimedAction = new DelayTimedAction(ce.getTimedActions().get(0).getSymbol(), ce.getTimedActions().get(0).getValue(twoclockta.getClockList().get(0)));
                delayTimedActions.add(delayTimedAction);
                for (int i1 = 0; i1 < ce.getTimedActions().size() - 1; i1++) {
                    double defference;

                    BigDecimal b1 = new BigDecimal(Double.toString(ce.getTimedActions().get(i1).getValue(twoclockta.getClockList().get(0))));
                    BigDecimal b2 = new BigDecimal(Double.toString(ce.getTimedActions().get(i1 + 1).getValue(twoclockta.getClockList().get(0))));
                    if (ce.getTimedActions().get(i1).isReset(twoclockta.getClockList().get(0))) {
                        defference = ce.getTimedActions().get(i1 + 1).getValue(twoclockta.getClockList().get(0));
                    } else {
                        defference = b2.subtract(b1).doubleValue();
                    }

                    for (int m = 1; m < twoclockta.getClockList().size(); m++) {
                        double defferenceeach;

                        BigDecimal b3 = new BigDecimal(Double.toString(ce.getTimedActions().get(i1).getValue(twoclockta.getClockList().get(m))));
                        BigDecimal b4 = new BigDecimal(Double.toString(ce.getTimedActions().get(i1 + 1).getValue(twoclockta.getClockList().get(m))));
                        if (ce.getTimedActions().get(i1).isReset(twoclockta.getClockList().get(m))) {
                            defferenceeach = ce.getTimedActions().get(i1 + 1).getValue(twoclockta.getClockList().get(m));
                        } else {
                            defferenceeach = b4.subtract(b3).doubleValue();
                        }
                        BigDecimal b5 = new BigDecimal(Double.toString(defference));
                        BigDecimal b6 = new BigDecimal(Double.toString(defferenceeach));
                    }
                    DelayTimedAction delayTimedAction2 = new DelayTimedAction(ce.getTimedActions().get(i1 + 1).getSymbol(), defference);
                    delayTimedActions.add(delayTimedAction2);
                }
                DelayTimeWord twoClockTimedWord = new DelayTimeWord(delayTimedActions);
                BooleanAnswer answer1 = twoclockta.reach(twoClockTimedWord);
                BooleanAnswer answer2 = hypothesis.reach(twoClockTimedWord);
                if (answer1.isAccept() != answer2.isAccept()) {
                    System.out.println("Equivalence result:"+ce);
                    reuse = true;
                    System.out.println("hypothesis location:" + hypothesis.getLocations().toString());
                    System.out.println("hypothesis transition:");
                    for (int i = 0; i < hypothesis.getTransitions().size(); i++) {
                        System.out.println(hypothesis.getTransitions().get(i).toString());
                    }

                } else {
                    ce = teacher.equivalence(hypothesis);
                    counter++;
                    reuse = false;
                }
                //  System.out.println(hypothesis);
            }

            long end = System.currentTimeMillis();
            System.out.println("#Membership:" + teacher.getCountmember());
            System.out.println("#M_opt:" + classificationTree.getMembertable());
            System.out.println("#M_ctx:" + classificationTree.getMemberctx());
            System.out.println("#Reset:" + teacher.getCounttran());
            System.out.println("#Equivalence:" + counter);
            System.out.println("Time:" + (end - start) + " ms");
            System.out.println("study complete");

        }
    }

}
