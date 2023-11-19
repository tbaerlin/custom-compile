package de.marketmaker.iview.tools.addsvgstyle;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class AddSvgStyleTask extends MatchingTask {
    private File cssFile;
    private File srcFile;
    private File srcDir;
    private File destDir;


    public void setCssFile(File cssFile) {
        this.cssFile = cssFile;
    }

    public void setSrcFile(File srcFile) {
        this.srcFile = srcFile;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }


    public static void main(String[] args) {
        final AddSvgStyleTask ass = new AddSvgStyleTask();
        ass.setProject(new Project());
        ass.setLocation(Location.UNKNOWN_LOCATION);
        ass.setIncludes("**/*.svg");
        ass.setCssFile(new File("/home/umaurer/produktion/prog/htdocs-tabor/vwd/svg/vwd.css"));
        ass.setSrcFile(new File("/export/home/umaurer/entwicklung/iview/mmgwt/src/main/artwork/svg/images/cg/pm/AdvisorySolutionIcons_VorherigesUNaechstes.svg"));
        ass.setSrcDir(new File("/export/home/umaurer/entwicklung/iview/mmgwt/src/main/artwork/svg"));
        ass.setDestDir(new File("/export/home/umaurer/tmp/pdf-svg"));
        ass.execute();
    }

    @Override
    public void execute() throws BuildException {
        try {
            checkFileParameters();

            // copy css file
            final File cssFileDest = new File(this.destDir, this.cssFile.getName());
            if (!(cssFileDest.isFile() && this.cssFile.lastModified() == cssFileDest.lastModified())) {
                //noinspection ResultOfMethodCallIgnored
                cssFileDest.getParentFile().mkdirs();
                copy(this.cssFile, cssFileDest);
                //noinspection ResultOfMethodCallIgnored
                cssFileDest.setLastModified(this.cssFile.lastModified());
                log("copy css file: " + this.cssFile.getName());
            }

            final String srcDirPath = this.srcDir.getAbsolutePath().replace('\\', '/') + "/";
            final File[] sourceFiles = getSourceFiles();
            int copyCounter = 0;
            int skipCounter = 0;
            for (File sourceFile : sourceFiles) {
                String path = sourceFile.getAbsolutePath().replace('\\', '/');
                if (path.startsWith(srcDirPath)) {
                    path = path.substring(srcDirPath.length());
                }

                final File fileDest = new File(this.destDir, path);
                if (fileDest.isFile() && sourceFile.lastModified() == fileDest.lastModified()) {
                    skipCounter++;
                    continue;
                }

                copyCounter++;
                addSvgStyle(sourceFile, fileDest, getCssReference(path));
                //noinspection ResultOfMethodCallIgnored
                fileDest.setLastModified(sourceFile.lastModified());
            }
            log("copied " + copyCounter + " svg files, skipped " + skipCounter + " svg files");
        }
        catch (IOException e) {
            throw new BuildException(e);
        }
    }


    private void checkFileParameters() {
        if (this.cssFile == null) {
            throw new BuildException("cssFile attribute is not set.");
        }
        if (!this.cssFile.isFile()) {
            throw new BuildException("cssFile ist not a valid file: " + this.cssFile.getAbsolutePath());
        }

        if (this.srcFile != null) {
            if (this.destDir == null) {
                throw new BuildException("destDir attribute is not set.");
            }
        }
        else {
            if (this.srcDir == null) {
                throw new BuildException("No input files. srcDir has to be set.");
            }
            if (this.destDir == null) {
                throw new BuildException("destDir attribute is not set.");
            }
        }
    }


    /**
     * Gets source files from the task parameters and child elements,
     * combines those to a one list, and returns the list.
     *
     * @return Array of source filename strings.
     */
    protected File[] getSourceFiles() {
        final List<File> inputFiles = new ArrayList<>(); // Input files in temp list.

        if (this.srcFile != null) {
            // Only one source and destination file have been set.
            inputFiles.add(this.srcFile);
        }
        else {
            // Unknown number of files have to be converted. destdir
            // attribute and either srcdir attribute or fileset element
            // have been set.

            // Read source files from the child patterns.
            // The value of srcdir attribute overrides the dir attribute in
            // fileset element.
            if (this.srcDir != null) {
                // fileset is declared in the super class.
                // Scan to get all the files in srcdir directory that
                // should be in input files.
                fileset.setDir(this.srcDir);
                final DirectoryScanner ds = fileset.getDirectoryScanner(getProject());
                final String[] includedFiles = ds.getIncludedFiles();
                // Add file and its path to the input file vector.
                for (String includedFile : includedFiles) {
                    inputFiles.add(new File(srcDir.getPath(), includedFile));
                }
            }
        }

        // Convert List to array and return the array.
        return inputFiles.toArray(new File[inputFiles.size()]);
    }


    private String getCssReference(String filename) {
        final char[] chars = filename.toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (c == '/') {
                sb.append("../");
            }
        }
        sb.append(this.cssFile.getName());
        return sb.toString();
    }


    private void addSvgStyle(File srcFile, File destFile, String cssReference) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        destFile.getParentFile().mkdirs();
        final InputStream is = new FileInputStream(srcFile);
        final OutputStream os = new FileOutputStream(destFile);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int buf;
            while (!isIn(buf = is.read(), '\n', -1)) {
                baos.write(buf);
            }
            boolean startWritten = false;
            final byte[] bytes = baos.toByteArray();
            if (isXmlStart(bytes)) {
                os.write(bytes);
                os.write('\n');
                startWritten = true;
            }
            os.write(("<?xml-stylesheet href=\"" + cssReference + "\" type=\"text/css\"?>\n").getBytes());
            if (!startWritten) {
                os.write(bytes);
                os.write('\n');
            }
            copy(is, os);
        }
        finally {
            closeQuietly(os);
            closeQuietly(is);
        }
    }

    private boolean isXmlStart(byte[] bytes) {
        byte[] test = new byte[]{'<', '?', 'x', 'm', 'l'};
        if (bytes.length < test.length) {
            return false;
        }
        for (int i = 0; i < test.length; i++) {
            if (bytes[i] != test[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean isIn(int c, int... list) {
        for (int i : list) {
            if (c == i) {
                return true;
            }
        }
        return false;
    }


    private void copy(File fileIn, File fileOut) throws IOException {
        final InputStream is = new FileInputStream(fileIn);
        final OutputStream os = new FileOutputStream(fileOut);
        try {
            copy(is, os);
        }
        finally {
            closeQuietly(os);
            closeQuietly(is);
        }
    }


    private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while ((len = inputStream.read(buf)) >= 0) {
            outputStream.write(buf, 0, len);
        }
    }


    private void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException e) {
            // ignore
        }
    }
}
