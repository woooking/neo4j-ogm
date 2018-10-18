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
 * Default implementation of a property writer. Uses the reflection based writing mechanism present.
 *
 * @param <T> Type of the entity who's properties are written to
 * @author Michael J. Simons
 * @soundtrack Queen - Bohemian Rhapsody: The Original Soundtrack
 * @since 3.2
 */
final class DefaultPropertyWriter<T> implements PropertyWriter<T> {

    private final T instance;

    DefaultPropertyWriter(T instance) {
        this.instance = instance;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    @Override
    public void writeTo(String propertyName, FieldInfo targetProperty, Object value) {
        if (targetProperty != null) {
            targetProperty.writeDirect(instance, value);
        }
    }
}
