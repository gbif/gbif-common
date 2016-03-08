package org.gbif.utils.file;

public class UnkownCharsetException extends RuntimeException {

  public UnkownCharsetException(Exception e) {
    super(e);
  }

  public UnkownCharsetException(String message) {
    super(message);
  }

  public UnkownCharsetException(String message, Exception e) {
    super(message, e);
  }
}
