import Com.Bills.Model.AddressDetails;
import Com.Bills.Model.HeaderDetails;
import Com.Bills.Model.ProductTableHeader;
import Com.Bills.Service.PDFInvoice;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Bill {

    public static class Product {
        private String name;
        private int quantity;
        private double price;
        private double total;

        public Product(String name, int quantity, double price, double total) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.total = total;
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("--------GADA ELECTRONICS--------");

        System.out.print("Enter Customer Name: ");
        String customerName = input.nextLine();

        System.out.print("Enter Customer Phone Number: ");
        String phoneNumber = input.nextLine();

        System.out.print("Enter the number of items: ");
        int numItems = input.nextInt();
        input.nextLine(); // Consume newline

        System.out.print("Enter Customer Address: ");
        String address = input.nextLine();

        System.out.print("Enter Customer Email: ");
        String email = input.nextLine();

        double totalAmount = 0;
        List<Product> productList = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            System.out.println("Item " + i + ":");
            System.out.print("Enter item name: ");
            String itemName = input.nextLine();

            System.out.print("Enter item price: ");
            double itemPrice = input.nextDouble();

            System.out.print("Enter item quantity: ");
            int itemQuantity = input.nextInt();
            input.nextLine(); // Consume newline

            double itemTotal = itemPrice * itemQuantity;
            totalAmount += itemTotal;

            productList.add(new Product(itemName, itemQuantity, itemPrice, itemTotal));
        }

        double gst = totalAmount * 0.18;
        double totalBill = totalAmount + gst;

        try {
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH_mm_ss");
            String formattedDateTime = currentDateTime.format(formatter);
            String pdfName = "src/" + formattedDateTime + ".pdf";

            // Create a PDF document and initialize the PDFInvoice
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfName));
            Document document = new Document(pdfDocument);
            PDFInvoice invoice = new PDFInvoice(pdfName, document);

            // Create Header
            HeaderDetails header = new HeaderDetails();
            header.setInvoiceNo("BI0001").setInvoiceDate(currentDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).build();
            invoice.createHeader(header);

            // Create Address
            AddressDetails addressDetails = new AddressDetails();
            addressDetails.setBillingCompanyText("Company Name :");
            addressDetails.setBillingCompany("--------GADA ELECTRONICS--------");
            addressDetails.setBillingNameText("Customer's Name :");
            addressDetails.setBillingName(customerName);
            addressDetails.setBillingAddressText("Customer's Address:");
            addressDetails.setBillingAddress(address);
            addressDetails.setBillingPhoneNumberText("Customer's Phone no:");
            addressDetails.setBillingPhoneNumber(phoneNumber);
            addressDetails.setBillingEmailText("Email:");
            addressDetails.setBillingEmail(email);
            invoice.createAddress(addressDetails);

            // Product Table
            ProductTableHeader productTableHeader = new ProductTableHeader();
            productTableHeader.setDescription("Description");
            productTableHeader.setQuantity("Qty");
            productTableHeader.setPrice("Unit Price");
            invoice.createTableHeader(productTableHeader);

            int totalQuantity = 0;
            for (Product product : productList) {
                totalQuantity += product.quantity;
                invoice.addProduct(product.name, product.quantity, product.price, product.total);
            }

            invoice.addProduct("Total", totalQuantity, 0, totalAmount);

            Table dashedBorderTable = PDFInvoice.fullwidthDashedBorder(new float[]{invoice.fullwidth[0]});
            document.add(dashedBorderTable);
            // Terms and Conditions
            List<String> TncList = new ArrayList<>();
            TncList.add("1. The Seller shall not be liable to the Buyer directly or indirectly for any loss or damage suffered by the Buyer.");
            TncList.add("2. The Seller warrants the product for one (1) year from the date of shipment");
            String imagePath = "C:\\Users\\Vivek\\Pictures\\Camera Roll\\20210120_093225.jpg";
            invoice.createTnc(TncList, false, imagePath);

            // Close the PDF document
            document.close();
            System.out.println("PDF generated successfully.");
        } catch (FileNotFoundException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        } finally {
            input.close();
        }
    }
}
