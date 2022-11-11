package main.map;

import java.util.ArrayList;
import java.util.List;

public class Geometry {

  private String type;
  private List<List<List<List<Double>>>> coordinates;

  public Geometry(String type, List<List<List<List<Double>>>> coordinates){
    this.type = type;
    this.coordinates = coordinates;
  }

  public String getType() {
    return this.type;
  }

  public List<List<List<List<Double>>>> getCoordinates() {
    return this.coordinates;
  }
}
