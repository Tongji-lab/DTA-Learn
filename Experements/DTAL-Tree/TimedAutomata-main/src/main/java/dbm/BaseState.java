package dbm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.TaLocation;

/*
时间自动机的状态，包括节点和DBM时间约束
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseState {
    private DBM dbm;

    public abstract BaseState getPreState();


//    @Override
//    public String toString(){
//        StringBuilder sb = new StringBuilder();
//        sb.append("\nlocation is :").append(location).append("\n");
//        sb.append("symbol is :").append(symbol).append("\n");
//        sb.append("guard is :");
//        List<Clock> clockList = dbm.getClockList();
//        Clock c1 = clockList.get(0);
//        Clock c2 = clockList.get(1);
//        sb.append("\n").append(c1.getName()).append(":");
//        if(dbm.getMatrix()[0][1].isEqual()){
//            sb.append("[");
//        }else {
//            sb.append("(");
//        }
//        sb.append(dbm.getMatrix()[0][1].getValue()*-1).append(",").append(dbm.getMatrix()[1][0].getValue());
//        if(dbm.getMatrix()[1][0].isEqual()){
//            sb.append("]");
//        }else {
//            sb.append(")");
//        }
//
//        sb.append("\n").append(c2.getName()).append(":");
//        if(dbm.getMatrix()[0][2].isEqual()){
//            sb.append("[");
//        }else {
//            sb.append("(");
//        }
//        sb.append(dbm.getMatrix()[0][2].getValue()*-1).append(",").append(dbm.getMatrix()[2][0].getValue());
//        if(dbm.getMatrix()[2][0].isEqual()){
//            sb.append("]");
//        }else {
//            sb.append(")");
//        }
//
//        sb.append("\n").append(c2.getName()).append("-").append(c1.getName()).append(":");
//        if(dbm.getMatrix()[1][2].isEqual()){
//            sb.append("[");
//        }else {
//            sb.append("(");
//        }
//        sb.append(dbm.getMatrix()[1][2].getValue()*-1).append(",").append(dbm.getMatrix()[2][1].getValue());
//        if(dbm.getMatrix()[2][1].isEqual()){
//            sb.append("]");
//        }else {
//            sb.append(")");
//        }
//
//        return sb.toString();
//    }

}
