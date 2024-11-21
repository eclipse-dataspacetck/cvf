package org.eclipse.dataspacetck.rendering.spi;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface TestPlanRenderer {

    void title(String title);

    void category(String category);

    void testSuite(String displayName);

    void testCase(String displayName, boolean isMandatory, String testNumber, @Nullable String specUrl, String mermaidDiagram);

    void testCase(String displayName, boolean isMandatory, String testNumber, @Nullable String specUrl, Path diagramImage);

    String render();

    void subTitle(String subTitle);
}
