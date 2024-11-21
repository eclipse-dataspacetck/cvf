package org.eclipse.dataspacetck.document;

import org.eclipse.dataspacetck.document.model.Category;
import org.eclipse.dataspacetck.document.model.TestMethod;
import org.eclipse.dataspacetck.document.model.TestSuite;
import org.eclipse.dataspacetck.rendering.markdown.TestPlanRendererImpl;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TestPlanRendererImplTest {

    private final TestPlanRendererImpl renderer = new TestPlanRendererImpl();

    @Test
    void title() {
        renderer.title("test title");
        assertThat(renderer.render()).isEqualTo("# test title\n");
    }

    @Test
    void category() {
        renderer.category(new Category("test category"));
        assertThat(renderer.render()).isEqualTo("## Category `test category`\n");
    }

    @Test
    void testSuite() {
        renderer.testSuite(new TestSuite("test suite"));
        assertThat(renderer.render()).isEqualTo("### Test suite `test suite`\n");
    }

    @Test
    void testCase_withImage() throws URISyntaxException {
        var res = Thread.currentThread().getContextClassLoader().getResource("testimage.png").toURI();
        renderer.testCase("test case name", true, "C-12343", "https://foo.bar", Path.of(res));

        var render = renderer.render();
        assertThat(render).contains("**C-12343 (mandatory)**<br/>\n");
        assertThat(render).contains("Description: test case name<br/>\n");
        assertThat(render).contains("Test Number: `C-12343`<br/>\n");
        assertThat(render).contains("_View in the [DSP Specification](https://foo.bar)_<br/>\n");
        assertThat(render).contains("![C-12343](testimage.png)\n");
        assertThat(render).endsWith("\n");
    }

    @Test
    void testCase_directEmbed() {
        renderer.testCase(new TestMethod("method()", "test case name", "C-12343", false, "graph TD; A-->B;"));

        var render = renderer.render();
        assertThat(render).contains("**C-12343 (optional)**<br/>\n");
        assertThat(render).contains("Description: test case name<br/>\n");
        assertThat(render).contains("Test Number: `C-12343`<br/>\n");
        assertThat(render).contains("_View in the [DSP Specification](https://foo.bar/spec/C-12343)_<br/>\n");
        assertThat(render).contains("```mermaid");
        assertThat(render).endsWith("\n");
    }

    @Test
    void render() {
    }
}