package com.base.orm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** This class represents a specific module persistence configuration and its
 *  used to bring this information to the
 * <br>
 * It holds the module name and a Set of classes for the persistent entities
 * in the given module.
 * <br>
 * @see HibernateComponent
 * @see OrmConfiguration
 */
public class ModulePersistenceRegistry implements
    Comparable<ModulePersistenceRegistry> {

  /** The module name, used in {@link HibernateComponent} to prefix table
   * names. never null nor empty. */
  // TODO (alejandro.jadzinsky 2016.11.18) module looks like a first class
  // citizen concept and should be modeled in its own class, though not in this
  // module.
  private String module;

  /** The Set of <code>@Entity</code> classes in the module, never null nor
   * empty. */
  private Set<Class<?>> entities = new HashSet<>();

  /** The mapping between the class generated as a key and the
   * {@link EntityFactory} implementation that builds the instance as a value,
   * never null. */
  private Map<Class<?>, EntityFactory> entitiesFactories = new HashMap<>();

  /** Constructor with mandatory parameters.
   *
   * @param theModule the module name, used to prefix table names and as an
   * id, thus two {@link ModulePersistenceRegistry} instances are considered
   * equals if their {@link #module} attributes are equals.
   */
  public ModulePersistenceRegistry(final String theModule) {
    module = theModule;
  }

  /** Adds a new relation between a persistent class and its related factory.
   * If the class was already present then its value is updated.
   *
   * @param clazz a Class instance, cannot be null.
   * @param factory an {@link EntityFactory} implementation, cannot be null.
   */
  public void add(final Class<?> clazz, final EntityFactory factory) {
    add(clazz);
    entitiesFactories.put(clazz, factory);
  }

  /** Adds a new class as a persistent entity in this Module.
   *
   * @param clazz aClass instance, cannot be null.
   */
  public void add(final Class<?> clazz) {
    entities.add(clazz);
  }

  /** Returns the {@link EntityFactory} associated with the given class.
   * @param <T> type of associated entity
   *
   * @param clazz a Class instance, cannot be null.
   *
   * @return an {@link EntityFactory} instance or null if none is found.
   */
  public <T> EntityFactory<T> getFactory(final Class<? extends T> clazz) {
    return entitiesFactories.get(clazz);
  }

  /** Returns the configured entities as a Set of classes.
   *
   * @return a set of Class instances, never null.
   */
  public Set<Class<?>> getEntities() {
    return Collections.unmodifiableSet(entities);
  }

  /** Returns the module identification, used to prefix table names in
   * generated schema.
   *
   * @return the module identification.
   */
  public String getModule() {
    return module;
  }

  /** Checks if an object is semantically equal to this instance.
   *
   * @param obj the object to be tested
   *
   * @return true if obj is not null, is of type
   * {@link ModulePersistenceRegistry} and this.{@link #module}.equals(
   * obj.module). Otherwise false.
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }

    if (!(obj instanceof ModulePersistenceRegistry)) {
      return false;
    }

    return module.equals(((ModulePersistenceRegistry) obj).module);
  }

  /** Returns this instance hashcode.
   *
   * @return an int greater than 0;
   */
  @Override
  public int hashCode() {
    return module.hashCode();
  }

  /** Compare another instance to this instance.
   *
   * @param that a {@link ModulePersistenceRegistry} instance.
   *
   * @return a negative integer, zero, or a positive integer as this object is
   * less than, equal to, or greater than the specified object
   */
  @Override
  public int compareTo(final ModulePersistenceRegistry that) {
    return module.compareTo(that.module);
  }
}
