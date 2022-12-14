package cvf.core.api.system;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A test that is required for conformance.
 */
@Inherited
@Retention(RUNTIME)
@Target(METHOD)
@Test
@Tag("mandatory")
public @interface MandatoryTest {

}
