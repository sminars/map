package main.map;

public class Feature {

  private String type;
  private Geometry geometry;
  private Property properties;


  public Feature(String type, Geometry geometry, Property properties){
    this.type = type;
    this.geometry = geometry;
    this.properties = properties;
  }
  // add properties

  public String getType() {
    return this.type;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }
  public Property getProperties() {
    return this.properties;
  }
}
