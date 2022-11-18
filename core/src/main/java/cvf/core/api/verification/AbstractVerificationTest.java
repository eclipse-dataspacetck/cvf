package cvf.core.api.verification;

import cvf.core.system.SystemBootstrapExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Base class for verification tests. Uses the system bootstrap extension.
 */
@ExtendWith(SystemBootstrapExtension.class)
public abstract class AbstractVerificationTest {
}
