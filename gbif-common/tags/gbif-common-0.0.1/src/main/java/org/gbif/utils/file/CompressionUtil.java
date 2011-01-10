package org.gbif.utils.file;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
  public static class UnsupportedCompressionType extends RuntimeException {

  }

  protected static final Logger log = Logger.getLogger(CompressionUtil.class);
  static final int BUFFER = 2048;

  private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while ((len = in.read(buffer)) >= 0) {
      out.write(buffer, 0, len);
    }

    in.close();
    out.close();
  }

  /**
   * Tries to decompress a file into a newly created temporary directory,
   * trying gzip or zip regardless of the filename or its suffix.
   * 
   * @param compressedFile
   * @return folder containing all decompressed files
   * @throws IOException
   */
  public static File decompressFile(File compressedFile) throws IOException,UnsupportedCompressionType {
	  // create empty tmp dir
	  File dir = File.createTempFile("dwca-", null);
	  if (dir.exists()){
		  dir.delete();
	  }
      dir.mkdirs();
      
      // decompress
      decompressFile(dir,compressedFile);

    return dir;
  }

  /**
   * Tries to decompress a file trying gzip or zip regardless of the filename or its suffix.
   * 
   * @param directory
   * @param compressedFile
   * @return list of decompressed files or empty list if archive couldnt be decompressed
   * @throws IOException
   */
  public static List<File> decompressFile(File directory, File compressedFile) throws IOException,
      UnsupportedCompressionType {
    List<File> files = new ArrayList<File>();
    // first try zip
    try {
      files = CompressionUtil.unzipFile(directory, compressedFile);
    } catch (ZipException e) {
      // nope. try gzip
      try {
        files = CompressionUtil.ungzipFile(directory, compressedFile);
      } catch (Exception e1) {
        log.warn("Unknown compression type. Neither zip nor gzip");
        throw new UnsupportedCompressionType();
      }
    }
    return files;
  }

  /**
   * @param compressedFile
   * @return boolean
   */
  private static boolean isGzip(File compressedFile) {
    String name = compressedFile.getName();
    return GzipUtils.isCompressedFilename(name);
  }

  /**
   * @param compressedFile
   * @return boolean
   */
  private static boolean isZip(File compressedFile) {
    String name = compressedFile.getName();
    return name.toLowerCase().endsWith(".zip");
  }

  public static List<File> ungzipFile(File directory, File zipFile) throws IOException {
    List<File> files = new ArrayList<File>();
    TarArchiveInputStream in = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(zipFile)));
    try {
      TarArchiveEntry entry = in.getNextTarEntry();
      while (entry != null) {
        if (entry.isDirectory()) {
          log.warn("TAR archive contains directories which are being ignored");
          entry = in.getNextTarEntry();
          continue;
        }
        String fn = new File(entry.getName()).getName();
        if (fn.startsWith(".")) {
          log.warn("TAR archive contains a hidden file which is being ignored");
          entry = in.getNextTarEntry();
          continue;
        }
        File targetFile = new File(directory, fn);
        if (targetFile.exists()) {
          log.warn("TAR archive contains duplicate filenames, only the first is being extracted");
          entry = in.getNextTarEntry();
          continue;
        }
        log.debug("Extracting file: " + entry.getName() + " to: " + targetFile.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(targetFile);
        IOUtils.copy(in, out);
        out.close();
        files.add(targetFile);
      }
    } finally {
      in.close();
    }
    return files;
  }

  public static List<File> unzipFile(File directory, File zipFile) throws IOException {
    ZipFile zf = new ZipFile(zipFile);
    Enumeration entries = zf.entries();
    List<File> files = new ArrayList<File>();
    while (entries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      if (entry.isDirectory()) {
        log.warn("ZIP archive contains directories which are being ignored");
        continue;
      }
      String fn = new File(entry.getName()).getName();
      if (fn.startsWith(".")) {
        log.warn("ZIP archive contains a hidden file which is being ignored");
        continue;
      }
      File targetFile = new File(directory, fn);
      files.add(targetFile);
      log.debug("Extracting file: " + entry.getName() + " to: " + targetFile.getAbsolutePath());
      copyInputStream(zf.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(targetFile)));
    }
    zf.close();
    return files;
  }

  /** Zip a directory with all files but skipping included subdirectories.
   * Only files directly within the directory are added to the archive.
 * @param dir the directory to zip
 * @param zipFile the zipped file
 * @throws IOException
 */
public static void zipDir(File dir, File zipFile) throws IOException {
	    Set<File> files = new HashSet<File>();
	    for (File f : dir.listFiles()){
	    	if (f.isFile()){
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

  public static void zipFiles(Set<File> files, File zipFile) throws IOException {
    if (files.isEmpty()) {
      log.warn("no files to zip.");
    } else {
      try {
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(zipFile);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        // out.setMethod(ZipOutputStream.DEFLATED);
        byte data[] = new byte[BUFFER];
        for (File f : files) {
          log.debug("Adding file " + f + " to archive");
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
        log.error("IOException while zipping files: " + files);
        throw e;
      }
    }
  }
}
