package com.test.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pets")
public class Pet {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "nick", nullable = false)
  private String nick;

  protected Pet() {}

  public Pet(final String theName) {
    nick = theName;
  }

  public Long getId() {
    return id;
  }

  public String getNick() {
    return nick;
  }
}
