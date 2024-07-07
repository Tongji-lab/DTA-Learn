package Experiment;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SMTJsonObject {
    private String name = null;
    private Map<String, List<String>> tran=null;
    private String init=null;
    private List<String> accept=null;
    private List<String> l=null;
    //private List<String> s=null;
    private List<String> sigma=null;


}
//package Experiment;
//
//import lombok.Data;
//
//import java.util.List;
//import java.util.Map;
//
//@Data
//public class SMTJsonObject {
//    private String name = null;
//    private List<String> l=null;
//    private List<String> sigma=null;
//    private Map<String, List<String>> tran=null;
//    private String init=null;
//    private List<String> accept=null;
//
//}
