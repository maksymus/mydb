package org.mydb.util.factory;

/**
 * Factory interface
 */
@FunctionalInterface
public interface ObjectFactory <T> {
    /**
     * Create object
     */
    T getObject();

    /**
     * Get Object class.
     * @return
     */
    default Class<T> getObjectClass() {
        return (Class<T>) ((T) this).getClass();
    }
}
