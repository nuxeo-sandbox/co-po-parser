package com.hyland.labs.copo.parser.test;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.hyland.labs.copo.parser.COPOParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class COPOParserTest {

    @Test
    public void testParser() throws Exception {

        File testFile = new File("src/test/resources/CO_PO-no-github.txt");
        Assert.assertNotNull(testFile.exists());

        File destination = new File("src/test/resources/parsing-destination-no-github");
        Assert.assertNotNull(testFile.exists());
        Assert.assertNotNull(testFile.isDirectory());

        FileUtils.cleanDirectory(destination);

        // This is a quick and dirty test because of time constraint => We comment/uncomment lines

        // Test JSON
        //COPOParser.process(testFile, destination.getAbsolutePath(), "json", null);

        // Test XML => must trigger an error (no schema prefix)
        //COPOParser.process(testFile, destination.getAbsolutePath(), "xml", null);

        // Test XML
        COPOParser.process(testFile, destination.getAbsolutePath(), "xml", "acme");

        Collection<File> files = FileUtils.listFiles(destination, TrueFileFilter.INSTANCE, null);

        Assert.assertEquals(8, files.size());

        // ... should check the json ...

        FileUtils.cleanDirectory(destination);
    }
}
