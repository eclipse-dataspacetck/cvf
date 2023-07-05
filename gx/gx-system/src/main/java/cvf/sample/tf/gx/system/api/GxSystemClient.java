package cvf.sample.tf.gx.system.api;

public interface GxSystemClient {

    Response invoke(String address, Object message);
}
