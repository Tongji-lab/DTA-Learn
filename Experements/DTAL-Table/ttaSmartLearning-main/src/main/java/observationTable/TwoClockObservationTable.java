package observationTable;

import com.microsoft.z3.*;
import frame.TwoClockLearner;
import frame.TwoClockTeacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import ta.*;
import ta.ota.*;
import ta.twoClockTA.*;
import defaultTeacher.PairRegion;
import timedAction.DelayTimedAction;
import timedAction.TwoClockResetTimedAction;
import timedWord.DelayTimeWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

@Data
public class TwoClockObservationTable implements TwoClockLearner<TwoClockResetLogicTimeWord> {
    private String name;
    private Set<String> sigma;
    private TwoClockTeacher<TwoClockResetLogicTimeWord, TwoClockResetLogicTimeWord, TwoClockTA, TwoClockLogicTimeWord> teacher;
//    private int[] kc;
private int kc;
    List<Clock> clockList=new ArrayList<>();
    private  int reset;
    private List<TwoClockResetLogicTimeWord> s;
    private List<TwoClockResetLogicTimeWord> r;
    private List<RegionTwoClockLogicTimedWord> suffixSet;
    private int memberctx=0;
    private int member=0;
    private int membertable=0;
    private Map<PairRegion, BooleanAnswer> answers;
    private TwoClockTA hypothesis;
    private int selectfromregionc;
    public TwoClockObservationTable(String name, Set<String> sigma, TwoClockTeacher<TwoClockResetLogicTimeWord, TwoClockResetLogicTimeWord, TwoClockTA, TwoClockLogicTimeWord> teacher,List<Clock> clockList,int kc) {
        this.name = name;
        this.sigma = sigma;
        this.teacher = teacher;
        this.clockList=clockList;
        this.kc=kc;
    }

    @Override
    public void init() {
        s=new ArrayList<>();
        r=new ArrayList<>();
        suffixSet = new ArrayList<>();
        s.add(TwoClockResetLogicTimeWord.emptyWord());
        suffixSet.add(RegionTwoClockLogicTimedWord.emptyWord());
        for (String symbol : sigma) {
            Map<Clock,Double> map=new HashMap<>();
            for (int i=0;i<clockList.size();i++){
            map.put(clockList.get(i),0d);}
            TwoClockLogicAction logicAction = new TwoClockLogicAction(symbol, map);
            TwoClockLogicTimeWord timeWord = new TwoClockLogicTimeWord(Arrays.asList(logicAction));
            timeWord.setTimedActions(Arrays.asList(logicAction));
            TwoClockResetLogicTimeWord resetLogicTimeWord = teacher.transferWord(timeWord);
            r.add(resetLogicTimeWord);
        }
        answers = new HashMap<>();
    }

    @Override
    public void learn() {
        fillTable();
        while (!isPrepared()) {
            TwoClockResetLogicTimeWord notclosed=isClosed();
            if (notclosed!=null) {
                makeClosed(notclosed);
            }
            if (!isConsistent()) {
                makeConsistent();
            }
        }
    }
    @Override
    public void refine(TwoClockResetLogicTimeWord counterExample) {
        Set<TwoClockResetLogicTimeWord> prefixesSet = counterExample.getAllPrefixes();
        prefixesSet.removeAll(s);
        prefixesSet.removeAll(r);
        r.addAll(prefixesSet);
        int old=member;
        fillTable(r);
        memberctx+=member-old;
        while (!isPrepared()) {
            TwoClockResetLogicTimeWord notclosed=isClosed();
            if (notclosed!=null) {
                makeClosed(notclosed);
            }
            if (!isConsistent()) {
                makeConsistent();
                show();
                show();
            }
        }
    }

    @Override
    public boolean check(TwoClockResetLogicTimeWord counterExample) {
        return false;
    }

    @Override

    public TwoClockTA buildHypothesis() {
        show();
        System.out.println("start build hypothesis");
        List<TaLocation> locationList = new ArrayList<>();
        List<TaTransition> transitionList = new ArrayList<>();
        //创建观察表每一行和location的映射关系
        Map<Row, TaLocation> rowLocationMap = new HashMap<>();
        List<TaLocation> regionsplit = new ArrayList<>();
        //根据s中的row来创建Location
        Map<TaLocation,TwoClockResetLogicTimeWord> stolocation = new HashMap<>();
        Set<Row> rowSet = new HashSet<>();
        int id = 1;
        for (TwoClockResetLogicTimeWord sWord : s) {
            Row row = row(sWord);
            if (!rowSet.contains(row)) {
                rowSet.add(row);
                boolean init=row(sWord).equals(row(TwoClockResetLogicTimeWord.emptyWord()));

                boolean accepted;
                if(sWord.getTimedActions()!=null){
                    PairRegion pairRegion = new PairRegion(sWord,null);
                    pairRegion.prefix=sWord;
                    pairRegion.suffix=null;
                    BooleanAnswer answer = answers.get(pairRegion);
                    if(answer!=null) {
                        accepted = answer.isAccept();
                    }
                    else {
                        accepted=false;
                    }
                }

                else {
                    PairRegion pairRegion = new PairRegion(null,null);
                    pairRegion.prefix=null;
                    pairRegion.suffix=null;
                       BooleanAnswer answer = answers.get(pairRegion);
                    accepted = answer.isAccept();

                }
                TaLocation location = new TaLocation(String.valueOf(id), String.valueOf(id), init, accepted);
                locationList.add(location);
                rowLocationMap.put(row, location);
                stolocation.put(location,sWord);
                id++;
            }
        }

        //根据观察表来创建Transition
        List<TwoClockResetLogicTimeWord> sr = getPrefixSet();
        for (TwoClockResetLogicTimeWord word : sr) {
            if (word.isEmpty()) {
                continue;
            }
            TwoClockResetLogicTimeWord pre = new TwoClockResetLogicTimeWord(null);
            if (word.size() != 1) {
                pre = word.subWord(0, word.size() - 1);
            } else {
                pre=TwoClockResetLogicTimeWord.emptyWord();
            }
            if (sr.contains(pre)) {
                    TwoClockResetLogicAction action = word.getLastResetAction();
                    TaLocation sourceLocation = rowLocationMap.get(row(pre));

                TaLocation targetLocation = rowLocationMap.get(row(word));
                    String symbol = action.getSymbol();
                    List<TimeGuard> timeGuardList=new ArrayList<>();
                    for (int i=0;i<clockList.size();i++){
                        TimeGuard timeGuard = TimeGuard.TwoClockbottomGuard(word.getLastLogicAction(), clockList.get(i));
                        timeGuardList.add(timeGuard);
                    }
                    EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
                    Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();

                    for (int i=0;i<clockList.size();i++){
                        clockTimeGuardMap.put(clockList.get(i),timeGuardList.get(i));
                    }
                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                    List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
                    for (int i=0;i<clockList.size()-1;i++){
                      for (int j=i+1;j<clockList.size();j++){
                          if ((int) Math.round(word.getLastLogicAction().getValue(clockList.get(i)) * 100) % 100 > (int) Math.round(word.getLastLogicAction().getValue(clockList.get(j)) * 100) % 100 && (int) Math.round(word.getLastLogicAction().getValue(clockList.get(i)) * 100) % 100 != 0 && (int) Math.round(word.getLastLogicAction().getValue(clockList.get(j)) * 100) % 100 != 0) {
                             DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
                              differenceEdgeTimeGuard.setLargerclock(true);
                              differenceEdgeTimeGuard.setClock1(clockList.get(i));
                              differenceEdgeTimeGuard.setClock2(clockList.get(j));
                              differenceEdgeTimeGuard.setHavelowerdiff(true);
                              differenceEdgeTimeGuard.setDifferencexylower((int) word.getLastLogicAction().getValue(clockList.get(i)) - (int) word.getLastLogicAction().getValue(clockList.get(j)));
                              differenceEdgeTimeGuard.setIsequallower(false);
                              differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
                          }
                          if ((int) Math.round(word.getLastLogicAction().getValue(clockList.get(i)) * 100) % 100 < (int) Math.round(word.getLastLogicAction().getValue(clockList.get(j)) * 100) % 100 && (int) Math.round(word.getLastLogicAction().getValue(clockList.get(i)) * 100) % 100 != 0 && (int) Math.round(word.getLastLogicAction().getValue(clockList.get(j)) * 100) % 100 != 0) {
                              DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
                              differenceEdgeTimeGuard.setClock1(clockList.get(i));
                              differenceEdgeTimeGuard.setClock2(clockList.get(j));
                              differenceEdgeTimeGuard.setLargerclock(true);
                              differenceEdgeTimeGuard.setHaveupperdiff(true);
                              differenceEdgeTimeGuard.setDifferencexyupper((int) word.getLastLogicAction().getValue(clockList.get(i)) - (int) word.getLastLogicAction().getValue(clockList.get(j)));
                              differenceEdgeTimeGuard.setIsequalupper(false);
                              differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);

                          }
                          if ((int) Math.round(word.getLastLogicAction().getValue(clockList.get(i)) * 100) % 100 == (int) Math.round(word.getLastLogicAction().getValue(clockList.get(j)) * 100) % 100)
                              if ((int) Math.round(word.getLastLogicAction().getValue(clockList.get(i)) * 100) % 100 != 0 && (int) Math.round(word.getLastLogicAction().getValue(clockList.get(j)) * 100) % 100 != 0) {
                                  DifferenceEdgeTimeGuard differenceEdgeTimeGuard = new DifferenceEdgeTimeGuard();
                                  differenceEdgeTimeGuard.setClock1(clockList.get(i));
                                  differenceEdgeTimeGuard.setClock2(clockList.get(j));
                                  differenceEdgeTimeGuard.setLargerclock(true);
                                  differenceEdgeTimeGuard.setHaveupperdiff(true);
                                  differenceEdgeTimeGuard.setDifferencexyupper((int) word.getLastLogicAction().getValue(clockList.get(i)) - (int) word.getLastLogicAction().getValue(clockList.get(j)));
                                  differenceEdgeTimeGuard.setIsequalupper(true);
                                  differenceEdgeTimeGuard.setHavelowerdiff(true);
                                  differenceEdgeTimeGuard.setDifferencexylower((int) word.getLastLogicAction().getValue(clockList.get(i)) - (int) word.getLastLogicAction().getValue(clockList.get(j)));
                                  differenceEdgeTimeGuard.setIsequallower(true);
                                  differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
                              }
                      }
                    }
                    edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
                    Set<Clock> resetClockSet = new HashSet<>();
                    for (int i=0;i<clockList.size();i++){
                        if (action.isReset(clockList.get(i))) {
                            resetClockSet.add(clockList.get(i));
                        }
                    }
                    TaTransition transition = new TaTransition(
                            sourceLocation, targetLocation, symbol, edgeTimeGuard, resetClockSet);
                    if (!transitionList.contains(transition)) {
                        transitionList.add(transition);
                    }
                }
            }
        TwoClockTA evidenceDOTA = new TwoClockTA(name, clockList,sigma, locationList, transitionList);
        evidenceToDOTA(evidenceDOTA,regionsplit);
//        evidenceToDOTA(evidenceDOTA);
        this.hypothesis = evidenceDOTA;
        System.out.println("membertable:"+membertable);
        System.out.println("memberctx:"+memberctx);
        return hypothesis;
    }

    Map<PairRegion,TwoClockResetLogicTimeWord> pairRegionTwoClockResetLogicTimeWordMap=new HashMap<>();

//    public TwoClockResetLogicTimeWord selectfromRegion(RegionTwoClockLogicTimedWord suffix,TwoClockResetLogicTimeWord prefix){
//       System.out.println("in selectfromRegion");
//        System.out.println("prefix:"+prefix);
//        System.out.println("suff:"+suffix.get(0).getRegion().getTimeGuardList().get(0).getLowerBound()+suffix.get(0).getRegion().getTimeGuardList().get(0).getUpperBound()+suffix.get(0).getRegion().getTimeGuardList().get(1).getLowerBound()+suffix.get(0).getRegion().getTimeGuardList().get(1).getUpperBound());
//
//        TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=null;
//        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(null);
//        List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
//
//        double[] difference = new double[this.getClockList().size()];
//        if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
//            TwoClockResetLogicAction twoClockLogicAction = prefix.get(prefix.size() - 1);
//            for (int p = 0; p < this.getClockList().size(); p++) {
//                if (!twoClockLogicAction.isReset(this.getClockList().get(p))) {
//                    difference[p] = twoClockLogicAction.getValue(this.getClockList().get(p));
//                } else {
//                    difference[p] = 0d;
//                }
//
//            }
//        }
//
//        for (int i = 0; i < suffix.size(); i++) {
//            if(twoClockResetLogicTimeWord!=null&&twoClockResetLogicTimeWord.getTimedActions()!=null&&twoClockResetLogicTimeWord.getTimedActions().size()!=0){
//                twoClockResetLogicTimeWord.setTimedActions(null);}
//            for (int i1=0;i1<clockList.size()-1;i1++){
//                for (int j=i+1;j<clockList.size();j++){
//                    int index = 0;
//                    for (int l = 1; l < i1 + 1; l++) {
//                        index += clockList.size() - l;
//                    }
//                    BigDecimal bigDecimal=new BigDecimal(difference[i1]);
//                    BigDecimal bigDecimal2=new BigDecimal(difference[j]);
//
//                    if(bigDecimal.subtract(bigDecimal2).doubleValue()<=0&&suffix.get(i).getRegion().getDifferList()[index - 1 + j - i1]==1||
//                            bigDecimal.subtract(bigDecimal2).doubleValue()>=0&&suffix.get(i).getRegion().getDifferList()[index - 1 + j - i1]==-1||
//                            bigDecimal.subtract(bigDecimal2).doubleValue()!=0&&suffix.get(i).getRegion().getDifferList()[index - 1 + j - i1]==0){
//                        return null;
//                    }
//                }
//            }
//
////                double []array=new double[clockList.size()];
//                BigDecimal bigDecimal=new BigDecimal(suffix.get(i).getRegion().getTimeGuardList().get(0).getLowerBound());
//                BigDecimal bigDecimal2=new BigDecimal(difference[0]);
//                double mind=bigDecimal.subtract(bigDecimal2).doubleValue();
//                for (int i1=0;i1<clockList.size();i1++){
//                    BigDecimal bigDecimal3=new BigDecimal(suffix.get(i).getRegion().getTimeGuardList().get(i1).getLowerBound());
//                    BigDecimal bigDecimal4=new BigDecimal(difference[i1]);
//                    double diff=bigDecimal3.subtract(bigDecimal4).doubleValue();
//                    if(diff<0){
//                        return twoClockResetLogicTimeWord;
//                    }
//                    else {
//                        BigDecimal bigDecimal5=new BigDecimal(mind);
//                        BigDecimal bigDecimal6=new BigDecimal(diff);
//                        if(bigDecimal6.subtract(bigDecimal5).doubleValue()>0){
//                            mind=diff;
//                        }
//                    }
//                }
//                double maxd=bigDecimal.subtract(bigDecimal2).doubleValue();
//                for (int i1=0;i1<clockList.size();i1++){
//                    BigDecimal bigDecimal3=new BigDecimal(suffix.get(i).getRegion().getTimeGuardList().get(i1).getLowerBound());
//                    BigDecimal bigDecimal4=new BigDecimal(difference[i1]);
//                    double diff=bigDecimal3.subtract(bigDecimal4).doubleValue();
//                    if(diff<0){
//                        return twoClockResetLogicTimeWord;
//                    }
//                    else {
//                        BigDecimal bigDecimal5=new BigDecimal(maxd);
//                        BigDecimal bigDecimal6=new BigDecimal(diff);
//                        if(bigDecimal5.subtract(bigDecimal6).doubleValue()>0){
//                            maxd=diff;
//                        }
//                    }
//                }
//
//                BigDecimal bigDecimal7=new BigDecimal(mind);
//                BigDecimal bigDecimal8=new BigDecimal(maxd);
//                if(bigDecimal7.subtract(bigDecimal8).doubleValue()==0){
//                    int d=0;
//                    Map<Clock,Double> map=new HashMap<>();
////                System.out.println("a:"+a);
////                System.out.println("c:"+c);
//                    for (int i1=0;i1<clockList.size();i1++){
//                        BigDecimal bigDecimal9=new BigDecimal(d);
//                        BigDecimal bigDecimal10=new BigDecimal(difference[i1]);
//                        map.put(clockList.get(i1),bigDecimal9.add(bigDecimal10).doubleValue());
//                    }
//                    TwoClockLogicAction twoClockLogicAction1=new TwoClockLogicAction(suffix.get(i).getSymbol(),map);
//                    twoClockLogicActionList.add(twoClockLogicAction1);
//                    twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//
//                    TwoClockLogicTimeWord logicTimeWord2=new TwoClockLogicTimeWord(null);
//                    if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
//                        logicTimeWord2 = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
//                        logicTimeWord2.setTimedActions(TwoClockResetLogicTimeWord.logicTimeWord(prefix).getTimedActions());
//                        logicTimeWord2 = TwoClockLogicTimeWord.concat(logicTimeWord2, twoClockLogicTimeWord);
//                    }
//                    else {
//                        logicTimeWord2=twoClockLogicTimeWord;
//                    }
//                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=teacher.transferWord(logicTimeWord2);
//                    twoClockResetLogicTimeWord=twoClockResetLogicTimeWord1;
//                    twoClockResetLogicTimeWord.setTimedActions(twoClockResetLogicTimeWord1.getTimedActions());
//                    for (int p=0;p<this.getClockList().size();p++){
//                        if (!twoClockResetLogicTimeWord1.getLastResetAction().isReset(this.getClockList().get(p))){
//                            difference[p]=twoClockResetLogicTimeWord1.getLastResetAction().getValue(this.getClockList().get(p));
//                        }
//                        else {
//                            difference[p]=0d;
//                        }
//                    }
//                }
//            else{
//                double d=0.05;
//                Map<Clock,Double> map=new HashMap<>();
////                System.out.println("a:"+a);
////                System.out.println("c:"+c);
//                for (int i1=0;i1<clockList.size();i1++){
//                    BigDecimal bigDecimal9=new BigDecimal(d);
//                    BigDecimal bigDecimal10=new BigDecimal(difference[i1]);
//                    map.put(clockList.get(i1),bigDecimal9.add(bigDecimal10).doubleValue());
//                }
//                TwoClockLogicAction twoClockLogicAction1=new TwoClockLogicAction(suffix.get(i).getSymbol(),map);
//                twoClockLogicActionList.add(twoClockLogicAction1);
//                twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//
//                TwoClockLogicTimeWord logicTimeWord2=new TwoClockLogicTimeWord(null);
//                if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
//                    logicTimeWord2 = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
//                    logicTimeWord2.setTimedActions(TwoClockResetLogicTimeWord.logicTimeWord(prefix).getTimedActions());
//                    logicTimeWord2 = TwoClockLogicTimeWord.concat(logicTimeWord2, twoClockLogicTimeWord);
//                }
//                else {
//                    logicTimeWord2=twoClockLogicTimeWord;
//                }
//                TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=teacher.transferWord(logicTimeWord2);
//                twoClockResetLogicTimeWord=twoClockResetLogicTimeWord1;
//                twoClockResetLogicTimeWord.setTimedActions(twoClockResetLogicTimeWord1.getTimedActions());
//                for (int p=0;p<this.getClockList().size();p++){
//                    if (!twoClockResetLogicTimeWord1.getLastResetAction().isReset(this.getClockList().get(p))){
//                        difference[p]=twoClockResetLogicTimeWord1.getLastResetAction().getValue(this.getClockList().get(p));
//                    }
//                    else {
//                        difference[p]=0d;
//                    }
//                }
//            }
//
//
//
//
//
//            //  System.out.println(s.getModel());
////            if  (result== Status.SATISFIABLE)
//            {
//
//
//                //  model = s.getModel();
//
//
//            }
//
//        }
//        PairRegion pairRegion=new PairRegion(prefix,suffix);
//        pairRegion.setPrefix(prefix);
//        pairRegion.setSuffix(suffix);
//        pairRegionTwoClockResetLogicTimeWordMap.put(pairRegion,twoClockResetLogicTimeWord);
//        return twoClockResetLogicTimeWord;
//    }


    public TwoClockResetLogicTimeWord selectfromRegion(RegionTwoClockLogicTimedWord suffix,TwoClockResetLogicTimeWord prefix) {
//        selectfromregionc++;
//        System.out.println("selectfromregionc:"+selectfromregionc);
        TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=null;
        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
        double[] difference = new double[this.getClockList().size()];
        if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
            TwoClockResetLogicAction twoClockLogicAction = prefix.get(prefix.size() - 1);
            for (int p = 0; p < this.getClockList().size(); p++) {
                if (!twoClockLogicAction.isReset(this.getClockList().get(p))) {
                    difference[p] = twoClockLogicAction.getValue(this.getClockList().get(p));
                } else {
                    difference[p] = 0d;
                }

            }
        }
        for (int i = 0; i < suffix.size(); i++) {
            if(twoClockResetLogicTimeWord!=null&&twoClockResetLogicTimeWord.getTimedActions()!=null&&twoClockResetLogicTimeWord.getTimedActions().size()!=0){
                twoClockResetLogicTimeWord.setTimedActions(null);}
            double[] l = new double[clockList.size()];
            for (int m = 0; m < clockList.size(); m++) {
                if (suffix.get(i).getRegion().getTimeGuardList().get(m).isLowerBoundOpen()) {
                    BigDecimal bigDecimal=new BigDecimal(suffix.get(i).getRegion().getTimeGuardList().get(m).getLowerBound());
                    BigDecimal bigDecimal1=new BigDecimal(0.05);
                    l[m] = (bigDecimal.add(bigDecimal1).doubleValue());
                } else {
                    l[m] = suffix.get(i).getRegion().getTimeGuardList().get(m).getLowerBound();
                }
            }
            double[] current2=new double[clockList.size()];
       //     double[] current=iterate(l,0, suffix.get(i),difference,current2);
            BigDecimal b1 = new BigDecimal(Double.toString(difference[0]));
            BigDecimal b2 = new BigDecimal(Double.toString(difference[1]));
            double[] current=z3(b1.subtract(b2).doubleValue(),suffix.get(i).getRegion().getTimeGuardList().get(0).getLowerBound()*1.0,difference[0],suffix.get(i).getRegion().getTimeGuardList().get(0).getUpperBound()*1.0,suffix.get(i).getRegion().getTimeGuardList().get(1).getLowerBound()*1.0,difference[1],suffix.get(i).getRegion().getTimeGuardList().get(1).getUpperBound()*1.0,suffix.get(i).getRegion().getTimeGuardList().get(0).isLowerBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(0).isUpperBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(1).isLowerBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(1).isUpperBoundOpen());
            if(current==null){
                return null;
            }
            Map<Clock, Double> map = new HashMap<>();
            for (int k=0;k<clockList.size();k++) {
                map.put(clockList.get(k),current[k]);
            }
            TwoClockLogicAction twoClockLogicAction1 = new TwoClockLogicAction(suffix.get(i).getSymbol(), map);
            twoClockLogicActionList.add(twoClockLogicAction1);
            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
            TwoClockLogicTimeWord logicTimeWord2=new TwoClockLogicTimeWord(null);
            if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
                logicTimeWord2 = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
                logicTimeWord2.setTimedActions(TwoClockResetLogicTimeWord.logicTimeWord(prefix).getTimedActions());
                logicTimeWord2 = TwoClockLogicTimeWord.concat(logicTimeWord2, twoClockLogicTimeWord);
            }
            else {
                logicTimeWord2=twoClockLogicTimeWord;
            }
            TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=teacher.transferWord(logicTimeWord2);
            reset++;
            twoClockResetLogicTimeWord=twoClockResetLogicTimeWord1;
            twoClockResetLogicTimeWord.setTimedActions(twoClockResetLogicTimeWord1.getTimedActions());
            for (int p=0;p<this.getClockList().size();p++){
                if (!twoClockResetLogicTimeWord1.getLastResetAction().isReset(this.getClockList().get(p))){
                    difference[p]=twoClockResetLogicTimeWord1.getLastResetAction().getValue(this.getClockList().get(p));
                }
                else {
                    difference[p]=0d;
                }

            }
        }
        return twoClockResetLogicTimeWord;
    }

    public static List<TimeGuard> complementaryoneclock(List<TimeGuard> guardList,double LowerBound,boolean clock2isopen){
        List<TimeGuard> complementaryList = new ArrayList<>();

        if(guardList.isEmpty()){
            // System.out.println("guardList为空");
            TimeGuard guard = new TimeGuard(clock2isopen,true,(int)LowerBound, TimeGuard.MAX_TIME);
            complementaryList.add(guard);
            return complementaryList;
        }
        //  System.out.println("guardList"+guardList);
        guardList.sort(new TimeGuardComparator());

        for (int i = 0; i < guardList.size() - 1; i++) {
            for (int j = i + 1; j < guardList.size(); j++) {

                if (guardList.get(i).getLowerBound() == guardList.get(j).getLowerBound() &&
                        guardList.get(i).isLowerBoundOpen()&&!guardList.get(j).isLowerBoundOpen()) {
                    TimeGuard transition = new TimeGuard();
                    transition = guardList.get(i);
                    guardList.set(i, guardList.get(j));
                    guardList.set(j, transition);
                }
            }
        }

        TimeGuard pre = guardList.get(0);
        if( pre.getLowerBound() > LowerBound || pre.isLowerBoundOpen() ){
            TimeGuard guard = new TimeGuard(clock2isopen, pre.isLowerBoundClose(),(int)LowerBound,pre.getLowerBound());
            complementaryList.add(guard);
        }

        for(int i = 1; i < guardList.size(); i++){
            TimeGuard current = guardList.get(i);
            if(pre.getUpperBound() != current.getLowerBound() || (pre.isUpperBoundOpen() && current.isLowerBoundOpen())){
                TimeGuard guard = new TimeGuard(pre.isUpperBoundClose(), current.isLowerBoundClose(),
                        pre.getUpperBound() , current.getLowerBound());
                complementaryList.add(guard);
            }
            pre = current;
        }

        if(pre.getUpperBound() != TimeGuard.MAX_TIME ){
            TimeGuard guard = new TimeGuard(pre.isUpperBoundClose(),true,
                    pre.getUpperBound() , TimeGuard.MAX_TIME);
            complementaryList.add(guard);
        }

        List<TimeGuard> delete=new ArrayList<>();
        for (TimeGuard timeGuard:complementaryList){
            if(timeGuard.getUpperBound()<(int)LowerBound||
                    timeGuard.getUpperBound()==(int)LowerBound&&timeGuard.isUpperBoundOpen()||
                    timeGuard.getUpperBound()==(int)LowerBound&&clock2isopen){
                delete.add(timeGuard);
            }
        }
        complementaryList.removeAll(delete);
        for (TimeGuard timeGuard:complementaryList){
            if(timeGuard.getLowerBound()<(int)LowerBound||
                    timeGuard.getLowerBound()==(int)LowerBound&&!timeGuard.isLowerBoundOpen()&&clock2isopen){
                timeGuard.setLowerBound((int)LowerBound);
                timeGuard.setLowerBoundOpen(clock2isopen);
            }
        }

        delete=new ArrayList<>();
        for (TimeGuard timeGuard:complementaryList){
            if(timeGuard.getUpperBound()==timeGuard.getLowerBound()&&timeGuard.isLowerBoundOpen()||
                    timeGuard.getUpperBound()==timeGuard.getLowerBound()&&clock2isopen){
                delete.add(timeGuard);
            }
        }
        complementaryList.removeAll(delete);

        delete=new ArrayList<>();
        for (TimeGuard timeGuard:complementaryList){
            if(timeGuard.getUpperBound()<timeGuard.getLowerBound()){
                delete.add(timeGuard);
            }
        }
        complementaryList.removeAll(delete);

        return complementaryList;
    }

    public static List<TimeGuard> complementaryoneclockforoneclock(List<TimeGuard> guardList,double LowerBound,boolean clock2isopen){
        List<TimeGuard> complementaryList = new ArrayList<>();

        if(guardList.isEmpty()){
            // System.out.println("guardList为空");
            TimeGuard guard = new TimeGuard(clock2isopen,true,(int)LowerBound, TimeGuard.MAX_TIME);
            complementaryList.add(guard);
            return complementaryList;
        }
        System.out.println("guardList"+guardList);
        guardList.sort(new TimeGuardComparator());
        System.out.println("guardList sort"+guardList);
        TimeGuard pre = guardList.get(0);
        if( pre.getLowerBound() > LowerBound || pre.getLowerBound() == LowerBound&&pre.isLowerBoundOpen()&&!clock2isopen ){
            TimeGuard guard = new TimeGuard(clock2isopen, pre.isLowerBoundClose(),(int)LowerBound,pre.getLowerBound());
            complementaryList.add(guard);
        }

        for(int i = 1; i < guardList.size(); i++){
            TimeGuard current = guardList.get(i);
            if(pre.getUpperBound() != current.getLowerBound() || (pre.isUpperBoundOpen() && current.isLowerBoundOpen())){
                TimeGuard guard = new TimeGuard(pre.isUpperBoundClose(), current.isLowerBoundClose(),
                        pre.getUpperBound() , current.getLowerBound());
                complementaryList.add(guard);
            }
            pre = current;
        }

        if(pre.getUpperBound() != TimeGuard.MAX_TIME ){
            TimeGuard guard = new TimeGuard(pre.isUpperBoundClose(),true,
                    pre.getUpperBound() , TimeGuard.MAX_TIME);
            complementaryList.add(guard);
        }

        return complementaryList;
    }

    public static double[] sort(double X[]) {
        for(int i = 0;i < X.length;i++){
            for(int j = 0;j < X.length-i-1;j++){
                if(X[j] > X[j+1]){
                    double t = X[j];
                    X[j] = X[j+1];
                    X[j+1] = t;
                }
            }
        }
        return  X;
    }

    private static List<TaTransition> complementaryfirst(double clock1LowerBound, double clock2LowerBound,List<TaTransition> transitionList, TaLocation targetLocation, Clock clock1, Clock clock2,boolean isreset1,boolean isreset2,boolean clock1isopen,boolean clock2isopen) {
        List<TaTransition> complementaryTranList = new ArrayList<>();
        List<TaTransition> transitionsuse=new ArrayList<>();
        for (TaTransition transition:transitionList){
            BigDecimal bigDecimal=new BigDecimal(Double.toString(transition.getLowerBound(clock1)));
            BigDecimal bigDecimal2=new BigDecimal(Double.toString(transition.getUpperBound(clock1)));
            BigDecimal bigDecimal3=new BigDecimal(Double.toString(clock1LowerBound));
            if(bigDecimal2.subtract(bigDecimal3).doubleValue()<0){

            }
            else if(bigDecimal2.subtract(bigDecimal3).doubleValue()==0&&transition.getTimeGuard(clock1).isLowerBoundOpen()||
                    bigDecimal2.subtract(bigDecimal3).doubleValue()==0&&clock1isopen){

            }
            else {
                transitionsuse.add(transition);
            }

        }
        if(transitionsuse.size()==0){
            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
            clockTimeGuardMap.put(clock1, new TimeGuard(clock1isopen, true, (int)clock1LowerBound, TimeGuard.MAX_TIME));
            clockTimeGuardMap.put(clock2, new TimeGuard(clock2isopen, true, (int)clock2LowerBound, TimeGuard.MAX_TIME));
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
            Set<Clock> resetClocks = new HashSet<>();
            if (isreset1) {
                resetClocks.add(clock1);
            }
            if (isreset2) {
                resetClocks.add(clock2);
            }
            TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
            complementaryTranList.add(t);
            return complementaryTranList;
        }
        double X[] =new double[2*transitionsuse.size()+1];
        int i=0;
        for (TaTransition transition : transitionsuse) {

            X[i]=transition.getLowerBound(clock1);
            i++;

        }
        for (TaTransition transition : transitionsuse) {
            X[i]=transition.getUpperBound(clock1);
            i++;
        }
        X[2*transitionsuse.size()]=clock1LowerBound;
        X=sort(X);

        for (int m=0;m< X.length-1;m++) {
            if (m > 0 && X[m] == X[m - 1] || (int) (X[m]) == TimeGuard.MAX_TIME) {

            } else {
                if (clock1isopen != true && m == 0 || m != 0){
                    List<TimeGuard> timeGuardListy = new ArrayList<>();
                    for (TaTransition transition : transitionList) {
                        if (transition.getLowerBound(clock1) < X[m] && transition.getUpperBound(clock1) > X[m] ||
                                transition.getLowerBound(clock1) == X[m] && transition.getTimeGuard(clock1).isLowerBoundOpen() == false
                                || transition.getUpperBound(clock1) == X[m] && transition.getTimeGuard(clock1).isUpperBoundOpen() == false) {
                            timeGuardListy.add(transition.getTimeGuard(clock2));
                        }
                    }
         //           System.out.println("timeGuardListy:"+timeGuardListy);
                    List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy, clock2LowerBound, clock2isopen);
          //         System.out.println("complementaryGuardListy:"+complementaryGuardListy);
                    for (TimeGuard complementaryGuardy : complementaryGuardListy) {
                        Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                        clockTimeGuardMap.put(clock1, new TimeGuard(false, false, (int) (X[m]), (int) (X[m])));
                        clockTimeGuardMap.put(clock2, complementaryGuardy);
                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                        edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                        Set<Clock> resetClocks = new HashSet<>();
                        if (isreset1) {
                            resetClocks.add(clock1);
                        }
                        if (isreset2) {
                            resetClocks.add(clock2);
                        }
                        TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
                        complementaryTranList.add(t);
                    }
                }
            }
        }

        for (int m=0,n=1;m< X.length-1;m++,n++) {
            if(X[n]==X[m]){

            }
            else {
                List<TimeGuard> timeGuardListy = new ArrayList<>();
                for (TaTransition transition : transitionList) {
                    if (transition.getLowerBound(clock1) <= X[m] && transition.getUpperBound(clock1) >= X[n]) {
                        timeGuardListy.add(transition.getTimeGuard(clock2));
                    }
                }
                List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy,clock2LowerBound,clock2isopen);
//                System.out.println("timeGuardListy2:"+timeGuardListy);
//                System.out.println("complementaryGuardListy2:"+complementaryGuardListy);
                for (TimeGuard complementaryGuardy : complementaryGuardListy) {
                    Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                    clockTimeGuardMap.put(clock1, new TimeGuard(true, true, (int) (X[m]), (int) (X[n])));
                    clockTimeGuardMap.put(clock2, complementaryGuardy);
                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                    Set<Clock> resetClocks = new HashSet<>();
                    if(isreset1){
                        resetClocks.add(clock1);}
                    if(isreset2){
                        resetClocks.add(clock2);}
                    TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
                    complementaryTranList.add(t);
                }
            }
        }
        if(X[X.length-1]!=TimeGuard.MAX_TIME)  {



            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
            if(X[X.length-1]==X[X.length-2]) {
                clockTimeGuardMap.put(clock1, new TimeGuard(true, true, (int) (X[X.length - 1]), TimeGuard.MAX_TIME));
            }else {
                clockTimeGuardMap.put(clock1, new TimeGuard(false, true, (int) (X[X.length - 1]), TimeGuard.MAX_TIME));
            }
            clockTimeGuardMap.put(clock2, new TimeGuard(clock2isopen, true, (int)clock2LowerBound, TimeGuard.MAX_TIME));
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
            Set<Clock> resetClocks = new HashSet<>();
            if(isreset1){
                resetClocks.add(clock1);}
            if(isreset2){
                resetClocks.add(clock2);}
            TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
            complementaryTranList.add(t);
        }
        for (int k=0;k<complementaryTranList.size();k++){
            if(clock1isopen&&complementaryTranList.get(k).getLowerBound(clock1)==clock1LowerBound&&complementaryTranList.get(k).isReset(clock1)){
                complementaryTranList.get(k).getTimeGuard(clock1).setLowerBoundOpen(true);
                if (complementaryTranList.get(k).getLowerBound(clock1)==complementaryTranList.get(k).getUpperBound(clock1)&&
                        complementaryTranList.get(k).getTimeGuard(clock1).isLowerBoundOpen()==true&&
                        complementaryTranList.get(k).getTimeGuard(clock1).isUpperBoundOpen()==false){
                    complementaryTranList.remove(k);
                    k=k-1;
                }
            }
        }
        for (int k=0;k<complementaryTranList.size();k++){
            if (complementaryTranList.get(k).getLowerBound(clock2)>complementaryTranList.get(k).getUpperBound(clock2)||
                    complementaryTranList.get(k).getLowerBound(clock2)==complementaryTranList.get(k).getUpperBound(clock2)&&
                            complementaryTranList.get(k).getTimeGuard(clock2).isLowerBoundOpen()==true&&
                            complementaryTranList.get(k).getTimeGuard(clock2).isUpperBoundOpen()==false){
                complementaryTranList.remove(k);
                k=k-1;

            }
        }
        List<TaTransition> delete=new ArrayList<>();
        for (TaTransition transition:complementaryTranList){
            if(transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getLowerBound()<clock1LowerBound||
                    transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getLowerBound()==clock1LowerBound&&
                            !transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isLowerBoundOpen()&&clock1isopen){
                delete.add(transition);
            }
        }
        complementaryTranList.removeAll(delete);
        return complementaryTranList;
    }

