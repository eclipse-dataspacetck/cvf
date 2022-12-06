package cvf.core.api.system;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to inject a test service on a field in a test class.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Inject {
}
