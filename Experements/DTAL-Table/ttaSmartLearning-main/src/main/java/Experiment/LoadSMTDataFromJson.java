////package Experiment;
////
////import com.fasterxml.jackson.databind.ObjectMapper;
////import ta.*;
////import ta.twoClockTA.TwoClockTA;
////
////import java.io.File;
////import java.io.IOException;
////import java.util.*;
////
////public class LoadSMTDataFromJson {
////
////    public static TwoClockTA load(String filepath) throws IOException {
////        ObjectMapper objectMapper = new ObjectMapper();
////        File file = new File(filepath);
////        SMTJsonObject object = objectMapper.readValue(file, SMTJsonObject.class);
////        System.out.println(object);
////        String TAname=object.getName();
////        List<TaLocation> locations=new ArrayList<>();
////        for (String l:object.getL()){
////            TaLocation l1=new TaLocation(l,"l"+l,false, false);
////            if(object.getInit().equals(l)){
////                l1.setInit(true);
////            }
////            if(object.getAccept().contains(l)){
////                l1.setAccept(true);
////            }
////            locations.add(l1);
////        }
////        Set<String> sigmaSet=new HashSet<>();
////        for (String sigma:object.getSigma()){
////            sigmaSet.add(sigma);
////        }
////        List<Clock> clockList=new ArrayList<>();
////        for(int i = 0; i < 2; ++i) {
////            Clock x = new Clock("x" + String.valueOf(i));
////            clockList.add(x);
////        }
////        //   System.out.println(locations);
////        List<TaTransition> transitions=new ArrayList<>();
////        for (List<String> stringList:object.getTran().values()){
////            TimeGuard timeGuard=new TimeGuard();
////            if(stringList.get(2).charAt(0)=='['){
////                timeGuard.setLowerBoundOpen(false);
////            }
////            else {
////                timeGuard.setLowerBoundOpen(true);
////            }
////            if(stringList.get(2).charAt(stringList.get(2).length()-1)==']'){
////                timeGuard.setUpperBoundOpen(false);
////            }
////            else {
////                timeGuard.setUpperBoundOpen(true);
////            }
////            if(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1).equals("+")){
////                timeGuard.setUpperBound(TimeGuard.MAX_TIME);
////            }
////            else {
////                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
////                timeGuard.setUpperBound(Integer.valueOf(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1)).intValue());
////            }
////            timeGuard.setLowerBound(Integer.valueOf(stringList.get(2).substring(1,stringList.get(2).indexOf(","))).intValue());
////
////            TimeGuard timeGuard2=new TimeGuard();
////            if(stringList.get(3).charAt(0)=='['){
////                timeGuard2.setLowerBoundOpen(false);
////            }
////            else {
////                timeGuard2.setLowerBoundOpen(true);
////            }
////            if(stringList.get(3).charAt(stringList.get(3).length()-1)==']'){
////                timeGuard2.setUpperBoundOpen(false);
////            }
////            else {
////                timeGuard2.setUpperBoundOpen(true);
////            }
////            if(stringList.get(3).substring(stringList.get(3).indexOf(",")+1,stringList.get(3).length()-1).equals("+")){
////                timeGuard2.setUpperBound(TimeGuard.MAX_TIME);
////            }
////            else {
////                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
////                timeGuard2.setUpperBound(Integer.valueOf(stringList.get(3).substring(stringList.get(3).indexOf(",")+1,stringList.get(3).length()-1)).intValue());
////            }
////            timeGuard2.setLowerBound(Integer.valueOf(stringList.get(3).substring(1,stringList.get(3).indexOf(","))).intValue());
////
////
////            // System.out.println(timeGuard);
////            Map<Clock, TimeGuard> clockTimeGuardMap=new HashMap<>();
////            clockTimeGuardMap.put(clockList.get(0),timeGuard);
////            clockTimeGuardMap.put(clockList.get(1),timeGuard2);
////            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
////            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
////            Set<Clock> resetclock=new HashSet<>();
////            if(stringList.get(4).equals("r")){
////                resetclock.add(clockList.get(0));
////            }
////            if(stringList.get(5).equals("r")){
////                resetclock.add(clockList.get(1));
////            }
////
//////            System.out.println(locations);
//////            System.out.println(Integer.valueOf(stringList.get(0)).intValue()-1);
//////            System.out.println(locations.get(0));
//////            System.out.println(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1));
//////            System.out.println(Integer.valueOf(stringList.get(4)).intValue()-1);
//////            System.out.println(locations.get(5));
//////            System.out.println(locations.get(Integer.valueOf(stringList.get(4)).intValue()-1));
//////            System.out.println(stringList.get(1));
//////            System.out.println(resetclock);
////            TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(6)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
////            //    TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(4)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
////
////            transitions.add(transition);
////        }
////        System.out.println(transitions);
////        TwoClockTA twoclockta=new TwoClockTA(TAname, clockList, sigmaSet, locations, transitions);
////        System.out.println(twoclockta.getClass().toString());
////        return twoclockta;
////    }
////}
//
//package Experiment;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ta.*;
//import ta.twoClockTA.TwoClockTA;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
//public class LoadSMTDataFromJson {
//
//    public static TwoClockTA load(String filepath) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        File file = new File(filepath);
//        SMTJsonObject object = objectMapper.readValue(file, SMTJsonObject.class);
//        System.out.println(object);
//        String TAname=object.getName();
//        List<TaLocation> locations=new ArrayList<>();
//        for (String l:object.getL()){
//            TaLocation l1=new TaLocation(l,"l"+l,false, false);
//            if(object.getInit().equals(l)){
//                l1.setInit(true);
//            }
//            if(object.getAccept().contains(l)){
//                l1.setAccept(true);
//            }
//            locations.add(l1);
//        }
//        Set<String> sigmaSet=new HashSet<>();
//        for (String sigma:object.getSigma()){
//            sigmaSet.add(sigma);
//        }
//        List<Clock> clockList=new ArrayList<>();
//        for(int i = 0; i < 1; ++i) {
//            Clock x = new Clock("x" + String.valueOf(i));
//            clockList.add(x);
//        }
//        //   System.out.println(locations);
//        List<TaTransition> transitions=new ArrayList<>();
//        for (List<String> stringList:object.getTran().values()){
//            TimeGuard timeGuard=new TimeGuard();
//            if(stringList.get(2).charAt(0)=='['){
//                timeGuard.setLowerBoundOpen(false);
//            }
//            else {
//                timeGuard.setLowerBoundOpen(true);
//            }
//            if(stringList.get(2).charAt(stringList.get(2).length()-1)==']'){
//                timeGuard.setUpperBoundOpen(false);
//            }
//            else {
//                timeGuard.setUpperBoundOpen(true);
//            }
//            if(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1).equals("+")){
//                timeGuard.setUpperBound(TimeGuard.MAX_TIME);
//            }
//            else {
//                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
//                timeGuard.setUpperBound(Integer.valueOf(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1)).intValue());
//            }
//            timeGuard.setLowerBound(Integer.valueOf(stringList.get(2).substring(1,stringList.get(2).indexOf(","))).intValue());
//            // System.out.println(timeGuard);
//            Map<Clock, TimeGuard> clockTimeGuardMap=new HashMap<>();
//            clockTimeGuardMap.put(clockList.get(0),timeGuard);
//            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//            Set<Clock> resetclock=new HashSet<>();
//               if(stringList.get(3).equals("r")){
//            resetclock.add(clockList.get(0));
//                }
//
////            System.out.println(locations);
////            System.out.println(Integer.valueOf(stringList.get(0)).intValue()-1);
////            System.out.println(locations.get(0));
////            System.out.println(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1));
////            System.out.println(Integer.valueOf(stringList.get(4)).intValue()-1);
////            System.out.println(locations.get(5));
////            System.out.println(locations.get(Integer.valueOf(stringList.get(4)).intValue()-1));
////            System.out.println(stringList.get(1));
////            System.out.println(resetclock);
//      //      TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(3)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
//                TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(4)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
//
//            transitions.add(transition);
//        }
//        System.out.println(transitions);
//        TwoClockTA twoclockta=new TwoClockTA(TAname, clockList, sigmaSet, locations, transitions);
//        System.out.println(twoclockta.getClass().toString());
//        return twoclockta;
//    }
//}
//package Experiment;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ta.*;
//import ta.twoClockTA.TwoClockTA;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//
//public class LoadSMTDataFromJson {
//
//    public static TwoClockTA load(String filepath) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        File file = new File(filepath);
//        SMTJsonObject object = objectMapper.readValue(file, SMTJsonObject.class);
//        System.out.println(object);
//        String TAname=object.getName();
//        List<TaLocation> locations=new ArrayList<>();
//        for (String l:object.getL()){
//            TaLocation l1=new TaLocation(l,"l"+l,false, false);
//            if(object.getInit().equals(l)){
//                l1.setInit(true);
//            }
//            if(object.getAccept().contains(l)){
//                l1.setAccept(true);
//            }
//            locations.add(l1);
//        }
//        Set<String> sigmaSet=new HashSet<>();
//        for (String sigma:object.getSigma()){
//            sigmaSet.add(sigma);
//        }
//        List<Clock> clockList=new ArrayList<>();
//        for(int i = 0; i < 2; ++i) {
//            Clock x = new Clock("x" + String.valueOf(i));
//            clockList.add(x);
//        }
//        //   System.out.println(locations);
//        List<TaTransition> transitions=new ArrayList<>();
//        for (List<String> stringList:object.getTran().values()){
//            TimeGuard timeGuard=new TimeGuard();
//            if(stringList.get(2).charAt(0)=='['){
//                timeGuard.setLowerBoundOpen(false);
//            }
//            else {
//                timeGuard.setLowerBoundOpen(true);
//            }
//            if(stringList.get(2).charAt(stringList.get(2).length()-1)==']'){
//                timeGuard.setUpperBoundOpen(false);
//            }
//            else {
//                timeGuard.setUpperBoundOpen(true);
//            }
//            if(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1).equals("+")){
//                timeGuard.setUpperBound(TimeGuard.MAX_TIME);
//            }
//            else {
//                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
//                timeGuard.setUpperBound(Integer.valueOf(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1)).intValue());
//            }
//            timeGuard.setLowerBound(Integer.valueOf(stringList.get(2).substring(1,stringList.get(2).indexOf(","))).intValue());
//
//            TimeGuard timeGuard2=new TimeGuard();
//            if(stringList.get(3).charAt(0)=='['){
//                timeGuard2.setLowerBoundOpen(false);
//            }
//            else {
//                timeGuard2.setLowerBoundOpen(true);
//            }
//            if(stringList.get(3).charAt(stringList.get(3).length()-1)==']'){
//                timeGuard2.setUpperBoundOpen(false);
//            }
//            else {
//                timeGuard2.setUpperBoundOpen(true);
//            }
//            if(stringList.get(3).substring(stringList.get(3).indexOf(",")+1,stringList.get(3).length()-1).equals("+")){
//                timeGuard2.setUpperBound(TimeGuard.MAX_TIME);
//            }
//            else {
//                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
//                timeGuard2.setUpperBound(Integer.valueOf(stringList.get(3).substring(stringList.get(3).indexOf(",")+1,stringList.get(3).length()-1)).intValue());
//            }
//            timeGuard2.setLowerBound(Integer.valueOf(stringList.get(3).substring(1,stringList.get(3).indexOf(","))).intValue());
//
//
//            // System.out.println(timeGuard);
//            Map<Clock, TimeGuard> clockTimeGuardMap=new HashMap<>();
//            clockTimeGuardMap.put(clockList.get(0),timeGuard);
//            clockTimeGuardMap.put(clockList.get(1),timeGuard2);
//            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
//            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//            Set<Clock> resetclock=new HashSet<>();
//               if(stringList.get(4).equals("r")){
//               resetclock.add(clockList.get(0));
//                }
//            if(stringList.get(5).equals("r")){
//                resetclock.add(clockList.get(1));
//            }
//
////            System.out.println(locations);
////            System.out.println(Integer.valueOf(stringList.get(0)).intValue()-1);
////            System.out.println(locations.get(0));
////            System.out.println(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1));
////            System.out.println(Integer.valueOf(stringList.get(4)).intValue()-1);
////            System.out.println(locations.get(5));
////            System.out.println(locations.get(Integer.valueOf(stringList.get(4)).intValue()-1));
////            System.out.println(stringList.get(1));
////            System.out.println(resetclock);
//            TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(6)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
//            //    TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(4)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
//
//            transitions.add(transition);
//        }
//        System.out.println(transitions);
//        TwoClockTA twoclockta=new TwoClockTA(TAname, clockList, sigmaSet, locations, transitions);
//        System.out.println(twoclockta.getClass().toString());
//        return twoclockta;
//    }
//}

package Experiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import ta.*;
import ta.twoClockTA.TwoClockTA;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LoadSMTDataFromJson {

    public static TwoClockTA load(String filepath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filepath);
        SMTJsonObject object = objectMapper.readValue(file, SMTJsonObject.class);
        System.out.println(object);
        String TAname=object.getName();
        List<TaLocation> locations=new ArrayList<>();
        for (String l:object.getL()){
            TaLocation l1=new TaLocation(l,"l"+l,false, false);
            if(object.getInit().equals(l)){
                l1.setInit(true);
            }
            if(object.getAccept().contains(l)){
                l1.setAccept(true);
            }
            locations.add(l1);
        }
        Set<String> sigmaSet=new HashSet<>();
        for (String sigma:object.getSigma()){
            sigmaSet.add(sigma);
        }
        List<Clock> clockList=new ArrayList<>();
        for(int i = 0; i < 2; ++i) {
            Clock x = new Clock("x" + String.valueOf(i));
            clockList.add(x);
        }
        //   System.out.println(locations);
        List<TaTransition> transitions=new ArrayList<>();
        for (List<String> stringList:object.getTran().values()){
            TimeGuard timeGuard=new TimeGuard();
            if(stringList.get(2).charAt(0)=='['){
                timeGuard.setLowerBoundOpen(false);
            }
            else {
                timeGuard.setLowerBoundOpen(true);
            }
            if(stringList.get(2).charAt(stringList.get(2).length()-1)==']'){
                timeGuard.setUpperBoundOpen(false);
            }
            else {
                timeGuard.setUpperBoundOpen(true);
            }
            if(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1).equals("+")){
                timeGuard.setUpperBound(TimeGuard.MAX_TIME);
            }
            else {
                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
                timeGuard.setUpperBound(Integer.valueOf(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1)).intValue());
            }
            timeGuard.setLowerBound(Integer.valueOf(stringList.get(2).substring(1,stringList.get(2).indexOf(","))).intValue());
            // System.out.println(timeGuard);
            Map<Clock, TimeGuard> clockTimeGuardMap=new HashMap<>();
            clockTimeGuardMap.put(clockList.get(0),timeGuard);
            TimeGuard timeGuard1=new TimeGuard();
            if(stringList.get(3).charAt(0)=='['){
                timeGuard1.setLowerBoundOpen(false);
            }
            else {
                timeGuard1.setLowerBoundOpen(true);
            }
            if(stringList.get(3).charAt(stringList.get(3).length()-1)==']'){
                timeGuard1.setUpperBoundOpen(false);
            }
            else {
                timeGuard1.setUpperBoundOpen(true);
            }
            if(stringList.get(3).substring(stringList.get(3).indexOf(",")+1,stringList.get(3).length()-1).equals("+")){
                timeGuard1.setUpperBound(TimeGuard.MAX_TIME);
            }
            else {
                //  System.out.println(stringList.get(2).substring(stringList.get(2).indexOf(",")+1,stringList.get(2).length()-1));
                timeGuard1.setUpperBound(Integer.valueOf(stringList.get(3).substring(stringList.get(3).indexOf(",")+1,stringList.get(3).length()-1)).intValue());
            }
            timeGuard1.setLowerBound(Integer.valueOf(stringList.get(3).substring(1,stringList.get(3).indexOf(","))).intValue());
            // System.out.println(timeGuard);
            //      Map<Clock, TimeGuard> clockTimeGuardMap=new HashMap<>();
            clockTimeGuardMap.put(clockList.get(1),timeGuard1);
            EdgeTimeGuard edgeTimeGuard=new EdgeTimeGuard();
            edgeTimeGuard.setClockTimeGuardMap(clockTimeGuardMap);
