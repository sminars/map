package main.weather;

import com.squareup.moshi.Moshi;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;
import main.weather.deserializeClasses.Forecast;
import main.weather.deserializeClasses.Weather;
import spark.Request;
import spark.Response;
import spark.Route;


/**
 * getweather at lat and lon points; returns the temperature; class handles that request.
 */
public class WeatherHandler implements Route {

  public WeatherHandler() {

  }

  /**
   * handles the getweather request and responds with the forcast at the given lon and lat in Json
   * format; processes errors if request cannot be made.
   * @param request the request recived to the server
   * @param response the response  from the server
   * @throws Exception if there is an exception
   * @returns serialized response in Json format
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> replies = new HashMap<>();

    if (request.queryParams().size() != 2) {
      replies.put("result", "error_bad_request");
      return new WeatherResponse(replies).serialize();
    }

    String latitude = request.queryParams("lat");
    String longitude = request.queryParams("lon");

    HttpRequest weatherRequest = HttpRequest.newBuilder()
        .uri(new URI("https://api.weather.gov/points/" + latitude + "," + longitude))
        .GET()
        .build();
    HttpResponse<String> weatherResponse = HttpClient.newBuilder()
        .build()
        .send(weatherRequest, BodyHandlers.ofString());

    if (weatherResponse.statusCode() == 404 || weatherResponse.statusCode() == 301
        || weatherResponse.statusCode() == 400) {
      replies.put("result", "error_bad_request");
      return new WeatherResponse(replies).serialize();
    }

    String forecastURL = this.deserializeURL(weatherResponse).properties.forecast;

    HttpRequest forecastRequest = HttpRequest.newBuilder()
        .uri(new URI(forecastURL))
        .GET()
        .build();
    HttpResponse<String> forecastResponse = HttpClient.newBuilder()
        .build()
        .send(forecastRequest, BodyHandlers.ofString());

    Integer currentTemperature = this.deserializeWeather(forecastResponse).properties.periods.get(
        0).temperature;

    replies.put("result", "success");
    replies.put("lat", latitude);
    replies.put("lon", longitude);
    replies.put("temperature", currentTemperature);
    return new WeatherResponse(replies).serialize();
  }

  /**
   * deserializes the Json from the weather api; gets the link to see the forcast.
   * @param response link to the api being deserialized
   * @return deserialized Forcast returns the link to get the forcast
   */
  public Weather deserializeURL(HttpResponse<String> response) {
    try {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(Weather.class).fromJson(response.body());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Gets the weather at a location lon and lat
   * @param response the link to get the Forcast
   * @return the Forcast for the given coordinates
   */
  public Forecast deserializeWeather(HttpResponse<String> response) {
    try {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(Forecast.class).fromJson(response.body());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Serialozed the response for getcsv.
   * @return serialized response in Json Format
   */
  public record WeatherResponse(Map<String, Object> response) {

    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(WeatherResponse.class).toJson(this);
    }
  }
}
