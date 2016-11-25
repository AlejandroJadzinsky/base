package com.base.orm;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang3.Validate;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties
    .EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** A base class for every <code>@Configuration</code> class that wants to
 * enable database persistence.
 */
@Configuration
@PropertySource("orm.properties")
@EnableConfigurationProperties
@EnableTransactionManagement(proxyTargetClass = true)
public abstract class OrmConfiguration {

  /** The Spring environment, needed to instantiate the
   *  {@link HibernateComponent}, never null. */
  @Autowired
  private ConfigurableEnvironment environment;

  /** The {@link ModulesRegistry} instance, never null. */
  private final ModulesRegistry registries;

  /** Default constructor.
   */
  public OrmConfiguration() {
    registries = ModulesRegistry.getInstance();
    registries.add(getModuleRegistry());
  }

  /** The tomcat jdbc pool properties.
   *
   * This is initialized from properties that start with 'datasource'.
   *
   * @return the pool properties, never null.
   */
  @Bean
  @ConfigurationProperties(prefix = "datasource")
  PoolProperties poolProperties() {
    return new PoolProperties();
  }

  /** Creates and returns a {@link DataSource} singleton instance
   * implementation.
   *
   * @return a {@link DataSource} instance, never null.
   */
  @Bean
  javax.sql.DataSource getDataSource() {
    return new DataSource(poolProperties());
  }

  /** Defines the transaction manager to use.
   *
   * @return a {@link PlatformTransactionManager} instance, never null.
   */
  @Bean
  PlatformTransactionManager getTransactionManager() {
    return getHibernateComponent().getTransactionManager();
  }

  /** Creates and returns a {@link HibernateComponent} singleton instance
   * implementation.
   *
   * @return a {@link HibernateComponent} instance, never null.
   */
  @Bean
  public HibernateComponent getHibernateComponent() {
    return new HibernateComponent(environment, getDataSource(),
        registries.getRegistries());
  }

  /** Returns the {@link ModulePersistenceRegistry} for this instance.
   *
   * @return a {@link ModulePersistenceRegistry} instance, never null.
   */
  protected abstract ModulePersistenceRegistry getModuleRegistry();

  /** A singleton class to keep a registry of all the defined
   *  {@link ModulePersistenceRegistry} from each {@link OrmConfiguration}
   *  implementation. */
  private static final class ModulesRegistry {
    /** The singleton instance. */
    private static ModulesRegistry instance;

    /** The set of {@link ModulePersistenceRegistry} instances, never null. */
    private final Set<ModulePersistenceRegistry> registries;

    /** Default constructor. */
    private ModulesRegistry() {
      registries = new ConcurrentSkipListSet<>();
    }

    /** Retrieves the registry of {@link ModulePersistenceRegistry}.
     *
     * @return a Set of {@link ModulePersistenceRegistry} instances, never null.
     */
    public Set<ModulePersistenceRegistry> getRegistries() {
      return Collections.unmodifiableSet(registries);
    }

    /** Add a new {@link ModulePersistenceRegistry} instance to the registry.
     *
     * @param registry a {@link ModulePersistenceRegistry} instance, cannot
     * be null.
     */
    public void add(final ModulePersistenceRegistry registry) {
      Validate.notNull(registry, "No ModulePersistenceRegistry set.");

      registries.add(registry);
    }

    /** Retrieves this class singleton instance.
     *
     * @return a {@link ModulesRegistry} instance, never null.
     */
    public static ModulesRegistry getInstance() {
      if (instance == null) {
        instance = new ModulesRegistry();
      }

      return instance;
    }
  }
}
