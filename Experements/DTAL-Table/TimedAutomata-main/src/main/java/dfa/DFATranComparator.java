package dfa;

public class DFATranComparator implements java.util.Comparator<DfaTransition> {
    @Override
    public int compare(DfaTransition o1, DfaTransition o2) {
        int var1 = o1.getSourceId().compareTo(o2.getSourceId());
        if (var1 != 0) {
            return var1;
        }
        int var2 = o1.getSymbol().compareTo(o2.getSymbol());
        if (var2 != 0) {
            return var2;
        }
        int var3 = o1.getTargetId().compareTo(o2.getTargetId());
        if (var3 != 0) {
            return var3;
        }
        return -1;
    }
}
