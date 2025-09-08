package Entrega1;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.io.File;

/**
 * This is the main class to generate files with information about salesmen, products, and sales.
 * Generated files:
 *  - output/salesmen.txt → contains ID, first name, and last name of each salesman.
 *  - output/products.txt → contains product code, name, and random price.
 *  - output/sales_[id].txt → contains the sales made by each salesman.
 */
public class GenerateInfoFiles {

    private static final String OUTPUT_DIR = "output/"; // output folder
    private static final String DOCUMENT_TYPE = "CC";   // defines the document type

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // ✅ Create output folder if it doesn't exist
            File outputDir = new File(OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Request general values for the report
            System.out.print("Enter the number of salesmen: ");
            int salesmanCount = scanner.nextInt();

            System.out.print("Enter the number of products: ");
            int productCount = scanner.nextInt();

            System.out.print("Enter the number of sales per salesman: ");
            int salesPerSalesman = scanner.nextInt();
            scanner.nextLine(); // clear buffer

            // arrays to store salesman data
            String[] firstNames = new String[salesmanCount];
            String[] lastNames = new String[salesmanCount];
            long[] ids = new long[salesmanCount];

            // loop to capture salesman information
            for (int i = 0; i < salesmanCount; i++) {
                System.out.print("Enter the ID of salesman " + (i + 1) + ": ");
                ids[i] = scanner.nextLong();
                scanner.nextLine();

                System.out.print("Enter the first name of salesman " + (i + 1) + ": ");
                firstNames[i] = scanner.nextLine();

                System.out.print("Enter the last name of salesman " + (i + 1) + ": ");
                lastNames[i] = scanner.nextLine();
            }

            // generate file with salesmen
            createSalesmenInfoFile(salesmanCount, firstNames, lastNames, ids);

            // generate file with products
            createProductsFile(productCount, scanner);

            // generate one sales file for each salesman
            for (int i = 0; i < salesmanCount; i++) {
                createSalesFile(salesPerSalesman, ids[i], firstNames[i], lastNames[i], productCount);
            }

            System.out.println("✅ Reports successfully generated in folder: " + OUTPUT_DIR);

        } catch (Exception e) {
            System.err.println("❌ Error generating files: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    /**
     * Generates the file with salesman information.
     * Structure: documentType;ID;firstName;lastName
     */
    public static void createSalesmenInfoFile(int salesmanCount, String[] firstNames, String[] lastNames, long[] ids) throws IOException {
        try (FileWriter writer = new FileWriter(OUTPUT_DIR + "salesmen.txt")) {
            writer.write("DocumentType;ID;FirstName;LastName\n"); // header
            for (int i = 0; i < salesmanCount; i++) {
                writer.write(DOCUMENT_TYPE + ";" + ids[i] + ";" + firstNames[i] + ";" + lastNames[i] + "\n");
            }
        }
    }

    /**
     * Generates the file with products.
     * Each product has a code (P0, P1, P2...), a user-entered name,
     * and a random price between 100,000 and 10,000,000.
     */
    public static void createProductsFile(int productsCount, Scanner scanner) throws IOException {
        Random random = new Random();
        try (FileWriter writer = new FileWriter(OUTPUT_DIR + "products.txt")) {
            writer.write("Code;Name;Price\n"); // header
            for (int i = 0; i < productsCount; i++) {
                System.out.print("Enter the name of product " + (i + 1) + ": ");
                String productName = scanner.nextLine();
                int price = 100000 + random.nextInt(9990000);
                writer.write("P" + i + ";" + productName + ";" + price + "\n");
            }
        }
    }

    /**
     * Generates the sales file for each salesman.
     * Structure:
     *  - documentType;ID
     *  - productCode;soldQuantity;
     */
    public static void createSalesFile(int salesCount, long id, String firstName, String lastName, int productCount) throws IOException {
        Random random = new Random();
        String filename = OUTPUT_DIR + "sales_" + id + ".txt";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("DocumentType;ID\n"); // header
            writer.write(DOCUMENT_TYPE + ";" + id + "\n");

            writer.write("ProductCode;QuantitySold;\n"); // sales header
            for (int i = 0; i < salesCount; i++) {
                int productId = random.nextInt(productCount);
                int quantity = 1 + random.nextInt(20);
                writer.write("P" + productId + ";" + quantity + ";\n");
            }
        }
    }
}
