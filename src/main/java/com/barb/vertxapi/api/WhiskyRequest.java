package com.barb.vertxapi.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WhiskyRequest {

  private String name;
  private String origin;

  @JsonIgnore
  private int id;

  @JsonCreator
  public WhiskyRequest(
      @JsonProperty("name") String name,
      @JsonProperty("origin") String origin) {
    this.name = name;
    this.origin = origin;
  }

  public int getId() {
    return id;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getOrigin() {
    return origin;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setOrigin(String origin) {
    this.origin = origin;
  }
}
