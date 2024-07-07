package dbm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ta.Clock;
import ta.TimeGuard;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBM {

    private List<Clock> clockList;
    private Value[][] matrix;
    public static DBM init(List<Clock> clockList){
        //n为时钟数量加一，需要一个零时钟
        int n = clockList.size()+1;
        //    System.out.println("n:"+n);
        //初始化DBM数组，每一个时钟的范围都是[0,+)
        Value[][] matrix = new Value[n][n];
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                matrix[i][j] = new Value(0,true);
            }
        }
        for(int i = 1; i < n; i++){
            matrix[i][0] = new Value(Integer.MAX_VALUE,false);
        }
        DBM dbm=new DBM(clockList,matrix);
        dbm.setClockList(clockList);
        dbm.setMatrix(matrix);
        return dbm;
    }


    //Floyds 算法,求最小环
    public void canonical(){
        //int[][][] shortest=new int[size()-1][size()-1][size()-1];
        for(int k = 0; k < size(); k++){
            // System.out.println("k"+k);
            for(int i = 0; i < size(); i++){
                for(int j = 0; j < size(); j++){
                    // shortest[i][j][k]=-1;
                    Value v1 = matrix[i][j];
                    Value v = Value.add(matrix[i][k],matrix[k][j]);
                    Value max=new Value(TimeGuard.MAX_TIME,true);
                    Value min=new Value(-1*TimeGuard.MAX_TIME,true);
//                    if(v.compareTo(max)>=0){
////                        v.setValue(Integer.MAX_VALUE);
////                        v.canEqual(false);
//                    }
//                   else if(v.compareTo(min)<=0){
//                        v.setValue(-1*Integer.MAX_VALUE);
//                        v.canEqual(false);
//                    }
//                    System.out.println("matrix[i][j]"+matrix[i][j]);
//                    System.out.println("matrix[i][k]"+matrix[i][k]);
//                    System.out.println("matrix[k][j]"+matrix[k][j]);
                    //    if(i<1){
                    // System.out.println("i"+i+"j"+j+"k"+k+"v1"+v1+"v"+v);}
                    if(matrix[i][j].compareTo(v) > 0){
                        // System.out.println("i:"+i+"j:"+j+"k:"+k+"matrix[i][j]:"+v1+"sum:"+v);
                        matrix[i][j] = v;
                        if(v.compareTo(min)<=0){
//                           v.setValue(-1*Integer.MAX_VALUE);
                            v.setValue(-1*TimeGuard.MAX_TIME);
                            v.canEqual(false);
                        }
                    }
                }
            }
        }
    }

    //up操作，取消DBM的上限约束
    public void up(){
        for(int i = 1; i <size();i++ ){
            matrix[i][0] = new Value(Integer.MAX_VALUE,false);
        }
    }

    //and操作
    public void and(Clock c, TimeGuard timeGuard, int index){
//        int index = clockList.indexOf(c);
//        System.out.println("c:"+c.toString());
//        System.out.println("c的index:"+index);
        Value upperBound = new Value(timeGuard.getUpperBound(),!timeGuard.isUpperBoundOpen());
        if(upperBound.getValue()==TimeGuard.MAX_TIME){
//            upperBound.setValue(Integer.MAX_VALUE);
        }
        if(upperBound.compareTo(matrix[index+1][0]) < 0){
            matrix[index+1][0] = upperBound;
        }
        Value lowerBound = new Value(timeGuard.getLowerBound()*(-1),!timeGuard.isLowerBoundOpen());

        if(lowerBound.getValue()==TimeGuard.MAX_TIME){
//            lowerBound.setValue(Integer.MAX_VALUE);
        }
        if(lowerBound.compareTo(matrix[0][index+1]) < 0){
            matrix[0][index+1] = lowerBound;
        }
    }
    public void and1(int biggerorsmall, int deffirencexy, boolean isequal,Clock clock1,Clock clock2, int index1,int index2){
//        int index1 = clockList.indexOf(clock1);
//        int index2 = clockList.indexOf(clock2);
        if(biggerorsmall==1){
            Value upperBound = new Value(deffirencexy,isequal);
            if(upperBound.compareTo(matrix[index1+1][index2+1]) < 0){
                matrix[index1+1][index2+1] = upperBound;
            }
        }
        else {
            Value lowerBound = new Value(deffirencexy*(-1),isequal);
            //      Value lowerBound = new Value(deffirencexy,isequal);
            if(lowerBound.compareTo(matrix[index2+1][index1+1]) < 0){
//            if(lowerBound.compareTo(matrix[2][1]) > 0){
                matrix[index2+1][index1+1] = lowerBound;
            }
        }
    }
    public void and2(int biggerorsmall, int deffirencexy, boolean isequal){
        if(biggerorsmall==1){
            Value upperBound = new Value(deffirencexy,isequal);
            if(upperBound.compareTo(matrix[3][4]) < 0){
                matrix[3][4] = upperBound;
            }
        }
        else {
            Value lowerBound = new Value(deffirencexy*(-1),isequal);
            if(lowerBound.compareTo(matrix[4][3]) < 0){
                matrix[4][3] = lowerBound;
            }
        }
    }

    //reset操作，对某一个时钟的值进行重置
    public void reset(Clock c){
        // System.out.println("传入的clock的name："+c.getName());
        int ind=Integer.valueOf(c.getName().substring(1));
//        int index = clockList.indexOf(c)+1;
        int index = ind+1;
        //    System.out.println("index："+index);
        for(int i = 0; i < size(); i++){
            matrix[index][i] = matrix[0][i];
            matrix[i][index] = matrix[i][0];
        }
    }

    public int size(){
        //  System.out.println("size()中的clockList.size():"+clockList.size());
        return clockList.size()+1;
    }

    //必须是canonical的才能判断
    public Boolean isConsistent(){
        for(int i = 0; i < size(); i++){
            if(matrix[i][i].compareTo(new Value(0,true)) < 0){
                return false;
            }
        }
        return true;
    }

    //DBM的包含判断，判断是否包含另外一个DBM的约束
    public boolean include(DBM dbm){
        for(int i = 0; i < size(); i++){
            for(int j = 0; j < size(); j++){
                if(matrix[i][j].compareTo(dbm.matrix[i][j]) < 0){
                    return false;
                }
            }
        }
        return true;
    }

    //做一个DBM的拷贝，避免污染数据
    public DBM copy(){
        Value[][] matrix1 = new Value[size()][size()];
        for(int i = 0; i < size(); i++){
            for(int j = 0; j < size(); j++){
                matrix1[i][j] = new Value(matrix[i][j].getValue(),matrix[i][j].isEqual());
            }
        }
        return new DBM(clockList,matrix1);
    }

    /*
    暂定两个时钟的DBM的输出
     */
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("the dbm matrix is:\n");
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix.length; j++){
                if(matrix[i][j].getValue() == Integer.MAX_VALUE){
                    sb.append("∞").append("<").append(" \t");
                }else {
                    sb.append(matrix[i][j].getValue());
                    if(matrix[i][j].isEqual()){
                        sb.append(" <=");
                    }else {
                        sb.append(" <");
                    }
                    sb.append(" \t");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

