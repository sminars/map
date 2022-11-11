package main.csv;

import com.squareup.moshi.Moshi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * This class is responsible for when getcsv is provided as a pathway retrieves the last load csv.
 */
public class GetCsvHandler implements Route {
  public List<List<String>> data;

  /**
   * constructor for getcsv.
   * @param currentCSV last loaded csv
   * */
  public GetCsvHandler(List<List<String>> currentCSV) {
    this.data = currentCSV;
  }

  /**
   * attempts get the given csv; If successful retrieves the contents if not then error message is
   * displayed.
   * @param request the request recived to the server
   * @param response the response  from the server
   * @return serialized response
   * @throws Exception checks for exceptions
   */
  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> replies = new HashMap<>();

    if (this.data.isEmpty()) {
      replies.put("result", "error_datasource");
      return new SerializeReplies(replies).serialize();
    } else {
      replies.put("result", "success");
      replies.put("data", this.data);
      return new SerializeReplies(replies).serialize();
    }
  }

  /**
   * Serialozed the response for getcsv.
   * @return serialized response in Json Format
   */
  public record SerializeReplies(Map<String, Object> response) {
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(SerializeReplies.class).toJson(this);
    }
  }
}
