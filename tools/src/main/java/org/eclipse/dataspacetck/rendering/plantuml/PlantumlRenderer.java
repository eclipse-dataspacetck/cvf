package org.eclipse.dataspacetck.rendering.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.eclipse.dataspacetck.rendering.spi.DiagramImageRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PlantumlRenderer implements DiagramImageRenderer {

    private FileFormat fileFormat;


    public PlantumlRenderer(String fileFormat) {
        this.fileFormat = Enum.valueOf(FileFormat.class, fileFormat.toUpperCase());
    }

    @Override
    public InputStream render(String diagramString) {
        var reader = new SourceStringReader(diagramString);
        var bos = new ByteArrayOutputStream();
        try {
            fileFormat = FileFormat.SVG;
            reader.outputImage(bos, new FileFormatOption(fileFormat));
            var input = new ByteArrayInputStream(bos.toByteArray());
            bos.flush();
            bos.close();
            return input;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
