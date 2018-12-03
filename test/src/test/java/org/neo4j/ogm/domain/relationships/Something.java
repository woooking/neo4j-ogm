package org.neo4j.ogm.domain.relationships;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Something {
    @Id @GeneratedValue
    private Long id;

    private String name;

    private ConnectionBetweenThings connectionBetweenThings;

    /*
    @Relationship("CONNECTION")
    private ConnectionBetweenThings connectionBetweenThings;


    @Relationship("CONNECTIONFOOBAR")
    private ConnectionBetweenThings anotherConnectionBetweenThings;
*/

    Something() {
    }

    public Long getId() {
        return id;
    }

    public Something(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ConnectionBetweenThings getConnectionBetweenThings() {
        return connectionBetweenThings;
    }

    public void setConnectionBetweenThings(
        ConnectionBetweenThings connectionBetweenThings) {
        this.connectionBetweenThings = connectionBetweenThings;
    }
}
