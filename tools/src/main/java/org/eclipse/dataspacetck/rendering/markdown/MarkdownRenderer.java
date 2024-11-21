/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.rendering.markdown;

import net.steppschuh.markdowngenerator.Markdown;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import org.eclipse.dataspacetck.document.model.Category;
import org.eclipse.dataspacetck.document.model.TestCase;
import org.eclipse.dataspacetck.document.model.TestSuite;
import org.eclipse.dataspacetck.rendering.mermaid.MermaidRenderer;
import org.eclipse.dataspacetck.rendering.plantuml.PlantumlRenderer;
import org.eclipse.dataspacetck.rendering.spi.TestPlanRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static net.steppschuh.markdowngenerator.Markdown.bold;
import static net.steppschuh.markdowngenerator.Markdown.codeBlock;
import static net.steppschuh.markdowngenerator.Markdown.image;
import static net.steppschuh.markdowngenerator.Markdown.italic;
import static net.steppschuh.markdowngenerator.Markdown.link;
import static net.steppschuh.markdowngenerator.Markdown.unorderedList;

/**
 * Renders a {@link org.eclipse.dataspacetck.document.model.TestGraph} and elements within into markdown.
 * This renderer can be configured to either embed Mermaid/Plantuml in the markdown, or to pre-render it to an image first, and
 * embed the diagram as image. Use the {@link Builder#preRenderImages(boolean)}, {@link Builder#baseFilePath(String)}
 * and {@link Builder#imageType(String)} to control this behaviour.
 */
public class MarkdownRenderer implements TestPlanRenderer {
    public static final String SEQUENCE_DIAGRAM = "sequenceDiagram";
    public static final String FOUR_SPACES_INDENT = "    ";
    private static final String NEWLINE = "\n";
    private static final String BR = "<br/>";
    private final StringBuilder stringBuilder = new StringBuilder();
    private String baseFilePath;
    private String imageType = "svg";
    private boolean preRenderImages;

    private MarkdownRenderer() {

    }

    @Override
    public void title(String title) {
        stringBuilder
                .append(heading(title, 0))
                .append(NEWLINE);
    }

    @Override
    public void subTitle(String subTitle) {
        stringBuilder.append(italic(subTitle))
                .append(NEWLINE)
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
    public void testCase(TestCase testCase) {
        var baseItem = baseTestCase(testCase.displayName(), testCase.isMandatory(), testCase.number(), testCase.specUrl());

        var diagramCode = testCase.diagramCode();

        if (diagramCode == null || diagramCode.isBlank()) {
            stringBuilder.append(baseItem);
            return;
        }
        if (!diagramCode.startsWith(SEQUENCE_DIAGRAM)) {
            diagramCode = SEQUENCE_DIAGRAM + NEWLINE + diagramCode;
        }
        if (preRenderImages) {
            // render an image file first, then reference the image in markdown
            var imageRenderer = diagramCode.trim().startsWith("@startuml") ? new PlantumlRenderer(imageType) : new MermaidRenderer(imageType);
            new File(baseFilePath).mkdirs();

            var filename = testCase.number() + "." + imageType;
            try (var stream = imageRenderer.render(diagramCode); var fos = new FileOutputStream(baseFilePath + File.separator + filename)) {
                stream.transferTo(fos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var altText = testCase.number();

            baseItem.append(image(altText, filename))
                    .append(NEWLINE);

        } else {
            // directly embed Mermaid/Plantuml in markdown

            String language;
            if (diagramCode.trim().startsWith("@startuml")) {
                language = "plantuml";
            } else {
                language = "mermaid";
            }
            baseItem.append(codeBlock(diagramCode, language))
                    .append(NEWLINE);
        }

        stringBuilder.append(unorderedList(baseItem.toString().replace(NEWLINE, NEWLINE + FOUR_SPACES_INDENT) + NEWLINE));

    }

    @Override
    public String render() {
        return stringBuilder.toString();
    }

    public void testCase(String displayName, boolean isMandatory, String testNumber, @Nullable String specUrl, Path diagramImage) {
        var baseItem = baseTestCase(displayName, isMandatory, testNumber, specUrl);


        baseItem.append(image(testNumber, diagramImage.getFileName().toString()))
                .append(NEWLINE);

        stringBuilder.append(unorderedList(baseItem.toString().replace(NEWLINE, NEWLINE + FOUR_SPACES_INDENT) + NEWLINE));
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

    //avoid headings with underlines
    @NotNull
    private Heading heading(String title, int level) {
        var heading = Markdown.heading(title, level);
        heading.setUnderlineStyle(false);
        return heading;
    }


    public static final class Builder {
        private final MarkdownRenderer renderer;

        private Builder() {
            renderer = new MarkdownRenderer();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder preRenderImages(boolean preRenderImages) {
            this.renderer.preRenderImages = preRenderImages;
            return this;
        }

        public Builder imageType(String imageType) {
            this.renderer.imageType = imageType;
            return this;
        }

        public Builder baseFilePath(String baseFilePath) {
            this.renderer.baseFilePath = baseFilePath;
            return this;
        }

        public MarkdownRenderer build() {
            if (renderer.preRenderImages) {
                if (renderer.baseFilePath == null) {
                    throw new IllegalArgumentException("A base path must be set if pre-rendering images is activated");
                }
                if (renderer.imageType == null) {
                    throw new IllegalArgumentException("An image type (svg or png) must be set if pre-rendering images is activated");
                }
            }
            return renderer;
        }
    }
}
