package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see HeapPage#HeapPage
 */
public class HeapFile implements DbFile {
    private int id;
    private File file;
    private TupleDesc td;

    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        // TODO: some code goes here
        this.file = f;
        this.id = f.getAbsoluteFile().hashCode();
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // TODO: some code goes here
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // TODO: some code goes here
        return this.id;
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {

        if (pid.getTableId() != this.getId()){
            throw new IllegalArgumentException();
        }

        //create new Heap Page ID for construction new Heap Page
        HeapPageId heapPageID = new HeapPageId(pid.getTableId(), pid.getPageNumber());
        RandomAccessFile randAccessFile;

        try{
            //open random access connection to our file
            randAccessFile = new RandomAccessFile(this.file,"r");
        }
        catch (FileNotFoundException ex){
            return null;
        }
        try{
            //create array of page size to store data and read page starting at correct offset
            byte[] data = new byte[BufferPool.getPageSize()];
            int offset = pid.getPageNumber() * BufferPool.getPageSize();
            randAccessFile.seek(offset);
            randAccessFile.readFully(data);
            randAccessFile.close();
            return new HeapPage(heapPageID, data);
        }
        catch (IOException ex){
            return null; 
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // TODO: some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        return (int)Math.ceil(this.file.length()/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        return new DbFileIterator(){
            private int pgNo = -1;
            private Iterator<Tuple> pageit;

            private Iterator<Tuple> getPageIterator(int pgNo) throws TransactionAbortedException, DbException {

                HeapPageId pid = new HeapPageId(getId(), pgNo);
                BufferPool bp = Database.getBufferPool();
                Page p = bp.getPage(tid, pid, Permissions.READ_ONLY);
                return ((HeapPage) p).iterator();
            }

            public void open() throws DbException, TransactionAbortedException {
                pgNo = 0;
                pageit = getPageIterator(pgNo);
            }

            public void close() {
                pgNo = -1;
                pageit = null;
            }

            public void rewind() throws DbException, TransactionAbortedException{
                pgNo = 0;
                pageit = getPageIterator(pgNo);
            }

            public boolean hasNext() throws DbException, TransactionAbortedException {
                if (pageit == null) {
                    return false;
                }
                if (pageit.hasNext()) {
                    return true;
                }
                while (pgNo + 1 < numPages()) {
                    pageit = getPageIterator(pgNo += 1);
                    if (pageit.hasNext()){
                        return true;
                    }
                }
                return false;
            }

            public Tuple next() throws DbException, TransactionAbortedException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                if(pageit.hasNext()) {
                    return pageit.next();
                } else {
                    return null;
                }
            }

        };
    }
}