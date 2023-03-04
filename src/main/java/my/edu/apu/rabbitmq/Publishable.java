package my.edu.apu.rabbitmq;

import java.io.*;

/**
 * Classes that extend <code>Publishable</code> will have helper functions to get the byte representation of the
 * serialized object.
 * <p>
 * The static class also provides a helper function to convert bytes to an object that derives <code>Publishable</code>.
 * See {@link Publishable#fromBytes(byte[])}
 */
public interface Publishable extends Serializable {

    @SuppressWarnings("unchecked")
    static <T extends Publishable> T fromBytes(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    default byte[] getBytes() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