//            Set<Clock> resetclock=new HashSet<>();
//               if(stringList.get(3).equals("r")){
//            resetclock.add(clockList.get(0));
//                }
            Set<Clock> resetclock=new HashSet<>();
            if(stringList.get(4).equals("r")){
                resetclock.add(clockList.get(0));
            }
            if(stringList.get(5).equals("r")){
                resetclock.add(clockList.get(1));
            }

//            System.out.println(locations);
//            System.out.println(Integer.valueOf(stringList.get(0)).intValue()-1);
//            System.out.println(locations.get(0));
//            System.out.println(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1));
//            System.out.println(Integer.valueOf(stringList.get(4)).intValue()-1);
//            System.out.println(locations.get(5));
//            System.out.println(locations.get(Integer.valueOf(stringList.get(4)).intValue()-1));
//            System.out.println(stringList.get(1));
//            System.out.println(resetclock);
//            TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(3)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
//                TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(4)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);
            TaTransition transition=new TaTransition(locations.get(Integer.valueOf(stringList.get(0)).intValue()-1),locations.get(Integer.valueOf(stringList.get(6)).intValue()-1),stringList.get(1),edgeTimeGuard,resetclock);

            transitions.add(transition);
        }
        System.out.println(transitions);
        TwoClockTA twoclockta=new TwoClockTA(TAname, clockList, sigmaSet, locations, transitions);
        System.out.println(twoclockta.getClass().toString());
        return twoclockta;
    }
}
