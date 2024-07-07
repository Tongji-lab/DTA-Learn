package classificationTree;

import classificationTree.node.InnerNode;
import classificationTree.node.LeafNode;
import classificationTree.node.Node;
import classificationTree.node.SiftResult;
import com.microsoft.z3.*;
import defaultTeacher.PairRegion;
import frame.Learner;
import frame.TwoClockTeacher;
import lombok.Data;
import ta.*;
import ta.ota.*;
import ta.twoClockTA.*;
import timedAction.DelayTimedAction;
import timedAction.TwoClockResetTimedAction;
import timedWord.DelayTimeWord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

@Data
public class ClassificationTree implements Learner<TwoClockResetLogicTimeWord> {

    private String name;
    private Set<String> sigma;
    private TwoClockTeacher<TwoClockResetLogicTimeWord, TwoClockResetLogicTimeWord, TwoClockTA, TwoClockLogicTimeWord> teacher;
    Clock x=new Clock("Hx");
    Clock y=new Clock("Hy");
   private int errorid;
   int selectfromregionc=0;
   int member=0;
   int memberctx=0;
   int membertable=0;
    private InnerNode root;
    private boolean isComplete = false;
    private Set<Track> trackSet = new HashSet<>();
    private  int reset=0;
    private TwoClockTA hypothesis;
    private Map<PairRegion, BooleanAnswer> answers;
    private Map<TaLocation, LeafNode> locationNodeMap;
    private Map<LeafNode, TaLocation> nodeLocationMap;
    BooleanAnswer qanswer=new BooleanAnswer(null,false);
    List<RegionTwoClockLogicTimedWord> regionerror=new ArrayList<>();
    TwoClockResetLogicTimeWord error1=new TwoClockResetLogicTimeWord(null);
    TwoClockResetLogicTimeWord error2=new TwoClockResetLogicTimeWord(null);
    TwoClockResetLogicTimeWord errorsuff=new TwoClockResetLogicTimeWord(null);
    List<Clock> clockList=new ArrayList<>();
    Map<TwoClockResetLogicTimeWord,BooleanAnswer> analyzectx=new HashMap<>();
    private int kc;
    public ClassificationTree(String name, Set<String> sigma, TwoClockTeacher<TwoClockResetLogicTimeWord, TwoClockResetLogicTimeWord, TwoClockTA, TwoClockLogicTimeWord> teacher,List<Clock> clockList,int kc) {
        this.name = name;
        this.sigma = sigma;
        this.teacher = teacher;
        this.clockList=clockList;
        this.kc=kc;
    }

    @Override
    public void init() {
        answers = new HashMap<>();
        root = new InnerNode(RegionTwoClockLogicTimedWord.emptyWord().emptyWord());
        LeafNode emptyLeaf = new LeafNode(TwoClockResetLogicTimeWord.emptyWord());
        BooleanAnswer key = answerforanalyzesuffixerrorindex(emptyLeaf.getWord(), root.getWord());
//        emptyLeaf.setAccpted(key.isAccept());
//        emptyLeaf.setInit(true);
        emptyLeaf.setPreNode(root);
        root.add(key, emptyLeaf);
        refineSymbolTrack(emptyLeaf);
    }

