package my.edu.apu.rabbitmq;

import java.io.*;

public interface Persistable extends Serializable {
    @SuppressWarnings("unchecked")
    static <T extends Persistable> T load(String path) {
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream objIn = new ObjectInputStream(fileIn)) {
            return (T) objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    default void save(String path) {
        try (FileOutputStream fileOut = new FileOutputStream(path);
             ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {
            objOut.writeObject(this);
            objOut.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
