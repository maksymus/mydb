package org.mydb.engine.table.datatype;

import java.util.HashMap;
import java.util.Map;

/**
 * Supported data types
 */
public abstract class DataType {
    private static Map<String, DataType> dataTypes = new HashMap<>();

    static {
        dataTypes.put("number", new Number());
        dataTypes.put("date", new Date());
        dataTypes.put("varchar", new Varchar());
    }

    public static DataType getDataType(String value) {
        return dataTypes.get(value.toLowerCase());
    }
}

