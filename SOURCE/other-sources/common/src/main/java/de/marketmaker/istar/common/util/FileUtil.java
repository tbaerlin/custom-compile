/*
 * FileUtil.java
 *
 * Created on 29.11.2004 15:32:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public final static Comparator<File> COMPARE_BY_LAST_MODIFIED = new Comparator<File>() {
        public int compare(File o1, File o2) {
            return new Long(o1.lastModified()).compareTo(o2.lastModified());
        }
    };

    public final static Comparator<File> COMPARE_BY_ABSOLUTE_PATH = new Comparator<File>() {
        public int compare(File o1, File o2) {
            return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
    };

    private FileUtil() {
    }

    /**
     * If f is a file and exists, it will be renamed to <tt>f.getName() + suffix</tt>.
     * @param f to be backed up
     * @param suffix appended to f's name to create name of backup file
     * @return true iff f does not exist or it could be renamed to the backup file, i.e., when
     * true is returned, the file f does not exist.
     */
    public static boolean backupFile(File f, String suffix) {
        if (!f.exists()) {
            return true;
        }
        if (!f.isFile()) {
            return false;
        }
        final File bak = new File(f.getParentFile(), f.getName() + suffix);
        if (bak.exists() && !bak.delete()) {
            return false;
        }
        return f.renameTo(bak);
    }

    public static File createTempDir() {
        for (int i = 1; true; i++) {
            File dir = new File(System.getProperty("java.io.tmpdir"), "tmp-" + i);
            if (!dir.exists() && dir.mkdir()) {
                return dir;
            }
        }
    }

    public static void deleteIfExists(final File f) {
        if (f.isFile() && !f.delete()) {
            logger.warn("<deleteIfExists> failed for " + f.getAbsolutePath());
        }
    }

    public static File ensureDir(File dir) {
        if (!isDirWriteable(dir)) {
            throw new IllegalStateException("invalid dir '" + dir.getAbsolutePath() + "'");
        }
        return dir;
    }

    /**
     * returns true iff the File supplied as a parameter is a writeable directory;
     * if the file does not exist, we try to create it first.
     */
    public static boolean isDirWriteable(File directory) {
        if (!directory.exists()) {
            // if we can create the dir, it is also writeable
            // if two threads enter this branch in parallel, only one will succeed in creating the dir,
            // so add a canWrite check to be on the safe side.
            return directory.mkdirs() || directory.canWrite();
        }
        else {
            return (directory.isDirectory() && directory.canWrite());
        }
    }

    /**
     * Returns a list of all files in dir and dir's subdirectories that ff accepts
     * @param dir start dir for recursive search
     * @param ff filter for files
     * @return list of accepted files, will never be null
     */
    public static List<File> listAllFiles(File dir, final FilenameFilter ff) {
        return listAllFiles(dir, new FileFilter() {
            public boolean accept(File pathname) {
                return ff.accept(pathname.getParentFile(), pathname.getName());
            }
        });
    }

    /**
     * Returns a list of all files in dir and dir's subdirectories that ff accepts
     * @param dir start dir for recursive search
     * @param ff filter for files
     * @return list of accepted files, will never be null
     */
    public static List<File> listAllFiles(File dir, final FileFilter ff) {
        final List<File> subDirs = new ArrayList<>();
        final List<File> result = new ArrayList<>();
        final File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    subDirs.add(pathname);
                }
                return ff == null || ff.accept(pathname);
            }
        });
        if (files != null) {
            result.addAll(Arrays.asList(files));
        }
        for (File subDir : subDirs) {
            result.addAll(listAllFiles(subDir, ff));
        }
        return result;
    }

    public static void unzipToDir(File zipFile, File workDir, long maxBps) throws IOException {
        unzipToDirImpl(workDir, new ThrottledInputStream(new FileInputStream(zipFile), maxBps));
    }

    /**
     * unzip given zip file to work directory.
     */
    public static void unzipToDir(File zipFile, File workDir) throws IOException {
        unzipToDirImpl(workDir, new FileInputStream(zipFile));
    }

    public static void unzipToDirImpl(File workDir, InputStream in) throws IOException {
        final ZipInputStream zin = new ZipInputStream(in);

        ZipEntry entry;
        final byte[] buf = new byte[8192];
        while ((entry = zin.getNextEntry()) != null) {
            final String name = entry.getName();

            final File file = new File(workDir, name);
            if (entry.isDirectory()) {
                file.mkdirs();
                continue;
            }

            final File tempFile = new File(file.getParentFile(), "." + file.getName() + ".tmp");
            final OutputStream out = new FileOutputStream(tempFile);

            int len;
            while ((len = zin.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            if (!tempFile.renameTo(file)) {
                throw new IOException("Could not rename file " + tempFile.getName() + " to "
                        + file.getAbsolutePath());
            }
        }

        zin.close();
    }

    /**
     * Creates a ZIP-compressed archive containing the files {@code filesToCompress} with name
     * {@code zipFile}. With respect to paths stored in the zip archive, the {@code baseDir} is
     * important. {@code baseDir} is the directory to which <em>relative paths</em> are stored in
     * the zip file.<br/>
     * E.g. if you have two files {@code /tmp/a} and {@code /tmp/dir/b} and {@code baseDir} is
     * {@code /tmp}, then two entries in the zip file will show up: {@code ./a} and {@code ./dir/b}.
     * If {@code baseDir == null}, only the <em>filenames</em> of given files are stored,
     * the whole directory structure is <em>flattened</em>! For the above two files this would
     * result in two zip entries {@code ./a} and {@code ./b}, 'removing' the directory {@code dir}.
     * @param filesToCompress the files to include in the zip archive
     * @param zipFile the name of the output zip archive
     * @param baseDir the directory to which relative paths are taken
     * @param maxBps maximum bytes per second to <em>read</em> from the files. This parameter may be
     * used to run this method as background process. {@code -1} disables throttling.
     * @throws IOException if any read or write operation fails.
     * @author Sebastian Wild
     */
    public static void zipFiles(final List<File> filesToCompress, final File zipFile,
            final File baseDir, long maxBps)
            throws IOException {
        final int bufferSize = 8192;
        if (filesToCompress == null || zipFile == null) {
            throw new IllegalArgumentException("arguments may not be null");
        }
        byte[] buffer = new byte[bufferSize];
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        for (final File file : filesToCompress) {
            final String name = baseDir == null ? file.getName() :
                    getRelativePath(baseDir, file);
            ZipEntry entry = new ZipEntry(name);
            zipOut.putNextEntry(entry);
            InputStream fileIn = maxBps == -1 ?
                    new BufferedInputStream(new FileInputStream(file), bufferSize) :
                    new ThrottledInputStream(new FileInputStream(file), maxBps);
            int read;
            while ((read = fileIn.read(buffer)) > -1) {
                zipOut.write(buffer, 0, read);
            }
            fileIn.close();
            zipOut.closeEntry();
        }
        zipOut.close();
    }

    /**
     * This method returns the relative pathname of {@code descendant} to {@code baseDir}.
     * This method requires {@code descendant} to be located inside {@code baseDir} directly
     * or in one of its subdirectories. This means, this method will <b>never</b> return a name
     * containing {@code ".."} to backtrack directories.<br/>
     * The returned String uses the path separator {@link java.io.File#separatorChar} of this VM.
     * @param baseDir the base directory
     * @param descendant the descendany of baseDir to compute the relative path of
     * @return relative path of descendant to baseDir
     * @throws IOException if {@link java.io.File#getCanonicalPath()} fails
     */
    private static String getRelativePath(File baseDir, File descendant) throws IOException {
        assert baseDir != null;
        assert descendant != null;
        final String base = baseDir.getCanonicalPath();
        final String desc = descendant.getCanonicalPath();
        if (desc.length() <= base.length() || !desc.startsWith(base)) {
            throw new IllegalArgumentException("Descendant \"" + desc + "\" " +
                    "is not located beneath baseDir \"" + base + "\".");
        }
        final String sub = desc.substring(base.length());
        return sub.charAt(0) == File.separatorChar ? sub.substring(1) : sub;
    }

    /**
     * Convenience overload, simply executes <br/>
     * {@code zipFiles(filesToCompress, zipFile, null, -1);} <br/>
     * see {@link #zipFiles(java.util.List, java.io.File, java.io.File, long)}
     * @param filesToCompress
     * @param zipFile
     * @throws IOException
     */
    public static void zipFiles(final List<File> filesToCompress, final File zipFile)
            throws IOException {
        zipFiles(filesToCompress, zipFile, null, -1);
    }

    public static File getUserResource(String projectName, String path) {
        final String userDir = System.getProperty("user.dir");
        if (userDir.endsWith(projectName)) {
            return new File(userDir + "/" + path);
        }
        else {
            return new File(userDir + "/" + projectName + "/" + path);
        }
    }

    public static void unGZip(File gzFile, File unGZipped, boolean deleteGZip) throws Exception {
        final GZIPInputStream is = new GZIPInputStream(new FileInputStream(gzFile));
        final OutputStream os = new FileOutputStream(unGZipped);
        final byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }

        os.close();
        is.close();
        if (deleteGZip) {
            gzFile.delete();
        }
    }
}
