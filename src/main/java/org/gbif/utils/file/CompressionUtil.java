/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressionUtil {

  private CompressionUtil() {
    throw new UnsupportedOperationException("Can't initialize class");
  }

  public static class UnsupportedCompressionType extends RuntimeException {

    public UnsupportedCompressionType() {}

    public UnsupportedCompressionType(String message) {
      super(message);
    }

    public UnsupportedCompressionType(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(CompressionUtil.class);
  private static final int BUFFER = 2048;
  private static final String APPLE_RESOURCE_FORK = "__MACOSX";
  private static final byte[] TAR_MAGIC_BYTES = new byte[] {'u', 's', 't', 'a', 'r'};

  /**
   * Tries to decompress a file into a newly created temporary directory, trying gzip or zip regardless of the filename
   * or its suffix.
   *
   * @return folder containing all decompressed files
   */
  public static File decompressFile(File compressedFile)
      throws IOException, UnsupportedCompressionType {
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
   * Tries to decompress a file using TAR+gzip, TAR or Zip regardless of the filename or its suffix.
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
  public static List<File> decompressFile(
      File directory, File compressedFile, boolean keepSubdirectories)
      throws IOException, UnsupportedCompressionType {
    List<File> files = null;

    // Test before trying gzip format
    if (isGzipFormat(compressedFile)) {
      try {
        LOG.debug("Uncompressing {} with gzip compression to {}", compressedFile, directory);
        files = untgzFile(directory, compressedFile);
      } catch (Exception e) {
        LOG.debug("Not gzip compression");
      }
    }

    // Test before trying TAR format
    if (isTarFormat(compressedFile)) {
      try {
        LOG.debug("Uncompressing {} with TAR compression to {}", compressedFile, directory);
        files = untarFile(directory, compressedFile);
      } catch (Exception e) {
        LOG.debug("Not TAR compression");
      }
    }

    // Then try zip
    if (files == null) {
      try {
        LOG.debug("Uncompressing {} with Zip compression to {}", compressedFile, directory);
        files = unzipFile(directory, compressedFile, keepSubdirectories);
      } catch (ZipException e) {
        LOG.debug("Not Zip compression");
        throw new UnsupportedCompressionType("Unknown compression type. Neither gzip nor Zip", e);
      }
    }

    if (files.isEmpty()) {
      LOG.warn("No files extracted from {}, tried TGZ, TAR and Zip compression.", compressedFile);
    }

    return files;
  }

  /**
   * Check the file's first two bytes, to see if they are the gzip magic number.
   * @param compressedFile compressed file
   * @return               true if the file is in gzip format
   * @throws IOException   if a problem occurred reading compressed file
   */
  private static boolean isGzipFormat(File compressedFile) throws IOException {
    try (RandomAccessFile file = new RandomAccessFile(compressedFile, "r")) {
      return GZIPInputStream.GZIP_MAGIC == (file.read() & 0xff | ((file.read() << 8) & 0xff00));
    }
  }

  /**
   * Check the file is a Tape ARchive (TAR).
   * @param compressedFile compressed file
   * @return               true if the file is a TAR
   * @throws IOException   if a problem occurred reading compressed file
   */
  private static boolean isTarFormat(File compressedFile) throws IOException {
    try (RandomAccessFile file = new RandomAccessFile(compressedFile, "r")) {
      // TAR files contain "ustar\0" or "ustar " at byte 257.
      // https://www.gnu.org/software/tar/manual/html_node/Standard.html
      byte[] at257 = new byte[5];
      file.seek(257);
      file.read(at257, 0, 5);
      return Arrays.equals(at257, TAR_MAGIC_BYTES);
    } catch (Exception e) {
      LOG.debug("Exc", e);
    }
    return false;
  }

  /**
   * Extracts a gzipped TAR file. Directory structure and hidden files (i.e. files starting with a dot) are ignored.
   *
   * @param directory where the file should be extracted to
   * @param tgzFile   to extract
   *
   * @return a list of all created files
   */
  public static List<File> untgzFile(File directory, File tgzFile) throws IOException {
    return untarStream(directory, new GZIPInputStream(new FileInputStream(tgzFile)));
  }

  /**
   * Extracts a plain TAR file. Directory structure and hidden files (i.e. files starting with a dot) are ignored.
   *
   * @param directory where the file should be extracted to
   * @param tarFile   to extract
   *
   * @return a list of all created files
   */
  public static List<File> untarFile(File directory, File tarFile) throws IOException {
    return untarStream(directory, new FileInputStream(tarFile));
  }

  /**
   * Extracts a TAR stream. Directory structure and hidden files (i.e. files starting with a dot) are ignored.
   *
   * @param directory where the file should be extracted to
   * @param tarStream to extract
   *
   * @return a list of all created files
   */
  private static List<File> untarStream(File directory, InputStream tarStream) throws IOException {
    List<File> files = new ArrayList<File>();
    try (TarArchiveInputStream in = new TarArchiveInputStream(tarStream)) {
      TarArchiveEntry entry;
      while ((entry = in.getNextTarEntry()) != null) {
        if (entry.isDirectory()) {
          LOG.debug("TAR archive contains directories which are being ignored");
          continue;
        }
        String fn = new File(entry.getName()).getName();
        if (fn.startsWith(".")) {
          LOG.debug("TAR archive contains a hidden file {} which is being ignored", fn);
          continue;
        }
        File targetFile = new File(directory, fn);
        if (targetFile.exists()) {
          LOG.warn("TAR archive contains duplicate filename {}, only the first was extracted", fn);
          continue;
        }
        LOG.debug("Extracting file: {} to: {}", entry.getName(), targetFile.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(targetFile)) {
          IOUtils.copy(in, out);
        }
        files.add(targetFile);
      }
    }
    return files;
  }

  /**
   * Gunzip a file.  Use this method with isTarred false if the gzip contains a single file.  If it's a gzip
   * of a TAR pass true to isTarred (or call @untgzFile(directory, tgzFile) which is what this method
   * just redirects to for isTarred).
   *
   * @param directory the output directory for the uncompressed file(s)
   * @param gzipFile  the gzip file
   * @param isTarred  true if the gzip contains a TAR
   *
   * @return a List of the uncompressed file name(s)
   *
   * @throws IOException if reading or writing fails
   */
  public static List<File> ungzipFile(File directory, File gzipFile, boolean isTarred)
      throws IOException {
    if (isTarred) return untgzFile(directory, gzipFile);

    List<File> files = new ArrayList<File>();
    GZIPInputStream in = null;
    BufferedOutputStream dest = null;
    try {
      in = new GZIPInputStream(new FileInputStream(gzipFile));

      // assume that the gzip filename is the filename + .gz
      String unzippedName = gzipFile.getName().substring(0, gzipFile.getName().lastIndexOf("."));
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
  public static void zipFiles(Collection<File> files, File rootContext, File zipFile)
      throws IOException {
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

          String zipPath =
              StringUtils.removeStart(
                  f.getAbsolutePath(), rootContext.getAbsolutePath() + File.separator);
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
   * Extracts a zipped file into a target directory. If the file is wrapped in a root directory, this is removed by
   * default. Other subdirectories are ignored according to the parameter keepSubdirectories.
   * </br>
   * The following types of files are also ignored by default:
   * i) hidden files (i.e. files starting with a dot)
   * ii) Apple resource fork (__MACOSX), including its subdirectories and subfiles
   *
   * @param directory          where the zipped file and its subdirectories should be extracted to
   * @param zipFile            to extract
   * @param keepSubdirectories whether to preserve subdirectories or not
   *
   * @return a list of all created files and directories extracted to target directory
   */
  public static List<File> unzipFile(File directory, File zipFile, boolean keepSubdirectories)
      throws IOException {
    LOG.debug(
        "Unzipping archive "
            + zipFile.getName()
            + " into directory: "
            + directory.getAbsolutePath());

    // This is changed from using ZipFile to a ZipInputStream since Java 8u192 can't open certain
    // Zip64 files.
    // https://bugs.openjdk.java.net/browse/JDK-8186464
    try (FileInputStream fInput = new FileInputStream(zipFile);
        ZipInputStream zipInput = new ZipInputStream(fInput)) {
      ZipEntry entry;

      while ((entry = zipInput.getNextEntry()) != null) {
        // ignore resource fork directories and subfiles
        if (entry.getName().toUpperCase().contains(APPLE_RESOURCE_FORK)) {
          LOG.debug("Ignoring resource fork file: " + entry.getName());
        }
        // ignore directories and hidden directories (e.g. .svn) (based on flag)
        else if (entry.isDirectory()) {
          if (isHiddenFile(new File(entry.getName()))) {
            LOG.debug("Ignoring hidden directory: " + entry.getName());
          } else if (keepSubdirectories) {
            new File(directory, entry.getName()).mkdir();
          } else {
            LOG.debug("Ignoring (sub)directory: " + entry.getName());
          }
        }
        // ignore hidden files
        else {
          if (isHiddenFile(new File(entry.getName()))) {
            LOG.debug("Ignoring hidden file: " + entry.getName());
          } else {
            File targetFile =
                (keepSubdirectories)
                    ? new File(directory, entry.getName())
                    : new File(directory, new File(entry.getName()).getName());
            // ensure parent folder always exists, and extract file
            createParentFolder(targetFile);

            LOG.debug("Extracting file: {} to: {}", entry.getName(), targetFile.getAbsolutePath());
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetFile.toPath()))) {
              IOUtils.copy(zipInput, out);
            }
          }
        }
      }
    }
    // remove the wrapping root directory and flatten structure
    if (keepSubdirectories) {
      removeRootDirectory(directory);
    }

    File[] files = directory.listFiles();

    return (files == null) ? new ArrayList<>() : Arrays.asList(files);
  }

  /**
   * @return true if file is a hidden file or directory, or if any of its parent directories are hidden checking
   * recursively
   */
  private static boolean isHiddenFile(File f) {
    if (f.getName().startsWith(".")) {
      return true;
    } else if (f.getParentFile() != null) {
      return isHiddenFile(f.getParentFile());
    }
    return false;
  }

  /**
   * Removes a wrapping root directory and flatten its structure by moving all that root directory's files and
   * subdirectories up to the same level as the root directory.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void removeRootDirectory(File directory) {
    File[] rootFiles = directory.listFiles((FileFilter) HiddenFileFilter.VISIBLE);
    if (rootFiles == null) {
      LOG.error("Failed to retrieve root directory from {}", directory.getAbsolutePath());
      return;
    }

    if (rootFiles.length != 1) {
      LOG.error("More than one root directory at {}", directory.getAbsolutePath());
      return;
    }

    File root = rootFiles[0];
    if (root.isDirectory()) {
      LOG.debug("Removing single root folder {} found in decompressed archive", root.getAbsoluteFile());
      Collection<File> filesAndDirs = FileUtils.listFilesAndDirs(root, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
      // directories shouldn't be in the end, sort
      List<File> sortedFilesAndDirs = filesAndDirs.stream()
          .sorted(Comparator.comparing(File::getAbsolutePath))
          .collect(Collectors.toList());

      for (File f : sortedFilesAndDirs) {
        File f2 = new File(directory, f.getName());
        f.renameTo(f2);
      }
      root.delete();
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
      LOG.debug(
          (parent.mkdirs())
              ? "Created parent directory: " + parent.getAbsolutePath()
              : "Failed to create parent directory: " + parent.getAbsolutePath());
    }
  }
}
