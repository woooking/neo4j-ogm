/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session;

import java.util.Map;
import java.util.function.Function;

/**
 * Interface to be implemented to override entity instances creation.
 * This is mainly designed for SDN, Spring data commons having some infrastructure code to do fancy
 * object instantiation using persistence constructors and ASM low level bytecode generation.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 * @since 3.1
 */
public interface EntityInstantiator {

    /**
     * Creates an instance of a given class.
     *
     * @param clazz          The class to materialize.
     * @param propertyValues Properties of the object (needed for constructors with args)
     * @param <T>            Type to create
     * @return The created instance.
     */
    <T> T createInstance(Class<T> clazz, Map<String, Object> propertyValues);

    /**
     * This methods shall return true if an instance of a class instantiated by this instantiator needs
     * further population after instantiation.
     *
     * @param clazz The class that is checked whether it requires further population or not after being instantiated
     * @param <T>
     * @return true by default
     * @since 3.2
     */
    default <T> boolean needsFurtherPopulation(Class<T> clazz, @SuppressWarnings("unused") T instance) {
        return true;
    }

    /**
     * @param <T>
     * @return The write to use if a new entity needs further population
     * @since 3.2
     */
    default <T> Function<T, PropertyWriter<T>> getPropertyWriterSupplier() {
        return initialInstance ->  new DefaultPropertyWriter<T>(initialInstance);
    }
}
