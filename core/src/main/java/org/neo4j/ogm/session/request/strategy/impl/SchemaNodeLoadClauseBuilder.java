/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.request.strategy.impl;

import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.neo4j.ogm.metadata.schema.Node;
import org.neo4j.ogm.metadata.schema.Relationship;
import org.neo4j.ogm.metadata.schema.Schema;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * Schema based load clause builder for nodes - starts from given node variable
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SchemaNodeLoadClauseBuilder extends AbstractSchemaLoadClauseBuilder implements LoadClauseBuilder {

    public SchemaNodeLoadClauseBuilder(Schema schema) {
        super(schema);
    }

    public String build(String variable, String label, int depth) {
        if (depth < 0) {
            return createExplicitMatchAndReturnClauses(label, variable, depth);
        }

        StringBuilder sb = new StringBuilder();

        newLine(sb);

        sb.append(" RETURN ");
        newLine(sb);
        sb.append(variable);
        newLine(sb);

        Node node = schema.findNode(label);
        expand(sb, variable, node, depth);

        return sb.toString();
    }

    String createExplicitMatchAndReturnClauses(String label, String variable, int maxDepth) {

        StringBuilder loadClause = new StringBuilder();
        RelationshipIterator iterator = new RelationshipIterator(schema.findNode(label), variable, maxDepth);
        while (iterator.hasNext()) {
            loadClause.append(" ").append(iterator.next());
        }

        String returnClause;
        int numberOfGeneratedPaths = iterator.getNumberOfGeneratedPaths();
        if (iterator.getNumberOfGeneratedPaths() == 0) {
            returnClause = " RETURN " + variable;
        } else {
            returnClause = Stream.iterate(0, i -> i + 1).limit(numberOfGeneratedPaths)
                .map(i -> "collect(p" + i + ")")
                .collect(joining("+", " RETURN n, ", ""));
        }
        loadClause.append(returnClause);

        return loadClause.toString();
    }

    static class RelationshipIterator implements Iterator<String> {

        private final String variable;
        private final int maxDepth;
        private final Set<Node> visitedSourceNode = new HashSet<>();
        private final boolean infiniteDepth;

        private List<Relationship> traversableRelationships;
        private int depth = 0;
        private AtomicInteger pathCounter = new AtomicInteger(0);
        private AtomicInteger varCounter = new AtomicInteger(0);

        RelationshipIterator(Node startNode, String variable, int maxDepth) {

            this.traversableRelationships = extractRelationships(startNode).collect(toList());
            this.variable = variable;

            this.infiniteDepth = maxDepth < 0;
            this.maxDepth = maxDepth;
        }

        private static Stream<Relationship> extractRelationships(Node node) {
            return node.relationships().values().stream();
        }

        private static String toTypePattern(Relationship relationship) {
            return ":`" + relationship.type() + "`";
        }

        public int getNumberOfGeneratedPaths() {
            return pathCounter.get();
        }

        @Override
        public boolean hasNext() {
            return (this.infiniteDepth || this.depth < this.maxDepth) && !traversableRelationships.isEmpty();
        }

        @Override
        public String next() {

            List<Relationship> standard = traversableRelationships;
            List<Relationship> selfReferential = Collections.emptyList();

            final StringBuilder m = new StringBuilder();
            if (infiniteDepth) {
                Map<Boolean, List<Relationship>> f = traversableRelationships.stream()
                    .collect(partitioningBy(Relationship::isSelfReferential));

                standard = f.get(false);
                selfReferential = f.get(true);
            }

            int x = varCounter.get();
            String currentVariable = variable + (x == 0 ? "" : Integer.toString(x));

            Supplier<String> prefixSupplier = () -> String
                .format("MATCH p%d=(%s)-[", pathCounter.getAndIncrement(), currentVariable);

            if (!standard.isEmpty()) {

                String suffix = String.format("*0..1]-(%s%d)", variable, varCounter.incrementAndGet());
                m.append(standard.stream().map(RelationshipIterator::toTypePattern).sorted()
                    .collect(joining("|", prefixSupplier.get(), suffix)));
            }

            selfReferential.forEach(relationship -> {

                m.append(prefixSupplier.get())
                    .append(toTypePattern(relationship))
                    .append("*0..]-()");
            });

            // Store all newly visited nodes.
            this.visitedSourceNode
                .addAll(this.traversableRelationships.stream().map(Relationship::start).collect(toList()));

            // Recompute the next, traversable relationships
            this.traversableRelationships = this.traversableRelationships.stream()
                .map(r -> r.other(r.start()))
                .distinct()
                .filter(n -> !visitedSourceNode.contains(n))
                .flatMap(RelationshipIterator::extractRelationships)
                .collect(toList());



            depth += 1;
            return m.toString();
        }
    }
}
