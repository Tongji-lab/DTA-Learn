package dfa;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class DFAUtil {

    public static DFA getDFAFromJsonFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String str = null;
        StringBuilder json = new StringBuilder();
        while ((str = reader.readLine()) != null) {
            json.append(str);
        }
        DFA dfa = getDFAFromJson(json.toString());
        return dfa;
    }

    private static DFA getDFAFromJson(String json) {
        //定义一个单时钟
        JSONObject jsonObject = JSON.parseObject(json);
        //获取name
        String name = jsonObject.getString("name");
        //获取sigma
        JSONArray jsonArray = jsonObject.getJSONArray("sigma");
        Set<String> sigma = new HashSet<>();
        jsonArray.stream().forEach(e -> {
            sigma.add((String) e);
        });

        //获取location
        Map<String, DfaLocation> idLocationMap = new HashMap<>();
        JSONArray locationArray = jsonObject.getJSONArray("l");
        String initId = jsonObject.getString("init");
        JSONArray acceptArray = jsonObject.getJSONArray("accept");
        Set<String> acceptSet = new HashSet<>();
        acceptArray.stream().forEach(e -> {
            acceptSet.add((String) e);
        });
        locationArray.stream().forEach(e -> {
            String id = (String) e;
            boolean isInit = StringUtils.equals(id, initId);
            boolean isAccept = acceptSet.contains(id);
            DfaLocation location = new DfaLocation(id, id, isInit, isAccept);
            idLocationMap.put(id, location);
        });
        List<DfaLocation> locations = new ArrayList<>(idLocationMap.values());


        //获取迁移
        JSONObject tranJsonObject = jsonObject.getJSONObject("tran");
        int size = tranJsonObject.size();
        List<DfaTransition> transitions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            JSONArray array = tranJsonObject.getJSONArray(String.valueOf(i));
            String sourceId = array.getString(0);
            String symbol = array.getString(1);
            String targetId = array.getString(2);
            DfaLocation sourceLocation = idLocationMap.get(sourceId);
            DfaLocation targetLocation = idLocationMap.get(targetId);
            DfaTransition transition = new DfaTransition(sourceLocation, targetLocation, symbol);
            transitions.add(transition);
        }
        transitions.sort(new DFATranComparator());
        DFA dfa = new DFA(name, sigma, locations, transitions);
        return dfa;
    }

    //求两个DFA的组合
    public static DFA parallelComposition(DFA dfa1, DFA dfa2) {
        //存放新旧节点对应关系的map
        Map<Pair, DfaLocation> pairLocationMap = new HashMap<>();

        //构造节点的笛卡尔积
        List<DfaLocation> newLocations = new ArrayList<>();
        for (DfaLocation l1 : dfa1.getLocations()) {
            for (DfaLocation l2 : dfa2.getLocations()) {
                DfaLocation newLocation = new DfaLocation.DfaLocationBuilder()
                        .id(l1.getId() + "_" + l2.getId())
                        .name(l1.getName() + "_" + l2.getName())
                        .accept(l1.isAccept() && l2.isAccept())
                        .init(l1.isInit() && l2.isInit())
                        .build();
                Pair pair = new Pair(l1, l2);
                pairLocationMap.put(pair, newLocation);
                newLocations.add(newLocation);
            }
        }

        //sigma求并集
        Set<String> sigma = new HashSet<>();
        sigma.addAll(dfa1.getSigma());
        sigma.addAll(dfa2.getSigma());

        //构造迁移的笛卡尔积
        //遍历sigma，分三种情况求迁移
        List<DfaTransition> newTransitions = new ArrayList<>();
        sigma.stream().forEach(e -> {
            //第一种情况，两边都含有相同的动作,需要对其进行同步操作
            if (dfa1.containsSymbol(e) && dfa2.containsSymbol(e)) {
                for (DfaTransition t1 : dfa1.getTransitions(e)) {
                    for (DfaTransition t2 : dfa2.getTransitions(e)) {
                        Pair sourcePair = new Pair(t1.getSourceLocation(), t2.getSourceLocation());
                        Pair targetPair = new Pair(t1.getTargetLocation(), t2.getTargetLocation());
                        DfaLocation sourceLocation = pairLocationMap.get(sourcePair);
                        DfaLocation targetLocation = pairLocationMap.get(targetPair);
                        DfaTransition newTransition = new DfaTransition.DfaTransitionBuilder()
                                .sourceLocation(sourceLocation)
                                .targetLocation(targetLocation)
                                .symbol(t1.getSymbol())
                                .build();
                        newTransitions.add(newTransition);
                    }
                }
            }
            //第二种情况，只有dfa1存在的动作
            if (dfa1.containsSymbol(e) && !dfa2.containsSymbol(e)) {
                asyncTransitions(dfa1.getTransitions(), dfa2.getLocations(), pairLocationMap, newTransitions, true);
            }
            //第三种情况，只有dfa2存在的动作
            if (!dfa1.containsSymbol(e) && dfa2.containsSymbol(e)) {
                asyncTransitions(dfa2.getTransitions(), dfa1.getLocations(), pairLocationMap, newTransitions, false);
            }
        });

        newTransitions.sort(new DFATranComparator());

        //构造笛卡尔积自动机DFA
        DFA newDFA = new DFA.DFABuilder()
                .name(dfa1.getName() + "_" + dfa2.getName())
                .locations(newLocations)
                .transitions(newTransitions)
                .sigma(sigma)
                .build();
        return newDFA;
    }

    private static void asyncTransitions(List<DfaTransition> transitions,
                                         List<DfaLocation> locations,
                                         Map<Pair, DfaLocation> pairLocationMap,
                                         List<DfaTransition> newTransitions,
                                         boolean flag
    ) {
        for (DfaTransition t : transitions) {
            for (DfaLocation l : locations) {
                Pair sourcePair;
                Pair targetPair;
                if (flag) {
                    sourcePair = new Pair(t.getSourceLocation(), l);
                    targetPair = new Pair(t.getTargetLocation(), l);
                } else {
                    sourcePair = new Pair(l, t.getSourceLocation());
                    targetPair = new Pair(l, t.getTargetLocation());
                }
                DfaLocation sourceLocation = pairLocationMap.get(sourcePair);
                DfaLocation targetLocation = pairLocationMap.get(targetPair);
                DfaTransition newTransition = new DfaTransition.DfaTransitionBuilder()
                        .sourceLocation(sourceLocation)
                        .targetLocation(targetLocation)
                        .symbol(t.getSymbol())
                        .build();
                newTransitions.add(newTransition);
            }
        }
    }


    @Data
    @AllArgsConstructor
    private static class Pair {
        private DfaLocation location1;
        private DfaLocation location2;
    }

}
