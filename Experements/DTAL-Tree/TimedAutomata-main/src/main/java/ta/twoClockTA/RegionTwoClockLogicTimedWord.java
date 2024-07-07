package ta.twoClockTA;

import timedAction.TwoClockTimedAction;
import timedWord.TwoClockTimedWord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionTwoClockLogicTimedWord extends TwoClockTimedWord<RegionTwoClockLogicAction> {
    public List<RegionTwoClockLogicAction> timedActions;
    public List<RegionTwoClockLogicAction> regiontimedActions;
   public List<RegionTwoClockLogicAction> getTimedActions(){
       return this.timedActions;
   }
   public void  setTimedActions(List<RegionTwoClockLogicAction> timedActions){
       this.timedActions=timedActions;
   }
    public void  setRegiontimedActions(List<RegionTwoClockLogicAction> regiontimedActions){
        this.regiontimedActions=regiontimedActions;
    }
    public List<RegionTwoClockLogicAction>  getRegiontimedActions(){
        return this.regiontimedActions;
    }
//   @Override
    public RegionTwoClockLogicTimedWord(List<RegionTwoClockLogicAction> timedActions) {
  //      this.timedActions = timedActions;
      super(timedActions);
    }
  public RegionTwoClockLogicTimedWord subWord( int fromIndex, int toIndex){
//       return null;
      List<RegionTwoClockLogicAction> subList = this.getRegiontimedActions().subList(fromIndex, toIndex);
      RegionTwoClockLogicTimedWord twoClockResetLogicTimeWord = new RegionTwoClockLogicTimedWord(subList);
      twoClockResetLogicTimeWord.setRegiontimedActions(subList);
      return twoClockResetLogicTimeWord;
  }

    public TwoClockTimedWord concat(RegionTwoClockLogicAction timedAction){
       return null;
    }

    public TwoClockLogicTimeWord concat2(TwoClockLogicTimeWord logicTimeWord, TwoClockLogicAction timedAction){
       return null;
    }

    public int size() {
        return timedActions.size();
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




