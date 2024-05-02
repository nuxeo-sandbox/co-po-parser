# co-po-parser

> [!NOTE]
> All this work is started for a specific Alfresco prospect. Using GitHub as a backup (and no need for private repo for now, nothing specific to the prospect)


## Description

Pars a text file, generate pdf + metadata that can then be bulk-imported in Alfresco.

## Usage as a Command Line

* Have Java 17 installed
* Get the final jar (co-po-parser.jar) from the Release folder
* ⚠️ Set the `ALF_PROP_FILE_SCHEMA_PREFIX` environment variable. it must contain the prefix used when exporting invoices as Alfresco Metadata file, for bulk import. For example, if the prefix is "acme", the XML will be:

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
   . . .
   <entry key="acme:voucher">V1234</entry>
   <entry key="acme:company">5678</entry>
   . . .
</properties>
```

* Run it as a command line. It requires 2 arguments:
  * -f /fullPath/to/the/CO_PO-file-to-ingest
  * -d /fullePath/to_existing/direcrory

The `-d` argument must be an _existing_ directory that will receive all the files

So, for example, say you have...

* The file to ingest at `/home/ubuntu/copotest/CO_PO`,  * and your created a directory at `home/ubuntu/copotest/invoices`,
* and the command is at `/home/ubuntu/copotest/co-po-parser.jar`

... the command is:

```
cd /home/ubuntu/copotest
export ALF_PROP_FILE_SCHEMA_PREFIX=acme
java -jar co-po-parser.jar -f /home/ubuntu/copotest/CO_PO -d home/ubuntu/copotest/invoices
```

Run it. The command outputs the received arguments, then processes the file, create the invoices and ends with outputting "Done".

In this example, the files are in `home/ubuntu/copotest/invoices`, there are 2 files per invoice, made unique by their invoice number:

* One is the pdf file of the invoice, named `{INVOICE_NUMBER}.pdf`. For example: AB123456.pdf, 7890123.pdf, etc.
* The other is the metadata file, as expected by [Alfresco Bulk Import](https://docs.alfresco.com/content-services/latest/admin/import-transfer/), an XML, named `{INVOICE_NUMBER}.pdf.metadata.properties.xml`. For example, assuming `ALF_PROP_FILE_SCHEMA_PREFIX` has been set to `acme`, and the invoice number is ABC123456, the file will be:


```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
  <entry key="type">cm:content</entry>
  <entry key="aspects">cm:versionable,test:Test</entry>
  <entry key="cm:title">ABC123456</entry>
  <entry key="cm:description">File #4 from bulk import of CO_PO-test-small.txt</entry>
  <entry key="hanes:voucher">V1234</entry>
  <entry key="hanes:company">0987</entry>
  <entry key="hanes:invoiceNumber">ABC123456</entry>
  <entry key="hanes:invoiceDate">20240414</entry>
  <entry key="hanes:invoiceAmount">128.07</entry>
  <entry key="hanes:poNumber">1234567890</entry>
</properties>
```


So, the result in the destination directory is:

```
AB123456.pdf
AB123456.pdf.metadata.properties.xml
CD789012.pdf
CD789012.pdf.metadata.properties.xml
EF345678.pdf
EF345678.pdf.metadata.properties.xml
. . . etc . . .
```


## WARNING

See the pom.xml file for dependencies. We use:

* PDFBox 3
* Apache Commons IO 2

See the code iat `Invoice#buildPdf`. If you depend on version 2 od PDFBox, change the code as described.