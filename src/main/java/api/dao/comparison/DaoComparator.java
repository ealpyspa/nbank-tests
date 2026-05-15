package api.dao.comparison;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class DaoComparator {
    private final DaoComparisonConfigLoader configLoader;

    public DaoComparator() {
        this.configLoader = new DaoComparisonConfigLoader("dao-comparison.properties");
    }

    public void compare(Object apiResponse, Object dao) {
        DaoComparisonConfigLoader.DaoComparisonRule rule = configLoader.getRuleFor(apiResponse.getClass());

        if (rule == null) {
            throw new RuntimeException("No comparison rule found for " + apiResponse.getClass().getSimpleName());
        }

        Map<String, String> fieldMappings = rule.getFieldMappings();

        for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
            String apiFieldName = mapping.getKey();
            String daoFieldName = mapping.getValue();

            Object apiValue = getFieldValue(apiResponse, apiFieldName);
            Object daoValue = getFieldValue(dao, daoFieldName);

            if (!areEqual(apiValue, daoValue)) {
                throw new AssertionError(String.format(
                        "Field mismatch for %s -> %s: API=%s, DAO=%s",
                        apiFieldName, daoFieldName, apiValue, daoValue
                ));
            }
        }
    }

    private boolean areEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;

        if (left instanceof Number && right instanceof Number) {
            double l = ((Number) left).doubleValue();
            double r = ((Number) right).doubleValue();
            return Math.abs(l - r) < 0.0001;
        }

        return Objects.equals(left, right);
    }

    private Object getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to get field value: " + fieldName,
                        e);
            }
        }

        throw new RuntimeException(
                "Field not found: " + fieldName + " in class " +
                        obj.getClass().getName()
        );
    }
}
