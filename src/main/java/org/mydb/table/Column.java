package org.mydb.table;

import org.mydb.table.datatype.DataType;

/**
 * Table column.
 */
public class Column {
    private String name;
    private int precision;
    private int scale;
    private DataType dataType;

    public static class ColumnBuilder {
        private String name;
        private int precision;
        private int scale;
        private DataType dataType;

        public ColumnBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ColumnBuilder setPrecision(int precision) {
            this.precision = precision;
            return this;
        }

        public ColumnBuilder setScale(int scale) {
            this.scale = scale;
            return this;
        }

        public ColumnBuilder setDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Column build() {
            Column column = new Column();
            column.setName(name);
            column.setPrecision(precision);
            column.setScale(scale);
            column.setDataType(dataType);
            return column;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}
