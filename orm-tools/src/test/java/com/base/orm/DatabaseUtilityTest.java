package com.base.orm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.test.entities.EntityRepository;
import com.test.entities.Module2Repository;
import com.test.entities.Person;
import com.test.entities.Place;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(
    classes = {
        Module1OrmConfiguration.class,
        Module2OrmConfiguration.class
    }
)
public class DatabaseUtilityTest {
  @Autowired
  private HibernateComponent hibernateComponent;

  @Autowired
  @Qualifier("module1.repository")
  private EntityRepository m1repository;

  @Autowired
  @Qualifier("module2.repository")
  private Module2Repository m2Repository;

  private DatabaseUtility utility;

  @Before
  public void setUp() {
    utility = new DatabaseUtility(hibernateComponent);
    utility.delete(Person.class, Place.class);
  }

  @Test
  public void runSqlScript_oneFile() {
    assertThat(m1repository.listPersons().isEmpty(), is(true));

    utility.runSqlScript("src/test/sql/000_init_persons.sql");
    assertThat(m1repository.listPersons().size(), is(3));

    utility.delete(Person.class);
    assertThat(m1repository.listPersons().isEmpty(), is(true));
  }

  @Test
  public void runSqlScript_directory() {
    assertThat(m1repository.listPersons().isEmpty(), is(true));
    assertThat(m1repository.listPlaces().isEmpty(), is(true));

    utility.runSqlScript("src/test/sql");
    assertThat(m1repository.listPersons().size(), is(3));
    assertThat(m1repository.listPlaces().size(), is(3));
  }

  @Test
  public void runSqlCommands() {
    assertThat(m1repository.listPersons().isEmpty(), is(true));
    assertThat(m1repository.listPlaces().isEmpty(), is(true));

    String insertPerson = "insert into m1_persons (e_mail, name) values ('p1@mail.com','person 1')";
    String insertPlace = "insert into m1_places (code) values ('code 1')";
    utility.runSqlCommands(insertPerson, insertPlace);

    assertThat(m1repository.listPersons().size(), is(1));
    assertThat(m1repository.listPlaces().size(), is(1));

    String deletePersons = "delete from m1_persons";
    String deletePlaces = "delete from m1_places";

    utility.runSqlCommands(deletePersons, deletePlaces);

    assertThat(m1repository.listPersons().isEmpty(), is(true));
    assertThat(m1repository.listPlaces().isEmpty(), is(true));
  }

  @Test
  public void generateSchema() {
    utility.generateSchemaScript("target/{dialect}_schema.sql");
  }

  @Test
  public void regenerateSchema() {
    utility.regenerateDevelopmentDatabase();
    assertThat(m1repository.listPersons().isEmpty(), is(true));
    assertThat(m1repository.listPlaces().isEmpty(), is(true));
    assertThat(m2Repository.listPets().isEmpty(), is(true));
  }
}