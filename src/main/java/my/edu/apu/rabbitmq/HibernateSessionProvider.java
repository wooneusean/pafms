package my.edu.apu.rabbitmq;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateSessionProvider {
    private static volatile HibernateSessionProvider instance;
    private final EntityManagerFactory entityManagerFactory;

    private HibernateSessionProvider() {
        entityManagerFactory = Persistence.createEntityManagerFactory("my.edu.apu.jpa");
    }

    public static HibernateSessionProvider getInstance() {
        HibernateSessionProvider result = instance;
        if (result != null) {
            return result;
        }

        synchronized (HibernateSessionProvider.class) {
            if (instance == null) {
                instance = new HibernateSessionProvider();
            }
            return instance;
        }
    }

    public EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
}
