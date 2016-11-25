package com.test.entities;

import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class Module2Repository {
  private SessionFactory sessionFactory;

  public Module2Repository(final SessionFactory factory) {
    sessionFactory = factory;
  }

  public Pet save(final Pet pet) {
    sessionFactory.getCurrentSession().saveOrUpdate(pet);
    return pet;
  }

  public Pet getPet(final Long id) {
    return sessionFactory.getCurrentSession().get(Pet.class, id);
  }

  @SuppressWarnings({"unchecked", "deprecated"})
  public List<Pet> listPets() {
    return sessionFactory.getCurrentSession().createCriteria(Pet.class)
        .list();
  }
}
