package cvf.ids;

import cvf.core.api.verification.AbstractVerificationTest;
import cvf.ids.system.api.connector.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

/**
 *
 */
@Tag("ids-cn")
public abstract class AbstractNegotiationVerificationTest extends AbstractVerificationTest {
    protected static final int WAIT_SECONDS = 15;

    protected Connector connector;

    @BeforeEach
    void setUp() {
        connector = new Connector();
    }
}
