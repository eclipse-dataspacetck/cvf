package org.eclipse.dataspacetck.annotation.processors;


import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedOptions({ TestPlanGenerator.OUTPUTDIR_OVERRIDE })
public class TestPlanGenerator extends AbstractProcessor {
    public static final String OUTPUTDIR_OVERRIDE = "cvf.outputDir";
    private static final String TESTPLAN_NAME = "testplan.md";

    // using the method here to be refactoring-safe
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(TestSequenceDiagram.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "hello from the annotation processor");
        if (roundEnv.processingOver()) {
            writeFile();
            return false;
        }
        return true;
    }

    private void writeFile() {
        try {
            var filer = processingEnv.getFiler();
            var location = processingEnv.getOptions().get(OUTPUTDIR_OVERRIDE);
            if (location != null) {
                new File(location).mkdirs();
                try (var writer = new BufferedWriter(new FileWriter(location + File.separator + TESTPLAN_NAME))) {
                    //todo: write output
                    writer.write("foo "+UUID.randomUUID());
                }
            } else {
                var resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", TESTPLAN_NAME);
                try (var writer = resource.openWriter()) {
                    // todo: write output
                    writer.write("bar " + UUID.randomUUID());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