    Map<PairRegion,TwoClockResetLogicTimeWord> pairRegionTwoClockResetLogicTimeWordMap=new HashMap<>();
//    public TwoClockResetLogicTimeWord selectfromRegion(RegionTwoClockLogicTimedWord suffix,TwoClockResetLogicTimeWord prefix){
//        System.out.println("prefix:"+prefix);
//        System.out.println("suff:"+suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList());
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
//        for (int i = 0; i < suffix.getRegiontimedActions().size(); i++) {
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
//                    if(bigDecimal.subtract(bigDecimal2).doubleValue()<=0&&suffix.getRegiontimedActions().get(i).getRegion().getDifferList()[index - 1 + j - i1]==1||
//                            bigDecimal.subtract(bigDecimal2).doubleValue()>=0&&suffix.getRegiontimedActions().get(i).getRegion().getDifferList()[index - 1 + j - i1]==-1||
//                            bigDecimal.subtract(bigDecimal2).doubleValue()!=0&&suffix.getRegiontimedActions().get(i).getRegion().getDifferList()[index - 1 + j - i1]==0){
//                        return null;
//                    }
//                }
//            }
//
////                double []array=new double[clockList.size()];
//            BigDecimal bigDecimal=new BigDecimal(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(0).getLowerBound());
//            BigDecimal bigDecimal2=new BigDecimal(difference[0]);
//            double mind=bigDecimal.subtract(bigDecimal2).doubleValue();
//            for (int i1=0;i1<clockList.size();i1++){
//                BigDecimal bigDecimal3=new BigDecimal(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(i1).getLowerBound());
//                BigDecimal bigDecimal4=new BigDecimal(difference[i1]);
//                double diff=bigDecimal3.subtract(bigDecimal4).doubleValue();
//                if(diff<0){
//                    return twoClockResetLogicTimeWord;
//                }
//                else {
//                    BigDecimal bigDecimal5=new BigDecimal(mind);
//                    BigDecimal bigDecimal6=new BigDecimal(diff);
//                    if(bigDecimal6.subtract(bigDecimal5).doubleValue()>0){
//                        mind=diff;
//                    }
//                }
//            }
//            double maxd=bigDecimal.subtract(bigDecimal2).doubleValue();
//            for (int i1=0;i1<clockList.size();i1++){
//                BigDecimal bigDecimal3=new BigDecimal(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(i1).getLowerBound());
//                BigDecimal bigDecimal4=new BigDecimal(difference[i1]);
//                double diff=bigDecimal3.subtract(bigDecimal4).doubleValue();
//                if(diff<0){
//                    return twoClockResetLogicTimeWord;
//                }
//                else {
//                    BigDecimal bigDecimal5=new BigDecimal(maxd);
//                    BigDecimal bigDecimal6=new BigDecimal(diff);
//                    if(bigDecimal5.subtract(bigDecimal6).doubleValue()>0){
//                        maxd=diff;
//                    }
//                }
//            }
//
//            BigDecimal bigDecimal7=new BigDecimal(mind);
//            BigDecimal bigDecimal8=new BigDecimal(maxd);
//            if(bigDecimal7.subtract(bigDecimal8).doubleValue()==0){
//                int d=0;
//                Map<Clock,Double> map=new HashMap<>();
////                System.out.println("a:"+a);
////                System.out.println("c:"+c);
//                for (int i1=0;i1<clockList.size();i1++){
//                    BigDecimal bigDecimal9=new BigDecimal(d);
//                    BigDecimal bigDecimal10=new BigDecimal(difference[i1]);
//                    map.put(clockList.get(i1),bigDecimal9.add(bigDecimal10).doubleValue());
//                }
//                TwoClockLogicAction twoClockLogicAction1=new TwoClockLogicAction(suffix.getRegiontimedActions().get(i).getSymbol(),map);
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
//                TwoClockLogicAction twoClockLogicAction1=new TwoClockLogicAction(suffix.getRegiontimedActions().get(i).getSymbol(),map);
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
//        System.out.println("twoClockResetLogicTimeWord:"+twoClockResetLogicTimeWord);
//        return twoClockResetLogicTimeWord;
//    }
public TwoClockResetLogicTimeWord selectfromRegion(RegionTwoClockLogicTimedWord suffix,TwoClockResetLogicTimeWord prefix) {
//    selectfromregionc++;
//    System.out.println("selectfromregionc:"+selectfromregionc);
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
    for (int i = 0; i < suffix.getRegiontimedActions().size(); i++) {
        if(twoClockResetLogicTimeWord!=null&&twoClockResetLogicTimeWord.getTimedActions()!=null&&twoClockResetLogicTimeWord.getTimedActions().size()!=0){
            twoClockResetLogicTimeWord.setTimedActions(null);}
        double[] l = new double[clockList.size()];
        for (int m = 0; m < clockList.size(); m++) {
            if (suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(m).isLowerBoundOpen()) {
                BigDecimal bigDecimal=new BigDecimal(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(m).getLowerBound());
                BigDecimal bigDecimal1=new BigDecimal(0.05);
                l[m] = (bigDecimal.add(bigDecimal1).doubleValue());
            } else {
                l[m] = suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(m).getLowerBound();
            }
        }
        double[] current2=new double[clockList.size()];
        //     double[] current=iterate(l,0, suffix.get(i),difference,current2);
        BigDecimal b1 = new BigDecimal(Double.toString(difference[0]));
        BigDecimal b2 = new BigDecimal(Double.toString(difference[1]));
        double[] current=z3(b1.subtract(b2).doubleValue(),suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(0).getLowerBound()*1.0,difference[0],suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(0).getUpperBound()*1.0,suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(1).getLowerBound()*1.0,difference[1],suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(1).getUpperBound()*1.0,suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(0).isLowerBoundOpen(),suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(0).isUpperBoundOpen(),suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(1).isLowerBoundOpen(),suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(1).isUpperBoundOpen());
        if(current==null){
            return null;
        }
        Map<Clock, Double> map = new HashMap<>();
        for (int k=0;k<clockList.size();k++) {
            map.put(clockList.get(k),current[k]);
        }
        TwoClockLogicAction twoClockLogicAction1 = new TwoClockLogicAction(suffix.getRegiontimedActions().get(i).getSymbol(), map);
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

//    private BooleanAnswer answer(TwoClockResetLogicTimeWord prefix, RegionTwoClockLogicTimedWord suffix) {
//        BooleanAnswer answer=new BooleanAnswer(null,false);
//        if (suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
//            List<TwoClockLogicAction> timedActions = new ArrayList<>();
//            for (int i = 0; i < suffix.getRegiontimedActions().size(); i++) {
//                Map<Clock, Double> map = new HashMap<>();
//                map.put(x, 0.0);
//                map.put(y, 0.0);
//                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(suffix.getRegiontimedActions().get(0).getSymbol(), map);
//                timedActions.add(twoClockLogicAction);
//            }
//            TwoClockLogicTimeWord logicTimeWord = new TwoClockLogicTimeWord(timedActions);
//            logicTimeWord.setTimedActions(timedActions);
//            TwoClockResetLogicTimeWord twoClockResetLogicTimeWordList = new TwoClockResetLogicTimeWord(null);
//            System.out.println("suffix:"+suffix);
//            System.out.println(suffix.getRegiontimedActions().size());
//            System.out.println("prefix:"+prefix);
//            twoClockResetLogicTimeWordList = selectfromRegion2(suffix, prefix);
//            if(twoClockResetLogicTimeWordList==null||twoClockResetLogicTimeWordList.getTimedActions()==null||twoClockResetLogicTimeWordList.getTimedActions().size()==0) {
//                boolean eq = false;
//                for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
//                    if (pairRegion1.prefix.equals(prefix) && pairRegion1.suffix.equals(suffix)) {
//                        eq = true;
//                        twoClockResetLogicTimeWordList = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
//                    }
//                }
//                if (!eq) {
//                    if (twoClockResetLogicTimeWordList == null || twoClockResetLogicTimeWordList.getTimedActions() == null || twoClockResetLogicTimeWordList.getTimedActions().size() == 0) {
//
//                        boolean allreset = true;
//                        for (Clock clock : clockList) {
//                            if (prefix.getLastResetAction().isReset(clock)) {
//                            } else {
//                                allreset = false;
//                            }
//                        }
//                        boolean needselect = true;
//                        if (allreset) {
//                            for (int i1 = 1; i1 < clockList.size(); i1++) {
//                                if (suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
//                                } else {
//                                    needselect = false;
//                                }
//                            }
//                        }
//                        if (needselect) {
//                            twoClockResetLogicTimeWordList = selectfromRegion(suffix, prefix);
//                        }
//                    }
//                }
//            }
//            if (twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()!= 0) {
//                answer = teacher.membership(twoClockResetLogicTimeWordList,this.clockList.size());
//                List<Resets> resetsList2=new ArrayList<>();
//                if(answer!=null){
//                    for(int i=prefix.size();i<twoClockResetLogicTimeWordList.size();i++){
//                        resetsList2.add(answer.getResets().get(i));
//                    }
//                    answer.setResets(resetsList2);
//                    PairRegion pairRegion = new PairRegion(prefix,suffix);
//                    pairRegion.prefix = prefix;
//                    pairRegion.suffix = suffix;
//                    answers.remove(pairRegion);;
//                    answers.put(pairRegion, answer);}
//                else {
//                    PairRegion pairRegion = new PairRegion(prefix,suffix);
//                    pairRegion.prefix = prefix;
//                    pairRegion.suffix = suffix;
//                    answers.remove(pairRegion);
//                    List<Resets> resetsList=new ArrayList<>();
//                    for(int i=0;i<suffix.size();i++){
////                            boolean[] actualreset={true,true};
//                        boolean[] actualreset=new boolean[2];
//                        for (int m=0;m<2;m++){
//                            actualreset[m]=true;
//                        }
//                        Resets resets=new Resets(actualreset);
//                        resetsList.add(resets);
//                    }
//                    answer = new BooleanAnswer(resetsList,false);
//                    answers.put(pairRegion, answer);
//                }
//            } else {
//                // System.out.println("answer:false");
//                PairRegion pairRegion = new PairRegion(prefix,suffix);
//                pairRegion.prefix = prefix;
//                pairRegion.suffix = suffix;
////                        if(prefix.size()==3&&prefix.getTimedActions().get(2).getValue(x)*100==105&&suffix.getTimedActions().get(0).getRegion().lowery*10==40){
////                            System.out.println("filltable开始输出");
////                            System.out.println(prefix.getTimedActions());
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().lowerx);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().upperx);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().lowerboundopenx);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().upperboundopenx);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().lowery);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().uppery);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().lowerboundopeny);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().upperboundopeny);
////                            System.out.println(suffix.getTimedActions().get(0).getRegion().differxy);
////                            System.out.println("false");
////                            System.out.println("filltable结束输出");
////                        }
//                answers.remove(pairRegion);
//                List<Resets> resetsList=new ArrayList<>();
//                int lastvalid=-1;
//                System.out.println("suffix.getRegiontimedActions().size():"+suffix.getRegiontimedActions().size());
//                for (int i=1;i<suffix.getRegiontimedActions().size()+1;i++) {
//                    System.out.println("i:"+i);
////                    System.out.println(suffix.subWord(0,i-1));
//                    System.out.println(suffix.subWord(0,i));
//                    twoClockResetLogicTimeWordList = selectfromRegion2(suffix.subWord(0,i), prefix);
//                    if (twoClockResetLogicTimeWordList == null || twoClockResetLogicTimeWordList.getTimedActions() == null || twoClockResetLogicTimeWordList.getTimedActions().size() == 0) {
//                        boolean eq = false;
//                        for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
//                            if (pairRegion1.prefix.equals(prefix) && pairRegion1.suffix.equals(suffix)) {
//                                eq = true;
//                                twoClockResetLogicTimeWordList = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
//                            }
//                        }
//                        if (!eq) {
//                            if (twoClockResetLogicTimeWordList == null || twoClockResetLogicTimeWordList.getTimedActions() == null || twoClockResetLogicTimeWordList.getTimedActions().size() == 0) {
//
//                                boolean allreset = true;
//                                for (Clock clock : clockList) {
//                                    if (prefix.getLastResetAction().isReset(clock)) {
//                                    } else {
//                                        allreset = false;
//                                    }
//                                }
//                                boolean needselect = true;
//                                if (allreset) {
//                                    for (int i1 = 1; i1 < clockList.size(); i1++) {
//                                        if (suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
//                                        } else {
//                                            needselect = false;
//                                        }
//                                    }
//                                }
//                                if (needselect) {
//                                    twoClockResetLogicTimeWordList = selectfromRegion(suffix.subWord(0,i), prefix);
//                                }
//                            }
//                        }
//                    }
//                    if (twoClockResetLogicTimeWordList != null && twoClockResetLogicTimeWordList.getTimedActions() != null && twoClockResetLogicTimeWordList.getTimedActions().size() != 0) {
//                        lastvalid=i-1;
//                        answer = teacher.membership(twoClockResetLogicTimeWordList, this.clockList.size());
//                        resetsList = new ArrayList<>();
//                        if (answer != null) {
//                            for (int i1 = prefix.size(); i1 < twoClockResetLogicTimeWordList.size(); i1++) {
//                                resetsList.add(answer.getResets().get(i1));
//                            }
//                        }
//
//                    }
//
//                }
//
//                for(int i=lastvalid+1;i<suffix.getRegiontimedActions().size();i++){
//                    boolean[] actualreset={true,true};
//                    Resets resets=new Resets(actualreset);
//                    resetsList.add(resets);
//                }
//                answer = new BooleanAnswer(resetsList,false);
//                //  System.out.println("remove之后的answers:"+answers);
//                answers.put(pairRegion, answer);
////                        System.out.println(pairRegion.prefix);
////                        System.out.println(pairRegion.suffix);
////                        System.out.println(answer);
//                //  System.out.println("answers:"+answers.get(0).);
//                // System.out.println("answers:"+answers);
//            }
//            System.out.println(answer.getResets());
//        }
//        else if(suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix.getTimedActions()==null||
//                suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix.getTimedActions().size()==0){
//            prefix.setTimedActions(null);
//            PairRegion pairRegion = new PairRegion(prefix, suffix);
//            pairRegion.prefix = prefix;
//            pairRegion.suffix = suffix;
//            answers.remove(pairRegion);
//            TwoClockLogicTimeWord twoClockLogicTimeWord = new TwoClockLogicTimeWord(null);
//            List<TwoClockLogicAction> timedActions = new ArrayList<>();
//            for (int i = 0; i < suffix.getRegiontimedActions().size(); i++) {
//                Map<Clock, Double> map = new HashMap<>();
//
//                for (int j = 0; j < clockList.size(); j++) {
//                    if (!suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(j).isLowerBoundOpen()) {
//                        map.put(clockList.get(j), Double.valueOf(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(j).getLowerBound()));
//                    } else {
//                        BigDecimal b1 = new BigDecimal(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(j).getLowerBound());
//                        BigDecimal b2 = new BigDecimal(0.1);
//                        map.put(clockList.get(j), b1.add(b2).doubleValue());
//                    }
//                }
//                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(suffix.getRegiontimedActions().get(i).getSymbol(), map);
//                timedActions.add(twoClockLogicAction);
//            }
//            twoClockLogicTimeWord.setTimedActions(timedActions);
//            TwoClockResetLogicTimeWord timeWord = selectfromRegion2(suffix, prefix);
//            if (timeWord == null || timeWord.getTimedActions() == null || timeWord.getTimedActions().size() == 0) {
//                // System.out.println("answer:false");
//                List<Resets> resetsList=new ArrayList<>();
//                int lastvalid=-1;
//                System.out.println("suffix.getRegiontimedActions().size():"+suffix.getRegiontimedActions().size());
//                for (int i=1;i<suffix.getRegiontimedActions().size()+1;i++) {
//                    System.out.println("i:"+i);
////                    System.out.println(suffix.subWord(0,i-1));
//                    System.out.println(suffix.subWord(0,i));
//                 TwoClockResetLogicTimeWord   twoClockResetLogicTimeWordList = selectfromRegion2(suffix.subWord(0,i), prefix);
//                    if (twoClockResetLogicTimeWordList != null && twoClockResetLogicTimeWordList.getTimedActions() != null && twoClockResetLogicTimeWordList.getTimedActions().size() != 0) {
//                        lastvalid=i-1;
//                        answer = teacher.membership(twoClockResetLogicTimeWordList, this.clockList.size());
//                        resetsList = new ArrayList<>();
//                        if (answer != null) {
//                            for (int i1 = 0; i1 < twoClockResetLogicTimeWordList.size(); i1++) {
//                                resetsList.add(answer.getResets().get(i1));
//                            }
//                        }
//
//                    }
//
//                }
//
//                for(int i=lastvalid+1;i<suffix.getRegiontimedActions().size();i++){
//                    boolean[] actualreset={true,true};
//                    Resets resets=new Resets(actualreset);
//                    resetsList.add(resets);
//                }
//                answer = new BooleanAnswer(resetsList,false);
//                //  System.out.println("remove之后的answers:"+answers);
//                answers.put(pairRegion, answer);
////                        System.out.println(pairRegion.prefix);
////                        System.out.println(pairRegion.suffix);
////                        System.out.println(answer);
//                //  System.out.println("answers:"+answers.get(0).);
//                // System.out.println("answers:"+answers);
//            } else {
//                answer = teacher.membership(timeWord,clockList.size());
//                answers.put(pairRegion, answer);
//            }
//        }
//        else if(suffix.getRegiontimedActions()==null&& prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
//
//            PairRegion pairRegion = new PairRegion(prefix,null);
//            pairRegion.prefix = prefix;
//            pairRegion.suffix = null;
//            answers.remove(pairRegion);
//            answer = teacher.membership(prefix,this.clockList.size());
//            if(answer!=null){
//                answer.setResets(null);
//                //  System.out.println("remove之后的answers:"+answers);
//                answers.put(pairRegion, answer);}
//            else {
//                answer = new BooleanAnswer(null,false);
//                answers.put(pairRegion,answer);
//            }
////                    System.out.println(pairRegion.prefix);
////                    System.out.println(pairRegion.suffix);
////                    System.out.println(answer);
////                    System.out.println("answers:"+answers);
//            //System.out.println("answers:"+answers.containsKey(pairRegion.prefix));
//        }
//        else {
//            prefix.setTimedActions(null);
//            //     System.out.println("情况4");
//            PairRegion pairRegion = new PairRegion(null,null);
//            pairRegion.prefix = null;
//            pairRegion.suffix = null;
//            answers.remove(pairRegion);
//            TwoClockResetLogicTimeWord resetLogicTimeWord = TwoClockResetLogicTimeWord.emptyWord();
//            boolean isaccept=teacher.membership(resetLogicTimeWord, clockList.size());
//            answer.setAccept(isaccept);
//            answer.setResets(null);
//            answers.put(pairRegion, answer);
//        }
//        return answer;
//    }

    private BooleanAnswer answerforanalyzesuffixerrorindex(TwoClockResetLogicTimeWord prefix, RegionTwoClockLogicTimedWord suffix) {
        BooleanAnswer answer;
        for (PairRegion pairRegion:answers.keySet()){
            if(pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix==null||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()==null&&pairRegion.prefix==null&&prefix==null||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()==0&&pairRegion.prefix==null&&prefix==null||
                    pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()==0&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                    pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()==null&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()==0&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0){
                return answers.get(pairRegion);
            }
//            if(pairRegion.suffix==null&&suffix==null&&pairRegion.prefix==null&&prefix==null){
//                return answers.get(pairRegion);
//            }
            else if(pairRegion.suffix==null&&suffix==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix)||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()==null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix)||
                    pairRegion.suffix==null&&suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()==0&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix)){
                return answers.get(pairRegion);
            }
            else if(pairRegion.suffix!=null&& pairRegion.suffix.equals(suffix)&&pairRegion.prefix==null&&prefix==null||
                    pairRegion.suffix!=null&& pairRegion.suffix.equals(suffix)&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()==null||
                    pairRegion.suffix!=null&& pairRegion.suffix.equals(suffix)&&pairRegion.prefix==null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()==0){
                System.out.println("find!!");
                System.out.println(pairRegion.suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList());
                return answers.get(pairRegion);
            }
            else if(pairRegion.suffix!=null&&pairRegion.prefix!=null&&pairRegion.prefix.equals(prefix) && pairRegion.suffix.equals(suffix)){
                return answers.get(pairRegion);
            }
        }
        if (suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix!=null&&prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {
            List<TwoClockLogicAction> timedActions = new ArrayList<>();
            for (int i = 0; i < suffix.getRegiontimedActions().size(); i++) {
                Map<Clock, Double> map = new HashMap<>();
              for (Clock clock:clockList){
                  map.put(clock,0.0);
              }
                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(suffix.getRegiontimedActions().get(0).getSymbol(), map);
                timedActions.add(twoClockLogicAction);
            }
            TwoClockLogicTimeWord logicTimeWord = new TwoClockLogicTimeWord(timedActions);
            logicTimeWord.setTimedActions(timedActions);
            TwoClockResetLogicTimeWord twoClockResetLogicTimeWordList = new TwoClockResetLogicTimeWord(null);
//            System.out.println("find before:"+selectfromregionc);
//            System.out.println("reset:"+reset);
            if(LogictoDelay(prefix)==null){
                System.out.println("invalid");
            }else {
            twoClockResetLogicTimeWordList = selectfromRegion2(suffix, prefix);}
//            System.out.println("prefix:"+prefix);
//            System.out.println("twoClockResetLogicTimeWordList:"+twoClockResetLogicTimeWordList);
            if(LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList==null||LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList.getTimedActions()==null||LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()==0||LogictoDelay(prefix)!=null&&twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()<suffix.getRegiontimedActions().size()+prefix.getTimedActions().size()) {
                boolean eq = false;
                for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
                    if (pairRegion1.prefix.equals(prefix) && pairRegion1.suffix.equals(suffix)) {
                        eq = true;
                        twoClockResetLogicTimeWordList = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
                    }
                }
                if (!eq) {
                    if (twoClockResetLogicTimeWordList == null || twoClockResetLogicTimeWordList.getTimedActions() == null || twoClockResetLogicTimeWordList.getTimedActions().size() == 0||twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()<suffix.getRegiontimedActions().size()+prefix.getTimedActions().size()) {

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
                                if (suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == suffix.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
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
      //      System.out.println("find after:"+selectfromregionc);
     //       System.out.println("reset:"+reset);
            if (twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()!= 0&&twoClockResetLogicTimeWordList.getTimedActions().size()==suffix.getRegiontimedActions().size()+prefix.getTimedActions().size()) {
       //        System.out.println("membership prefix:"+prefix);
        //        System.out.println("membership suffix:"+suffix);
         //       System.out.println("start membership");
                boolean isaccept = teacher.membership(twoClockResetLogicTimeWordList,this.clockList.size());
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
                answer=new BooleanAnswer(resetsList,isaccept);
                answer.setAccept(isaccept);
                answer.setResets(resetsList);
                PairRegion pairRegion = new PairRegion(prefix, suffix);
                pairRegion.prefix = prefix;
                pairRegion.suffix = suffix;
                answers.remove(pairRegion);
                answers.put(pairRegion, answer);
                return answer;
            } else {
                PairRegion pairRegion = new PairRegion(prefix,suffix);
                pairRegion.prefix = prefix;
                pairRegion.suffix = suffix;
                List<Resets> resetsList=new ArrayList<>();
                int k=0;
                if(twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()!=0){
                    k=twoClockResetLogicTimeWordList.getTimedActions().size();
                    for(int i=prefix.getTimedActions().size();i<k;i++){
                        boolean[] actualreset = new boolean[clockList.size()];
                        for (int m = 0; m < clockList.size(); m++) {
                            actualreset[m] = twoClockResetLogicTimeWordList.getTimedActions().get(i).isReset(clockList.get(m));
                        }
                        Resets resets=new Resets(actualreset);
                        resetsList.add(resets);
                    }

                    for(int i=k-prefix.getTimedActions().size();i<suffix.getRegiontimedActions().size();i++){
                        boolean[] actualreset = new boolean[clockList.size()];
                        for (int m = 0; m < clockList.size(); m++) {
                            actualreset[m] = true;
                        }
                        Resets resets=new Resets(actualreset);
                        resetsList.add(resets);
                    }
                }
                else {
                    for(int i=0;i<suffix.getRegiontimedActions().size();i++){
                        boolean[] actualreset = new boolean[clockList.size()];
                        for (int m = 0; m < clockList.size(); m++) {
                            actualreset[m] = true;
                        }
                        Resets resets=new Resets(actualreset);
                        resetsList.add(resets);
                    }
                }

//                System.out.println(suffix.getRegiontimedActions().size());
//                System.out.println("resetsList"+resetsList);
                answer = new BooleanAnswer(resetsList,false);
                answers.remove(pairRegion);
                answers.put(pairRegion, answer);
         //       System.out.println("not find:"+answers.get(pairRegion));
                return answer;
            }
        }
        else if(suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix==null||
                suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix.getTimedActions()==null||
                suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()!=0&&prefix.getTimedActions().size()==0){
            prefix.setTimedActions(null);
            PairRegion pairRegion = new PairRegion(prefix, suffix);
            pairRegion.prefix = prefix;
            pairRegion.suffix = suffix;
//            System.out.println("find before:"+selectfromregionc);
//            System.out.println("reset:"+reset);
            TwoClockResetLogicTimeWord twoClockResetLogicTimeWordList = selectfromRegion2(suffix, prefix);
//            System.out.println("find after:"+selectfromregionc);
//            System.out.println("reset:"+reset);
//            System.out.println("prefix:"+prefix);
//            System.out.println("twoClockResetLogicTimeWordList:"+twoClockResetLogicTimeWordList);
            if (twoClockResetLogicTimeWordList == null || twoClockResetLogicTimeWordList.getTimedActions() == null || twoClockResetLogicTimeWordList.getTimedActions().size() == 0||twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()<suffix.getRegiontimedActions().size()) {
                List<Resets> resetsList=new ArrayList<>();
                int k=0;
                if(twoClockResetLogicTimeWordList!=null&&twoClockResetLogicTimeWordList.getTimedActions()!=null&&twoClockResetLogicTimeWordList.getTimedActions().size()!=0){
                    k=twoClockResetLogicTimeWordList.getTimedActions().size();
                }
                for(int i=0;i<k;i++){
                    boolean[] actualreset = new boolean[clockList.size()];
                    for (int m = 0; m < clockList.size(); m++) {
                        actualreset[m] = twoClockResetLogicTimeWordList.getTimedActions().get(i).isReset(clockList.get(m));
                    }
                    Resets resets=new Resets(actualreset);
                    resetsList.add(resets);
                }
                for (int i = k; i < suffix.getRegiontimedActions().size(); i++) {
                    boolean[] actualreset = new boolean[clockList.size()];
                    for (int m = 0; m < clockList.size(); m++) {
                        actualreset[m] = true;
                    }
                    Resets resets=new Resets(actualreset);
                    resetsList.add(resets);
                }
        //        System.out.println(suffix.getRegiontimedActions().size());
        //        System.out.println("resetsList"+resetsList);
                answer = new BooleanAnswer(resetsList,false);
                answers.remove(pairRegion);
                answers.put(pairRegion, answer);
                return  answer;
            } else {
         //       System.out.println("membership prefix:"+prefix);
         //       System.out.println("membership suffix:"+suffix);
         //       System.out.println("start membership");
                boolean isaccept = teacher.membership(twoClockResetLogicTimeWordList,this.clockList.size());
                member++;
                List<Resets> resetsList = new ArrayList<>();
                for (int i = 0; i < twoClockResetLogicTimeWordList.size(); i++) {
                    boolean[] actualreset = new boolean[clockList.size()];
                    for (int m = 0; m < clockList.size(); m++) {
                        if(twoClockResetLogicTimeWordList.getTimedActions().get(i).getResetClockSet().contains(clockList.get(m))){
                            actualreset[m] = true;
                        }
                    }
                    Resets resets = new Resets(actualreset);
                    resetsList.add(resets);
                }
                answer=new BooleanAnswer(resetsList,isaccept);
                answer.setAccept(isaccept);
                answer.setResets(resetsList);
                answers.remove(pairRegion);
                answers.put(pairRegion, answer);
                return answer;
            }
        }
        else if(suffix==null&& prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0
                ||suffix!=null&&suffix.getRegiontimedActions()==null&& prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0
                ||suffix!=null&&suffix.getRegiontimedActions()!=null&&suffix.getRegiontimedActions().size()==0&& prefix!=null&&prefix.getTimedActions()!=null&&prefix.getTimedActions().size()!=0) {

            PairRegion pairRegion = new PairRegion(prefix,null);
            pairRegion.prefix = prefix;
            pairRegion.suffix = null;
            answers.remove(pairRegion);
//            System.out.println("membership prefix:"+prefix);
//            System.out.println("membership suffix:"+suffix);
//            System.out.println("start membership");
            boolean isaccept = teacher.membership(prefix,this.clockList.size());
            member++;
            answer=new BooleanAnswer(null,isaccept);
            answer.setAccept(isaccept);
            answer.setResets(null);
            answers.put(pairRegion, answer);
            return answer;
        }
        else {
            prefix.setTimedActions(null);
            PairRegion pairRegion = new PairRegion(null,null);
            pairRegion.prefix = null;
            pairRegion.suffix = null;
            answers.remove(pairRegion);
            TwoClockResetLogicTimeWord resetLogicTimeWord = TwoClockResetLogicTimeWord.emptyWord();
//            System.out.println("membership prefix:"+prefix);
//            System.out.println("membership suffix:"+suffix);
//            System.out.println("start membership");
            boolean isaccept = teacher.membership(resetLogicTimeWord,this.clockList.size());
            member++;
            answer=new BooleanAnswer(null,isaccept);
            answer.setAccept(isaccept);
            answer.setResets(null);
            answers.put(pairRegion, answer);
            return answer;
        }
    }

    public TwoClockResetLogicTimeWord selectfromRegion2(RegionTwoClockLogicTimedWord suffix,TwoClockResetLogicTimeWord prefix) {
        selectfromregionc++;
//        System.out.println("selectfromregionc:"+selectfromregionc);
//        System.out.println("prefix:"+prefix);
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
        for (int i = 0; i < suffix.getRegiontimedActions().size(); i++) {
//            System.out.println("i"+i);
//            System.out.println(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList());
//            if(twoClockResetLogicTimeWord!=null&&twoClockResetLogicTimeWord.getTimedActions()!=null&&twoClockResetLogicTimeWord.getTimedActions().size()!=0){
//                twoClockResetLogicTimeWord.setTimedActions(null);}
            double[] l = new double[clockList.size()];
            for (int m = 0; m < clockList.size(); m++) {
                if (suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(m).isLowerBoundOpen()) {
                    BigDecimal bigDecimal=new BigDecimal(Double.toString(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(m).getLowerBound()));
                    BigDecimal bigDecimal1=new BigDecimal(Double.toString(0.05));
                    BigDecimal add=bigDecimal.add(bigDecimal1);
                    BigDecimal diff=new BigDecimal(Double.toString(difference[m]));
//                    if(add.subtract(diff).doubleValue()<0){
//                        l[m] = diff.doubleValue();
//                    }
//                    else {
                        l[m] = (bigDecimal.add(bigDecimal1).doubleValue());
        //            }
                } else {
                    l[m] = suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList().get(m).getLowerBound();
                }
            }
            double[] current2=new double[clockList.size()];
//            System.out.println("difference:"+difference);
//            System.out.println(suffix.getRegiontimedActions().get(i).getRegion().getTimeGuardList());
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
            double[] current=iterate(l,0, suffix.getRegiontimedActions().get(i),difference,current2);
//            BigDecimal b1 = new BigDecimal(Double.toString(difference[0]));
//            BigDecimal b2 = new BigDecimal(Double.toString(difference[1]));
//            double[] current=z3(b1.subtract(b2).doubleValue(),suffix.get(i).getRegion().getTimeGuardList().get(0).getLowerBound()*1.0,difference[0],suffix.get(i).getRegion().getTimeGuardList().get(0).getUpperBound()*1.0,suffix.get(i).getRegion().getTimeGuardList().get(1).getLowerBound()*1.0,difference[1],suffix.get(i).getRegion().getTimeGuardList().get(1).getUpperBound()*1.0,suffix.get(i).getRegion().getTimeGuardList().get(0).isLowerBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(0).isUpperBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(1).isLowerBoundOpen(),suffix.get(i).getRegion().getTimeGuardList().get(1).isUpperBoundOpen());
            if(current==null){
           //     return null;
                return twoClockResetLogicTimeWord;
            }
            Map<Clock, Double> map = new HashMap<>();
            for (int k=0;k<clockList.size();k++) {
                map.put(clockList.get(k),current[k]);
            }
            TwoClockLogicAction twoClockLogicAction1 = new TwoClockLogicAction(suffix.getRegiontimedActions().get(i).getSymbol(), map);
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
            System.out.println("logicTimeWord2:"+logicTimeWord2);
            TwoClockResetLogicTimeWord twoClockResetLogicTimeWord1=teacher.transferWord(logicTimeWord2);
            reset++;
            if(twoClockResetLogicTimeWord!=null&&twoClockResetLogicTimeWord.getTimedActions()!=null&&twoClockResetLogicTimeWord.getTimedActions().size()!=0){
                twoClockResetLogicTimeWord.setTimedActions(null);}
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


    public void refineSymbolTrack(LeafNode leafNode) {
        TwoClockResetLogicTimeWord prefix = leafNode.getWord();
        for (String symbol : sigma) {
            Map<Clock,Double> map=new HashMap<>();
            for (int m=0;m<clockList.size();m++){
                map.put(clockList.get(m),0d);
            }
            TwoClockLogicAction logicAction = new TwoClockLogicAction(symbol, map);
            TwoClockLogicTimeWord logicTimeWord=new TwoClockLogicTimeWord(null);
            if(prefix.getTimedActions()==null){
                List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
                twoClockLogicActionList.add(logicAction);
                logicTimeWord.setTimedActions(twoClockLogicActionList);
            }
            else {
                logicTimeWord = TwoClockResetLogicTimeWord.logicTimeWord(prefix).concat(logicAction);}
            TwoClockResetLogicTimeWord resetWord = teacher.transferWord(logicTimeWord);
            TwoClockResetLogicAction resetAction = resetWord.getLastResetAction();
         //   System.out.println("start sift");
          //  System.out.println("selectfromregionc:"+selectfromregionc);
            LeafNode target = sift(resetWord).getLeafNode();
         //   System.out.println("end sift");
         //   System.out.println("selectfromregionc:"+selectfromregionc);
            // TwoClockResetLogicAction resetAction = resetWord.getLastResetAction();
            Track track = new Track(leafNode, target, resetAction);
            trackSet.add(track);
        }

    }

    private SiftResult sift(TwoClockResetLogicTimeWord word) {
        Node currentNode = root;
        while (currentNode.isInnerNode()) {
            InnerNode node = (InnerNode) currentNode;
            BooleanAnswer key = answerforanalyzesuffixerrorindex(word, node.getWord());
            Node next = node.getChild(key);
            if (next == null) {
                LeafNode leafNode = new LeafNode(word, false, false);
                node.add(key, leafNode);
                leafNode.setPreNode(node);
                refineSymbolTrack(leafNode);
                return new SiftResult(leafNode, true);
            }
            currentNode = next;
        }
        return new SiftResult((LeafNode) currentNode, false);
    }

    @Override
    public void learn() {

    }

    @Override
    public void refine(TwoClockResetLogicTimeWord counterExample) {

    }

    @Override
    public boolean check(TwoClockResetLogicTimeWord counterExample) {
        return false;
    }


    //    public void refine(TwoClockResetLogicTimeWord ce) {
//        ErrorIndexResult result = errorIndexAnalyse(ce);
//        int errorIndex = result.getIndex();
////        TaLocation uLocation = hypothesis.reach(ce.subWord(0, errorIndex));
//        TaLocation uLocation = hypothesis.reach(TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(0, errorIndex)));
//        TwoClockLogicAction action = ce.get(errorIndex).logicTimedAction();
//
//
//        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2=new RegionTwoClockLogicTimedWord(null);
//        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//        TwoClockLogicTimeWord timeWord=new TwoClockLogicTimeWord(null);
//        List<TwoClockLogicAction> twoClockLogicActionList2=new ArrayList<>();
//        twoClockLogicActionList2.add(action);
//        timeWord.setTimedActions(twoClockLogicActionList2);
//        List<Region> regionList2 = transfertoRegion(timeWord);
//        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(action.getSymbol(), regionList2.get(0));
//        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//
//        regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//        regionTwoClockLogicTimedWord2.setRegiontimedActions(regionTwoClockLogicActionList2);
//
//
//        System.out.println("uLocation:"+uLocation);
//        LeafNode sourceNode = locationNodeMap.get(uLocation);
//        System.out.println("sourceNode:"+sourceNode);
//        TwoClockResetLogicTimeWord uWord = sourceNode.getWord();
//        System.out.println("uWord:"+uWord);
//        TwoClockLogicTimeWord nextWord=new TwoClockLogicTimeWord(null);
//        TwoClockResetLogicTimeWord resetNextWord=new TwoClockResetLogicTimeWord(null);
//        if(uWord.getTimedActions()==null){
//            List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
//            twoClockLogicActionList.add(action);
//            nextWord.setTimedActions(twoClockLogicActionList);
//            resetNextWord = teacher.transferWord(nextWord);
////            List<TwoClockResetLogicAction> twoClockResetLogicActions=new ArrayList<>();
////            twoClockResetLogicActions.add(ce.get(errorIndex));
////            resetNextWord.setTimedActions(twoClockResetLogicActions);
//        }
//        else {
//
////        nextWord =TwoClockResetLogicTimeWord.logicTimeWord(uWord).concat(action);
//            List<TwoClockLogicAction> timedActions2 = new ArrayList<>();
//            for (int i = 0; i < regionTwoClockLogicTimedWord2.getRegiontimedActions().size(); i++) {
//                Map<Clock, Double> map = new HashMap<>();
////                map.put(x, 0.0);
////                map.put(y, 0.0);
//                for (int k=0;k<clockList.size();k++) {
//                    map.put(clockList.get(k),0.0);
//                }
//                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getSymbol(), map);
//                timedActions2.add(twoClockLogicAction);
//            }
//            TwoClockLogicTimeWord logicTimeWord = new TwoClockLogicTimeWord(timedActions2);
//            logicTimeWord.setTimedActions(timedActions2);
//            System.out.println("1:"+uWord);
//            System.out.println("2:"+regionTwoClockLogicTimedWord2);
//            System.out.println("3:"+logicTimeWord);
//            System.out.println("4:"+action);
//            System.out.println("5:"+regionTwoClockLogicTimedWord2.getRegiontimedActions().size());
//            resetNextWord =selectfromRegion2(regionTwoClockLogicTimedWord2,uWord);
//
//            if(resetNextWord==null||resetNextWord.getTimedActions()==null||resetNextWord.getTimedActions().size()==0) {
//                boolean eq = false;
//                for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
//                    if (pairRegion1.prefix.equals(uWord) && pairRegion1.suffix.equals(regionTwoClockLogicTimedWord2)) {
//                        eq = true;
//                        resetNextWord = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
//                    }
//                }
//                if (!eq) {
//                    if (resetNextWord == null || resetNextWord.getTimedActions() == null || resetNextWord.getTimedActions().size() == 0) {
//
//                        boolean allreset = true;
//                        for (Clock clock : clockList) {
//                            if (uWord.getLastResetAction().isReset(clock)) {
//                            } else {
//                                allreset = false;
//                            }
//                        }
//                        boolean needselect = true;
//                        if (allreset) {
//                            for (int i1 = 1; i1 < clockList.size(); i1++) {
//                                if (regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
//                                } else {
//                                    needselect = false;
//                                }
//                            }
//                        }
//                        if (needselect) {
//                            resetNextWord = selectfromRegion(regionTwoClockLogicTimedWord2,uWord);
//                        }
//                    }
//                }
//            }
//
//
//        }
////        TwoClockResetLogicTimeWord resetNextWord = teacher.transferWord(nextWord,x,y);
//        if(resetNextWord==null){
////            if (!regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopenx && !regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopeny && regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerx * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowery * 100
////                    || regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopenx && regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopeny && regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerx * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowery * 100) {
//            List<TwoClockLogicAction> timedActions3 = new ArrayList<>();
//            Map<Clock, Double> map = new HashMap<>();
//
//            for (int j = 0; j < clockList.size(); j++) {
//                if (!regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).isLowerBoundOpen()) {
//                    map.put(clockList.get(j), Double.valueOf(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound()));
//                } else {
//                    BigDecimal b1 = new BigDecimal(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound());
//                    BigDecimal b2 = new BigDecimal(0.1);
//                    map.put(clockList.get(j), b1.add(b2).doubleValue());
//                }
//            }
//            TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(action.getSymbol(), map);
//            timedActions3.add(twoClockLogicAction);
//            //   System.out.println("timedActions:"+timedActions);
//            TwoClockLogicTimeWord twoClockLogicTimeWord3 = new TwoClockLogicTimeWord(timedActions3);
//            twoClockLogicTimeWord3.setTimedActions(timedActions3);
//            nextWord = TwoClockResetLogicTimeWord.logicTimeWord(uWord).concat(action);
//            resetNextWord = teacher.transferWord(nextWord);
//            //  }
//        }
//        System.out.println("resetNextWord:"+resetNextWord);
//        SiftResult siftResult = sift(resetNextWord);
//        LeafNode targetNode = siftResult.getLeafNode();//u'
//        System.out.println("targetNode:"+targetNode);
//        TwoClockResetLogicAction lastResetAction = resetNextWord.getLastResetAction();
//        System.out.println("lastResetAction:"+lastResetAction);
//        if (result.getErrorEnum() == ErrorEnum.ResetError) {//区间划分有误，加入迁移（u,u',ctx[EI]）
//            System.out.println("前缀分析错误");
//            System.out.println("sourceNode:"+sourceNode);
//            System.out.println("lastResetAction:"+lastResetAction);
//            System.out.println("targetNode:"+targetNode);
//            List<TwoClockResetLogicAction> actions2=new ArrayList<>();
//            actions2.add(lastResetAction);
//            TwoClockResetLogicTimeWord twoClockResetLogicTimeWord2=new TwoClockResetLogicTimeWord(actions2);
//            twoClockResetLogicTimeWord2.setTimedActions(actions2);
//            List<Region> regions2=transfertoRegion(TwoClockResetLogicTimeWord.logicTimeWord(twoClockResetLogicTimeWord2));
//            for (Track track:trackSet){
//             if(track.getSource().equals(sourceNode)){
//                 List<TwoClockResetLogicAction> actions=new ArrayList<>();
//                 actions.add(track.getAction());
//                 TwoClockResetLogicTimeWord twoClockResetLogicTimeWord=new TwoClockResetLogicTimeWord(actions);
//                 twoClockResetLogicTimeWord.setTimedActions(actions);
//                 List<Region> regions=transfertoRegion(TwoClockResetLogicTimeWord.logicTimeWord(twoClockResetLogicTimeWord));
//                boolean regionsame=true;
//                 if(regions.get(0).getDifferList().length!=regions2.get(0).getDifferList().length){
//                     regionsame=false;
//                }else {
//                     for (int i = 0; i < regions.get(0).getDifferList().length; i++) {
//                         if (regions.get(0).getDifferList()[i] != regions2.get(0).getDifferList()[i]) {
//                             regionsame = false;
//                             break;
//                         }
//                     }
//                 }
//                    if(regionsame){
//                         for (int m=0;m<clockList.size();m++) {
//                             if (!regions.get(0).getTimeGuardList().get(m).equals(regions2.get(0).getTimeGuardList().get(m))) {
//                                 regionsame=false;
//                                 break;
//                             }
//                         }
//                         }
//                    if(regionsame){
//                        TwoClockLogicTimeWord suffix = TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(errorIndex, ce.size()));
//                        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//                        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                        //   System.out.println("suffix:"+suffix);
//                        for(int m=0;m<suffix.size();m++){
//                            //     System.out.println("suffix.subWord(m,m+1):"+suffix.subWord(m,m+1));
//                            List<Region> regionList = transfertoRegion(suffix.subWord(m,m+1));
//                            RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(suffix.get(m).getSymbol(), regionList.get(0));
//                            regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                        }
//                        regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//                        regionTwoClockLogicTimedWord.setRegiontimedActions(regionTwoClockLogicActionList);
//                        //RegionTwoClockLogicTimedWord suffix = ce.subWord(errorIndex + 1, ce.size()).logicTimeWord();
//                        InnerNode innerNode = new InnerNode(regionTwoClockLogicTimedWord);//后缀为EI往后的部分
//                        System.out.println("InnerNode:"+innerNode);//paper (a,1.5)
//                        System.out.println("targetNode:"+targetNode);
//                        InnerNode father = sourceNode.getPreNode();//paper sift(a,0)的父节点(empty的父节点)
//                        // ResetAnswer resetAnswer = null;
//                        BooleanAnswer resetAnswer = null;
//                        try {
//                            for (BooleanAnswer r : father.getKeyChildMap().keySet()) {
//                                if (father.getChild(r) == sourceNode) {
//                                    resetAnswer = r;
//                                }
//                            }
//                        }catch (Exception e){
//                            System.out.println(e);
//                        }
//                        father.getKeyChildMap().put(resetAnswer, innerNode);
//                        innerNode.setPreNode(father);//(a,1.5)的父节点置为sift(a,0)的父节点(empty的父节点)
//                        targetNode.setPreNode(innerNode);//sift(a,0)（empty）的父节点置为（a,1.5）
//                        boolean init = ce.subWord(0, errorIndex).isEmpty();//(a,0)是否为空,准备为(a,0)构建新节点
//                        boolean accept = teacher.membership(ce.subWord(0, errorIndex),this.clockList.size()).isAccept();//(a,0)是否被接收,准备为(a,0)构建新节点
//                        LeafNode newLeafNode = new LeafNode(ce.subWord(0, errorIndex), init, accept);//加入叶节点𝑢 ⋅ 𝑐𝑡𝑥[𝐸𝐼]
//                        System.out.println("sourceNode:"+sourceNode);
//                        System.out.println("newLeafNode:"+newLeafNode);
//                        newLeafNode.setPreNode(innerNode);
//                        innerNode.add(answer(sourceNode.getWord(), regionTwoClockLogicTimedWord), sourceNode);//(a,1.5)构建子节点empty(原来的sift(a,0))
//                        innerNode.add(answer(newLeafNode.getWord(), regionTwoClockLogicTimedWord), newLeafNode);//(a,1.5)构建子节点(a,0)
//                        //refine transition
//                        if(sourceNode.getword()!=null&&sourceNode.getword().getTimedActions()!=null){
//                        refineNode(sourceNode);
//                        }
//                        //add transition
//                        refineSymbolTrack(newLeafNode);
//                    }
//                    else {
//                        Track trackadd = new Track(sourceNode, targetNode, lastResetAction);
//                        trackSet.add(trackadd);
//                        return;
//                    }
//
//             }
//
//            }
//        }
//        if (result.getErrorEnum() == ErrorEnum.ConsistentError) {//一致性有误，叶节点分裂
//            System.out.println("叶节点分裂");
//
//            if (siftResult.isCompleteOperation()) {
//                Track track = new Track(sourceNode, targetNode, lastResetAction);
//                trackSet.add(track);
//                return;
//            } else {
//                TaLocation vLocation = nodeLocationMap.get(targetNode);//u'
//                boolean isPass = checkIsPass(uLocation, vLocation, lastResetAction);
//                if (!isPass) {
//                    Track track = new Track(sourceNode, targetNode, lastResetAction);
//                    trackSet.add(track);
//                    return;
//                } else {
//                    TwoClockLogicTimeWord suffix = TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(errorIndex + 1, ce.size()));
//                    RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//                    List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                    //   System.out.println("suffix:"+suffix);
//                    for(int m=0;m<suffix.size();m++){
//                        //     System.out.println("suffix.subWord(m,m+1):"+suffix.subWord(m,m+1));
//                        List<Region> regionList = transfertoRegion(suffix.subWord(m,m+1));
//                        RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(suffix.get(m).getSymbol(), regionList.get(0));
//                        regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                    }
//                    regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//                    regionTwoClockLogicTimedWord.setRegiontimedActions(regionTwoClockLogicActionList);
//                    //RegionTwoClockLogicTimedWord suffix = ce.subWord(errorIndex + 1, ce.size()).logicTimeWord();
//                    InnerNode innerNode = new InnerNode(regionTwoClockLogicTimedWord);//后缀为EI+1往后的部分
//                    System.out.println("InnerNode:"+innerNode);//paper (a,1.5)
//                    System.out.println("targetNode:"+targetNode);
//                    InnerNode father = targetNode.getPreNode();//paper sift(a,0)的父节点(empty的父节点)
//                    // ResetAnswer resetAnswer = null;
//                    BooleanAnswer resetAnswer = null;
//                    try {
//                        for (BooleanAnswer r : father.getKeyChildMap().keySet()) {
//                            if (father.getChild(r) == targetNode) {
//                                resetAnswer = r;
//                            }
//                        }
//                    }catch (Exception e){
//                        System.out.println(e);
//                    }
//                    father.getKeyChildMap().put(resetAnswer, innerNode);
//                    innerNode.setPreNode(father);//(a,1.5)的父节点置为sift(a,0)的父节点(empty的父节点)
//                    targetNode.setPreNode(innerNode);//sift(a,0)（empty）的父节点置为（a,1.5）
//                    boolean init = resetNextWord.isEmpty();//(a,0)是否为空,准备为(a,0)构建新节点
//                    boolean accept = teacher.membership(resetNextWord,this.clockList.size()).isAccept();//(a,0)是否被接收,准备为(a,0)构建新节点
//                    LeafNode newLeafNode = new LeafNode(resetNextWord, init, accept);//加入叶节点𝑢 ⋅ 𝑐𝑡𝑥[𝐸𝐼]
//                    System.out.println("newLeafNode:"+newLeafNode);
//                    newLeafNode.setPreNode(innerNode);
//                    innerNode.add(answer(targetNode.getWord(), regionTwoClockLogicTimedWord), targetNode);//(a,1.5)构建子节点empty(原来的sift(a,0))
//                    innerNode.add(answer(newLeafNode.getWord(), regionTwoClockLogicTimedWord), newLeafNode);//(a,1.5)构建子节点(a,0)
//                    //refine transition
//                    refineNode(targetNode);
//                    //add transition
//                    refineSymbolTrack(newLeafNode);
//                }
//            }
//        }
//    }
//    public  void  refinesuff(TwoClockResetLogicTimeWord ce,int errorIndex){
////        TaLocation uLocation = hypothesis.reach(ce.subWord(0, errorIndex));
//        TaLocation uLocation = hypothesis.reach(TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(0, errorIndex)));
//        TwoClockLogicAction action = ce.get(errorIndex).logicTimedAction();
//
//
//        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2=new RegionTwoClockLogicTimedWord(null);
//        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
//        TwoClockLogicTimeWord timeWord=new TwoClockLogicTimeWord(null);
//        List<TwoClockLogicAction> twoClockLogicActionList2=new ArrayList<>();
//        twoClockLogicActionList2.add(action);
//        timeWord.setTimedActions(twoClockLogicActionList2);
//        List<Region> regionList2 = transfertoRegion(timeWord);
//        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(action.getSymbol(), regionList2.get(0));
//        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
//
//        regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
//        regionTwoClockLogicTimedWord2.setRegiontimedActions(regionTwoClockLogicActionList2);
//
//
//        System.out.println("uLocation:"+uLocation);
//        LeafNode sourceNode = locationNodeMap.get(uLocation);
//        System.out.println("sourceNode:"+sourceNode);
//        TwoClockResetLogicTimeWord uWord = sourceNode.getWord();
//        System.out.println("uWord:"+uWord);
//        TwoClockLogicTimeWord nextWord=new TwoClockLogicTimeWord(null);
//        TwoClockResetLogicTimeWord resetNextWord=new TwoClockResetLogicTimeWord(null);
//        if(uWord.getTimedActions()==null){
//            List<TwoClockLogicAction> twoClockLogicActionList=new ArrayList<>();
//            twoClockLogicActionList.add(action);
//            nextWord.setTimedActions(twoClockLogicActionList);
//            resetNextWord = teacher.transferWord(nextWord);
////            List<TwoClockResetLogicAction> twoClockResetLogicActions=new ArrayList<>();
////            twoClockResetLogicActions.add(ce.get(errorIndex));
////            resetNextWord.setTimedActions(twoClockResetLogicActions);
//        }
//        else {
//
////        nextWord =TwoClockResetLogicTimeWord.logicTimeWord(uWord).concat(action);
//            List<TwoClockLogicAction> timedActions2 = new ArrayList<>();
//            for (int i = 0; i < regionTwoClockLogicTimedWord2.getRegiontimedActions().size(); i++) {
//                Map<Clock, Double> map = new HashMap<>();
////                map.put(x, 0.0);
////                map.put(y, 0.0);
//                for (int k=0;k<clockList.size();k++) {
//                    map.put(clockList.get(k),0.0);
//                }
//                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getSymbol(), map);
//                timedActions2.add(twoClockLogicAction);
//            }
//            TwoClockLogicTimeWord logicTimeWord = new TwoClockLogicTimeWord(timedActions2);
//            logicTimeWord.setTimedActions(timedActions2);
//            System.out.println("1:"+uWord);
//            System.out.println("2:"+regionTwoClockLogicTimedWord2);
//            System.out.println("3:"+logicTimeWord);
//            System.out.println("4:"+action);
//            System.out.println("5:"+regionTwoClockLogicTimedWord2.getRegiontimedActions().size());
//            resetNextWord =selectfromRegion2(regionTwoClockLogicTimedWord2,uWord);
//
//            if(resetNextWord==null||resetNextWord.getTimedActions()==null||resetNextWord.getTimedActions().size()==0) {
//                boolean eq = false;
//                for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
//                    if (pairRegion1.prefix.equals(uWord) && pairRegion1.suffix.equals(regionTwoClockLogicTimedWord2)) {
//                        eq = true;
//                        resetNextWord = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
//                    }
//                }
//                if (!eq) {
//                    if (resetNextWord == null || resetNextWord.getTimedActions() == null || resetNextWord.getTimedActions().size() == 0) {
//
//                        boolean allreset = true;
//                        for (Clock clock : clockList) {
//                            if (uWord.getLastResetAction().isReset(clock)) {
//                            } else {
//                                allreset = false;
//                            }
//                        }
//                        boolean needselect = true;
//                        if (allreset) {
//                            for (int i1 = 1; i1 < clockList.size(); i1++) {
//                                if (regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
//                                } else {
//                                    needselect = false;
//                                }
//                            }
//                        }
//                        if (needselect) {
//                            resetNextWord = selectfromRegion(regionTwoClockLogicTimedWord2,uWord);
//                        }
//                    }
//                }
//            }
//
//
//        }
////        TwoClockResetLogicTimeWord resetNextWord = teacher.transferWord(nextWord,x,y);
//        if(resetNextWord==null){
////            if (!regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopenx && !regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopeny && regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerx * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowery * 100
////                    || regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopenx && regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerboundopeny && regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowerx * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().lowery * 100) {
//            List<TwoClockLogicAction> timedActions3 = new ArrayList<>();
//            Map<Clock, Double> map = new HashMap<>();
//
//            for (int j = 0; j < clockList.size(); j++) {
//                if (!regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).isLowerBoundOpen()) {
//                    map.put(clockList.get(j), Double.valueOf(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound()));
//                } else {
//                    BigDecimal b1 = new BigDecimal(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound());
//                    BigDecimal b2 = new BigDecimal(0.1);
//                    map.put(clockList.get(j), b1.add(b2).doubleValue());
//                }
//            }
//            TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(action.getSymbol(), map);
//            timedActions3.add(twoClockLogicAction);
//            //   System.out.println("timedActions:"+timedActions);
//            TwoClockLogicTimeWord twoClockLogicTimeWord3 = new TwoClockLogicTimeWord(timedActions3);
//            twoClockLogicTimeWord3.setTimedActions(timedActions3);
//            nextWord = TwoClockResetLogicTimeWord.logicTimeWord(uWord).concat(action);
//            resetNextWord = teacher.transferWord(nextWord);
//            //  }
//        }
//        System.out.println("resetNextWord:"+resetNextWord);
//        SiftResult siftResult = sift(resetNextWord);
//        LeafNode targetNode = siftResult.getLeafNode();//u'
//        System.out.println("targetNode:"+targetNode);
//        TwoClockResetLogicAction lastResetAction = resetNextWord.getLastResetAction();
//        System.out.println("lastResetAction:"+lastResetAction);
//      //一致性有误，叶节点分裂
//            System.out.println("叶节点分裂");
//
//            if (siftResult.isCompleteOperation()) {
//                Track track = new Track(sourceNode, targetNode, lastResetAction);
//                trackSet.add(track);
//                return;
//            } else {
//                TaLocation vLocation = nodeLocationMap.get(targetNode);//u'
//                boolean isPass = checkIsPass(uLocation, vLocation, lastResetAction);
//                if (!isPass) {
//                    Track track = new Track(sourceNode, targetNode, lastResetAction);
//                    trackSet.add(track);
//                    return;
//                } else {
//                    TwoClockLogicTimeWord suffix = TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(errorIndex + 1, ce.size()));
//                    RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//                    List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                    //   System.out.println("suffix:"+suffix);
//                    for(int m=0;m<suffix.size();m++){
//                        //     System.out.println("suffix.subWord(m,m+1):"+suffix.subWord(m,m+1));
//                        List<Region> regionList = transfertoRegion(suffix.subWord(m,m+1));
//                        RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(suffix.get(m).getSymbol(), regionList.get(0));
//                        regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                    }
//                    regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//                    regionTwoClockLogicTimedWord.setRegiontimedActions(regionTwoClockLogicActionList);
//                    //RegionTwoClockLogicTimedWord suffix = ce.subWord(errorIndex + 1, ce.size()).logicTimeWord();
//                    InnerNode innerNode = new InnerNode(regionTwoClockLogicTimedWord);//后缀为EI+1往后的部分
//                    System.out.println("InnerNode:"+innerNode);//paper (a,1.5)
//                    System.out.println("targetNode:"+targetNode);
//                    InnerNode father = targetNode.getPreNode();//paper sift(a,0)的父节点(empty的父节点)
//                    // ResetAnswer resetAnswer = null;
//                    BooleanAnswer resetAnswer = null;
//                    try {
//                        for (BooleanAnswer r : father.getKeyChildMap().keySet()) {
//                            if (father.getChild(r) == targetNode) {
//                                resetAnswer = r;
//                            }
//                        }
//                    }catch (Exception e){
//                        System.out.println(e);
//                    }
//                    father.getKeyChildMap().put(resetAnswer, innerNode);
//                    innerNode.setPreNode(father);//(a,1.5)的父节点置为sift(a,0)的父节点(empty的父节点)
//                    targetNode.setPreNode(innerNode);//sift(a,0)（empty）的父节点置为（a,1.5）
//                    boolean init = resetNextWord.isEmpty();//(a,0)是否为空,准备为(a,0)构建新节点
//                    boolean accept = teacher.membership(resetNextWord,this.clockList.size()).isAccept();//(a,0)是否被接收,准备为(a,0)构建新节点
//                    LeafNode newLeafNode = new LeafNode(resetNextWord, init, accept);//加入叶节点𝑢 ⋅ 𝑐𝑡𝑥[𝐸𝐼]
//                    System.out.println("newLeafNode:"+newLeafNode);
//                    newLeafNode.setPreNode(innerNode);
//                    innerNode.add(answer(targetNode.getWord(), regionTwoClockLogicTimedWord), targetNode);//(a,1.5)构建子节点empty(原来的sift(a,0))
//                    innerNode.add(answer(newLeafNode.getWord(), regionTwoClockLogicTimedWord), newLeafNode);//(a,1.5)构建子节点(a,0)
//                    //refine transition
//                    refineNode(targetNode);
//                    //add transition
//                    refineSymbolTrack(newLeafNode);
//                }
//            }
//
//    }
    public void refine(TwoClockResetLogicTimeWord ce,boolean reuse){
//        ErrorIndexResult result=new ErrorIndexResult(0,null);
//        for (ErrorIndexResult indexResult:errorIndexAnalyse(ce,reuse).keySet()){
//            result.setErrorEnum(indexResult.getErrorEnum());
//            result.setIndex(indexResult.getIndex());
//        }
        System.out.println(member);
        int old=member;
        Map<ErrorIndexResult,RegionTwoClockLogicTimedWord> indexAnalyse=errorIndexAnalyse(ce,reuse,teacher.membership(ce,clockList.size()));
        member++;
        memberctx+=member-old;
        System.out.println("memberctx:"+memberctx);
        int old2=member;
        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//        ErrorIndexResult result = errorIndexAnalyse(ce,reuse).keySet();
//        int errorIndex = result.getIndex();
        System.out.println("indexAnalyse:"+indexAnalyse);
        ErrorIndexResult result=new ErrorIndexResult(0,null);
        for (ErrorIndexResult indexResult:indexAnalyse.keySet()) {
            result.setErrorEnum(indexResult.getErrorEnum());
            result.setIndex(indexResult.getIndex());
            if (indexAnalyse.get(indexResult) != null) {
                regionTwoClockLogicTimedWord = indexAnalyse.get(indexResult);
                regionTwoClockLogicTimedWord.setRegiontimedActions(indexAnalyse.get(indexResult).getRegiontimedActions());
            }
        }
        int errorIndex=result.getIndex();
        System.out.println("errorIndex:"+errorIndex);
        TaLocation uLocation = hypothesis.reach(TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(0, errorIndex)));
        TwoClockLogicAction action = ce.get(errorIndex).logicTimedAction();
        RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord2=new RegionTwoClockLogicTimedWord(null);
        List<RegionTwoClockLogicAction> regionTwoClockLogicActionList2 = new ArrayList<>();
        TwoClockLogicTimeWord timeWord=new TwoClockLogicTimeWord(null);
        List<TwoClockLogicAction> twoClockLogicActionList2=new ArrayList<>();
        twoClockLogicActionList2.add(action);
        timeWord.setTimedActions(twoClockLogicActionList2);
        List<Region> regionList2 = transfertoRegion(timeWord);
        RegionTwoClockLogicAction regionTwoClockLogicAction2 = new RegionTwoClockLogicAction(action.getSymbol(), regionList2.get(0));
        regionTwoClockLogicActionList2.add(regionTwoClockLogicAction2);
        regionTwoClockLogicTimedWord2 = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList2);
        regionTwoClockLogicTimedWord2.setRegiontimedActions(regionTwoClockLogicActionList2);

        if (result.getErrorEnum() == ErrorEnum.ResetError) {//区间划分有误，加入迁移（u,u',ctx[EI]）
            System.out.println("uLocation:"+uLocation);
            LeafNode sourceNode = locationNodeMap.get(uLocation);
            System.out.println("sourceNode:"+sourceNode);
            TwoClockResetLogicTimeWord uWord = sourceNode.getWord();
            System.out.println("uWord:"+uWord);
            TwoClockResetLogicTimeWord resetNextWord=new TwoClockResetLogicTimeWord(null);

            TwoClockLogicTimeWord nextWord=new TwoClockLogicTimeWord(null);
            List<TwoClockLogicAction> timedActions2 = new ArrayList<>();
            for (int i = 0; i < regionTwoClockLogicTimedWord2.getRegiontimedActions().size(); i++) {
                Map<Clock, Double> map = new HashMap<>();
                for (int k=0;k<clockList.size();k++) {
                    map.put(clockList.get(k),0.0);
                }
                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getSymbol(), map);
                timedActions2.add(twoClockLogicAction);
            }
            TwoClockLogicTimeWord logicTimeWord = new TwoClockLogicTimeWord(timedActions2);
            logicTimeWord.setTimedActions(timedActions2);
//            System.out.println("1:"+uWord);
//            System.out.println("2:"+regionTwoClockLogicTimedWord2);
//            System.out.println("3:"+logicTimeWord);
//            System.out.println("4:"+action);
//            System.out.println("5:"+regionTwoClockLogicTimedWord2.getRegiontimedActions().size());
            TwoClockResetLogicTimeWord or=new TwoClockResetLogicTimeWord(resetNextWord.getTimedActions());
            or.setTimedActions(resetNextWord.getTimedActions());
            boolean valid=true;
//            System.out.println("find before:"+selectfromregionc);
//            System.out.println("reset:"+reset);
            if(resetNextWord!=null&resetNextWord.getTimedActions()!=null&&LogictoDelay(resetNextWord)==null){
                valid=false;
            }
            else {
                resetNextWord = selectfromRegion2(regionTwoClockLogicTimedWord2, uWord);
            }
      //      System.out.println("resetNextWord:"+resetNextWord);
            if(valid&&resetNextWord==null||valid&&resetNextWord.getTimedActions()==null||valid&&resetNextWord.getTimedActions().size()==0) {
                boolean eq = false;
                for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
                    if (pairRegion1.prefix.equals(uWord) && pairRegion1.suffix.equals(regionTwoClockLogicTimedWord2)) {
                        eq = true;
                        resetNextWord = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
                    }
                }
                if (!eq) {
                    if (resetNextWord == null || resetNextWord.getTimedActions() == null || resetNextWord.getTimedActions().size() == 0) {

                        boolean allreset = true;
                        for (Clock clock : clockList) {
                            if (uWord==null||uWord.getTimedActions()==null||uWord.getLastResetAction().isReset(clock)) {
                            } else {
                                allreset = false;
                            }
                        }
                        boolean needselect = true;
                        if (allreset) {
                            for (int i1 = 1; i1 < clockList.size(); i1++) {
                                if (regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
                                } else {
                                    needselect = false;
                                }
                            }
                        }
                        if (needselect) {
                            resetNextWord = selectfromRegion(regionTwoClockLogicTimedWord2,uWord);
                        }
                    }
                }
            }

       //     System.out.println("find after:"+selectfromregionc);
       //     System.out.println("reset:"+reset);
//        TwoClockResetLogicTimeWord resetNextWord = teacher.transferWord(nextWord,x,y);
            if(resetNextWord==null){
                List<TwoClockLogicAction> timedActions3 = new ArrayList<>();
                Map<Clock, Double> map = new HashMap<>();

                for (int j = 0; j < clockList.size(); j++) {
                    if (!regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).isLowerBoundOpen()) {
                        map.put(clockList.get(j), Double.valueOf(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound()));
                    } else {
                        BigDecimal b1 = new BigDecimal(Double.toString(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound()));
                        BigDecimal b2 = new BigDecimal(Double.toString(0.1));
                        map.put(clockList.get(j), b1.add(b2).doubleValue());
                    }
                }
                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(action.getSymbol(), map);
                timedActions3.add(twoClockLogicAction);
                //   System.out.println("timedActions:"+timedActions);
                TwoClockLogicTimeWord twoClockLogicTimeWord3 = new TwoClockLogicTimeWord(timedActions3);
                twoClockLogicTimeWord3.setTimedActions(timedActions3);
                if(uWord!=null&&uWord.getTimedActions()!=null){
                nextWord = TwoClockResetLogicTimeWord.logicTimeWord(uWord).concat(action);}
                else {
                    List<TwoClockLogicAction> actions=new ArrayList<>();
                    actions.add(action);
                    nextWord.setTimedActions(actions);
                }
                resetNextWord = teacher.transferWord(nextWord);
                //  }
            }
//            System.out.println("resetNextWord:"+resetNextWord);
//            System.out.println("start sift");
//            System.out.println("selectfromregionc:"+selectfromregionc);
            SiftResult siftResult = sift(resetNextWord);
//            System.out.println("end sift");
//            System.out.println("selectfromregionc:"+selectfromregionc);
            LeafNode targetNode = siftResult.getLeafNode();//u'
   //         System.out.println("targetNode:"+targetNode);
            TwoClockResetLogicAction lastResetAction = resetNextWord.getLastResetAction();
    //        System.out.println("lastResetAction:"+lastResetAction);
         //   System.out.println("trackSet1:"+trackSet);
//            System.out.println("前缀分析错误");
//            System.out.println("sourceNode:"+sourceNode);
//            System.out.println("lastResetAction:"+lastResetAction);
//            System.out.println("targetNode:"+targetNode);
//            System.out.println("trackSet:"+trackSet);
                Track trackadd = new Track(sourceNode, targetNode, lastResetAction);
//                System.out.println("reach!!!!");
//                System.out.println(trackadd);
                trackSet.add(trackadd);
            membertable+=member-old2;
     //       System.out.println("membertable:"+membertable);
                return;
         //   }
        }
        if (result.getErrorEnum() == ErrorEnum.ConsistentError) {//一致性有误，叶节点分裂
         //   System.out.println("叶节点分裂");
        //    System.out.println("uLocation:"+uLocation);
            LeafNode sourceNode = locationNodeMap.get(uLocation);
        //    System.out.println("sourceNode:"+sourceNode);
            TwoClockResetLogicTimeWord uWord = sourceNode.getWord();
          //  System.out.println("uWord:"+uWord);
            TwoClockResetLogicTimeWord resetNextWord=new TwoClockResetLogicTimeWord(null);
            TwoClockLogicTimeWord nextWord=new TwoClockLogicTimeWord(null);
            List<TwoClockLogicAction> timedActions2 = new ArrayList<>();
            for (int i = 0; i < regionTwoClockLogicTimedWord2.getRegiontimedActions().size(); i++) {
                Map<Clock, Double> map = new HashMap<>();
                for (int k=0;k<clockList.size();k++) {
                    map.put(clockList.get(k),0.0);
                }
                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getSymbol(), map);
                timedActions2.add(twoClockLogicAction);
            }
            TwoClockLogicTimeWord logicTimeWord = new TwoClockLogicTimeWord(timedActions2);
            logicTimeWord.setTimedActions(timedActions2);
//            System.out.println("1:"+uWord);
//            System.out.println("2:"+regionTwoClockLogicTimedWord2);
//            System.out.println("3:"+logicTimeWord);
//            System.out.println("4:"+action);
//            System.out.println("5:"+regionTwoClockLogicTimedWord2.getRegiontimedActions().size());
            boolean valid=true;
//            System.out.println("find before:"+selectfromregionc);
//            System.out.println("reset:"+reset);
            if(resetNextWord!=null&resetNextWord.getTimedActions()!=null&&LogictoDelay(resetNextWord)==null){
                valid=false;
            }
            else {
                resetNextWord = selectfromRegion2(regionTwoClockLogicTimedWord2, uWord);
            }
            if(valid&&resetNextWord==null||valid&&resetNextWord.getTimedActions()==null||valid&&resetNextWord.getTimedActions().size()==0) {
                boolean eq = false;
                for (PairRegion pairRegion1 : pairRegionTwoClockResetLogicTimeWordMap.keySet()) {
                    if (pairRegion1.prefix.equals(uWord) && pairRegion1.suffix.equals(regionTwoClockLogicTimedWord2)) {
                        eq = true;
                        resetNextWord = pairRegionTwoClockResetLogicTimeWordMap.get(pairRegion1);
                    }
                }
                if (!eq) {
                    if (resetNextWord == null || resetNextWord.getTimedActions() == null || resetNextWord.getTimedActions().size() == 0) {

                        boolean allreset = true;
                        for (Clock clock : clockList) {
                            if (uWord.getLastResetAction().isReset(clock)) {
                            } else {
                                allreset = false;
                            }
                        }
                        boolean needselect = true;
                        if (allreset) {
                            for (int i1 = 1; i1 < clockList.size(); i1++) {
                                if (regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(0).getLowerBound() * 100 == regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(i1).getLowerBound() * 100) {
                                } else {
                                    needselect = false;
                                }
                            }
                        }
                        if (needselect) {
                            resetNextWord = selectfromRegion(regionTwoClockLogicTimedWord2,uWord);
                        }
                    }
                }
            }
//            System.out.println("find after:"+selectfromregionc);
//            System.out.println("reset:"+reset);
            if(resetNextWord==null){
                List<TwoClockLogicAction> timedActions3 = new ArrayList<>();
                Map<Clock, Double> map = new HashMap<>();

                for (int j = 0; j < clockList.size(); j++) {
                    if (!regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).isLowerBoundOpen()) {
                        map.put(clockList.get(j), Double.valueOf(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound()));
                    } else {
                        BigDecimal b1 = new BigDecimal(Double.toString(regionTwoClockLogicTimedWord2.getRegiontimedActions().get(0).getRegion().getTimeGuardList().get(j).getLowerBound()));
                        BigDecimal b2 = new BigDecimal(Double.toString(0.1));
                        map.put(clockList.get(j), b1.add(b2).doubleValue());
                    }
                }
                TwoClockLogicAction twoClockLogicAction = new TwoClockLogicAction(action.getSymbol(), map);
                timedActions3.add(twoClockLogicAction);
                TwoClockLogicTimeWord twoClockLogicTimeWord3 = new TwoClockLogicTimeWord(timedActions3);
                twoClockLogicTimeWord3.setTimedActions(timedActions3);
                nextWord = TwoClockResetLogicTimeWord.logicTimeWord(uWord).concat(action);
                resetNextWord = teacher.transferWord(nextWord);
            }
