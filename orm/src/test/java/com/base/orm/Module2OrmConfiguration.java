package com.base.orm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.entities.Module2Repository;
import com.test.entities.Pet;

@Configuration("module2")
public class Module2OrmConfiguration extends OrmConfiguration {
  @Override
  protected ModulePersistenceRegistry getModuleRegistry() {
    ModulePersistenceRegistry registry = new ModulePersistenceRegistry("m2");
    registry.add(Pet.class);
    return registry;
  }

  @Bean("module2.repository")
  public Module2Repository getRepository() {
    return new Module2Repository(getHibernateComponent().getSessionFactory());
  }
}
