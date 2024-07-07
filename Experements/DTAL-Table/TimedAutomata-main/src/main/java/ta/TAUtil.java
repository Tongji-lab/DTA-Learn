package ta;

import dbm.BaseState;
import dbm.DBM;
import dbm.LocationState;
import dbm.TransitionState;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public final class TAUtil {

    //时间自动机的可达性算法，判断当前自动机是否存在可达的接收状态
    public static Map<List<TransitionState>,List<Set<Clock>>> reachable(TA ta) {
        List<Set<Clock>> resets=new ArrayList<>();
        List<Clock> clockList = new ArrayList<>(ta.getClockList());
        //初始化DBM
        DBM initDbm = DBM.init(clockList);
        //获取初始节点
        TaLocation initLocation = ta.getInitLocation();
        //创建已访问的状态空间
        Set<LocationState> visited = new HashSet<>();
        //创建待访问的状态队列
        LinkedList<LocationState> waitQueue = new LinkedList<>();
        //初始状态
        LocationState initLocationState = new LocationState(initDbm, initLocation);
        //将初始状态加入到待访问队列中,这里的waitQueue中的都是以访问状态，他们的后续节点状态是未访问的
        waitQueue.offer(initLocationState);
        //遍历待访问队列，这里采用了BSF的方式
        while (!waitQueue.isEmpty()) {
            //取出一个状态
            LocationState current = waitQueue.poll();
            //       System.out.println("current:"+current);
            //将状态加入到已访问集合中
            visited.add(current);
            //获取当前状态的节点
            TaLocation location = current.getLocation();
            //判断，当前节点是否是不接收状态
            //如果是接收状态,返回状态路径
            if (location.isAccept()) {
                List<TransitionState> transitionStates = new LinkedList<>();
                BaseState state = current;
                while (null != state){
                    if (state instanceof TransitionState){
                        transitionStates.add(0, (TransitionState) state);
                    }
                    state = state.getPreState();
                }
                //System.out.println("返回states");
                Map<List<TransitionState>,List<Set<Clock>>> map=new HashMap<>();
                map.put(transitionStates,resets);
                return map;

            }
            //否则，获取当前节点的后续迁移
            List<TaTransition> taTransitions = ta.getTransitions(location, null, null);
            //   System.out.println("location:"+location.getId());
            //对迁移进行遍历
            Boolean s=new Boolean(false);
            flag:
            for (TaTransition t : taTransitions) {
                //      System.out.println("该location下的迁移:"+t.toString()+t.getEdgeTimeGuard().edgeTimeGuardList.get(0).clockTimeGuardMap+t.getEdgeTimeGuard().edgeTimeGuardList.get(1).clockTimeGuardMap+t.getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(2))+t.getClockTimeGuardMap().getClockTimeGuardMap().get(clockList.get(3)));
                //获取该迁移的时钟约束
                EdgeTimeGuard edgeTimeGuard=t.getEdgeTimeGuard();
                //Map<Clock, TimeGuard> timeGuardClockMap = t.getClockTimeGuardMap();
                //对当前DBM进行拷贝
                DBM dbm = current.getDbm().copy();
                //求约束后的DBM
                for (Map.Entry<Clock, TimeGuard> entry : edgeTimeGuard.edgeTimeGuardList.get(0).clockTimeGuardMap.entrySet()) {
                    //System.out.println("and的entry:"+entry);
                    dbm.and(entry.getKey(), entry.getValue(),clockList.indexOf(entry.getKey()));
                }
                for (Map.Entry<Clock, TimeGuard> entry : edgeTimeGuard.edgeTimeGuardList.get(1).clockTimeGuardMap.entrySet()) {
                    dbm.and(entry.getKey(), entry.getValue(),clockList.indexOf(entry.getKey())+edgeTimeGuard.edgeTimeGuardList.get(0).clockTimeGuardMap.size());

                }
                for (int i=0;i<edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().size();i++){
                    if(edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).isHaveupperdiff()){
                        dbm.and1(1,edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getDifferencexyupper(), edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).isIsequalupper(),edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock1(),edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock2(),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock1()),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock2()));//1是小于，上界
                    }
                    if(edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).isHavelowerdiff()){
                        dbm.and1(2,edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getDifferencexylower(), edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).isIsequallower(),edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock1(),edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock2(),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock1()),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(0).getDifferenceEdgeTimeGuards().get(i).getClock2()));//2是大于，下界
                    }

                }

                for (int i=0;i<edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().size();i++){
                    if(edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).isHaveupperdiff()){
                        dbm.and1(1,edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getDifferencexyupper(), edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).isIsequalupper(),edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock1(),edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock2(),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock1())+edgeTimeGuard.edgeTimeGuardList.get(0).clockTimeGuardMap.size(),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock2())+edgeTimeGuard.edgeTimeGuardList.get(0).clockTimeGuardMap.size());//1是小于，上界
                    }
                    if(edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).isHavelowerdiff()){
                        dbm.and1(2,edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getDifferencexylower(), edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).isIsequallower(),edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock1(),edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock2(),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock1())+edgeTimeGuard.edgeTimeGuardList.get(0).clockTimeGuardMap.size(),clockList.indexOf(edgeTimeGuard.edgeTimeGuardList.get(1).getDifferenceEdgeTimeGuards().get(i).getClock2())+edgeTimeGuard.edgeTimeGuardList.get(0).clockTimeGuardMap.size());//2是大于，下界
                    }

                }
                //最小化
                dbm.canonical();
