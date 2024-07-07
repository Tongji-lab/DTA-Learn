package ta;

import java.util.List;

public class ParralEdgeTimeGuard extends EdgeTimeGuard{
    List<EdgeTimeGuard> edgeTimeGuardList;
    public void setEdgeTimeGuardList(List<EdgeTimeGuard> edgeTimeGuardList){
        this.edgeTimeGuardList=edgeTimeGuardList;
    }
    public List<EdgeTimeGuard> getEdgeTimeGuardList(){
        return this.edgeTimeGuardList;
    }


}
