package org.gbif.utils.file.csv;

public class UnknownDelimitersException extends RuntimeException {

  public UnknownDelimitersException(Exception e) {
    super(e);
  }

  public UnknownDelimitersException(String message) {
    super(message);
  }

  public UnknownDelimitersException(String message, Exception e) {
    super(message, e);
  }
}
