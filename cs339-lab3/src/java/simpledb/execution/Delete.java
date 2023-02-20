package simpledb.execution;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Type;
import simpledb.storage.BufferPool;
import simpledb.storage.IntField;
import simpledb.storage.Tuple;
import simpledb.storage.TupleDesc;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;
    private TransactionId t;
    private OpIterator child;
    private TupleDesc deleteTd;
    private boolean done;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     *
     * @param t     The transaction this delete runs in
     * @param child The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, OpIterator child) {
        this.t = t;
        this.child = child;
        this.deleteTd = new TupleDesc(new Type[]{Type.INT_TYPE});
      }

    public TupleDesc getTupleDesc() {
        return deleteTd;
    }

    public void open() throws DbException, TransactionAbortedException {
        super.open();
        child.open();
        done = false;
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
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     *
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (!done) {
          int cnt = 0;
          try {
            while (child.hasNext()) {
              Database.getBufferPool().deleteTuple(t, child.next());
              ++cnt;
            }
          } catch (DbException | IOException e) {
            e.printStackTrace();
          }
          Tuple res = new Tuple(deleteTd);
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
