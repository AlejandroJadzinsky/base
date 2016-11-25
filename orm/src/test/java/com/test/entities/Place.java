package com.test.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "places")
public class Place {

  @Id
  private String code;

  @Transient
  private PlaceService service;

  Place(final PlaceService theService) {
    service = theService;
  }

  public Place(final String theCode, final PlaceService theService) {
    this(theService);
    code = theCode;
  }

  public String getCode() {
    return code;
  }

  public PlaceService getService() {
    return service;
  }
}
