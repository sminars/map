//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package main.csv;

import java.util.List;

public class FactoryFailureException extends Exception {
  final List<String> row;

  public FactoryFailureException(List<String> row) {
    this.row = row;
  }
}
