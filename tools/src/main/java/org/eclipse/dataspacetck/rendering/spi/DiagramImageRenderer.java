package org.eclipse.dataspacetck.rendering.spi;

import java.io.InputStream;

public interface DiagramImageRenderer {
    InputStream render(String diagramString);
}
