package com.test.entities;

import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class EntityRepository {
  private SessionFactory sessionFactory;

  public EntityRepository(final SessionFactory factory) {
    sessionFactory = factory;
  }

  public Person save(final Person person) {
    sessionFactory.getCurrentSession().saveOrUpdate(person);
    return person;
  }

  public Person getPerson(final Long id) {
    return sessionFactory.getCurrentSession().get(Person.class, id);
  }

  @SuppressWarnings({"unchecked", "deprecated"})
  public List<Person> listPersons() {
    return sessionFactory.getCurrentSession().createCriteria(Person.class)
        .list();
  }

  public Place save(final Place place) {
    sessionFactory.getCurrentSession().saveOrUpdate(place);
    return place;
  }

  public Place getPlace(final String code) {
    return sessionFactory.getCurrentSession().get(Place.class, code);
  }

  @SuppressWarnings({"unchecked", "deprecated"})
  public List<Person> listPlaces() {
    return sessionFactory.getCurrentSession().createCriteria(Place.class)
        .list();
  }
}
