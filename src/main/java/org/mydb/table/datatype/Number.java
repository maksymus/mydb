package org.mydb.table.datatype;

/**
 * Number data type.
 */
public class Number extends DataType implements WithPrecision, WithScale {
    @Override
    public int maxPrecision() {
        return 10;
    }

    @Override
    public int defaultPrecision() {
        return 10;
    }

    @Override
    public int defaultScale() {
        return 0;
    }
}
