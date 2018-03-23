package org.gbif.utils.file.csv;

import java.io.IOException;

public class UnknownDelimitersException extends IOException {

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
