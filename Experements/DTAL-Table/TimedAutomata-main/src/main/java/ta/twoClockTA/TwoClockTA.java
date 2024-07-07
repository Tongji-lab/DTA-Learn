package ta.twoClockTA;

import lombok.Data;
import ta.*;
import ta.ota.*;
import timedAction.DelayTimedAction;
import timedWord.DelayTimeWord;

import java.math.*;
import java.util.*;

/**
 * TwoClockTA 是TA的一个子集，具有两个时钟
 */
@Data
public class TwoClockTA extends TA {

    public TwoClockTA(String name, List<Clock> clockList, Set<String> sigma, List<TaLocation> locations, List<TaTransition> transitions) {
        super(name, clockList, sigma, locations, transitions);
    }
//    public int[]  getK(){//find max constraint in TA
//        int[] K=new int[getClockList().size()];
//
//        for (int i=0;i<getClockList().size();i++){
//            for (TaTransition transition:getTransitions()) {
//                if(transition.getUpperBound(getClockList().get(i))<TimeGuard.MAX_TIME&&transition.getUpperBound(getClockList().get(i))>K[i]){
//                    K[i]=transition.getUpperBound(getClockList().get(i));
//                }
//                else if (transition.getLowerBound(getClockList().get(i)) > K[i]) {
//                    K[i] = transition.getLowerBound(getClockList().get(i));
//                }
//            }
//        }
//        return K;
//    }

