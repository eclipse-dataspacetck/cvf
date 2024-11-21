package org.eclipse.dataspacetck.annotation.processors;


import org.eclipse.dataspacetck.api.system.MandatoryTest;
import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
import org.eclipse.dataspacetck.document.model.TestCase;
import org.eclipse.dataspacetck.document.model.TestGraph;
import org.eclipse.dataspacetck.rendering.markdown.MarkdownRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

import static java.util.Optional.ofNullable;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({ TestPlanGenerator.OUTPUTDIR_OVERRIDE, TestPlanGenerator.FORCE_CONVERSION, TestPlanGenerator.CONVERSION_FORMAT, })
public class TestPlanGenerator extends AbstractProcessor {
    public static final String OUTPUTDIR_OVERRIDE = "cvf.outputDir";
    public static final String FORCE_CONVERSION = "cvf.conversion.force";
    public static final String CONVERSION_FORMAT = "cvf.conversion.format"; // "png" or "svg"

    private static final String DEFAULT_IMAGE_FORMAT = "svg";
    private static final String TESTPLAN_NAME = "testplan.md";
    private final TestGraph testGraph = new TestGraph();

    // using the method here to be refactoring-safe
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(TestSequenceDiagram.class.getName(), MandatoryTest.class.getName(), Test.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeFile();
            return false;
        } else {
            gatherAllTestMethods(roundEnv);
        }

        return true;
    }

    private void gatherAllTestMethods(RoundEnvironment roundEnv) {
        var tests = roundEnv.getElementsAnnotatedWith(Test.class);
        var testSequenceDiagrams = roundEnv.getElementsAnnotatedWith(TestSequenceDiagram.class);
        var mandatoryTests = roundEnv.getElementsAnnotatedWith(MandatoryTest.class);

        Set<Element> elements = new HashSet<>();
        elements.addAll(tests);
        elements.addAll(testSequenceDiagrams);
        elements.addAll(mandatoryTests);

        elements.stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .distinct()
                .forEach(this::updateGraph);
    }

    private void writeFile() {
        try {

            var basePath = ofNullable(processingEnv.getOptions().get(OUTPUTDIR_OVERRIDE)).orElse(processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "", "testplan").toUri().toString());

            var renderer = MarkdownRenderer.Builder.newInstance().baseFilePath(basePath)
                    .imageType(ofNullable(processingEnv.getOptions().get(CONVERSION_FORMAT)).orElse(DEFAULT_IMAGE_FORMAT))
                    .preRenderImages(Boolean.parseBoolean(processingEnv.getOptions().get(FORCE_CONVERSION)))
                    .build();

            renderer.title("DSP Test Plan Document");
            renderer.subTitle("Created " + new Date());

            testGraph.render(renderer);

            var location = processingEnv.getOptions().get(OUTPUTDIR_OVERRIDE);
            if (location != null) {
                new File(location).mkdirs();
                try (var writer = new BufferedWriter(new FileWriter(location + File.separator + TESTPLAN_NAME))) {
                    writer.write(renderer.render());
                }
            } else {
                var resource = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "testplan/" + TESTPLAN_NAME);
                try (var writer = resource.openWriter()) {
                    writer.write(renderer.render());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Updates the internal graph representation with the current {@link Element}
     *
     * @param element A {@link Element} where {@link Element#getKind()} must be {@link ElementKind#METHOD}
     */
    private void updateGraph(Element element) {

        var displayName = element.getAnnotation(DisplayName.class).value();
        var number = element.getSimpleName().toString();
        var diagramString = element.getAnnotation(TestSequenceDiagram.class).value();
        var isMandatory = element.getAnnotation(MandatoryTest.class) != null;
        var testSuite = ofNullable(element.getEnclosingElement().getAnnotation(DisplayName.class)).map(DisplayName::value).orElseGet(() -> element.getEnclosingElement().getSimpleName().toString());
        var category = ofNullable(element.getEnclosingElement().getAnnotation(Tag.class)).map(Tag::value).orElse("");

        var testMethod = new TestCase(element.getSimpleName().toString(), displayName, number, isMandatory, diagramString);
        testGraph.category(category)
                .testSuite(testSuite)
                .insert(testMethod);
    }

}