private static List<TaTransition> complementary(double clock1LowerBound, double clock2LowerBound,List<TaTransition> transitionList, TaLocation targetLocation, Clock clock1, Clock clock2,boolean isreset1,boolean isreset2,boolean clock1isopen,boolean clock2isopen) {
   // System.out.println(transitionList);
        List<TaTransition> complementaryTranList = new ArrayList<>();
    double X[] =new double[2*transitionList.size()+1];
    int i=0;
    for (TaTransition transition : transitionList) {

        X[i]=transition.getLowerBound(clock1);
        i++;

    }
    for (TaTransition transition : transitionList) {
        X[i]=transition.getUpperBound(clock1);
        i++;
    }
    for (i=0;i<X.length;i++){
    }
    X[2*transitionList.size()]=clock1LowerBound;
    X=sort(X);
//    for (int k=0;k<X.length;k++){
//        System.out.println(X[k]);
//    }
//    System.out.println("X:"+X);
    for (int m=0;m< X.length-1;m++) {
        if (m > 0 && X[m] == X[m - 1] || (int) (X[m]) == TimeGuard.MAX_TIME) {

        } else {
            if (clock1isopen != true && m == 0 || m != 0){
                List<TimeGuard> timeGuardListy = new ArrayList<>();
            for (TaTransition transition : transitionList) {
//                System.out.println("边下的transition：" + transition);
//                System.out.println("X[m]:" + X[m]);
                if (transition.getLowerBound(clock1) < X[m] && transition.getUpperBound(clock1) > X[m] ||
                        transition.getLowerBound(clock1) == X[m] && transition.getTimeGuard(clock1).isLowerBoundOpen() == false
                        || transition.getUpperBound(clock1) == X[m] && transition.getTimeGuard(clock1).isUpperBoundOpen() == false) {
//                    System.out.println("X[m]:" + X[m]);
//                    System.out.println(transition.getTimeGuard(clock2).toString());
                    timeGuardListy.add(transition.getTimeGuard(clock2));
                }
            }
            //   List<TimeGuard> complementaryGuardListy = TimeGuardUtil.complementary(timeGuardListy);
            List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy, clock2LowerBound, clock2isopen);
//            System.out.println("点的timeGuardListy:" + timeGuardListy);
//            System.out.println("点的complementaryGuardListy:" + complementaryGuardListy);
            for (TimeGuard complementaryGuardy : complementaryGuardListy) {
                Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                clockTimeGuardMap.put(clock1, new TimeGuard(false, false, (int) (X[m]), (int) (X[m])));
                clockTimeGuardMap.put(clock2, complementaryGuardy);
                EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                Set<Clock> resetClocks = new HashSet<>();
                if (isreset1) {
                    resetClocks.add(clock1);
                }
                if (isreset2) {
                    resetClocks.add(clock2);
                }
                TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
                   System.out.println("t:" + t.toString());
                complementaryTranList.add(t);
            }
        }
    }
    }

    for (int m=0,n=1;m< X.length-1;m++,n++) {
        if(X[n]==X[m]){

        }
        else {
            List<TimeGuard> timeGuardListy = new ArrayList<>();
            for (TaTransition transition : transitionList) {
                if (transition.getLowerBound(clock1) <= X[m] && transition.getUpperBound(clock1) >= X[n]) {
//                    System.out.println("X[m]:"+X[m]);
//                    System.out.println("X[n]:"+X[n]);
                    timeGuardListy.add(transition.getTimeGuard(clock2));
                }
            }
          //  List<TimeGuard> complementaryGuardListy = TimeGuardUtil.complementary(timeGuardListy);
            List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy,clock2LowerBound,clock2isopen);
            for (TimeGuard complementaryGuardy : complementaryGuardListy) {
                Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                clockTimeGuardMap.put(clock1, new TimeGuard(true, true, (int) (X[m]), (int) (X[n])));
                clockTimeGuardMap.put(clock2, complementaryGuardy);
                EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                Set<Clock> resetClocks = new HashSet<>();
                if(isreset1){
                    resetClocks.add(clock1);}
                if(isreset2){
                    resetClocks.add(clock2);}
                TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
              //     System.out.println("t:" + t.toString());
                complementaryTranList.add(t);
            }
        }
    }

    for (int k=0;k<complementaryTranList.size();k++){
        if(clock1isopen&&complementaryTranList.get(k).getLowerBound(clock1)==clock1LowerBound&&complementaryTranList.get(k).isReset(clock1)){
            complementaryTranList.get(k).getTimeGuard(clock1).setLowerBoundOpen(true);
         //   System.out.println(complementaryTranList.get(k).toString());
            if (complementaryTranList.get(k).getLowerBound(clock1)==complementaryTranList.get(k).getUpperBound(clock1)&&
                    complementaryTranList.get(k).getTimeGuard(clock1).isLowerBoundOpen()==true&&
                    complementaryTranList.get(k).getTimeGuard(clock1).isUpperBoundOpen()==false){
                complementaryTranList.remove(k);
                k=k-1;
            }
        }
    }
    for (int k=0;k<complementaryTranList.size();k++){
            if (complementaryTranList.get(k).getLowerBound(clock2)>complementaryTranList.get(k).getUpperBound(clock2)||
                    complementaryTranList.get(k).getLowerBound(clock2)==complementaryTranList.get(k).getUpperBound(clock2)&&
                    complementaryTranList.get(k).getTimeGuard(clock2).isLowerBoundOpen()==true&&
                    complementaryTranList.get(k).getTimeGuard(clock2).isUpperBoundOpen()==false){
                complementaryTranList.remove(k);
                k=k-1;

        }
    }
//System.out.println("complementaryTranList:"+complementaryTranList);
    return complementaryTranList;
}

    private static List<TaTransition> complementary(List<TimeGuard> timeGuardList,List<TaTransition> transitionList, TaLocation targetLocation, List<Clock> clockList,Set<Clock> clockSet) {
        List<TaTransition> transitionList1=new ArrayList<>();
        List<Map<Clock,TimeGuard>> mapListLast=new ArrayList<>();
        for (int i=0;i<transitionList.size();i++){
            List<Map<Clock,TimeGuard>> mapList=new ArrayList<>();
            for (int j=0;j<transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().size();j++){
                if(transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getLowerBound()>timeGuardList.get(j).getLowerBound()||
                        transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getLowerBound()==timeGuardList.get(j).getLowerBound()&& transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).isLowerBoundOpen()&&timeGuardList.get(j).isLowerBoundClose()){
                    TimeGuard timeGuard = new TimeGuard(timeGuardList.get(j).isLowerBoundOpen(),!transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).isLowerBoundOpen(),timeGuardList.get(j).getLowerBound(),transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getLowerBound());
                    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
                    clockTimeGuardMap.put(clockList.get(j), timeGuard);
                    mapList.add(clockTimeGuardMap);
                }
            }
            if(mapList.size()==mapListLast.size()){
                boolean eq=true;
                for (int j=0;j<mapList.size();j++){
                    for (Clock clock:mapList.get(j).keySet()){
                      if(mapListLast.get(j).get(clock)==null||mapList.get(j).get(clock).getUpperBound()!=mapListLast.get(j).get(clock).getUpperBound()||
                              mapList.get(j).get(clock).getLowerBound()!=mapListLast.get(j).get(clock).getLowerBound()||
                              mapList.get(j).get(clock).isUpperBoundOpen()!=mapListLast.get(j).get(clock).isUpperBoundOpen()||
                              mapList.get(j).get(clock).isLowerBoundOpen()!=mapListLast.get(j).get(clock).isLowerBoundOpen()){
                          eq=false;
                      }
                    }
                }
                if(eq){
                    mapList.clear();
                }
            }
         if(mapList!=null&&mapList.size()!=0){
             mapListLast=mapList;
            if(transitionList1.size()!=0){
                List<TaTransition> transitionList2=new ArrayList<>();
                for (int p=0;p<transitionList1.size();p++){
                    for (int k=0;k<mapList.size();k++) {
                        EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
                        Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
                        for (Clock clock:transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().keySet()){
                            clockTimeGuardMap.put(clock,transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock));}
                        for (Clock clock:mapList.get(k).keySet()){
                            boolean sameclock=false;
                            for (Clock clock1:transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().keySet()){
                                if(clock1.equals(clock)){
                                    sameclock=true;
                                    int lower;
                                    boolean loweropen;
                                    if(mapList.get(k).get(clock).getLowerBound()<transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound()||
                                            mapList.get(k).get(clock).getLowerBound()==transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound()&&mapList.get(k).get(clock).isLowerBoundClose()&&transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isLowerBoundOpen()){
                                        lower=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound();
                                        loweropen=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isLowerBoundOpen();
                                    }
                                    else {
                                        lower=mapList.get(k).get(clock).getLowerBound();
                                        loweropen=mapList.get(k).get(clock).isLowerBoundOpen();
                                    }

                                    int upper;
                                    boolean upperopen;
                                    if(mapList.get(k).get(clock).getUpperBound()>transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound()||
                                            mapList.get(k).get(clock).getUpperBound()==transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound()&&mapList.get(k).get(clock).isUpperBoundClose()&&transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isUpperBoundOpen()){
                                        upper=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound();
                                        upperopen=transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isUpperBoundOpen();
                                    }
                                    else {
                                        upper=mapList.get(k).get(clock).getUpperBound();
                                        upperopen=mapList.get(k).get(clock).isUpperBoundOpen();
                                    }
                                    TimeGuard timeGuard=new TimeGuard(loweropen,upperopen,lower,upper);
                                    clockTimeGuardMap.put(clock,timeGuard);
                                }
                            }
                            if(!sameclock){
                                clockTimeGuardMap.put(clock,mapList.get(k).get(clock));
                            }
                        }
                        edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                        TaTransition taTransition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, clockSet);
                        transitionList2.add(taTransition);
                    }
                }
                transitionList1.clear();
                transitionList1=transitionList2;
            }
            else {
                for (int k = 0; k < mapList.size(); k++) {
                    EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
                    for (Clock clock : mapList.get(k).keySet())
                        for (int i1 = 0; i1 < clockList.size(); i1++) {
                            if (!clockList.get(i1).equals(clock)) {
                                TimeGuard timeGuard = new TimeGuard(timeGuardList.get(i1).isLowerBoundOpen(), true, timeGuardList.get(i1).getLowerBound(), TimeGuard.MAX_TIME);
                                mapList.get(k).put(clockList.get(i1), timeGuard);
                            }
                        }


                    edgeTimeGuard.setClockTimeGuardMap(mapList.get(k));
                    TaTransition taTransition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, clockSet);
                    transitionList1.add(taTransition);
                }
            }

            }

        }
    //    System.out.println("返回的transitionList1："+transitionList1);
        List<TaTransition> delete=new ArrayList<>();
        for (int i=0;i<transitionList1.size()-1;i++){
            for (int j=i+1;j<transitionList1.size();j++){
                boolean eq=true;
//                System.out.println(transitionList1.get(i));
//                System.out.println(transitionList1.get(j));
                for (int m=0;m<clockList.size();m++){
                    if(transitionList1.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).isLowerBoundOpen()!=transitionList1.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).isLowerBoundOpen()||
                            transitionList1.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).isUpperBoundOpen()!=transitionList1.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).isUpperBoundOpen()||
                            transitionList1.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).getLowerBound()!=transitionList1.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).getLowerBound()||
                            transitionList1.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).getUpperBound()!=transitionList1.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).getUpperBound()){
                     //   System.out.println("不相等");
                        eq=false;
                    }
                }
                if(eq){
                    delete.add(transitionList1.get(j));
                }
            }
        }
       // System.out.println("delete:"+delete);
        transitionList1.removeAll(delete);
    //    System.out.println("去重之后的transitionList1："+transitionList1);
        return transitionList1;
    }


//    private static List<TaTransition> complementary(double clock1LowerBound, double clock2LowerBound,List<TaTransition> transitionList, TaLocation targetLocation, Clock clock1, Clock clock2,boolean isreset1,boolean isreset2,boolean clock1isopen,boolean clock2isopen) {
//        // System.out.println(transitionList);
//        List<TaTransition> complementaryTranList = new ArrayList<>();
//        double X[] =new double[2*transitionList.size()+1];
//        int i=0;
//        for (TaTransition transition : transitionList) {
//
//            X[i]=transition.getLowerBound(clock1);
//            i++;
//
//        }
//        for (TaTransition transition : transitionList) {
//            X[i]=transition.getUpperBound(clock1);
//            i++;
//        }
//        for (i=0;i<X.length;i++){
//        }
//        X[2*transitionList.size()]=clock1LowerBound;
//        X=sort(X);
////    for (int k=0;k<X.length;k++){
////        System.out.println(X[k]);
////    }
//        for (int m=0;m< X.length-1;m++) {
//            if (m > 0 && X[m] == X[m - 1] || (int) (X[m]) == TimeGuard.MAX_TIME) {
//
//            } else {
//                if (clock1isopen != true && m == 0 || m != 0){
//                    List<TimeGuard> timeGuardListy = new ArrayList<>();
//                    for (TaTransition transition : transitionList) {
////                System.out.println("边下的transition：" + transition);
////                System.out.println("X[m]:" + X[m]);
//                        if (transition.getLowerBound(clock1) < X[m] && transition.getUpperBound(clock1) > X[m] ||
//                                transition.getLowerBound(clock1) == X[m] && transition.getTimeGuard(clock1).isLowerBoundOpen() == false
//                                || transition.getUpperBound(clock1) == X[m] && transition.getTimeGuard(clock1).isUpperBoundOpen() == false) {
////                    System.out.println("X[m]:" + X[m]);
////                    System.out.println(transition.getTimeGuard(clock2).toString());
//                            timeGuardListy.add(transition.getTimeGuard(clock2));
//                        }
//                    }
//                    //   List<TimeGuard> complementaryGuardListy = TimeGuardUtil.complementary(timeGuardListy);
//                    List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy, clock2LowerBound, clock2isopen);
////            System.out.println("点的timeGuardListy:" + timeGuardListy);
////            System.out.println("点的complementaryGuardListy:" + complementaryGuardListy);
//                    for (TimeGuard complementaryGuardy : complementaryGuardListy) {
//                        Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//                        clockTimeGuardMap.put(clock1, new TimeGuard(false, false, (int) (X[m]), (int) (X[m])));
//                        clockTimeGuardMap.put(clock2, complementaryGuardy);
//                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//                        edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//                        Set<Clock> resetClocks = new HashSet<>();
//                        if (isreset1) {
//                            resetClocks.add(clock1);
//                        }
//                        if (isreset2) {
//                            resetClocks.add(clock2);
//                        }
//                        TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
//                        //   System.out.println("t:" + t.toString());
//                        complementaryTranList.add(t);
//                    }
//                }
//            }
//        }
//
//        for (int m=0,n=1;m< X.length-1;m++,n++) {
//            if(X[n]==X[m]){
//
//            }
//            else {
//                List<TimeGuard> timeGuardListy = new ArrayList<>();
//                for (TaTransition transition : transitionList) {
//                    if (transition.getLowerBound(clock1) <= X[m] && transition.getUpperBound(clock1) >= X[n]) {
////                    System.out.println("X[m]:"+X[m]);
////                    System.out.println("X[n]:"+X[n]);
//                        timeGuardListy.add(transition.getTimeGuard(clock2));
//                    }
//                }
//                //  List<TimeGuard> complementaryGuardListy = TimeGuardUtil.complementary(timeGuardListy);
//                List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy,clock2LowerBound,clock2isopen);
////            System.out.println("区间的timeGuardListy:" + timeGuardListy);
////            System.out.println("区间的complementaryGuardListy:" + complementaryGuardListy);
//                for (TimeGuard complementaryGuardy : complementaryGuardListy) {
//                    Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//                    clockTimeGuardMap.put(clock1, new TimeGuard(true, true, (int) (X[m]), (int) (X[n])));
//                    clockTimeGuardMap.put(clock2, complementaryGuardy);
//                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//                    Set<Clock> resetClocks = new HashSet<>();
//                    if(isreset1){
//                        resetClocks.add(clock1);}
//                    if(isreset2){
//                        resetClocks.add(clock2);}
//                    TaTransition t = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClocks);
//                    //    System.out.println("t:" + t.toString());
//                    complementaryTranList.add(t);
//                }
//            }
//        }
//        for (int k=0;k<complementaryTranList.size();k++){
////        System.out.println(complementaryTranList.get(k).toString());
////        System.out.println(complementaryTranList.get(k).isReset(clock1));
////        System.out.println(complementaryTranList.get(k).isReset(clock2));
////        System.out.println(clock1isopen);
//            if(clock1isopen&&complementaryTranList.get(k).getLowerBound(clock1)==clock1LowerBound&&complementaryTranList.get(k).isReset(clock1)){
//                complementaryTranList.get(k).getTimeGuard(clock1).setLowerBoundOpen(true);
//                //   System.out.println(complementaryTranList.get(k).toString());
//                if (complementaryTranList.get(k).getLowerBound(clock1)==complementaryTranList.get(k).getUpperBound(clock1)&&
//                        complementaryTranList.get(k).getTimeGuard(clock1).isLowerBoundOpen()==true&&
//                        complementaryTranList.get(k).getTimeGuard(clock1).isUpperBoundOpen()==false){
//                    complementaryTranList.remove(k);
//                    k=k-1;
//                }
//            }
//        }
//        for (int k=0;k<complementaryTranList.size();k++){
////        System.out.println(complementaryTranList.get(k).toString());
////        System.out.println(complementaryTranList.get(k).isReset(clock1));
////        System.out.println(complementaryTranList.get(k).isReset(clock2));
////        System.out.println(clock1isopen);
//
//            //   System.out.println(complementaryTranList.get(k).toString());
//            if (complementaryTranList.get(k).getLowerBound(clock2)>complementaryTranList.get(k).getUpperBound(clock2)||
//                    complementaryTranList.get(k).getLowerBound(clock2)==complementaryTranList.get(k).getUpperBound(clock2)&&
//                            complementaryTranList.get(k).getTimeGuard(clock2).isLowerBoundOpen()==true&&
//                            complementaryTranList.get(k).getTimeGuard(clock2).isUpperBoundOpen()==false){
//                complementaryTranList.remove(k);
//                k=k-1;
//
//            }
//        }
////System.out.println("complementaryTranList:"+complementaryTranList);
//        return complementaryTranList;
//    }


    public List<TaTransition> resttransition(TaTransition taTransition,TaTransition taTransitionchild){
    List<TaTransition> complementaryTranList=new ArrayList<>();
    List<TaTransition> transitionList1=new ArrayList<>();
        List<Map<Clock,TimeGuard>> mapList=new ArrayList<>();
        for (int j=0;j<taTransitionchild.getClockTimeGuardMap().getClockTimeGuardMap().size();j++){
            Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
            if(taTransition.getClockTimeGuardMap().getClockTimeGuardMap().get(j).getUpperBound()>(taTransitionchild.getClockTimeGuardMap().getClockTimeGuardMap().get(j).getLowerBound()+1)||
                    taTransition.getClockTimeGuardMap().getClockTimeGuardMap().get(j).getUpperBound()==(taTransitionchild.getClockTimeGuardMap().getClockTimeGuardMap().get(j).getLowerBound()+1)&&
                        !taTransition.getClockTimeGuardMap().getClockTimeGuardMap().get(j).isLowerBoundOpen()){

                TimeGuard timeGuard = new TimeGuard(false, taTransition.getClockTimeGuardMap().getClockTimeGuardMap().get(j).isUpperBoundOpen(), taTransitionchild.getClockTimeGuardMap().getClockTimeGuardMap().get(j).getLowerBound()+1, taTransition.getClockTimeGuardMap().getClockTimeGuardMap().get(j).getUpperBound());
                clockTimeGuardMap.put(clockList.get(j), timeGuard);
                mapList.add(clockTimeGuardMap);
            }
        }

    for (int k=0;k<mapList.size();k++) {
        EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
        edgeTimeGuard.setClockTimeGuardMap(mapList.get(k));
        Set<Clock> clockSet = new HashSet<>();
        for (int n = 0; n < clockList.size(); n++) {
            clockSet.add(clockList.get(n));
        }
        TaTransition taTransitionnew = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, clockSet);
        transitionList1.add(taTransitionnew);
    }
