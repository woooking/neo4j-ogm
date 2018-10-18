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

package org.neo4j.ogm.metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.utils.ClassUtils;

/**
 * This conversions are applied when writing database content to fields of entities.
 *
 * @author Michael J. Simons
 * @soundtrack Helge Schneider - Heart Attack No. 1
 * @since 3.2
 */
public final class DefaultFieldConversions {

    public static Function<Object, Object> applyFieldConversionOrCoerceIfNecessary(final FieldInfo targetFieldInfo) {

        return propertyValue -> {
            Object finalValue;
            if (targetFieldInfo.hasPropertyConverter()) {
                finalValue = targetFieldInfo.getPropertyConverter().toEntityAttribute(propertyValue);
            } else if (targetFieldInfo.isScalar()) {
                String descriptor = targetFieldInfo.getTypeDescriptor();
                finalValue = Utils.coerceTypes(ClassUtils.getType(descriptor), propertyValue);
            } else {
                finalValue = propertyValue;
            }
            return finalValue;
        };
    }

    public static Function<Object, Object> mergeAndCoercePossibleArray(final FieldInfo targetFieldInfo) {

        return propertyValue -> {
            ClassInfo classInfo = targetFieldInfo.containingClassInfo();
            String propertyName = targetFieldInfo.propertyName();
            Object mergedAndCoercedValue = propertyValue;
            if (mergedAndCoercedValue != null && mergedAndCoercedValue.getClass().isArray()) {
                mergedAndCoercedValue = Arrays.asList((Object[]) mergedAndCoercedValue);
            }
            Class<?> paramType = targetFieldInfo.type();
            if (paramType.isArray() || Iterable.class.isAssignableFrom(paramType)) {
                Class elementType = underlyingElementType(classInfo, propertyName);
                mergedAndCoercedValue = paramType.isArray()
                    ? EntityAccessManager.merge(paramType, mergedAndCoercedValue, new Object[] {}, elementType)
                    : EntityAccessManager.merge(paramType, mergedAndCoercedValue, Collections.EMPTY_LIST, elementType);
            }
            return mergedAndCoercedValue;
        };
    }

    private static Class underlyingElementType(ClassInfo classInfo, String propertyName) {

        return Optional.ofNullable(fieldInfoForPropertyName(classInfo, propertyName))
            .map(FieldInfo::getTypeDescriptor)
            .<Class>map(ClassUtils::getType)
            .orElseGet(() -> classInfo.getUnderlyingClass());
    }

    private static FieldInfo fieldInfoForPropertyName(ClassInfo classInfo, String propertyName) {

        return Optional.ofNullable(classInfo.labelFieldOrNull())
            .filter(fieldInfo -> fieldInfo.getName().toLowerCase().equalsIgnoreCase(propertyName.toLowerCase()))
            .orElseGet(() -> classInfo.propertyField(propertyName));
    }

    private DefaultFieldConversions() {
    }
}
