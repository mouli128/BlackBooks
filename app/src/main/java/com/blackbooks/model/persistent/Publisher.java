package com.blackbooks.model.persistent;

import com.blackbooks.model.metadata.Column;
import com.blackbooks.model.metadata.Column.SQLiteDataType;
import com.blackbooks.model.metadata.Table;

import java.io.Serializable;

@Table(name = Publisher.NAME, version = 1)
public class Publisher implements Serializable {

    public static final String NAME = "PUBLISHER";

    private static final long serialVersionUID = -3478748810451913296L;

    @Column(name = Cols.PUB_ID, primaryKey = true, type = SQLiteDataType.INTEGER, version = 1)
    public Long id;

    @Column(name = Cols.PUB_NAME, mandatory = true, unique = true, type = SQLiteDataType.TEXT, version = 1)
    public String name;

    /**
     * Default constructor.
     */
    public Publisher() {
    }

    /**
     * Copy constructor.
     *
     * @param publisher Publisher.
     */
    public Publisher(Publisher publisher) {
        this();
        this.id = publisher.id;
        this.name = publisher.name;
    }

    public final static class Cols {
        public final static String PUB_ID = "PUB_ID";
        public final static String PUB_NAME = "PUB_NAME";
    }
}
