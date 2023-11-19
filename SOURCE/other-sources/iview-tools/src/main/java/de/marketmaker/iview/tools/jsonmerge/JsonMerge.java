package de.marketmaker.iview.tools.jsonmerge;

import de.marketmaker.istar.common.util.I18nMap;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.LogChute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import java.util.Set;

/**
 * @author umaurer
 */
public class JsonMerge implements InitializingBean {
    public static void main(String[] args) {
        final JsonMerge jm = new JsonMerge();
        jm.setTemplateRoot(new File("/home/umaurer/local/entwicklung/iview/mmgwt/src/main/velocity"));
        jm.setFileJsonTemplate(new File("/home/umaurer/local/entwicklung/iview/mmgwt/src/main/velocity/bcc/guidefs-bcc.json"));
        jm.setLocale("de");
        jm.setFileI18nDefault(new File("/home/umaurer/local/entwicklung/iview/mmgwt/src/main/java/de/marketmaker/iview/mmgwt/mmweb/client/I18n.properties"));
        jm.setFileI18n(null);
        jm.setZone("bcc");
        jm.setFileResult(new File(System.getProperty("user.home") + "/tmp/guidefs.test.json"));
        try {
            jm.afterPropertiesSet();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private File templateRoot;
    private File fileJsonTemplate;
    private String locale;
    private File fileI18nDefault;
    private File fileI18n;
    private String zone;
    private File fileResult;
    private LogChute logChute;

    public void setTemplateRoot(File templateRoot) {
        this.templateRoot = templateRoot;
    }

    @Required
    public void setFileJsonTemplate(File fileJsonTemplate) {
        this.fileJsonTemplate = fileJsonTemplate;
    }

    @Required
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Required
    public void setFileI18nDefault(File fileI18nDefault) {
        this.fileI18nDefault = fileI18nDefault;
    }

    public void setFileI18n(File fileI18n) {
        this.fileI18n = fileI18n;
    }

    @Required
    public void setZone(String zone) {
        this.zone = zone;
    }

    @Required
    public void setFileResult(File fileResult) {
        this.fileResult = fileResult;
    }

    public void setLogChute(LogChute logChute) {
        this.logChute = logChute;
    }

    public void afterPropertiesSet() throws Exception {
        checkFile("fileJsonTemplate", this.fileJsonTemplate);
        checkString("locale", this.locale);
        checkFile("fileI18nDefault", this.fileI18nDefault);
        checkString("zone", this.zone);
        checkFile("fileResult", this.fileResult);

        if (this.templateRoot == null) {
            this.templateRoot = this.fileJsonTemplate.getParentFile();
        }

        final I18nMap i18nMap = readI18n();

        final VelocityEngine ve = initializeVelocity();
        final VelocityContext vc = new VelocityContext();
        vc.put("locale", this.locale);
        vc.put("i18n", i18nMap);
        vc.put("zone", this.zone);

        final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.fileResult), "UTF-8"));
        final Template template = ve.getTemplate(getRelativeTemplatePath());
        template.merge(vc, writer);
        writer.close();

        final Set<String> invalidKeys = i18nMap.getInvalidKeys();
        if (!invalidKeys.isEmpty()) {
            if (this.logChute != null) {
                this.logChute.log(LogChute.WARN_ID, "The following keys are not defined:");
                for (String key : invalidKeys) {
                    this.logChute.log(LogChute.WARN_ID, "    " + key);
                }
            }
            final int amount = invalidKeys.size();
            throw new RuntimeException("JsonMerge didn't find " + amount + (amount == 1 ? " key" : " keys"));
        }
    }

    private String getRelativeTemplatePath() {
        final String path = this.fileJsonTemplate.getAbsolutePath();
        final String root = this.templateRoot.getAbsolutePath();
        if (path.startsWith(root)) {
            return path.substring(root.length() + 1);
        }
        else {
            return this.fileJsonTemplate.getName();
        }
    }

    private void checkFile(String variableName, File file) {
        if (file == null) {
            throw new RuntimeException("property missing: " + variableName);
        }
    }

    private void checkString(String variableName, String value) {
        if (value == null) {
            throw new RuntimeException("property missing: " + variableName);
        }
    }


    private I18nMap readI18n() throws IOException {
        final I18nMap map = new I18nMap();
        addAll(map, this.fileI18nDefault, false);
        addAll(map, this.fileI18n, true);
        return map;
    }

    private void addAll(I18nMap map, File file, boolean nullAllowed) throws IOException {
        if (nullAllowed && (file == null || !file.isFile())) {
            return;
        }
        final Properties properties = new Properties();
        final Reader reader = new FileReader(file);
        properties.load(reader);
        reader.close();

        for (final String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
    }

    private VelocityEngine initializeVelocity() throws Exception {
        final VelocityEngine ve = new VelocityEngine();
        ve.setProperty("input.encoding", "UTF-8");
        ve.setProperty("output.encoding", "UTF-8");
        ve.setProperty("resource.loader", "file");
        ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        ve.setProperty("file.resource.loader.path", this.templateRoot.getAbsolutePath());
        ve.setProperty("file.resource.loader.cache", true);
        ve.setProperty("file.resource.loader.modificationCheckInterval", "0");
        ve.setProperty("runtime.references.strict", true);
        if (this.logChute != null) {
            ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this.logChute);
        }
        ve.init();
        return ve;
    }
}
