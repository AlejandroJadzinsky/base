package com.test.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "persons")
public class Person {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "e_mail", nullable = false)
  private String eMail;

  @Column(nullable = false)
  private String name;

  protected Person() {}

  public Person(final String theEmail, final String theName) {
    eMail = theEmail;
    name = theName;
  }

  public Long getId() {
    return id;
  }

  public String geteMail() {
    return eMail;
  }

  public String getName() {
    return name;
  }
}
