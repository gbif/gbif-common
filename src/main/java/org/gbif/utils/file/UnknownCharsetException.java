package org.gbif.utils.file;

public class UnknownCharsetException extends RuntimeException {

  public UnknownCharsetException(Exception e) {
    super(e);
  }

  public UnknownCharsetException(String message) {
    super(message);
  }

  public UnknownCharsetException(String message, Exception e) {
    super(message, e);
  }
}