//        if(transitionList1.size()!=0){
//            List<TaTransition> transitionList2=new ArrayList<>();
//            for (int p=0;p<transitionList1.size();p++){
//                for (int k=0;k<mapList.size();k++) {
//                    EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
//                    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
//                    for (Clock clock:transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().keySet()){
//                        clockTimeGuardMap.put(clock,transitionList1.get(p).getClockTimeGuardMap().getClockTimeGuardMap().get(clock));}
//                    for (Clock clock:mapList.get(k).keySet()){
//                        clockTimeGuardMap.put(clock,mapList.get(k).get(clock));}
//                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//                    Set<Clock> clockSet = new HashSet<>();
//                    for (int n = 0; n < clockList.size(); n++) {
//                        clockSet.add(clockList.get(n));
//                    }
//                    TaTransition taTransition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, clockSet);
//                    transitionList2.add(taTransition);
//                }
//            }
//            transitionList1.clear();
//            transitionList1=transitionList2;
//        }
//        else {
//            for (int k=0;k<mapList.size();k++) {
//                EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
//                edgeTimeGuard.setClockTimeGuardMap(mapList.get(k));
//                Set<Clock> clockSet = new HashSet<>();
//                for (int n = 0; n < clockList.size(); n++) {
//                    clockSet.add(clockList.get(n));
//                }
//                TaTransition taTransition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, clockSet);
//                transitionList1.add(taTransition);
//            }
//
//
//        }

//    return complementaryTranList;
    return transitionList1;

//        List<TaTransition> transitionList=new ArrayList<>();
//        if(taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()>(lowerboundvalue2+1)&&taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()>(lowerboundvalue1+1)||
//        taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()==(lowerboundvalue2+1)&&
//                !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isLowerBoundOpen()&&taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()>(lowerboundvalue1+1)
//        ||taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()>(lowerboundvalue2+1)&&taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()==(lowerboundvalue1+1)&&
//                !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isLowerBoundOpen()||
//                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()==(lowerboundvalue2+1)&&
//                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isLowerBoundOpen()&&taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()==(lowerboundvalue1+1)&&
//                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isLowerBoundOpen()){
//            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//            clockTimeGuardMap.put(clock1, new TimeGuard(lowerboundopen1, true, lowerboundvalue1, lowerboundvalue1+1));
//            clockTimeGuardMap.put(clock2, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen(), lowerboundvalue2+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()));
//            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//            Set<Clock> resetClocks = new HashSet<>();
//            if (taTransition.isReset(clock1)) {
//                resetClocks.add(clock1);
//            }
//            if (taTransition.isReset(clock2)) {
//                resetClocks.add(clock2);
//            }
//            TaTransition t = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, resetClocks);
//
//            transitionList.add(t);
//
//            Map<Clock, TimeGuard> clockTimeGuardMap2 = new HashMap<>();
//            clockTimeGuardMap2.put(clock1, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen(), lowerboundvalue1+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()));
//            clockTimeGuardMap2.put(clock2, new TimeGuard(lowerboundopen2, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen(), lowerboundvalue2, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()));
//            EdgeTimeGuard edgeTimeGuard2=new EdgeTimeGuard();
//            edgeTimeGuard2.setClockTimeGuardMap(clockTimeGuardMap2);
//            TaTransition t2 = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard2, resetClocks);
//            transitionList.add(t2);
//
//            return transitionList;
//        }
//        else if(taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()>(lowerboundvalue2+1)||
//                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()==(lowerboundvalue2+1)&&
//                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isLowerBoundOpen()){
//            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//            clockTimeGuardMap.put(clock1, new TimeGuard(lowerboundopen1, true, lowerboundvalue1, lowerboundvalue1+1));
//            clockTimeGuardMap.put(clock2, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen(), lowerboundvalue2+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()));
//            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//            Set<Clock> resetClocks = new HashSet<>();
//            if (taTransition.isReset(clock1)) {
//                resetClocks.add(clock1);
//            }
//            if (taTransition.isReset(clock2)) {
//                resetClocks.add(clock2);
//            }
//            TaTransition t = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, resetClocks);
//
//            transitionList.add(t);
//            return transitionList;
//        }
//
//        else if(taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()>(lowerboundvalue1+1)||
//                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()==(lowerboundvalue1+1)&&
//                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isLowerBoundOpen()){
//            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//            clockTimeGuardMap.put(clock2, new TimeGuard(lowerboundopen2, true, lowerboundvalue2, lowerboundvalue2+1));
//            clockTimeGuardMap.put(clock1, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen(), lowerboundvalue1+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()));
//            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//            Set<Clock> resetClocks = new HashSet<>();
//            if (taTransition.isReset(clock1)) {
//                resetClocks.add(clock1);
//            }
//            if (taTransition.isReset(clock2)) {
//                resetClocks.add(clock2);
//            }
//            TaTransition t = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, resetClocks);
//
//            transitionList.add(t);
//            return transitionList;
//        }
//
//return transitionList;

}


    public List<TaTransition> resttransition(TaTransition taTransition,boolean lowerboundopen1,int lowerboundvalue1,boolean lowerboundopen2,int lowerboundvalue2,Clock clock1,Clock clock2){
        List<TaTransition> transitionList=new ArrayList<>();
        if(taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()>(lowerboundvalue2+1)&&
                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()>(lowerboundvalue1+1)

                ||
                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()==(lowerboundvalue2+1)&&
                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen()
                        &&taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()>(lowerboundvalue1+1)

                ||taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()>(lowerboundvalue2+1)&&
                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()==(lowerboundvalue1+1)&&
                !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen()

                ||
                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()==(lowerboundvalue2+1)&&
                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen()&&
                        taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()==(lowerboundvalue1+1)&&
                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen()){
            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
            clockTimeGuardMap.put(clock1, new TimeGuard(lowerboundopen1, true, lowerboundvalue1, lowerboundvalue1+1));
            clockTimeGuardMap.put(clock2, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen(), lowerboundvalue2+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()));
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
            Set<Clock> resetClocks = new HashSet<>();
            if (taTransition.isReset(clock1)) {
                resetClocks.add(clock1);
            }
            if (taTransition.isReset(clock2)) {
                resetClocks.add(clock2);
            }
            TaTransition t = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, resetClocks);

            transitionList.add(t);

            Map<Clock, TimeGuard> clockTimeGuardMap2 = new HashMap<>();
            clockTimeGuardMap2.put(clock1, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen(), lowerboundvalue1+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()));
            clockTimeGuardMap2.put(clock2, new TimeGuard(lowerboundopen2, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen(), lowerboundvalue2, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()));
            EdgeTimeGuard edgeTimeGuard2=new EdgeTimeGuard();
            edgeTimeGuard2.setClockTimeGuardMap(clockTimeGuardMap2);
            TaTransition t2 = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard2, resetClocks);
            transitionList.add(t2);

            return transitionList;
        }
        else if(taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()>(lowerboundvalue2+1)||
                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()==(lowerboundvalue2+1)&&
                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen()){
            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
            clockTimeGuardMap.put(clock1, new TimeGuard(lowerboundopen1, true, lowerboundvalue1, lowerboundvalue1+1));
            clockTimeGuardMap.put(clock2, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isUpperBoundOpen(), lowerboundvalue2+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getUpperBound()));
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
            Set<Clock> resetClocks = new HashSet<>();
            if (taTransition.isReset(clock1)) {
                resetClocks.add(clock1);
            }
            if (taTransition.isReset(clock2)) {
                resetClocks.add(clock2);
            }
            TaTransition t = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, resetClocks);

            transitionList.add(t);
            return transitionList;
        }

        else if(taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()>(lowerboundvalue1+1)||
                taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()==(lowerboundvalue1+1)&&
                        !taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen()){
            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
            clockTimeGuardMap.put(clock2, new TimeGuard(lowerboundopen2, true, lowerboundvalue2, lowerboundvalue2+1));
            clockTimeGuardMap.put(clock1, new TimeGuard(false, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isUpperBoundOpen(), lowerboundvalue1+1, taTransition.getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getUpperBound()));
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
            Set<Clock> resetClocks = new HashSet<>();
            if (taTransition.isReset(clock1)) {
                resetClocks.add(clock1);
            }
            if (taTransition.isReset(clock2)) {
                resetClocks.add(clock2);
            }
            TaTransition t = new TaTransition(taTransition.getSourceLocation(), taTransition.getTargetLocation(), taTransition.getSymbol(), edgeTimeGuard, resetClocks);

            transitionList.add(t);
            return transitionList;
        }

        return transitionList;

    }
