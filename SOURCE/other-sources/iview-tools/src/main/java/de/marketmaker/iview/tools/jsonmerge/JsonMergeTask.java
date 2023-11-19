package de.marketmaker.iview.tools.jsonmerge;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * @author umaurer
 */
public class JsonMergeTask extends MatchingTask {
    private File templatePath;
    private String[] locales;
    private File i18nPath;
    private String zone;
    private File destinationPath;

    public void setTemplatePath(File templatePath) {
        this.templatePath = templatePath;
    }

    public void setLocales(String locales) {
        this.locales = locales.split("\\s*,\\s*");
    }

    public void setI18nPath(File i18nPath) {
        this.i18nPath = i18nPath;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setDestinationPath(File destinationPath) {
        this.destinationPath = destinationPath;
    }


    @Override
    public void execute() throws BuildException {
        checkNull(this.templatePath, "templatePath");
        checkNull(this.locales, "locales");
        checkNull(this.i18nPath, "i18nPath");
        checkNull(this.zone, "zone");
        checkNull(this.destinationPath, "destinationPath");

        final List<File> listTemplates = new ArrayList<File>();
        addTemplates(this.templatePath, listTemplates);
        addTemplates(new File(this.templatePath, this.zone), listTemplates);

        for (File fileTemplate : listTemplates) {
            for (String locale : this.locales) {
                mergeTemplate(fileTemplate, locale);
            }
        }
    }

    private void mergeTemplate(File fileTemplate, String locale) {
        final File fileI18nDefault = new File(this.i18nPath, "I18n.properties");
        final File fileI18n = new File(this.i18nPath, "I18n_" + locale + ".properties");
        log("json-merge " + fileTemplate.getAbsolutePath()
                + "\n    for  " + this.zone
                + "\n    with " + fileI18nDefault.getAbsolutePath()
                + "\n    and  " + fileI18n.getAbsolutePath()
        );
        final JsonMerge jm = new JsonMerge();
        jm.setTemplateRoot(this.templatePath);
        jm.setFileJsonTemplate(fileTemplate);
        jm.setLocale(locale);
        jm.setFileI18nDefault(fileI18nDefault);
        jm.setFileI18n(fileI18n);
        jm.setZone(this.zone);
        jm.setLogChute(new AntLogChute(this));
        jm.setFileResult(new File(this.destinationPath, fileTemplate.getName().replaceAll("(\\.[^\\.]*)", "-" + locale + "$1")));
        try {
            jm.afterPropertiesSet();
        }
        catch (Exception e) {
            throw new BuildException("cannot merge " + fileTemplate.getAbsolutePath() + " with " + locale, e);
        }
    }

    private void checkNull(final Object param, final String paramName) {
        if (param == null) {
            throw new BuildException(paramName + " not specified");
        }
    }

    private void addTemplates(File templatePath, List<File> listTemplates) {
        if (!templatePath.isDirectory()) {
            log("path does not exist: " + templatePath.getAbsolutePath());
            return;
        }
        final File[] files = templatePath.listFiles(new FileFilter() {
            public boolean accept(File path) {
                return path.isFile();
            }
        });
        Collections.addAll(listTemplates, files);
    }

}
