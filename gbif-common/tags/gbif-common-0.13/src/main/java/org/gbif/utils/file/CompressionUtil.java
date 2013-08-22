package org.gbif.utils.file;

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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Defaults keeping subDirectories to false.
   *
   * @see org.gbif.utils.file.CompressionUtil#decompressFile(java.io.File, java.io.File, boolean)
   */
  public static List<File> decompressFile(File directory, File compressedFile)
    throws IOException, UnsupportedCompressionType {
    return decompressFile(directory, compressedFile, false);
  }

  /**
   * Tries to decompress a file trying gzip or zip regardless of the filename or its suffix.
   *
   * @param directory      directory where archive's contents will be decompressed to
   * @param compressedFile compressed file
   *
   * @return list of files that have been extracted or null an empty list if archive couldn't be decompressed
   *
   * @throws IOException                if problem occurred reading compressed file, or directory couldn't be written
   *                                    to
   * @throws UnsupportedCompressionType if the compression type wasn't recognized
   */
  public static List<File> decompressFile(File directory, File compressedFile, boolean keepSubdirectories)
    throws IOException, UnsupportedCompressionType {
    List<File> files = null;
    // first try zip
    try {
      files = unzipFile(directory, compressedFile, keepSubdirectories);
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
   * Gunzip a file.  Use this method with isTarred false if the gzip contains a single file.  If it's a gzip
   * of a tar archive pass true to isTarred (or call @ungzipFile(directory, zipFile) which is what this method
   * just redirects to for isTarred).
   *
   * @param directory the output directory for the uncompressed file(s)
   * @param zipFile   the gzip file
   * @param isTarred  true if the gzip contains a tar archive
   *
   * @return a List of the uncompressed file name(s)
   *
   * @throws IOException if reading or writing fails
   */
  public static List<File> ungzipFile(File directory, File zipFile, boolean isTarred) throws IOException {
    if (isTarred) return ungzipFile(directory, zipFile);

    List<File> files = new ArrayList<File>();
    GZIPInputStream in = null;
    BufferedOutputStream dest = null;
    try {
      in = new GZIPInputStream(new FileInputStream(zipFile));

      // assume that the gzip filename is the filename + .gz
      String unzippedName = zipFile.getName().substring(0, zipFile.getName().lastIndexOf("."));
      File outputFile = new File(directory, unzippedName);
      LOG.debug("Extracting file: {} to: {}", unzippedName, outputFile.getAbsolutePath());
      FileOutputStream fos = new FileOutputStream(outputFile);

      dest = new BufferedOutputStream(fos, BUFFER);
      int count;
      byte[] data = new byte[BUFFER];
      while ((count = in.read(data, 0, BUFFER)) != -1) {
        dest.write(data, 0, count);
      }
      files.add(outputFile);
    } finally {
      if (in != null) in.close();
      if (dest != null) {
        dest.flush();
        dest.close();
      }
    }

    return files;
  }

  /**
   * Defaults keepSubdirectories to false.
   *
   * @see org.gbif.utils.file.CompressionUtil#unzipFile(java.io.File, java.io.File, boolean)
   */
  public static List<File> unzipFile(File directory, File zipFile) throws IOException {
     return unzipFile(directory, zipFile, false);
  }

  /**
   * Zip a directory with all files but skipping included subdirectories.
   * Only files directly within the directory are added to the archive.
   *
   * @param dir     the directory to zip
   * @param zipFile the zipped file
   */
  public static void zipDir(File dir, File zipFile) throws IOException {
    zipDir(dir, zipFile, false);
  }

  /**
   * Zip a directory with all files. Files in Subdirectories will be included if the inclSubdirs is true.
   *
   * @param dir     the directory to zip
   * @param zipFile the zipped file
   * @param inclSubdirs if true includes all subdirectories recursively
   */
  public static void zipDir(File dir, File zipFile, boolean inclSubdirs) throws IOException {
    Collection<File> files = org.apache.commons.io.FileUtils.listFiles(dir, null, inclSubdirs);
    zipFiles(files, dir, zipFile);
  }

  public static void zipFile(File file, File zipFile) throws IOException {
    Set<File> files = new HashSet<File>();
    files.add(file);
    zipFiles(files, file.getParentFile(), zipFile);
  }

  /**
   * Creates a zip archive from a given collection of files.
   * In order to preserve paths in the archive a rootContext can be specified which will be removed from the individual
   * zip entries. For example a rootContext of /home/freak with a file /home/freak/photo/birthday.jpg to be zipped
   * will result in a zip entry with a path photo/birthday.jpg.
   *
   * @param files to be included in the zip archive
   * @param rootContext optional path to be removed from each file
   * @param zipFile the zip file to be created
   * @throws IOException
   */
  public static void zipFiles(Collection<File> files, File rootContext, File zipFile) throws IOException {
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

          String zipPath = StringUtils.removeStart(f.getAbsolutePath(), rootContext.getAbsolutePath() + File.separator);
          ZipEntry entry = new ZipEntry(zipPath);
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

  /**
   * Extracts a zipped file into a target directory. Subdirectories or hidden files (i.e. files starting with a dot)
   * are being ignored according to the parameter keepSubdirectories.
   *
   * @param directory          where the zipped file and its subdirectories should be extracted to
   * @param zipFile            to extract
   * @param keepSubdirectories whether to preserve subdirectories or not
   *
   * @return a list of all created files
   */
  public static List<File> unzipFile(File directory, File zipFile, boolean keepSubdirectories) throws IOException {
    LOG.debug("Unzipping archive " + zipFile.getName() + " into directory: " + directory.getAbsolutePath());
    List<File> files = new ArrayList<File>();
    ZipFile zf = new ZipFile(zipFile);
    Enumeration<? extends ZipEntry> entries = zf.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      // is the entry a directory?
      if (entry.isDirectory()) {
        continue;
      } else {
        // destination file, different depending if we want to keep subdirectories or not
        File targetFile = (keepSubdirectories) ? new File(directory, entry.getName())
          : new File(directory, new File(entry.getName()).getName());
        // collect file
        files.add(targetFile);
        // ignore resource fork (__MACOSX)
        if (targetFile.getAbsolutePath().toUpperCase().contains("__MACOSX")) {
          LOG.debug("Ignoring file: " + targetFile.getAbsolutePath());
        } else {
          // ensure parent folder always exists
          createParentFolder(targetFile);
          // extract file
          extractFile(zf, entry, targetFile);
        }
      }
    }
    zf.close();
    return files;
  }

  /**
   * Extract an entry from a zipped file into a target file.
   *
   * @param zf         .zip file being unzipped (ZipFile)
   * @param entry      entry in .zip file currently being examined (ZipEntry)
   * @param targetFile destination file to extract to
   */
  private static void extractFile(ZipFile zf, ZipEntry entry, File targetFile) {
    try {
      LOG.debug("Extracting file: {} to: {}", entry.getName(), targetFile.getAbsolutePath());
      InputStream in = zf.getInputStream(entry);
      OutputStream out = new BufferedOutputStream(new FileOutputStream(targetFile));
      try {
        IOUtils.copy(zf.getInputStream(entry), out);
      } finally {
        in.close();
        out.close();
      }
    } catch (IOException e) {
      LOG.error("File could not be extraced: " + e.getMessage(), e);
    }
  }

  /**
   * Make parent folder.
   *
   * @param file destination file
   */
  private static void createParentFolder(File file) {
    File parent = new File(file.getParent());
    if (!parent.exists()) {
      LOG.debug((parent.mkdirs()) ? "Created parent directory: " + parent.getAbsolutePath()
        : "Failed to create parent directory: " + parent.getAbsolutePath());
    }
  }
}
