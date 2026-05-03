package common.extension;

import api.configs.Config;
import common.annotations.APIVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public class APIVersionExtension implements ExecutionCondition {
    private static final String VERSION_PROPERTY = "testsVersion";
    private static final String DEFAULT_VERSION = "default";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        String currentVersion = getCurrentVersion();
        Optional<APIVersion> apiVersionAnnotation = findAnnotation(context);

        if (apiVersionAnnotation.isEmpty()) {
            return ConditionEvaluationResult.enabled(
                    "APIVersion not specified. Test is treated as common."
            );
        }

        String requiredVersion = apiVersionAnnotation.get().value();

        if (requiredVersion.equals(currentVersion)) {
            return ConditionEvaluationResult.enabled(
                    String.format(
                            "Test enabled for API version '%s'. Current version: '%s'.",
                            requiredVersion,
                            currentVersion
                    )
            );
        }

        return ConditionEvaluationResult.disabled(
                String.format(
                        "Test skipped. Required API version: '%s', current version: '%s'.",
                        requiredVersion,
                        currentVersion
                )
        );
    }

    private String getCurrentVersion() {
        String version = Config.getProperty(VERSION_PROPERTY);
        if (version == null || version.isBlank()) {
            return DEFAULT_VERSION;
        }
        return version.trim();
    }

    private Optional<APIVersion> findAnnotation(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        if (element.isPresent()) {
            APIVersion methodAnnotation = element.get().getAnnotation(APIVersion.class);
            if (methodAnnotation != null) {
                return Optional.of(methodAnnotation);
            }
        }

        Optional<Class<?>> testClass = context.getTestClass();
        if (testClass.isPresent()) {
            APIVersion classAnnotation = testClass.get().getAnnotation(APIVersion.class);
            if (classAnnotation != null) {
                return Optional.of(classAnnotation);
            }
        }

        return Optional.empty();
    }
}
