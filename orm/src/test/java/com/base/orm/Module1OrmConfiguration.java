package com.base.orm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.entities.EntityRepository;
import com.test.entities.Person;
import com.test.entities.Place;
import com.test.entities.PlaceFactory;

@Configuration("module1")
public class Module1OrmConfiguration extends OrmConfiguration {
  @Override
  protected ModulePersistenceRegistry getModuleRegistry() {
    ModulePersistenceRegistry registry = new ModulePersistenceRegistry("m1");
    registry.add(Person.class);
    registry.add(Place.class, new PlaceFactory());
    return registry;
  }

  @Bean("module1.repository")
  public EntityRepository getRepository() {
    return new EntityRepository(getHibernateComponent().getSessionFactory());
  }
}
