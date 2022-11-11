package main.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GeoData {
  private String type;
  private Set<Feature> features;

  public GeoData(String type, Set<Feature> features){
    this.type = type;
    this.features = features;
  }
  public String getType(){
    return this.type;
  }

  public Set<Feature> getFeatures(){return this.features; }
}

