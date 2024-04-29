package com.hyland.labs.copo.parser;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONObject;

/**
 * Hello world!
 */
public class COPOParser {
    // 1.PROGRAM ZA401 [spaces] Hanesbrands Inc. [spaces] DATE: 03/15/24
    // (This line must not be printed)
    public static final String BEGIN_RECORD_TOKEN = "1.PROGRAM ZA401";
    // 0. [spaces] GRAND TOTAL [spaces] 22.12
    public static final String END_RECORD_TOKEN = "GRAND TOTAL        ";
    
    public static final String VOUCHER_LINE_REGEX = "^\\$0.VOUCHER # *([A-Z0-9]*)";
    
    public static final Pattern VOUCHER_LINE_PATTERN = Pattern.compile(VOUCHER_LINE_REGEX);
    
    public static final String COMPANY_LINE_REGEX = "^\\$0. *COMPANY # *([A-Z0-9]*)";
    
    public static final Pattern COMPANY_LINE_PATTERN = Pattern.compile(COMPANY_LINE_REGEX);
    
    public static final String INVOICE_INFO_LINE1_REGEX = "^\\ . *INV-NUMBER  *INV-DATE *INV-AMT *PO-NUMBER *VNDR-NUMBER *HNDL-CODE";
    
    public static final Pattern INVOICE_INFO_LINE1_PATTERN = Pattern.compile(INVOICE_INFO_LINE1_REGEX);
    
    public static final String INVOICE_INFO_LINE2_REGEX = "^\\ . *([A-Z0-9]*) *([0-9]*) *([0-9]*.[0-9]{2}) *([A-Z0-9]*) *([A-Z0-9]*)";
    
    public static final Pattern INVOICE_INFO_LINE2_PATTERN = Pattern.compile(INVOICE_INFO_LINE2_REGEX);

    
    File copoFile;
    
    String destinationDirectoryPath;

    public COPOParser(File copoFile, String destinationDirectoryPath) {
        this.copoFile = copoFile;
        this.destinationDirectoryPath = destinationDirectoryPath;
        if(!this.destinationDirectoryPath.endsWith("/")) {
            this.destinationDirectoryPath += "/";
        }
    }
    
    public static void process(File copoFile, String destinationDirectoryPath) throws IOException {
        COPOParser parser = new COPOParser(copoFile, destinationDirectoryPath);
        parser.process();
    }
    
    public void process() throws IOException {
        
        try(LineIterator it = FileUtils.lineIterator(copoFile, "UTF-8")) {
            
            String voucher;
            String company;
            while (it.hasNext()) {
                
               
                // ========================================
                // First line: should be record start
                // ========================================
                String line = it.nextLine();
                if(!isRecordStart(line)) {
                    throw new RuntimeException("Line should be a Record-Start, and starts with '" + BEGIN_RECORD_TOKEN + "'");
                }

                // ========================================
                // Voucher
                // ========================================
                voucher = null;
                do {
                    line = it.nextLine();
                    Matcher m = VOUCHER_LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        voucher = m.group(1);
                    }
                } while(voucher == null);

                // ========================================
                // Voucher
                // ========================================
                company = null;
                do {
                    line = it.nextLine();
                    Matcher m = COMPANY_LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        company = m.group(1);
                    }
                } while(voucher == null);
                
            }
            
        } catch (IOException e) {
            throw e;
        }
        
        
    }
    
    protected File toPDF(String text, String coreFileName) throws IOException {
                
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        String[] textArray = text.split("\n");
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();

        // WARNING: this is for PDFBox v2.n, it changed in PDFBox 3
        //       contentStream.setFont( PDType1Font.COURIER, 10 );
        // PDF3 style:
        PDType1Font courier = new PDType1Font(Standard14Fonts.FontName.COURIER);
        contentStream.setFont(courier, 10);

        contentStream.setLeading(4f);
        contentStream.newLineAtOffset(40, 750);

        for (String line : textArray) {
            contentStream.showText(line);
            contentStream.newLine();
            contentStream.newLine();
        }
        contentStream.endText();
        contentStream.close();

        String finalFullPath = destinationDirectoryPath + "CO_PO_" + coreFileName + ".pdf";
        document.save(finalFullPath);
        document.close();
        
        return new File(finalFullPath);
    }
    
    protected boolean isRecordStart(String line) {
        return line.indexOf(BEGIN_RECORD_TOKEN) == 0;
    }
    
    protected boolean isRecordEnd(String line) {
        return line.indexOf(END_RECORD_TOKEN) > -1;
    }
}
