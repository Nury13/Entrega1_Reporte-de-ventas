package Entrega1;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class to process input files and generate reports.
 * Input:
 *  - output/salesmen.txt
 *  - output/products.txt
 *  - output/sales_[id].txt (one per salesman)
 *
 * Output:
 *  - output/report_salesmen.csv
 *  - output/report_products.csv
 */
public class Main {

    private static final String INPUT_DIR = "output/";
    private static final String SALESMEN_FILE = INPUT_DIR + "salesmen.txt";
    private static final String PRODUCTS_FILE = INPUT_DIR + "products.txt";

    public static void main(String[] args) {
        try {
            // Load salesmen and products
            Map<Long, String> salesmen = loadSalesmen();
            Map<String, Product> products = loadProducts();

            // Process sales files
            Map<Long, Double> salesmanRevenue = new HashMap<>();
            Map<String, Integer> productQuantities = new HashMap<>();

            Files.list(Paths.get(INPUT_DIR))
                    .filter(path -> path.getFileName().toString().startsWith("sales_"))
                    .forEach(path -> processSalesFile(path.toFile(), products, salesmanRevenue, productQuantities));

            // Generate reports
            generateSalesmenReport(salesmen, salesmanRevenue);
            generateProductsReport(products, productQuantities);

            System.out.println("Reports successfully generated.");

        } catch (Exception e) {
            System.err.println("❌ Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------- LOADERS -----------------

    private static Map<Long, String> loadSalesmen() throws IOException {
        Map<Long, String> salesmen = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SALESMEN_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                long id = Long.parseLong(parts[1]);
                String fullName = parts[2] + " " + parts[3];
                salesmen.put(id, fullName);
            }
        }
        return salesmen;
    }

    private static Map<String, Product> loadProducts() throws IOException {
        Map<String, Product> products = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                String code = parts[0];
                String name = parts[1];
                double price = Double.parseDouble(parts[2]);
                products.put(code, new Product(code, name, price));
            }
        }
        return products;
    }

    // ----------------- PROCESS SALES -----------------

    private static void processSalesFile(File file, Map<String, Product> products,
                                         Map<Long, Double> salesmanRevenue,
                                         Map<String, Integer> productQuantities) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip "DocumentType;ID"
            String[] idLine = br.readLine().split(";");
            long salesmanId = Long.parseLong(idLine[1]);

            br.readLine(); // skip "ProductCode;QuantitySold;"
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                String productCode = parts[0];
                int quantity = Integer.parseInt(parts[1]);

                Product product = products.get(productCode);
                if (product != null) {
                    // update salesman revenue
                    double revenue = quantity * product.price;
                    salesmanRevenue.put(salesmanId, salesmanRevenue.getOrDefault(salesmanId, 0.0) + revenue);

                    // update product quantities
                    productQuantities.put(productCode, productQuantities.getOrDefault(productCode, 0) + quantity);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
        }
    }

    // ----------------- REPORTS -----------------

    private static void generateSalesmenReport(Map<Long, String> salesmen, Map<Long, Double> salesmanRevenue) throws IOException {
        List<Map.Entry<Long, Double>> sorted = salesmanRevenue.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        try (FileWriter writer = new FileWriter(INPUT_DIR + "report_salesmen.csv")) {
            writer.write("ID;Name;Revenue\n");
            for (Map.Entry<Long, Double> entry : sorted) {
                writer.write(entry.getKey() + ";" + salesmen.get(entry.getKey()) + ";" + entry.getValue() + "\n");
            }
        }
    }

    private static void generateProductsReport(Map<String, Product> products, Map<String, Integer> productQuantities) throws IOException {
        List<Map.Entry<String, Integer>> sorted = productQuantities.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        try (FileWriter writer = new FileWriter(INPUT_DIR + "report_products.csv")) {
            writer.write("ProductCode;Name;Price;QuantitySold\n");
            for (Map.Entry<String, Integer> entry : sorted) {
                Product p = products.get(entry.getKey());
                writer.write(p.code + ";" + p.name + ";" + p.price + ";" + entry.getValue() + "\n");
            }
        }
    }

    // ----------------- HELPER CLASS -----------------

    static class Product {
        String code;
        String name;
        double price;

        Product(String code, String name, double price) {
            this.code = code;
            this.name = name;
            this.price = price;
        }
    }
}