//    private void evidenceToDOTA(TwoClockTA dota,List<TaLocation> regionsplit) {
//
//        List<TaTransition> Totaltransitions=new ArrayList<>();
//        List<TaTransition> newregionbypartition=new ArrayList<>();
//        for (TaLocation l : dota.getLocations()) {
//            List<TaTransition> transitionList = new ArrayList<>();
//            for (String action : dota.getSigma()) {
//                List<TaTransition> transitionListsigma = new ArrayList<>();
//                List<TaTransition> transitionListsigmapre = new ArrayList<>();
//                List<TaTransition> transitions = dota.getTransitions(l, action, null);
//                transitions.sort(new OTATranComparator(clockList.get(0)));
//
//                for (int k = 0; k < clockList.size() - 1; k++) {
//                    for (int i = 0; i < transitions.size() - 1; i++) {
//                        for (int j = i + 1; j < transitions.size(); j++) {
//                            if (transitions.get(i).getLowerBound(clockList.get(k)) == transitions.get(j).getLowerBound(clockList.get(k)) &&
//                                    transitions.get(i).getTimeGuard(clockList.get(k)).isLowerBoundOpen() == transitions.get(j).getTimeGuard(clockList.get(k)).isLowerBoundOpen()) {
//                                if (transitions.get(i).getLowerBound(clockList.get(k + 1)) > transitions.get(j).getLowerBound(clockList.get(k + 1)) ||
//                                        transitions.get(i).getLowerBound(clockList.get(k + 1)) == transitions.get(j).getLowerBound(clockList.get(k + 1)) && !transitions.get(i).getTimeGuard(clockList.get(k + 1)).isLowerBoundClose() && transitions.get(j).getTimeGuard(clockList.get(k + 1)).isLowerBoundClose()) {
//                                    TaTransition transition = new TaTransition();
//                                    transition = transitions.get(i);
//                                    transitions.set(i, transitions.get(j));
//                                    transitions.set(j, transition);
//                                }
//                            }
//                        }
//                    }
//                }
//                for (TaTransition transition : transitions) {
//                    for (Clock clock : clockList) {
//                        if (transition.getTimeGuard(clock).getLowerBound() > kc) {
//                            List<TimeGuard> subguard1timeGuardList = new ArrayList<>();
//                            for (Clock clockall : clockList) {
//                                if (transition.getTimeGuard(clockall).getLowerBound() > kc) {
//                                    TimeGuard subguard1clock = new TimeGuard(true, true, kc, TimeGuard.MAX_TIME);
//                                    subguard1timeGuardList.add(subguard1clock);
//                                } else if (!transition.getTimeGuard(clockall).isLowerBoundOpen()) {
//                                    TimeGuard subguard1clock = new TimeGuard(false, false, transition.getTimeGuard(clockall).getLowerBound(), transition.getTimeGuard(clockall).getLowerBound());
//                                    subguard1timeGuardList.add(subguard1clock);
//                                } else if (transition.getTimeGuard(clockall).isLowerBoundOpen()) {
//                                    TimeGuard subguard1clock = new TimeGuard(true, true, transition.getTimeGuard(clockall).getLowerBound(), transition.getTimeGuard(clockall).getLowerBound() + 1);
//                                    subguard1timeGuardList.add(subguard1clock);
//                                }
//                            }
//
//                            Map<Clock, TimeGuard> map1 = new HashMap<>();
//                            for (int m = 0; m < clockList.size(); m++) {
//                                map1.put(clockList.get(m), subguard1timeGuardList.get(m));
//                            }
//                            EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
//                            edgeTimeGuard.setClockTimeGuardMap(map1);
//                            TaTransition transition2 = new TaTransition();
//                            transition2.setClockTimeGuardMap(edgeTimeGuard);
//                            transition2.setTargetLocation(transition.getTargetLocation());
//                            transition2.setSourceLocation(transition.getSourceLocation());
//                            transition2.setSymbol(transition.getSymbol());
//                            transition2.setResetClockSet(transition.getResetClockSet());
//                            transitionListsigma.add(transition2);
//                            transitionListsigmapre.add(transition2);
//                            ;
//                        }
//                    }
//                }
//                Map<Integer, Region> idtoregion = new HashMap<>();
////                for (int i = 0; i < transitions.size(); i++) {
////                    if (!transitionListsigma.contains(transitions.get(i))) {
////                        List<TimeGuard> timeGuardList = new ArrayList();
////                        for (Clock clock : clockList) {
////                            timeGuardList.add(transitions.get(i).getEdgeTimeGuard().getClockTimeGuardMap().get(clock));
////                        }
////                        int q = 0;
////                        for (int m = 1; m < clockList.size(); m++) {
////                            q += clockList.size() - m;
////                        }
////                        int[] differList = new int[q];
////                        for (int m = 0; m < clockList.size() - 1; m++) {
////                            for (int n = m + 1; n < clockList.size(); n++) {
////                                if (!transitions.get(i).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff() &&
////                                        transitions.get(i).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()) {
////                                    int index = 0;
////                                    for (int k = 1; k < m + 1; k++) {
////                                        index += clockList.size() - k;
////                                    }
////                                    differList[index - 1 + n - m] = 1;
////                                }
////                                if (transitions.get(i).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff() &&
////                                        !transitions.get(i).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()) {
////                                    int index = 0;
////                                    for (int k = 1; k < m + 1; k++) {
////                                        index += clockList.size() - k;
////                                    }
////                                    differList[index - 1 + n - m] = -1;
////                                }
////                            }
////                        }
////                        Region region = new Region(timeGuardList, differList);
////                        idtoregion.put(i, region);
////                    }
////                }
//                List<TaTransition> repeattrans = new ArrayList<>();
//                for (int i = transitions.size() - 2; i >= 0; i--) {//find min transition in a set containing same transitions
//                    if (i >= 1 && transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() == transitions.get(i - 1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() &&
//                            transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == transitions.get(i - 1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() &&
//                            transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == transitions.get(i - 1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() &&
//                            transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundClose() == transitions.get(i - 1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundClose()) {
//
//                    } else {
//                        repeattrans.add(transitions.get(i));
//                        for (int j = i + 1; j < transitions.size(); j++) {//find the set containing same transitions
//                            if (transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() && transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen() &&
//                                    transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() &&
//                                    transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() &&
//                                    transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() &&
//                                    transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen()) {
//                                repeattrans.add(transitions.get(j));
//                            }
//                        }
//                    }
//                }
//                for (int m = 0; m < repeattrans.size() - 1; m++) {//delete same region
//                    for (int n = m + 1; n < repeattrans.size(); n++) {
//                        if (repeattrans.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff() == repeattrans.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff() &&
//                                repeattrans.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff() == repeattrans.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()) {
//                            repeattrans.remove(repeattrans.get(n));
//                            n = n - 1;
//                        }
//                    }
//                }
//                for (int m = 0; m < repeattrans.size() - 1; m++) {//delete same region
//                    boolean remove = true;
//                    for (int n = m + 1; n < repeattrans.size(); n++) {
//                        if (repeattrans.get(m).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() == repeattrans.get(n).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() &&
//                                repeattrans.get(m).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == repeattrans.get(n).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() &&
//                                repeattrans.get(m).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == repeattrans.get(n).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() &&
//                                repeattrans.get(m).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundClose() == repeattrans.get(n).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundClose()) {
//                            remove = false;
//                            break;
//                        }
//                    }
//                    if (remove) {
//                        repeattrans.remove(repeattrans.get(m));
//                        m = m - 1;
//                    }
//                }
//                for (int i1 = transitions.size() - 1; i1 >= 0; i1--) {
//                    if(!repeattrans.contains(transitions.get(i1))){
//                    if (i1 == transitions.size() - 1 && transitionListsigma.size() == 0) {
//                        List<TimeGuard> timeGuardList = new ArrayList<>();
//                        for (int m = 0; m < clockList.size(); m++) {
//                            TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
//                            timeGuardList.add(timeGuard);
//                        }
//                        List<TimeGuard> subguard1timeGuardList = new ArrayList<>();
//                        for (int m = 0; m < clockList.size(); m++) {
//                            TimeGuard subguard1clock = new TimeGuard(timeGuardList.get(m).isLowerBoundOpen(), true, timeGuardList.get(m).getLowerBound(), TimeGuard.MAX_TIME);
//                            subguard1timeGuardList.add(subguard1clock);
//                        }
//                        Map<Clock, TimeGuard> map1 = new HashMap<>();
//                        for (int m = 0; m < clockList.size(); m++) {
//                            map1.put(clockList.get(m), subguard1timeGuardList.get(m));
//                        }
//                        EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
//                        edgeTimeGuard.setClockTimeGuardMap(map1);
//                        TaTransition transition = new TaTransition();
//                        transition.setClockTimeGuardMap(edgeTimeGuard);
//                        transition.setTargetLocation(transitions.get(i1).getTargetLocation());
//                        transition.setSourceLocation(transitions.get(i1).getSourceLocation());
//                        transition.setSymbol(transitions.get(i1).getSymbol());
//                        transition.setResetClockSet(transitions.get(i1).getResetClockSet());
//                        transitionListsigma.add(transition);
//                        transitionListsigmapre.add(transition);
//                    } else {
//                        if (i1 >= transitions.size() - transitionListsigma.size() - 1) {
//                            List<TimeGuard> timeGuardList = new ArrayList<>();
//                            for (int m = 0; m < clockList.size(); m++) {
//                                TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
//                                timeGuardList.add(timeGuard);
//                            }
//                            if (clockList.size() == 2) {
//                                TimeGuard timeGuard1 = transitions.get(i1).getTimeGuard(clockList.get(0));
//                                TimeGuard timeGuard2 = transitions.get(i1).getTimeGuard(clockList.get(1));
//                                transitionListsigma.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
//                                transitionListsigmapre.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
//
//                            }
////                            if (clockList.size() == 1) {
////                                //transitionListsigma.addAll(complementary(timeGuardList,transitionListsigma, transitions.get(i1).getTargetLocation(), clockList,transitions.get(i1).getResetClockSet()));
////                                List<TimeGuard> list = new ArrayList<>();
////                                transitionListsigma.addAll(complementaryoneclockforoneclock(list, transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen()));
////                                transitionListsigmapre.addAll(complementaryoneclockforoneclock(list, transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen()));
////
////                            }
//                        } else {
//                            List<TimeGuard> timeGuardList = new ArrayList<>();
//                            for (int m = 0; m < clockList.size(); m++) {
//                                TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
//
//                                timeGuardList.add(timeGuard);
//                            }
//
//                            if (clockList.size() == 2) {
//                                TimeGuard timeGuard1 = transitions.get(i1).getTimeGuard(clockList.get(0));
//                                TimeGuard timeGuard2 = transitions.get(i1).getTimeGuard(clockList.get(1));
//                                transitionListsigma.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
//                                transitionListsigmapre.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
//
//                            }
//                            if (clockList.size() == 1) {
//                                transitionListsigma.addAll(complementary(timeGuardList, transitionListsigma, transitions.get(i1).getTargetLocation(), clockList, transitions.get(i1).getResetClockSet()));
//                                transitionListsigmapre.addAll(complementary(timeGuardList, transitionListsigma, transitions.get(i1).getTargetLocation(), clockList, transitions.get(i1).getResetClockSet()));
//
//                            }
//                        }
//                    }
//                }
//                    else {
//                        if (i1 == transitions.size() - 1 && transitionListsigma.size() == 0) {
//                            List<TimeGuard> timeGuardList = new ArrayList<>();
//                            for (int m = 0; m < clockList.size(); m++) {
//                                TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
//                                timeGuardList.add(timeGuard);
//                            }
//                            List<TimeGuard> subguard1timeGuardList = new ArrayList<>();
//                            for (int m = 0; m < clockList.size(); m++) {
//                                TimeGuard subguard1clock = new TimeGuard(timeGuardList.get(m).isLowerBoundOpen(), true, timeGuardList.get(m).getLowerBound(), TimeGuard.MAX_TIME);
//                                subguard1timeGuardList.add(subguard1clock);
//                            }
//                            Map<Clock, TimeGuard> map1 = new HashMap<>();
//                            for (int m = 0; m < clockList.size(); m++) {
//                                map1.put(clockList.get(m), subguard1timeGuardList.get(m));
//                            }
//                            EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
//                            edgeTimeGuard.setClockTimeGuardMap(map1);
//                            TaTransition transition = new TaTransition();
//                            transition.setClockTimeGuardMap(edgeTimeGuard);
//                            transition.setTargetLocation(transitions.get(i1).getTargetLocation());
//                            transition.setSourceLocation(transitions.get(i1).getSourceLocation());
//                            transition.setSymbol(transitions.get(i1).getSymbol());
//                            transition.setResetClockSet(transitions.get(i1).getResetClockSet());
//                            transitionListsigma.add(transition);
//                            transitionListsigmapre.add(transition);
//                        } else {
//                                List<TimeGuard> timeGuardList = new ArrayList<>();
//                                for (int m = 0; m < clockList.size(); m++) {
//                                    TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
//                                    timeGuardList.add(timeGuard);
//                                }
//                                if (clockList.size() == 2) {
//                                    TimeGuard timeGuard1 = transitions.get(i1).getTimeGuard(clockList.get(0));
//                                    TimeGuard timeGuard2 = transitions.get(i1).getTimeGuard(clockList.get(1));
//                                    transitionListsigma.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
//                                    transitionListsigmapre.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
//                                }
////                                if (clockList.size() == 1) {
////                                    //transitionListsigma.addAll(complementary(timeGuardList,transitionListsigma, transitions.get(i1).getTargetLocation(), clockList,transitions.get(i1).getResetClockSet()));
////                                    List<TimeGuard> list = new ArrayList<>();
////                                    transitionListsigma.addAll(complementaryoneclockforoneclock(list, transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen()));
////                                    transitionListsigmapre.addAll(complementaryoneclockforoneclock(list, transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen()));
////
////                                }
//                                for (int k=0; k<transitionListsigma.size();k++) {
//                                    if (transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen() &&
//                                            transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() == transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound() &&
//                                            transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen() == transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen() &&
//                                            transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == transitions.get(i1).getTimeGuard(clockList.get(1)).getLowerBound()) {
//                                        transitionListsigma.addAll(resttransition(transitionListsigma.get(k), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(),
//                                                transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).getLowerBound(), dota.getClockList().get(0), dota.getClockList().get(1)));
//                                        transitionListsigma.remove(transitionListsigma.get(k));
//                                        k=k-1;
//                                    }
//                                }
//                                List<TaTransition> transitionListrepeat=new ArrayList<>();
//                                for (TaTransition transition:repeattrans){
//                                    for (Clock clock:clockList){
//                                        if(transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound()==transitions.get(i1).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getLowerBound()&&
//                                                transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound()==transitions.get(i1).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).getUpperBound()&&
//                                            transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isLowerBoundOpen()==transitions.get(i1).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isLowerBoundOpen()&&
//                                                transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isUpperBoundClose()==transitions.get(i1).getClockTimeGuardMap().getClockTimeGuardMap().get(clock).isUpperBoundClose()){
//                                            transitionListsigma.add(transition);
//                                            transitionListrepeat.add(transition);
//                                        }
//                                    }
//                                }
//
//                                boolean upregion=false;
//                                boolean middleregion=false;
//                                boolean downregion=false;
//
//                                if(transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&!transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
//                                    downregion=true;
//                                }
//                                if(!transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
//                                    upregion=true;
//                                }
//                                if(transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
//                                    middleregion=true;
//                                }
//
//                                if(transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&!transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
//                                    downregion=true;
//                                }
//                                if(!transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
//                                    upregion=true;
//                                }
//                                if(transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
//                                    middleregion=true;
//                                }
//
//                                if(!upregion){
//                                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//                                    edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
//                                    List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
//                                    DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
//                                    differenceEdgeTimeGuard.setLargerclock(true);
//                                    differenceEdgeTimeGuard.setClock1(clockList.get(0));
//                                    differenceEdgeTimeGuard.setClock2(clockList.get(1));
//                                    differenceEdgeTimeGuard.setHaveupperdiff(true);
//                                    differenceEdgeTimeGuard.setDifferencexyupper(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
//                                    differenceEdgeTimeGuard.setIsequalupper(false);
//                                    differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
//                                    edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
//                                    Set<Clock> resetClockSet = new HashSet<>();
//                                    if (transitionListrepeat.get(0).isReset(clockList.get(0))) {
//                                        resetClockSet.add(clockList.get(0));
//                                    }
//                                    if (transitionListrepeat.get(0).isReset(clockList.get(1))) {
//                                        resetClockSet.add(clockList.get(1));
//                                    }
//                                    TaTransition transition = new TaTransition(
//                                            transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
//                                    transitionListsigma.add(transition);
//                                }
//                                if(!middleregion){
//                                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//                                    edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
//                                    List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
//                                    DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
//                                    differenceEdgeTimeGuard.setLargerclock(true);
//                                    differenceEdgeTimeGuard.setClock1(clockList.get(0));
//                                    differenceEdgeTimeGuard.setClock2(clockList.get(1));
//                                    differenceEdgeTimeGuard.setLargerclock(true);
//                                    differenceEdgeTimeGuard.setHavelowerdiff(true);
//                                    differenceEdgeTimeGuard.setDifferencexylower(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
//                                    differenceEdgeTimeGuard.setIsequallower(true);
//                                    differenceEdgeTimeGuard.setHaveupperdiff(true);
//                                    differenceEdgeTimeGuard.setDifferencexyupper(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
//                                    differenceEdgeTimeGuard.setIsequalupper(true);
//                                    differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
//                                    edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
//                                    Set<Clock> resetClockSet = new HashSet<>();
//                                    if (transitionListrepeat.get(0).isReset(clockList.get(0))) {
//                                        resetClockSet.add(clockList.get(0));
//                                    }
//                                    if (transitionListrepeat.get(0).isReset(clockList.get(1))) {
//                                        resetClockSet.add(clockList.get(1));
//                                    }
//                                    TaTransition transition = new TaTransition(
//                                            transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
//                                    transitionListsigma.add(transition);
//                                }
//                                if(!downregion){
//                                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//                                    edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
//                                    List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
//                                    DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
//                                    differenceEdgeTimeGuard.setLargerclock(true);
//                                    differenceEdgeTimeGuard.setClock1(clockList.get(0));
//                                    differenceEdgeTimeGuard.setClock2(clockList.get(1));
//                                    differenceEdgeTimeGuard.setLargerclock(true);
//                                    differenceEdgeTimeGuard.setHavelowerdiff(true);
//                                    differenceEdgeTimeGuard.setDifferencexylower(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
//                                    differenceEdgeTimeGuard.setIsequallower(false);
//                                    differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
//                                    edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
//                                    Set<Clock> resetClockSet = new HashSet<>();
//                                    if (transitionListrepeat.get(0).isReset(clockList.get(0))) {
//                                        resetClockSet.add(clockList.get(0));
//                                    }
//                                    if (transitionListrepeat.get(0).isReset(clockList.get(1))) {
//                                        resetClockSet.add(clockList.get(1));
//                                    }
//                                    TaTransition transition = new TaTransition(
//                                            transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
//                                    transitionListsigma.add(transition);
//                                }
//                        }
//                    }
//            }
//                System.out.println("transitionsigma new:"+transitionListsigma);
//                transitionList.addAll(transitionListsigma);
//            }
//            Totaltransitions.addAll(transitionList);
//        }
//
//        dota.setTransitions(Totaltransitions);
//    }
    private void evidenceToDOTA(TwoClockTA dota,List<TaLocation> regionsplit) {

        List<TaTransition> Totaltransitions=new ArrayList<>();
        List<TaTransition> newregionbypartition=new ArrayList<>();
        for (TaLocation l : dota.getLocations()) {
            List<TaTransition> transitionList = new ArrayList<>();
            for (String action : dota.getSigma()) {
                List<TaTransition> transitionListsigma = new ArrayList<>();
                List<TaTransition> transitions = dota.getTransitions(l, action, null);
                transitions.sort(new OTATranComparator(clockList.get(0)));
             //   System.out.println("transitions:"+transitions);
                for (int k=0;k<clockList.size()-1;k++){
                    for (int i = 0; i < transitions.size() - 1; i++) {
                        for (int j = i + 1; j < transitions.size(); j++) {
                            if (transitions.get(i).getLowerBound(clockList.get(k)) == transitions.get(j).getLowerBound(clockList.get(k)) &&
                                    transitions.get(i).getTimeGuard(clockList.get(k)).isLowerBoundOpen() == transitions.get(j).getTimeGuard(clockList.get(k)).isLowerBoundOpen()) {
                                if (transitions.get(i).getLowerBound(clockList.get(k+1)) > transitions.get(j).getLowerBound(clockList.get(k+1)) ||
                                        transitions.get(i).getLowerBound(clockList.get(k+1)) == transitions.get(j).getLowerBound(clockList.get(k+1)) && !transitions.get(i).getTimeGuard(clockList.get(k+1)).isLowerBoundClose() && transitions.get(j).getTimeGuard(clockList.get(k+1)).isLowerBoundClose()) {
                                    TaTransition transition = new TaTransition();
                                    transition = transitions.get(i);
                                    transitions.set(i, transitions.get(j));
                                    transitions.set(j, transition);
                                }
                            }
                        }
                    }
                }
                for (TaTransition transition:transitions){
                    for (Clock clock:clockList){
//                    if(transition.getTimeGuard(clock).getLowerBound()>kc[clockList.indexOf(clock)]){
                        if(transition.getTimeGuard(clock).getLowerBound()>kc||transition.getTimeGuard(clock).getLowerBound()==kc&&transition.getTimeGuard(clock).isLowerBoundOpen()){
                            List<TimeGuard> subguard1timeGuardList=new ArrayList<>();
                            for (Clock clockall:clockList){
//                            if(transition.getTimeGuard(clockall).getLowerBound()>kc[clockList.indexOf(clock)]){
                                if(transition.getTimeGuard(clockall).getLowerBound()>kc||transition.getTimeGuard(clock).getLowerBound()==kc&&transition.getTimeGuard(clock).isLowerBoundOpen()){
//                                TimeGuard subguard1clock = new TimeGuard(true, true, kc[clockList.indexOf(clock)], TimeGuard.MAX_TIME);
                                    TimeGuard subguard1clock = new TimeGuard(true, true, kc, TimeGuard.MAX_TIME);
                                    subguard1timeGuardList.add(subguard1clock);
                                }
                                else if(!transition.getTimeGuard(clockall).isLowerBoundOpen()){
                                    TimeGuard subguard1clock = new TimeGuard(false, false, transition.getTimeGuard(clockall).getLowerBound(), transition.getTimeGuard(clockall).getLowerBound());
                                    subguard1timeGuardList.add(subguard1clock);
                                }
                                else if(transition.getTimeGuard(clockall).isLowerBoundOpen()){
                                    TimeGuard subguard1clock = new TimeGuard(true, true, transition.getTimeGuard(clockall).getLowerBound(), transition.getTimeGuard(clockall).getLowerBound()+1);
                                    subguard1timeGuardList.add(subguard1clock);
                                }
                            }

                            Map<Clock, TimeGuard> map1 = new HashMap<>();
                            for (int m=0;m<clockList.size();m++){
                                map1.put(clockList.get(m),subguard1timeGuardList.get(m));
                            }
                            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                            edgeTimeGuard.setClockTimeGuardMap(map1);
                            TaTransition transition2=new TaTransition();
                            transition2.setClockTimeGuardMap(edgeTimeGuard);
                            transition2.setTargetLocation(transition.getTargetLocation());
                            transition2.setSourceLocation(transition.getSourceLocation());
                            transition2.setSymbol(transition.getSymbol());
                            transition2.setResetClockSet(transition.getResetClockSet());
                            transitionListsigma.add(transition2);;
                        }
                    }
                }
        //        System.out.println("transitionListsigma:"+transitionListsigma);
                for (int i1 = transitions.size() - 1; i1 >= 0; i1--) {
           //         System.out.println("transition:"+transitions.get(i1));
                    if (i1==transitions.size()-1&&transitionListsigma.size()==0){
                        List<TimeGuard> timeGuardList = new ArrayList<>();
                        for (int m = 0; m < clockList.size(); m++) {
                            TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
                            timeGuardList.add(timeGuard);
                        }
                        List<TimeGuard> subguard1timeGuardList = new ArrayList<>();
                        for (int m = 0; m < clockList.size(); m++) {
                            TimeGuard subguard1clock = new TimeGuard(timeGuardList.get(m).isLowerBoundOpen(), true, timeGuardList.get(m).getLowerBound(), TimeGuard.MAX_TIME);
                            subguard1timeGuardList.add(subguard1clock);
                        }
                        Map<Clock, TimeGuard> map1 = new HashMap<>();
                        for (int m = 0; m < clockList.size(); m++) {
                            map1.put(clockList.get(m), subguard1timeGuardList.get(m));
                        }
                        EdgeTimeGuard edgeTimeGuard = new EdgeTimeGuard();
                        edgeTimeGuard.setClockTimeGuardMap(map1);
                        TaTransition transition = new TaTransition();
                        transition.setClockTimeGuardMap(edgeTimeGuard);
                        transition.setTargetLocation(transitions.get(i1).getTargetLocation());
                        transition.setSourceLocation(transitions.get(i1).getSourceLocation());
                        transition.setSymbol(transitions.get(i1).getSymbol());
                        transition.setResetClockSet(transitions.get(i1).getResetClockSet());
                        transitionListsigma.add(transition);
                    } else {
                        if (i1 >= transitions.size() - transitionListsigma.size()-1) {
                            List<TimeGuard> timeGuardList = new ArrayList<>();
                            for (int m = 0; m < clockList.size(); m++) {
                                TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));
                                timeGuardList.add(timeGuard);
                            }
                            if(clockList.size()==2) {
                                TimeGuard timeGuard1 = transitions.get(i1).getTimeGuard(clockList.get(0));
                                TimeGuard timeGuard2 = transitions.get(i1).getTimeGuard(clockList.get(1));
                                transitionListsigma.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
                            }
                            if(clockList.size()==1){
                                //transitionListsigma.addAll(complementary(timeGuardList,transitionListsigma, transitions.get(i1).getTargetLocation(), clockList,transitions.get(i1).getResetClockSet()));
                                List<TimeGuard> list=new ArrayList<>();
                                for (TaTransition transition:transitionListsigma){
                                    list.add(transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)));
                                }
                                List<TimeGuard> timeGuards=complementaryoneclockforoneclock(list,transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(),transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen());
                                for (TimeGuard complementaryGuardy : timeGuards) {
                                    Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                                    clockTimeGuardMap.put(clockList.get(0), complementaryGuardy);
                                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                                    Set<Clock> resetClocks = new HashSet<>();
                                    if(transitions.get(i1).isReset(clockList.get(0))){
                                        resetClocks.add(clockList.get(0));}
                                    TaTransition t = new TaTransition(transitions.get(i1).getSourceLocation(), transitions.get(i1).getTargetLocation(), transitions.get(i1).getSymbol(), edgeTimeGuard, resetClocks);
                                    transitionListsigma.add(t);
                                }
                            }
                        } else {
                            List<TimeGuard> timeGuardList = new ArrayList<>();
                            for (int m = 0; m < clockList.size(); m++) {
                                TimeGuard timeGuard = transitions.get(i1).getTimeGuard(clockList.get(m));

                                timeGuardList.add(timeGuard);
                            }

                            if(clockList.size()==2) {
                                TimeGuard timeGuard1 = transitions.get(i1).getTimeGuard(clockList.get(0));
                                TimeGuard timeGuard2 = transitions.get(i1).getTimeGuard(clockList.get(1));
                                transitionListsigma.addAll(complementaryfirst(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i1).getTargetLocation(), clockList.get(0), clockList.get(1), transitions.get(i1).isReset(clockList.get(0)), transitions.get(i1).isReset(clockList.get(1)), transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i1).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));
                            }
                            if(clockList.size()==1){
                                //transitionListsigma.addAll(complementary(timeGuardList,transitionListsigma, transitions.get(i1).getTargetLocation(), clockList,transitions.get(i1).getResetClockSet()));
                                List<TimeGuard> list=new ArrayList<>();
                                for (TaTransition transition:transitionListsigma){
                                    list.add(transition.getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)));
                                }
                                List<TimeGuard> timeGuards=complementaryoneclockforoneclock(list,transitions.get(i1).getTimeGuard(clockList.get(0)).getLowerBound(),transitions.get(i1).getTimeGuard(clockList.get(0)).isLowerBoundOpen());
                                for (TimeGuard complementaryGuardy : timeGuards) {
                                    Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
                                    clockTimeGuardMap.put(clockList.get(0), complementaryGuardy);
                                    EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                                    edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
                                    Set<Clock> resetClocks = new HashSet<>();
                                    if(transitions.get(i1).isReset(clockList.get(0))){
                                        resetClocks.add(clockList.get(0));}
                                    TaTransition t = new TaTransition(transitions.get(i1).getSourceLocation(), transitions.get(i1).getTargetLocation(), transitions.get(i1).getSymbol(), edgeTimeGuard, resetClocks);
                                    transitionListsigma.add(t);
                                }
                            }
                        }
                    }
                  //  System.out.println("transitionListsigma old:"+transitionListsigma);
                }
                if(clockList.size()==2){
                    for(int i=transitions.size()-2;i>=0;i--){//find min transition in a set containing same transitions
                        if(i>=1&&transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound()== transitions.get(i-1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() &&
                                transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == transitions.get(i-1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen()&&
                                transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == transitions.get(i-1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound()&&
                                transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundClose()==transitions.get(i-1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundClose()){

                        }
                        else {
                            List<TaTransition> transitionListrepeat=new ArrayList<>();
                            transitionListrepeat.add(transitions.get(i));
                            for (int j = i + 1; j < transitions.size(); j++) {//find the set containing same transitions
                                if (transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen()&&transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen()&&
                                        transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() &&
                                        transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen()&&
                                        transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound()&&
                                        transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen()==transitions.get(j).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen()){
                                    transitionListrepeat.add(transitions.get(j));
                                }
                            }
                            for(int m=0;m<transitionListrepeat.size()-1;m++){//delete same region
                                for(int n=m+1;n<transitionListrepeat.size();n++){
                                    if(transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()==transitionListrepeat.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&
                                            transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()==transitionListrepeat.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        transitionListrepeat.remove(transitionListrepeat.get(n));
                                        n=n-1;
                                    }
                                }
                            }
                            if(transitionListrepeat.size()>1) {
                                for (int k=0; k<transitionListsigma.size();k++) {
                                    if (transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).isLowerBoundOpen() == transitions.get(i).getTimeGuard(clockList.get(0)).isLowerBoundOpen() &&
                                            transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(0)).getLowerBound() == transitions.get(i).getTimeGuard(clockList.get(0)).getLowerBound() &&
                                            transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(1)).isLowerBoundOpen() == transitions.get(i).getTimeGuard(clockList.get(1)).isLowerBoundOpen() &&
                                            transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(1)).getLowerBound() == transitions.get(i).getTimeGuard(clockList.get(1)).getLowerBound()) {
                                        transitionListsigma.addAll(resttransition(transitionListsigma.get(k), transitions.get(i).getTimeGuard(clockList.get(0)).isLowerBoundOpen(), transitions.get(i).getTimeGuard(clockList.get(0)).getLowerBound(),
                                                transitions.get(i).getTimeGuard(clockList.get(1)).isLowerBoundOpen(), transitions.get(i).getTimeGuard(clockList.get(1)).getLowerBound(), dota.getClockList().get(0), dota.getClockList().get(1)));
                                        transitionListsigma.remove(transitionListsigma.get(k));
                                        k=k-1;
                                    }
                                }
                                transitionListsigma.addAll(transitionListrepeat);
                                if(transitionListrepeat.size()==2){
//                                    System.out.println("transitionListrepeat3:"+transitionListrepeat);
//                                    System.out.println("transitionsigma:"+transitionListsigma);
                                    boolean upregion=false;
                                    boolean middleregion=false;
                                    boolean downregion=false;

                                    if(transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&!transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        downregion=true;
                                    }
                                    if(!transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        upregion=true;
                                    }
                                    if(transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(0).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        middleregion=true;
                                    }

                                    if(transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&!transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        downregion=true;
                                    }
                                    if(!transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        upregion=true;
                                    }
                                    if(transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHavelowerdiff()&&transitionListrepeat.get(1).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(0).isHaveupperdiff()){
                                        middleregion=true;
                                    }

                                    if(!upregion){
                                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                                        edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
                                        List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
                                        DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
                                        differenceEdgeTimeGuard.setLargerclock(true);
                                        differenceEdgeTimeGuard.setClock1(clockList.get(0));
                                        differenceEdgeTimeGuard.setClock2(clockList.get(1));
                                        differenceEdgeTimeGuard.setHaveupperdiff(true);
                                        differenceEdgeTimeGuard.setDifferencexyupper(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
                                        differenceEdgeTimeGuard.setIsequalupper(false);
                                        differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
                                        edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
                                        Set<Clock> resetClockSet = new HashSet<>();
                                        if (transitionListrepeat.get(0).isReset(clockList.get(0))) {
                                            resetClockSet.add(clockList.get(0));
                                        }
                                        if (transitionListrepeat.get(0).isReset(clockList.get(1))) {
                                            resetClockSet.add(clockList.get(1));
                                        }
                                        TaTransition transition = new TaTransition(
                                                transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
                                        transitionListsigma.add(transition);
                                    }
                                    if(!middleregion){
                                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                                        edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
                                        List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
                                        DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
                                        differenceEdgeTimeGuard.setLargerclock(true);
                                        differenceEdgeTimeGuard.setClock1(clockList.get(0));
                                        differenceEdgeTimeGuard.setClock2(clockList.get(1));
                                        differenceEdgeTimeGuard.setLargerclock(true);
                                        differenceEdgeTimeGuard.setHavelowerdiff(true);
                                        differenceEdgeTimeGuard.setDifferencexylower(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
                                        differenceEdgeTimeGuard.setIsequallower(true);
                                        differenceEdgeTimeGuard.setHaveupperdiff(true);
                                        differenceEdgeTimeGuard.setDifferencexyupper(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
                                        differenceEdgeTimeGuard.setIsequalupper(true);
                                        differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
                                        edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
                                        Set<Clock> resetClockSet = new HashSet<>();
                                        if (transitionListrepeat.get(0).isReset(clockList.get(0))) {
                                            resetClockSet.add(clockList.get(0));
                                        }
                                        if (transitionListrepeat.get(0).isReset(clockList.get(1))) {
                                            resetClockSet.add(clockList.get(1));
                                        }
                                        TaTransition transition = new TaTransition(
                                                transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
                                        transitionListsigma.add(transition);
                                    }
                                    if(!downregion){
                                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                                        edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
                                        List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
                                        DifferenceEdgeTimeGuard differenceEdgeTimeGuard=new DifferenceEdgeTimeGuard();
                                        differenceEdgeTimeGuard.setLargerclock(true);
                                        differenceEdgeTimeGuard.setClock1(clockList.get(0));
                                        differenceEdgeTimeGuard.setClock2(clockList.get(1));
                                        differenceEdgeTimeGuard.setLargerclock(true);
                                        differenceEdgeTimeGuard.setHavelowerdiff(true);
                                        differenceEdgeTimeGuard.setDifferencexylower(transitionListrepeat.get(0).getLowerBound(clockList.get(0))-transitionListrepeat.get(0).getLowerBound(clockList.get(1)));
                                        differenceEdgeTimeGuard.setIsequallower(false);
                                        differenceEdgeTimeGuards.add(differenceEdgeTimeGuard);
                                        edgeTimeGuard.setDifferenceEdgeTimeGuards(differenceEdgeTimeGuards);
                                        Set<Clock> resetClockSet = new HashSet<>();
                                        if (transitionListrepeat.get(0).isReset(clockList.get(0))) {
                                            resetClockSet.add(clockList.get(0));
                                        }
                                        if (transitionListrepeat.get(0).isReset(clockList.get(1))) {
                                            resetClockSet.add(clockList.get(1));
                                        }
                                        TaTransition transition = new TaTransition(
                                                transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
                                        transitionListsigma.add(transition);
                                    }
                                }

                            }
                        }
                    }}
         //       System.out.println("transitionsigma new:"+transitionListsigma);
                List<TaTransition> delete=new ArrayList<>();
                for (int i=0;i<transitionListsigma.size()-1;i++){
                    for (int j=i+1;j<transitionListsigma.size();j++){
                       boolean eq=true;
                         for (Clock clock:clockList){
                             if(transitionListsigma.get(i).getEdgeTimeGuard().getClockTimeGuardMap().get(clock).equals(transitionListsigma.get(j).getEdgeTimeGuard().getClockTimeGuardMap().get(clock))){

                             }
                             else {
                                 eq=false;
                                 break;
                             }
                         }
                         if(eq){
                             delete.add(transitionListsigma.get(i));
                             break;
                         }
                    }
                }
                transitionListsigma.removeAll(delete);
                transitionList.addAll(transitionListsigma);
            }
            Totaltransitions.addAll(transitionList);
        }

        dota.setTransitions(Totaltransitions);
    }

    public  void evidenceToDOTA(TwoClockTA dota){

        List<TaTransition> Totaltransitions=new ArrayList<>();
//        Clock clock = dota.getClock();
//    Clock clock1 = dota.getClockList().get(0);
//    Clock clock2 = dota.getClockList().get(1);
        List<TaTransition> newregionbypartition=new ArrayList<>();
        for (TaLocation l : dota.getLocations()) {
   //         System.out.println("初始location：" + l.toString());
            List<TaTransition> transitionList = new ArrayList<>();
            for (String action : dota.getSigma()) {
                List<TaTransition> transitionListsigma = new ArrayList<>();
                List<TaTransition> transitions = dota.getTransitions(l, action, null);
//            transitions.sort(new OTATranComparator(clock1));
                transitions.sort(new OTATranComparator(clockList.get(0)));

                for (int k=0;k<clockList.size()-1;k++){
                    for (int i = 0; i < transitions.size() - 1; i++) {
                        for (int j = i + 1; j < transitions.size(); j++) {

                            if (transitions.get(i).getLowerBound(clockList.get(k)) == transitions.get(j).getLowerBound(clockList.get(k)) &&
                                    transitions.get(i).getTimeGuard(clockList.get(k)).isLowerBoundOpen() == transitions.get(j).getTimeGuard(clockList.get(k)).isLowerBoundOpen()) {
                                //System.out.println("需要换位");
                                if (transitions.get(i).getLowerBound(clockList.get(k+1)) > transitions.get(j).getLowerBound(clockList.get(k+1)) ||
                                        transitions.get(i).getLowerBound(clockList.get(k+1)) == transitions.get(j).getLowerBound(clockList.get(k+1)) && !transitions.get(i).getTimeGuard(clockList.get(k+1)).isLowerBoundClose() && transitions.get(j).getTimeGuard(clockList.get(k+1)).isLowerBoundClose()) {
                                    TaTransition transition = new TaTransition();
                                    transition = transitions.get(i);
                                    transitions.set(i, transitions.get(j));
                                    transitions.set(j, transition);
                                }
                            }
                        }
                    }
                }


//for (int k=0;k<clockList.size()-1;k++){
//            for (int i = 0; i < transitions.size() - 1; i++) {
//                for (int j = i + 1; j < transitions.size(); j++) {
//
//
//                    if (transitions.get(i).getLowerBound(clockList.get(k)) == transitions.get(j).getLowerBound(clockList.get(k)) &&
//                            transitions.get(i).getTimeGuard(clockList.get(k)).isLowerBoundOpen() == transitions.get(j).getTimeGuard(clockList.get(k)).isLowerBoundOpen()) {
//                        //System.out.println("需要换位");
//                        if (transitions.get(i).getLowerBound(clockList.get(k+1)) > transitions.get(j).getLowerBound(clockList.get(k+1)) ||
//                                transitions.get(i).getLowerBound(clockList.get(k+1)) == transitions.get(j).getLowerBound(clockList.get(k+1)) && !transitions.get(i).getTimeGuard(clockList.get(k+1)).isLowerBoundClose() && transitions.get(j).getTimeGuard(clockList.get(k+1)).isLowerBoundClose()) {
//                            TaTransition transition = new TaTransition();
//                            transition = transitions.get(i);
//                            transitions.set(i, transitions.get(j));
//                            transitions.set(j, transition);
//                        }
//                      }
//                    }
//                }
//            }

    //            System.out.println("transitions:"+transitions);
                for (int i = transitions.size() - 1; i >= 0; i--) {
                    if(i==transitions.size()-1){
           //             System.out.println("transitions.get(i):"+transitions.get(i));
                        List<TimeGuard> timeGuardList=new ArrayList<>();
                        for (int m=0;m<clockList.size();m++){
                            TimeGuard timeGuard = transitions.get(i).getTimeGuard(clockList.get(m));
                            timeGuardList.add(timeGuard);
                        }
//                    TimeGuard timeGuard1 = transitions.get(i).getTimeGuard(clock1);
//                    TimeGuard timeGuard2 = transitions.get(i).getTimeGuard(clock2);
                        List<TimeGuard> subguard1timeGuardList=new ArrayList<>();
                        for (int m=0;m<clockList.size();m++){
                            TimeGuard subguard1clock = new TimeGuard(timeGuardList.get(m).isLowerBoundOpen(), true, timeGuardList.get(m).getLowerBound(), TimeGuard.MAX_TIME);
                            subguard1timeGuardList.add(subguard1clock);
                        }
//                    TimeGuard subguard1clock1 = new TimeGuard(timeGuard1.isLowerBoundOpen(), false, timeGuard1.getLowerBound(), TimeGuard.MAX_TIME);
//                    TimeGuard subguard1clock2 = new TimeGuard(timeGuard2.isLowerBoundOpen(), false, timeGuard2.getLowerBound(), TimeGuard.MAX_TIME);
//
                        //  System.out.println("subguard1clock2:"+subguard1clock2.toString());
                        Map<Clock, TimeGuard> map1 = new HashMap<>();
                        for (int m=0;m<clockList.size();m++){
                            map1.put(clockList.get(m),subguard1timeGuardList.get(m));
                        }
//                    map1.put(clock1, subguard1clock1);
//                    map1.put(clock2, subguard1clock2);
                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                        edgeTimeGuard.setClockTimeGuardMap(map1);
                        TaTransition transition=new TaTransition();
                        transition.setClockTimeGuardMap(edgeTimeGuard);
                        transition.setTargetLocation(transitions.get(i).getTargetLocation());
                        transition.setSourceLocation(transitions.get(i).getSourceLocation());
                        transition.setSymbol(transitions.get(i).getSymbol());
                        transition.setResetClockSet(transitions.get(i).getResetClockSet());
                        transitionListsigma.add(transition);
                    }
                    else {
             //           System.out.println("被划分的transition:"+transitions.get(i).toString());
                        List<TimeGuard> timeGuardList=new ArrayList<>();
                        for (int m=0;m<clockList.size();m++){
                            TimeGuard timeGuard = transitions.get(i).getTimeGuard(clockList.get(m));

                            timeGuardList.add(timeGuard);
                        }
//                    TimeGuard timeGuard1 = transitions.get(i).getTimeGuard(clockList.get(0));
//                    TimeGuard timeGuard2 = transitions.get(i).getTimeGuard(clockList.get(1));
            //            System.out.println("transitions.get(i).getResetClockSet():"+transitions.get(i).getResetClockSet());
                        //         transitionListsigma.addAll(complementary(timeGuard1.getLowerBound(), timeGuard2.getLowerBound(), transitionListsigma, transitions.get(i).getTargetLocation(), clockList.get(0), clockList.get(1),transitions.get(i).isReset(clockList.get(0)),transitions.get(i).isReset(clockList.get(1)),transitions.get(i).getTimeGuard(clockList.get(0)).isLowerBoundOpen(),transitions.get(i).getTimeGuard(clockList.get(1)).isLowerBoundOpen()));

                        transitionListsigma.addAll(complementary(timeGuardList,transitionListsigma, transitions.get(i).getTargetLocation(), clockList,transitions.get(i).getResetClockSet()));
                    }
                }
                //System.out.println("transitionListsigma:"+transitionListsigma);

//            for(int i=transitions.size()-2;i>=0;i--){
//                boolean eq=true;
//                    for (int m = 0; m < clockList.size(); m++) {//找不重复的或者重复中最小的
//                        if (transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).getLowerBound() == transitions.get(i-1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).getLowerBound()&&
//                                transitions.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).isLowerBoundClose()==transitions.get(i-1).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(m)).isLowerBoundClose()) {
//
//                        }
//                        else {
//                            eq=false;
//                        }
//                    }
//                    if(i>=1&&eq){
//
//                    }
//
////                if(i>=1&&transitions.get(i).getLowerBound(clock1) == transitions.get(i-1).getLowerBound(clock1) &&
////                        transitions.get(i).getTimeGuard(clock1).isLowerBoundOpen() == transitions.get(i-1).getTimeGuard(clock1).isLowerBoundOpen()&&
////                        transitions.get(i).getLowerBound(clock2) == transitions.get(i-1).getLowerBound(clock2)&&
////                        transitions.get(i).getTimeGuard(clock2).isLowerBoundClose()==transitions.get(i-1).getTimeGuard(clock2).isLowerBoundClose()){
////
////                }
//                else {
//                List<TaTransition> transitionListrepeat=new ArrayList<>();
//                transitionListrepeat.add(transitions.get(i));
//                for (int j = i + 1; j < transitions.size(); j++) {
////                    if(i==6&&j==7){
////                       System.out.println(transitions.get(i));
////                        System.out.println(transitions.get(j));
////                    }
//                    boolean eq2=false;
//                    int c=0;
//                    for (int m=0;m<clockList.size();c++) {
//                        if (transitions.get(i).getTimeGuard(clockList.get(m)).isLowerBoundOpen()) {
//                            c++;
//                        }
//                    }
//                    if(c>=2){
//                        eq2=true;
//                    }
//                    if(eq2){//第i个transition是存在差值的
//                        boolean eq3=true;
//                        for(int m=0;m<clockList.size();m++){
//                            if(transitions.get(i).getLowerBound(clockList.get(m))==transitions.get(j).getLowerBound(clockList.get(m))){
////只判断了第j个transition和第i个是否相同，没考虑开闭情况是否相同
//                            }
//                            else {
//                                eq3=false;
//                            }
//                        }
//                        if(eq3){
//                            transitionListrepeat.add(transitions.get(j));
//                        }
//                    }
//
////                    if (transitions.get(i).getTimeGuard(clock1).isLowerBoundOpen()&&transitions.get(i).getTimeGuard(clock2).isLowerBoundOpen()&&transitions.get(i).getLowerBound(clock1) == transitions.get(j).getLowerBound(clock1) &&
////                            transitions.get(i).getTimeGuard(clock1).isLowerBoundOpen() == transitions.get(j).getTimeGuard(clock1).isLowerBoundOpen()&&
////                            transitions.get(i).getLowerBound(clock2) == transitions.get(j).getLowerBound(clock2)&&
////                            transitions.get(i).getTimeGuard(clock2).isLowerBoundOpen()==transitions.get(j).getTimeGuard(clock2).isLowerBoundOpen()){
////                        transitionListrepeat.add(transitions.get(j));
////                    }
//                }
//                  //  System.out.println("transitionListrepeat:"+transitionListrepeat);
//                    for(int m=0;m<transitionListrepeat.size()-1;m++){
//                        for(int n=m+1;n<transitionListrepeat.size();n++){
//
////删除在同一个region里面的点的transistion
//                            boolean sameregion=true;
//                            for (int p=0;p<clockList.size()-1;p++){
//                                for (int q=p+1;q<clockList.size();q++){
//                                    int indexm=-1;
//                                    int indexn=-1;
//                                    for (int o=0;o<transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().size();o++) {
//
//
//                                        if (transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(o).getClock1() == clockList.get(p)&&
//                                                transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(o).getClock2() == clockList.get(q)){
//                                               indexm=o;
//                                               break;
//                                            }
//
//                                        }
//                                    for (int o=0;o<transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().size();o++) {
//
//
//                                        if (transitionListrepeat.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(o).getClock1() == clockList.get(p)&&
//                                                transitionListrepeat.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(o).getClock2() == clockList.get(q)){
//                                            indexn=o;
//                                            break;
//                                        }
//
//                                    }
//                                    if(indexm<0&&indexn>=0||indexm>=0&&indexn<0){
//                                        sameregion=false;
//                                    }
//                                    if(indexm>=0&&indexn>=0){
//                                        boolean eqregion=true;
//                                        for (int s=0;s<clockList.size();s++){
//                                          if(transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(indexm).isHavelowerdiff()==transitionListrepeat.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(indexn).isHavelowerdiff()
//                                                    && transitionListrepeat.get(m).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(indexm).isHaveupperdiff()==transitionListrepeat.get(n).getEdgeTimeGuard().getDifferenceEdgeTimeGuards().get(indexn).isHaveupperdiff()){
//
//                                            }
//                                        else {
//                                              eqregion=false;
//                                          }
//
//                                        }
//                                        if(!eqregion){
//                                            sameregion=false;
//                                        }
//                                    }
//                                    if(sameregion){
//                                        transitionListrepeat.remove(transitionListrepeat.get(n));
//                                        n=n-1;
//                                    }
//                                }
//                            }
////                            if(transitionListrepeat.get(m).getEdgeTimeGuard().isHavelowerdiff()==transitionListrepeat.get(n).getEdgeTimeGuard().isHavelowerdiff()&&
////                                    transitionListrepeat.get(m).getEdgeTimeGuard().isHaveupperdiff()==transitionListrepeat.get(n).getEdgeTimeGuard().isHaveupperdiff()){
////                                transitionListrepeat.remove(transitionListrepeat.get(n));
////                                n=n-1;
////                            }
//                        }
//                    }
//                if(transitionListrepeat.size()>1) {//删掉原先已经划分好的覆盖多region的边
//                  //  System.out.println("transitionListrepeat2:"+transitionListrepeat);
//                    for (int k=0; k<transitionListsigma.size();k++) {
//                        boolean sametr=true;
//                        for (int p=0;p<clockList.size();p++){
//                            if(transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(p)).isLowerBoundOpen() == transitions.get(i).getTimeGuard(clockList.get(p)).isLowerBoundOpen() &&
//                                    transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clockList.get(p)).getLowerBound() == transitions.get(i).getTimeGuard(clockList.get(p)).getLowerBound()){
//
//                            }
//                            else {
//                                sametr=false;
//                            }
//                        }
////                        if (transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).isLowerBoundOpen() == transitions.get(i).getTimeGuard(clock1).isLowerBoundOpen() &&
////                                transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clock1).getLowerBound() == transitions.get(i).getTimeGuard(clock1).getLowerBound() &&
////                                transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).isLowerBoundOpen() == transitions.get(i).getTimeGuard(clock2).isLowerBoundOpen() &&
////                                transitionListsigma.get(k).getEdgeTimeGuard().getClockTimeGuardMap().get(clock2).getLowerBound() == transitions.get(i).getTimeGuard(clock2).getLowerBound()) {
//                         if(sametr){
//                            transitionListsigma.addAll(resttransition(transitionListsigma.get(k), transitions.get(i)));
//                            transitionListsigma.remove(transitionListsigma.get(k));
//                            k=k-1;
//                        }
//                    }
//                    transitionListsigma.addAll(transitionListrepeat);
//
//
////                    if(transitionListrepeat.size()==2){
//                    if(transitionListrepeat.size()>=2){
////                        boolean upregion=false;
////                        boolean middleregion=false;
////                        boolean downregion=false;
//                        for (int m=transitionListrepeat.size()-1;m>0;m--){
//                            TaTransition transition = new TaTransition(
//                                    transitionListrepeat.get(m).getSourceLocation(), transitionListrepeat.get(m).getTargetLocation(), transitionListrepeat.get(m).getSymbol(), transitionListrepeat.get(m).getEdgeTimeGuard(), transitionListrepeat.get(m).getResetClockSet());
//                            transitionListsigma.add(transition);
//                        }
//
//
////                        if(transitionListrepeat.get(0).getEdgeTimeGuard().isHavelowerdiff()&&!transitionListrepeat.get(0).getEdgeTimeGuard().isHaveupperdiff()){
////                            downregion=true;
////                        }
////                        if(!transitionListrepeat.get(0).getEdgeTimeGuard().isHavelowerdiff()&&transitionListrepeat.get(0).getEdgeTimeGuard().isHaveupperdiff()){
////                            upregion=true;
////                        }
////                        if(transitionListrepeat.get(0).getEdgeTimeGuard().isHavelowerdiff()&&transitionListrepeat.get(0).getEdgeTimeGuard().isHaveupperdiff()){
////                            middleregion=true;
////                        }
////
////                        if(transitionListrepeat.get(1).getEdgeTimeGuard().isHavelowerdiff()&&!transitionListrepeat.get(1).getEdgeTimeGuard().isHaveupperdiff()){
////                            downregion=true;
////                        }
////                        if(!transitionListrepeat.get(1).getEdgeTimeGuard().isHavelowerdiff()&&transitionListrepeat.get(1).getEdgeTimeGuard().isHaveupperdiff()){
////                            upregion=true;
////                        }
////                        if(transitionListrepeat.get(1).getEdgeTimeGuard().isHavelowerdiff()&&transitionListrepeat.get(1).getEdgeTimeGuard().isHaveupperdiff()){
////                            middleregion=true;
////                        }
////
////                        if(!upregion){
////                            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
////                            edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
////                            edgeTimeGuard.setLargerclock(true);
////                            edgeTimeGuard.setHaveupperdiff(true);
////                            edgeTimeGuard.setDifferencexyupper(transitionListrepeat.get(0).getLowerBound(x)-transitionListrepeat.get(0).getLowerBound(y));
////                            edgeTimeGuard.setIsequalupper(false);
////                            Set<Clock> resetClockSet = new HashSet<>();
////                            if (transitionListrepeat.get(0).isReset(clock1)) {
////                                resetClockSet.add(clock1);
////                            }
////                            if (transitionListrepeat.get(0).isReset(clock1)) {
////                                resetClockSet.add(clock2);
////                            }
////                            TaTransition transition = new TaTransition(
////                                    transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
////                            transitionListsigma.add(transition);
////                        }
////                        if(!middleregion){
////                            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
////                            edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
////                            edgeTimeGuard.setLargerclock(true);
////                            edgeTimeGuard.setHavelowerdiff(true);
////                            edgeTimeGuard.setDifferencexylower(transitionListrepeat.get(0).getLowerBound(x)-transitionListrepeat.get(0).getLowerBound(y));
////                            edgeTimeGuard.setIsequallower(true);
////                            edgeTimeGuard.setHaveupperdiff(true);
////                            edgeTimeGuard.setDifferencexyupper(transitionListrepeat.get(0).getLowerBound(x)-transitionListrepeat.get(0).getLowerBound(y));
////                            edgeTimeGuard.setIsequalupper(true);
////                            Set<Clock> resetClockSet = new HashSet<>();
////                            if (transitionListrepeat.get(0).isReset(clock1)) {
////                                resetClockSet.add(clock1);
////                            }
////                            if (transitionListrepeat.get(0).isReset(clock1)) {
////                                resetClockSet.add(clock2);
////                            }
////                            TaTransition transition = new TaTransition(
////                                    transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
////                            transitionListsigma.add(transition);
////                        }
////                        if(!downregion){
////                            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
////                            edgeTimeGuard.setClockTimeGuardMap(transitionListrepeat.get(0).getEdgeTimeGuard().getClockTimeGuardMap());
////                            edgeTimeGuard.setLargerclock(true);
////                            edgeTimeGuard.setHavelowerdiff(true);
////                            edgeTimeGuard.setDifferencexylower(transitionListrepeat.get(0).getLowerBound(x)-transitionListrepeat.get(0).getLowerBound(y));
////                            edgeTimeGuard.setIsequallower(false);
////                            Set<Clock> resetClockSet = new HashSet<>();
////                            if (transitionListrepeat.get(0).isReset(clock1)) {
////                                resetClockSet.add(clock1);
////                            }
////                            if (transitionListrepeat.get(0).isReset(clock1)) {
////                                resetClockSet.add(clock2);
////                            }
////                            TaTransition transition = new TaTransition(
////                                    transitionListrepeat.get(0).getSourceLocation(), transitionListrepeat.get(0).getTargetLocation(), transitionListrepeat.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
////                            transitionListsigma.add(transition);
////                        }
//                    }
//
//                }
//                }
//            }

                transitionList.addAll(transitionListsigma);
            }
          //  System.out.println("transitionList:"+transitionList);
            Totaltransitions.addAll(transitionList);
        }

//    List<TaTransition> deletetransitionList=new ArrayList<>();
//    for (TaTransition transition:Totaltransitions) {
//        for (int i = 0; i < clockList.size(); i++) {
//            if (transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(i)).getLowerBound() == transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(i)).getUpperBound() &&
//                    transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(i)).isLowerBoundOpen() == false &&
//                    transition.getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(i)).isUpperBoundOpen() == true) {
//                deletetransitionList.add(transition);
//            }
//            if (transition.getLowerBound(clock1) == transition.getUpperBound(clock1) &&
//                    transition.getTimeGuard(clock1).isLowerBoundOpen() == false &&
//                    transition.getTimeGuard(clock1).isUpperBoundOpen() == true) {
//                deletetransitionList.add(transition);
//            }
//            if (transition.getLowerBound(clock2) == transition.getUpperBound(clock2) &&
//                    transition.getTimeGuard(clock2).isLowerBoundOpen() == false &&
//                    transition.getTimeGuard(clock2).isUpperBoundOpen() == true) {
//                deletetransitionList.add(transition);
//            }
//        }
//    }
//    Totaltransitions.removeAll(deletetransitionList);
        dota.setTransitions(Totaltransitions);

        //     System.out.println("dota.getTransitions()"+dota.getTransitions());
    }

    public void show() {
        List<String> stringList = new ArrayList<>();
        List<String> suffixStringList = new ArrayList<>();
        List<TwoClockResetLogicTimeWord> prefixList = getPrefixList();
        int maxLen = 0;
        for (TwoClockResetLogicTimeWord word : prefixList) {
            String s = word.toString();
            stringList.add(s);
            maxLen = maxLen > s.length() ? maxLen : s.length();
        }
        for (RegionTwoClockLogicTimedWord words : suffixSet) {
            StringBuilder s=new StringBuilder();
            if(words.getTimedActions()==null){
                s.append("null");
            }
            else {
            for (int i=0;i<words.getTimedActions().size();i++){
                s.append(words.getTimedActions().get(i).getSymbol().toString());

                for (int m=0;m<clockList.size();m++){
                    s.append(words.getTimedActions().get(i).getRegion().getTimeGuardList().get(m).getLowerBound());
                    s.append(words.getTimedActions().get(i).getRegion().getTimeGuardList().get(m).isLowerBoundOpen());
                    s.append(words.getTimedActions().get(i).getRegion().getTimeGuardList().get(m).getUpperBound());
                    s.append(words.getTimedActions().get(i).getRegion().getTimeGuardList().get(m).isUpperBoundOpen());
                    s.append(words.getTimedActions().get(i).getRegion().getDifferList());


                }
            }}
            suffixStringList.add(s.toString());
        }


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < maxLen; i++) {
            sb.append(" ");
        }
        sb.append("|");
        for (String s : suffixStringList) {
            sb.append(s);
            sb.append("|");
        }
        sb.append("\n");

        for (int i = 0; i < prefixList.size(); i++) {
            String prefixString = stringList.get(i);
            sb.append(prefixString);
            int slen = s.size();
            for (int k = 0; k < maxLen - prefixString.length(); k++) {
                sb.append(" ");
            }
            sb.append("|");
            for (int j = 0; j < suffixSet.size(); j++) {
                PairRegion pair = new PairRegion(prefixList.get(i),suffixSet.get(j));
                pair.prefix=prefixList.get(i);
                pair.suffix= suffixSet.get(j);

                if(suffixSet.get(j).getTimedActions().size()!=0&&prefixList.get(i).getTimedActions()!=null){
                    PairRegion pairRegion = new PairRegion(prefixList.get(i),suffixSet.get(j));
                    pairRegion.prefix=prefixList.get(i);
                    pairRegion.suffix=suffixSet.get(j);
                    BooleanAnswer answer = answers.get(pairRegion);
                    if(answer!=null) {
                        boolean b = answer.isAccept();
                        String a = b ? "+" : "-";
                        sb.append(a);
                        if(answer.getResets()!=null){
                            for (int m=0;m<clockList.size();m++){
                                if(answer.getResets().get(0).getActualResets()[m]){
                                    sb.append("T");
                                }
                                else {
                                    sb.append("F");
                                }

                            }
                        }
                    }
                    else {
                        String a = "#" ;
                        sb.append(a);
                    }
                }
                else if (suffixSet.get(j).getTimedActions().size()==0&&prefixList.get(i).getTimedActions()!=null){
                    PairRegion pairRegion = new PairRegion(prefixList.get(i),null);
                    pairRegion.prefix=prefixList.get(i);
                    pairRegion.suffix=null;
                    BooleanAnswer answer = answers.get(pairRegion);
                    if(answer!=null) {
                        boolean b = answer.isAccept();
                        String a = b ? "+" : "-";
                        sb.append(a);
                    }
                    else {
                        sb.append("#");
                    }
                }
                else if(suffixSet.get(j).getTimedActions().size()!=0&&prefixList.get(i).getTimedActions()==null){
                    PairRegion pairRegion = new PairRegion(null,suffixSet.get(j));
                    pairRegion.prefix=null;
                    pairRegion.suffix=suffixSet.get(j);
                    BooleanAnswer answer = answers.get(pairRegion);
                    if(answer!=null) {
                        boolean b = answer.isAccept();
                        String a = b ? "+" : "-";
                        sb.append(a);
                    }
                    else {
                        sb.append("#");
                    }
                }
                else {
                    PairRegion pairRegion = new PairRegion(null,null);
                    pairRegion.prefix=null;
                    pairRegion.suffix=null;
                    BooleanAnswer answer = answers.get(pairRegion);
                    if(answer!=null){
                    boolean b = answer.isAccept();
                    String a = b ? "+" : "-";
                    sb.append(a);}
                    else {
                        sb.append("#");
                    }
                }
                String suffixString = suffixStringList.get(j);
                for (int k = 0; k < suffixString.length() - 1; k++) {
                    sb.append(" ");
                }
                sb.append("|");
            }
            sb.append("\n");

            if (i == slen - 1) {
                for (int k = 0; k < maxLen; k++) {
                    sb.append("-");
                }
                sb.append("|");
                for (String suffixString : suffixStringList) {
                    for (int k = 0; k < suffixString.length(); k++) {
                        sb.append("-");
                    }
                    sb.append("|");
                }
                sb.append("\n");
            }
        }
        System.out.println(sb);
    }

    @Override
    public TwoClockTA getFinalHypothesis() {
        return hypothesis;
    }


    private void fillTable() {
        fillTable(s);
        fillTable(r);
    }
    public TwoClockResetLogicTimeWord selectfromRegion2(RegionTwoClockLogicTimedWord suffix,TwoClockResetLogicTimeWord prefix) {
        selectfromregionc++;
//        System.out.println("selectfromregionc:"+selectfromregionc);
//        System.out.println("prefix:"+prefix);
        if(suffix.size()!=0){
            for(int i1=0;i1<suffix.size();i1++){
              //  System.out.println("suffix:"+suffix.getTimedActions().get(i1).getRegion().getTimeGuardList());
            }}
        TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=null;
        TwoClockLogicTimeWord twoClockLogicTimeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
        double[] difference = new double[this.getClockList().size()];
        if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
            TwoClockResetLogicAction twoClockLogicAction = prefix.get(prefix.size() - 1);
            for (int p = 0; p < this.getClockList().size(); p++) {
                if (!twoClockLogicAction.isReset(this.getClockList().get(p))) {
                    difference[p] = twoClockLogicAction.getValue(this.getClockList().get(p));
                } else {
                    difference[p] = 0d;
                }

            }
        }
        for (int i = 0; i < suffix.size(); i++) {
//            System.out.println("i"+i);
//            System.out.println(suffix.get(i).getRegion().getTimeGuardList());
            if(twoClockResetLogicTimeWord!=null&&twoClockResetLogicTimeWord.getTimedActions()!=null&&twoClockResetLogicTimeWord.getTimedActions().size()!=0){
                twoClockResetLogicTimeWord.setTimedActions(null);}
            double[] l = new double[clockList.size()];
            for (int m = 0; m < clockList.size(); m++) {
                if (suffix.get(i).getRegion().getTimeGuardList().get(m).isLowerBoundOpen()) {
                    BigDecimal bigDecimal=new BigDecimal(Double.toString(suffix.get(i).getRegion().getTimeGuardList().get(m).getLowerBound()));
                    BigDecimal bigDecimal1=new BigDecimal(Double.toString(0.05));
                    BigDecimal add=bigDecimal.add(bigDecimal1);
                    BigDecimal diff=new BigDecimal(Double.toString(difference[m]));
//                    if(add.subtract(diff).doubleValue()<0){
//                        l[m] = diff.doubleValue();
//                    }
//                    else {
                        l[m] = (bigDecimal.add(bigDecimal1).doubleValue());
 //                   }
                } else {
                    l[m] = suffix.get(i).getRegion().getTimeGuardList().get(m).getLowerBound();
                }
            }
            double[] current2=new double[clockList.size()];
//            System.out.println("difference:"+difference);
//            System.out.println(suffix.get(i).getRegion().getTimeGuardList());
//            for (int i1=0;i1<l.length;i1++){
//                System.out.println("l[i1]:"+l[i1]);
//            }
            BigDecimal bigDecimal=new BigDecimal(Double.toString(l[0]));
            BigDecimal bigDecimal2=new BigDecimal(Double.toString(l[1]));
            BigDecimal bigDecimal3=new BigDecimal(Double.toString(difference[0]));
            BigDecimal bigDecimal4=new BigDecimal(Double.toString(difference[1]));
            BigDecimal diffl=bigDecimal.subtract(bigDecimal2);
            BigDecimal diffdiff=bigDecimal3.subtract(bigDecimal4);
//            System.out.println(bigDecimal2.doubleValue());
//            System.out.println(diffdiff.doubleValue());
//            System.out.println(bigDecimal2.add(diffdiff).doubleValue());
            if(diffl.subtract(diffdiff).doubleValue()<0){
                l[0]=bigDecimal2.add(diffdiff).doubleValue();
            }
//            for (int i1=0;i1<l.length;i1++){
//                System.out.println("l[i1]:"+l[i1]);
//            }
            BigDecimal diffl2=bigDecimal2.subtract(bigDecimal);
            BigDecimal diffdiff2=bigDecimal4.subtract(bigDecimal3);
            if(diffl2.subtract(diffdiff2).doubleValue()<0){
                l[1]=bigDecimal.add(diffdiff2).doubleValue();
            }
//            for (int i1=0;i1<l.length;i1++){
//                System.out.println("l[i1]:"+l[i1]);
//            }
            double[] current=iterate(l,0, suffix.get(i),difference,current2);
//            BigDecimal b1 = new BigDecimal(Double.toString(difference[0]));
//            BigDecimal b2 = new BigDecimal(Double.toString(difference[1]));
//            double[] current=z3(b1.subtract(b2).doubleValue(),suffix.get(i).getRegion().getTimeGuardList().get(0).getLowerBound()*1.0,difference[0],suffix.get(i).getRegion().getTimeGuardList().get(0).getUpperBound()*1.0,suffix.get(i).getRegion().getTimeGuardList().get(1).getLowerBound()*1.0,difference[1],suffix.get(i).getRegion().getTimeGuardList().get(1).getUpperBound()*1.0,suffix.get(i).getRegion().getTimeGuardList().get(0).isLowerBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(0).isUpperBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(1).isLowerBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(1).isUpperBoundOpen());
            if(current==null){
                return null;
            }
            Map<Clock, Double> map = new HashMap<>();
            for (int k=0;k<clockList.size();k++) {
                map.put(clockList.get(k),current[k]);
            }
            TwoClockLogicAction twoClockLogicAction1 = new TwoClockLogicAction(suffix.get(i).getSymbol(), map);
            twoClockLogicActionList.add(twoClockLogicAction1);
            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
            TwoClockLogicTimeWord logicTimeWord2=new TwoClockLogicTimeWord(null);
            if(prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
                logicTimeWord2 = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
                logicTimeWord2.setTimedActions(TwoClockResetLogicTimeWord.logicTimeWord(prefix).getTimedActions());
                logicTimeWord2 = TwoClockLogicTimeWord.concat(logicTimeWord2, twoClockLogicTimeWord);
            }
            else {
                logicTimeWord2=twoClockLogicTimeWord;
            }
         //   System.out.println("logicTimeWord2:"+logicTimeWord2);
            TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=teacher.transferWord(logicTimeWord2);
            reset++;
            twoClockResetLogicTimeWord=twoClockResetLogicTimeWord1;
            twoClockResetLogicTimeWord.setTimedActions(twoClockResetLogicTimeWord1.getTimedActions());
            for (int p=0;p<this.getClockList().size();p++){
                if (!twoClockResetLogicTimeWord1.getLastResetAction().isReset(this.getClockList().get(p))){
                    difference[p]=twoClockResetLogicTimeWord1.getLastResetAction().getValue(this.getClockList().get(p));
                }
                else {
                    difference[p]=0d;
                }

            }
        }
        return twoClockResetLogicTimeWord;
    }
    public  double[] z3(double diff,double lowera1,double lowera2,double uppera1,double lowerc1,double lowerc2,double upperc1,boolean alo,boolean auo,boolean clo,boolean cuo) {
//        System.out.println("h w");
//        System.out.println(diff);
//        System.out.println(lowera1);
//        System.out.println(lowera2);
//        System.out.println(uppera1);
//        System.out.println(lowerc1);
//        System.out.println(lowerc2);
//        System.out.println(upperc1);
        HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);
        Solver s = ctx.mkSolver();
        RealExpr a = ctx.mkRealConst("a");
        RealExpr c = ctx.mkRealConst("c");
        ArithExpr left = ctx.mkSub(a, c);
        String s2=String.valueOf( (int)(diff*1000));
  //      System.out.println("s2:"+ctx.mkReal(s2.concat("/1000")));
        BoolExpr equation = ctx.mkEq(left, ctx.mkReal(s2.concat("/1000")));
        s.add(equation);
        if(alo){
            String s1=String.valueOf( (int)(lowera1*1000));
            s.add(ctx.mkGt(a,  ctx.mkReal(s1.concat("/1000"))));
        }
        else {
            String s1=String.valueOf( (int)(lowera1*1000));
     //       System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(a,  ctx.mkReal(s1.concat("/1000"))));
        }
        String s3=String.valueOf( (int)(lowera2*1000));
      //  System.out.println("s3:"+ctx.mkReal(s3.concat("/1000")));
        s.add(ctx.mkGe(a,  ctx.mkReal(s3.concat("/1000"))));
        if(auo){
            String s1=String.valueOf( (int)(uppera1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGt(  ctx.mkReal(s1.concat("/1000")),a));
        }
        else {
            String s1=String.valueOf( (int)(uppera1*1000));
        //    System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(  ctx.mkReal(s1.concat("/1000")),a));
        }
        if(clo){
            String s1=String.valueOf( (int)(lowerc1*1000));
        //    System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGt(c,  ctx.mkReal(s1.concat("/1000"))));
        }
        else {
            String s1=String.valueOf( (int)(lowerc1*1000));
         //   System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(c,  ctx.mkReal(s1.concat("/1000"))));
        }
        String s4=String.valueOf( (int)(lowerc2*1000));
        //System.out.println("s4:"+ctx.mkReal(s4.concat("/1000")));
        s.add(ctx.mkGe(c,  ctx.mkReal(s4.concat("/1000"))));
        if(cuo) {
            String s1=String.valueOf( (int)(upperc1*1000));
          //  System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGt( ctx.mkReal(s1.concat("/1000")),c));
        }
        else {
            String s1=String.valueOf( (int)(upperc1*1000));
            //System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(  ctx.mkReal(s1.concat("/1000")),c));
        }
        Status result = s.check();
        if (result == Status.SATISFIABLE){
//            System.out.println("model for: x + y*c*e = d + a, a > 0");
//            System.out.print(s.getModel());
//            System.out.println(a.toString());
            Expr a_value = s.getModel().evaluate(a, false);
            Expr c_value = s.getModel().evaluate(c, false);
            double[] results=new double[2];
            if (a_value.toString().contains("/")) {
                String d = a_value.toString().split("/")[0];
                String b = a_value.toString().split("/")[1];
                results[0] = Double.valueOf(d) / Double.valueOf(b);
            } else {
                results[0] = Double.valueOf(a_value.toString());
            }
            if (c_value.toString().contains("/")) {
                String d = c_value.toString().split("/")[0];
                String b = c_value.toString().split("/")[1];
                results[1] = Double.valueOf(d) / Double.valueOf(b);
            } else {
                results[1] = Double.valueOf(c_value.toString());
            }
          //  System.out.println("results:"+results[0]+results[1]);
            return results;

        }
        else if(result == Status.UNSATISFIABLE)
            System.out.println("unsat");
        return null;
//        else
//            System.out.println("unknow");
//        return null;
    }
    public  double[] iterate(double[] l,int index,RegionTwoClockLogicAction suffix,double[] difference,double[] current){

        boolean canstop = false;
        if(index==this.clockList.size()-1) {
            BigDecimal bigDecimal=new BigDecimal(Double.toString(0));
            BigDecimal bigDecimal1=new BigDecimal(Double.toString(0.05));
            for (double xv=l[index]; suffix.getRegion().getTimeGuardList().get(index).isPass(xv);xv=bigDecimal.add(bigDecimal1).doubleValue()){
                boolean inscope = true;
                current[index]=xv;
                for (int k = 0; k < this.getClockList().size()-1; k++) {
                    if (suffix.getRegion().getTimeGuardList().get(k).isPass(current[k])) {

                    } else {
                        inscope = false;
                    }
                }
                boolean eqdiff = true;
                for (int m = 0; m < this.getClockList().size()-1; m++) {
                    for (int j = m + 1; j < this.getClockList().size(); j++) {
                        BigDecimal b1 = new BigDecimal(Double.toString(difference[m]));
                        BigDecimal b2 = new BigDecimal(Double.toString(difference[j]));
                        BigDecimal b3 = new BigDecimal(Double.toString(current[m]));
                        BigDecimal b4 = new BigDecimal(Double.toString(current[j]));
                        BigDecimal b5 = new BigDecimal(b3.subtract(b4).doubleValue());
                        BigDecimal b6 = new BigDecimal(b1.subtract(b2).doubleValue());
//            System.out.println("current[m]:"+current[m]);
//            System.out.println(b1);
//            System.out.println(b2);
//            System.out.println(b3.toString());
//            System.out.println(b4.toString());
//            System.out.println(inscope);
                        if (b5.subtract(b6).doubleValue()==0.0) {


                        } else {
                            eqdiff = false;
                            break;
                        }

                    }
                }

                if (!canstop&&inscope && eqdiff){
                    return current;
                }
                bigDecimal = new BigDecimal(Double.toString(xv));
            }
        }
        else {
            BigDecimal bigDecimal=new BigDecimal(0);
            BigDecimal bigDecimal1=new BigDecimal(0.05);
            for (double xv=l[index]; suffix.getRegion().getTimeGuardList().get(index).isPass(xv);xv=bigDecimal1.add(bigDecimal).doubleValue()){
                //  System.out.println("xv:"+xv);
                current[index]=xv;
                double[] current22=  iterate(l,index+1,suffix,difference,current);
                if(current22!=null) {
                    return current22;
                }
                //System.out.println("xv2:"+xv);
                bigDecimal = new BigDecimal(Double.toString(xv));
                bigDecimal1 = new BigDecimal(Double.toString(0.05));

            }

        }
        return null;
    };

    private void fillTable(List<TwoClockResetLogicTimeWord> set) {
        for (TwoClockResetLogicTimeWord prefix : set) {
          //  System.out.println("filltable prefix:"+prefix);
            for (RegionTwoClockLogicTimedWord suffix : suffixSet) {
                if(suffix.size()!=0){
                    for(int i1=0;i1<suffix.size();i1++){
//                System.out.println("filltable suffix:"+suffix.getTimedActions().get(i1).getRegion().getTimeGuardList());
                    }}
                boolean have = false;
                for (PairRegion pairRegion:answers.keySet()){
//                    if(pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix==null){
//                        have=true;
//                    }
                    if(pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix==null||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()==null&&pairRegion.prefix==null&&prefix==null||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()!=null&&suffix.getTimedActions().size()==0&&pairRegion.prefix==null&&prefix==null||
                            pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()!=null&&suffix.getTimedActions().size()==0&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                            pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()!=null&&suffix.getTimedActions().size()==0&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0){
                        have=true;
                    }
                    else if(pairRegion.suffix==null&&suffix==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix)||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix)||
                            pairRegion.suffix==null&&suffix!=null&&suffix.getTimedActions()!=null&&suffix.getTimedActions().size()==0&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix)){
                        have=true;
                    }
                    else if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suffix)&&pairRegion.prefix==null&&prefix==null||
                            pairRegion.suffix!=null&& pairRegion.suffix.equals(suffix)&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                            pairRegion.suffix!=null&& pairRegion.suffix.equals(suffix)&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0){
                        have=true;
                    }
                    else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix) && pairRegion.suffix.equals(suffix)){
                        have=true;
                    }
                }
                if (!have) {
                    if (suffix!=null&&suffix.getTimedActions() != null && suffix.getTimedActions().size() != 0 && prefix!=null&&prefix.getTimedActions() != null && prefix.getTimedActions().size() != 0) {
                        TwoClockResetLogicTimeWord twoClockResetLogicTimeWordList = new TwoClockResetLogicTimeWord(null);
//                        System.out.println("find before:"+selectfromregionc);
//                        System.out.println("reset:"+reset);
                        if(LogictoDelay(prefix)==null){
                         //   System.out.println("invalid");
                        }else {
                        twoClockResetLogicTimeWordList = selectfromRegion2(suffix, prefix);}
                        if (LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList == null || LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList.getTimedActions() == null || LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList.getTimedActions().size() == 0) {
                         //   System.out.println("selectfromRegion2 null");
                            boolean eq = false;
                            for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
                                if (pairRegion1.prefix.equals(prefix) && pairRegion1.suffix.equals(suffix)) {
                                    eq = true;
                                    twoClockResetLogicTimeWordList = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
                                }
                            }

                            if (!eq) {
                                if (twoClockResetLogicTimeWordList == null || twoClockResetLogicTimeWordList.getTimedActions() == null || twoClockResetLogicTimeWordList.getTimedActions().size() == 0) {

                                    boolean allreset = true;
                                    for (Clock clock : clockList) {
                                        if (prefix.getLastResetAction().isReset(clock)) {
                                        } else {
                                            allreset = false;
                                        }
                                    }
                                    boolean needselect = true;
                                    if (allreset) {
                                        for (int i1 = 1; i1 < clockList.size(); i1++) {
                                            if (suffix.get(0).getRegion().getTimeGuardList().get(0).getLowerBound()  == suffix.get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() ) {
                                            } else {
                                                needselect = false;
                                            }
                                        }
                                    }
                                    if (needselect) {
                                        twoClockResetLogicTimeWordList = selectfromRegion(suffix, prefix);
                                    }
                                }
                            }
                        }
//                        System.out.println("find after:"+selectfromregionc);
//                        System.out.println("reset:"+reset);
                        if (twoClockResetLogicTimeWordList != null && twoClockResetLogicTimeWordList.getTimedActions() != null && twoClockResetLogicTimeWordList.getTimedActions().size() != 0) {
          //         System.out.println("twoClockResetLogicTimeWordList:"+twoClockResetLogicTimeWordList);
                            boolean isaccept=teacher.membership(twoClockResetLogicTimeWordList, clockList.size());
                            member++;
                            List<Resets> resetsList = new ArrayList<>();
                            for (int i = prefix.size(); i < twoClockResetLogicTimeWordList.size(); i++) {
                                boolean[] actualreset = new boolean[clockList.size()];
                                for (int m = 0; m < clockList.size(); m++) {
                                    if(twoClockResetLogicTimeWordList.getTimedActions().get(i).getResetClockSet().contains(clockList.get(m))){
                                        actualreset[m] = true;
                                    }
                                }
                                Resets resets = new Resets(actualreset);
                                resetsList.add(resets);
                            }
                            BooleanAnswer answer=new BooleanAnswer(resetsList,isaccept);
                            answer.setAccept(isaccept);
                            answer.setResets(resetsList);
                                PairRegion pairRegion = new PairRegion(prefix, suffix);
                                pairRegion.prefix = prefix;
                                pairRegion.suffix = suffix;
                                answers.remove(pairRegion);
                                answers.put(pairRegion, answer);
                        } else {
                            PairRegion pairRegion = new PairRegion(prefix, suffix);
                            pairRegion.prefix = prefix;
                            pairRegion.suffix = suffix;
                            answers.remove(pairRegion);
                            List<Resets> resetsList = new ArrayList<>();
                            for (int i = 0; i < suffix.size(); i++) {
                                boolean[] actualreset = new boolean[clockList.size()];
                                for (int m = 0; m < clockList.size(); m++) {
                                    actualreset[m] = true;
                                }
                                Resets resets = new Resets(actualreset);
                                resetsList.add(resets);
                            }
                            BooleanAnswer answer = new BooleanAnswer(resetsList, false);
                            answers.put(pairRegion, answer);
                        }
                    } else if (suffix!=null&&suffix.getTimedActions() != null && suffix.getTimedActions().size() != 0 && prefix == null ||
                            suffix!=null&&suffix.getTimedActions() != null && suffix.getTimedActions().size() != 0 && prefix.getTimedActions() == null ||
                            suffix!=null&&suffix.getTimedActions() != null && suffix.getTimedActions().size() != 0 && prefix.getTimedActions().size() == 0) {
                        prefix.setTimedActions(null);
                        PairRegion pairRegion = new PairRegion(null, suffix);
                        pairRegion.prefix = null;
                        pairRegion.suffix = suffix;
                        answers.remove(pairRegion);
//                        System.out.println("find before:"+selectfromregionc);
//                        System.out.println("reset:"+reset);
                        TwoClockResetLogicTimeWord timeWord = selectfromRegion2(suffix, prefix);
//                        System.out.println("find after:"+selectfromregionc);
//                        System.out.println("reset:"+reset);
                        if (timeWord == null || timeWord.getTimedActions() == null || timeWord.getTimedActions().size() == 0) {
                            List<Resets> resetsList = new ArrayList<>();
                            for (int i = 0; i < suffix.size(); i++) {
                                boolean[] actualreset = new boolean[clockList.size()];
                                for (int m = 0; m < clockList.size(); m++) {
                                    actualreset[m] = true;
                                }
                                Resets resets = new Resets(actualreset);
                                resetsList.add(resets);
                            }
                            BooleanAnswer answer = new BooleanAnswer(resetsList, false);
                            answers.put(pairRegion, answer);
                        } else {
                            boolean isaccept=teacher.membership(timeWord, clockList.size());
                            member++;
                            List<Resets> resetsList = new ArrayList<>();
                            for (int i = 0; i < timeWord.size(); i++) {
                                boolean[] actualreset = new boolean[clockList.size()];
                                for (int m = 0; m < clockList.size(); m++) {
                                    if(timeWord.getTimedActions().get(i).getResetClockSet().contains(clockList.get(m))){
                                        actualreset[m] = true;
                                    }
                                }
                                Resets resets = new Resets(actualreset);
                                resetsList.add(resets);
                            }
                            BooleanAnswer answer=new BooleanAnswer(resetsList,isaccept);
                            answer.setAccept(isaccept);
                            answer.setResets(resetsList);
                            answers.put(pairRegion, answer);
                        }
                    } else if (suffix==null && prefix!=null&&prefix.getTimedActions() != null && prefix.getTimedActions().size() != 0 ||
                            suffix.getTimedActions()==null && prefix!=null&&prefix.getTimedActions() != null && prefix.getTimedActions().size() != 0 ||
                            suffix.getTimedActions().size() == 0 && prefix!=null&&prefix.getTimedActions() != null && prefix.getTimedActions().size() != 0) {
                        PairRegion pairRegion = new PairRegion(prefix, null);
                        pairRegion.prefix = prefix;
                        pairRegion.suffix = null;
                        answers.remove(pairRegion);
                        boolean isaccept=teacher.membership(prefix, clockList.size());
                        member++;
                        BooleanAnswer answer = new BooleanAnswer(null,isaccept);
                        answer.setAccept(isaccept);
                        answer.setResets(null);
                        answers.put(pairRegion, answer);

                    } else {
                        prefix.setTimedActions(null);
                        PairRegion pairRegion = new PairRegion(null, null);
                        pairRegion.prefix = null;
                        pairRegion.suffix = null;
                        answers.remove(pairRegion);
                        TwoClockResetLogicTimeWord resetLogicTimeWord = TwoClockResetLogicTimeWord.emptyWord();
                        boolean isaccept=teacher.membership(resetLogicTimeWord, clockList.size());
                        member++;
                        BooleanAnswer answer = new BooleanAnswer(null,isaccept);
                        answer.setAccept(isaccept);
                        answer.setResets(null);
                        answers.put(pairRegion, answer);
                    }
                }
            }
        }
    }
    private boolean isPrepared() {
        return isClosed()==null && isConsistent();

    }

