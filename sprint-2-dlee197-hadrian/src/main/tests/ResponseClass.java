package main.tests;

import main.map.GeoData;

public class ResponseClass {
  public String response;
  public GeoData data;

  public ResponseClass(String response, GeoData data){
    this.response = response;
    this.data = data;
  }

}
