package main.map;

import java.util.List;
import java.util.Map;

public class Property {
  private String state;
  private String city;
  private String name;
  private String holc_id;
  private String holc_grade;
  private String neighborhood_id;
//  private Map<String,String> neighborhood_description_data;

  public Property(String state, String city, String name, String holc_id, String holc_grade, String neighborhood_id, Map<String,String> neighborhood_description_data){
    this.state = state;
    this.city = city;
    this.name = name;
    this.holc_id = holc_id;
    this.holc_grade = holc_grade;
    this.neighborhood_id = neighborhood_id;
//    this.neighborhood_description_data = neighborhood_description_data;
  }
}
