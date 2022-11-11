package main.csv;

import java.util.List;
import main.csv.CreatorFromRow;
import main.csv.FactoryFailureException;

/**
 * This class implements the CreatorFromRow interface. The class is designed to be edited by the
 * developer to create a strategy to determine the type of object they want the CSV parser to
 * convert each row into
 */
public class UserStrategy implements CreatorFromRow<List<String>> {

  private int rowLength = 0;

  /**
   * Takes a list of strings from the CSV data and returns a list of strings. Class is meant to be
   * chnaged by the user to enable the strategy they want to use on the CSV data
   *
   * @param row of the CSV data provided by the user
   * @return a List of Strings
   * @throws FactoryFailureException if Factory class fails
   */
  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    if (this.rowLength == 0) {
      this.rowLength = row.size();
    }
    if (this.rowLength != row.size()) {
      throw new FactoryFailureException(row);
    }
    return row;
  }
}