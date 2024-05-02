package com.hyland.labs.copo.parser;

import java.io.File;

/**
 * Hello world!
 */
public class COPOParserMain {

    public static void main(String[] args) throws Exception {
        
        if(args.length < 4) {
            String help = "co-po-parser requires at least 3 arguments:\n";
            help += "-met, Metadata Export Type. Must be json or xml\n";
            help += "-f, the CO_PO file to parse\n";
            help += "-d, the destination Directory Path\n";
            help += "-sp, schema prefix. Required and used only if -met is xml\n";
            help += "\nExamples\n";
            help += "json export of the metadata:\njava -jar co-po-parser.jar -f /path/to/co-po -d /path/to/export/directory -met json\n";
            help += "xml export of the metadata:\njava -jar co-po-parser.jar -f /path/to/co-po -d /path/to/export/directory -met xml -sp acme\n";
            
            System.out.println(help);
            return;
        }
        
        String copoFilePath = null;
        String destinationDirectoryPath = null;
        String exportType = null;
        String schemaPrefix = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch(arg) {
            case "-f":
                i += 1;
                copoFilePath = args[i];
                break;

            case "-d":
                i += 1;
                destinationDirectoryPath = args[i];
                break;

            case "-met":
                i += 1;
                exportType = args[i];
                break;

            case "-sp":
                i += 1;
                schemaPrefix = args[i];
                break;
            }
        }
        
        if(exportType == null || (!"json".equals(exportType) && !"xml".equals(exportType))) {
            System.out.println("Metadata Export Type is required and must be either json or xml (case sensitive)");
            return;
        }
        
        if("xml".equals(exportType) && (schemaPrefix == null || schemaPrefix.isEmpty())) {
            System.out.println("When Metadata Export Type is xml, schemaPrefix cannot be empty.");
            return;
        }
        
        String msg = "==============================\nCOPOParser, arguments received:\n";
        msg += "copoFilePath: " + copoFilePath + "\n";
        msg += "Dest. Directory Path: " + destinationDirectoryPath + "\n";
        msg += "Metadata Export Type: " + exportType + "\n";
        msg += "Schema Prexif (if export type is xml): " + schemaPrefix + "\n";
        msg += "==============================";
        System.out.println(msg);

        File copo = new File(copoFilePath);
        COPOParser.process(copo, destinationDirectoryPath, exportType, schemaPrefix);
        

        System.out.println("Done");
    }
}
