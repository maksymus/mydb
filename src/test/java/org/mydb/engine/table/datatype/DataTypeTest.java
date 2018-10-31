package org.mydb.engine.table.datatype;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class DataTypeTest {

    @Test
    public void getDataType_number() {
        DataType number = DataType.getDataType("NUMBER");
        assertThat(number, Matchers.instanceOf(Number.class));
    }

    @Test
    public void getDataType_number_case() {
        DataType number = DataType.getDataType("NuMBEr");
        assertThat(number, Matchers.instanceOf(Number.class));
    }

    @Test
    public void getDataType_varchar() {
        DataType number = DataType.getDataType("varchar");
        assertThat(number, Matchers.instanceOf(Varchar.class));
    }

    @Test
    public void getDataType_date() {
        DataType number = DataType.getDataType("date");
        assertThat(number, Matchers.instanceOf(Date.class));
    }

    @Test
    public void getDataType_no_data_type() {
        DataType number = DataType.getDataType("missingtype");
        assertThat(number, Matchers.nullValue());
    }
}