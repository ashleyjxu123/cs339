package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         */
        public final Type fieldType;

        /**
         * The name of the field
         */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return An iterator which iterates over all the field TDItems
     *         that are included in this TupleDesc
     */
    public Iterator<TDItem> iterator() {
        return new Iterator<TDItem>() {
            int i = -1;
            public boolean hasNext()
            {
                return TDItems[i + 1] != null;
            }
            public TDItem next(){
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                i += 1;
                return TDItems[i];
            }
        };
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     *
     * @param typeAr  array specifying the number of and types of fields in this
     *                TupleDesc. It must contain at least one entry.
     * @param fieldAr array specifying the names of the fields. Note that names may
     *                be null.
     */
    public int n;
    public TDItem[] TDItems;
    
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        n = typeAr.length;
        TDItems = new TDItem[n];
        for(int i = 0; i < typeAr.length; i++){
            TDItem tditem = new TDItem(typeAr[i], fieldAr[i]);
            TDItems[i] = tditem;
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     *
     * @param typeAr array specifying the number of and types of fields in this
     *               TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        n = typeAr.length;
        TDItems = new TDItem[n];
        for(int i = 0; i < typeAr.length; i++){
            TDItem tditem = new TDItem(typeAr[i], null);
            TDItems[i] = tditem;
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        return n;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     *
     * @param i index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        try {
            return TDItems[i].fieldName;
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     *
     * @param i The index of the field to get the type of. It must be a valid
     *          index.
     * @return the type of the ith field
     * @throws NoSuchElementException if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        try {
            return TDItems[i].fieldType;
        } catch (Exception e) {
            throw new NoSuchElementException();
        }
    }

    /**
     * Find the index of the field with a given name.
     *
     * @param name name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException if no field with a matching name is found.
     */
    public int indexForFieldName(String name) throws NoSuchElementException {
        for(int i = 0; i < n; i++){
            if (this.getFieldName(i) != null && TDItems[i].fieldName.equals(name)){
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        int sum = 0;
        for(TDItem item : TDItems){
            sum += item.fieldType.getLen();
        }
        return sum;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     *
     * @param td1 The TupleDesc with the first fields of the new TupleDesc
     * @param td2 The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        int size = td1.numFields() + td2.numFields();
        Type[] newType = new Type[size];
        String[] newNames = new String[size];
        int i = 0;
        while (i < td1.numFields()){
            newType[i] = td1.TDItems[i].fieldType;
            newNames[i] = td1.TDItems[i].fieldName;
            i++;
        }
        int j = 0;
        while (j < td2.numFields()) {
            newType[i] = td2.TDItems[j].fieldType;
            newNames[i] = td2.TDItems[j].fieldName;
            i++;
            j++;
        }

        TupleDesc newTup = new TupleDesc(newType, newNames);
        return newTup;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     *
     * @param o the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        if (o instanceof TupleDesc){
            TupleDesc comp = (TupleDesc)o;
            if (comp.numFields() ==this.numFields()){
                for (int i = 0; i < this.numFields(); i++){
                    if (this.getFieldType(i) != comp.getFieldType(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     *
     * @return String describing this descriptor.
     */
    public String toString() {
        String result = "";
        for (int i = 0; i < this.numFields(); i++) {
            result += this.TDItems[i].fieldType.toString();
            result += "(" + this.TDItems[i].fieldName + "), ";
        }
        return result;
    }
}
