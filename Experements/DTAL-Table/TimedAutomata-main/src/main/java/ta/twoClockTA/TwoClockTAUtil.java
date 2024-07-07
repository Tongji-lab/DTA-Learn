package ta.twoClockTA;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import dfa.DFA;
import dfa.DfaLocation;
import dfa.DfaTransition;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import ta.*;
import ta.ota.DOTA;
import ta.ota.OTATranComparator;

import java.io.*;
import java.util.*;

public class TwoClockTAUtil {

//    public static void main(String[] args){
//        DOTA dota = createRandomDOTA(4,2,4,1);
//        writeDOTA2Json(dota,null);
//    }

    public static void writeDOTA2Json(TwoClockTA dtta, String path) throws IOException {
        Clock clock1 = dtta.getClockList().get(0);
        Clock clock2 = dtta.getClockList().get(1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", dtta.getName());
        JSONArray sigmaArray = new JSONArray();
        sigmaArray.addAll(dtta.getSigma());
        jsonObject.put("sigma", sigmaArray);
        JSONArray lArray = new JSONArray();
        for (TaLocation location: dtta.getLocations()){
            lArray.add(location.getName());
        }
        jsonObject.put("l",lArray);
        JSONArray acceptArray = new JSONArray();
        for (TaLocation location: dtta.getAcceptedLocations()){
            acceptArray.add(location.getName());
        }
        jsonObject.put("accept",acceptArray);
        jsonObject.put("init",dtta.getInitLocation().getName());
        JSONObject transObject = new JSONObject();
        dtta.getTransitions().sort(new OTATranComparator(clock1));
        dtta.getTransitions().sort(new OTATranComparator(clock2));
        List<TaTransition> transitions = dtta.getTransitions();
        for (int i = 0; i < transitions.size(); i++){
            TaTransition taTransition = transitions.get(i);
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(taTransition.getSourceLocation().getName());
            jsonArray.add(taTransition.getSymbol());
            jsonArray.add(taTransition.getTimeGuard(clock1).toString());
            jsonArray.add(taTransition.getTimeGuard(clock2).toString());
            jsonArray.add(taTransition.getResetClockSet().contains(clock1)?"r":"n");
            jsonArray.add(taTransition.getResetClockSet().contains(clock2)?"r":"n");
            jsonArray.add(taTransition.getTargetLocation().getName());
            transObject.put(String.valueOf(i), jsonArray);
        }
        jsonObject.put("tran",transObject);
//        System.out.println(jsonObject.toString(SerializerFeature.PrettyFormat));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
        bw.write(jsonObject.toString(SerializerFeature.PrettyFormat));
        bw.flush();;
        bw.close();
    }


    @Data
    @AllArgsConstructor
    private static class Pair {
        private TaLocation location;
        private DfaLocation dfaLocation;
    }

}