private TwoClockResetLogicTimeWord isClosed() {
    Set<Row> sRowSet = getRows(s);
    for (TwoClockResetLogicTimeWord regionTwoClockResetLogicTimedWord:r){
        Row row=row(regionTwoClockResetLogicTimedWord);
        boolean contains=false;
        for (Row rows:sRowSet){
            boolean contain=true;
            for (int i=0;i<rows.size();i++){
                if(rows.get(i).isAccept()!=row.get(i).isAccept()||rows.get(i).getResets()!=null&&!rows.get(i).getResets().equals(row.get(i).getResets())){
                    contain=false;
                    break;
                }
            }
            if(contain){
                contains=true;
                break;
            }
        }
        if(!contains){
            return regionTwoClockResetLogicTimedWord;
        }
    }
    return null;
}

private void makeClosed(TwoClockResetLogicTimeWord word) {
            s.add(word);
            r.remove(word);
            for (String action : sigma) {
                 Map<Clock,Double> map=new HashMap<>();
                for (int m=0;m<clockList.size();m++){
                    map.put(clockList.get(m),0d);
                }
                            TwoClockLogicAction logicAction = new TwoClockLogicAction(action, map);
                            logicAction.setSymbol(action);
                            logicAction.setClockValueMap(map);
                            TwoClockLogicTimeWord logicTimeWord = TwoClockResetLogicTimeWord.logicTimeWord(word).concat(logicAction);
                            TwoClockResetLogicTimeWord resetWord = teacher.transferWord(logicTimeWord);
                            if (!s.contains(resetWord) && !r.contains(resetWord)) {
                                r.add(resetWord);
                            }
                        }
            int old=member;
            fillTable(r);
            membertable+=member-old;
            System.out.println("membertable:"+membertable);
}

    private Set<Row> getRows(List<TwoClockResetLogicTimeWord> set) {
        Set<Row> rows = new HashSet<>();
        if (set != null && !set.isEmpty()) {
            for (int i=0;i<set.size();i++){
                rows.add(rowclose(set.get(i)));
            }
        }
        return rows;
    }



    private Row row(TwoClockResetLogicTimeWord resetLogicTimeWord) {
        if (!s.contains(resetLogicTimeWord) && !r.contains(resetLogicTimeWord)) {
            return null;
        }
        Row row = new Row();
        for (RegionTwoClockLogicTimedWord suffixWord : suffixSet) {
            if(suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0){
                PairRegion pairRegion = new PairRegion(resetLogicTimeWord,suffixWord);
                pairRegion.prefix=resetLogicTimeWord;
                pairRegion.suffix=suffixWord;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);}
            else if (suffixWord==null&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0||
                    suffixWord!=null&&suffixWord.getTimedActions()==null&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0||
                    suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()==0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0){
                PairRegion pairRegion = new PairRegion(resetLogicTimeWord,null);
                pairRegion.prefix=resetLogicTimeWord;
                pairRegion.suffix=null;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);
            }
            else if(suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord==null||
                    suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()==null||
                    suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()==0){
                PairRegion pairRegion = new PairRegion(null,suffixWord);
                pairRegion.prefix=null;
                pairRegion.suffix=suffixWord;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);
            }
            else {
                PairRegion pairRegion = new PairRegion(null,null);
                pairRegion.prefix=null;
                pairRegion.suffix=null;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);
            }
        }
        return row;
    }


    private Row rowclose(TwoClockResetLogicTimeWord resetLogicTimeWord) {
        if (!s.contains(resetLogicTimeWord) && !r.contains(resetLogicTimeWord)) {
            return null;
        }
        Row row = new Row();
        for (RegionTwoClockLogicTimedWord suffixWord : suffixSet) {
            if(suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0){
                PairRegion pairRegion = new PairRegion(resetLogicTimeWord,suffixWord);
                pairRegion.prefix=resetLogicTimeWord;
                pairRegion.suffix=suffixWord;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);}
            else if (suffixWord==null&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0||
                    suffixWord!=null&&suffixWord.getTimedActions()==null&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0||
                    suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()==0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()!=0){
                PairRegion pairRegion = new PairRegion(resetLogicTimeWord,null);
                pairRegion.prefix=resetLogicTimeWord;
                pairRegion.suffix=null;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);
            }
            else if(suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord==null||
                    suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()==null||
                    suffixWord!=null&&suffixWord.getTimedActions()!=null&&suffixWord.getTimedActions().size()!=0&&resetLogicTimeWord!=null&&resetLogicTimeWord.getTimedActions()!=null&&resetLogicTimeWord.getTimedActions().size()==0){
                PairRegion pairRegion = new PairRegion(null,suffixWord);
                pairRegion.prefix=null;
                pairRegion.suffix=suffixWord;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);
            }
            else {
                PairRegion pairRegion = new PairRegion(null,null);
                pairRegion.prefix=null;
                pairRegion.suffix=null;
                BooleanAnswer answer = answers.get(pairRegion);
                row.add(answer);
            }
        }
        return row;
    }

    private RegionTwoClockResetLogicTimedWord keyWord;
    private List<TwoClockResetLogicTimeWord> unConsistentCouple = null;
    private RegionTwoClockLogicAction key = null;

