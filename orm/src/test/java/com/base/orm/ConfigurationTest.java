package com.base.orm;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.test.entities.EntityRepository;
import com.test.entities.Module2Repository;
import com.test.entities.Person;
import com.test.entities.Pet;
import com.test.entities.Place;
import com.test.entities.PlaceService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(
    classes = {
        Module1OrmConfiguration.class,
        Module2OrmConfiguration.class
    }
)
public class ConfigurationTest {

  @Autowired
  @Qualifier("module1.repository")
  private EntityRepository m1Repository;

  @Autowired
  @Qualifier("module2.repository")
  private Module2Repository m2Repository;

  @Test
  public void saveEntity() {
    Person person = new Person("p1@gmail.com", "p1");

    assertThat(person.getId(), is(nullValue()));
    Person savedPerson = m1Repository.save(person);
    assertThat(savedPerson.getId(), is(notNullValue()));
    Person retrievedPerson = m1Repository.getPerson(savedPerson.getId());
    assertThat(retrievedPerson.geteMail(), is(person.geteMail()));
    assertThat(retrievedPerson.getName(), is(person.getName()));
  }

  @Test
  public void saveTuplizedEntity() {
    Place place = new Place("place1", new PlaceService("service"));

    m1Repository.save(place);
    Place retrievedPlace = m1Repository.getPlace(place.getCode());

    assertThat(retrievedPlace.getCode(), is(place.getCode()));
    assertThat(retrievedPlace.getService(), is(notNullValue()));
    assertThat(retrievedPlace.getService().getName(), is("tuplized service"));
  }

  @Test
  public void saveModule2() {
    Pet pet = new Pet("spot");

    assertThat(pet.getId(), is(nullValue()));
    Pet savedPet = m2Repository.save(pet);
    assertThat(savedPet.getId(), is(notNullValue()));
    Pet retrievedPet = m2Repository.getPet(savedPet.getId());
    assertThat(retrievedPet.getNick(), is(pet.getNick()));
    assertThat(retrievedPet.getId(), is(pet.getId()));
  }
}
