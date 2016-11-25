package com.test.entities;

import com.base.orm.EntityFactory;

public class PlaceFactory implements EntityFactory<Place> {

  private PlaceService service;

  public PlaceFactory() {
    service = new PlaceService("tuplized service");
  }

  @Override
  public Place create() {
    return new Place(service);
  }
}