    public int  getK(){
        int K=0;
        for (TaTransition transition:getTransitions()) {
            for (Clock clock : getClockList()) {
                if(transition.getUpperBound(clock)<TimeGuard.MAX_TIME&&transition.getUpperBound(clock)>K){
                    K=transition.getUpperBound(clock);
                }
                else if (transition.getLowerBound(clock) > K) {
                    K = transition.getLowerBound(clock);
                }
            }
        }
        return K;
    }
    //给定一个重置逻辑时间字，TwoCLockTA最多一个路径
    public BooleanAnswer reach(DelayTimeWord resetLogicTimeWord) {

        List<Resets> resets=new ArrayList<>();
        TaLocation location = getInitLocation();
        //存储当前时钟值,初始化0
        double value = 0.0;
        double[] difference=new double[this.getClockList().size()];

        List<DelayTimedAction> actions = resetLogicTimeWord.getTimedActions();
        int bigger=0;
        flag:
        for (DelayTimedAction action : actions) {
            //     System.out.println(action.toString());
//            System.out.println(clock1.toString());
//            for (int i1=0;i1<difference.length;i1++){
//                System.out.println("difference:"+difference[i1]);
//            }
            Map<Clock,Double> map=new HashMap<>();
            for (int i1=0;i1<this.getClockList().size();i1++){
                BigDecimal b1 = new BigDecimal(Double.toString(difference[i1]));
                BigDecimal b2 = new BigDecimal(Double.toString(action.getValue()));
                map.put(this.getClockList().get(i1),b1.add(b2).doubleValue());
            }
            TwoClockLogicAction logicTimedAction=new TwoClockLogicAction(action.getSymbol(),map);
            //   System.out.println("map:"+map);

            List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
            for (TaTransition transition : transitions) {
                //   System.out.println("transition:"+transition.toString());
//                for (int i=0;i<this.getClockList().size();i++){
//                    if(transition.isReset(this.getClockList().get(i))!=action.isReset(this.getClockList().get(i))){
//                        continue ;
//                    }
//                }
//                if (transition.isReset(clock1) != action.isReset(hc1)) {
//                    continue;
//                }
//                if (transition.isReset(clock2) != action.isReset(hc2)) {
//                    continue;
//                }
//                TimeGuard timeGuard1 = transition.getTimeGuard(clock1);
//                TimeGuard timeGuard2 = transition.getTimeGuard(clock2);
                List<TimeGuard> timeGuardList=new ArrayList<>();
                for (int i=0;i<this.getClockList().size();i++) {
                    TimeGuard timeGuard1 = transition.getTimeGuard(this.getClockList().get(i));
                    timeGuardList.add(timeGuard1);
                }


//                if(timeGuard1.isPass(action.getValue(hc1))
//                        && timeGuard2.isPass(action.getValue(hc2))){
//                //   System.out.println("guard通过");
//                }
                //   System.out.println("bigger:"+bigger+"difference:"+difference+"差值:"+(action.getValue(hc1)-action.getValue(hc2))+(((int)(action.getValue(hc1)*10)-(int)(action.getValue(hc2)*10)))/10.0);

                List<BigDecimal> bigDecimalList=new ArrayList<>();
                for (int i=0;i<this.getClockList().size();i++){
                    BigDecimal b1 = new BigDecimal(Double.toString(logicTimedAction.getValue(this.getClockList().get(i))));
                    bigDecimalList.add(b1);
                }

                boolean inscope=true;
                for (int i=0;i<this.getClockList().size();i++){
                    if(timeGuardList.get(i).isPass(logicTimedAction.getValue(this.getClockList().get(i)))){

                    }
                    else {
                        inscope=false;
                    }
                }
                boolean eqdiff=true;
                for (int i=0;i<this.getClockList().size();i++){
                    for (int j=i+1;j<this.getClockList().size();j++){
                        BigDecimal b1 = new BigDecimal(Double.toString(difference[i]));
                        BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));
                        if(Double.doubleToLongBits(bigDecimalList.get(i).subtract(bigDecimalList.get(j)).doubleValue())==Double.doubleToLongBits(b1.subtract(b2).doubleValue())){

                        }
                        else {
                            eqdiff=false;
                            break;
                        }

                    }
                }


                if ((inscope && eqdiff)) {

                    //       System.out.println("inscope && eqdiff");
//
//                BigDecimal b1 = new BigDecimal(Double.toString(action.getValue(hc1)));
//                BigDecimal b2 = new BigDecimal(Double.toString(action.getValue(hc2)));
//                if ((timeGuard1.isPass(action.getValue(hc1))
//                        && timeGuard2.isPass(action.getValue(hc2))&&bigger==1&&
//                        (b1.subtract(b2).doubleValue()*100==difference*100))||
//                        (timeGuard1.isPass(action.getValue(hc1))
//                                && timeGuard2.isPass(action.getValue(hc2))&&bigger==2&&
//                                (b2.subtract(b1).doubleValue()*100==difference*100))||
//                        (timeGuard1.isPass(action.getValue(hc1))
//                                && timeGuard2.isPass(action.getValue(hc2))&&bigger==0&&
//                                (action.getValue(hc1)==action.getValue(hc2)))) {
//                     if(!transition.getEdgeTimeGuard().getLargerclock()||transition.getEdgeTimeGuard().getLargerclock()&& transition.getEdgeTimeGuard().isHavelowerdiff()
//                      &&!transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()
//                     ||transition.getEdgeTimeGuard().getLargerclock()&& transition.getEdgeTimeGuard().isHavelowerdiff()&&!transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                             b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                             ||transition.getEdgeTimeGuard().getLargerclock()&& !transition.getEdgeTimeGuard().isHavelowerdiff()
//                             &&transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()
//                             ||transition.getEdgeTimeGuard().getLargerclock()&& !transition.getEdgeTimeGuard().isHavelowerdiff()
//                             &&transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()
//                             ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                             b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()&&
//                             b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                             ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                             b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()&&
//                             b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                             ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                             b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()&&
//                             b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()
//                             ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                             b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()&&
//                             b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()){
                    //   System.out.println("有后继");
                    //   System.out.println("difference:"+difference);
                    location = transition.getTargetLocation();
                    boolean[] actualResets=new boolean[this.getClockList().size()];
                    for (int i=0;i<this.getClockList().size();i++){
                        actualResets[i]=transition.isReset(this.getClockList().get(i));
                    }

//                    boolean[] actualResets = {transition.isReset(clock1), transition.isReset(clock2)};
                    Resets resets1 = new Resets(actualResets);
                    resets.add(resets1);
//                    if (transition.isReset(clock1) == true && transition.isReset(clock2) == true) {
//                        difference = 0;
//                        bigger = 0;
//                    }
//                    if (transition.isReset(clock1) == true && transition.isReset(clock2) == false) {
////                        if (bigger==2||bigger==0){
////                        difference=difference+action.getValue(hc2);
////                        bigger=2;}
////                        else {
//                        difference = action.getValue(hc2);
//                        bigger = 2;
//                        //  }
//                    }
//                    if (transition.isReset(clock1) == false && transition.isReset(clock2) == true) {
////                        if (bigger==1||bigger==0) {
////                            difference = difference + action.getValue(hc1);
////                            bigger = 1;
////                        }
////                        else {
//                        difference = action.getValue(hc1);
//                        bigger = 1;
//                        //   }
//                    }
//                    if (transition.isReset(clock1) == false && transition.isReset(clock2) == false) {
//
//                    }
                    for (int i=0;i<this.getClockList().size();i++){
                        if (!transition.isReset(this.getClockList().get(i))){
                            difference[i]=logicTimedAction.getValue(this.getClockList().get(i));
                        }
                        else {
                            difference[i]=0;
                        }

                    }
                    continue flag;
                }

            }
            return null;
        }
        BooleanAnswer booleanAnswer=new BooleanAnswer(resets,location.isAccept());

        return booleanAnswer;
    }

    public BooleanAnswer reach(TwoClockResetLogicTimeWord resetLogicTimeWord) {
        List<Resets> resets=new ArrayList<>();
        TaLocation location = getInitLocation();
        double[] difference=new double[this.getClockList().size()];
        List<TwoClockResetLogicAction> actions = resetLogicTimeWord.getTimedActions();
        flag:
        for (TwoClockResetLogicAction action : actions) {
            List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
            for (TaTransition transition : transitions) {
                for (int i=0;i<this.getClockList().size();i++){
                    if(transition.isReset(this.getClockList().get(i))!=action.isReset(this.getClockList().get(i))){
                        continue ;
                    }
                }
                List<TimeGuard> timeGuardList=new ArrayList<>();
                for (int i=0;i<this.getClockList().size();i++) {
                    TimeGuard timeGuard1 = transition.getTimeGuard(this.getClockList().get(i));
                    timeGuardList.add(timeGuard1);
                }
                List<BigDecimal> bigDecimalList=new ArrayList<>();
                for (int i=0;i<this.getClockList().size();i++){
                    BigDecimal b1 = new BigDecimal(Double.toString(action.getValue(this.getClockList().get(i))));
                    bigDecimalList.add(b1);
                }

                boolean inscope=true;
                for (int i=0;i<this.getClockList().size();i++){
                    if(timeGuardList.get(i).isPass(action.getValue(this.getClockList().get(i)))){

                    }
                    else {
                        inscope=false;
                    }
                }
                boolean eqdiff=true;
                for (int i=0;i<this.getClockList().size();i++){
                    for (int j=i+1;j<this.getClockList().size();j++){
                        BigDecimal b1 = new BigDecimal(Double.toString(difference[i]));
                        BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));

                        BigDecimal b3=new BigDecimal(bigDecimalList.get(i).subtract(bigDecimalList.get(j)).doubleValue());
                        BigDecimal b4=new BigDecimal(b1.subtract(b2).doubleValue());
                        if(b3.subtract(b4).doubleValue()==0){

                        }
                        else {
                            eqdiff=false;
                            break;
                        }

                    }
                }
                if ((inscope && eqdiff)) {
                    location = transition.getTargetLocation();
                    boolean[] actualResets=new boolean[this.getClockList().size()];
                    for (int i=0;i<this.getClockList().size();i++){
                        actualResets[i]=transition.isReset(this.getClockList().get(i));
                    }
                    Resets resets1 = new Resets(actualResets);
                    resets.add(resets1);
                    for (int i=0;i<this.getClockList().size();i++){
                        if (!transition.isReset(this.getClockList().get(i))){
                            difference[i]=action.getValue(this.getClockList().get(i));
                        }
                        else {
                            difference[i]=0;
                        }

                    }
                    continue flag;
                }

            }
            return null;
        }
        BooleanAnswer booleanAnswer=new BooleanAnswer(resets,location.isAccept());
        return booleanAnswer;
    }

    //给定一个逻辑时间字，DOTA最多一个路径
    public TaLocation reach(TwoClockLogicTimeWord logicTimeWord) {
      //  System.out.println("进入reach函数");
        TaLocation location = getInitLocation();
        Clock clock1 = getClockList().get(0);
    //    Clock clock2 = getClockList().get(1);
        //存储当前时钟值,初始化0
        double value = 0.0;
        double[] difference=new double[this.getClockList().size()];
        Map<Clock, Double> clockDoubleMap = new HashMap<>();
        clockDoubleMap.put(clock1, 0.0);
     //   clockDoubleMap.put(clock2, 0.0);

        List<TwoClockLogicAction> actions = logicTimeWord.getTimedActions();
        int bigger=0;
      //  System.out.println("获取action");
        flag:
        for (TwoClockLogicAction action : actions) {
      //      System.out.println(action.toString());
//            System.out.println(clock1.toString());
         //   System.out.println("locationid:"+location.getId());
        //    List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
        //    System.out.println(transitions);

            List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
            for (TaTransition transition : transitions) {
                //System.out.println(transition.getEdgeTimeGuard().getEdgeTimeGuardList().get(0));
                List<TimeGuard> timeGuardList=new ArrayList<>();
                for (int i=0;i<this.getClockList().size();i++) {
                    TimeGuard timeGuard1 = transition.getTimeGuard(this.getClockList().get(i));
         //           System.out.println("timeGuard1:"+timeGuard1);
                    timeGuardList.add(timeGuard1);
                }
           //     System.out.println("asdd");

                List<BigDecimal> bigDecimalList=new ArrayList<>();
                for (int i=0;i<this.getClockList().size();i++){
                    BigDecimal b1 = new BigDecimal(Double.toString(action.getValue(this.getClockList().get(i))));
                    bigDecimalList.add(b1);
                }

                boolean inscope=true;
                for (int i=0;i<this.getClockList().size();i++){
                    if(timeGuardList.get(i).isPass(action.getValue(this.getClockList().get(i)))){

                    }
                    else {
                        inscope=false;
                    }
                }
             //   System.out.println("inscope:"+inscope);
                boolean eqdiff=true;
                for (int i=0;i<this.getClockList().size();i++){
                    for (int j=i+1;j<this.getClockList().size();j++){
                        BigDecimal b1 = new BigDecimal(Double.toString(difference[i]));
                        BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));
                        if(Double.doubleToLongBits(bigDecimalList.get(i).subtract(bigDecimalList.get(j)).doubleValue())==Double.doubleToLongBits(b1.subtract(b2).doubleValue())){

                        }
                        else {
                            eqdiff=false;
                            break;
                        }

                    }
                }
           //     System.out.println("eqdiff:"+eqdiff);
                if ((inscope && eqdiff)) {

//                for (int i=0;i<this.getClockList().size();i++){
//                    if(transition.isReset(this.getClockList().get(i))!=action.isReset(this.getClockList().get(i))){
//                        continue ;
//                    }
//                }

//            for (TaTransition transition : transitions) {
//                TimeGuard timeGuard1 = transition.getTimeGuard(clock1);
//                TimeGuard timeGuard2 = transition.getTimeGuard(clock2);
////
//                  System.out.println("bigger:"+bigger+"差值"+((int)(action.getValue(clock1)*100)-(int)(action.getValue(clock2)*100))/100.0);
//                  System.out.println("没有除法的差值"+((int)(action.getValue(clock1)*100)-(int)(action.getValue(clock2)*100)));
//                System.out.println("没有除法和int的差值"+((action.getValue(clock1)*100)-(action.getValue(clock2)*100)));
//                System.out.println("没有除法和int的差值再除100.0"+((action.getValue(clock1)*100)-(action.getValue(clock2)*100))/100.0);
//                BigDecimal b1 = new BigDecimal(Double.toString(action.getValue(clock1)));
//                BigDecimal b2 = new BigDecimal(Double.toString(action.getValue(clock2)));
              //  System.out.println(b1.subtract(b2).doubleValue());
                //  System.out.println(action.getValue(hc1)-action.getValue(hc2));
//                if ((timeGuard1.isPass(action.getValue(clock1))
//                        && timeGuard2.isPass(action.getValue(clock2))&&bigger==1&&
//                        (b1.subtract(b2).doubleValue()*100==difference*100))||
//                        (timeGuard1.isPass(action.getValue(clock1))
//                                && timeGuard2.isPass(action.getValue(clock2))&&bigger==2&&
//                                (b2.subtract(b1).doubleValue()*100==difference*100))||
//                        (timeGuard1.isPass(action.getValue(clock1))
//                                && timeGuard2.isPass(action.getValue(clock1))&&bigger==0&&
//                                (action.getValue(clock1)==action.getValue(clock2)))) {
//                    if(!transition.getEdgeTimeGuard().getLargerclock()||transition.getEdgeTimeGuard().getLargerclock()&& transition.getEdgeTimeGuard().isHavelowerdiff()
//                            &&!transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&& transition.getEdgeTimeGuard().isHavelowerdiff()&&!transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                            b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&& !transition.getEdgeTimeGuard().isHavelowerdiff()
//                            &&transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&& !transition.getEdgeTimeGuard().isHavelowerdiff()
//                            &&transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                            b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()&&
//                            b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                            b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()&&
//                            b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                            b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()&&
//                            b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()
//                            ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                            b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()&&
//                            b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()){
                    //  System.out.println("difference:"+difference);
                    location = transition.getTargetLocation();
//                    System.out.println("clock1的guard"+timeGuard1.toString());
//                    System.out.println("clock2的guard"+timeGuard2);
//                    System.out.println("clock1是否重置"+transition.isReset(clock1));
//                    System.out.println("clock2是否重置"+transition.isReset(clock1));
                    for (int i=0;i<this.getClockList().size();i++){
                            if (!transition.isReset(this.getClockList().get(i))){
                                difference[i]=action.getValue(this.getClockList().get(i));
                            }
                            else {
                                difference[i]=0;
                            }
                    }
//                    if (transition.isReset(clock1) == true && transition.isReset(clock2) == true) {
//                        difference = 0;
//                        bigger = 0;
//                    }
//                    if (transition.isReset(clock1) == true && transition.isReset(clock2) == false) {
////                        if (bigger==2||bigger==0){
////                            difference=difference+action.getValue(clock2);
////                            bigger=2;}
////                        else {
//                        difference = action.getValue(clock2);
//                        bigger = 2;
//                        //}
//                    }
//                    if (transition.isReset(clock1) == false && transition.isReset(clock2) == true) {
////                        if (bigger==1||bigger==0) {
////                            difference = difference + action.getValue(clock1);
////                            bigger = 1;
////                        }
////                        else {
//                        //   System.out.println("重置了y");
//                        difference = action.getValue(clock1);
//                        bigger = 1;
//                        //    }
//                    }
//                    if (transition.isReset(clock1) == false && transition.isReset(clock2) == false) {
//
//                    }
                    //   System.out.println(difference);
                     continue flag;
                }

            }

