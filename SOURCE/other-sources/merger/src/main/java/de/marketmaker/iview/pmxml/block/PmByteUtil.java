package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTColumnSpec;
import de.marketmaker.iview.pmxml.DTFooterCell;
import de.marketmaker.iview.pmxml.DTRowBlock;
import de.marketmaker.iview.pmxml.DTRowGroup;
import de.marketmaker.iview.pmxml.DTSingleRow;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.EvalLayoutChartResponse;
import de.marketmaker.iview.pmxml.EvalLayoutReportResponse;
import de.marketmaker.iview.pmxml.EvalLayoutTableResponse;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.MMIndexedString;
import de.marketmaker.iview.pmxml.MMNumber;
import de.marketmaker.iview.pmxml.MMString;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 20.08.13 13:21
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PmByteUtil {
    public static final short EXCEL_FORMAT_GENERAL = (short) 0x0; // general format
    public static final short EXCEL_FORMAT_NUMBER = (short) 0x4; //"#,##0.00" in Excel(EN)
    public static final short EXCEL_FORMAT_PERCENT = (short) 0xA; // "0.00%" in Excel(EN)
    public static final short EXCEL_FORMAT_DATE = (short) 0xE; // "m/d/yy" in Excel(EN)
    public static final short EXCEL_FORMAT_TIME = (short) 0x14;  // "h:mm" in Excel(EN)
    public static final short EXCEL_FORMAT_DATE_TIME = (short) 0x16; // "m/d/yy h:mm" in Excel(EN)
    public static final short EXCEL_FORMAT_TEXT = (short) 0x31; // text cell style "@"

    public static byte[] getPdf(JaxbHandler jaxbHandler, PmxmlExchangeDataResponse pmxmlResponse) throws InvalidResponseException {
        final PmExchangeData.ResponseWrapper responseWrapper = PmExchangeData.extractResponse(pmxmlResponse, EvalLayoutReportResponse.class);
        final EvalLayoutReportResponse reportResponse = jaxbHandler.unmarshal(
                responseWrapper.getRawXml(), EvalLayoutReportResponse.class
        );
        InvalidResponseException.check(reportResponse.getQueryResponseState());
        return reportResponse.getContent();
    }

    public static byte[] getChart(JaxbHandler jaxbHandler, PmxmlExchangeDataResponse pmxmlResponse) throws InvalidResponseException {
        final PmExchangeData.ResponseWrapper responseWrapper = PmExchangeData.extractResponse(pmxmlResponse, EvalLayoutChartResponse.class);
        final EvalLayoutChartResponse chartResponse = jaxbHandler.unmarshal(
                responseWrapper.getRawXml(), EvalLayoutChartResponse.class
        );
        InvalidResponseException.check(chartResponse.getQueryResponseState());
        if (chartResponse.getCharts().isEmpty()) {
            final String pixelB64 = "R0lGODlhAQABAPAAAAAAAAAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="; // empty pixel
            return Base64.decodeBase64(pixelB64.getBytes());
        }
        else {
            return chartResponse.getCharts().get(0).getContent();
        }
    }

    public static byte[] getExcelSheet(JaxbHandler jaxbHandler, PmxmlExchangeDataResponse pmxmlResponse) throws IOException, InvalidResponseException {
        final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateOptionalTimeParser();

        final PmExchangeData.ResponseWrapper responseWrapper = PmExchangeData.extractResponse(pmxmlResponse, EvalLayoutTableResponse.class);
        final EvalLayoutTableResponse table = jaxbHandler.unmarshal(
                responseWrapper.getRawXml(), EvalLayoutTableResponse.class
        );
        InvalidResponseException.check(table.getQueryResponseState());
        final HSSFWorkbook wb = new HSSFWorkbook();
        final HSSFSheet sheet = wb.createSheet();

        int rowNum = 0;
        int colNum = 0;

        //Title
        final HSSFRow titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue(table.getTable().getCaption());

        sheet.createRow(rowNum++); //Empty row

        //Header
        final HSSFRow headRow = sheet.createRow(rowNum++);
        final List<DTColumnSpec> specs = table.getTable().getColumnSpecs();
        final List<CellStyle> cellStyles = new ArrayList<>(specs.size());
        for (DTColumnSpec spec : specs) {
            headRow.createCell(colNum++).setCellValue(stripOffBlanksAndLineBreaks(spec.getCaption()));
            cellStyles.add(createCellStyle(wb, spec));
        }

        //Body
        final List<DTSingleRow> dtSingleRows = expandAllDTRowGroups(table.getTable().getToplevelGroup().getGroupedRows());
        for (DTSingleRow dtSingleRow : dtSingleRows) {
            colNum = 0;
            final HSSFRow row = sheet.createRow(rowNum++);
            final List<DTCell> dtCells = dtSingleRow.getCells();
            for (DTCell dtCell : dtCells) {
                int currentColumn = colNum;
                final CellStyle cellStyle = cellStyles.get(currentColumn);
                final HSSFCell cell = row.createCell(colNum++);
                setCellValueAndStyle(wb, dateTimeFormatter, cellStyle, dtCell, cell);
                if(currentColumn > 0) {
                    sheet.autoSizeColumn(currentColumn);
                }
            }
        }

        sheet.createRow(rowNum++); //Empty row

        //Footer
        final List<DTFooterCell> footerCells = table.getTable().getFooterCells();
        for (DTFooterCell footerCell : footerCells) {
            final HSSFRow footerRow = sheet.createRow(rowNum++);
            footerRow.createCell(0).setCellValue(footerCell.getName());
            footerRow.createCell(1).setCellValue(footerCell.getValue());
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }

    /**
     * For default Excel cell formats refer to {@see http://poi.apache.org/apidocs/org/apache/poi/ss/usermodel/BuiltinFormats.html}
     */
    private static CellStyle createCellStyle(HSSFWorkbook workbook, DTColumnSpec dtColumnSpec) {
        final ParsedTypeInfo pti = dtColumnSpec.getTypeInfo();

        final CellStyle cellStyle = workbook.createCellStyle();

        switch(pti.getTypeId()) {
            case TI_NUMBER:
                if(pti.isNumberProcent()) {
                    cellStyle.setDataFormat(EXCEL_FORMAT_PERCENT);
                }
                else {
                    cellStyle.setDataFormat(EXCEL_FORMAT_NUMBER);
                }
                break;
            case TI_DATE:
                switch (pti.getDateKind()) {
                    case DK_TIME:
                        cellStyle.setDataFormat(EXCEL_FORMAT_TIME);
                        break;
                    case DK_DATE:
                        cellStyle.setDataFormat(EXCEL_FORMAT_DATE);
                        break;
                    case DK_DATE_TIME:
                    default:
                        cellStyle.setDataFormat(EXCEL_FORMAT_DATE_TIME);
                }
                break;
            case TI_MEMO:
                cellStyle.setWrapText(true);
            case TI_STRING:
                cellStyle.setDataFormat(EXCEL_FORMAT_TEXT);
                break;
            default:
                cellStyle.setDataFormat(EXCEL_FORMAT_GENERAL);
        }
        return cellStyle;
    }

    private static void setCellValueAndStyle(Workbook workbook, DateTimeFormatter dateTimeFormatter, CellStyle cellStyle, DTCell dtCell, HSSFCell cell) {
        final MM item = dtCell.getItem();

        cell.setCellStyle(cellStyle);

        if(item instanceof ErrorMM) {
            cell.setCellValue(((ErrorMM) item).getErrorString());
            final CellStyle errorCellStyle = workbook.createCellStyle();
            errorCellStyle.setDataFormat(EXCEL_FORMAT_GENERAL);
            cell.setCellStyle(errorCellStyle);
        }
        else if(item instanceof MMIndexedString) {
            cell.setCellValue(((MMIndexedString) item).getCode());
        }
        else if(item instanceof MMString) {
            cell.setCellValue(((MMString) item).getValue());
        }
        else if(item instanceof MMNumber) {
            try {
                cell.setCellValue(Double.parseDouble(((MMNumber) item).getValue()));
            }
            catch(NumberFormatException | NullPointerException e) {
                // NPE may occur if PM sends a MMNumber with xsd:nil for the value, which indicates that the delphi
                // native double is either NaN or Infinity. However, according to the XSD specs, only digits and
                // xsd:nil are valid values for xsd:decimal. Unfortunately, they do not set the MMNumber itself to
                // null, but its value.
                cell.setCellValue(dtCell.getContent());
            }
        }
        else if(item instanceof MMDateTime) {
            try {
                final DateTime dateTime = dateTimeFormatter.parseDateTime(((MMDateTime) item).getValue());
                // update date time format for the whole column if it is not midnight
                if(dateTime.withTimeAtStartOfDay().isBefore(dateTime) && cell.getCellStyle().getDataFormat() == EXCEL_FORMAT_DATE) {
                    cell.getCellStyle().setDataFormat(EXCEL_FORMAT_DATE_TIME);
                }
                cell.setCellValue(dateTime.toDate());
            }
            catch(RuntimeException re) {
                cell.setCellValue(dtCell.getContent());
            }
        }
        else {
            cell.setCellValue(dtCell.getContent());
        }
    }

    private static String stripOffBlanksAndLineBreaks(String caption) {
        if(!StringUtils.hasText(caption)) {
            return null;
        }
        return caption.replaceAll("\\s+", " ").replaceAll("-\\s+", "").replaceAll("/\\s+", "/");
    }

    protected static List<DTSingleRow> expandAllDTRowGroups(List<DTRowBlock> in) {
        final List<DTSingleRow> out = new ArrayList<>();
        final ArrayList<DTRowBlock> toExpand = new ArrayList<>(in);

        while (!toExpand.isEmpty()) {
            final DTRowBlock block = toExpand.remove(0);
            if (block instanceof DTSingleRow) {
                out.add((DTSingleRow) block);
            }
            else if (block instanceof DTRowGroup) {
                for (DTRowBlock toAdd : ((DTRowGroup) block).getGroupedRows()) {
                    toExpand.add(toAdd);
                }
            }
        }
        return out;
    }
}