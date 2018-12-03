package org.neo4j.ogm.metadata;

public class RelationshipDescription {
    private final String type;

    private final String description;

    public RelationshipDescription(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
