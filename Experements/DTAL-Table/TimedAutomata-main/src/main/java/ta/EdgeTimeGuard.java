package ta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeTimeGuard {
    Map<Clock,TimeGuard> clockTimeGuardMap=new HashMap<Clock, TimeGuard>();
//    private boolean largerclock=false;//no difference is 0,otherwise is 1;
//
//    private boolean haveupperdiff=false;
//    private int differencexyupper;
//    private boolean havelowerdiff=false;
//    private int differencexylower;
//
//    private boolean isequalupper;
//
//    private boolean isequallower;
List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards=new ArrayList<>();
    List<EdgeTimeGuard> edgeTimeGuardList;
    public void setEdgeTimeGuardList(List<EdgeTimeGuard> edgeTimeGuardList){
        this.edgeTimeGuardList=edgeTimeGuardList;
    }
    public List<EdgeTimeGuard> getEdgeTimeGuardList(){
        return this.edgeTimeGuardList;
    }
    public void setClockTimeGuardMap(Map<Clock,TimeGuard> clockTimeGuardMap){
        this.clockTimeGuardMap=clockTimeGuardMap;
    }
//    public void setLargerclock(boolean largerclock){
//        this.largerclock=largerclock;
//    }
//    public void setDifferencexyupper(int differencexyupper){
//        this.differencexyupper=differencexyupper;
//    }
//    public void setDifferencexylower(int differencexylower){
//        this.differencexylower=differencexylower;
//    }
//    public void setIsequalupper(boolean isequalupper){
//        this.isequalupper=isequalupper;
//    }
//    public void setIsequallower(boolean isequallower){
//        this.isequallower=isequallower;
//    }
//    public void setHaveupperdiff(boolean haveupperdiff){
//        this.haveupperdiff=haveupperdiff;
//    }
//    public void setHavelowerdiff(boolean havelowerdiff){
//        this.havelowerdiff=havelowerdiff;
//    }
    public Map<Clock,TimeGuard> getClockTimeGuardMap(){
        return this.clockTimeGuardMap;
    }

//    public boolean getLargerclock(){
//        return this.largerclock;
//    }
//
//    public int getDifferencexyupper(){
//        return this.differencexyupper;
//    }
//    public int getDifferencexylower(){
//        return this.differencexylower;
//    }
//    public boolean isIsequalupper(){
//        return this.isequalupper;
//    }
//    public boolean isIsequallower(){
//        return this.isequallower;
//    }
//    public boolean isHaveupperdiff(){
//        return this.haveupperdiff;
//    }
//    public boolean isHavelowerdiff(){
//        return this.havelowerdiff;
//    }
    public List<DifferenceEdgeTimeGuard> getDifferenceEdgeTimeGuards(){
        return this.differenceEdgeTimeGuards;
    }
    public void setDifferenceEdgeTimeGuards(List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards){
        this.differenceEdgeTimeGuards=differenceEdgeTimeGuards;
    }
    public  EdgeTimeGuard(Map<Clock,TimeGuard> clockTimeGuardMap,List<DifferenceEdgeTimeGuard> differenceEdgeTimeGuards){
   this.clockTimeGuardMap=clockTimeGuardMap;
   this.differenceEdgeTimeGuards=differenceEdgeTimeGuards;
    }
    public EdgeTimeGuard(){

    }
    public EdgeTimeGuard copy(){
        return new EdgeTimeGuard(clockTimeGuardMap,differenceEdgeTimeGuards);
    }
}
