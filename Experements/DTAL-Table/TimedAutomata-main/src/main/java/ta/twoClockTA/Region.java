package ta.twoClockTA;

import ta.TimeGuard;

import java.util.ArrayList;
import java.util.List;

public class Region {
//   public double lowerx;
//    public boolean lowerboundopenx;
//    public double upperx;
//    public boolean upperboundopenx;
//    public double lowery;
//    public boolean lowerboundopeny;
//    public double uppery;
//    public boolean upperboundopeny;
//    public int differxy;

    List<TimeGuard> timeGuardList=new ArrayList<>();
    int[] differList;//按照x-y，x-z，y-z的顺序
//    public Region(double lowerx,boolean lowerboundopenx,double upperx, boolean upperboundopenx,double lowery, boolean lowerboundopeny,double uppery,boolean upperboundopeny,int differxy){
//       // super(lowerx,lowerboundopenx,upperx,upperboundopenx,lowery,lowerboundopeny,uppery,upperboundopeny,differxy);
//        this.lowerx=lowerx;
//        this.lowerboundopenx=lowerboundopenx;
//        this.upperx=upperx;
//        this.upperboundopenx=upperboundopenx;
//        this.lowery=lowery;
//        this.lowerboundopeny=lowerboundopeny;
//        this.uppery=uppery;
//        this.upperboundopeny=upperboundopeny;
//        this.differxy=differxy;
//
//    }
    public Region(List<TimeGuard> timeGuardList, int[] differList){
        this.timeGuardList=timeGuardList;
        this.differList=differList;
    }
    public void setTimeGuardList(List<TimeGuard> timeGuardList){
        this.timeGuardList=timeGuardList;
    }
    public void setDifferList(int[] differList){
        this.differList=differList;
    }
    public  List<TimeGuard> getTimeGuardList(){
        return  timeGuardList;
    }
    public  int[] getDifferList(){
        return differList;
    }
//    double getLowerx(){
//        return lowerx;
//    }
//    double getUpperx(){ return upperx; }
//    double getLowery(){
//        return lowery;
//    }
//    double getUppery(){
//        return uppery;
//    }
//    boolean getLowerboundopenx(){
//        return lowerboundopenx;
//    }
//    boolean getUpperboundopenx(){
//        return upperboundopenx;
//    }
//    boolean getLowerboundopeny(){
//        return lowerboundopeny;
//    }
//    boolean getUpperboundopeny(){
//        return upperboundopeny;
//    }
//    int getDifferxy(){
//        return differxy;
//    }
//    void setLowerx(double a){ lowerx=a; }
//    void setLowery(double a){
//        lowery=a;
//    }
//    void setUpperx(double a){ upperx=a; }
//    void setUppery(double a){
//        uppery=a;
//    }
//    void setLowerboundopenx(boolean a){
//        lowerboundopenx=a;
//    }
//    void setUpperboundopenx(boolean a){
//        upperboundopenx=a;
//    }
//    void setLowerboundopeny(boolean a){
//        lowerboundopeny=a;
//    }
//    void setUpperboundopeny(boolean a){
//        upperboundopeny=a;
//    }
//    void setdifferyx(int a){
//        differxy=a;
//    }
//    public boolean isPassX(Double value){
//        if(lowerboundopenx&&upperboundopenx){
//            if(value>lowerx&&value<upperx){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        else if(!lowerboundopenx&&upperboundopenx){
//            if(value>=lowerx&&value<upperx){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        else if(lowerboundopenx&&!upperboundopenx){
//            if(value>lowerx&&value<=upperx){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        else{
//            if(value>=lowerx&&value<=upperx){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//
//    }
//    public boolean isPassY(Double value){
//        if(lowerboundopeny&&upperboundopeny){
//            if(value>lowery&&value<uppery){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        else if(!lowerboundopeny&&upperboundopeny){
//            if(value>=lowery&&value<uppery){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        else if(lowerboundopeny&&!upperboundopeny){
//            if(value>lowery&&value<=uppery){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//        else{
//            if(value>=lowery&&value<=uppery){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }
//
//    }
}

