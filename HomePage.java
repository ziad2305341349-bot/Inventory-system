package umsSPEC;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class HomePage extends JFrame {

    JButton btnCustomer, btnCategory, btnBrand, btnSupplier, btnProduct, btnPurchases, btnOrders;

    public HomePage() {
        setTitle("Inventory Management System");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(null);

        // Title
        JLabel title = new JLabel("📊 Inventory Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(44, 62, 80));
        title.setBounds(200, 40, 500, 40);
        add(title);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 1, 10, 15));
        buttonPanel.setBounds(250, 100, 300, 360);
        buttonPanel.setBackground(Color.WHITE);
        add(buttonPanel);

        // Buttons
        btnCustomer = createStyledButton("👤 Customer");
        btnCategory = createStyledButton("📂 Category");
        btnBrand = createStyledButton("🏷️ Brand");
        btnSupplier = createStyledButton("🚚 Supplier");
        btnProduct = createStyledButton("📦 Product");
        btnPurchases = createStyledButton("🛒 Purchases");
        btnOrders = createStyledButton("🧾 Orders");

        // Add buttons to panel
        buttonPanel.add(btnCustomer);
        buttonPanel.add(btnCategory);
        buttonPanel.add(btnBrand);
        buttonPanel.add(btnSupplier);
        buttonPanel.add(btnProduct);
        buttonPanel.add(btnPurchases);
        buttonPanel.add(btnOrders);

        // Add action listeners
        btnCustomer.addActionListener(e -> {
            new CustomerManagement().setVisible(true);
            dispose();
        });

        btnCategory.addActionListener(e -> {
            new CategoryManagement().setVisible(true);
            dispose();
        });
        
        btnBrand.addActionListener(e -> {
            new BrandManagement().setVisible(true);
            dispose();
        });
        
        btnSupplier.addActionListener(e -> {
            new SupplierManagement().setVisible(true);
            dispose();
        });

        btnProduct.addActionListener(e -> {
            new ProductManagement().setVisible(true);
            dispose();
        });
        btnPurchases.addActionListener(e -> {
            new PurchaseManagement().setVisible(true);
            dispose();
        });
        btnOrders.addActionListener(e -> {
            new OrderManagement().setVisible(true);
            dispose();
        });
        
        // You can later add action listeners for other buttons
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(52, 152, 219));
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HomePage().setVisible(true));
    }
}
