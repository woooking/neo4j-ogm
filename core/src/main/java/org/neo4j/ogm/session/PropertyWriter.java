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
package org.neo4j.ogm.session;

import org.neo4j.ogm.metadata.FieldInfo;

/**
 * Contract for writing properties to entities.
 *
 * @param <T> Type of the entity who's properties are written to
 * @author Michael J. Simons
 * @soundtrack Queen - Bohemian Rhapsody: The Original Soundtrack
 * @since 3.2
 */
public interface PropertyWriter<T> {

    T getInstance();

    /**
     * Shall write {@code value} to the property named {@code propertyName} of the entity {@code instance}. If the
     * instance is recreated during the process, return the new instance.
     *
     * @param propertyName   The name of the property
     * @param targetProperty The field describing the property
     * @param value          The new value to write
     * @return Possible new instance of the same entity.
     */
    void writeTo(String propertyName, FieldInfo targetProperty, Object value);
}
