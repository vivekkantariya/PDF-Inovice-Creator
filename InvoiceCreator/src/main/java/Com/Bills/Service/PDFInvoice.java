package Com.Bills.Service;

import Com.Bills.Model.AddressDetails;
import Com.Bills.Model.HeaderDetails;
import Com.Bills.Model.Product;
import Com.Bills.Model.ProductTableHeader;
import com.codingerror.model.MyFooter;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.DashedBorder;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class PDFInvoice {
    private Document document;
    private PdfDocument pdfDocument;
    private String pdfName;
    private PdfFont titleFont;
    private PdfFont headerFont;
    private PdfFont regularFont;

    private Color titleColor = Color.BLACK;
    private Color headerColor = new DeviceRgb(64, 64, 64); // Dark gray
    private Color backgroundColor = Color.WHITE;
    float threecol = 190f;
    float twocol = 285f;
    float twocol150 = twocol + 150f;
    float twocolumnWidth[] = { twocol150, twocol };
    float threeColumnWidth[] = { threecol, threecol, threecol };
    public float[] fullwidth = { threecol * 3 };
    private ProductTableHeader productTableHeader;

    public PDFInvoice(String pdfName, Document document) throws FileNotFoundException {
        this.pdfName = pdfName;
        this.pdfDocument = new PdfDocument(new PdfWriter(pdfName));
        this.pdfDocument.setDefaultPageSize(PageSize.A4);
        this.document = document;

        try {
            titleFont = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
            headerFont = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
            regularFont = PdfFontFactory.createFont(FontConstants.HELVETICA);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTableHeader(ProductTableHeader productTableHeader) {
        Paragraph productPara = new Paragraph("Products");
        document.add(productPara.setBold());

        float descriptionWidth = 300f;
        float quantityWidth = 100f;
        float priceWidth = 100f;
        float totalWidth = 100f;

        float[] columnWidths = { descriptionWidth, quantityWidth, priceWidth, totalWidth };

        Table threeColTable1 = new Table(columnWidths);
        threeColTable1.setBackgroundColor(headerColor, 0.7f).setMarginTop(10);

        threeColTable1.addCell(new Cell().add("Description").setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER));
        threeColTable1.addCell(new Cell().add("Quantity").setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        threeColTable1.addCell(new Cell().add("Price").setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setMarginRight(10f);;
        threeColTable1.addCell(new Cell().add("Total").setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));

        document.add(threeColTable1);
    }

    public void createDocument(String pdfName, List<Product> productList, AddressDetails addressDetails, HeaderDetails headerDetails, List<String> tncList, Boolean lastPage, String imagePath) throws FileNotFoundException {
        PdfWriter pdfWriter = new PdfWriter(pdfName);
        pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        this.document = new Document(pdfDocument);
        document.setMargins(20, 20, 50, 20);  // Adjust margins as needed
        createTableHeader(productTableHeader);
    }

    public void createTnc(List<String> TncList,Boolean lastPage,String imagePath) {
        if(lastPage) {
            float threecol = 190f;
            float fullwidth[] = {threecol * 3};
            Table tb = new Table(fullwidth);
            tb.addCell(new Cell().add("TERMS AND CONDITIONS\n").setBold().setBorder(Border.NO_BORDER));
            for (String tnc : TncList) {
                tb.addCell(new Cell().add(tnc).setBorder(Border.NO_BORDER));
            }
            document.add(tb);
        }else {
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new MyFooter(document,TncList,imagePath));
        }
        document.close();
    }

    public void createProduct(List<Product> productList) {
        float threecol = 190f;
        float fullwidth[] = { threecol * 3 };
        Table threeColTable2 = new Table(threeColumnWidth);
        float totalSum = getTotalSum(productList);
        Table totalSumTable = new Table(fullwidth);
        totalSumTable.addCell(new Cell().add("Total Sum:").setBold().setBorder(Border.NO_BORDER));
        totalSumTable.addCell(new Cell().add(String.valueOf(totalSum)).setBold().setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
        document.add(totalSumTable);

        float totalRowWidth = 500f; // Adjust this value based on your layout
        float[] totalRowColumnWidths = { totalRowWidth }; // Adjust widths as needed

        Table totalRowTable = new Table(totalRowColumnWidths);
        totalRowTable.setBackgroundColor(headerColor, 0.7f);

        for (Product product : productList) {
            float total = product.getQuantity() * product.getPriceperpeice();
            String itemName = product.getPname().orElse("");
            int itemQuantity = product.getQuantity();
            double itemPrice = product.getPriceperpeice();
            double itemTotal = itemQuantity * itemPrice;
            createProductRow(itemName, itemQuantity, itemPrice, itemTotal);
        }

        document.add(threeColTable2.setMarginBottom(20f));
        float onetwo[] = { threecol + 125f, threecol * 2 };
        Table threeColTable4 = new Table(onetwo);
        threeColTable4.addCell(new Cell().add("").setBorder(Border.NO_BORDER));
        threeColTable4.addCell(new Cell().add(fullwidthDashedBorder(fullwidth)).setBorder(Border.NO_BORDER));
        document.add(threeColTable4);

        totalRowTable.addCell(new Cell(1, 2).add("Total").setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
        totalRowTable.addCell(new Cell().add(String.valueOf(totalSum)).setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setMarginRight(10f);

        document.add(totalRowTable);

        document.add(fullwidthDashedBorder(fullwidth));
        document.add(new Paragraph("\n"));
        document.add(getDividerTable(fullwidth).setBorder(new SolidBorder(Color.GRAY, 1)).setMarginBottom(15f));

        Table threeColTable3 = new Table(threeColumnWidth);
        threeColTable3.addCell(new Cell().add("").setBorder(Border.NO_BORDER)).setMarginLeft(10f);
        threeColTable3.addCell(new Cell().add("Total").setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        threeColTable3.addCell(new Cell().add(String.valueOf(totalSum)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setMarginRight(15f);

        document.add(threeColTable3);
        document.add(fullwidthDashedBorder(fullwidth));
        document.add(new Paragraph("\n"));
        document.add(getDividerTable(fullwidth).setBorder(new SolidBorder(Color.GRAY, 1)).setMarginBottom(15f));
    }
    private void createProductRow(String itemName, int itemQuantity, double itemPrice, double itemTotal) {
        Cell cellItemName = new Cell().add(itemName).setFont(regularFont).setFontColor(Color.BLACK).setBorder(Border.NO_BORDER);
        Cell cellQuantity = new Cell().add(String.valueOf(itemQuantity)).setFont(regularFont)
                .setFontColor(Color.BLACK).setHorizontalAlignment(HorizontalAlignment.CENTER).setBorder(Border.NO_BORDER);
        Cell cellPrice = new Cell().add(String.format("%.2f", itemPrice)).setFont(regularFont)
                .setFontColor(Color.BLACK).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER);
        Cell cellTotal = new Cell().add(String.format("%.2f", itemTotal)).setFont(regularFont)
                .setFontColor(Color.BLACK).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER);

        if (itemName.equals("Total")) {
            cellTotal.setFontColor(Color.WHITE)
                    .setBackgroundColor(new DeviceRgb(64, 64, 64)) // Dark gray background
                    .setBold(); // Make the text bold
        }

        Table productTable = new Table(threeColumnWidth);
        productTable.addCell(cellItemName);
        productTable.addCell(cellQuantity);
        productTable.addCell(cellPrice);
        productTable.addCell(cellTotal);

        productTable.setMarginTop(20); // Adjust the spacing as needed

        document.add(productTable);
    }

    public float getTotalSum(List<Product> productList) {
        return  (float)productList.stream().mapToLong((p)-> (long) (p.getQuantity()*p.getPriceperpeice())).sum();
    }

    public void createAddress(AddressDetails addressDetails) {
        // Table for Billing and Shipping Information
        Table infoTable = new Table(twocolumnWidth);
        infoTable.addCell(getBillingandShippingCell(addressDetails.getBillingInfoText())).setPadding(5);
        document.add(infoTable.setMarginBottom(12f));

        // Info First Row
        Table infoFirstRowTable = new Table(twocolumnWidth);
        infoFirstRowTable.addCell(getCell10fLeft(addressDetails.getBillingCompanyText(),true));
        infoFirstRowTable.addCell(getCell10fLeft(addressDetails.getBillingCompany(),false));
        document.add(infoFirstRowTable.setMarginBottom(10f));

        // Info Second Row
        Table infoSecondRowTable = new Table(twocolumnWidth);
        infoSecondRowTable.addCell(getCell10fLeft(addressDetails.getBillingNameText(),true));
        infoSecondRowTable.addCell(getCell10fLeft(addressDetails.getBillingName(),false));
        document.add(infoSecondRowTable.setMarginBottom(10f));


        // Info Third Row (Billing Address and Email)
        Table infoThirdRowTable = new Table(twocolumnWidth);
        infoThirdRowTable.addCell(getCell10fLeft(addressDetails.getBillingAddressText(), true));
        infoThirdRowTable.addCell(getCell10fLeft(addressDetails.getBillingAddress(), false));
        document.add(infoThirdRowTable.setMarginBottom(10f));

        // Info Fourth Row (Phone Number)
        Table infoFourthRowTable = new Table(twocolumnWidth);
        Cell cellPhoneText = new Cell().add(getCell10fLeft(addressDetails.getBillingPhoneNumberText(), true)).setBorder(Border.NO_BORDER);
        Cell cellPhoneNumber = new Cell().add(getCell10fLeft(addressDetails.getBillingPhoneNumber(), false)).setBorder(Border.NO_BORDER);
        infoFourthRowTable.addCell(cellPhoneText);
        infoFourthRowTable.addCell(cellPhoneNumber);
        cellPhoneText.setTextAlignment(TextAlignment.LEFT).setVerticalAlignment(VerticalAlignment.MIDDLE);
        cellPhoneNumber.setTextAlignment(TextAlignment.LEFT).setVerticalAlignment(VerticalAlignment.MIDDLE);
        document.add(infoFourthRowTable.setMarginBottom(10f));

        Table infoFifthRowTable = new Table(twocolumnWidth);
        infoFifthRowTable.addCell(getCell10fLeft(addressDetails.getBillingEmailText(), true));
        infoFifthRowTable.addCell(getCell10fLeft(addressDetails.getBillingEmail(), false));
        document.add(infoFifthRowTable.setMarginBottom(10f));

        // Add dashed border below address section
        document.add(fullwidthDashedBorder(fullwidth));
    }
    public void addProduct(String name, int quantity, double price, double total) {
        float[] columnWidths = {340f, 60f, 80f, 100f}; // Adjust column widths as needed
        Table productTable = new Table(columnWidths);

        productTable.addCell(new Cell().add(name).setBorder(Border.NO_BORDER));
        productTable.addCell(new Cell().add(String.valueOf(quantity)).setHorizontalAlignment(HorizontalAlignment.CENTER).setBorder(Border.NO_BORDER)).setPadding(5);;
        productTable.addCell(new Cell().add(String.format("%.1f", price)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setPadding(5);;
        productTable.addCell(new Cell().add(String.format("%.2f", total)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setPadding(5);;

        document.add(productTable);
    }


    public void createHeader(HeaderDetails header) {
        Table table=new Table(twocolumnWidth);
        table.addCell(new Cell().add(header.getInvoiceTitle()).setFontSize(20f).setBorder(Border.NO_BORDER).setBold());
        Table nestedtabe=new Table(new float[]{twocol/2,twocol/2});
        nestedtabe.addCell(getHeaderTextCell(header.getInvoiceNoText()));
        nestedtabe.addCell(getHeaderTextCellValue(header.getInvoiceNo()));
        nestedtabe.addCell(getHeaderTextCell(header.getInvoiceDateText()));
        nestedtabe.addCell(getHeaderTextCellValue(header.getInvoiceDate()));
        table.addCell(new Cell().add(nestedtabe).setBorder(Border.NO_BORDER));
        Border gb=new SolidBorder(header.getBorderColor(),2f);
        document.add(table);
        document.add(getNewLineParagraph());
        document.add(getDividerTable(fullwidth).setBorder(gb));
        document.add(getNewLineParagraph());
    }



    static  Table getDividerTable(float[] fullwidth) {
        return new Table(fullwidth);
    }

    public static Table fullwidthDashedBorder(float[] fullwidth) {
        Table tableDivider2=new Table(fullwidth);
        Border dgb=new DashedBorder(Color.GRAY,0.5f);
        tableDivider2.setBorder(dgb);
        return tableDivider2;
    }

    static  Paragraph getNewLineParagraph() {
        return new Paragraph("\n");
    }

    static Cell getHeaderTextCell(String textValue) {
        return new Cell().add(textValue).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
    }

    static Cell getHeaderTextCellValue(String textValue) {
        return new Cell().add(textValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }

    static Cell getBillingandShippingCell(String textValue) {
        return new Cell().add(textValue).setFontSize(12f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }

    static  Cell getCell10fLeft(String textValue,Boolean isBold) {
        Cell myCell=new Cell().add(textValue).setFontSize(10f).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).setPadding(5);
        return  isBold ?myCell.setBold():myCell;
    }
}