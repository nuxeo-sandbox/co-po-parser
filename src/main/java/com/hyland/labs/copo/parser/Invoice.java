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
    
    String schemaPrefix;

    public Invoice(String voucher, String company, String invoiceNumber, String invoiceDateStr, String invoiceAmountStr,
            String poNumber, String schemaPrefix) {

        this.voucher = voucher;
        this.company = company;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDateStr = invoiceDateStr;
        this.invoiceAmount = Double.valueOf(invoiceAmountStr);
        this.poNumber = poNumber;
        this.schemaPrefix = schemaPrefix;
    }

    /**
     * IMPORTANT
     * We do not use XML Java class because we are short in time, so we generate the XML by just exporting text.
     * 
     * @return a XMl file property of the data
     * @since TODO
     */
    public File toXmlFilePropertyForAlfrescoBulkImport(String destinationDirectoryPath, long index, String copoFileName)
            throws IOException {

        if (schemaPrefix == null || schemaPrefix.isEmpty()) {
            throw new IllegalArgumentException("The schemaPrefix is not defined.");
        }

        if (!fullpathExists(destinationDirectoryPath)) {
            throw new IllegalArgumentException("destinationDirectoryPath must be a valid, existing path");
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xml += "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n";
        xml += "<properties>\n";
        xml += "  <entry key=\"type\">cm:content</entry>\n";
        xml += "  <entry key=\"aspects\">cm:versionable,test:Test</entry>\n";
        xml += "  <entry key=\"cm:title\">" + invoiceNumber + "</entry>\n";
        if (index > 0) {
            xml += "  <entry key=\"cm:description\">File #" + index + " from bulk import of " + copoFileName
                    + "</entry>\n";
        } else {
            xml += "  <entry key=\"cm:description\">From bulk import of " + copoFileName + "</entry>\n";
        }
        // Specific
        xml += "  <entry key=\"" + schemaPrefix + ":voucher\">" + voucher + "</entry>\n";
        xml += "  <entry key=\"" + schemaPrefix + ":company\">" + company + "</entry>\n";
        xml += "  <entry key=\"" + schemaPrefix + ":invoiceNumber\">" + invoiceNumber + "</entry>\n";
        xml += "  <entry key=\"" + schemaPrefix + ":invoiceDate\">" + invoiceDateStr + "</entry>\n";
        xml += "  <entry key=\"" + schemaPrefix + ":invoiceAmount\">" + invoiceAmount + "</entry>\n";
        xml += "  <entry key=\"" + schemaPrefix + ":poNumber\">" + poNumber + "</entry>\n";

        xml += "</properties>\n";

        if (!destinationDirectoryPath.endsWith("/")) {
            destinationDirectoryPath += "/";
        }
        String finalFullPath = destinationDirectoryPath + invoiceNumber + ".pdf.metadata.properties.xml";
        File f = new File(finalFullPath);
        FileUtils.writeStringToFile(f, xml, "UTF-8");
        return f;
    }

    /**
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
     * @return the File of the JSON of the invoice
     * @since TODO
     */
    public File toJsonFile(String destinationDirectoryPath) throws IOException {

        if (!fullpathExists(destinationDirectoryPath)) {
            throw new IllegalArgumentException("destinationDirectoryPath must be a valid, existing path");
        }

        JSONObject obj = getJson();
        String objStr = obj.toString();

        if (!destinationDirectoryPath.endsWith("/")) {
            destinationDirectoryPath += "/";
        }
        String finalFullPath = destinationDirectoryPath + invoiceNumber + "-Metadata.json";
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

        if (!fullpathExists(destinationDirectoryPath)) {
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
        // contentStream.newLineAtOffset(40, 750);
        contentStream.newLineAtOffset(20, 750);

        for (String line : textArray) {
            if (removeFirstNChars > 0) {
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
        String finalFullPath = destinationDirectoryPath + invoiceNumber + ".pdf";
        document.save(finalFullPath);
        document.close();

        pdf = new File(finalFullPath);
        return pdf;
    }

    protected boolean fullpathExists(String path) {

        if (path == null) {
            return false;
        }

        File f = new File(path);
        return f.exists();

    }
    /*
    protected void createInAlfresco() throws IOException, AuthenticationException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://someservername:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/12341234-1234-1234-1234-123412341234/children");
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials ("admin","adminpswd");
        httpPost.addHeader (new BasicScheme().authenticate(creds,httpPost, null));

        File payload = new File ("/path/to/my/file.pdf");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create(); // Entity builder

        builder.addPart("filedata", new FileBody(payload)); // this is where I was struggling
        builder.addTextBody ("name", "thenamegoeshere");
        builder.addTextBody ("foo", "foo");
        builder.addTextBody ("bar", "bar");
        builder.addTextBody ("description", "descriptiongoeshere");

        builder.addTextBody ("overwrite", "true");

        HttpEntity entity = builder.build();

        httpPost.setHeader("Accept","application/json");
        httpPost.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPost); // Post the request and get response

        System.out.println(response.toString()); // Response print to console

        httpClient.close();  // close the client
    }*/

}
