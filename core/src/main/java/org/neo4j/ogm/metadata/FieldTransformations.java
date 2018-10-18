package org.neo4j.ogm.metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.utils.ClassUtils;

/**
 * This transformations are applied when writing database content to fields of entities.
 *
 * @author Michael J. Simons
 * @soundtrack Helge Schneider - Heart Attack No. 1
 * @since 3.1.5
 */
public final class FieldTransformations {
    public static Object applyFieldConversionOrCoerceIfNecessary(final FieldInfo targetFieldInfo, final Object propertyValue) {

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
    }

    public static Object mergeAndCoercePossibleArray(final FieldInfo targetFieldInfo, final Object propertyValue) {

        ClassInfo classInfo = targetFieldInfo.containingClassInfo();
        String propertyName = targetFieldInfo.propertyName();
        Object mergedAndCoercedValue = propertyValue;
        if (mergedAndCoercedValue != null && mergedAndCoercedValue.getClass().isArray()) {
            mergedAndCoercedValue = Arrays.asList((Object[]) mergedAndCoercedValue);
        }
        Class<?> paramType = targetFieldInfo.type();
        if (paramType.isArray() || Iterable.class.isAssignableFrom(paramType)) {
            Class elementType = underlyingElementType(propertyName, classInfo);
            mergedAndCoercedValue = paramType.isArray()
                ? EntityAccessManager.merge(paramType, mergedAndCoercedValue, new Object[] {}, elementType)
                : EntityAccessManager.merge(paramType, mergedAndCoercedValue, Collections.EMPTY_LIST, elementType);
        }
        return mergedAndCoercedValue;
    }

    private static Class underlyingElementType(String propertyName, ClassInfo classInfo) {

        return Optional.ofNullable(fieldInfoForPropertyName(propertyName, classInfo))
            .map(FieldInfo::getTypeDescriptor)
            .<Class>map(ClassUtils::getType)
            .orElseGet(() -> classInfo.getUnderlyingClass());
    }

    private static FieldInfo fieldInfoForPropertyName(String propertyName, ClassInfo classInfo) {

        return Optional.ofNullable(classInfo.labelFieldOrNull())
            .filter(fieldInfo -> fieldInfo.getName().toLowerCase().equalsIgnoreCase(propertyName.toLowerCase()))
            .orElseGet(() -> classInfo.propertyField(propertyName));
    }

    private FieldTransformations() {
    }
}
