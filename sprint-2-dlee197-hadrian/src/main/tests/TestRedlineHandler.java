package main.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.map.Feature;
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

    Moshi moshi = new Moshi.Builder().build();
    RedlineResponse actual = moshi.adapter(RedlineResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    Set<Feature> returnfeatures = actual.response.data.getFeatures();
    assertEquals(1, returnfeatures.size());
    ArrayList<Feature> featureslist = new ArrayList(returnfeatures);
    assertEquals(featureslist.get(0).getProperties().getCity(),"Lincoln");
  }

  /**
   * Testing that the correct data is provided when an API call is made with
   * a slightly larger latitude and longitude bounding box
   * @throws IOException
   */
  @Test
  public void testGetLargerArea() throws IOException {
    HttpURLConnection clientConnection = tryRequest("getredlinedata?latmin=-97&latmax=-96&lonmin=40.83&lonmax=41");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    RedlineResponse actual = moshi.adapter(RedlineResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    Set<Feature> returnfeatures = actual.response.data.getFeatures();

    assertEquals(4, returnfeatures.size());
    ArrayList<String> citynames = new ArrayList<>();
    for(Feature feature: returnfeatures) {
      assertEquals((feature.getGeometry().getCoordinates().equals(null)),false);
      assertEquals(feature.getProperties().getCity(), "Lincoln");
      assertEquals(feature.getProperties().getState(), "NE");
      assertEquals(feature.getProperties().getName(), null);
    }
  }

  /**
   * Testing that the number of features returned when getting everything
   * is equal to the number of all the features in the geoJSON file
   * @throws IOException
   */
  @Test
  public void testGetEverything() throws IOException {
    HttpURLConnection clientConnection = tryRequest("getredlinedata");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    RedlineResponse actual = moshi.adapter(RedlineResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    Set<Feature> returnfeatures = actual.response.data.getFeatures();

    assertEquals(8878, returnfeatures.size());
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
