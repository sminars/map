package main.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.weather.WeatherHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * tests getweather requests to see if the proper response is given.
 */
public class TestWeather {

  //setsup ports
  @BeforeAll
  public static void setup_before() {
    Spark.port(111);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  //setup for before the test
  @BeforeEach
  public void setup() {

    Spark.get("weather", new WeatherHandler());
    Spark.init();
    Spark.awaitInitialization();
  }

  //setup for after the test
  @AfterEach
  public void teardown() {
    Spark.unmap("/weather");
    Spark.awaitStop();
  }

  //sets up a connection
  static private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:"+ Spark.port() + "/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.connect();
    return clientConnection;
  }

  //Tests for valid weather points should return the points in json format
  @Test
  public void testWeatherValidPoints() throws IOException {
    HttpURLConnection clientConnection = tryRequest("weather?lat=41.8258&lon=-71.4029");
    //check valid connection
    assertEquals(200, clientConnection.getResponseCode());

    //makes the expected response
    Map<String, Object> expected = new HashMap();
    expected.put("result", "success");
    expected.put("lat", "41.8258");
    expected.put("lon", "-71.4029");

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Map<String, Object>> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected.get("result"), actual.get("response").get("result"));
    assertEquals(expected.get("lat"), actual.get("response").get("lat"));
    assertEquals(expected.get("lon"), actual.get("response").get("lon"));
  }

  /*
   * Tests entering coordinates that dont exists error message appear
   */
  @Test
  public void testWeatherInValidPoints() throws IOException {
    HttpURLConnection clientConnection = tryRequest("weather?lat=50&lon=-100");
    //check valid connection
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expected = new HashMap();
    expected.put("result", "error_bad_request");

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Map<String, Object>> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected.get("result"), actual.get("response").get("result"));
  }

  /*
   * entering coordinates that are out of bounds. Error message should appear
   */
  @Test
  public void testWeatheroutOfBoundsPoints() throws IOException {
    HttpURLConnection clientConnection = tryRequest("weather?lat=5000&lon=-100000");
    //check valid connection
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expected = new HashMap();
    expected.put("result", "error_bad_request");

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Map<String, Object>> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected.get("result"), actual.get("response").get("result"));
  }

  /*
   * entering params red and blue with valid coordinates; invalid params should be lon & lat
   */
  @Test
  public void ErrorWrongParam() throws IOException {
    HttpURLConnection clientConnection = tryRequest("weather?blue=41.8258&red=-71.4029");
    //check valid connection
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expected = new HashMap();
    expected.put("result", "error_bad_request");

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Map<String, Object>> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected.get("result"), actual.get("response").get("result"));
  }

  /*
   * entering coordinates that are out of bounds
   */
  @Test
  public void ErrorTestOneParam() throws IOException {
    HttpURLConnection clientConnection = tryRequest("weather?lat=41.8258");
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expected = new HashMap();
    expected.put("result", "error_bad_request");

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Map<String, Object>> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected.get("result"), actual.get("response").get("result"));
  }
}