//            for (TaTransition transition : transitions) {
//                TimeGuard timeGuard1 = transition.getTimeGuard(clock1);
//                TimeGuard timeGuard2 = transition.getTimeGuard(clock2);
//                if (timeGuard1.isPass(action.getValue(clock1))
//                        && timeGuard2.isPass(action.getValue(clock2))) {
//                    location = transition.getTargetLocation();
//                  //  System.out.println("遍历的location："+location);
//                    continue flag;
//                }
//            }
           // return null;
        }
        //System.out.println(location.getId());
        return location;
    }
    public TwoClockResetLogicTimeWord transferResetbyteacher(TwoClockLogicTimeWord logicTimeWord) {
        TaLocation location = this.getInitLocation();
        double[] difference=new double[this.getClockList().size()];
        List<TwoClockLogicAction> actions = logicTimeWord.getTimedActions();
        List<TwoClockResetLogicAction> resetActions = new ArrayList();
        boolean end = false;
        Iterator var11 = actions.iterator();

        while(true) {
            label32:
            while(var11.hasNext()) {
                TwoClockLogicAction action = (TwoClockLogicAction)var11.next();
                if (!end) {
                    List<TaTransition> transitions = this.getTransitions(location, action.getSymbol(), (TaLocation)null);
                    Iterator var14 = transitions.iterator();
                    while(var14.hasNext()) {
                        TaTransition transition = (TaTransition) var14.next();
                        List<TimeGuard> timeGuardList=new ArrayList<>();
                        for (int i=0;i<this.getClockList().size();i++) {
                            TimeGuard timeGuard1 = transition.getTimeGuard(this.getClockList().get(i));
                            timeGuardList.add(timeGuard1);
                        }
                        List<BigDecimal> bigDecimalList=new ArrayList<>();
                        for (int i=0;i<this.getClockList().size();i++){
                        BigDecimal b1 = new BigDecimal(Double.toString(action.getValue(this.getClockList().get(i))));
                            bigDecimalList.add(b1);
                        }
                        boolean inscope=true;
                        for (int i=0;i<this.getClockList().size();i++){
                            if(timeGuardList.get(i).isPass(action.getValue(this.getClockList().get(i)))){

                            }
                            else {
                                inscope=false;
                            }
                        }
                        boolean eqdiff=true;
                        for (int i=0;i<this.getClockList().size()-1;i++){
                            for (int j=i+1;j<this.getClockList().size();j++){
                                BigDecimal b1 = new BigDecimal(Double.toString(difference[i]));
                                BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));
                                if(Double.doubleToLongBits(bigDecimalList.get(i).subtract(bigDecimalList.get(j)).doubleValue())==Double.doubleToLongBits(b1.subtract(b2).doubleValue())){

                                }
                                else {
                                    eqdiff=false;
                                    break;
                                }

                            }
                        }
                        if ((inscope && eqdiff)) {
                            location = transition.getTargetLocation();
                            Set<Clock> clockResetSet = new HashSet();
                            clockResetSet.addAll(transition.getResetClockSet());
                            TwoClockResetLogicAction resetLogicAction = new TwoClockResetLogicAction(action.getSymbol(), clockResetSet, action.getClockValueMap());
                            resetLogicAction.setClockValueMap(action.getClockValueMap());
                            resetLogicAction.setSymbol(action.getSymbol());
                            resetLogicAction.setResetClockSet(clockResetSet);
                            resetActions.add(resetLogicAction);

                            for (int i=0;i<this.getClockList().size();i++){
                                if (!transition.isReset(this.getClockList().get(i))){
                                    difference[i]=action.getValue(this.getClockList().get(i));
                                }
                                else {
                                    difference[i]=0.0;
                                }

                            }
                            continue label32;

                    }
                    }

                    end = true;
                }

                if (end) {
                    Set<Clock> clockResetSet = new HashSet();
                    for (int i=0;i<this.getClockList().size();i++){
                        clockResetSet.add(this.getClockList().get(i));
                    }
                    TwoClockResetLogicAction resetLogicAction = new TwoClockResetLogicAction(action.getSymbol(), clockResetSet, action.getClockValueMap());
                    resetLogicAction.setResetClockSet(clockResetSet);
                    resetLogicAction.setSymbol(action.getSymbol());
                    resetLogicAction.setClockValueMap(action.getClockValueMap());
                    resetActions.add(resetLogicAction);
                }
            }

            TwoClockResetLogicTimeWord TwoClockResetLogicTimeWord = new TwoClockResetLogicTimeWord(resetActions);
            TwoClockResetLogicTimeWord.setTimedActions(resetActions);
            return TwoClockResetLogicTimeWord;
        }
    }

    public TwoClockResetLogicTimeWord transferReset(TwoClockLogicTimeWord logicTimeWord) {
        TaLocation location = getInitLocation();
        Clock clock1 = getClockList().get(0);
        Clock clock2 = getClockList().get(1);
     //   System.out.println("clock1:"+clock1);
//        System.out.println(logicTimeWord.toString());
//        System.out.println(logicTimeWord.getTimedActions().get(0).getSymbol());
//        System.out.println(logicTimeWord.getTimedActions().get(0).getClockValueMap().get(0));
//        System.out.println(logicTimeWord.getTimedActions().get(0).getClockValueMap().get(1));
        //存储当前时钟值,初始化0
        double value = 0.0;
        Map<Clock, Double> clockDoubleMap = new HashMap<>();
        clockDoubleMap.put(clock1, 0.0);
        clockDoubleMap.put(clock2, 0.0);

        List<TwoClockLogicAction> actions = logicTimeWord.getTimedActions();

   //    System.out.println("actions为："+actions.toString());
        List<TwoClockResetLogicAction> resetActions = new ArrayList<>();
        boolean end = false;
        double difference=0;
        int bigger=0;
        flag:
        for (TwoClockLogicAction action : actions) {
           // System.out.println("action为："+action.toString());
                List<TaTransition> transitions = getTransitions(location, action.getSymbol(), null);
                for (TaTransition transition : transitions) {
                    //   System.out.println("transition:"+transition.toString());
                    TimeGuard timeGuard1 = transition.getTimeGuard(clock1);
                    TimeGuard timeGuard2 = transition.getTimeGuard(clock2);
//                    System.out.println("timeGuard1为:"+timeGuard1);
//                    System.out.println("timeGuard2为:"+timeGuard2);

//                    if (timeGuard1.isPass(action.getValue(clock1))
//                            && timeGuard2.isPass(action.getValue(clock2))) {
//                        location = transition.getTargetLocation();
//                        Set<Clock> clockResetSet = new HashSet<>();
//                        clockResetSet.addAll(transition.getResetClockSet());
//          //              System.out.println("clockResetSet为："+clockResetSet.toString());
////                        TwoClockResetLogicAction resetLogicAction = new TwoClockResetLogicAction(
////                                action.getSymbol(), clockResetSet, action.getClockValueMap());
//                        TwoClockResetLogicAction resetLogicAction=new TwoClockResetLogicAction(action.getSymbol(), clockResetSet, action.getClockValueMap());
//                        resetLogicAction.setClockValueMap(action.getClockValueMap());
//                        resetLogicAction.setSymbol(action.getSymbol());
//                        resetLogicAction.setResetClockSet(clockResetSet);
//                   //     System.out.println("resetLogicAction为"+resetLogicAction.toString());
//                        resetActions.add(resetLogicAction);
//                        continue flag;
//                    }


                    if ((timeGuard1.isPass(action.getValue(clock1))
                            && timeGuard2.isPass(action.getValue(clock2)))) {
                        BigDecimal b1 = new BigDecimal(Double.toString(action.getValue(clock1)));
                        BigDecimal b2 = new BigDecimal(Double.toString(action.getValue(clock2)));
//                        if(!transition.getEdgeTimeGuard().getLargerclock()||transition.getEdgeTimeGuard().getLargerclock()&& transition.getEdgeTimeGuard().isHavelowerdiff()
//                                &&!transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&& transition.getEdgeTimeGuard().isHavelowerdiff()&&!transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                                b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&& !transition.getEdgeTimeGuard().isHavelowerdiff()
//                                &&transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&& !transition.getEdgeTimeGuard().isHavelowerdiff()
//                                &&transition.getEdgeTimeGuard().isHaveupperdiff()&& b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                                b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()&&
//                                b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                                b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()&&
//                                b1.subtract(b2).doubleValue()*100>=transition.getEdgeTimeGuard().getDifferencexylower()*100&&transition.getEdgeTimeGuard().isIsequallower()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                                b1.subtract(b2).doubleValue()*100<=transition.getEdgeTimeGuard().getDifferencexyupper()*100&&transition.getEdgeTimeGuard().isIsequalupper()&&
//                                b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()
//                                ||transition.getEdgeTimeGuard().getLargerclock()&&transition.getEdgeTimeGuard().isHavelowerdiff()&&transition.getEdgeTimeGuard().isHaveupperdiff()&&
//                                b1.subtract(b2).doubleValue()*100<transition.getEdgeTimeGuard().getDifferencexyupper()*100&&!transition.getEdgeTimeGuard().isIsequalupper()&&
//                                b1.subtract(b2).doubleValue()*100>transition.getEdgeTimeGuard().getDifferencexylower()*100&&!transition.getEdgeTimeGuard().isIsequallower()){
//



                            //       System.out.println("有后继");
                        //   System.out.println("difference:"+difference);
                        location = transition.getTargetLocation();
                        Set<Clock> clockResetSet = new HashSet();
                        clockResetSet.addAll(transition.getResetClockSet());
                        //          System.out.println("clockResetSet为：" + clockResetSet.toString());
                        TwoClockResetLogicAction resetLogicAction = new TwoClockResetLogicAction(action.getSymbol(), clockResetSet, action.getClockValueMap());
                        resetLogicAction.setClockValueMap(action.getClockValueMap());
                        resetLogicAction.setSymbol(action.getSymbol());
                        Set<Clock> clockResetSetReturn = new HashSet<>();
                        if (clockResetSet.contains(clock1)) {
                            //    System.out.println(clock1+"被重置");
                            clockResetSetReturn.add(clock1);
                        }
                        if (clockResetSet.contains(clock2)) {
                            //   System.out.println(clock2+"被重置");
                            clockResetSetReturn.add(clock2);
                        }
                        resetLogicAction.setResetClockSet(clockResetSetReturn);
                        //         System.out.println("resetLogicAction为" + resetLogicAction.toString());
                        resetActions.add(resetLogicAction);
                        // continue label32;
                    }

            }
//            if (end) {
//                Set<Clock> clockResetSet = new HashSet<>();
//                clockResetSet.add(clock1);
//                clockResetSet.add(clock2);
//                TwoClockResetLogicAction resetLogicAction = new TwoClockResetLogicAction(
//                        action.getSymbol(), clockResetSet, action.getClockValueMap());
//                resetActions.add(resetLogicAction);
//            }
        }
        //System.out.println("resetActions为:"+resetActions);
        TwoClockResetLogicTimeWord TwoClockResetLogicTimeWord=new TwoClockResetLogicTimeWord(resetActions);
       // System.out.println("未set之前的TwoClockResetLogicTimeWord"+TwoClockResetLogicTimeWord.toString());
        TwoClockResetLogicTimeWord.setTimedActions(resetActions);
       // System.out.println("TwoClockResetLogicTimeWord"+TwoClockResetLogicTimeWord.toString());
      //  return new TwoClockResetLogicTimeWord(resetActions);
        return TwoClockResetLogicTimeWord;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//        sb.append("{\n\t").append("\"sigma\":[");
//        for (String action : getSigma()) {
//            sb.append("\"" + action + "\",");
//        }
//        sb.deleteCharAt(sb.length() - 1).append("],\n\t").append("\"init\":");
//        String init = getInitLocation().getId() + "";
//        sb.append(init).append(",\n\t").append("\"name\":\"").append(getName()).append("\"\n\t");
//        sb.append("\"s\":[");
//        for (TaLocation l : getLocations()) {
//            sb.append(l.getId()).append(",");
//        }
//        sb.deleteCharAt(sb.length() - 1).append("]\n\t\"tran\":{\n");
//
//        getTransitions().sort(new OTATranComparator(clock));
//        for (int i = 0; i < getTransitions().size(); i++) {
//            TaTransition t = getTransitions().get(i);
//            String reset = t.getResetClockSet().contains(clock) ? "r" : "n";
//            sb.append("\t\t\"").append(i).append("\":[")
//                    .append(t.getSourceId()).append(",")
//                    .append("\"").append(t.getSymbol()).append("\",")
//                    .append("\"").append(t.getTimeGuard(clock)).append("\",")
//                    .append(t.getTargetId()).append(", ").append(reset).append("]").append(",\n");
//        }
//        sb.deleteCharAt(sb.length() - 2);
//        sb.append("\t},\n\t").append("\"accpted\":[");
//        for (TaLocation l : getAcceptedLocations()) {
//            sb.append(l.getId()).append(",");
//        }
//        sb.deleteCharAt(sb.length() - 1).append("]\n}");
        return sb.toString();
    }


}
