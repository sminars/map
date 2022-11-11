package main.map;

import com.squareup.moshi.Moshi;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.weather.WeatherHandler.WeatherResponse;
import main.weather.deserializeClasses.Forecast;
import main.weather.deserializeClasses.Weather;
import spark.Request;
import spark.Response;
import spark.Route;

public class RedlineHandler implements Route {

  public RedlineHandler() {

  }

  //DOCUMENT THIS
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> replies = new HashMap<>();

    if (request.queryParams().size() == 0) {
      try{
        BufferedReader reader = new BufferedReader(
            new FileReader("src/main/csv/data/fullDownload.json"));
        GeoData geodata = this.deserializeGeoJson(reader.readLine());
        replies.put("result", "success");
        replies.put("data", geodata);
        return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
      }
      catch(IOException e){
        e.printStackTrace();
        return null;
      }
    }
    if (request.queryParams().size() != 4) {
      replies.put("result", "error_bad_request");
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }


    double latmin = Double.parseDouble(request.queryParams("latmin"));
    double latmax = Double.parseDouble(request.queryParams("latmax"));
    double lonmin = Double.parseDouble(request.queryParams("lonmin"));
    double lonmax = Double.parseDouble(request.queryParams("lonmax"));

    if(latmax < latmin || lonmax < lonmin){
      System.out.println("Max and min must have valid values");
      replies.put("result", "error_bad_request");
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }


    try {
      BufferedReader reader = new BufferedReader(
          new FileReader("src/main/csv/data/fullDownload.json"));
      GeoData geodata = this.deserializeGeoJson(reader.readLine());

      Set<Feature> oldfeatures = geodata.getFeatures();
      Set<Feature> newfeatures = new HashSet();

      double lat;
      double lon;
      boolean isvalid;

      for(Feature feature : oldfeatures){
        isvalid = true;
        if(feature.getGeometry() == null){
          isvalid = false;
        }
        else{
          List<List<List<List<Double>>>> coordinatearray = feature.getGeometry().getCoordinates();
          for(int i = 0; i < coordinatearray.size(); i++){
            for(int i2 = 0; i2 < coordinatearray.get(i).size(); i2++){
              for(int i3 = 0; i3 < coordinatearray.get(i).get(i2).size(); i3++){
                if (isvalid) {
                  lat = coordinatearray.get(i).get(i2).get(i3).get(0);
                  lon = coordinatearray.get(i).get(i2).get(i3).get(1);
                  if (!(latmin < lat && lat < latmax && lonmin < lon && lon < lonmax)) {
                    isvalid = false;
                  }
                }
              }
            }
          }
        }
        if(isvalid){
          newfeatures.add(feature);
        }
      }
      System.out.println("filtered feature size: " + newfeatures.size());

      // By the end of this we should only have the completely contained features
      // Turn this new data back into a geodata, serialize it, and then return this as a reply.

      GeoData resultdata = new GeoData(geodata.getType(),newfeatures);

      //System.out.println("filtered result data: " + resultdata.getFeatures().toString());

      replies.put("result", "success");
      replies.put("data", resultdata);
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }
    catch(IOException e){
      replies.put("result", "error_bad_request");
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }
  }

  public GeoData deserializeGeoJson(String jsondata) {
    try {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(GeoData.class).fromJson(jsondata);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Serialozed the response for getcsv.
   * @return serialized response in Json Format
   */
  public record RedlineResponse(Map<String, Object> response) {
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(RedlineHandler.RedlineResponse.class).toJson(this);
      }
      catch(Exception e){
        e.printStackTrace();
        return null;
      }
    }
  }
}
