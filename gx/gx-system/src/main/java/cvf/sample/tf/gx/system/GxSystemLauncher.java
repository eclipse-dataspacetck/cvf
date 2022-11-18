package cvf.sample.tf.gx.system;

import cvf.core.spi.system.ClientConfiguration;
import cvf.core.spi.system.SystemConfiguration;
import cvf.core.spi.system.SystemLauncher;
import cvf.sample.tf.gx.system.api.GxSystemClient;
import cvf.sample.tf.gx.system.api.Response;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public class GxSystemLauncher implements SystemLauncher {
    private static final Set<Class<?>> TYPES = Set.of(GxSystemClient.class);

    private ExecutorService executor;

    @Override
    public void start(SystemConfiguration configuration) {
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public <T> boolean providesService(Class<T> type) {
        return type.equals(GxSystemClient.class);
    }

    @Override
    @Nullable
    public <T> T getService(Class<T> type, ClientConfiguration configuration, String scopeId) {
        requireNonNull(type);
        requireNonNull(configuration);
        if (!GxSystemClient.class.equals(type)) {
            throw new IllegalArgumentException("Unsupported client type: " + type.getName());
        }
        return type.cast(new SampleSystemClient());
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private class SampleSystemClient implements GxSystemClient {

        @Override
        public Response invoke(String address, Object message) {
            executor.submit(() -> {
                var httpClient = new OkHttpClient.Builder().build();
                var request = new Request.Builder().url("http://localhost:8083" + "/foo").get().build();   // xcv
                try (var response = httpClient.newCall(request).execute()) {
                    if (response.code() != 200) {
                        System.out.println();
                    }
                    var body = response.body();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            return new Response();
        }
    }

}
