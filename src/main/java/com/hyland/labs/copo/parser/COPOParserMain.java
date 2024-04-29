package com.hyland.labs.copo.parser;

import java.io.File;

/**
 * Hello world!
 */
public class COPOParserMain {

    public static void main(String[] args) throws Exception {
        
        if(args.length < 4) {
            String help = "Need 2 arguments:\n";
            help += "-f copoFilePath\n";
            help += "-d destinationDirectoryPath";
            
            System.out.println(help);
            return;
        }
        
        String copoFilePath = null;
        String destinationDirectoryPath = null;
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
            }
        }
        String msg = "==============================\nCOPOParser, arguments received:\n";
        msg += "copoFilePath: " + copoFilePath + "\n";
        msg += "Dest. Directory Path: " + destinationDirectoryPath + "\n";
        msg += "==============================";
        System.out.println(msg);

        File copo = new File(copoFilePath);
        COPOParser.process(copo, destinationDirectoryPath);

        System.out.println("Done");
    }
}
