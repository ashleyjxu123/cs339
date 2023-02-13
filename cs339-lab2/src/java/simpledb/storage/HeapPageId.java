package simpledb.storage;

import java.util.Objects;

import simpledb.common.Catalog;
import simpledb.common.Database;

/**
 * Unique identifier for HeapPage objects.
 */
public class HeapPageId implements PageId {


    public final Integer tid;
    public final Integer pg;
    /**
     * Constructor. Create a page id structure for a specific page of a
     * specific table.
     *
     * @param tableId The table that is being referenced
     * @param pgNo    The page number in that table.
     */
    public HeapPageId(int tableId, int pgNo) {
        this.tid = tableId;
        this.pg = pgNo;
    }

    /**
     * @return the table associated with this PageId
     */
    public int getTableId() {
        return tid;
    }

    /**
     * @return the page number in the table getTableId() associated with
     *         this PageId
     */
    public int getPageNumber() {
        return this.pg;
    }

    /**
     * @return a hash code for this page, represented by a combination of
     *         the table number and the page number (needed if a PageId is used as a
     *         key in a hash table in the BufferPool, for example.)
     * @see BufferPool
     */
    public int hashCode() {
        int hc = Integer.hashCode(this.tid);
        hc += Integer.hashCode(this.pg);
        return hc;
    }

    /**
     * Compares one PageId to another.
     *
     * @param o The object to compare against (must be a PageId)
     * @return true if the objects are equal (e.g., page numbers and table
     *         ids are the same)
     */
    public boolean equals(Object o) {
        if (o instanceof PageId) {
            PageId p = (PageId) o;
            return p.getPageNumber() == this.pg && p.getTableId() == this.tid;
        }
        return false;
    }

    /**
     * Return a representation of this object as an array of
     * integers, for writing to disk.  Size of returned array must contain
     * number of integers that corresponds to number of args to one of the
     * constructors.
     */
    public int[] serialize() {
        int[] data = new int[2];

        data[0] = getTableId();
        data[1] = getPageNumber();

        return data;
    }

}
