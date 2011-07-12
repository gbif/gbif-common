package org.gbif.utils.file;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class CompressionUtil {

  private CompressionUtil() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  public static class UnsupportedCompressionType extends RuntimeException {

    public UnsupportedCompressionType() {
    }

    public UnsupportedCompressionType(String message) {
      super(message);
    }

    public UnsupportedCompressionType(String message, Throwable cause) {
      super(message, cause);
    }

  }

  private static final Logger LOG = LoggerFactory.getLogger(CompressionUtil.class);
  private static final int BUFFER = 2048;

  /**
   * Tries to decompress a file into a newly created temporary directory, trying gzip or zip regardless of the filename
   * or its suffix.
   *
   * @return folder containing all decompressed files
   */
  public static File decompressFile(File compressedFile) throws IOException, UnsupportedCompressionType {
    // create empty tmp dir
    File dir = File.createTempFile("gbif-", null);
    if (dir.exists() && !dir.delete()) {
      throw new IOException("Couldn't delete temporary directory");
    }

    if (!dir.mkdirs()) {
      throw new IOException("Couldn't create temporary directory for decompression");
    }

    // decompress
    decompressFile(dir, compressedFile);

    return dir;
  }

  /**
   * Tries to decompress a file trying gzip or zip regardless of the filename or its suffix.
   *
   * @return list of decompressed files or empty list if archive couldn't be decompressed
   */
  public static List<File> decompressFile(File directory, File compressedFile)
    throws IOException, UnsupportedCompressionType {
    List<File> files = null;
    // first try zip
    try {
      files = unzipFile(directory, compressedFile);
    } catch (ZipException e) {
      LOG.debug("No zip compression");
    }

    // Try gzip if needed
    if (files == null) {
      try {
        files = ungzipFile(directory, compressedFile);
      } catch (Exception e) {
        LOG.debug("No gzip compression");
        throw new UnsupportedCompressionType("Unknown compression type. Neither zip nor gzip", e);
      }
    }

    return files;
  }

  /**
   * Extracts a gzipped file. Subdirectories or hidden files (i.e. files starting with a dot) are being ignored.
   *
   * @param directory where the file should be extracted to
   * @param zipFile   to extract
   *
   * @return a list of all created files
   */
  public static List<File> ungzipFile(File directory, File zipFile) throws IOException {
    List<File> files = new ArrayList<File>();
    TarArchiveInputStream in = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(zipFile)));
    try {
      TarArchiveEntry entry = in.getNextTarEntry();
      while (entry != null) {
        if (entry.isDirectory()) {
          LOG.debug("TAR archive contains directories which are being ignored");
          entry = in.getNextTarEntry();
          continue;
        }
        String fn = new File(entry.getName()).getName();
        if (fn.startsWith(".")) {
          LOG.debug("TAR archive contains a hidden file which is being ignored");
          entry = in.getNextTarEntry();
          continue;
        }
        File targetFile = new File(directory, fn);
        if (targetFile.exists()) {
          LOG.warn("TAR archive contains duplicate filenames, only the first is being extracted");
          entry = in.getNextTarEntry();
          continue;
        }
        LOG.debug("Extracting file: {} to: {}", entry.getName(), targetFile.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(targetFile);
        try {
          IOUtils.copy(in, out);
          out.close();
        } finally {
          IOUtils.closeQuietly(out);
        }
        files.add(targetFile);
      }
    } finally {
      in.close();
    }
    return files;
  }

  /**
   * Extracts a zipped file. Subdirectories or hidden files (i.e. files starting with a dot) are being ignored.
   *
   * @param directory where the file should be extracted to
   * @param zipFile   to extract
   *
   * @return a list of all created files
   */
  public static List<File> unzipFile(File directory, File zipFile) throws IOException {
    ZipFile zf = new ZipFile(zipFile);
    Enumeration<? extends ZipEntry> entries = zf.entries();
    List<File> files = new ArrayList<File>();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.isDirectory()) {
        LOG.debug("ZIP archive contains directories which are being ignored");
        continue;
      }
      String fn = new File(entry.getName()).getName();
      if (fn.startsWith(".")) {
        LOG.debug("ZIP archive contains a hidden file which is being ignored");
        continue;
      }
      File targetFile = new File(directory, fn);
      files.add(targetFile);
      LOG.debug("Extracting file: {} to: {}", entry.getName(), targetFile.getAbsolutePath());

      InputStream in = zf.getInputStream(entry);
      OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
      try {
        IOUtils.copy(zf.getInputStream(entry), out);
      } finally {
        in.close();
        out.close();
      }
    }
    zf.close();
    return files;
  }

  /**
   * Zip a directory with all files but skipping included subdirectories. Only files directly within the directory are
   * added to the archive.
   *
   * @param dir     the directory to zip
   * @param zipFile the zipped file
   */
  public static void zipDir(File dir, File zipFile) throws IOException {
    Set<File> files = new HashSet<File>();
    for (File f : dir.listFiles()) {
      if (f.isFile()) {
        files.add(f);
      }
    }
    zipFiles(files, zipFile);
  }

  public static void zipFile(File file, File zipFile) throws IOException {
    Set<File> files = new HashSet<File>();
    files.add(file);
    zipFiles(files, zipFile);
  }

  public static void zipFiles(Collection<File> files, File zipFile) throws IOException {
    if (files.isEmpty()) {
      LOG.info("no files to zip.");
    } else {
      try {
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        // out.setMethod(ZipOutputStream.DEFLATED);
        byte[] data = new byte[BUFFER];
        for (File f : files) {
          LOG.debug("Adding file {} to archive", f);
          FileInputStream fi = new FileInputStream(f);
          origin = new BufferedInputStream(fi, BUFFER);
          ZipEntry entry = new ZipEntry(f.getName());
          out.putNextEntry(entry);
          int count;
          while ((count = origin.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
          }
          origin.close();
        }
        out.finish();
        out.close();
      } catch (IOException e) {
        LOG.error("IOException while zipping files: {}", files);
        throw e;
      }
    }
  }
}
