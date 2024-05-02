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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 * Hello world!
 */
public class COPOParser {
    // 1.PROGRAM ZA401 [spaces] Hanesbrands Inc. [spaces] DATE: 03/15/24
    // (This line must not be printed)
    public static final String BEGIN_RECORD_TOKEN = "1.PROGRAM ZA401";

    // 0. [spaces] GRAND TOTAL [spaces] 22.12
    public static final String END_RECORD_TOKEN = "GRAND TOTAL        ";

    public static final String VOUCHER_LINE_REGEX = "0.VOUCHER # *([A-Z0-9]*)";

    public static final Pattern VOUCHER_LINE_PATTERN = Pattern.compile(VOUCHER_LINE_REGEX);

    public static final String COMPANY_LINE_REGEX = "0. *COMPANY # *([A-Z0-9]*)";

    public static final Pattern COMPANY_LINE_PATTERN = Pattern.compile(COMPANY_LINE_REGEX);

    public static final String INVOICE_INFO_LINE1_REGEX = " . *INV-NUMBER  *INV-DATE *INV-AMT *PO-NUMBER *VNDR-NUMBER *HNDL-CODE";

    public static final Pattern INVOICE_INFO_LINE1_PATTERN = Pattern.compile(INVOICE_INFO_LINE1_REGEX);

    // See below, // Saddly, can't find a pattern that works for these 4 groups...
    public static final String INVOICE_INFO_LINE2_REGEX = "-. *([A-Z0-9]*) *(\\d{8}) *([0-9.]+) *([A-Z0-9]*) .*";

    public static final Pattern INVOICE_INFO_LINE2_PATTERN = Pattern.compile(INVOICE_INFO_LINE2_REGEX);

    File copoFile;

    String destinationDirectoryPath;
    
    String metadataExportType = null;
    
    String schemaPrefix = null;

    public COPOParser(File copoFile, String destinationDirectoryPath, String metadataExportType, String schemaPrefix) {
        this.copoFile = copoFile;
        this.destinationDirectoryPath = destinationDirectoryPath;
        if (!this.destinationDirectoryPath.endsWith("/")) {
            this.destinationDirectoryPath += "/";
        }
        this.metadataExportType = metadataExportType;
        if(metadataExportType == null || (!"json".equals(metadataExportType) && !"xml".equals(metadataExportType))) {
            throw new IllegalArgumentException("Metadata Export Type must be either json or xml (case sensitive).");
        }

        this.schemaPrefix = schemaPrefix;
        if("xml".equals(metadataExportType) && (schemaPrefix == null || schemaPrefix.isEmpty())) {
            throw new IllegalArgumentException("When Metadata Export Type is xml, schemaPrefix cannot be empty.");
        }
    }

    public static void process(File copoFile, String destinationDirectoryPath, String metadataExportType, String schemaPrefix) throws IOException {
        COPOParser parser = new COPOParser(copoFile, destinationDirectoryPath, metadataExportType, schemaPrefix);
        parser.process();
    }

    public void process() throws IOException {

        try (LineIterator it = FileUtils.lineIterator(copoFile, "UTF-8")) {

            String voucher;
            String company;
            String invoiceNumber;
            String invoiceDateStr;
            String invoiceAmountStr;
            String poNumber;
            String textForPdf = "";
            long invoiceCount = 0;
            while (it.hasNext()) {

                // ========================================
                // First line: should be record start
                // ========================================
                String line = it.nextLine();
                if (!isRecordStart(line)) {
                    throw new RuntimeException(
                            "Line should be a Record-Start, and starts with '" + BEGIN_RECORD_TOKEN + "'");
                }

                // Now, all the lines are to be exported to pdf
                textForPdf = "";

                // ========================================
                // Voucher
                // ========================================
                voucher = null;
                do {
                    line = it.nextLine();
                    textForPdf += line + "\n";

                    Matcher m = VOUCHER_LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        voucher = m.group(1);
                    }
                } while (voucher == null);
                // Error handling if voucher is null..., but nextLine() would have failed before that

                // ========================================
                // Company
                // ========================================
                company = null;
                do {
                    line = it.nextLine();
                    textForPdf += line + "\n";

                    Matcher m = COMPANY_LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        company = m.group(1);
                    }
                } while (company == null);
                // Error handling if company is null..., but nextLine() would have failed before that

                // ========================================
                // Invoice number, date, etc.
                // ========================================
                invoiceNumber = null;
                invoiceDateStr = null;
                invoiceAmountStr = null;
                poNumber = null;
                do {
                    line = it.nextLine();
                    textForPdf += line + "\n";

                    Matcher m = INVOICE_INFO_LINE1_PATTERN.matcher(line);
                    if (m.matches()) {
                        line = it.nextLine();
                        textForPdf += line + "\n";

                        // Saddly, can't find a pattern for these 4 groups...
                        /*
                        m = INVOICE_INFO_LINE2_PATTERN.matcher(line);
                        // Assume it matches
                        invoiceNumber = m.group(1);
                        invoiceDateStr = m.group(2);
                        invoiceAmountStr = m.group(3);
                        poNumber = m.group(4);
                        */
                        // => doing it the ugly way...
                        String[] values = line.split("\\s+");
                        // First will be "-."
                        invoiceNumber = values[1];
                        invoiceDateStr = values[2];
                        invoiceAmountStr = values[3];
                        poNumber = values[4];
                    }
                } while (invoiceNumber == null);

                // ========================================
                // Get the rest of the record
                // ========================================
                do {
                    line = it.nextLine();
                    textForPdf += line + "\n";
                } while (!isRecordEnd(line));

                // ========================================
                // Generate JSON and PDF
                // ========================================
                Invoice invoice = new Invoice(voucher, company, invoiceNumber, invoiceDateStr, invoiceAmountStr,
                        poNumber, schemaPrefix);
                invoiceCount += 1;
                // Generate PDF
                invoice.buildPdf(textForPdf, destinationDirectoryPath, 2);
                
                // Generate JSON or XML
                if(metadataExportType.equals("json")) {
                    invoice.toJsonFile(destinationDirectoryPath);
                } else {
                    invoice.toXmlFilePropertyForAlfrescoBulkImport(destinationDirectoryPath, invoiceCount, copoFile.getName());
                }
            } // while (it.hasNext())

        } catch (IOException e) {
            throw e;
        }

    }

    protected boolean isRecordStart(String line) {
        return line.indexOf(BEGIN_RECORD_TOKEN) == 0;
    }

    protected boolean isRecordEnd(String line) {
        return line.indexOf(END_RECORD_TOKEN) > -1;
    }
   
}
