package org.neo4j.ogm.domain.relationships;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity("CONNECTION")
public class ConnectionBetweenThings {
    @Id @GeneratedValue
    private Long id;

    private String name;

    @StartNode
    private Something from;

    @EndNode
    private Something to;

    ConnectionBetweenThings() {
    }

    public ConnectionBetweenThings(String name, Something from, Something to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public Something getFrom() {
        return from;
    }

    public Something getTo() {
        return to;
    }
}
