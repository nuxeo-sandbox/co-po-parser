# co-po-parser

> [!NOTE]
> All this work is started for a specific Alfresco prospect. Using GitHub as a backup (and no need for private repo for now, nothing specific to the prospect)


## Description

Well. Parsing a text file :-)

## Usage as a Command Line

* Have Java 17 installed
* Get the final jar (co-po-parser.jar) from the Release folder
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
java -jar co-po-parser.jar -f /home/ubuntu/copotest/CO_PO -d home/ubuntu/copotest/invoices
```

Run it. The command outputs the received arguments, then processes the file, create the invoices and ends with outputting "Done".

The files are in `home/ubuntu/copotest/invoices`, there are 2 files per invoice, made unique by their invoice number:

* One is the metadata file, a JSON, named `CO_PO_{INVOICE_NUMBER}-Metadata.json`. It contains the following properties, to be saved as field in a document in the repository:


```
{
  "voucher": "E57924",
  "invoiceNumber": "1PV47LV67946",
  "company": "3800",
  "invoiceDateStr": "20240314",
  "invoiceAmount": 188.94,
  "poNumber": "380790198"
}
```

* One is the PDF, `CO_PO_{INVOICE_NUMBER}.pdf`


So, the result is:

```
CO_PO_1LMNFRYL7TXQ-Metadata.jsonCO_PO_1LMNFRYL7TXQ.pdfCO_PO_1PV47LV67946-Metadata.jsonCO_PO_1PV47LV67946.pdfCO_PO_1WMLGLJT7J6F-Metadata.jsonCO_PO_1WMLGLJT7J6F.pdfCO_PO_PR0100373326-Metadata.jsonCO_PO_PR0100373326.pdf
. . .
```


## WARNING

See the pom.xml file for dependencies. We use:

* PDFBox 3
* Apache Commons IO 2

See the code iat `Invoice#buildPdf`. If you depend on version 2 od PDFBox, change the code as described.