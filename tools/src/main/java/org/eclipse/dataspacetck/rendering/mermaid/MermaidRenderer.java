package org.eclipse.dataspacetck.rendering.mermaid;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.dataspacetck.rendering.spi.DiagramImageRenderer;

import java.io.IOException;
import java.io.InputStream;

public class MermaidRenderer implements DiagramImageRenderer {
    private static final String KROKI_URL = "https://kroki.io/mermaid/svg";

    @Override
    public InputStream render(String diagramString) {
        var client = new OkHttpClient();
        var body = RequestBody.create(diagramString, MediaType.parse("text/plain"));
        var request = new Request.Builder()
                .url(KROKI_URL)
                .post(body)
                .build();
        try (var response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().byteStream();
            } else {
                throw new RuntimeException("Unexpected HTTP response code: %s, message: %s".formatted(response.code(), response.message()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
