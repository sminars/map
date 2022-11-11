package main.server;

import static spark.Spark.after;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import main.csv.GetCsvHandler;
import main.csv.LoadCsvHandler;
import main.map.RedlineHandler;
import spark.Spark;
import main.weather.WeatherHandler;
/**
 * Top-level class for this demo. Contains the main() method which starts Spark and runs the various handlers.
 *
 * We have three endpoints. get csv, loadcsv and getweather
 */
public class Server {

    public static void main(String[] args) {
      HashMap<String, Boolean> validpaths = new HashMap<>();
      List<List<String>> currCSVData = new ArrayList<>();

      //mock data filepaths and has headers recorded
      validpaths.put("src/main/csv/data/mockdata1.csv",true);
      validpaths.put("src/main/csv/data/mockdata2.csv",true);
      validpaths.put("src/main/csv/data/emptyCsv.csv", false);
      validpaths.put("src/main/csv/data/badFormat.csv",true);

      Spark.port(133);

      after((request, response) -> {
        response.header("Access-Control-Allow-Origin", "*");
        response.header("Access-Control-Allow-Methods", "*");
      });

      //endpoints
      Spark.get("loadcsv", new LoadCsvHandler(currCSVData, validpaths));
      Spark.get("getcsv", new GetCsvHandler(currCSVData));
      Spark.get("weather", new WeatherHandler());
      Spark.get("getredlinedata", new RedlineHandler());

      Spark.init();
      Spark.awaitInitialization();
      System.out.println("Server started.");
    }
}
