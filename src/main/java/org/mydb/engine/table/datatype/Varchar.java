package org.mydb.engine.table.datatype;

/**
 * Varchar data type
 */
public class Varchar extends DataType implements WithPrecision {
    @Override
    public int maxPrecision() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int defaultPrecision() {
        return Integer.MAX_VALUE;
    }
}
