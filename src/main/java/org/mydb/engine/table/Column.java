package org.mydb.engine.table;

import org.mydb.engine.table.datatype.DataType;
import org.mydb.engine.table.datatype.WithPrecision;
import org.mydb.engine.table.datatype.WithScale;

import java.util.Optional;

/**
 * Table column.
 */
public class Column {
    private String name;
    private DataType dataType;
    private Optional<Integer> precision = Optional.empty();
    private Optional<Integer> scale = Optional.empty();

    public Column(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;

        if (dataType instanceof WithPrecision) {
            int defaultPrecision = ((WithPrecision) dataType).defaultPrecision();
            precision = Optional.of(defaultPrecision);
        }

        if (dataType instanceof WithScale) {
            int defaultScale = ((WithScale) dataType).defaultScale();
            scale = Optional.of(defaultScale);
        }
    }

    public static class ColumnBuilder {
        private String name;
        private DataType dataType;
        private Optional<Integer> precision = Optional.empty();
        private Optional<Integer> scale = Optional.empty();

        public ColumnBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ColumnBuilder setPrecision(int precision) {
            this.precision = Optional.of(precision);
            return this;
        }

        public ColumnBuilder setScale(int scale) {
            this.scale = Optional.of(scale);
            return this;
        }

        public ColumnBuilder setDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Column build() {
            Column column = new Column(name, dataType);

            precision.ifPresent(column::setPrecision);
            scale.ifPresent(column::setScale);

            return column;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getPrecision() {
        return precision.orElse(0);
    }

    public void setPrecision(int precision) {
        if (!(dataType instanceof WithPrecision))
            return;

        WithPrecision withPrecision = (WithPrecision) this.dataType;
        int maxPrecision = withPrecision.maxPrecision();

        if (maxPrecision < precision) {
            throw new TableException(String.format("precision %d exceeds max value %d", precision, maxPrecision));
        }

        this.precision = Optional.of(precision);
    }

    public int getScale() {
        return scale.orElse(0);
    }

    public void setScale(int scale) {
        if (!(dataType instanceof WithScale))
            return;

        this.scale = Optional.of(scale);
    }
}
