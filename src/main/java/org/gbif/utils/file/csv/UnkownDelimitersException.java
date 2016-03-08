package org.gbif.utils.file.csv;

public class UnkownDelimitersException extends RuntimeException {

  public UnkownDelimitersException(Exception e) {
    super(e);
  }

  public UnkownDelimitersException(String message) {
    super(message);
  }

  public UnkownDelimitersException(String message, Exception e) {
    super(message, e);
  }
}