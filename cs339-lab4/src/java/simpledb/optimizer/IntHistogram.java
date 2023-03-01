package simpledb.optimizer;

import simpledb.execution.Predicate;

/**
 * A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {


    private int numb; // number of buckets
    private int min;
    private int max;
    private int ntups;
    private int[] height;
    private int[] left;
    private int[] right;
    /**
     * Create a new IntHistogram.
     * <p>
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * <p>
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * <p>
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't
     * simply store every value that you see in a sorted list.
     *
     * @param buckets The number of buckets to split the input value into.
     * @param min     The minimum integer value that will ever be passed to this class for histogramming
     * @param max     The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
        this.numb = buckets;
        this.min = min;
        this.max = max;
        this.height = new int[numb];
        this.left = new int[numb];
        this.right = new int[numb];

        int width = (this.max - this.min + 1) / this.numb;

        for (int i = 0; i < this.numb; i++) {
            this.left[i] = (i * width) + this.min; // finding the lower bound of bucket i
            if (i > 0) {
                this.right[i - 1] = this.left[i] - 1;
            }
        }
        this.right[numb - 1] = max;
    }

    public int findBucket(int v) {
        if (v < min) {
            return -1;
        } else if (v > max) {
            return numb;
        }
        int low = 0;
        int high = numb - 1;
        int mid = 0;

        while (low <= high) {
            mid = (low + high) / 2;

            if (v >= left[mid] && v <= right[mid]){
                return mid;
            } else if (v < left[mid]) {
                high = mid - 1;
            } else if (v > right[mid]) {
                low = mid + 1;
            }
        }
        return 0;

    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     *
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
        int bucket = findBucket(v);
        height[bucket] += 1;
        ntups += 1;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * <p>
     * For example, if "op" is "GREATER_THAN" and "v" is 5,
     * return your estimate of the fraction of elements that are greater than 5.
     *
     * @param op Operator
     * @param v  Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

        int bucket = findBucket(v);

        // keep track of elements that are either greater, less than, or equal to v
        // based on the operator
        double less = 0;
        double eq = 0;
        double greater = 0;
        for (int i = 0; i < numb; i++) {
            if (i < bucket) {
                // add the height of bucket i to less tracker
                less += ((double) height[i]) / ntups;
            } else if (i > bucket) {
                greater += ((double) height[i]) / ntups;
            } else {
                int width = right[bucket] - left[bucket] + 1;
                eq += (((double) height[bucket]) / width) / ntups;
                less += (height[bucket] / ntups) * ((v - left[i]) / width);
                greater += (height[bucket] / ntups) * ((right[i] - v) / width);
            }
        }

        if (op == Predicate.Op.EQUALS) {
            return eq;
        } else if (op == Predicate.Op.LESS_THAN) {
            return less;
        } else if (op == Predicate.Op.LESS_THAN_OR_EQ) {
            return less + eq;
        } else if (op == Predicate.Op.GREATER_THAN) {
            return greater;
        } else if (op == Predicate.Op.GREATER_THAN_OR_EQ) {
            return greater + eq;
        } else if (op == Predicate.Op.NOT_EQUALS) {
            return 1 - eq;
        } else if (op == Predicate.Op.LIKE) {
            return eq;
        } 
        return -1.0;
    }

    /**
     * @return the average selectivity of this histogram.
     *         <p>
     *         This is not an indispensable method to implement the basic
     *         join optimization. It may be needed if you want to
     *         implement a more efficient optimization
     */
    public double avgSelectivity() {
        // TODO: some code goes here
        return 1.0;
    }

    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        String res = "";
        for (int i = 0; i < numb; i++) {
            res += left[i] + ", " + right[i] + "; " + height[i] + "\n";
        }
        return res;
    }
}
