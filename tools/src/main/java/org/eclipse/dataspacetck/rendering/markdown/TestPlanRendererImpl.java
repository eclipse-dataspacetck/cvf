package org.eclipse.dataspacetck.rendering.markdown;

import net.steppschuh.markdowngenerator.Markdown;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import org.eclipse.dataspacetck.document.model.Category;
import org.eclipse.dataspacetck.document.model.TestMethod;
import org.eclipse.dataspacetck.document.model.TestSuite;
import org.eclipse.dataspacetck.rendering.spi.TestPlanRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static net.steppschuh.markdowngenerator.Markdown.bold;
import static net.steppschuh.markdowngenerator.Markdown.codeBlock;
import static net.steppschuh.markdowngenerator.Markdown.image;
import static net.steppschuh.markdowngenerator.Markdown.italic;
import static net.steppschuh.markdowngenerator.Markdown.link;
import static net.steppschuh.markdowngenerator.Markdown.unorderedList;

public class TestPlanRendererImpl implements TestPlanRenderer {
    public static final String SEQUENCE_DIAGRAM = "sequenceDiagram";
    public static final String FOUR_SPACES_INDENT = "    ";
    private static final String NEWLINE = "\n";
    private static final String BR = "<br/>";
    private final StringBuilder stringBuilder = new StringBuilder();
    private boolean renderImage = false;

    public TestPlanRendererImpl() {
    }

    public TestPlanRendererImpl(boolean renderImage) {
        this.renderImage = renderImage;
    }

    @Override
    public void title(String title) {
        stringBuilder
                .append(heading(title, 0))
                .append(NEWLINE);
    }

    @Override
    public void category(Category category) {
        stringBuilder.append(heading("Category `" + category.name() + "`", 2))
                .append(NEWLINE);
    }

    @Override
    public void testSuite(TestSuite testSuite) {
        stringBuilder.append(heading("Test suite `" + testSuite.name() + "`", 3))
                .append(NEWLINE);
    }

    @Override
    public void testCase(TestMethod testMethod) {
        var baseItem = baseTestCase(testMethod.displayName(), testMethod.isMandatory(), testMethod.number(), testMethod.specUrl());

        var diagramCode = testMethod.diagramCode();

        if (diagramCode == null || diagramCode.isBlank()) {
            stringBuilder.append(baseItem);
            return;
        }
        if (!diagramCode.startsWith(SEQUENCE_DIAGRAM)) {
            diagramCode = SEQUENCE_DIAGRAM + NEWLINE + diagramCode;
        }

        String language;
        if (diagramCode.trim().startsWith("@startuml")) {
            language = "plantuml";
        } else {
            language = "mermaid";
        }

        baseItem.append(codeBlock(diagramCode, language))
                .append(NEWLINE);
        stringBuilder.append(unorderedList(baseItem.toString().replace(NEWLINE, NEWLINE + FOUR_SPACES_INDENT) + NEWLINE));

    }

    public void testCase(String displayName, boolean isMandatory, String testNumber, @Nullable String specUrl, Path diagramImage) {
        var baseItem = baseTestCase(displayName, isMandatory, testNumber, specUrl);


        baseItem.append(image(testNumber, diagramImage.getFileName().toString()))
                .append(NEWLINE);

        stringBuilder.append(unorderedList(baseItem.toString().replace(NEWLINE, NEWLINE + FOUR_SPACES_INDENT) + NEWLINE));
    }

    @Override
    public String render() {
        return stringBuilder.toString();
    }

    @Override
    public void subTitle(String subTitle) {
        stringBuilder.append(italic(subTitle))
                .append(NEWLINE)
                .append(NEWLINE);
    }

    private StringBuilder baseTestCase(String displayName, boolean isMandatory, String testNumber, @Nullable String specUrl) {

        var item = new StringBuilder();
        item.append(bold(testNumber + (isMandatory ? " (mandatory)" : " (optional)")))
                .append(BR)
                .append(NEWLINE).append("Description: ").append(displayName)
                .append(BR)
                .append(NEWLINE).append("Test Number: `").append(testNumber).append("`")
                .append(BR)
                .append(NEWLINE);

        if (specUrl != null) {
            item.append(italic("View in the " + link("DSP Specification", specUrl)))
                    .append(BR)
                    .append(NEWLINE);
        }

        return item;
    }

    @NotNull
    private Heading heading(String title, int level) {
        var heading = Markdown.heading(title, level);
        heading.setUnderlineStyle(false);
        return heading;
    }


}
