package com.data_management;

import java.io.IOException;

public interface DataReader {

    /**
     * Reads data from a source and stores it in the provided DataStorage.
     * For file-based readers this loads all records at once.
     * For streaming readers this establishes a connection and begins receiving data.
     *
     * @param dataStorage the storage where parsed records will be added
     * @throws IOException if the data source cannot be accessed
     */
    void readData(DataStorage dataStorage) throws IOException;

    /**
     * Establishes a connection to the data source if one is required.
     * File-based implementations may leave this empty.
     *
     * @throws IOException if the connection cannot be established
     */
    default void connect() throws IOException {}
}
