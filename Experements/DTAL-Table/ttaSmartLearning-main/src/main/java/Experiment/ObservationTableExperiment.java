package Experiment;
import ta.*;
import ta.twoClockTA.TwoClockResetLogicTimeWord;
import ta.twoClockTA.TwoClockTA;
import defaultTeacher.DefaultEquivalenceQuery;
import defaultTeacher.DefaultTeacher;
import observationTable.TwoClockObservationTable;
import ta.TimeGuard;
import java.io.IOException;
import ta.Clock;
import ta.twoClockTA.TwoClockTAUtil;

import java.util.*;
public class ObservationTableExperiment {
    public static void main(String[] args) throws IOException {

        List<TwoClockTA> twoClockTAS=new ArrayList<>();

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\6_1_8_2\\6_1_8_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_1_8_2\\8_1_8_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_8_2\\8_2_8_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_2_16_2\\8_2_16_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\8_3_8_2\\8_3_8_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_8_2\\10_2_8_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_2_16_2\\10_2_16_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\10_3_8_2\\10_3_8_2-10.json"));

        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\12_2_8_2\\12_2_8_2-10.json"));


        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-1.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-2.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-3.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-4.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-5.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-6.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-7.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-8.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-9.json"));
        twoClockTAS.add(LoadSMTDataFromJson.load("..\\..\\..\\jsons\\14_2_8_2\\14_2_8_2-10.json"));
//
        for (TwoClockTA twoclockta:twoClockTAS) {
            int counter=0;
            System.out.println("Target DTA:" +twoclockta.getName()+ twoclockta.getLocations());
            for (int i = 0; i < twoclockta.getTransitions().size(); i++) {
                System.out.println(twoclockta.getTransitions().get(i));
            }
            DefaultEquivalenceQuery.completeTwoClock(twoclockta);
            System.out.println("Complete Target DTA:"+twoclockta.getName() + twoclockta.getLocations());
            for (int i = 0; i < twoclockta.getTransitions().size(); i++) {
                System.out.println(twoclockta.getTransitions().get(i));
            }
            DefaultTeacher teacher = new DefaultTeacher(twoclockta);
            int k = twoclockta.getK();

            TwoClockObservationTable observationTable = new TwoClockObservationTable("table", twoclockta.getSigma(), teacher, twoclockta.getClockList(), k);

            long start = System.currentTimeMillis();
            //自定义学习流程
            //1、观察表初始化
            observationTable.init();
//        observationTable.show();
            //2、开始学习
            observationTable.learn();
            observationTable.show();

            //3、生成假设
            TwoClockTA hypothesis = observationTable.buildHypothesis();
            //4、等价判断
            TwoClockResetLogicTimeWord ce = null;
            while (null != (ce = teacher.equivalence(hypothesis))) {
//                if(oldce.getTimedActions().equals(ce.getTimedActions())){
//                    System.out.println("study failed");
//                    break;
//                }
                //oldce.setTimedActions(ce.getTimedActions());
                counter++;
                //System.out.println("ctx:"+ce.toString());
                observationTable.refine(ce);
                // System.out.println("refine结束");
                //  observationTable.show();

                hypothesis = observationTable.buildHypothesis();
                //  System.out.println(hypothesis);
            }
            counter=counter+1;
            System.out.println("hypothesis location:" + hypothesis.getLocations().toString());
            System.out.println("hypothesis transition:");
            for (int i = 0; i < hypothesis.getTransitions().size(); i++) {
                System.out.println(hypothesis.getTransitions().get(i).toString());
            }
            long end = System.currentTimeMillis();
            System.out.println("#Membership:" + teacher.getCountmember());
            System.out.println("#M_opt:" + observationTable.getMembertable());
            System.out.println("#M_ctx:" + observationTable.getMemberctx());
            System.out.println("#Reset:" + teacher.getCounttran());
            System.out.println("#Equivalence:" + counter);
            System.out.println("Time:" + (end - start) + " ms");
            System.out.println("study complete");

        }
    }

}
