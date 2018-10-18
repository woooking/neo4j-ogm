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
package org.neo4j.ogm.context;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.PropertyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for populating the properties of an entity. It decouples information retrieval for fields,
 * necessary transformations and the writing process.
 * <br>
 * The idea is to pass the lookups for the target field and also the lookups from that field to specific conversions
 * from the graph-to-entity-mapper in charge and asking the {@link org.neo4j.ogm.session.EntityInstantiator} for a
 * {@link PropertyWriter} as that may depends on how the entity has been initialized in the first place.
 * <br>
 * After populating all properties, it's paramount to use the {@link #getPopulatedEntity() populated entity} from there
 * on as the instance may has changed in the process.
 *
 * @param <T> Type of the entity to populate
 * @author Michael J. Simons
 * @soundtrack Queen - Bohemian Rhapsody: The Original Soundtrack
 * @since 3.2
 */
final class EntityPopulator<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityPopulator.class);

    private final Function<String, FieldInfo> fieldInfoLookup;
    private final Function<T, PropertyWriter<T>> propertyWriterSupplier;

    EntityPopulator(
        Function<String, FieldInfo> fieldInfoLookup,
        Function<T, PropertyWriter<T>> propertyWriterSupplier
    ) {
        this.fieldInfoLookup = fieldInfoLookup;
        this.propertyWriterSupplier = propertyWriterSupplier;
    }

    public PropertyWriterFacade populate(T entity) {
        return new PropertyWriterFacade(propertyWriterSupplier.apply(entity));
    }

    class PropertyWriterFacade {

        private final PropertyWriter<T> propertyWriter;
        private final Function<FieldInfo, Function<Object, Object>> applicableFieldTransformationsLookup;

        private PropertyWriterFacade(PropertyWriter<T> propertyWriter) {
            this(propertyWriter, fieldInfo -> Function.identity());
        }

        private PropertyWriterFacade(PropertyWriter<T> propertyWriter,
            Function<FieldInfo, Function<Object, Object>> applicableFieldTransformationsLookup) {
            this.propertyWriter = propertyWriter;
            this.applicableFieldTransformationsLookup = applicableFieldTransformationsLookup;
        }

        public PropertyWriterFacade using(Function<FieldInfo, Function<Object, Object>> fieldConversions) {
            return new PropertyWriterFacade(propertyWriter, fieldConversions);
        }

        T with(Map<String, Object> properties) {
            properties.forEach(this::write);
            return propertyWriter.getInstance();
        }

        T with(List<Property<String, Object>> properties) {
            properties.forEach(this::write);
            return propertyWriter.getInstance();
        }

        private void write(Property<String, Object> property) {
            write(property.getKey(), property.getValue());
        }

        private void write(String propertyName, Object newValue) {
            FieldInfo targetFieldInfo = fieldInfoLookup.apply(propertyName);
            Object valueToWrite = newValue;
            if (targetFieldInfo == null) {
                LOGGER.debug("Unable to find field info for property: {} on class: {}", propertyName,
                    propertyWriter.getInstance().getClass().getName());
            } else {
                valueToWrite = applicableFieldTransformationsLookup.apply(targetFieldInfo).apply(newValue);
            }

            propertyWriter.writeTo(propertyName, targetFieldInfo, valueToWrite);
        }
    }
}
