package cvf.core.api.system;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Denotes a configuration parameter whose value is injected from an environment variable.
 * <p/>
 * The environment variable key is constructed by concatenating the currently executing test method name and the field name using the `_` character and converting to uppercase.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigParam {
    /**
     * If the value is required.
     */
    boolean required() default false;
}

