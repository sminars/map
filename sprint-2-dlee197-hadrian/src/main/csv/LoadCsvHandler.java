package main.csv;

import com.squareup.moshi.Moshi;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * loads a csv; sees if the filepath given is valid.
 */
public class LoadCsvHandler implements Route {
  private final List<List<String>> csvData;
  private final HashMap<String, Boolean> validpaths;

  /**
   * Constructor for loadcsv.
   * @param data contetents of csv in string format
   * @param validpaths filepaths mapped to if the csv file has a header
   */
  public LoadCsvHandler(List<List<String>> data, HashMap<String, Boolean> validpaths) {
    this.csvData = data;
    this.validpaths = validpaths;
  }

  /**
   * attempts to load the csv file; errors displayed if not properly loaded.
   * @param request the request recived to the server
   * @param response the response  from the server
   * @return serialized response in Json
   * @throws Exception checks for exceptions
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String filepath = request.queryParams("filepath");
    Map<String, Object> replies = new HashMap<>();

    if (this.validpaths.containsKey(filepath)) {
      CSVParser parser = new CSVParser(new FileReader(filepath), new UserStrategy(),
          this.validpaths.get(filepath));
      List parsedData = parser.parseData();
      this.csvData.clear();
      if (parsedData.contains(null)) {
        replies.put("result", "error_bad_json");
        return new SerializeReplies(replies).serialize();
      }
      if (parsedData.isEmpty()) {
        replies.put("result", "error_bad_json");
        return new SerializeReplies(replies).serialize();
      }
      replies.put("result", "success");
      replies.put("filepath", filepath);
      this.csvData.add(parsedData);
      return new SerializeReplies(replies).serialize();
    } else {
      this.csvData.clear();
      replies.put("result", "error_datasource");
      return new SerializeReplies(replies).serialize();
    }
  }

  /**
   * Serialozed the response for loadcsv.
   * @return serialized response in Json Format
   */
  public record SerializeReplies(Map<String, Object> response) {
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SerializeReplies.class).toJson(this);
    }
  }
}