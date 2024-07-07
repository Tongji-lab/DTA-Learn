package ta;

public class DifferenceEdgeTimeGuard {
     Clock clock1;
     Clock clock2;
    private boolean largerclock=false;//no difference is 0,otherwise is 1;

    private boolean haveupperdiff=false;
    private int differencexyupper;
    private boolean havelowerdiff=false;
    private int differencexylower;

    private boolean isequalupper;

    private boolean isequallower;
    public void setClock1(Clock clock1){
        this.clock1=clock1;
    }
    public void setClock2(Clock clock2){
        this.clock2=clock2;
    }
    public void setLargerclock(boolean largerclock){
        this.largerclock=largerclock;
    }
    public void setDifferencexyupper(int differencexyupper){
        this.differencexyupper=differencexyupper;
    }
    public void setDifferencexylower(int differencexylower){
        this.differencexylower=differencexylower;
    }
    public void setIsequalupper(boolean isequalupper){
        this.isequalupper=isequalupper;
    }
    public void setIsequallower(boolean isequallower){
        this.isequallower=isequallower;
    }
    public void setHaveupperdiff(boolean haveupperdiff){
        this.haveupperdiff=haveupperdiff;
    }
    public void setHavelowerdiff(boolean havelowerdiff){
        this.havelowerdiff=havelowerdiff;
    }
    public boolean getLargerclock(){
        return this.largerclock;
    }

    public int getDifferencexyupper(){
        return this.differencexyupper;
    }
    public int getDifferencexylower(){
        return this.differencexylower;
    }
    public boolean isIsequalupper(){
        return this.isequalupper;
    }
    public boolean isIsequallower(){
        return this.isequallower;
    }
    public boolean isHaveupperdiff(){
        return this.haveupperdiff;
    }
    public boolean isHavelowerdiff(){
        return this.havelowerdiff;
    }
    public Clock getClock1(){
        return this.clock1;
    }
    public Clock getClock2(){
        return this.clock2;
    }
}
