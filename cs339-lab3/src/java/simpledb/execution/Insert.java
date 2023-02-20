package simpledb.execution;

import simpledb.transaction.TransactionId;

import java.io.IOException;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;
import simpledb.storage.BufferPool;
import simpledb.storage.IntField;
import simpledb.storage.Tuple;
import simpledb.common.Type;

/**
 * Inserts tuples read from the child operator into the tableId specified in the
 * constructor
 */
public class Insert extends Operator {

    private static final long serialVersionUID = 1L;
    private TransactionId t;
    private OpIterator child;
    private int tableId;
    private TupleDesc insertTd;
    private boolean done;

    /**
     * Constructor.
     *
     * @param t       The transaction running the insert.
     * @param child   The child operator from which to read tuples to be inserted.
     * @param tableId The table in which to insert tuples.
     * @throws DbException if TupleDesc of child differs from table into which we are to
     *                     insert.
     */
    public Insert(TransactionId t, OpIterator child, int tableId)
            throws DbException {
                this.t = t;
                this.child = child;
                this.tableId = tableId;
                this.insertTd = new TupleDesc(new Type[]{Type.INT_TYPE});
              }

    public TupleDesc getTupleDesc() {
        return insertTd;
    }

    public void open() throws DbException, TransactionAbortedException {
        super.open();
        child.open();
    }

    public void close() {
        child.close();
        super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        close();
        open();
    }

    /**
     * Inserts tuples read from child into the tableId specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (!done) {
          int cnt = 0;
          try {
            while (child.hasNext()) {
              Database.getBufferPool().insertTuple(t, tableId, child.next());
              ++cnt;
            }
          } catch (DbException | IOException e) {
            e.printStackTrace();
          }
          Tuple res = new Tuple(insertTd);
          res.setField(0, new IntField(cnt));
          done = true;
          return res;
        } else {
          return null;
        }
      }

    @Override
    public OpIterator[] getChildren() {
        return new OpIterator[]{child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        child = children[0];
    }
}
