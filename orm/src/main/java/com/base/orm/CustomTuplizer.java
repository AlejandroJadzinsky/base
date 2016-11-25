package com.base.orm;

import org.hibernate.bytecode.spi.ReflectionOptimizer;
import org.hibernate.bytecode.spi.ReflectionOptimizer.InstantiationOptimizer;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.PojoEntityInstantiator;
import org.hibernate.tuple.entity.PojoEntityTuplizer;
import org.springframework.beans.DirectFieldAccessor;

/** A {@link PojoEntityTuplizer} specialization that provides Hibernate a
 * strategy to build entities without default constructors using an {@link
 * EntityFactory} that has all the necessary dependencies to instantiate the
 * required entity.
 */
public class CustomTuplizer extends PojoEntityTuplizer {
  /** The reflection optimizer. */
  private ReflectionOptimizer optimizer;

  /** Creates an instance using a {@link PersistentClass}.
   *
   * @param entityMetamodel an EntityModel instance, cannot be null.
   * @param mappedEntity a PersistentClass instance, cannot be null.
   */
  public CustomTuplizer(final EntityMetamodel entityMetamodel,
      final PersistentClass mappedEntity) {
    super(entityMetamodel, mappedEntity);

    // Hack to obtain the superclass configured reflection optimizer.
    DirectFieldAccessor dfa = new DirectFieldAccessor(this);
    optimizer = (ReflectionOptimizer) dfa.getPropertyValue("optimizer");
  }

  /** {@inheritDoc}.*/
  @Override
  protected Instantiator buildInstantiator(final EntityMetamodel metamodel,
      final PersistentClass persistentClass) {
    InstantiationOptimizer localOptimizer = null;
    if (optimizer != null) {
      localOptimizer = optimizer.getInstantiationOptimizer();
    }
    return new CustomInstantiator(metamodel, persistentClass, localOptimizer);
  }

  /** Instantiator that serch within the PersistenceUnits the ones that
   * has been declared a Factory and use it as Instantiator.
   */
  public static class CustomInstantiator extends PojoEntityInstantiator {

    /** The Hibernate's EntityMetamodel instance, never null. e*/
    private final EntityMetamodel entityMetamodel;

    /** The Hibernate's PersistentClass, never null. */
    private final PersistentClass persistentClass;

    /** Creates a new instance of the Instantiator.
     *
     * @param metamodel the Hibernate's EntityMetamodel instance
     * @param aPersistentClass the Hibernate's persistent class.
     * @param optimizer the reflection optimizer.
     */
    public CustomInstantiator(final EntityMetamodel metamodel,
        final PersistentClass aPersistentClass,
        final InstantiationOptimizer optimizer) {
      super(metamodel, aPersistentClass, optimizer);

      entityMetamodel = metamodel;
      persistentClass = aPersistentClass;
    }

    /** {@inheritDoc}.*/
    @Override
    public Object instantiate() {
      HibernateComponent hibernate = entityMetamodel.getSessionFactory()
          .getServiceRegistry().getService(HibernateComponent.class);
      EntityFactory factory = hibernate.getFactory(persistentClass);

      if (factory != null) {
        return factory.create();
      }
      return super.instantiate();
    }
  }
}
