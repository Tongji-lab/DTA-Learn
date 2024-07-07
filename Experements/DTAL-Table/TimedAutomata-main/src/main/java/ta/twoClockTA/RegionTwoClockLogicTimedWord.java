package ta.twoClockTA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionTwoClockLogicTimedWord {
    public List<RegionTwoClockLogicAction> timedActions;
   public List<RegionTwoClockLogicAction> getTimedActions(){
       return this.timedActions;
   }
   public void  setTimedActions(List<RegionTwoClockLogicAction> timedActions){
       this.timedActions=timedActions;
   }
    public RegionTwoClockLogicTimedWord(List<RegionTwoClockLogicAction> timedActions) {
        this.timedActions = timedActions;
//        super(timedActions);
    }

    public int size() {
        return timedActions.size();
    }

    public boolean isEmpty() {
        return this.timedActions.isEmpty();
    }

    public RegionTwoClockLogicAction getLastLogicAction() {
        RegionTwoClockLogicAction resetAction = (RegionTwoClockLogicAction)this.getTimedActions().get(this.size() - 1);
        return new RegionTwoClockLogicAction(resetAction.getSymbol(), resetAction.getRegion());
    }

    public RegionTwoClockLogicTimedWord subWord(int fromIndex, int toIndex) {
        try {
            List<RegionTwoClockLogicAction> subList = this.getTimedActions().subList(fromIndex, toIndex);
            RegionTwoClockLogicTimedWord twoClockResetLogicTimeWord = new RegionTwoClockLogicTimedWord(subList);
            twoClockResetLogicTimeWord.setTimedActions(subList);
            return twoClockResetLogicTimeWord;
        } catch (Exception var5) {
            return emptyWord();
        }
    }

    public RegionTwoClockLogicAction getLastResetAction() {
        return (RegionTwoClockLogicAction)this.getTimedActions().get(this.size() - 1);
    }

    public RegionTwoClockLogicAction get(int i) {
        return timedActions.get(i);
    }
    public static RegionTwoClockLogicTimedWord emptyWord() {
        // return new TwoClockLogicTimeWord();
        return new RegionTwoClockLogicTimedWord(new ArrayList<RegionTwoClockLogicAction>());
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




