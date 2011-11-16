package org.gbif.metadata;

public class MetadataException extends Exception {

  public MetadataException(Exception e) {
    super(e);
  }

  public MetadataException(String message) {
    super(message);
  }

  public MetadataException(String message, Exception e) {
    super(message, e);
  }
}