//            System.out.println("resetNextWord:"+resetNextWord);
//            System.out.println("start sift");
//            System.out.println("selectfromregionc:"+selectfromregionc);
            SiftResult siftResult = sift(resetNextWord);
//            System.out.println("end sift");
//            System.out.println("selectfromregionc:"+selectfromregionc);
//            System.out.println("siftResult:"+siftResult.getLeafNode().getword());
            TaLocation locationq = hypothesis.reach(TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(0,errorIndex+1)));
            TwoClockResetLogicTimeWord uWordq = locationNodeMap.get(locationq).getWord();
//            System.out.println("uWordq:"+uWordq);
//            System.out.println("uWordq:"+siftResult.getLeafNode().getword());
//            System.out.println(siftResult.getLeafNode().getword().equals(uWordq));
            if(!siftResult.getLeafNode().getword().equals(uWordq)){
                System.out.println("case 1");
                Track trackadd = new Track(sourceNode, siftResult.getLeafNode(), resetNextWord.getLastResetAction());
                trackSet.add(trackadd);
                return;
            }
            else {
                LeafNode targetNode = siftResult.getLeafNode();//u'
              //  TwoClockLogicTimeWord suffix = TwoClockResetLogicTimeWord.logicTimeWord(ce.subWord(errorIndex + 1, ce.size()));
//                RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
//                List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
//                for(int m=0;m<suffix.size();m++){
//                    List<Region> regionList = transfertoRegion(suffix.subWord(m,m+1));
//                    RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(suffix.get(m).getSymbol(), regionList.get(0));
//                    regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
//                }
//                regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
//                regionTwoClockLogicTimedWord.setRegiontimedActions(regionTwoClockLogicActionList);
                InnerNode innerNode = new InnerNode(regionTwoClockLogicTimedWord);//后缀为EI+1往后的部分
                System.out.println("InnerNode:"+innerNode);//paper (a,1.5)
                System.out.println("targetNode:"+targetNode);
                InnerNode father = targetNode.getPreNode();//paper sift(a,0)的父节点(empty的父节点)
                // ResetAnswer resetAnswer = null;
                BooleanAnswer resetAnswer = null;
                try {
                    for (BooleanAnswer r : father.getKeyChildMap().keySet()) {
                        if (father.getChild(r) == targetNode) {
                            resetAnswer = r;
                        }
                    }
                }catch (Exception e){
                    System.out.println(e);
                }
                father.getKeyChildMap().put(resetAnswer, innerNode);
                innerNode.setPreNode(father);//(a,1.5)的父节点置为sift(a,0)的父节点(empty的父节点)
                targetNode.setPreNode(innerNode);//sift(a,0)（empty）的父节点置为（a,1.5）
                LeafNode newLeafNode = new LeafNode(resetNextWord, false, false);//加入叶节点𝑢 ⋅ 𝑐𝑡𝑥[𝐸𝐼]
                System.out.println("newLeafNode:"+newLeafNode);
System.out.println(leafList());
                    List<LeafNode> nodeList = leafList();
                    for (int i = 0; i < nodeList.size(); i++) {
                        LeafNode node = nodeList.get(i);
                        System.out.println("node.getWord():"+node.getWord());
                    }
                System.out.println("end");
                newLeafNode.setPreNode(innerNode);
//                innerNode.add(answerforanalyzesuffixerrorindex(targetNode.getWord(), regionTwoClockLogicTimedWord), targetNode);//(a,1.5)构建子节点empty(原来的sift(a,0))
                innerNode.add(qanswer, targetNode);//(a,1.5)构建子节点empty(原来的sift(a,0))
                innerNode.add(answerforanalyzesuffixerrorindex(newLeafNode.getWord(), regionTwoClockLogicTimedWord), newLeafNode);//(a,1.5)构建子节点(a,0)
                //refine transition
                System.out.println("refine");
                refineNode(targetNode);
                //add transition
                System.out.println("refineSymbolTrack");
                refineSymbolTrack(newLeafNode);
            }
            membertable+=member-old2;
            System.out.println("membertable:"+membertable);
        }
