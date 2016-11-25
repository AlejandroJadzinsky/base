package com.test.entities;

public class PlaceService {
  private final String name;

  public PlaceService(final String theName) {
    name = theName;
  }

  public String getName() {
    return name;
  }
}
