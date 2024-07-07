package dbm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Value implements Comparable<Value>{
    private int value;
    private boolean equal;

    public static Value add(Value v2, Value v3){
        int value = v2.getValue()+v3.getValue();
        boolean equal = v2.isEqual() && v3.isEqual();
        Value v1 = new Value(value,equal);
        return v1;
    }

    @Override
    public int compareTo(Value o) {
        if(this.getValue() < o.getValue()){
            return -1;
        }
        if(this.getValue() == o.getValue()){
            if(this.equal == false && o.equal == true){
                return -1;
            }
            if(this.equal == false && o.equal == false){
                return 0;
            }
            if(this.equal == true && o.equal == true){
                return 0;
            }
        }
        return 1;
    }
}
