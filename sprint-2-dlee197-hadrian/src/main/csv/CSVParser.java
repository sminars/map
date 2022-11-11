package main.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * uses CSV data provided by the user and uses it to count the rows, columns, characters and words
 * The user can provide a creator strategy interface which can determine which type of object the
 * CSV parser will turn the row into.
 *
 * @param <T> Generic type allows the user to decide which type the data will be transformed into
 */
public class CSVParser<T> {

  private final BufferedReader input;
  private CreatorFromRow<T> creator = null;
  private final boolean hasHeaderRow;

  /**
   * This constructor should be called when the user wants a strategy-based interface on how the csv
   * data is handled.
   *
   * @param reader csv data of type Reader
   * @param creator the strategy interface the user wants to apply on the csv data
   */
  public CSVParser(Reader reader, CreatorFromRow<T> creator, Boolean hasHeaderRow) {
    this.input = new BufferedReader(reader);

    if (creator == null) {
      throw new IllegalArgumentException("Not a valid creator strategy");
    }
    this.creator = creator;
    this.hasHeaderRow = hasHeaderRow;
  }

  /**
   * Calculates the row count, word count, character count and column count for the CSV file. Only
   * counts the headersin the counts if the header is the onlu line in the file
   *
   * @return List containing in order the row count, word count, character count and column count
   * @throws IOException if the data by the CSV is invalid
   */
  public List<Integer> calculateCounts() throws IOException {
    int rowCount = 0;
    int wordCount = 0;
    int charCount = 0;
    int columnCount = 0;

    String line = this.getData();

    String[] CSVRow;
    while (line != null) {
      CSVRow = (line.split(",")); // CSVRow array of all words in csv row
      if (columnCount < CSVRow.length) {
        columnCount = CSVRow.length; // gets the csv row with the most column in it
      }
      for (String s : CSVRow) {
        String[] wordArr = s.trim().split(" ");
        for (String word : wordArr) {
          if (!word.equals(" ") && !word.equals("")) {
            wordCount++;
            charCount += word.length();
          }
        }
      }
      line = this.input.readLine();
      rowCount++;
    }
    return List.of(wordCount, charCount, rowCount, columnCount);
  }

  /**
   * This method uses the calculateCounts() method to calculate the amount of rows columns
   * characters and words and prints the amounts out.
   */
  public void printCounts() {
    List<Integer> counts = null;
    try {
      counts = this.calculateCounts();
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Improper CSV file.");
    }

    System.out.println("Words: " + counts.get(0));
    System.out.println("Characters: " + counts.get(1));
    System.out.println("Rows: " + counts.get(2));
    System.out.println("Columns: " + counts.get(3));
  }

  /**
   * reads in the csv Data then uses parseCreatorRow() to which uses the create() method from the
   * CreatorFromRow interface to convert each row of the CSV data in the users desired strategy.
   *
   * @return List of the objects the user wants to turn the csv data rows into.
   */
  public List<T> parseData() {
    try {
      List<T> parsedCSV = new ArrayList<>();
      String line = this.getData();
      while (line != null) {
        parsedCSV.add(this.parseCreatorRow(line));
        line = this.input.readLine();
      }
      return parsedCSV;
    } catch (IOException e) {
      System.err.println("Please use a valid file. " + e.getMessage());
      return null;
    }
  }

  /**
   * Called in parseData() and calculateCounts() this method will determine if the CSV file has more
   * than one line. If it does the head row of the file will be ignored. If the CSV file contains
   * only one row it will not be ignored and will be given as data
   *
   * @return a row of the CSV data as a String
   */
  private String getData() {
    try {
      if (this.hasHeaderRow) {
        this.input.readLine(); // header first line of file
        String line = this.input.readLine(); // Second line of file.
        return line;
      } else {
        String line = this.input.readLine();
        return line;
      }
    } catch (IOException e) {
      System.err.println("Please use a valid CSV.");
      return null;
    }
  }

  /**
   * Turns a row from the CSV data into the users preference.
   *
   * @param line row in csv Data.
   * @return type depending on how the user edits CreatorFromRow. Null if an invalid file is used.
   */
  public T parseCreatorRow(String line) {
    List<String> row = List.of((line.split(",")));
    try {
      return this.creator.create(row);
    } catch (FactoryFailureException e) {
      e.printStackTrace();
      return null;
    }
  }
}