//    public boolean isConsistent() {
//        unConsistentCouple = new ArrayList<>();
//        Set<TwoClockLogicAction> logicActionSet = getLastActionSet();
//        List<TwoClockResetLogicTimeWord> list = getPrefixList();
//        for (int i = 0; i < list.size(); i++) {
//            Row row1 = row(list.get(i));
//            for (int j = i + 1; j < list.size(); j++) {
//                Row row2 = row(list.get(j));
//                boolean eq=true;
//                for (int k = 0; k < row1.size(); k++) {
//
//                    if(row1.get(k)==null&&row2.get(k)==null||
//                            row1.get(k)!=null&&row2.get(k)!=null&&
//                                    row1.get(k).equals(row2.get(k))){
//                    }
//                    else {
//                        eq=false;
//                    }
//                }
//                if (eq) {
//                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=new TwoClockResetLogicTimeWord(null);
//                    twoClockResetLogicTimeWord1.setTimedActions(null);
//                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(null);
//                    twoClockResetLogicTimeWord2.setTimedActions(null);
//                    PairRegion pair1=new PairRegion(null,null);
//                    PairRegion pair2=new PairRegion(null,null);
//                    for (TwoClockLogicAction action : logicActionSet) {
//                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//                        twoClockResetLogicTimeWord1=new TwoClockResetLogicTimeWord(null);
//                        twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(null);
//                        {
//                            List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
//                            twoClockLogicActionList.add(action);
//                            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
//                            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//                            List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
//                            List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
//                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                            regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
////
//                            for (TwoClockResetLogicTimeWord twoClockResetLogicTimeWord : list) {
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() >= 2 && list.get(i)!=null&&list.get(i).getTimedActions() != null&&list.get(i).getTimedActions().size()!=0) {
//
//                                    boolean preeq = true;
//                                    if (twoClockResetLogicTimeWord.size() - 1 != list.get(i).size()) {
//                                        preeq = false;
//                                    }
//                                    else {
//                                        for (int k = 0; k < twoClockResetLogicTimeWord.size() - 1; k++) {
//                                            for (int q = 0; q < clockList.size(); q++) {
//                                                BigDecimal bigDecimal=new BigDecimal(twoClockResetLogicTimeWord.getTimedActions().get(k).getValue(clockList.get(q)));
//                                                BigDecimal bigDecimal2=new BigDecimal(list.get(i).getTimedActions().get(k).getValue(clockList.get(q)));
//                                                if (bigDecimal.subtract(bigDecimal2).doubleValue()==0 &&
//                                                        twoClockResetLogicTimeWord.getTimedActions().get(k).getSymbol().equals(list.get(i).getTimedActions().get(k).getSymbol())) {
//
//                                                } else {
//                                                    preeq = false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (preeq) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal1=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound() );
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//
//                                            if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0&&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            } else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
////                                        if(eq2){
////                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
////                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
////                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
////                                                    eq2=false;
////                                                    break;
////                                                }
////                                            }
////                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord1 = twoClockResetLogicTimeWord;
//                                                                        }
//                                    }
//                                }
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() == 1) {
//                                    if (list.get(i) == null || list.get(i).getTimedActions() == null || list.get(i).getTimedActions().size() == 0) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal1=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound() );
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//
//                                            if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0&&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            } else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
////                                        if(eq2){
////                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
////                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
////                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
////                                                    eq2=false;
////                                                    break;
////                                                }
////                                            }
////                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord1 = twoClockResetLogicTimeWord;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        {
//                            List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
//                            twoClockLogicActionList.add(action);
//                            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
//                            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//                            List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
//                            List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
//                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                            regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//                            for (TwoClockResetLogicTimeWord twoClockResetLogicTimeWord : list) {
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() >= 2 && list.get(j)!=null && list.get(j).getTimedActions() != null&& list.get(j).getTimedActions().size() != 0) {
//                                    boolean preeq = true;
//                                    if (twoClockResetLogicTimeWord.size() - 1 != list.get(j).size()) {
//                                        preeq = false;
//                                    } else {
//                                        for (int k = 0; k < twoClockResetLogicTimeWord.size() - 1; k++) {
//                                            for (int q = 0; q < clockList.size(); q++) {
//                                                BigDecimal bigDecimal1=new BigDecimal(twoClockResetLogicTimeWord.getTimedActions().get(k).getValue(clockList.get(q)));
//                                                BigDecimal bigDecimal2=new BigDecimal(list.get(j).getTimedActions().get(k).getValue(clockList.get(q)));
//                                                if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0 &&
//                                                        twoClockResetLogicTimeWord.getTimedActions().get(k).getSymbol().equals(list.get(j).getTimedActions().get(k).getSymbol())) {
//
//                                                } else {
//                                                    preeq = false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (preeq) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            if (bigDecimal.subtract(bigDecimal2).doubleValue()==0.0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0.0 &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            } else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
//                                        if(eq2){
//                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
//                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
//                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
//                                                    eq2=false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord2 = twoClockResetLogicTimeWord;
//                                        }
//                                    }
//                                }
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() == 1) {
//                                    if (list.get(j) == null || list.get(j).getTimedActions() == null || list.get(j).getTimedActions().size() == 0) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal1=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0&&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0 &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            } else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
//                                        if(eq2){
//                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
//                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
//                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
//                                                    eq2=false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord2 = twoClockResetLogicTimeWord;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        if (twoClockResetLogicTimeWord1 != null && twoClockResetLogicTimeWord2!= null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null) {
//                            for(RegionTwoClockLogicTimedWord suff:suffixSet) {
//                                BooleanAnswer answer1=new BooleanAnswer(null,false);
//                                BooleanAnswer answer2=new BooleanAnswer(null,false);
//                                if (suff == null||suff.getTimedActions()==null || suff.getTimedActions().size() == 0) {
//                                    for (PairRegion pairRegion:answers.keySet()){
//                                        if(pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null||
//                                           pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()==null||
//                                           pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord1.getTimedActions().size()==0){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1)){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                    }
//                                    for (PairRegion pairRegion:answers.keySet()){
//                                        if(pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null||
//                                           pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null||
//                                           pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()==0){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2)){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                    }
//                                } else {
//
//                                    for (PairRegion pairRegion:answers.keySet()){
//                                        if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1) && pairRegion.suffix.equals(suff)){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                    }
//                                    for (PairRegion pairRegion:answers.keySet()){
//                                        if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null||
//                                                pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null||
//                                                pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()==0){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2) && pairRegion.suffix.equals(suff)){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                    }
//                                }
//                                boolean reseteq=true;
//                                if(answer1.getResets()!=null&&answer2.getResets()!=null){
//                                    for(int m=0;m<answer1.getResets().size();m++){
//                                        if(!answer1.getResets().get(m).equals(answer2.getResets().get(m))){
//                                            reseteq=false;
//                                        }
//                                    }
//                                    }
//
//                                if(answer1.getResets()==null&&answer2.getResets()!=null||answer1.getResets()!=null&&answer2.getResets()==null){
//                                    reseteq=false;
//                                }
//                                boolean reseteq2=true;
//                                for (int m = 0; m < clockList.size(); m++) {
//                                    if (twoClockResetLogicTimeWord1.getLastResetAction().isReset(clockList.get(m)) ==
//                                            twoClockResetLogicTimeWord2.getLastResetAction().isReset(clockList.get(m))) {
//
//                                    } else {
//                                        reseteq2 = false;
//                                    }
//                                }
//                                if (answer1.isAccept() != answer2.isAccept()||!reseteq||!reseteq2) {
//                                    unConsistentCouple.add(twoClockResetLogicTimeWord1);
//                                    unConsistentCouple.add(twoClockResetLogicTimeWord2);
//                                    List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
//                                    twoClockLogicActionList.add(action);
//                                    TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
//                                    twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//                                    List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
//                                    RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
//                                    key = regionTwoClockLogicAction;
//                                    return false;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }

//    public boolean isConsistent(){
//        unConsistentCouple = new ArrayList<>();
////        Set<TwoClockLogicAction> logicActionSet = getLastActionSet();
//        Set<TwoClockLogicAction> logicActionSet = getLastActionSet();
//        List<TwoClockResetLogicTimeWord> list = getPrefixList();
//        //System.out.println("list:"+list);
//        for (int i = 0; i < list.size(); i++) {
//            Row row1 = row(list.get(i));
//            for (int j = i + 1; j < list.size(); j++) {
//                Row row2 = row(list.get(j));
//                boolean eq=true;
//                for (int k = 0; k < row1.size(); k++) {
//
//                    if(row1.get(k)==null&&row2.get(k)==null||
//                            row1.get(k)!=null&&row2.get(k)!=null&&
//                                    row1.get(k).equals(row2.get(k))){
//                    }
//                    else {
//                        eq=false;
//                    }
//                }
//                if (eq) {
//                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=new TwoClockResetLogicTimeWord(null);
//                    twoClockResetLogicTimeWord1.setTimedActions(null);
//                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(null);
//                    twoClockResetLogicTimeWord2.setTimedActions(null);
//                    PairRegion pair1=new PairRegion(null,null);
//                    PairRegion pair2=new PairRegion(null,null);
//                    for (TwoClockLogicAction action : logicActionSet) {
//                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//                        twoClockResetLogicTimeWord1=new TwoClockResetLogicTimeWord(null);
//                        twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(null);
//                        {
//
//                            List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
//                            twoClockLogicActionList.add(action);
//                            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
//                            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//                            List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
//                            List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
//                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                            regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//
//                            for (TwoClockResetLogicTimeWord twoClockResetLogicTimeWord : list) {
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() >= 2 && list.get(i)!=null&&list.get(i).getTimedActions() != null&&list.get(i).getTimedActions().size()!=0) {
//
//                                    boolean preeq = true;
//                                    if (twoClockResetLogicTimeWord.size() - 1 != list.get(i).size()) {
//                                        preeq = false;
//                                    }
//                                    else {
//                                        for (int k = 0; k < twoClockResetLogicTimeWord.size() - 1; k++) {
//                                            for (int q = 0; q < clockList.size(); q++) {
//                                                BigDecimal bigDecimal=new BigDecimal(twoClockResetLogicTimeWord.getTimedActions().get(k).getValue(clockList.get(q)));
//                                                BigDecimal bigDecimal2=new BigDecimal(list.get(i).getTimedActions().get(k).getValue(clockList.get(q)));
//                                                if (bigDecimal.subtract(bigDecimal2).doubleValue()==0 &&
//                                                        twoClockResetLogicTimeWord.getTimedActions().get(k).getSymbol().equals(list.get(i).getTimedActions().get(k).getSymbol())) {
//
//                                                } else {
//                                                    preeq = false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    }
//
//
//                                    if (preeq) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal1=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound() );
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//
//                                            if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0&&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            }
//
//                                            else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
//                                        if(eq2){
//                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
//                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
//                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
//                                                    eq2=false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord1 = twoClockResetLogicTimeWord;
//                                        }
//                                    }
//                                }
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() == 1) {
//                                    if (list.get(i) == null || list.get(i).getTimedActions() == null || list.get(i).getTimedActions().size() == 0) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal1=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound() );
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//
//                                            if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0&&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            }
//                                            else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
//                                        if(eq2){
//                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
//                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
//                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
//                                                    eq2=false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord1 = twoClockResetLogicTimeWord;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        {
//                            List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
//                            twoClockLogicActionList.add(action);
//                            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
//                            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//                            List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
//                            List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
//                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                            regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//                            for (TwoClockResetLogicTimeWord twoClockResetLogicTimeWord : list) {
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() >= 2 && list.get(j).getTimedActions() != null&& list.get(j).getTimedActions() != null&& list.get(j).getTimedActions().size() != 0) {
//                                    boolean preeq = true;
//                                    if (twoClockResetLogicTimeWord.size() - 1 != list.get(j).size()) {
//                                        preeq = false;
//                                    } else {
//                                        for (int k = 0; k < twoClockResetLogicTimeWord.size() - 1; k++) {
//                                            for (int q = 0; q < clockList.size(); q++) {
//                                                BigDecimal bigDecimal1=new BigDecimal(twoClockResetLogicTimeWord.getTimedActions().get(k).getValue(clockList.get(q)));
//                                                BigDecimal bigDecimal2=new BigDecimal(list.get(j).getTimedActions().get(k).getValue(clockList.get(q)));
//                                                if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0 &&
//                                                        twoClockResetLogicTimeWord.getTimedActions().get(k).getSymbol().equals(list.get(j).getTimedActions().get(k).getSymbol())) {
//
//                                                } else {
//                                                    preeq = false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    }
//                                    if (preeq) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            if (bigDecimal.subtract(bigDecimal2).doubleValue()==0.0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0.0 &&                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            } else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
//                                        if(eq2){
//                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
//                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
//                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
//                                                    eq2=false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord2 = twoClockResetLogicTimeWord;
//                                            //           System.out.println("eq twoClockResetLogicTimeWord2:"+twoClockResetLogicTimeWord2);
//                                        }
//                                    }
//                                }
//                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() == 1) {
//                                    if (list.get(j) == null || list.get(j).getTimedActions() == null || list.get(j).getTimedActions().size() == 0) {
//                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
//                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
//                                        twoClockLogicActionList2.add(twoClockLogicAction);
//                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
//                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
//                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
//                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
//                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        boolean eq2 = true;
//                                        for (int q = 0; q < clockList.size(); q++) {
//                                            BigDecimal bigDecimal1=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
//                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
//                                            if (bigDecimal1.subtract(bigDecimal2).doubleValue()==0&&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0 &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//
//                                            } else {
//                                                eq2 = false;
//                                                break;
//                                            }
//                                        }
//                                        if(eq2){
//                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
//                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
//                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
//                                                    eq2=false;
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        if (eq2) {
//                                            twoClockResetLogicTimeWord2 = twoClockResetLogicTimeWord;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        if (twoClockResetLogicTimeWord1 != null && twoClockResetLogicTimeWord2!= null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null) {
//                            boolean isconsistent=true;
//                            for(RegionTwoClockLogicTimedWord suff:suffixSet) {
//                                BooleanAnswer answer1=new BooleanAnswer(null,false);
//                                BooleanAnswer answer2=new BooleanAnswer(null,false);
//                                if (suff == null || suff.getTimedActions().size() == 0) {
//                                    for (PairRegion pairRegion:answers.keySet()){
////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
////                                            answer1=answers.get(pairRegion);
////                                        }
////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
////                                            answer1=answers.get(pairRegion);
////                                        }
//
//
//                                        if(pairRegion.suffix==null&&suff.getTimedActions().size() == 0&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix==null&&suff.getTimedActions().size() == 0&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1)){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                    }
//
//
//                                    for (PairRegion pairRegion:answers.keySet()){
////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
////                                            answer1=answers.get(pairRegion);
////                                        }
////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
////                                            answer1=answers.get(pairRegion);
////                                        }
//
//
//                                        if(pairRegion.suffix==null&&suff.getTimedActions().size() == 0&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix==null&&suff.getTimedActions().size() == 0&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2)){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                    }
//
////                                    for (PairRegion pairRegion : answers.keySet()) {
//////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
//////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
////                                        boolean psuffnull=false;
////                                        if(pairRegion.suffix==null||pairRegion.suffix!=null&&pairRegion.suffix.getTimedActions()==null||pairRegion.suffix!=null&&pairRegion.suffix.getTimedActions()!=null&&pairRegion.suffix.getTimedActions().size()==0){
////                                            psuffnull=true;
////                                        }
////                                        boolean suffnull=false;
////                                        if(suff==null||suff!=null&&suff.getTimedActions()==null||suff!=null&&suff.getTimedActions()!=null&&suff.getTimedActions().size()==0){
////                                            suffnull=true;
////                                        }
////                                        boolean pprenull=false;
////                                        if(pairRegion.prefix==null||pairRegion.prefix!=null&&pairRegion.prefix.getTimedActions()==null||pairRegion.prefix!=null&&pairRegion.prefix.getTimedActions()!=null&&pairRegion.prefix.getTimedActions().size()==0){
////                                            pprenull=true;
////                                        }
////                                        boolean prenull=false;
////                                        if(twoClockResetLogicTimeWord1==null||twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()==null||twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord1.getTimedActions().size()==0){
////                                            prenull=true;
////                                        }
////
////                                        if (psuffnull&&suffnull&&pprenull&&pprenull) {
////                                            answer1=answers.get(pairRegion);
////                                        } else if (psuffnull&&suffnull && !pprenull&& !prenull && pairRegion.prefix.getTimedActions().equals(twoClockResetLogicTimeWord1.getTimedActions())) {
////                                            answer1=answers.get(pairRegion);
////                                        } else if (!psuffnull&&!suffnull && pprenull&& prenull && pairRegion.suffix.getTimedActions().equals(suff.getTimedActions())) {
////                                            answer1=answers.get(pairRegion);
////                                        } else if (!psuffnull&&!suffnull && !pprenull&& !prenull && pairRegion.prefix.getTimedActions().equals(twoClockResetLogicTimeWord1.getTimedActions()) && pairRegion.suffix.getTimedActions().equals(suff.getTimedActions())) {
////                                            answer1=answers.get(pairRegion);
////                                        }
////                                    }
//
////                                    for (PairRegion pairRegion : answers.keySet()) {
//////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
//////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
////                                        boolean psuffnull=false;
////                                        if(pairRegion.suffix==null||pairRegion.suffix!=null&&pairRegion.suffix.getTimedActions()==null||pairRegion.suffix!=null&&pairRegion.suffix.getTimedActions()!=null&&pairRegion.suffix.getTimedActions().size()==0){
////                                            psuffnull=true;
////                                        }
////                                        boolean suffnull=false;
////                                        if(suff==null||suff!=null&&suff.getTimedActions()==null||suff!=null&&suff.getTimedActions()!=null&&suff.getTimedActions().size()==0){
////                                            suffnull=true;
////                                        }
////                                        boolean pprenull=false;
////                                        if(pairRegion.prefix==null||pairRegion.prefix!=null&&pairRegion.prefix.getTimedActions()==null||pairRegion.prefix!=null&&pairRegion.prefix.getTimedActions()!=null&&pairRegion.prefix.getTimedActions().size()==0){
////                                            pprenull=true;
////                                        }
////                                        boolean prenull=false;
////                                        if(twoClockResetLogicTimeWord2==null||twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null||twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()==0){
////                                            prenull=true;
////                                        }
////
////                                        if (psuffnull&&suffnull&&pprenull&&pprenull) {
////                                            answer1=answers.get(pairRegion);
////                                        } else if (psuffnull&&suffnull && !pprenull&& !prenull && pairRegion.prefix.getTimedActions().equals(twoClockResetLogicTimeWord2.getTimedActions())) {
////                                            answer1=answers.get(pairRegion);
////                                        } else if (!psuffnull&&!suffnull && pprenull&& prenull && pairRegion.suffix.getTimedActions().equals(suff.getTimedActions())) {
////                                            answer1=answers.get(pairRegion);
////                                        } else if (!psuffnull&&!suffnull && !pprenull&& !prenull && pairRegion.prefix.getTimedActions().equals(twoClockResetLogicTimeWord2.getTimedActions()) && pairRegion.suffix.getTimedActions().equals(suff.getTimedActions())) {
////                                            answer1=answers.get(pairRegion);
////                                        }
////                                    }
//                                } else {
////                                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWordList1 = new TwoClockResetLogicTimeWord(null);
////                                    twoClockResetLogicTimeWordList1 = selectfromRegion2(suff, resetWord1);
////                                    if(twoClockResetLogicTimeWordList1==null||twoClockResetLogicTimeWordList1.getTimedActions()==null||twoClockResetLogicTimeWordList1.getTimedActions().size()==0) {
////                                        boolean eq2 = false;
////                                        for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
////                                            if (pairRegion1.prefix.equals(resetWord1) && pairRegion1.suffix.equals(suff)) {
////                                                eq2 = true;
////                                                System.out.println("equal");
////                                                twoClockResetLogicTimeWordList1 = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
////                                            }
////                                        }
////                                        if (!eq2) {
//////                                            for (PairRegion pairRegion: pairRegionTwoClockResetLogicTimeWordMap.keySet()){
//////                                                System.out.println(pairRegion);
//////                                            }
//////                                            System.out.println(eq2);
//////                                            System.out.println("重新找");
////                                            if (twoClockResetLogicTimeWordList1 == null || twoClockResetLogicTimeWordList1.getTimedActions() == null || twoClockResetLogicTimeWordList1.getTimedActions().size() == 0) {
////
////                                                boolean allreset = true;
////                                                for (Clock clock : clockList) {
////                                                    if (resetWord1.getLastResetAction().isReset(clock)) {
////                                                    } else {
////                                                        allreset = false;
////                                                    }
////                                                }
////                                                boolean needselect = true;
////                                                if (allreset) {
////                                                    for (int i1 = 1; i1 < clockList.size(); i1++) {
////                                                        if (suff.get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == suff.get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
////                                                        } else {
////                                                            needselect = false;
////                                                        }
////                                                    }
////                                                }
////                                                if (needselect) {
////                                                    twoClockResetLogicTimeWordList1 = selectfromRegion(suff, resetWord1);
////                                                }
////                                            }
////                                        }
////                                    }
////                                    if (twoClockResetLogicTimeWordList1 != null&&twoClockResetLogicTimeWordList1.getTimedActions()!=null&&twoClockResetLogicTimeWordList1.getTimedActions().size()!=0) {
////                                        answer1=teacher.membership(twoClockResetLogicTimeWordList1);
////
////                                    }
//
//                                    for (PairRegion pairRegion:answers.keySet()){
////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
////                                            answer1=answers.get(pairRegion);
////                                        }
////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
////                                            answer1=answers.get(pairRegion);
////                                        }
//
//
//                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1)){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1) && pairRegion.suffix.equals(suff)){
//                                            answer1=answers.get(pairRegion);
//                                        }
//                                    }
//
////                                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWordList2 = new TwoClockResetLogicTimeWord(null);
////                                    twoClockResetLogicTimeWordList2 = selectfromRegion2(suff, resetWord2);
////                                    if(twoClockResetLogicTimeWordList2==null||twoClockResetLogicTimeWordList2.getTimedActions()==null||twoClockResetLogicTimeWordList2.getTimedActions().size()==0) {
////                                        boolean eq2 = false;
////                                        for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
////                                            if (pairRegion1.prefix.equals(resetWord2) && pairRegion1.suffix.equals(suff)) {
////                                                eq2 = true;
////                                                System.out.println("equal");
////                                                twoClockResetLogicTimeWordList2 = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
////                                            }
////                                        }
////
////                                        if (!eq2) {
//////                                            for (PairRegion pairRegion: pairRegionTwoClockResetLogicTimeWordMap.keySet()){
//////                                                System.out.println(pairRegion);
//////                                            }
//////                                            System.out.println(eq2);
//////                                            System.out.println("重新找");
////                                            if (twoClockResetLogicTimeWordList2 == null || twoClockResetLogicTimeWordList2.getTimedActions() == null || twoClockResetLogicTimeWordList2.getTimedActions().size() == 0) {
////
////                                                boolean allreset = true;
////                                                for (Clock clock : clockList) {
////                                                    if (resetWord2.getLastResetAction().isReset(clock)) {
////                                                    } else {
////                                                        allreset = false;
////                                                    }
////                                                }
////                                                boolean needselect = true;
////                                                if (allreset) {
////                                                    for (int i1 = 1; i1 < clockList.size(); i1++) {
////                                                        if (suff.get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == suff.get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
////                                                        } else {
////                                                            needselect = false;
////                                                        }
////                                                    }
////                                                }
////                                                if (needselect) {
////                                                    twoClockResetLogicTimeWordList2 = selectfromRegion(suff, resetWord2);
////                                                }
////                                            }
////                                        }
////                                    }
////                                    if (twoClockResetLogicTimeWordList2 != null&&twoClockResetLogicTimeWordList2.getTimedActions()!=null&&twoClockResetLogicTimeWordList2.getTimedActions().size()!=0) {
////                                        answer2=teacher.membership(twoClockResetLogicTimeWordList2);
////                                    }
//                                    for (PairRegion pairRegion:answers.keySet()){
////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord2) && pairRegion.suffix.equals(suff)){
////                                            answer2=answers.get(pairRegion);
////                                        }
////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord2)){
////                                            answer2=answers.get(pairRegion);
////                                        }
//
//                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2)){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                        else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2) && pairRegion.suffix.equals(suff)){
//                                            answer2=answers.get(pairRegion);
//                                        }
//                                    }
//                                }
//                                boolean reseteq=true;
//                                if(answer1.getResets()!=null&&answer2.getResets()!=null){
////System.out.println("answer1.getResets():"+answer1.getResets());
////                                    System.out.println("answer2.getResets():"+answer2.getResets());
//                                    for(int m=0;m<answer1.getResets().size();m++){
//                                        if(!answer1.getResets().get(m).equals(answer2.getResets().get(m))){
//                                            reseteq=false;
//                                        }
//                                    }
//
////                                        if(answer1.getResets()!=answer1.getResets()){
////
////                                            reseteq=false;
////                                        }
//                                }
//
//                                if(answer1.getResets()==null&&answer2.getResets()!=null||answer1.getResets()!=null&&answer2.getResets()==null){
//                                    reseteq=false;
//                                }
//                                boolean reseteq2=true;
//                                for (int m = 0; m < clockList.size(); m++) {
//                                    if (twoClockResetLogicTimeWord1.getLastResetAction().isReset(clockList.get(m)) ==
//                                            twoClockResetLogicTimeWord2.getLastResetAction().isReset(clockList.get(m))) {
//
//                                    } else {
//                                        reseteq2 = false;
//                                    }
//                                }
//                                if (answer1.isAccept() != answer2.isAccept()||!reseteq||!reseteq2) {
//                                    isconsistent=false;
//                                    //     show();
////                                    System.out.println(answer1);
////                                    System.out.println(answer2);
//                                    //        System.out.println("bbbbbbb!!");
////                                    System.out.println(pair1);
////                                    answers.put(pair1, answer1);
////                                    answers.put(pair2, answer2);
//                                    System.out.println("cccc!!!");
//                                    //     show();
////                                    List<Region> regionList = transfertoRegion(TwoClockResetLogicTimeWord.logicTimeWord(resetWord1));
////                                    List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
////                                    for (int m = 0; m < regionList.size(); m++) {
////                                        RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(resetWord1.getTimedActions().get(m).getSymbol(), regionList.get(m));
////                                        regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
////                                    }
////                                    RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
////                                    regionTwoClockLogicTimedWord.setTimedActions(regionTwoClockLogicActionList);
////                                    System.out.println("dddd!!!");
////                                    RegionTwoClockResetLogicTimedWord twoClockResetLogicTimedWord=new RegionTwoClockResetLogicTimedWord(null);
////                                    List<RegionTwoClockResetLogicAction> regionTwoClockResetLogicActionList=new ArrayList<>();
////                                    for (int m=0;m<resetWord1.getTimedActions().size();m++){
////                                        RegionTwoClockResetLogicAction regionTwoClockResetLogicAction=new RegionTwoClockResetLogicAction(resetWord1.getTimedActions().get(m).getSymbol(),resetWord1.getTimedActions().get(m).getResetClockSet(),regionTwoClockLogicTimedWord.getTimedActions().get(m).getRegion());
////                                        regionTwoClockResetLogicActionList.add(regionTwoClockResetLogicAction);
////                                    }
////                                    twoClockResetLogicTimedWord.setTimedActions(regionTwoClockResetLogicActionList);
////                                    System.out.println("eeee!!!");
////                                    RegionTwoClockResetLogicTimedWord regionTwoClockResetLogicTimedWordCouple=twoClockResetLogicTimedWord;
////
////                                    List<Region> regionList2 = transfertoRegion(TwoClockResetLogicTimeWord.logicTimeWord(resetWord2));
////                                    List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
////                                    for (int m = 0; m < regionList2.size(); m++) {
////                                        RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(resetWord2.getTimedActions().get(m).getSymbol(), regionList2.get(m));
////                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction);
////                                    }
////                                    RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
////                                    regionTwoClockLogicTimedWord2.setTimedActions(regionTwoClockLogicActionList2);
////
////                                    RegionTwoClockResetLogicTimedWord twoClockResetLogicTimedWord2=new RegionTwoClockResetLogicTimedWord(null);
////                                    List<RegionTwoClockResetLogicAction> regionTwoClockResetLogicActionList2=new ArrayList<>();
////                                    for (int m=0;m<resetWord2.getTimedActions().size();m++){
////                                        RegionTwoClockResetLogicAction regionTwoClockResetLogicAction=new RegionTwoClockResetLogicAction(resetWord2.getTimedActions().get(m).getSymbol(),resetWord2.getTimedActions().get(m).getResetClockSet(),regionTwoClockLogicTimedWord2.getTimedActions().get(m).getRegion());
////                                        regionTwoClockResetLogicActionList2.add(regionTwoClockResetLogicAction);
////                                    }
////                                    twoClockResetLogicTimedWord2.setTimedActions(regionTwoClockResetLogicActionList2);
////
////                                  RegionTwoClockResetLogicTimedWord regionTwoClockResetLogicTimedWordCouple2=twoClockResetLogicTimedWord2;
//
//
////                                    unConsistentCouple.add(regionTwoClockResetLogicTimedWordCouple);
////                                    unConsistentCouple.add(regionTwoClockResetLogicTimedWordCouple2);
////                                    unConsistentCouple.add(list.get(i));
////                                    unConsistentCouple.add(list.get(j));
//                                    unConsistentCouple.add(twoClockResetLogicTimeWord1);
//                                    unConsistentCouple.add(twoClockResetLogicTimeWord2);
//
//                                    List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
//                                    twoClockLogicActionList.add(action);
//                                    TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
//                                    twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
//                                    List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
//                                    RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
//                                    key = regionTwoClockLogicAction;
//                                    System.out.println("return false");
//                                    return false;
//                                }
//                            }
//                        }
////                        if(twoClockResetLogicTimeWord1==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null||
////                                twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null||
////                                twoClockResetLogicTimeWord2==null&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null||
////                                twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null) {
////                            boolean needsplit = false;
////                            for (Clock clock : clockList) {
////                                if (action.getValue(clock) > kc) {
////                                    needsplit = true;
////                                    break;
////                                }
////                            }
////                            if (needsplit) {
////                                Map<Clock, Double> map = new HashMap<>();
////                                for (int m = 0; m < clockList.size(); m++) {
////                                    map.put(clockList.get(m), kc + 1.0);
////                                }
////                                TwoClockLogicAction action1 = new TwoClockLogicAction(action.getSymbol(), map);
////                                action1.setSymbol(action.getSymbol());
////                                action1.setClockValueMap(map);
////                                System.out.println("action1:" + action1);
////                                System.out.println(list.get(i));
////                                System.out.println(list.get(j));
////                                List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
////                                twoClockLogicActionList.add(action1);
////                                TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
////                                twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
////                                List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
////                                List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
////                                RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action1.getSymbol(), regionList.get(0));
////                                regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
////                                regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
////                                System.out.println(regionTwoClockLogicTimedWord.get(0).getRegion().getTimeGuardList());
////                                TwoClockResetLogicTimeWord select = selectfromRegion2(regionTwoClockLogicTimedWord, list.get(i));
////                                TwoClockResetLogicTimeWord select2 = selectfromRegion2(regionTwoClockLogicTimedWord, list.get(j));
////                                System.out.println(select);
////                                System.out.println(select2);
////                                BooleanAnswer answer1 = teacher.membership(select, clockList.size());
////                                BooleanAnswer answer2 = teacher.membership(select2, clockList.size());
////                                if (answer1 != answer2) {
////                                    makeconsistent = true;
////                                    regionTwoClockLogicTimedWordmakeconsistent = null;
////                                    System.out.println("cccc!!!");
////                                    unConsistentCouple.add(twoClockResetLogicTimeWord1);
////                                    unConsistentCouple.add(twoClockResetLogicTimeWord2);
////
////                                    List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
////                                    twoClockLogicActionList2.add(action1);
////                                    TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
////                                    twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
////                                    List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
////                                    RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(action1.getSymbol(), regionList2.get(0));
////                                    key = regionTwoClockLogicAction2;
////                                    System.out.println(list.get(i));
////                                    System.out.println(list.get(j));
////                                    System.out.println(action1);
////                                    System.out.println("return false");
////
////                                    return false;
////                                }
////                            }
////                        }
////                        else if(twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord1.getTimedActions().size()!=0&&twoClockResetLogicTimeWord2==null||
////                                twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord1.getTimedActions().size()!=0&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null) {
////                            boolean needsplit = false;
////                            for (Clock clock : clockList) {
////                                if (action.getValue(clock) > kc) {
////                                    needsplit = true;
////                                    break;
////                                }
////                            }
////                            if (needsplit) {
////                                System.out.println("bourd1:"+twoClockResetLogicTimeWord1);
////                                System.out.println("bourd2:"+list.get(i));
////                                System.out.println("bourd2:"+list.get(j));
////                                List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
////                                twoClockLogicActionList.add(action);
////                                TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
////                                twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
////                                List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
////                                List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
////                                RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
////                                regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
////                                regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
////                                TwoClockResetLogicTimeWord select = selectfromRegion2(regionTwoClockLogicTimedWord, list.get(j));
////                                if (select == null || select != null && select.getTimedActions() == null) {
////                                    for (RegionTwoClockLogicTimedWord suff : suffixSet) {
////                                        BooleanAnswer answer1 = new BooleanAnswer(null, false);
////                                        BooleanAnswer answer2 = new BooleanAnswer(null, false);
////                                        if (suff == null || suff.getTimedActions().size() == 0) {
//////                                    answer1 = teacher.membership(resetWord1,clockList.size());
////                                            answer1 = teacher.membership(twoClockResetLogicTimeWord1, clockList.size());
////
////                                            answer1.setResets(null);
//////                                    answer1.setResets(null);
////                                            //   haveanswer1=true;
//////                                    answer2 = teacher.membership(resetWord2,clockList.size());
//////                                        answer2 = teacher.membership(twoClockResetLogicTimeWord2,clockList.size());
//////
//////                                        answer2.setResets(null);
////                                        } else {
//////
////
////                                            for (PairRegion pairRegion : answers.keySet()) {
//////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
//////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
////
////
////                                                if (pairRegion.suffix == null && suff == null && pairRegion.prefix == null && twoClockResetLogicTimeWord1 == null) {
////                                                    answer1 = answers.get(pairRegion);
////                                                } else if (pairRegion.suffix == null && suff == null && pairRegion.prefix != null && pairRegion.prefix.equals(twoClockResetLogicTimeWord1)) {
////                                                    answer1 = answers.get(pairRegion);
////                                                } else if (pairRegion.suffix != null && pairRegion.suffix.equals(suff) && pairRegion.prefix == null && twoClockResetLogicTimeWord1 == null) {
////                                                    answer1 = answers.get(pairRegion);
////                                                } else if (pairRegion.suffix != null && pairRegion.prefix != null && pairRegion.prefix.equals(twoClockResetLogicTimeWord1) && pairRegion.suffix.equals(suff)) {
////                                                    answer1 = answers.get(pairRegion);
////                                                }
////                                            }
////                                        }
////
////                                        if (answer1.isAccept() == true) {
//////                                        isconsistent=false;
////                                            makeconsistent = true;
////                                            regionTwoClockLogicTimedWordmakeconsistent = suff;
////                                            System.out.println("cccc!!!");
////                                            unConsistentCouple.add(twoClockResetLogicTimeWord1);
////                                            unConsistentCouple.add(twoClockResetLogicTimeWord2);
////
////                                            List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
////                                            twoClockLogicActionList2.add(action);
////                                            TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
////                                            twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
////                                            List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
////                                            RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(action.getSymbol(), regionList2.get(0));
////                                            key = regionTwoClockLogicAction2;
////                                            System.out.println(list.get(i));
////                                            System.out.println(list.get(j));
////                                            System.out.println(action);
////                                            System.out.println("return false");
////
////                                            return false;
////                                        }
////                                    }
////                                }
////                            }
////                        }
////                        else if(twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()!=0&&twoClockResetLogicTimeWord1==null||
////                                twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()!=0&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()==null) {
////                            boolean needsplit = false;
////                            for (Clock clock : clockList) {
////                                if (action.getValue(clock) > kc) {
////                                    needsplit = true;
////                                    break;
////                                }
////                            }
////                            if (needsplit) {
////                                System.out.println("bourd1:"+twoClockResetLogicTimeWord2);
////                                System.out.println("bourd2:"+list.get(i));
////                                System.out.println("bourd2:"+list.get(j));
////                                List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
////                                twoClockLogicActionList.add(action);
////                                TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
////                                twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
////                                List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
////                                List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
////                                RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
////                                regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
////                                regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
////                                TwoClockResetLogicTimeWord select = selectfromRegion2(regionTwoClockLogicTimedWord, list.get(i));
////                                if (select == null || select != null && select.getTimedActions() == null) {
////                                    for (RegionTwoClockLogicTimedWord suff : suffixSet) {
////                                        BooleanAnswer answer1 = new BooleanAnswer(null, false);
////                                        BooleanAnswer answer2 = new BooleanAnswer(null, false);
////                                        if (suff == null || suff.getTimedActions().size() == 0) {
//////                                    answer1 = teacher.membership(resetWord1,clockList.size());
//////                                        answer1 = teacher.membership(twoClockResetLogicTimeWord1,clockList.size());
//////
//////                                        answer1.setResets(null);
//////                                    answer1.setResets(null);
////                                            //   haveanswer1=true;
//////                                    answer2 = teacher.membership(resetWord2,clockList.size());
////                                            answer2 = teacher.membership(twoClockResetLogicTimeWord2, clockList.size());
////
////                                            answer2.setResets(null);
////                                        } else {
//////
////
////                                            for (PairRegion pairRegion : answers.keySet()) {
//////                                        if(pairRegion.suffix!=null&&pairRegion.prefix.equals(resetWord1) && pairRegion.suffix.equals(suff)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
//////                                        if(pairRegion.suffix==null&&suff==null&&pairRegion.prefix.equals(resetWord1)){
//////                                            answer1=answers.get(pairRegion);
//////                                        }
////
////
////                                                if (pairRegion.suffix == null && suff == null && pairRegion.prefix == null && twoClockResetLogicTimeWord2 == null) {
////                                                    answer2 = answers.get(pairRegion);
////                                                } else if (pairRegion.suffix == null && suff == null && pairRegion.prefix != null && pairRegion.prefix.equals(twoClockResetLogicTimeWord2)) {
////                                                    answer2 = answers.get(pairRegion);
////                                                } else if (pairRegion.suffix != null && pairRegion.suffix.equals(suff) && pairRegion.prefix == null && twoClockResetLogicTimeWord2 == null) {
////                                                    answer2 = answers.get(pairRegion);
////                                                } else if (pairRegion.suffix != null && pairRegion.prefix != null && pairRegion.prefix.equals(twoClockResetLogicTimeWord2) && pairRegion.suffix.equals(suff)) {
////                                                    answer2 = answers.get(pairRegion);
////                                                }
////                                            }
////                                        }
////
////                                        if (answer2.isAccept() == true) {
//////                                        isconsistent=false;
////                                            makeconsistent = true;
////                                            regionTwoClockLogicTimedWordmakeconsistent = suff;
////                                            System.out.println("cccc!!!");
////                                            unConsistentCouple.add(twoClockResetLogicTimeWord1);
////                                            unConsistentCouple.add(twoClockResetLogicTimeWord2);
////
////                                            List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
////                                            twoClockLogicActionList2.add(action);
////                                            TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
////                                            twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
////                                            List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
////                                            RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(action.getSymbol(), regionList2.get(0));
////                                            key = regionTwoClockLogicAction2;
////                                            System.out.println(list.get(i));
////                                            System.out.println(list.get(j));
////                                            System.out.println("return false");
////                                            return false;
////                                        }
////                                    }
////                                }
////                            }
////                        }
//                    }
//                }
//            }
//        }
//        return true;
//    }

    public  boolean isConsistent(){
        unConsistentCouple = new ArrayList<>();
        Set<TwoClockLogicAction> logicActionSet = getLastActionSet();
        List<TwoClockResetLogicTimeWord> list = getPrefixList();
        //System.out.println("list:"+list);
        for (int i = 0; i < list.size(); i++) {
            Row row1 = row(list.get(i));
            for (int j = i + 1; j < list.size(); j++) {
                Row row2 = row(list.get(j));
                boolean eq=true;
                for (int k = 0; k < row1.size(); k++) {

                    if(row1.get(k)==null&&row2.get(k)==null||
                            row1.get(k)!=null&&row2.get(k)!=null&&
                                    row1.get(k).equals(row2.get(k))){
                    }
                    else {
                        eq=false;
                    }
                }
                if (eq) {
//                    System.out.println(list.get(i));
//                    System.out.println(list.get(j));
                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord22=new TwoClockResetLogicTimeWord(null);
                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord12=new TwoClockResetLogicTimeWord(null);
                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=new TwoClockResetLogicTimeWord(null);
                    twoClockResetLogicTimeWord1.setTimedActions(null);
                    TwoClockResetLogicTimeWord twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(null);
                    twoClockResetLogicTimeWord2.setTimedActions(null);
                    PairRegion pair1=new PairRegion(null,null);
                    PairRegion pair2=new PairRegion(null,null);
                    for (TwoClockLogicAction action : logicActionSet) {
                        //    System.out.println("action:"+action);
                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
                        twoClockResetLogicTimeWord1=new TwoClockResetLogicTimeWord(null);
                        twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(null);
                        TwoClockLogicTimeWord word1;
                        {

                            List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
                            twoClockLogicActionList.add(action);
                            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
                            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
                            List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
                            List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
                            regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);

                            for (TwoClockResetLogicTimeWord twoClockResetLogicTimeWord : list) {
                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() >= 2 && list.get(i).getTimedActions() != null) {

                                    boolean preeq = true;
                                    if (twoClockResetLogicTimeWord.size() - 1 != list.get(i).size()) {
                                        preeq = false;
                                    }
                                    else {
                                        for (int k = 0; k < twoClockResetLogicTimeWord.size() - 1; k++) {
                                            for (int q = 0; q < clockList.size(); q++) {
                                                if (Double.doubleToLongBits(twoClockResetLogicTimeWord.getTimedActions().get(k).getValue(clockList.get(q))) ==Double.doubleToLongBits(list.get(i).getTimedActions().get(k).getValue(clockList.get(q)))   &&
                                                        twoClockResetLogicTimeWord.getTimedActions().get(k).getSymbol().equals(list.get(i).getTimedActions().get(k).getSymbol())) {

                                                } else {
                                                    preeq = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }


                                    if (preeq) {
                                        //       System.out.println("preeq:"+twoClockResetLogicTimeWord);
                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
                                        //   System.out.println("twoClockLogicAction:"+twoClockLogicAction);
                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
                                        twoClockLogicActionList2.add(twoClockLogicAction);
                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
                                        //   System.out.println("twoClockLogicTimeWord2:"+twoClockLogicTimeWord2);
                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
                                        boolean eq2 = true;

                                        for (int q = 0; q < clockList.size(); q++) {
                                            if (regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {

                                            } else {
                                                eq2 = false;
                                                break;
                                            }
                                        }
                                        if(eq2){
                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
                                                    eq2=false;
                                                    break;
                                                }
                                            }
                                        }
                                        if (eq2) {
                                            twoClockResetLogicTimeWord1 = twoClockResetLogicTimeWord;
                                        }
                                    }
                                }
                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() == 1) {
                                    if (list.get(i) == null || list.get(i).getTimedActions() == null || list.get(i).getTimedActions().size() == 0) {
                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
                                        twoClockLogicActionList2.add(twoClockLogicAction);
                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
                                        boolean eq2 = true;
                                        for (int q = 0; q < clockList.size(); q++) {
                                            if (regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {

                                            } else {
                                                eq2 = false;
                                                break;
                                            }
                                        }
                                        if(eq2){
                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
                                                    eq2=false;
                                                    break;
                                                }
                                            }
                                        }
                                        if (eq2) {
                                            twoClockResetLogicTimeWord1 = twoClockResetLogicTimeWord;
                                        }
                                    }
                                }
                            }
                            //    System.out.println("twoClockResetLogicTimeWord1:"+twoClockResetLogicTimeWord1);
                            // List<TwoClockResetLogicTimeWord> twoClockResetLogicTimeWord=selectfromRegion(regionTwoClockLogicTimedWord,list.get(i),0, regionTwoClockLogicTimedWord.size(),logicTimeWord2, x, y);
                        }
                        TwoClockLogicTimeWord word2;
                        {
                            List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
                            twoClockLogicActionList.add(action);
                            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
                            twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
                            List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
                            List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
                            regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);

                            if (list.get(j).getTimedActions() != null) {
                                pair2.prefix = list.get(j);
                            } else {
                                pair2.prefix = null;
                            }
                            pair2.suffix = regionTwoClockLogicTimedWord;
                            for (TwoClockResetLogicTimeWord twoClockResetLogicTimeWord : list) {

                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() >= 2 && list.get(j).getTimedActions() != null) {

                                    boolean preeq = true;
                                    if (twoClockResetLogicTimeWord.size() - 1 != list.get(j).size()) {
                                        preeq = false;
                                    } else {
                                        for (int k = 0; k < twoClockResetLogicTimeWord.size() - 1; k++) {
                                            for (int q = 0; q < clockList.size(); q++) {
                                                if (Double.doubleToLongBits(twoClockResetLogicTimeWord.getTimedActions().get(k).getValue(clockList.get(q)))== Double.doubleToLongBits(list.get(j).getTimedActions().get(k).getValue(clockList.get(q)))  &&
                                                        twoClockResetLogicTimeWord.getTimedActions().get(k).getSymbol().equals(list.get(j).getTimedActions().get(k).getSymbol())) {

                                                } else {
                                                    preeq = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (preeq) {
                                        //      System.out.println("preeq:"+twoClockResetLogicTimeWord);
                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
                                        twoClockLogicActionList2.add(twoClockLogicAction);
                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//                                        System.out.println("222:"+regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList());
//                                        System.out.println(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList());
                                        boolean eq2 = true;
//                                        System.out.println("deter");
//                                        System.out.println(regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol());
//                                        System.out.println(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol());
//                                        System.out.println(regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().getClass().toString());
//                                        System.out.println(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol().getClass().toString());
//                                        System.out.println(regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol()));
                                        for (int q = 0; q < clockList.size(); q++) {
                                            BigDecimal bigDecimal=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
                                            BigDecimal bigDecimal2=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound());
                                            BigDecimal bigDecimal3=new BigDecimal(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());
                                            BigDecimal bigDecimal4=new BigDecimal(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound());

                                            if (regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {
//                                            if (bigDecimal.subtract(bigDecimal2).doubleValue()==0.0 &&
//                                                    bigDecimal3.subtract(bigDecimal4).doubleValue()==0.0 &&                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
//                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {

                                            } else {
                                                eq2 = false;
                                                break;
                                            }
                                        }
                                        if(eq2){
                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
                                                    eq2=false;
                                                    break;
                                                }
                                            }
                                        }
                                        if (eq2) {
                                            twoClockResetLogicTimeWord2 = twoClockResetLogicTimeWord;
                                            //           System.out.println("eq twoClockResetLogicTimeWord2:"+twoClockResetLogicTimeWord2);
                                        }
                                    }
                                }
                                if (twoClockResetLogicTimeWord != null && twoClockResetLogicTimeWord.getTimedActions() != null && twoClockResetLogicTimeWord.getTimedActions().size() != 0 && twoClockResetLogicTimeWord.getTimedActions().size() == 1) {
                                    if (list.get(j) == null || list.get(j).getTimedActions() == null || list.get(j).getTimedActions().size() == 0) {
                                        TwoClockLogicAction twoClockLogicAction = twoClockResetLogicTimeWord.getLastLogicAction();
                                        List<TwoClockLogicAction> twoClockLogicActionList2 = new ArrayList<>();
                                        twoClockLogicActionList2.add(twoClockLogicAction);
                                        TwoClockLogicTimeWord twoClockLogicTimeWord2 = new TwoClockLogicTimeWord(twoClockLogicActionList2);
                                        twoClockLogicTimeWord2.setTimedActions(twoClockLogicActionList2);
                                        List<Region> regionList2 = transfertoRegion(twoClockLogicTimeWord2);
                                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
                                        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(twoClockLogicAction.getSymbol(), regionList2.get(0));
                                        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
                                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
                                        boolean eq2 = true;
                                        for (int q = 0; q < clockList.size(); q++) {
                                            if (regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getLowerBound() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).getUpperBound()  &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isLowerBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getTimeGuardList().get(q).isUpperBoundOpen() &&
                                                    regionTwoClockLogicTimedWord.getTimedActions().get(0).getSymbol().equals(regionTwoClockLogicTimedWord2.getTimedActions().get(0).getSymbol())) {

                                            } else {
                                                eq2 = false;
                                                break;
                                            }
                                        }
                                        if(eq2){
                                            for (int i1=0;i1<regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList().length;i1++){
                                                if(regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().getDifferList()[i1]!=
                                                        regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().getDifferList()[i1]){
                                                    eq2=false;
                                                    break;
                                                }
                                            }
                                        }
                                        if (eq2) {
//                                                regionTwoClockLogicTimedWord.getTimedActions().get(0).getRegion().differxy*100 == regionTwoClockLogicTimedWord2.getTimedActions().get(0).getRegion().differxy*100) {
                                            twoClockResetLogicTimeWord2 = twoClockResetLogicTimeWord;
                                        }
                                    }
                                }
                            }

                        }


//                        System.out.println("twoClockResetLogicTimeWord1:"+twoClockResetLogicTimeWord1);
//                        System.out.println("twoClockResetLogicTimeWord2:"+twoClockResetLogicTimeWord2);
                        if (twoClockResetLogicTimeWord1 != null && twoClockResetLogicTimeWord2!= null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null) {
//System.out.println("twoClockResetLogicTimeWord1:"+twoClockResetLogicTimeWord1);
//                            System.out.println("twoClockResetLogicTimeWord2:"+twoClockResetLogicTimeWord2);
//                            System.out.println("list.get(i):"+list.get(i));
//                            System.out.println("list.get(j):"+list.get(j));
//                            word1=TwoClockResetLogicTimeWord.logicTimeWord(twoClockResetLogicTimeWord1);
//                            word2=TwoClockResetLogicTimeWord.logicTimeWord(twoClockResetLogicTimeWord2);
//                            TwoClockResetLogicTimeWord resetWord1 = teacher.transferWord(word1);
//                            TwoClockResetLogicTimeWord resetWord2 = teacher.transferWord(word2);
                            boolean isconsistent=true;
                            for(RegionTwoClockLogicTimedWord suff:suffixSet) {
                                BooleanAnswer answer1=new BooleanAnswer(null,false);
                                BooleanAnswer answer2=new BooleanAnswer(null,false);
                                if (suff == null || suff.getTimedActions().size() == 0) {
                                    for (PairRegion pairRegion:answers.keySet()){
                                        if(pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null||
                                                pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()==null||
                                                pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1!=null&&twoClockResetLogicTimeWord1.getTimedActions()!=null&&twoClockResetLogicTimeWord1.getTimedActions().size()==0){
                                            answer1=answers.get(pairRegion);
                                        }
                                        else if(pairRegion.suffix==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1)){
                                            answer1=answers.get(pairRegion);
                                        }
                                    }
                                    for (PairRegion pairRegion:answers.keySet()){
                                        if(pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null||
                                                pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null||
                                                pairRegion.suffix==null&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()==0){
                                            answer2=answers.get(pairRegion);
                                        }
                                        else if(pairRegion.suffix==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2)){
                                            answer2=answers.get(pairRegion);
                                        }
                                    }
                                } else {

                                    for (PairRegion pairRegion:answers.keySet()){
                                        if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord1==null){
                                            answer1=answers.get(pairRegion);
                                        }
                                        else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord1) && pairRegion.suffix.equals(suff)){
                                            answer1=answers.get(pairRegion);
                                        }
                                    }
                                    for (PairRegion pairRegion:answers.keySet()){
                                        if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2==null||
                                                pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()==null||
                                                pairRegion.suffix!=null&& pairRegion.suffix.equals(suff)&&pairRegion.prefix==null&&twoClockResetLogicTimeWord2!=null&&twoClockResetLogicTimeWord2.getTimedActions()!=null&&twoClockResetLogicTimeWord2.getTimedActions().size()==0){
                                            answer2=answers.get(pairRegion);
                                        }
                                        else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(twoClockResetLogicTimeWord2) && pairRegion.suffix.equals(suff)){
                                            answer2=answers.get(pairRegion);
                                        }
                                    }
                                }
                                boolean reseteq=true;
                                if(answer1.getResets()!=null&&answer2.getResets()!=null){
//System.out.println("answer1.getResets():"+answer1.getResets());
//                                    System.out.println("answer2.getResets():"+answer2.getResets());
                                    for(int m=0;m<answer1.getResets().size();m++){
                                        if(!answer1.getResets().get(m).equals(answer2.getResets().get(m))){
                                            reseteq=false;
                                        }
                                    }

//                                        if(answer1.getResets()!=answer1.getResets()){
//
//                                            reseteq=false;
//                                        }
                                }

                                if(answer1.getResets()==null&&answer2.getResets()!=null||answer1.getResets()!=null&&answer2.getResets()==null){
                                    reseteq=false;
                                }
                                boolean reseteq2=true;
                                for (int m = 0; m < clockList.size(); m++) {
                                    if (twoClockResetLogicTimeWord1.getLastResetAction().isReset(clockList.get(m)) ==
                                            twoClockResetLogicTimeWord2.getLastResetAction().isReset(clockList.get(m))) {

                                    } else {
                                        reseteq2 = false;
                                    }
                                }
                                if (answer1.isAccept() != answer2.isAccept()||!reseteq||!reseteq2) {
                                    isconsistent=false;

                                    unConsistentCouple.add(twoClockResetLogicTimeWord1);
                                    unConsistentCouple.add(twoClockResetLogicTimeWord2);

                                    List<TwoClockLogicAction> twoClockLogicActionList = new ArrayList<>();
                                    twoClockLogicActionList.add(action);
                                    TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(twoClockLogicActionList);
                                    twoClockLogicTimeWord.setTimedActions(twoClockLogicActionList);
                                    List<Region> regionList = transfertoRegion(twoClockLogicTimeWord);
                                    RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(action.getSymbol(), regionList.get(0));
                                    key = regionTwoClockLogicAction;
                                    System.out.println("return false");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    private void makeConsistent() {
        System.out.println("unConsistentCouple"+unConsistentCouple);
            TwoClockResetLogicTimeWord word1 = unConsistentCouple.get(0);
            TwoClockResetLogicTimeWord word2 = unConsistentCouple.get(1);
            for (RegionTwoClockLogicTimedWord w : suffixSet) {
                PairRegion pair1 = new PairRegion(word1, w);
                pair1.prefix = word1;
                if (w==null||w!=null&&w.getTimedActions()==null||w!=null&&w.getTimedActions()!=null&&w.getTimedActions().size() == 0) {
                    pair1.suffix = null;
                } else {
                    pair1.suffix = w;
                }
                PairRegion pair2 = new PairRegion(word2, w);
                pair2.prefix = word2;
                if (w==null||w!=null&&w.getTimedActions()==null||w!=null&&w.getTimedActions()!=null&&w.getTimedActions().size() == 0) {
                    pair2.suffix = null;
                } else {
                    pair2.suffix = w;
                }
                BooleanAnswer answer1 = answers.get(pair1);
                BooleanAnswer answer2 = answers.get(pair2);
                boolean reseteq = true;
                boolean reseteqsuff = true;

                for (int m = 0; m < clockList.size(); m++) {
                    if (word1.get(word1.size() - 1).isReset(clockList.get(m)) ==
                            word2.get(word2.size() - 1).isReset(clockList.get(m))) {

                    } else {
                        reseteq = false;
                    }
                }

                if (w != null && w.getTimedActions() != null && w.getTimedActions().size() != 0) {
                for(int i=0;i<answer1.getResets().size();i++){
                    if(!answer1.getResets().get(i).equals(answer2.getResets().get(i))){
                        reseteqsuff=false;
                    }
                }
                }
                if (!reseteq) {
                    RegionTwoClockLogicTimedWord word = consistentWord(key, null);
                    suffixSet.add(word);
                    break;
                } else if (!answer1.equals(answer2) || !reseteqsuff) {
                    RegionTwoClockLogicTimedWord word = consistentWord(key, w);
                    suffixSet.add(word);
                    break;
                }
            }
            int old=member;
            fillTable();
            membertable+=member-old;
    }
    private List<Region> transfertoRegion(TwoClockLogicTimeWord word){
        if(clockList.size()==2) {
            List<Region> RegionList = new ArrayList<>();
            for (int i = 0; i < word.size(); i++) {
                boolean[] lowerboundopen = new boolean[clockList.size()];
                boolean[] upperboundopen = new boolean[clockList.size()];
                int[] lower = new int[clockList.size()];
                int[] upper = new int[clockList.size()];
                List<TimeGuard> timeGuardList = new ArrayList<>();
                boolean maxregion = true;
                boolean maxregionone = false;
                for (int m = 0; m < clockList.size(); m++) {
                    BigDecimal bigDecimal = new BigDecimal(word.get(i).getValue(clockList.get(m)));
//                    BigDecimal bigDecimal1 = new BigDecimal(kc[m]);
                    BigDecimal bigDecimal1 = new BigDecimal(kc);
                    if (bigDecimal.subtract(bigDecimal1).doubleValue() <= 0) {
                        maxregion = false;
                        break;
                    }
                }
                if (word.get(i).getValue(clockList.get(0)) <= kc && word.get(i).getValue(clockList.get(1)) > kc ||
                        word.get(i).getValue(clockList.get(1)) <= kc && word.get(i).getValue(clockList.get(0)) > kc) {
                    maxregionone = true;
                }
                if (maxregion) {
                    for (int m = 0; m < clockList.size(); m++) {
                        TimeGuard timeGuard = new TimeGuard(true, true, kc, TimeGuard.MAX_TIME);
                        timeGuardList.add(timeGuard);
                    }
                    int q = 0;
                    for (int m = 1; m < clockList.size(); m++) {
                        q += clockList.size() - m;
                    }
                    int[] differList = new int[q];
                    Region region = new Region(timeGuardList, differList);
                    RegionList.add(region);
                } else if (maxregionone) {
                    if (word.get(i).getValue(clockList.get(0)) <= kc && word.get(i).getValue(clockList.get(1)) > kc) {
                        lower[0] = (int) word.get(i).getValue(clockList.get(0));
                        upper[0] = (int) word.get(i).getValue(clockList.get(0));
                        if (word.get(i).getValue(clockList.get(0)) * 10 % 10 != 0) {
                            lowerboundopen[0] = true;
                            upperboundopen[0] = true;
                            lower[0] = (int) word.get(i).getValue(clockList.get(0));
                            upper[0] = (int) word.get(i).getValue(clockList.get(0)) + 1;
                        }
                        TimeGuard timeGuard = new TimeGuard(lowerboundopen[0], upperboundopen[0], lower[0], upper[0]);
                        timeGuardList.add(timeGuard);
                        TimeGuard timeGuard2 = new TimeGuard(true, true, kc, TimeGuard.MAX_TIME);
                        timeGuardList.add(timeGuard2);
                        int q = 0;
                        for (int m = 1; m < clockList.size(); m++) {
                            q += clockList.size() - m;
                        }
                        int[] differList = new int[q];
                        Region region = new Region(timeGuardList, differList);
                        RegionList.add(region);
                    } else {
                        TimeGuard timeGuard = new TimeGuard(true, true, kc, TimeGuard.MAX_TIME);
                        timeGuardList.add(timeGuard);
                        lower[1] = (int) word.get(i).getValue(clockList.get(1));
                        upper[1] = (int) word.get(i).getValue(clockList.get(1));
                        if (word.get(i).getValue(clockList.get(1)) * 10 % 10 != 0) {
                            lowerboundopen[1] = true;
                            upperboundopen[1] = true;
                            lower[1] = (int) word.get(i).getValue(clockList.get(1));
                            upper[1] = (int) word.get(i).getValue(clockList.get(1)) + 1;
                        }
                        TimeGuard timeGuard2 = new TimeGuard(lowerboundopen[1], upperboundopen[1], lower[1], upper[1]);
                        timeGuardList.add(timeGuard2);
                        int q = 0;
                        for (int m = 1; m < clockList.size(); m++) {
                            q += clockList.size() - m;
                        }
                        int[] differList = new int[q];
                        Region region = new Region(timeGuardList, differList);
                        RegionList.add(region);
                    }
                } else {
                    for (int m = 0; m < clockList.size(); m++) {
                        lower[m] = (int) word.get(i).getValue(clockList.get(m));
                        upper[m] = (int) word.get(i).getValue(clockList.get(m));
                        if (word.get(i).getValue(clockList.get(m)) * 10 % 10 != 0) {
                            lowerboundopen[m] = true;
                            upperboundopen[m] = true;
                            lower[m] = (int) word.get(i).getValue(clockList.get(m));
                            upper[m] = (int) word.get(i).getValue(clockList.get(m)) + 1;
                        }
                        TimeGuard timeGuard = new TimeGuard(lowerboundopen[m], upperboundopen[m], lower[m], upper[m]);
                        timeGuardList.add(timeGuard);
                    }
                    int q = 0;
                    for (int m = 1; m < clockList.size(); m++) {
                        q += clockList.size() - m;
                    }
                    int[] differList = new int[q];
                    for (int m = 0; m < clockList.size() - 1; m++) {
                        for (int n = m + 1; n < clockList.size(); n++) {
                            if (word.get(i).getValue(clockList.get(m)) * 100 % 100 > word.get(i).getValue(clockList.get(n)) * 100 % 100 && word.get(i).getValue(clockList.get(m)) * 100 % 100 != 0 && word.get(i).getValue(clockList.get(n)) * 100 % 100 != 0) {
                                int index = 0;
                                for (int l = 1; l < m + 1; l++) {
                                    index += clockList.size() - l;
                                }
                                differList[index - 1 + n - m] = 1;
                            }
                            if (word.get(i).getValue(clockList.get(m)) * 100 % 100 < word.get(i).getValue(clockList.get(n)) * 100 % 100 && word.get(i).getValue(clockList.get(m)) * 100 % 100 != 0 && word.get(i).getValue(clockList.get(n)) * 100 % 100 != 0) {
                                int index = 0;
                                for (int l = 1; l < m + 1; l++) {
                                    index += clockList.size() - l;
                                }
                                differList[index - 1 + n - m] = -1;
                            }
                        }
                    }

                    Region region = new Region(timeGuardList, differList);
                    RegionList.add(region);
                }
            }
            return RegionList;
        }
        if(clockList.size()==1){
            List<Region> RegionList = new ArrayList<>();
            for (int i = 0; i < word.size(); i++) {
                boolean[] lowerboundopen = new boolean[clockList.size()];
                boolean[] upperboundopen = new boolean[clockList.size()];
                int[] lower = new int[clockList.size()];
                int[] upper = new int[clockList.size()];
                List<TimeGuard> timeGuardList = new ArrayList<>();
                boolean maxregion = true;
                for (int m = 0; m < clockList.size(); m++) {
                    BigDecimal bigDecimal = new BigDecimal(word.get(i).getValue(clockList.get(m)));
                    BigDecimal bigDecimal1 = new BigDecimal(kc);
                    if (bigDecimal.subtract(bigDecimal1).doubleValue() <= 0) {
                        maxregion = false;
                        break;
                    }
                }
                if (maxregion) {
                    for (int m = 0; m < clockList.size(); m++) {
                        TimeGuard timeGuard = new TimeGuard(true, true, kc, TimeGuard.MAX_TIME);
                        timeGuardList.add(timeGuard);
                    }
                    int q = 0;
                    for (int m = 1; m < clockList.size(); m++) {
                        q += clockList.size() - m;
                    }
                    int[] differList = new int[q];
                    Region region = new Region(timeGuardList, differList);
                    RegionList.add(region);
                }  else {
                    for (int m = 0; m < clockList.size(); m++) {
                        lower[m] = (int) word.get(i).getValue(clockList.get(m));
                        upper[m] = (int) word.get(i).getValue(clockList.get(m));
                        if (word.get(i).getValue(clockList.get(m)) * 10 % 10 != 0) {
                            lowerboundopen[m] = true;
                            upperboundopen[m] = true;
                            lower[m] = (int) word.get(i).getValue(clockList.get(m));
                            upper[m] = (int) word.get(i).getValue(clockList.get(m)) + 1;
                        }
                        TimeGuard timeGuard = new TimeGuard(lowerboundopen[m], upperboundopen[m], lower[m], upper[m]);
                        timeGuardList.add(timeGuard);
                    }
                    int q = 0;
                    for (int m = 1; m < clockList.size(); m++) {
                        q += clockList.size() - m;
                    }
                    int[] differList = new int[q];
                    for (int m = 0; m < clockList.size() - 1; m++) {
                        for (int n = m + 1; n < clockList.size(); n++) {
                            if (word.get(i).getValue(clockList.get(m)) * 100 % 100 > word.get(i).getValue(clockList.get(n)) * 100 % 100 && word.get(i).getValue(clockList.get(m)) * 100 % 100 != 0 && word.get(i).getValue(clockList.get(n)) * 100 % 100 != 0) {
                                int index = 0;
                                for (int l = 1; l < m + 1; l++) {
                                    index += clockList.size() - l;
                                }
                                differList[index - 1 + n - m] = 1;
                            }
                            if (word.get(i).getValue(clockList.get(m)) * 100 % 100 < word.get(i).getValue(clockList.get(n)) * 100 % 100 && word.get(i).getValue(clockList.get(m)) * 100 % 100 != 0 && word.get(i).getValue(clockList.get(n)) * 100 % 100 != 0) {
                                int index = 0;
                                for (int l = 1; l < m + 1; l++) {
                                    index += clockList.size() - l;
                                }
                                differList[index - 1 + n - m] = -1;
                            }
                        }
                    }

                    Region region = new Region(timeGuardList, differList);
                    RegionList.add(region);
                }
            }
            return RegionList;
        }
        return null;
    }

    private RegionTwoClockLogicTimedWord consistentWord(RegionTwoClockLogicAction logicTimedAction, RegionTwoClockLogicTimedWord logicTimeWord) {
        List<RegionTwoClockLogicAction> logicTimedActions = new ArrayList<>();
        logicTimedActions.add(logicTimedAction);
        if(logicTimeWord!=null&&logicTimeWord.getTimedActions()!=null){
        logicTimedActions.addAll(logicTimeWord.getTimedActions());}
        RegionTwoClockLogicTimedWord twoClockLogicTimeWord=new RegionTwoClockLogicTimedWord(logicTimedActions);
        twoClockLogicTimeWord.setTimedActions(logicTimedActions);
        return twoClockLogicTimeWord;
    }


private Set<TwoClockLogicAction> getLastActionSet() {
    List<TwoClockResetLogicTimeWord> sr = getPrefixSet();
    Set<TwoClockLogicAction> lastActionSet = new HashSet<>();
    for (TwoClockResetLogicTimeWord resetWord : sr) {
        if (!resetWord.isEmpty()) {
            TwoClockLogicAction last = resetWord.getLastLogicAction();
            lastActionSet.add(last);
        }
    }
    return lastActionSet;
}


    private List<TwoClockResetLogicTimeWord> getPrefixSet() {
        List<TwoClockResetLogicTimeWord> sr = new ArrayList<>();
        sr.addAll(s);
        sr.addAll(r);
        return sr;
    }

private List<TwoClockResetLogicTimeWord> getPrefixList() {
    List<TwoClockResetLogicTimeWord> logicTimeWordList = new ArrayList<>();
    logicTimeWordList.addAll(s);
    logicTimeWordList.addAll(r);
    return logicTimeWordList;
}


    @Data
    @AllArgsConstructor
    private static class Pair {
        private TwoClockResetLogicTimeWord prefix;//前缀是逻辑的，之后要改成delay
        private TwoClockLogicTimeWord suffix;
        public TwoClockLogicTimeWord timeWord() {
            if(prefix.getTimedActions()!=null){
            TwoClockLogicTimeWord pre = TwoClockResetLogicTimeWord.logicTimeWord(prefix);
          //  System.out.println("pre:"+pre);//把前缀的重置信息去掉
            return TwoClockLogicTimeWord.concat(pre,suffix);}
            else{
                //    return TwoClockLogicTimeWord.concat(TwoClockLogicTimeWord.emptyWord(),suffix);
                return suffix;
            }
        }
    }

    @Data
    private static class Row {
        private List<BooleanAnswer> answers;

        public Row() {
            answers = new ArrayList<>();
        }

        public int size() {
            return answers.size();
        }

        public BooleanAnswer get(int i) {
            return answers.get(i);
        }

        public void add(BooleanAnswer answer) {
            answers.add(answer);
        }

        @Override
        public int hashCode() {
            int hash = 0;
            int i = 1;
            for (BooleanAnswer b : answers) {
                if (b!=null&&b.isAccept() == true) {
                    hash += i;
                    i *= 2;
                }
            }
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            Row row = (Row) o;
            if (this.size() != row.size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if(this.get(i)==null&&row.get(i)==null){
                    return true;
                }
               else if (this.get(i)==null&&row.get(i)!=null||this.get(i)!=null&&row.get(i)==null||this.get(i).isAccept() != row.get(i).isAccept()||this.get(i).getResets()!=null&&row.get(i).getResets()!=null&&!this.get(i).getResets().equals(row.get(i).getResets())) {
                    return false;
                }
            }
            return true;
        }
    }

    public DelayTimeWord LogictoDelay(TwoClockResetLogicTimeWord timeWord){
        //   System.out.println(timeWord);
        List<DelayTimedAction> delayTimedActions = new ArrayList<>();
        for (Clock clock : clockList) {
            BigDecimal b3 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(0).getValue(clock)));
            BigDecimal b4 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(0).getValue(clockList.get(0))));