//                System.out.println("该location canonical之后的dbm:"+dbm.toString());
                //判断与约束是否合理
                if (dbm.isConsistent()) {
                    //  System.out.println("约束合理");
                    //迁移状态
                    TransitionState transitionState = new TransitionState(dbm, t.getSymbol(), current);
                    dbm = dbm.copy();
                    //      System.out.println("t.getResetClockSet():"+t.getResetClockSet());
                    for (Clock c : t.getResetClockSet()) {
                        dbm.reset(c);
                    }
                    dbm.canonical();
                    dbm.up();

                    LocationState newLocationState = new LocationState(dbm, t.getTargetLocation());
                    resets.add(t.getResetClockSet());
                    newLocationState.setPreState(transitionState);
                    for (LocationState n : visited) {
                        if (n.include(newLocationState)) {
                            continue flag;
                        }
                    }
                    visited.add(newLocationState);
                    //    System.out.println("加入到waitQueue的newLocationState:"+newLocationState.toString());
                    waitQueue.offer(newLocationState);
                    //    System.out.println("waitQueue:"+waitQueue);
                }
            }
        }
        return null;
    }

    //时间自动机的平行组合
    public static TA parallelCombination(TA ta1, TA ta2) {

        //存放新旧节点对应关系的map
        Map<Pair, TaLocation> pairLocationMap = new HashMap<>();

        //构造节点的笛卡尔积
        List<TaLocation> newLocations = new ArrayList<>();
        for (TaLocation l1 : ta1.getLocations()) {
            for (TaLocation l2 : ta2.getLocations()) {
                TaLocation newLocation = new TaLocation.TaLocationBuilder()
                        .id(l1.getId() + "_" + l2.getId())
                        .name(l1.getName() + "_" + l2.getName())
                        .accept(l1.isAccept() && l2.isAccept())
                        .init(l1.isInit() && l2.isInit())
                        .build();
//                System.out.println("newLocation.getName():"+newLocation.getName());
//                System.out.println(l1.getName()+l1.isAccept()+l2.getName()+l2.isAccept());
//                System.out.println(newLocation.isAccept());
//                System.out.println(newLocation.isInit());
                Pair pair = new Pair(l1, l2);
                pairLocationMap.put(pair, newLocation);
                newLocations.add(newLocation);
            }
        }

        //sigma求并集
        Set<String> sigma = new HashSet<>();
        sigma.addAll(ta1.getSigma());
        sigma.addAll(ta2.getSigma());

        //时钟求并集
        List<Clock> clocks = new ArrayList<>();
        clocks.addAll(ta1.getClockList());
        clocks.addAll(ta2.getClockList());

        //构造迁移的笛卡尔积
        //遍历sigma，分三种情况求迁移
        List<TaTransition> newTransitions = new ArrayList<>();
        sigma.forEach(e -> {
            //第一种情况，两边都含有相同的动作,需要对其进行同步操作
            if (ta1.containsSymbol(e) && ta2.containsSymbol(e)) {
                for (TaTransition t1 : ta1.getTransitions(null, e, null)) {
                    for (TaTransition t2 : ta2.getTransitions(null, e, null)) {
                        Pair sourcePair = new Pair(t1.getSourceLocation(), t2.getSourceLocation());
                        Pair targetPair = new Pair(t1.getTargetLocation(), t2.getTargetLocation());
                        TaLocation sourceLocation = pairLocationMap.get(sourcePair);
                        TaLocation targetLocation = pairLocationMap.get(targetPair);
                        //时钟约束求并集
                        List<EdgeTimeGuard> edgeTimeGuardList=new ArrayList<>();
                        EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
                        Map<Clock, TimeGuard> timeGuardClockMap = new HashMap<>();
                        timeGuardClockMap.putAll(t1.getEdgeTimeGuard().getClockTimeGuardMap());
                        //timeGuardClockMap.putAll(t2.getEdgeTimeGuard().getClockTimeGuardMap());
                        edgeTimeGuard.setClockTimeGuardMap(timeGuardClockMap);
//                        edgeTimeGuard.setLargerclock(t1.getEdgeTimeGuard().getLargerclock());
//                        edgeTimeGuard.setHavelowerdiff(t1.getEdgeTimeGuard().isHavelowerdiff());
//                        edgeTimeGuard.setDifferencexylower(t1.getEdgeTimeGuard().getDifferencexylower());
//                        edgeTimeGuard.setIsequallower(t1.getEdgeTimeGuard().isIsequallower());
//                        edgeTimeGuard.setHaveupperdiff(t1.getEdgeTimeGuard().isHaveupperdiff());
//                        edgeTimeGuard.setDifferencexyupper(t1.getEdgeTimeGuard().getDifferencexyupper());
//                        edgeTimeGuard.setIsequalupper(t1.getEdgeTimeGuard().isIsequalupper());
                        edgeTimeGuard.setDifferenceEdgeTimeGuards(t1.getEdgeTimeGuard().getDifferenceEdgeTimeGuards());
                        edgeTimeGuardList.add(edgeTimeGuard);

                        EdgeTimeGuard edgeTimeGuard2=new EdgeTimeGuard();
                        Map<Clock, TimeGuard> timeGuardClockMap2 = new HashMap<>();
                        timeGuardClockMap2.putAll(t2.getEdgeTimeGuard().getClockTimeGuardMap());
                        //timeGuardClockMap.putAll(t2.getEdgeTimeGuard().getClockTimeGuardMap());
                        edgeTimeGuard2.setClockTimeGuardMap(timeGuardClockMap2);
//                        edgeTimeGuard2.setLargerclock(t2.getEdgeTimeGuard().getLargerclock());
//                        edgeTimeGuard2.setHavelowerdiff(t2.getEdgeTimeGuard().isHavelowerdiff());
//                        edgeTimeGuard2.setDifferencexylower(t2.getEdgeTimeGuard().getDifferencexylower());
//                        edgeTimeGuard2.setIsequallower(t2.getEdgeTimeGuard().isIsequallower());
//                        edgeTimeGuard2.setHaveupperdiff(t2.getEdgeTimeGuard().isHaveupperdiff());
//                        edgeTimeGuard2.setDifferencexyupper(t2.getEdgeTimeGuard().getDifferencexyupper());
//                        edgeTimeGuard2.setIsequalupper(t2.getEdgeTimeGuard().isIsequalupper());
                        edgeTimeGuard2.setDifferenceEdgeTimeGuards(t2.getEdgeTimeGuard().getDifferenceEdgeTimeGuards());
                        edgeTimeGuardList.add(edgeTimeGuard2);
                        EdgeTimeGuard addedgeTimeGuard=new EdgeTimeGuard();
                        addedgeTimeGuard.setEdgeTimeGuardList(edgeTimeGuardList);
//System.out.println("addedgeTimeGuard.getEdgeTimeGuardList():"+addedgeTimeGuard.getEdgeTimeGuardList().get(0).getClockTimeGuardMap());
//System.out.println("addedgeTimeGuard.getEdgeTimeGuardList():"+addedgeTimeGuard.getEdgeTimeGuardList().get(1).getClockTimeGuardMap());



//                        if(t1.getEdgeTimeGuard().isHavelowerdiff()&&t2.getEdgeTimeGuard().isHavelowerdiff()){
//                            if(t1.getEdgeTimeGuard().getDifferencexylower()>t2.getEdgeTimeGuard().getDifferencexylower()||
//                                    t1.getEdgeTimeGuard().getDifferencexylower()==t2.getEdgeTimeGuard().getDifferencexylower()&&t1.getEdgeTimeGuard().isIsequallower()){
//                                edgeTimeGuard.setLargerclock(true);
//                                edgeTimeGuard.setHavelowerdiff(true);
//                                edgeTimeGuard.setDifferencexylower(t1.getEdgeTimeGuard().getDifferencexylower());
//                                edgeTimeGuard.setIsequallower(t1.getEdgeTimeGuard().isIsequallower());}
//                            else{
//                                edgeTimeGuard.setLargerclock(true);
//                                edgeTimeGuard.setHavelowerdiff(true);
//                                edgeTimeGuard.setDifferencexylower(t2.getEdgeTimeGuard().getDifferencexylower());
//                                edgeTimeGuard.setIsequallower(t2.getEdgeTimeGuard().isIsequallower());
//                            }
//                        }
//                       else if(t1.getEdgeTimeGuard().isHavelowerdiff()&&!t2.getEdgeTimeGuard().isHavelowerdiff()){
//                                edgeTimeGuard.setLargerclock(true);
//                            edgeTimeGuard.setHavelowerdiff(true);
//                            edgeTimeGuard.setDifferencexylower(t1.getEdgeTimeGuard().getDifferencexylower());
//                            edgeTimeGuard.setIsequallower(t1.getEdgeTimeGuard().isIsequallower());
//                        }
//                       else if(!t1.getEdgeTimeGuard().isHavelowerdiff()&&t2.getEdgeTimeGuard().isHavelowerdiff()){
//                            edgeTimeGuard.setLargerclock(true);
//                            edgeTimeGuard.setHavelowerdiff(true);
//                            edgeTimeGuard.setDifferencexylower(t2.getEdgeTimeGuard().getDifferencexylower());
//                            edgeTimeGuard.setIsequallower(t2.getEdgeTimeGuard().isIsequallower());
//                        }
//
//                        if(t1.getEdgeTimeGuard().isHaveupperdiff()&&t2.getEdgeTimeGuard().isHaveupperdiff()){
//                            if(t1.getEdgeTimeGuard().getDifferencexylower()<t2.getEdgeTimeGuard().getDifferencexylower()||
//                                    t1.getEdgeTimeGuard().getDifferencexylower()==t2.getEdgeTimeGuard().getDifferencexylower()&&t1.getEdgeTimeGuard().isIsequallower())
//                            {     edgeTimeGuard.setLargerclock(true);
//                            edgeTimeGuard.setHaveupperdiff(true);
//                            edgeTimeGuard.setDifferencexyupper(t1.getEdgeTimeGuard().getDifferencexylower());
//                            edgeTimeGuard.setIsequalupper(t1.getEdgeTimeGuard().isIsequallower());}
//                            else{
//                                edgeTimeGuard.setLargerclock(true);
//                                edgeTimeGuard.setHaveupperdiff(true);
//                                edgeTimeGuard.setDifferencexyupper(t2.getEdgeTimeGuard().getDifferencexylower());
//                                edgeTimeGuard.setIsequalupper(t2.getEdgeTimeGuard().isIsequallower());
//                            }
//                        }
//                        else if(t1.getEdgeTimeGuard().isHaveupperdiff()&&!t2.getEdgeTimeGuard().isHaveupperdiff()){
//                            edgeTimeGuard.setLargerclock(true);
//                            edgeTimeGuard.setHaveupperdiff(true);
//                            edgeTimeGuard.setDifferencexyupper(t1.getEdgeTimeGuard().getDifferencexyupper());
//                            edgeTimeGuard.setIsequalupper(t1.getEdgeTimeGuard().isIsequalupper());
//                        }
//                        else if(!t1.getEdgeTimeGuard().isHaveupperdiff()&&t2.getEdgeTimeGuard().isHaveupperdiff()){
//                            edgeTimeGuard.setLargerclock(true);
//                            edgeTimeGuard.setHaveupperdiff(true);
//                            edgeTimeGuard.setDifferencexyupper(t2.getEdgeTimeGuard().getDifferencexyupper());
//                            edgeTimeGuard.setIsequalupper(t2.getEdgeTimeGuard().isIsequalupper());
//                        }
                        //重置时钟集合求并集
                        Set<Clock> resetClocks = new HashSet<>();
                        resetClocks.addAll(t1.getResetClockSet());
                        //        System.out.println("t1.getResetClockSet():"+t1.getResetClockSet());
                        //      resetClocks.addAll(t2.getResetClockSet());
                        for (int i=0;i<ta2.getClockList().size();i++){
                            if(t2.getResetClockSet().contains(ta2.getClockList().get(i))){
                                Clock x = new Clock("x" + String.valueOf(i+ta1.getClockList().size()));
                                resetClocks.add(x);
                            }
                        }

                        //       System.out.println("t2.getResetClockSet():"+t2.getResetClockSet());
//System.out.println("11111");
//                        for (Clock clock:resetClocks){
//                            System.out.println("平行组合中的clock:"+clock.getName());
//                        }
                        //构建新的迁移
                        TaTransition newTransition = new TaTransition.TaTransitionBuilder()
                                .sourceLocation(sourceLocation)
                                .targetLocation(targetLocation)
                                .symbol(t1.getSymbol())
                                .clockTimeGuardMap(addedgeTimeGuard)
                                .resetClockSet(resetClocks)
                                .build();
                        newTransitions.add(newTransition);
                    }
                }
            }
            //第二种情况，只有ta1存在的动作
            if (ta1.containsSymbol(e) && !ta2.containsSymbol(e)) {
                asyncTransitions(ta1.getTransitions(), ta2.getLocations(), pairLocationMap, newTransitions);
            }
            //第三种情况，只有dfa2存在的动作
            if (!ta1.containsSymbol(e) && ta2.containsSymbol(e)) {
                asyncTransitions(ta2.getTransitions(), ta1.getLocations(), pairLocationMap, newTransitions);
            }
        });

        newTransitions.sort(new Comparator<TaTransition>() {
            @Override
            public int compare(TaTransition o1, TaTransition o2) {
                return o1.getSourceId().compareTo(o2.getSourceId());
            }
        });

        //构造组合自动机TA
        TA newTA = new TA.TABuilder()
                .name(ta1.getName() + "_" + ta2.getName())
                .locations(newLocations)
                .transitions(newTransitions)
                .sigma(sigma)
                .clockList(clocks)
                .build();
        return newTA;

    }

    private static void asyncTransitions(List<TaTransition> transitions,
                                         List<TaLocation> locations,
                                         Map<Pair, TaLocation> pairLocationMap,
                                         List<TaTransition> newTransitions) {
        for (TaTransition t : transitions) {
            for (TaLocation l : locations) {
                Pair sourcePair = new Pair(t.getSourceLocation(), l);
                Pair targetPair = new Pair(t.getTargetLocation(), l);
                TaLocation sourceLocation = pairLocationMap.get(sourcePair);
                TaLocation targetLocation = pairLocationMap.get(targetPair);
                TaTransition newTransition = new TaTransition.TaTransitionBuilder()
                        .sourceLocation(sourceLocation)
                        .targetLocation(targetLocation)
                        .symbol(t.getSymbol())
                        .clockTimeGuardMap(t.getClockTimeGuardMap())
                        .resetClockSet(t.getResetClockSet())
                        .build();
                newTransitions.add(newTransition);
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class Pair {
        TaLocation location1;
        TaLocation location2;
    }

    //时间自动机求补
//    public static TA completeTA(TA ta) {
//        ta = ta.copy();
//        return null;
//    }

    //时间自动机取反
    public static TA negTA(TA ta) {
        TA neg = ta.copy();
        for (TaLocation l : neg.getLocations()) {
            l.setAccept(!l.isAccept());
        }
        return neg;
    }


}
