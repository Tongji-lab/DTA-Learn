package Experiment;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LearnResult {
    private String name;
    private double costTime1;
    private double membershipCount1;
    private double equivalenceCount1;
    private double costTime2;
    private double membershipCount2;
    private double equivalenceCount2;
    @JSONField(serialize=false)
    private List<ItemResultPair> itemResultList = new ArrayList<>();

    public void addItemResult(ItemResult itemResult1, ItemResult itemResult2)
    {
        ItemResultPair pair = new ItemResultPair(itemResult1,itemResult2);
        itemResultList.add(pair);
    }

    public LearnResult() {

    }

    public double getCostTime1(){
        sort();
        for(int i = 5; i< 10; i++){
            costTime1 +=itemResultList.get(i)
                    .getItemResult1()
                    .getCostTime();
        }
        return costTime1 *1.0/5;
    }

    public double getMembershipCount1(){
        sort();
        for(int i = 5; i< 10; i++){
            membershipCount1 +=itemResultList.get(i)
                    .getItemResult1()
                    .getMembershipCount();
        }
        return membershipCount1 *1.0/5;
    }

    public double getEquivalenceCount1(){
        sort();
        for(int i = 5; i< 10; i++){
            equivalenceCount1 +=itemResultList.get(i)
                    .getItemResult1()
                    .getEquivalenceCount();
        }
        return equivalenceCount1 *1.0/5;
    }

    public double getCostTime2(){
        sort();
        for(int i = 5; i< 10; i++){
            costTime2 +=itemResultList.get(i)
                    .getItemResult2()
                    .getCostTime();
        }
        return costTime2 *1.0/5;
    }

    public double getMembershipCount2(){
        sort();
        for(int i = 5; i< 10; i++){
            membershipCount2 +=itemResultList.get(i)
                    .getItemResult2()
                    .getMembershipCount();
        }
        return membershipCount2 *1.0/5;
    }

    public double getEquivalenceCount2(){
        sort();
        for(int i = 5; i< 10; i++){
            equivalenceCount2 +=itemResultList.get(i)
                    .getItemResult2()
                    .getEquivalenceCount();
        }
        return equivalenceCount2 *1.0/5;
    }

    public void sort(){
        itemResultList.sort((c1,c2)->{
            return (int)(c1.getItemResult1().getCostTime())-(int)(c2.getItemResult1().getCostTime());
        });
    }
    @Data
    @AllArgsConstructor
    private class ItemResultPair {
        private ItemResult itemResult1;
        private ItemResult itemResult2;
    }
}
