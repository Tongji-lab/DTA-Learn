package ta.twoClockTA;

import java.util.ArrayList;
import java.util.List;

public class RegionTwoClockResetLogicTimedWord {

    public List<RegionTwoClockResetLogicAction> timedActions;
    public List<RegionTwoClockResetLogicAction> getTimedActions(){
        return this.timedActions;
    }
    public void  setTimedActions(List<RegionTwoClockResetLogicAction> timedActions){
        this.timedActions=timedActions;
    }
    public RegionTwoClockResetLogicTimedWord(List<RegionTwoClockResetLogicAction> timedActions) {
        this.timedActions = timedActions;
//        super(timedActions);
    }
    public static RegionTwoClockResetLogicTimedWord emptyWord() {
        //return new TwoClockResetLogicTimeWord();
        return new RegionTwoClockResetLogicTimedWord(new ArrayList<RegionTwoClockResetLogicAction>());
    }
    public int size() {
        return timedActions.size();
    }

    public boolean isEmpty() {
        if(this.timedActions==null||this.timedActions.size()==0){
            return true;
        }
//        return this.timedActions.isEmpty();
        else {
            return false;
        }
    }

    public RegionTwoClockLogicAction getLastLogicAction() {
        RegionTwoClockResetLogicAction resetAction = (RegionTwoClockResetLogicAction)this.getTimedActions().get(this.size() - 1);
        return new RegionTwoClockLogicAction(resetAction.getSymbol(), resetAction.getRegion());
    }

    public RegionTwoClockResetLogicTimedWord subWord(int fromIndex, int toIndex) {
        try {
            List<RegionTwoClockResetLogicAction> subList = this.getTimedActions().subList(fromIndex, toIndex);
            RegionTwoClockResetLogicTimedWord twoClockResetLogicTimeWord = new RegionTwoClockResetLogicTimedWord(subList);
            twoClockResetLogicTimeWord.setTimedActions(subList);
            return twoClockResetLogicTimeWord;
        } catch (Exception var5) {
            return emptyWord();
        }
    }

    public RegionTwoClockResetLogicAction getLastResetAction() {
        return (RegionTwoClockResetLogicAction)this.getTimedActions().get(this.size() - 1);
    }

    public static RegionTwoClockLogicTimedWord logicTimeWord(RegionTwoClockResetLogicTimedWord resetLogicTimeWord) {
        List<RegionTwoClockLogicAction> logicTimedActions = new ArrayList<>();
        for(RegionTwoClockResetLogicAction resetlogicactions:resetLogicTimeWord.getTimedActions()) {
            RegionTwoClockLogicAction logicTimedAction = new RegionTwoClockLogicAction(resetlogicactions.getSymbol(), resetlogicactions.getRegion());
            logicTimedAction.setRegion(resetlogicactions.getRegion());
            logicTimedAction.setSymbol(resetlogicactions.getSymbol());
            logicTimedActions.add(logicTimedAction);
        }
        RegionTwoClockLogicTimedWord logicTimeWord=new RegionTwoClockLogicTimedWord(logicTimedActions);
        logicTimeWord.setTimedActions(logicTimedActions);
        return logicTimeWord;
    }
    public boolean contains(RegionTwoClockResetLogicTimedWord regionTwoClockResetLogicTimedWord){
//System.out.println("contains:"+regionTwoClockResetLogicTimedWord.getTimedActions());
//        System.out.println("contains:"+this.getTimedActions());
        if(regionTwoClockResetLogicTimedWord.getTimedActions()==null&&this.getTimedActions()==null){
            return true;
        }
     if(regionTwoClockResetLogicTimedWord.getTimedActions()!=null&&this.getTimedActions()!=null&&regionTwoClockResetLogicTimedWord.getTimedActions().size()==this.getTimedActions().size()) {
         Boolean eq = true;

         for (int i = 0; i < this.getTimedActions().size(); i++) {
             boolean clockeq=true;
             for (int k=0;k<this.getTimedActions().get(0).getRegion().getTimeGuardList().size();k++){
                 if(this.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).getLowerBound()!=regionTwoClockResetLogicTimedWord.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).getLowerBound()||
                         this.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).isLowerBoundOpen()!=regionTwoClockResetLogicTimedWord.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).isLowerBoundOpen()||
                         this.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).getUpperBound()!=regionTwoClockResetLogicTimedWord.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).getUpperBound()||
                         this.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).isUpperBoundOpen()!=regionTwoClockResetLogicTimedWord.getTimedActions().get(i).getRegion().getTimeGuardList().get(k).isUpperBoundOpen()){
                     clockeq=false;
                 }

             }
             if (this.getTimedActions().get(i).getSymbol() == regionTwoClockResetLogicTimedWord.getTimedActions().get(i).getSymbol() &&clockeq
             ) {

             } else {
                 eq = false;
                 break;
             }
         }
         if (eq) {
             return true;

         }
         else {
         return false;}
     }
     return false;
    }
    public RegionTwoClockResetLogicAction get(int i) {
        return timedActions.get(i);
    }
    public static TwoClockLogicTimeWordTwoClockLogicTimeWord concatRegion(TwoClockLogicTimeWord timedword1, RegionTwoClockLogicTimedWord timedword2) {
        //  System.out.println("TwoClockLogicTimeWord函数中timedword1"+timedword1);
        //   System.out.println("TwoClockLogicTimeWord函数中timedword2"+timedword2);
        TwoClockLogicTimeWordTwoClockLogicTimeWord twoClockLogicTimeWord;
        if (timedword1.getTimedActions() == null) {
            twoClockLogicTimeWord=new TwoClockLogicTimeWordTwoClockLogicTimeWord(null,timedword2);
        } else {
            twoClockLogicTimeWord=new TwoClockLogicTimeWordTwoClockLogicTimeWord(timedword1,timedword2);

        }
        return twoClockLogicTimeWord;
    }


}
