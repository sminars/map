package main.util;

import com.squareup.moshi.Moshi;
import java.util.Map;

/**
 * This class allows developers to easily serialize any external dataSource they provide to this
 * class. The user can call this class and pass in the object they want to serialize in Json format
 */
public record SerializeExternalData(Map<String, Object> response) {

  /**
   * Serilizes the given data
   * @return response in Json Format
   */
  String serializeData() {
    Moshi moshi = new Moshi.Builder().build();
    return moshi.adapter(SerializeExternalData.class).toJson(this);
  }
}

