package simpledb.execution;

import simpledb.common.Type;
import simpledb.storage.*;
import java.util.*;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    public int gbfield;
    public Type gbfieldtype;
    public int afield;
    public Op what;
    public Map<Field, Object> grouping;

    /**
     * Aggregate constructor
     *
     * @param gbfield     the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield      the 0-based index of the aggregate field in the tuple
     * @param what        aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afield = afield;
        this.what = what;
        this.grouping  = new HashMap<>();

        if (what != Op.COUNT) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
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
            aggVal = 0;
        }
        aggVal = (Integer) aggVal + 1;
        grouping.put(newField, aggVal);
    }

    /**
     * Create a OpIterator over group aggregate results.
     *
     * @return a OpIterator whose tuples are the pair (groupVal,
     *         aggregateVal) if using group, or a single (aggregateVal) if no
     *         grouping. The aggregateVal is determined by the type of
     *         aggregate specified in the constructor.
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
            int val = (Integer) e.getValue();

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
