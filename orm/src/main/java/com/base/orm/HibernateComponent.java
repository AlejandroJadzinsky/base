package com.base.orm;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.Service;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.stereotype.Component;

/** This class is intended to be a wrapper around Hibernate infrastructure
 * and act as its single point of contact.
 * <br>
 * It is exposed as a <code>@Bean</code> in {@link OrmConfiguration}
 * configuration.
 * <br>
 * The {@link #moduleRegistries} brings the configured persistent classes in
 * each module, needed to generate the {@link SessionFactory} along with the
 * proper database schema.
 * <br>
 * Also the {@link ModulePersistenceRegistry#module} is used to prefix
 * entities table names. e. g. if you annotate in your 'login' module an entity
 * UserRole like this <code>@Table(name="user_roles")</code> the actual table
 * name will be 'login_user_roles'.
 */
@Component
public class HibernateComponent implements Service {

  /** The Spring Environment, never null. */
  private final ConfigurableEnvironment environment;

  /** The {@link DataSource} singleton instance, never null. */
  private final DataSource dataSource;

  /** The Set of configured {@link ModulePersistenceRegistry} instances, never
   *  null nor empty. */
  private final Set<ModulePersistenceRegistry> moduleRegistries;

  /** The {@link Metadata} singleton instance, never null. */
  private final Metadata metadata;

  /** The {@link SessionFactory} singleton instance, never null. */
  private final SessionFactory sessionFactory;

  /** The {@link HibernateTransactionManager} singleton instance, never null. */
  private final HibernateTransactionManager transactionManager;

  /** Constructor with mandatory parameters.
   *
   * @param theEnvironment a Spring {@link ConfigurableEnvironment} instance,
   * cannot be null.
   * @param theDataSource a {@link DataSource} instance, cannot be null.
   * @param theRegistries a set of {@link ModulePersistenceRegistry} instances,
   * cannot be null nor empty.
   */
  public HibernateComponent(final ConfigurableEnvironment theEnvironment,
      final DataSource theDataSource,
      final Set<ModulePersistenceRegistry> theRegistries) {
    Validate.notNull(theEnvironment, "No Spring environment instance.");
    Validate.notNull(theEnvironment, "No DataSource instance.");
    Validate.notEmpty(theRegistries, "No module configuration provided");

    moduleRegistries = new HashSet<>();
    environment = theEnvironment;
    dataSource = theDataSource;
    moduleRegistries.addAll(theRegistries);
    metadata = buildMetadata();
    sessionFactory = metadata.getSessionFactoryBuilder().build();
    transactionManager = buildTransactionManager();
  }

  /** Retrieves the {@link SessionFactory} singleton instance.
   *
   * @return a {@link SessionFactory} instance, never null.
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /* ****************************************************************
   * package access attributes
   * ****************************************************************/

  /** Expose the {@link DataSource} to be used by DatabaseUtility
   *
   * @return a {@link DataSource} instance, never null.
   */
  DataSource getDataSource() {
    return dataSource;
  }

  /** Expose the {@link Metadata} to be used by DatabaseUtility
   *
   * @return a {@link Metadata} instance, never null.
   */
  Metadata getMetadata() {
    return metadata;
  }

  /** Indicates if this instance configuration use an in memory data source. To
   * be used by DatabaseUtility
   *
   * @return true if the data source is running in memory, otherwise false.
   */
  boolean isInMemoryDatasource() {
    return environment.getProperty("datasource.url").contains("mem");
  }

  /** Retrieves this instance configured dialect. To be used by DatabaseUtility
   *
   * @return the configured Dialect, never null nor empty.
   */
  String getDialect() {
    return environment.getProperty("hibernate.dialect");
  }

  /** Expose the {@link HibernateTransactionManager} to be used by
   * {@link OrmConfiguration}
   *
   * @return a {@link HibernateTransactionManager} instance, never null.
   */
  HibernateTransactionManager getTransactionManager() {
    return transactionManager;
  }

  /** Returns the {@link EntityFactory} associated with the given persistent
   * class. Used by {@link CustomTuplizer}
   *
   * @return an {@link EntityFactory} instance, never null.
   */
  EntityFactory getFactory(final PersistentClass entity) {
    ModulePersistenceRegistry registry = findRegistry(entity);
    return registry.getFactory(entity.getMappedClass());
  }

  /* ****************************************************************
   * private methods and functions
   * ****************************************************************/

  private Metadata buildMetadata() {
    StandardServiceRegistry standardRegistry;
    standardRegistry = new StandardServiceRegistryBuilder()
        .applySettings(getHibernateProperties())
        .applySetting("hibernate.connection.datasource", dataSource)
        .addService(getClass(), this)
        .build();

    MetadataSources sources = new MetadataSources(standardRegistry);
    moduleRegistries.forEach(r -> r.getEntities()
        .forEach(sources::addAnnotatedClass));

    Metadata createdMetadata = sources
        .getMetadataBuilder()
        .build();

    createdMetadata.getEntityBindings()
        .forEach(pc -> {
          pc.addTuplizer(EntityMode.POJO, CustomTuplizer.class.getName());
          pc.getTable().setName(getTableName(pc));
        });

    return createdMetadata;
  }

  private String getTableName(final PersistentClass entity) {
    ModulePersistenceRegistry registry = findRegistry(entity);

    if (StringUtils.isBlank(registry.getModule())) {
      return entity.getTable().getName();
    }

    return registry.getModule() + "_" + entity.getTable().getName();
  }

  private ModulePersistenceRegistry findRegistry(final PersistentClass entity) {
    return moduleRegistries.stream()
        .filter(p -> p.getEntities().contains(entity.getMappedClass()))
        .findFirst() //should be the only one
        .get(); //should always be present
  }

  private Properties getHibernateProperties() {
    Properties properties = new Properties();
    for (PropertySource<?> propertySource : environment.getPropertySources()) {
      if (propertySource instanceof EnumerablePropertySource<?>) {
        EnumerablePropertySource<?> source;
        source = (EnumerablePropertySource<?>) propertySource;
        for (String name : source.getPropertyNames()) {
          if (name.startsWith("hibernate.")) {
            properties.setProperty(name, environment.getProperty(name));
          }
        }
      }
    }

    return properties;
  }

  private HibernateTransactionManager buildTransactionManager() {
    HibernateTransactionManager txManager = new HibernateTransactionManager();
    txManager.setSessionFactory(sessionFactory);
    return txManager;
  }
}
