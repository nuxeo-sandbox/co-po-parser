/*
 * (C) Copyright 2024 Hyland (http://hyland.com/)  and others.
 *
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
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package com.hyland.labs.copo.parser;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONObject;

import org.apache.commons.io.FileUtils;

/**
 * Store the invoice info, to be saved the repository in the corresponding fields.
 * 
 * Also builds the PDF and the JSON.
 */
public class Invoice {

    String voucher;

    String company;

    String invoiceNumber;

    String invoiceDateStr;

    double invoiceAmount;

    String poNumber;

    File pdf;

    public Invoice(String voucher, String company, String invoiceNumber, String invoiceDateStr, String invoiceAmountStr,
            String poNumber) {

        this.voucher = voucher;
        this.company = company;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDateStr = invoiceDateStr;
        this.invoiceAmount = Double.valueOf(invoiceAmountStr);
        this.poNumber = poNumber;
    }
    
    /**
     * 
     * @return the JSON of the invoice
     * @since TODO
     */
    public JSONObject getJson() {
        JSONObject obj = new JSONObject();
        
        obj.put("voucher", voucher);
        obj.put("company", company);
        obj.put("invoiceNumber", invoiceNumber);
        obj.put("invoiceDateStr", invoiceDateStr);
        obj.put("invoiceAmount", invoiceAmount);
        obj.put("poNumber", poNumber);
        
        return obj;
    }

    /**
     * 
     * @return the File of the JSON of the invoice
     * @since TODO
     */
    public File getJsonAsFile(String destinationDirectoryPath) throws IOException {
        
        if(!fullpathExists(destinationDirectoryPath)) {
            throw new IllegalArgumentException("destinationDirectoryPath must be a valid, existing path");
        }
        
        JSONObject obj = getJson();
        String objStr = obj.toString();
        
        if (!destinationDirectoryPath.endsWith("/")) {
            destinationDirectoryPath += "/";
        }
        String finalFullPath = destinationDirectoryPath + "CO_PO_" + invoiceNumber + "-Metadata.json";
        File f = new File(finalFullPath);
        FileUtils.writeStringToFile(f, objStr, "UTF-8");
        return f;
    }

    
    /**
     * Warning: before calling Invoice#getPdf, you must have called Invoice#buildPdf
     * 
     * @return the PDF of this invoice
     * @since TODO
     */
    public File getPdf() {
        if (pdf == null) {
            throw new RuntimeException("pdf is null, Invoice#buildPdf should have been called prior ro getPdf().");
        }
        return pdf;
    }

    public File buildPdf(String textForPdf, String destinationDirectoryPath, int removeFirstNChars) throws IOException {
        
        if(!fullpathExists(destinationDirectoryPath)) {
            throw new IllegalArgumentException("destinationDirectoryPath must be a valid, existing path");
        }
                
        PDDocument document = new PDDocument();
        // PDPage page = new PDPage(PDRectangle.A4);
        // Create a U.S. Letter size rectangle (8.5 x 11 inches)
        PDRectangle usLetter = new PDRectangle(612, 792); // 72 points per inch
        PDPage page = new PDPage(usLetter);
        document.addPage(page);

        String[] textArray = textForPdf.split("\n");

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();

        // WARNING: this is for PDFBox v2.n, it changed in PDFBox 3
        // contentStream.setFont( PDType1Font.COURIER, 7 );
        // PDF3 style:
        PDType1Font courier = new PDType1Font(Standard14Fonts.FontName.COURIER);
        contentStream.setFont(courier, 7);

        contentStream.setLeading(4f);
        //contentStream.newLineAtOffset(40, 750);
        contentStream.newLineAtOffset(20, 750);

        for (String line : textArray) {
            if(removeFirstNChars > 0) {
                contentStream.showText(line.substring(removeFirstNChars));
            } else {
                contentStream.showText(line);
            }
            contentStream.newLine();
            contentStream.newLine();
        }
        contentStream.endText();
        contentStream.close();

        if (!destinationDirectoryPath.endsWith("/")) {
            destinationDirectoryPath += "/";
        }
        String finalFullPath = destinationDirectoryPath + "CO_PO_" + invoiceNumber + ".pdf";
        document.save(finalFullPath);
        document.close();

        pdf = new File(finalFullPath);
        return pdf;
    }
    
    protected boolean fullpathExists(String path) {
        
        if(path == null) {
            return false;
        }
        
        File f = new File(path);
        return f.exists();
        
    }

}
