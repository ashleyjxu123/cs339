package simpledb.execution;

import java.util.*;
import simpledb.common.Type;
import simpledb.storage.*;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    public int gbfield;
    public Type gbfieldtype;
    public int afield;
    public Op what;
    public Map<Field, Object> grouping;

    /**
     * Aggregate constructor
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or
     *                    NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null
     *                    if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;
        this.grouping  = new HashMap<>();
    }

    private Object getInit() {
        switch (what) {
            case MIN: return Integer.MAX_VALUE;
            case MAX: return Integer.MIN_VALUE;
            case SUM: return 0;
            case COUNT: return 0;
            case SUM_COUNT: return new Integer[] {0, 0};
            case AVG: return new Integer[] {0, 0};
            case SC_AVG: return new Integer[] {0, 0};
        }
        return null;
    }

    private Object getAggVal(Object agg, int val) {
        switch (what) {
            case MIN: return Integer.min((Integer) agg, val);
            case MAX: return Integer.max((Integer) agg, val);
            case SUM: return ((Integer) agg) + val;
            case COUNT: return ((Integer) agg) + 1;
            case AVG: 
                Integer[] p = (Integer[]) agg;
                return new Integer[] {p[0] + val, p[1] + 1};
            case SUM_COUNT: 
                Integer[] p2 = (Integer[]) agg;
                return new Integer[]{p2[0] + val, p2[1] + 1};
            case SC_AVG:
                Integer[] p3 = (Integer[]) agg;
                return new Integer[] {p3[0] + val, p3[1] + 1};

        }
        return null;
    }
 
    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     *
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        Field newField = null;
        if (gbfield != NO_GROUPING && tup.getTupleDesc().getFieldType(gbfield) == gbfieldtype) {
            newField = tup.getField(gbfield);
        }

        Object aggVal = grouping.get(newField);
        if (aggVal == null) {
            aggVal = getInit();
        }
        aggVal = getAggVal(aggVal, ((IntField) tup.getField(afield)).getValue());
        grouping.put(newField, aggVal);
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public OpIterator iterator() {
        ArrayList<Tuple> tuples = new ArrayList<>();
        TupleDesc td = null;
        if (gbfield == NO_GROUPING){
            td = new TupleDesc(new Type[]{Type.INT_TYPE}, new String[]{"a"});
        } else {
            td = new TupleDesc(new Type[]{gbfieldtype, Type.INT_TYPE}, new String[]{"g", "a"});
        }
        for(Map.Entry<Field, Object> e: grouping.entrySet()) {
            Field groupVal = e.getKey();
            int val;
            if (what == Op.AVG || what == Op.SC_AVG) {
                Integer[] p = ((Integer[]) e.getValue());
                val = p[0] / p[1];
            } else {
                val = (Integer) e.getValue();
            }

            if (gbfield == NO_GROUPING){
                Tuple t = new Tuple(td);
                t.setField(0, new IntField(val));
                tuples.add(t);
            } else {
                Tuple t = new Tuple(td);
                t.setField(0, groupVal);
                t.setField(1, new IntField(val));
                tuples.add(t);
            }
        }
        return new TupleIterator(td, tuples);
    }

}
