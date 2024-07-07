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


    private static List<TimeGuard> obtainGuardList(List<TaTransition> transitionList, Clock clock) {
        List<TimeGuard> timeGuardList = new ArrayList<>();
        for (TaTransition transition : transitionList) {
            timeGuardList.add(transition.getTimeGuard(clock));
        }
        return timeGuardList;
    }

//    public static DOTA getCartesian(DOTA ota, DFA dfa) {
//
//        Clock clock = ota.getClock();
//        Map<Pair, TaLocation> pairLocationMap = new HashMap<>();
//        //构造节点的笛卡儿积
//
//        for (TaLocation l1 : ota.getLocations()) {
//            for (DfaLocation l2 : dfa.getLocations()) {
//                String id = l1.getId() + "-" + l2.getId();
//                String name = l1.getName() + "_" + l2.getName();
//                boolean accept = l1.isAccept() && l2.isAccept();
//                boolean init = l1.isInit() && l2.isInit();
//                TaLocation taLocation = new TaLocation(id, name, accept, init);
//                Pair pair = new Pair(l1, l2);
//                pairLocationMap.put(pair, taLocation);
//            }
//        }
//        List<TaLocation> newLocations = new ArrayList<>(pairLocationMap.values());
//
//        //sigma求并集
//        Set<String> sigma = new HashSet<>();
//        sigma.addAll(ota.getSigma());
//        sigma.addAll(dfa.getSigma());
//
//        //构造迁移的笛卡尔积
//        //遍历sigma，分三种情况求迁移
//        List<TaTransition> newTransitions = new ArrayList<>();
//        sigma.stream().forEach(e -> {
//            //第一种情况，两边都含有相同的动作,需要对其进行同步操作
//            if (ota.containsSymbol(e) && dfa.containsSymbol(e)) {
//                for (TaTransition t1 : ota.getTransitions(null, e, null)) {
//                    for (DfaTransition t2 : dfa.getTransitions(e)) {
//                        Pair sourcePair = new Pair(t1.getSourceLocation(), t2.getSourceLocation());
//                        Pair targetPair = new Pair(t1.getTargetLocation(), t2.getTargetLocation());
//                        TaLocation sourceLocation = pairLocationMap.get(sourcePair);
//                        TaLocation targetLocation = pairLocationMap.get(targetPair);
//                        Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//                        clockTimeGuardMap.put(clock, t1.getTimeGuard(clock));
//                        Set<Clock> clockSet = t1.getResetClockSet();
//                        TaTransition newTransition = new TaTransition(
//                                sourceLocation, targetLocation, t1.getSymbol(), clockTimeGuardMap, clockSet);
//                        newTransitions.add(newTransition);
//                    }
//                }
//            }
//            //第二种情况，只有ota存在的动作
//            if (ota.containsSymbol(e) && !dfa.containsSymbol(e)) {
//                asyncTransitions(clock, ota.getTransitions(), dfa.getLocations(), pairLocationMap, newTransitions);
//            }
//            //第三种情况，只有dfa2存在的动作
//            if (!ota.containsSymbol(e) && dfa.containsSymbol(e)) {
//                asyncDfaTransitions(clock, dfa.getTransitions(), ota.getLocations(), pairLocationMap, newTransitions);
//            }
//        });
//
//        //构造笛卡尔积自动机DFA
//        String name = ota.getName() + "_" + dfa.getName();
//        DOTA newOTA = new DOTA(name, sigma, newLocations, newTransitions, clock);
//        return newOTA;
//    }


//    private static void asyncDfaTransitions(
//            Clock clock,
//            List<DfaTransition> transitions,
//            List<TaLocation> locationList,
//            Map<Pair, TaLocation> pairLocationMap,
//            List<TaTransition> newTransitions) {
//
//        for (DfaTransition t : transitions) {
//            for (TaLocation l : locationList) {
//                Pair sourcePair = new Pair(l, t.getSourceLocation());
//                Pair targetPair = new Pair(l, t.getTargetLocation());
//                TaLocation sourceLocation = pairLocationMap.get(sourcePair);
//                TaLocation targetLocation = pairLocationMap.get(targetPair);
//
//                Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//                clockTimeGuardMap.put(clock, new TimeGuard("[0,+)"));
//                Set<Clock> clockSet = new HashSet<>();
//                TaTransition newTransition = new TaTransition(
//                        sourceLocation, targetLocation, t.getSymbol(), clockTimeGuardMap, clockSet);
//                newTransitions.add(newTransition);
//            }
//        }
//    }

//    private static void asyncTransitions(Clock clock,
//                                         List<TaTransition> transitions,
//                                         List<DfaLocation> locations,
//                                         Map<Pair, TaLocation> pairLocationMap,
//                                         List<TaTransition> newTransitions
//    ) {
//        for (TaTransition t : transitions) {
//            for (DfaLocation l : locations) {
//                Pair sourcePair = new Pair(t.getSourceLocation(), l);
//                Pair targetPair = new Pair(t.getTargetLocation(), l);
//                TaLocation sourceLocation = pairLocationMap.get(sourcePair);
//                TaLocation targetLocation = pairLocationMap.get(targetPair);
//                Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//                clockTimeGuardMap.put(clock, t.getTimeGuard(clock));
//                Set<Clock> clockSet = t.getResetClockSet();
//                TaTransition newTransition = new TaTransition(
//                        sourceLocation, targetLocation, t.getSymbol(), clockTimeGuardMap, clockSet);
//                newTransitions.add(newTransition);
//            }
//        }
//    }
//    private static void asyncTransitionsinUPPAAL(
//                                         List<TaTransition> transitions,
//                                         List<TaLocation> locations,
//                                         Map<PairinCombine, TaLocation> pairLocationMap,
//                                         List<TaTransition> newTransitions
//    ) {
//        for (TaTransition t : transitions) {
//            for (TaLocation l : locations) {
//                PairinCombine sourcePair = new PairinCombine();
//                sourcePair.location1=t.getSourceLocation();
//                sourcePair.location2=l;
//                PairinCombine targetPair = new PairinCombine();
//                targetPair.location1=t.getTargetLocation();
//                targetPair.location2=l;
//                TaLocation sourceLocation = pairLocationMap.get(sourcePair);
//                TaLocation targetLocation = pairLocationMap.get(targetPair);
//                Map<Clock, TimeGuard> clockTimeGuardMap = new HashMap<>();
//                clockTimeGuardMap=t.getClockTimeGuardMap();
//                Set<Clock> clockSet = t.getResetClockSet();
//                TaTransition newTransition = new TaTransition(
//                        sourceLocation, targetLocation, t.getSymbol(), clockTimeGuardMap, clockSet);
//                newTransitions.add(newTransition);
//            }
//        }
//    }

    @Data
    @AllArgsConstructor
    private static class Pair {
        private TaLocation location;
        private DfaLocation dfaLocation;
    }

}
