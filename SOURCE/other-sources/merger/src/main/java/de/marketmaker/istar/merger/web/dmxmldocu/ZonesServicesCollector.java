/*
 * ZonesServicesCollector.java
 *
 * Created on 15.03.2012 11:30:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.dmxmldocu;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.FieldSample;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.MmValidator;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletCommand;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletCommandField;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.rawdoclet.RawDocletService;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.BlockParameterGuiType;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.DmxmlBlockDocumentation;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.DmxmlBlockParameterDocumentation;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.DmxmlDocumentationRepository;
import de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.ObjectFactory;
import de.marketmaker.istar.merger.web.easytrade.block.AtomController;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Reads the given Spring context files and resolves for each zone the block names to AtomController
 * classes.
 * <p/>
 * This class is not thread-safe.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class ZonesServicesCollector implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File zonesPath = new File("WEB-INF/zones");

    private File outputPath;

    private String blocksZoneProperty = "sample-form.html.default.blocks";

    private String[] zonesWithDocuForced = new String[0];

    private Marshaller marshaller;

    private Unmarshaller unmarshaller;

    private ServiceControllerProvider serviceControllerProvider;

    private DmxmlJavadocRepository dmxmlJavadocRepository;

    private final Map<String, DmxmlBlockDocumentation> blocks = new TreeMap<>();

    private String extendsZoneProperty = "extends";

    private String xsdNameZoneProperty = "context.xsdName";

    private Set<String> zonesWithDocuForcedSet;

    private BufferedWriter reportWriter;

    private BufferedWriter problemsWriter;

    private String reportIndent = "";

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.outputPath == null) {
            throw new RuntimeException("parameter not specified: outputPath");
        }
        this.outputPath.mkdirs();
        this.zonesWithDocuForcedSet = new HashSet<>(Arrays.asList(this.zonesWithDocuForced));
        createZoneDocu();
    }

    public void createZoneDocu() {
        this.reportWriter = null;
        this.problemsWriter = null;
        try {
            try {
                this.problemsWriter = new BufferedWriter(new FileWriter(new File(this.outputPath, "docuProblems.log")));
                this.reportWriter = new BufferedWriter(new FileWriter(new File(this.outputPath, "docuReport.log")));
            } catch (Exception e) {
                this.logger.warn("<createZoneDocu> Could not write missingDocu file.", e);
            }
            report("Starting with block documentation");
            moreReportIndent();
            for (File zone : allZonePropertiesFile()) {
                try {
                    final Properties zoneProps = loadZoneProps(zone);
                    final String zoneName = FilenameUtils.removeExtension(zone.getName());
                    if (zoneDeservesDocu(zoneProps, zoneName)) {
                        report("Creating documentation for zone " + zoneName);
                        moreReportIndent();
                        this.logger.info("<createZoneDocu> Creating docu for zone " + zoneName);
                        final String[] allowedBlocks = getAllowedBlocksForZone(zone, zoneProps);
                        report("allowed blocks for zone : " + Arrays.toString(allowedBlocks));
                        writeServiceDocu(zoneName, allowedBlocks);
                        lessReportIndent();
                    }
                    else {
                        report("Skipping docu for zone " + zoneName);
                    }
                } catch (Exception e) {
                    this.logger.warn("<createZoneDocu> Failed to load properties from " + zone, e);
                    reportProblem("Could not load zone properties for " + zone);
                }
            }
        } finally {
            lessReportIndent();
            report("Finished creating block documentation!");
            IOUtils.closeQuietly(this.reportWriter);
            IOUtils.closeQuietly(this.problemsWriter);
            this.reportWriter = null;
            this.problemsWriter = null;
        }
    }

    private Properties loadZoneProps(File zone) throws IOException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(zone));
            Properties zoneProps = new Properties();
            zoneProps.load(in);
            return zoneProps;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private boolean zoneDeservesDocu(Properties zone, String zoneName) {
        // A does not need docu if it does not declare and XSD file of its own
        final String xsdFileName = zone.getProperty(xsdNameZoneProperty);
        final String xsdName = StringUtils.removeEnd(xsdFileName, "-xml.xsd");
        return zoneName.equals(xsdName) || this.zonesWithDocuForcedSet.contains(zoneName);
    }

    private File[] allZonePropertiesFile() {
        return zonesPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".prop");
            }
        });
    }

    private String[] getAllowedBlocksForZone(File zone, Properties zoneProps) throws IOException {
        final String allowedBlockList = zoneProps.getProperty(blocksZoneProperty);
        if (allowedBlockList != null) {
            return allowedBlockList.split(",");
        }
        else {
            // check if zone extends another
            final String baseZoneName = zoneProps.getProperty(extendsZoneProperty);
            if (baseZoneName != null) {
                final File baseZone = new File(zone.getParent(), baseZoneName + ".prop");
                return getAllowedBlocksForZone(baseZone, loadZoneProps(baseZone));
            }
            else {
                return new String[0];
            }
        }
    }

    private void writeServiceDocu(String zoneName, String[] allowedBlocks) {
        try {
            DmxmlDocumentationRepository repository = new DmxmlDocumentationRepository();
            repository.setDmxmlBlocks(new DmxmlDocumentationRepository.DmxmlBlocks());
            for (String block : allowedBlocks) {
                report("Creating docu for block " + block);
                moreReportIndent();
                final DmxmlBlockDocumentation blockDocu = getBlockDocu(block);
                if (blockDocu != null) {
                    repository.getDmxmlBlocks().getBlock().add(blockDocu);
                }
                lessReportIndent();
            }
            //noinspection SynchronizeOnNonFinalField
            synchronized (this.marshaller) {
                marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
                marshaller.marshal(new ObjectFactory().createDmxmlDocumentationRepository(repository),
                        new StreamResult(new File(outputPath, zoneName + "-blocks.xml")));
            }
        } catch (Exception e) {
            this.logger.warn("<writeServiceDocu> Writing docu for zone " + zoneName + " failed.", e);
        }
    }


    private DmxmlBlockDocumentation getBlockDocu(String block) throws ClassNotFoundException {
        if (!blocks.containsKey(block)) {
            final AtomController atomController = serviceControllerProvider.getController(block);
            report("Controller for block: " + atomController);
            if (atomController == null) {
                this.logger.warn("<getBlockDocu> Could not find controller for block " + block);
                reportProblem("No atom controller for block " + block);
                return null;
            }
            DmxmlBlockDocumentation blockDocu = new DmxmlBlockDocumentation();
            blockDocu.setParameters(new DmxmlBlockDocumentation.Parameters());
            final String controllerClass = atomController.getClass().getCanonicalName();
            blockDocu.setBlockName(block);
            blockDocu.setControllerClassName(controllerClass);
            final RawDocletService service = dmxmlJavadocRepository.getServiceByName(controllerClass);
            if (service != null) {
                blockDocu.setDescription(service.getDescription());
                blockDocu.setFirstSentence(service.getFirstSentence());
            }
            else {
                reportProblem("No javadoc.xml entry for controller class " + controllerClass);
            }
            addParameterDocu(block, atomController, blockDocu, service);
            blocks.put(block, blockDocu);
        }
        else {
            report("Using cached block docu");
        }
        return blocks.get(block);
    }

    private void addParameterDocu(String block, AtomController controller,
            DmxmlBlockDocumentation blockDocu, RawDocletService service) {
        if (controller instanceof EasytradeCommandController) {
            Class commandClass = ((EasytradeCommandController) controller).getCommandClass();
            final String rawCommandClass = commandClass.getCanonicalName();
            report("Command class: " + rawCommandClass);
            blockDocu.setCommandClassName(rawCommandClass);
            List<RawDocletCommand> commandAndSuperclassDocu = new ArrayList<>(4);
            addCommandAndSuperclassesDocu(commandClass, commandAndSuperclassDocu);
            report("Creating parameter docu");
            moreReportIndent();
            Set<String> addedParameters = new HashSet<>();
            for (RawDocletCommand command : commandAndSuperclassDocu) {
                for (RawDocletCommandField field : command.getFields().getField()) {
                    if (isInternal((EasytradeCommandController) controller, field)) {
                        report("Skipping internal parameter " + field.getName());
                        continue;
                    }
                    if (addedParameters.contains(field.getName())) {
                        // parameter has been added in subclass, so we can skip it here
                        // This happens if getter methods are overridden
                        continue;
                    }
                    final Class<?> type = typeForFieldType(field.getType());
                    report("Parameter " + field.getName() + " (" + type + ")");
                    DmxmlBlockParameterDocumentation param = new DmxmlBlockParameterDocumentation();
                    param.setValidators(new DmxmlBlockParameterDocumentation.Validators());
                    blockDocu.getParameters().getParameter().add(param);

                    param.setName(field.getName());
                    param.setDescription(field.getDescription());
                    param.setRequired(field.isRequired());
                    setParameterSample(service, field, param);

                    copyValidators(param, field);
                    param.setMultiValued(field.getDimension() > 0);

                    if (type == null) {
                        reportProblem("Could not find type of field " + field.getName() + " in command class " + rawCommandClass);
                        continue;
                    }
                    param.setGuiType(typeForClass(type));
                    param.setEnumValues(enumValuesForClass(type));
                    param.setCommandFieldJavaType(type.getCanonicalName());

                    addedParameters.add(field.getName());
                }
            }
            lessReportIndent();
        }
    }

    private void setParameterSample(RawDocletService service, RawDocletCommandField field,
            DmxmlBlockParameterDocumentation param) {
        if (service == null) {
            this.logger.error("<setParameterSample> service is null, no sample values for field '" + field.getName() + "'");
        } else {
            param.setSampleValue(field.getSampleValue());
            for (FieldSample fieldSample : service.getCommandFieldSamples()) {
                if (fieldSample.getName().equals(field.getName())) {
                    param.setSampleValue(fieldSample.getSample());
                }
            }
        }
    }

    private boolean isInternal(EasytradeCommandController controller, RawDocletCommandField field) {
        try {
            BeanWrapper beanWrapper = new BeanWrapperImpl(controller.getCommandClass().newInstance());
            final Method readMethod = beanWrapper.getPropertyDescriptor(field.getName()).getReadMethod();
            if (readMethod != null) {
                final MmInternal mmInternal = readMethod.getAnnotation(MmInternal.class);
                return mmInternal != null;
            }
            else {
                reportProblem("Failed to get getter method object for property " + field.getName() + " in command class " + controller.getCommandClass().getCanonicalName() + ". ");
                return false;
            }
        } catch (Exception e) {
            // finding getter method might fail
            // let's be conservative and include the property as parameter
            reportProblem("Failed to find getter method for property " + field.getName() + " in command class " + controller.getCommandClass().getCanonicalName() + ". " + e);
            this.logger.warn("<isInternal> Getter method extraction failed ", e);
            return false;
        }
    }

    private RawDocletCommand getCommandClassDocu(String rawCommandClass) {
        return this.dmxmlJavadocRepository.getCommandByName(rawCommandClass.replace('$', '.'));
    }

    private void addCommandAndSuperclassesDocu(Class<?> commandClass,
            List<RawDocletCommand> docuList) {
        final String className = commandClass.getCanonicalName();
        final RawDocletCommand docu = getCommandClassDocu(className);
        report("Docu for " + className + ": " + docu);
        if (docu == null) {
            reportProblem("No javadoc.xml entry for command class " + className);
        }
        else {
            docuList.add(docu);
        }
        final Class<?> superclass = commandClass.getSuperclass();
        if (superclass != Object.class) {
            addCommandAndSuperclassesDocu(superclass, docuList);
        }
    }

    private void copyValidators(DmxmlBlockParameterDocumentation param,
            RawDocletCommandField field) {
        for (MmValidator validator : field.getValidators().getValidator()) {
            final de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.MmValidator copy = new de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.MmValidator();
            param.getValidators().getValidator().add(copy);
            copy.setValidatorType(validator.getValidatorType());
            final de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.MmValidator.Parameters params = new de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.MmValidator.Parameters();
            copy.setParameters(params);
            for (MmValidator.Parameters.Parameter parameter : validator.getParameters().getParameter()) {
                final de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.MmValidator.Parameters.Parameter copyParam = new de.marketmaker.istar.merger.web.dmxmldocu.jaxb.repo.MmValidator.Parameters.Parameter();
                params.getParameter().add(copyParam);
                copyParam.setName(parameter.getName());
                copyParam.setValue(parameter.getValue());
            }
        }
    }

    private Class<?> typeForFieldType(final String typeName) {
        try {
            return ClassUtils.primitiveToWrapper(ClassUtils.getClass(typeName));
        } catch (ClassNotFoundException e) {
            // in MmField, classes appear with their javadoc names.
            // Hence inner classes end up with a dot instead of a $ as separator
            final int i = typeName.lastIndexOf('.');
            if (i == -1) return Void.TYPE;
            StringBuilder s = new StringBuilder(typeName);
            s.setCharAt(i, '$');
            return typeForFieldType(s.toString());
        }
    }

    public static Map<Class<?>, BlockParameterGuiType> TYPES = new IdentityHashMap<>(17);

    static {
        TYPES.put(String.class, BlockParameterGuiType.STRING);
        TYPES.put(Integer.class, BlockParameterGuiType.INTEGER);
        TYPES.put(Long.class, BlockParameterGuiType.LONG);
        TYPES.put(Double.class, BlockParameterGuiType.DOUBLE);
        TYPES.put(Boolean.class, BlockParameterGuiType.BOOLEAN);
        TYPES.put(BigDecimal.class, BlockParameterGuiType.BIG_DECIMAL);
        TYPES.put(DateTime.class, BlockParameterGuiType.DATE_TIME);
        TYPES.put(Period.class, BlockParameterGuiType.TIME_PERIOD);
        TYPES.put(LocalDate.class, BlockParameterGuiType.LOCAL_DATE);
    }

    public static BlockParameterGuiType typeForClass(Class<?> clazz) {
        if (TYPES.containsKey(clazz)) {
            return TYPES.get(clazz);
        }
        else if (clazz.isEnum()) {
            return BlockParameterGuiType.ENUM;
        }
        else {
            return BlockParameterGuiType.OTHER;
        }
    }

    public DmxmlBlockParameterDocumentation.EnumValues enumValuesForClass(Class<?> clazz) {
        DmxmlBlockParameterDocumentation.EnumValues res = new DmxmlBlockParameterDocumentation.EnumValues();
        if (clazz.isEnum()) {
            final Object[] enumConstants = clazz.getEnumConstants();
            for (Object constant : enumConstants) {
                res.getEnumValue().add(constant.toString());
            }
        }
        return res;
    }

    private void moreReportIndent() {
        this.reportIndent += "  ";
    }

    private void lessReportIndent() {
        if (this.reportIndent.length() >= 2) {
            this.reportIndent = this.reportIndent.substring(2);
        }
    }

    private void report(String message) {
        writeIfNotNull(this.reportWriter, this.reportIndent + message);
    }

    private void reportProblem(String message) {
        writeIfNotNull(this.problemsWriter, message);
    }

    private void writeIfNotNull(BufferedWriter writer, String message) {
        if (writer != null) {
            try {
                writer.write(message);
                writer.newLine();
            } catch (IOException e) {
                this.logger.warn("<writeIfNotNull> failed", e);
            }
        }
    }


    public String getBlocksZoneProperty() {
        return blocksZoneProperty;
    }

    public void setBlocksZoneProperty(String blocksZoneProperty) {
        this.blocksZoneProperty = blocksZoneProperty;
    }

    public File getZonesPath() {
        return zonesPath;
    }

    public void setZonesPath(File zonesPath) {
        this.zonesPath = zonesPath;
    }

    public ServiceControllerProvider getServiceControllerProvider() {
        return serviceControllerProvider;
    }

    public void setServiceControllerProvider(ServiceControllerProvider serviceControllerProvider) {
        this.serviceControllerProvider = serviceControllerProvider;
    }

    public DmxmlJavadocRepository getDmDocResource() {
        return dmxmlJavadocRepository;
    }

    public void setDmDocResource(DmxmlJavadocRepository dmxmlJavadocRepository) {
        this.dmxmlJavadocRepository = dmxmlJavadocRepository;
    }

    public File getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(File outputPath) {
        this.outputPath = outputPath;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public Unmarshaller getUnmarshaller() {
        return unmarshaller;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public String[] getZonesWithDocuForced() {
        return zonesWithDocuForced;
    }

    public void setZonesWithDocuForced(String[] zonesWithDocuForced) {
        this.zonesWithDocuForced = zonesWithDocuForced;
    }
}
