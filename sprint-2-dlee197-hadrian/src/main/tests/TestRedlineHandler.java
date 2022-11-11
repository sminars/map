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
import main.map.RedlineHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestRedlineHandler {

  //Sets up ports
  @BeforeAll
  public static void setup_before() {
    Spark.port(11);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  //sets up the mocks for the tests
  @BeforeEach
  public void setup() {
    Spark.get("getredlinedata", new RedlineHandler());
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("/getredlinedata");
    Spark.awaitStop();
  }


  static private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Testing that the correct data is provided when an API call is made with
   * valid latitude and longitude ranges
   * @throws IOException
   */
  @Test
  public void testGetSmallArea() throws IOException {
    HttpURLConnection clientConnection = tryRequest("getredlinedata?latmin=-97&latmax=-96&lonmin=40.85&lonmax=41");
    assertEquals(200, clientConnection.getResponseCode());

    String expected = "{response={result=success, data={features=[{geometry={coordinates=[[[[-96.624807, 40.85673], [-96.624845, 40.8525], [-96.639993, 40.852794], [-96.640032, 40.857053], [-96.624807, 40.85673]]]], type=MultiPolygon}, properties={city=Lincoln, holc_grade=B, holc_id=B9, neighborhood_id=9572, state=NE}, type=Feature}], type=FeatureCollection}}}";

    Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("success", new HashMap<>());
    
    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(expected, actual.toString());
  }

  /**
   * Testing that an error response is outputted when the incorrect number
   * of parameters is provided
   * @throws IOException
   */
  @Test
  public void testWrongNumberOfParameters() throws IOException {
    HttpURLConnection clientConnection = tryRequest("getredlinedata?latmin=-97&latmax=-96&lonmin=40.85");
    assertEquals(200, clientConnection.getResponseCode());

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_bad_request");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual);

  }

  /**
   * Testing that an error response is outputted when the minimum latitude is greater
   * than the maximum latitude
   * @throws IOException
   */
  @Test
  public void testInvalidLatBounds() throws IOException {
    HttpURLConnection clientConnection = tryRequest("getredlinedata?latmin=85&latmax=80&lonmin=40.85&lonmax=41");
    assertEquals(200, clientConnection.getResponseCode());

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_bad_request");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual);

  }

  /**
   * Testing that an error response is outputted when the minimum longitude is greater
   * than the maximum longitude
   * @throws IOException
   */
  @Test
  public void testInvalidLonBounds() throws IOException {
    HttpURLConnection clientConnection = tryRequest("getredlinedata?latmin=40&latmax=41&lonmin=100&lonmax=90");
    assertEquals(200, clientConnection.getResponseCode());

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_bad_request");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual);

  }

  /**
   * Testing that an error response is outputted when a user provides latitude and longitude
   * ranges where no redlining data is available
   * @throws IOException
   */
  @Test
  public void testNoAvailableData() throws IOException {
    // clicking an area where no redlining data is available
    HttpURLConnection clientConnection = tryRequest("getredlinedata?latmin=0&latmax=96&lonmin=40.85&lonmax=41");
    assertEquals(200, clientConnection.getResponseCode());

    String expected = "{response={result=success, data={features=[], type=FeatureCollection}}}";

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual.toString());
  }



}
