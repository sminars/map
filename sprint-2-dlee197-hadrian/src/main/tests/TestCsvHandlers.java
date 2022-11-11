package main.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.csv.CSVParser;
import main.csv.GetCsvHandler;
import main.csv.LoadCsvHandler;
import main.csv.UserStrategy;
import main.weather.WeatherHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

/**
 * Tests the loadcsv and getcsv tests. Uses integration testing to test how the 2 interact and unit
 * testing to check for specific parts of each class
 */
public class TestCsvHandlers {
  HashMap<String, Boolean> validpaths = new HashMap<>();
  List<List<String>> data = new ArrayList<>();

  //Sets up ports
  @BeforeAll
  public static void setup_before() {
    Spark.port(11);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  //sets up the mocks for the tests
  @BeforeEach
  public void setup() {
    this.validpaths.put("src/main/csv/data/mockdata1.csv",true); //valid csv
    this.validpaths.put("src/main/csv/data/mockdata2.csv",true); //valid csv
    this.validpaths.put("src/main/csv/data/emptyCsv.csv",false); //Invalid empty csv
    this.validpaths.put("src/main/csv/data/badFormat.csv",true); //Invalid csv, wrong format

    Spark.get("loadcsv", new LoadCsvHandler(this.data, this.validpaths));
    Spark.get("getcsv", new GetCsvHandler(this.data));
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("/loadcsv");
    Spark.unmap("/getcsv");
    Spark.unmap("/weather");
    Spark.awaitStop();
  }

  //sets up connection
  static private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:"+Spark.port()+"/"+apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.connect();
    return clientConnection;
  }

  //tests if the load csv works and integration testing to see if changes result if another is shown
  @Test
  public void testLoadCsvSuccess() throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=src/main/csv/data/mockdata1.csv");
    clientConnection.getResponseCode();
    //check valid connection
    assertEquals(200, clientConnection.getResponseCode());

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "success");
    expectedResult.put("filepath", "src/main/csv/data/mockdata1.csv");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    //test successful request
    assertEquals(expected, actual);

    HttpURLConnection clientConnection1 = tryRequest("loadcsv?filepath=src/main/csv/data/mockdata2.csv");
    clientConnection1.getResponseCode();
    //check valid connection
    assertEquals(200, clientConnection.getResponseCode());

    //makes the expected response
    Map<String, Object> expectedResult1 = new HashMap();
    expectedResult1.put("result", "success");
    expectedResult1.put("filepath", "src/main/csv/data/mockdata2.csv");
    Map<String, Map<String,Object>> expected1 = new HashMap();
    expected1.put("response", expectedResult1);

    //retrieves the actual server response
    Map<String, Object> actual1 = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    //test to see if changes when mock2 is entered
    assertEquals(expected1, actual1);
  }

  //tests csv with wrong filepath
  @Test
  public void testLoadCsvErrorWrongFilepath() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=invalidFilepath");
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_datasource");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual);
  }

  //Tests a load csv with null filepath
  @Test
  public void testLoadCsvErrorNullFilepath() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=");
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_datasource");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual);
  }

  @Test
  public void testLoadCsvBadFormatCsv() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=src/main/csv/data/badFormat.csv");
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_bad_json");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
  }

  //tests loading an CSV with ill format that is empty
  @Test
  public void testLoadCsvEmptyFile() throws IOException {
    HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=src/main/csv/data/emptyCsv.csv");
    clientConnection.getResponseCode();

    //makes the expected response
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_bad_json");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    assertEquals(expected, actual);
  }

  //Tests valid csv and load before
  @Test
  public void testGetCsvSuccess() throws IOException{
    HttpURLConnection loadConnection = tryRequest("loadcsv?filepath=src/main/csv/data/mockdata1.csv");
    loadConnection.getResponseCode();
    assertEquals(200, loadConnection.getResponseCode());

    HttpURLConnection getConnection = tryRequest("getcsv");
    getConnection.getResponseCode();
    assertEquals(200, getConnection.getResponseCode());

    //makes the expected response
    CSVParser<List<String>> parser = new CSVParser(
        new FileReader("src/main/csv/data/mockdata1.csv"), new UserStrategy(), true);
    List<List<String>> contents = parser.parseData();
    List<List<List<String>>> contentsList = new ArrayList<>();
    contentsList.add(contents);
    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "success");
    expectedResult.put("data", contentsList);
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(getConnection.getInputStream()));

    assertEquals(expected, actual);
  }

  //tests get without loading first error should appear
  @Test
  public void testGetCsvError() throws IOException{
    HttpURLConnection getConnection = tryRequest("getcsv");
    getConnection.getResponseCode();
    assertEquals(200, getConnection.getResponseCode());

    Map<String, Object> expectedResult = new HashMap();
    expectedResult.put("result", "error_datasource");
    Map<String, Map<String,Object>> expected = new HashMap();
    expected.put("response", expectedResult);

    //retrieves the actual server response
    Moshi moshi = new Moshi.Builder().build();
    Map<String, Object> actual = moshi.adapter(Map.class)
        .fromJson(new Buffer().readFrom(getConnection.getInputStream()));

    assertEquals(expected, actual);
  }
}