//            System.out.println(b3.subtract(b4).doubleValue());
            if (b3.subtract(b4).doubleValue() == 0.0) {

            } else {
                return null;
            }
        }
        //     System.out.println("合法");
        DelayTimedAction delayTimedAction = new DelayTimedAction(timeWord.getTimedActions().get(0).getSymbol(), timeWord.getTimedActions().get(0).getValue(clockList.get(0)));
        delayTimedAction.setSymbol(timeWord.getTimedActions().get(0).getSymbol());
        delayTimedAction.setValue(timeWord.getTimedActions().get(0).getValue(clockList.get(0)));
        delayTimedActions.add(delayTimedAction);
        for (int i1 = 0; i1 < timeWord.getTimedActions().size()-1; i1++) {
            double defference;

            BigDecimal b1 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1).getValue(clockList.get(0))));
            BigDecimal b2 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1 + 1).getValue(clockList.get(0))));
            if(timeWord.getTimedActions().get(i1).isReset(clockList.get(0))){
                defference=timeWord.getTimedActions().get(i1+1).getValue(clockList.get(0));
            }
            else {
                defference=b2.subtract(b1).doubleValue();
            }

            for (int m=1;m<clockList.size();m++) {
                double defferenceeach;

                BigDecimal b3 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1).getValue(clockList.get(m))));
                BigDecimal b4 = new BigDecimal(Double.toString(timeWord.getTimedActions().get(i1 + 1).getValue(clockList.get(m))));
                if(timeWord.getTimedActions().get(i1).isReset(clockList.get(m))){
                    defferenceeach=timeWord.getTimedActions().get(i1+1).getValue(clockList.get(m));
                }
                else {
                    defferenceeach=b4.subtract(b3).doubleValue();
                }
                BigDecimal b5 = new BigDecimal(Double.toString(defference));
                BigDecimal b6 = new BigDecimal(Double.toString(defferenceeach));

                if (b5.subtract(b6).doubleValue() == 0.0) {

                } else {
                    return null;
                }
            }
            DelayTimedAction delayTimedAction2 = new DelayTimedAction(timeWord.getTimedActions().get(i1 + 1).getSymbol(), defference);
            delayTimedAction2.setSymbol(timeWord.getTimedActions().get(i1 + 1).getSymbol());
            delayTimedAction2.setValue(defference);
            delayTimedActions.add(delayTimedAction2);
        }
        DelayTimeWord twoClockTimedWord = new DelayTimeWord(delayTimedActions);
        //   twoClockTimedWord.setTimedActions(delayTimedActions);
        return twoClockTimedWord;
    }
}
