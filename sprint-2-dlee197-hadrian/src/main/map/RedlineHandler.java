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

  public RedlineHandler() {}

  /**
   * returns the GeoJSON redlining data within a bounding box specified by the user.
   * If no bounding box is specified, will return all GeoJSON data
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Initialize our replies map
    Map<String, Object> replies = new HashMap<>();

    // If we get no query parameters(aka no bounding box), simply return the entire GeoJSON
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

    // Check if we have exactly four parameters. If not, throw an error
    if (request.queryParams().size() != 4) {
      replies.put("result", "error_bad_request");
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }

    // Access the query parameters
    double latmin = Double.parseDouble(request.queryParams("latmin"));
    double latmax = Double.parseDouble(request.queryParams("latmax"));
    double lonmin = Double.parseDouble(request.queryParams("lonmin"));
    double lonmax = Double.parseDouble(request.queryParams("lonmax"));

    // Check if the parameters' maxes are greater than the mins, throw an error if not
    if(latmax < latmin || lonmax < lonmin){
      System.out.println("Max and min must have valid values");
      replies.put("result", "error_bad_request");
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }

    try {
      // Read in our GeoJSON data and convert it into a geodata class
      BufferedReader reader = new BufferedReader(
          new FileReader("src/main/csv/data/fullDownload.json"));
      GeoData geodata = this.deserializeGeoJson(reader.readLine());

      // Make a new list to contain the filtered list of features
      Set<Feature> oldfeatures = geodata.getFeatures();
      Set<Feature> newfeatures = new HashSet();

      // lat and lon temporarily store the coordinates of each point in each feature
      double lat;
      double lon;
      // isvalid starts out true for each feature and determines whether or not that feature
      // will make it into the final list of filtered features
      boolean isvalid;

      // Iterate through all the features
      for(Feature feature : oldfeatures){
        isvalid = true;
        // If a feature has null geometry, it is marked as invalid
        if(feature.getGeometry() == null){
          isvalid = false;
        }
        // Iterate through all the points in the feature's geometry
        else{
          List<List<List<List<Double>>>> coordinatearray = feature.getGeometry().getCoordinates();
          for(int i = 0; i < coordinatearray.size(); i++){
            for(int i2 = 0; i2 < coordinatearray.get(i).size(); i2++){
              for(int i3 = 0; i3 < coordinatearray.get(i).get(i2).size(); i3++){
                if (isvalid) {
                  lat = coordinatearray.get(i).get(i2).get(i3).get(0);
                  lon = coordinatearray.get(i).get(i2).get(i3).get(1);
                  // Check to see if each point lies within our bounding box. If it does not,
                  // then the feature is marked as invalid.
                  if (!(latmin < lat && lat < latmax && lonmin < lon && lon < lonmax)) {
                    isvalid = false;
                  }
                }
              }
            }
          }
        }
        if(isvalid){
          // After iterating through every coordinate in the feature's geometry, check to see if
          // the feature is valid. If so, add it to the list of filtered features
          newfeatures.add(feature);
        }
      }
      // Brief sanity check + used for testing purposes
      System.out.println("filtered feature size: " + newfeatures.size());

      // By the end of this we should only have the features that are
      // completely within the bounding box.
      // Turn this new data back into a GeoData object, serialize it,
      // and then return this as a reply.

      GeoData resultdata = new GeoData(geodata.getType(),newfeatures);

      replies.put("result", "success");
      replies.put("data", resultdata);
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }
    catch(IOException e){
      replies.put("result", "error_bad_request");
      return new main.map.RedlineHandler.RedlineResponse(replies).serialize();
    }
  }

  /**
   * Deserializes our GeoJSON file.
   * @return GeoData object.
   */
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
   * Serializes the response for getredlinedata.
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