//        membertable+=member-old2;
//        System.out.println("membertable:"+membertable);
    }

//    @Override
//    public boolean check(TwoClockResetLogicTimeWord counterExample) {
//        try{
//            errorIndexAnalyse(counterExample);
//        }catch (Exception e){
//            return false;
//        }
//        return true;
//    }

    private Map<ErrorIndexResult,RegionTwoClockLogicTimedWord> errorIndexAnalyse(TwoClockResetLogicTimeWord ce,boolean reuse,boolean ceqc) {
        if(!reuse){
            errorid=0;
            regionerror=new ArrayList<>();
        }
        System.out.println("errorid:"+errorid);
        for (int i = errorid; i < ce.size(); i++) {
            TwoClockResetLogicTimeWord prefix = ce.subWord(0, i+1);
            TwoClockResetLogicTimeWord hWord = teacher.transferWord(hypothesis,TwoClockResetLogicTimeWord.logicTimeWord(prefix));
            if (!prefix.equals(hWord)) {
                errorid=i;
                Map<ErrorIndexResult,RegionTwoClockLogicTimedWord> map=new HashMap<>();
                map.put(new ErrorIndexResult(i, ErrorEnum.ResetError),null);
                return map;
            }

            TwoClockResetLogicTimeWord suffix = ce.subWord(i+1, ce.size());
            TaLocation location = hypothesis.reach(TwoClockResetLogicTimeWord.logicTimeWord(prefix));
            TwoClockResetLogicTimeWord uWord = locationNodeMap.get(location).getWord();
            System.out.println("suffix:"+suffix);
            RegionTwoClockLogicTimedWord regionTwoClockLogicTimedWord=new RegionTwoClockLogicTimedWord(null);
            if(i+1<=regionerror.size()){
                regionTwoClockLogicTimedWord=regionerror.get(i);
                regionTwoClockLogicTimedWord.setRegiontimedActions(regionerror.get(i).getRegiontimedActions());
                System.out.println("use");
            }
           else {
                if (suffix != null && suffix.getTimedActions() != null && suffix.getTimedActions().size() != 0) {
                    List<RegionTwoClockLogicAction> regionTwoClockLogicActionList = new ArrayList<>();
                    for (int m = 0; m < suffix.size(); m++) {
                        List<Region> regionList = transfertoRegion(TwoClockResetLogicTimeWord.logicTimeWord(suffix.subWord(m, m + 1)));
                        RegionTwoClockLogicAction regionTwoClockLogicAction = new RegionTwoClockLogicAction(suffix.get(m).getSymbol(), regionList.get(0));
                        regionTwoClockLogicActionList.add(regionTwoClockLogicAction);
                    }
                    regionTwoClockLogicTimedWord = new RegionTwoClockLogicTimedWord(regionTwoClockLogicActionList);
                    regionTwoClockLogicTimedWord.setRegiontimedActions(regionTwoClockLogicActionList);
                    regionerror.add(regionTwoClockLogicTimedWord);
                }
            }
            System.out.println("prefix:"+prefix);
            System.out.println("uWordz:"+uWord);
            System.out.println("regionTwoClockLogicTimedWord.getRegiontimedActions():"+regionTwoClockLogicTimedWord.getRegiontimedActions());
            if(error1.equals(prefix)&&error2.equals(uWord)&&errorsuff.equals(suffix)){
               // return new ErrorIndexResult(i, ErrorEnum.ConsistentError);
                Map<ErrorIndexResult,RegionTwoClockLogicTimedWord> map=new HashMap<>();
                map.put(new ErrorIndexResult(i, ErrorEnum.ResetError),regionTwoClockLogicTimedWord);
                return map;
            }

//            BooleanAnswer key1 = answerforanalyzesuffixerrorindex(prefix, regionTwoClockLogicTimedWord);
            List<Resets> resetsList = new ArrayList<>();
            for (int i1 = i+1; i1 < ce.size(); i1++) {
                boolean[] actualreset = new boolean[clockList.size()];
                for (int m = 0; m < clockList.size(); m++) {
                    if(ce.getTimedActions().get(i1).getResetClockSet().contains(clockList.get(m))){
                        actualreset[m] = true;
                    }
                }
                Resets resets = new Resets(actualreset);
                resetsList.add(resets);
            }
            BooleanAnswer key1 = new BooleanAnswer(resetsList,ceqc);
            key1.setAccept(ceqc);
            key1.setResets(resetsList);
         //  System.out.println("key11:"+key1.isAccept());

            BooleanAnswer key2 = answerforanalyzesuffixerrorindex(uWord, regionTwoClockLogicTimedWord);
         //   System.out.println("key1:"+key1.isAccept());
         //   System.out.println("key2:"+key2.isAccept());
         //   System.out.println("key1:"+key1.getResets());
         //   System.out.println("key2:"+key2.getResets());
            if (!key1.equals(key2)) {
                qanswer=key2;
                qanswer.setResets(key2.getResets());
                qanswer.setAccept(key2.isAccept());
        //        System.out.println(i);
                error1.setTimedActions(prefix.getTimedActions());
                error2.setTimedActions(uWord.getTimedActions());
                errorsuff.setTimedActions(suffix.getTimedActions());
                errorid=i;
                Map<ErrorIndexResult,RegionTwoClockLogicTimedWord> map=new HashMap<>();
                map.put(new ErrorIndexResult(i, ErrorEnum.ConsistentError),regionTwoClockLogicTimedWord);
                return map;
            //    return new ErrorIndexResult(i, ErrorEnum.ConsistentError);
            }
        }
        throw new RuntimeException("找不到错误位置，请检查代码");
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

    private boolean checkIsPass(TaLocation qu, TaLocation qv, TwoClockResetLogicAction action) {
        List<TaTransition> transitionList = getHypothesis().getTransitions(qu, null, qv);
//        Clock clock1 = hypothesis.getClockList().get(0);
//        Clock clock2 = hypothesis.getClockList().get(1);
        boolean isPass = false;
        for (TaTransition t : transitionList) {
//            Map<Clock, Double> clockDoubleMap = new HashMap<>();
//            System.out.println("hypothesis.getClockList().get(0)"+hypothesis.getClockList().get(0));
//            clockDoubleMap.put(clock1, action.getValue(x));
//            clockDoubleMap.put(clock2, action.getValue(y));
//            System.out.println("ispass:"+action);
//            System.out.println(t);
            if (t.isPass(action.getSymbol(), action.getClockValueMap())) {
                isPass = true;
                for (int i=0;i<this.clockList.size();i++){
                    if(t.isReset(this.clockList.get(i)) != action.isReset(this.clockList.get(i))){
                        isPass = false;
                    }
                }
                break;
            }
        }
        return isPass;
    }

    //把指向TargetNode的迁移重新分配
    private void refineNode(LeafNode targetNode) {
        RegionTwoClockLogicTimedWord suffix = targetNode.getPreNode().getWord();
        Iterator<Track> iterator = trackSet.iterator();
        List<LeafNode> newNodeWordList = new ArrayList<>();
        while (iterator.hasNext()) {
            Track track = iterator.next();
            if (track.getTarget().equals(targetNode)) {
                TwoClockResetLogicAction resetLogicAction = track.getAction();
                TwoClockResetLogicTimeWord resetLogicTimeWord = new TwoClockResetLogicTimeWord(null);
                List<TwoClockResetLogicAction> actions=new ArrayList<>();
                if(track.getSource().getWord()!=null&&track.getSource().getWord().getTimedActions()!=null&&track.getSource().getWord().getTimedActions().size()!=0){
                actions.addAll(track.getSource().getWord().getTimedActions());}
                actions.add(resetLogicAction);
                resetLogicTimeWord.setTimedActions(actions);
                BooleanAnswer key = answerforanalyzesuffixerrorindex(resetLogicTimeWord, suffix);
                InnerNode innerNode = targetNode.getPreNode();
                LeafNode node = (LeafNode) innerNode.getChild(key);
                if (node == null) {
                    node = new LeafNode(resetLogicTimeWord,
                            false, false);
                    node.setPreNode(innerNode);
                    innerNode.add(key, node);
                    newNodeWordList.add(node);
                }
                track.setTarget(node);
            }
        }
        for (LeafNode newLeafNode : newNodeWordList) {
            refineSymbolTrack(newLeafNode);
        }
    }

    @Override
    public TwoClockTA buildHypothesis() {
        locationNodeMap = new HashMap<>();
        nodeLocationMap = new HashMap<>();

        List<TaLocation> locationList = buildLocationList();

        List<TaTransition> transitionList = buildTransitionList();
      //  System.out.println("transitionList:"+transitionList);
//        List<Clock> clockList=new ArrayList<>();
//        clockList.add(clock1);
//        clockList.add(clock2);
        TwoClockTA evidenceDOTA = new TwoClockTA(name, clockList,sigma, locationList, transitionList);
        evidenceToDOTA(evidenceDOTA,null);
        this.hypothesis = evidenceDOTA;
        System.out.println("membertable:"+membertable);
        System.out.println("memberctx:"+memberctx);
        System.out.println("member:"+member);
        return hypothesis;
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
                 //   System.out.println("timeGuardListy:"+timeGuardListy);
                    List<TimeGuard> complementaryGuardListy = complementaryoneclock(timeGuardListy, clock2LowerBound, clock2isopen);
                //    System.out.println("complementaryGuardListy:"+complementaryGuardListy);
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


    public static List<TimeGuard> complementaryoneclockforoneclock(List<TimeGuard> guardList,double LowerBound,boolean clock2isopen){
        List<TimeGuard> complementaryList = new ArrayList<>();

        if(guardList.isEmpty()){
            // System.out.println("guardList为空");
            TimeGuard guard = new TimeGuard(clock2isopen,true,(int)LowerBound, TimeGuard.MAX_TIME);
            complementaryList.add(guard);
            return complementaryList;
        }
   //     System.out.println("guardList"+guardList);
        guardList.sort(new TimeGuardComparator());
     //   System.out.println("guardList sort"+guardList);
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

    private void evidenceToDOTA(TwoClockTA dota,List<TaLocation> regionsplit) {

        List<TaTransition> Totaltransitions=new ArrayList<>();
        List<TaTransition> newregionbypartition=new ArrayList<>();
        for (TaLocation l : dota.getLocations()) {
            List<TaTransition> transitionList = new ArrayList<>();
            for (String action : dota.getSigma()) {
                List<TaTransition> transitionListsigma = new ArrayList<>();
                List<TaTransition> transitions = dota.getTransitions(l, action, null);
                transitions.sort(new OTATranComparator(clockList.get(0)));
        //        System.out.println("transitions:"+transitions);
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
          //      System.out.println("transitionListsigma:"+transitionListsigma);
                for (int i1 = transitions.size() - 1; i1 >= 0; i1--) {
             //       System.out.println("transition:"+transitions.get(i1));
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
                   // System.out.println("transitionListsigma old:"+transitionListsigma);
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
           //     System.out.println("transitionsigma new:"+transitionListsigma);
                transitionList.addAll(transitionListsigma);
            }
            Totaltransitions.addAll(transitionList);
        }

        dota.setTransitions(Totaltransitions);
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
                        //   System.out.println("t:" + t.toString());
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
//            System.out.println("区间的timeGuardListy:" + timeGuardListy);
//            System.out.println("区间的complementaryGuardListy:" + complementaryGuardListy);
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
                    //    System.out.println("t:" + t.toString());
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
        //System.out.println(transitionList);
        // List<TaTransition> complementaryTranList=new ArrayList<>();
        List<TaTransition> transitionList1=new ArrayList<>();
        List<Map<Clock,TimeGuard>> mapListLast=new ArrayList<>();
        for (int i=0;i<transitionList.size();i++){
            //      System.out.println("complementary中transitionList.get(i):"+transitionList.get(i));
            List<Map<Clock,TimeGuard>> mapList=new ArrayList<>();
            for (int j=0;j<transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().size();j++){
                if(transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getLowerBound()>timeGuardList.get(j).getLowerBound()||
                        transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getLowerBound()==timeGuardList.get(j).getLowerBound()&& transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).isLowerBoundOpen()&&timeGuardList.get(j).isLowerBoundClose()){
                    TimeGuard timeGuard = new TimeGuard(timeGuardList.get(j).isLowerBoundOpen(),!transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).isLowerBoundOpen(),timeGuardList.get(j).getLowerBound(),transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getLowerBound());
                    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
                    clockTimeGuardMap.put(clockList.get(j), timeGuard);
                    mapList.add(clockTimeGuardMap);
                }
//                else {
//                    System.out.println("特殊处理");
//                    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<>();
//                    clockTimeGuardMap.put(clockList.get(j), timeGuardList.get(j));
//                    mapList.add(clockTimeGuardMap);
//                }
//                if(transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getUpperBound()==TimeGuard.MAX_TIME){
//
//                }
//                else {
//                    TimeGuard timeGuard2 = new TimeGuard(!transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).isUpperBoundOpen(),true,transitionList.get(i).getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(j)).getUpperBound(),TimeGuard.MAX_TIME);
//                    Map<Clock,TimeGuard> clockTimeGuardMap2=new HashMap<>();
//                    clockTimeGuardMap2.put(clockList.get(j), timeGuard2);
//                    mapList.add(clockTimeGuardMap2);
//                }
            }
//            System.out.println("mapListLast:"+mapListLast);
//            System.out.println("mapList:"+mapList);
            if(mapList.size()==mapListLast.size()){
                boolean eq=true;
                for (int j=0;j<mapList.size();j++){
                    for (Clock clock:mapList.get(j).keySet()){
//                        System.out.println(mapList.get(j).get(clock));
//                        System.out.println("mapListLast.get(j).get(clock):"+mapListLast.get(j).get(clock));
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
            // System.out.println("clear之后mapList:"+mapList);
            if(mapList!=null&&mapList.size()!=0){
           //     System.out.println("mapList:"+mapList);
                mapListLast=mapList;
                //   System.out.println("transitionList1:"+transitionList1);
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
                            //          Set<Clock> resetclockSet = new HashSet<>();
//                        for (int n = 0; n < clockList.size(); n++) {
////                            System.out.println("clockList.get(n):"+clockList.get(n));
////                            System.out.println("clockSet.contains(clockList.get(n):"+clockSet.contains(clockList.get(n)));
//                            if(clockSet.contains(clockList.get(n)));
//                            resetclockSet.add(clockList.get(n));
//                        }
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

//                    Set<Clock> resetclockSet = new HashSet<>();
//                    for (int n = 0; n < clockList.size(); n++) {
////                        System.out.println("clockList.get(n):"+clockList.get(n));
////                        System.out.println("clockSet.contains(clockList.get(n):"+clockSet.contains(clockList.get(n)));
//                        if(clockSet.contains(clockList.get(n)));
//                        resetclockSet.add(clockList.get(n));
//                    }

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

    private List<TaTransition> buildTransitionList() {
        List<TaTransition> transitionList = new ArrayList<>();
     //   System.out.println(trackSet);
        for (Track track : trackSet) {
    //        System.out.println("track:"+track);
            LeafNode sourceNode = track.getSource();
            LeafNode targetNode = track.getTarget();
            TaLocation sourceLocation = nodeLocationMap.get(sourceNode);
            TaLocation targetLocation = nodeLocationMap.get(targetNode);
            TwoClockResetLogicAction action = track.getAction();
            String symbol = action.getSymbol();
            Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
            for (int i=0;i<this.clockList.size();i++){
                TimeGuard timeGuard = TimeGuard.TwoClockbottomGuard(action.logicTimedAction(),this.clockList.get(i));
                clockTimeGuardMap.put(this.clockList.get(i), timeGuard);
            }
//            TimeGuard timeGuard1 = TimeGuard.TwoClockbottomGuard(action.logicTimedAction(),clock1);
//            TimeGuard timeGuard2 = TimeGuard.TwoClockbottomGuard(action.logicTimedAction(),clock2);
//            clockTimeGuardMap.put(clock1, timeGuard1);
//            clockTimeGuardMap.put(clock2, timeGuard2);
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
            Set<Clock> resetClockSet = new HashSet<>();
            for (int i=0;i<this.clockList.size();i++){
                if (action.isReset(this.clockList.get(i))) {
                    resetClockSet.add(this.clockList.get(i));
                }
            }
//            TaTransition transition = new TaTransition(transitionList.get(0).getSourceLocation(), targetLocation, transitionList.get(0).getSymbol(), edgeTimeGuard, resetClockSet);
            TaTransition transition = new TaTransition(
                    sourceLocation, targetLocation, symbol, edgeTimeGuard, resetClockSet);
        //    System.out.println(transition);
            if (!transitionList.contains(transition)) {
                transitionList.add(transition);
            }
        }
    //    System.out.println(transitionList);
        return transitionList;
    }

    private List<TaLocation> buildLocationList() {
        List<LeafNode> nodeList = leafList();
        List<TaLocation> locationList = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            LeafNode node = nodeList.get(i);
            boolean isInit=node.getword()==null||node.getword().getTimedActions()==null||node.getword().getTimedActions().size()==0;
    //      System.out.println(member);
            boolean isAccepted=answerforanalyzesuffixerrorindex(node.getword(),null).isAccept();
       //     System.out.println(member);
            TaLocation location = new TaLocation(
                    String.valueOf(i + 1),
                    String.valueOf(i + 1),
                    isInit,
                    isAccepted);
         //   System.out.println(location.getId()+node.toString());
            locationList.add(location);
            nodeLocationMap.put(node, location);
            locationNodeMap.put(location, node);
        }
        return locationList;
    }

    private List<LeafNode> leafList() {
        Map<TwoClockResetLogicTimeWord, LeafNode> leafMap = getLeafMap();
        return new ArrayList<>(leafMap.values());
    }

    private Map<TwoClockResetLogicTimeWord, LeafNode> getLeafMap() {
        Map<TwoClockResetLogicTimeWord, LeafNode> leafMap = new HashMap<>();
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (node.isLeaf()) {
                LeafNode leaf = (LeafNode) node;
                TwoClockResetLogicTimeWord suffix = leaf.getWord();
                leafMap.put(suffix, leaf);
            } else {
                InnerNode innerNode = (InnerNode) node;
                queue.addAll(innerNode.getChildList());
            }
        }
        return leafMap;
    }

    @Override
    public TwoClockTA getFinalHypothesis() {
        return hypothesis;
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
       // System.out.println("s2:"+ctx.mkReal(s2.concat("/1000")));
        BoolExpr equation = ctx.mkEq(left, ctx.mkReal(s2.concat("/1000")));
        s.add(equation);
        if(alo){
            String s1=String.valueOf( (int)(lowera1*1000));
            s.add(ctx.mkGt(a,  ctx.mkReal(s1.concat("/1000"))));
        }
        else {
            String s1=String.valueOf( (int)(lowera1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(a,  ctx.mkReal(s1.concat("/1000"))));
        }
        String s3=String.valueOf( (int)(lowera2*1000));
      //  System.out.println("s3:"+ctx.mkReal(s3.concat("/1000")));
        s.add(ctx.mkGe(a,  ctx.mkReal(s3.concat("/1000"))));
        if(auo){
            String s1=String.valueOf( (int)(uppera1*1000));
      //      System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGt(  ctx.mkReal(s1.concat("/1000")),a));
        }
        else {
            String s1=String.valueOf( (int)(uppera1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(  ctx.mkReal(s1.concat("/1000")),a));
        }
        if(clo){
            String s1=String.valueOf( (int)(lowerc1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGt(c,  ctx.mkReal(s1.concat("/1000"))));
        }
        else {
            String s1=String.valueOf( (int)(lowerc1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGe(c,  ctx.mkReal(s1.concat("/1000"))));
        }
        String s4=String.valueOf( (int)(lowerc2*1000));
     //   System.out.println("s4:"+ctx.mkReal(s4.concat("/1000")));
        s.add(ctx.mkGe(c,  ctx.mkReal(s4.concat("/1000"))));
        if(cuo) {
            String s1=String.valueOf( (int)(upperc1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
            s.add(ctx.mkGt( ctx.mkReal(s1.concat("/1000")),c));
        }
        else {
            String s1=String.valueOf( (int)(upperc1*1000));
       //     System.out.println("s1:"+ctx.mkReal(s1.concat("/1000")));
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
           // System.out.println("results:"+results[0]+results[1]);
            return results;

        }
        else if(result == Status.UNSATISFIABLE)
            System.out.println("unsat");
        return null;
//        else
//            System.out.println("unknow");
//        return null;
    }
    @Override
    public void show() {
        System.out.println("trackSet:"+trackSet);
        List<InnerNode> innerNodes=new ArrayList<>();
        innerNodes.add(root);
        for (;;){
            System.out.println("innerNodes.get(0).getWord():"+innerNodes.get(0).getWord().toString());
            System.out.println("innerNodes.get(0).getKeyChildMap():"+innerNodes.get(0).getKeyChildMap());
            System.out.println("innerNodes.get(0).getChildList():"+innerNodes.get(0).getChildList());
            for(int j=0;j<innerNodes.get(0).getChildList().size();j++) {
                if (innerNodes.get(0).getChildList().get(j).isInnerNode()) {
                    innerNodes.add((InnerNode)innerNodes.get(0).getChildList().get(j));
                }
            }
            innerNodes.remove(innerNodes.get(0));
            if(innerNodes.size()==0){
                break;
            }
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
