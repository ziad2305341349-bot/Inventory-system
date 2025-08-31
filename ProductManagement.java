package umsSPEC;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ProductManagement extends JFrame implements ActionListener {

    JLabel titleLabel;
    JButton btnAdd, btnEdit, btnView, btnDelete, btnBack, btnRefresh;
    JTable productTable;
    DefaultTableModel tableModel;

    Connection conn;

    public ProductManagement() {
        setTitle("SPEC - Product Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        titleLabel = new JLabel("📦 Product Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(40, 75, 99));
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        btnAdd = createButton("➕ Add Product", 30, 90);
        btnEdit = createButton("✏️ Edit Selected", 30, 150);
        btnView = createButton("👁️ View Details", 30, 210);
        btnDelete = createButton("🗑️ Delete Selected", 30, 270);
        btnBack = createButton("🔙 Back", 30, 330);
        btnRefresh = createButton("💾 Refresh", 30, 390);

        add(btnAdd);
        add(btnEdit);
        add(btnView);
        add(btnDelete);
        add(btnBack);
        add(btnRefresh);

        String[] columnNames = {"Product ID", "Name", "Category", "Brand", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);
        productTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        productTable.setRowHeight(25);
        productTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBounds(240, 90, 720, 420);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane);

        connectToDatabase();
        createProductTable();
        loadFromDatabase();

        btnRefresh.addActionListener(e -> loadFromDatabase());
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 180, 40);
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 120, 180));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.addActionListener(this);
        return button;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
            String name = JOptionPane.showInputDialog(this, "Enter Product Name:");
            String category = JOptionPane.showInputDialog(this, "Enter Category:");
            String brand = JOptionPane.showInputDialog(this, "Enter Brand:");
            String price = JOptionPane.showInputDialog(this, "Enter Price:");
            if (id != null && name != null && category != null && brand != null && price != null) {
                insertProduct(id, name, category, brand, price);
                loadFromDatabase();
            }
        } else if (e.getSource() == btnEdit) {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                String name = JOptionPane.showInputDialog(this, "Edit Name:", tableModel.getValueAt(row, 1));
                String category = JOptionPane.showInputDialog(this, "Edit Category:", tableModel.getValueAt(row, 2));
                String brand = JOptionPane.showInputDialog(this, "Edit Brand:", tableModel.getValueAt(row, 3));
                String price = JOptionPane.showInputDialog(this, "Edit Price:", tableModel.getValueAt(row, 4));
                if (name != null && category != null && brand != null && price != null) {
                    updateProduct(id, name, category, brand, price);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        } else if (e.getSource() == btnView) {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                StringBuilder details = new StringBuilder();
                for (int i = 0; i < productTable.getColumnCount(); i++) {
                    details.append(productTable.getColumnName(i)).append(": ")
                           .append(tableModel.getValueAt(row, i)).append("\n");
                }
                JOptionPane.showMessageDialog(this, details.toString(), "Product Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to view.");
            }
        } else if (e.getSource() == btnDelete) {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this product?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteProduct(id);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            }
        } else if (e.getSource() == btnBack) {
            dispose();
            new HomePage().setVisible(true);
        }
    }

    private void connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void createProductTable() {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                     "id VARCHAR(255) PRIMARY KEY, " +
                     "name VARCHAR(255), " +
                     "category VARCHAR(255), " +
                     "brand VARCHAR(255), " +
                     "price VARCHAR(50))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage());
        }
    }

    private void insertProduct(String id, String name, String category, String brand, String price) {
        String sql = "INSERT INTO products (id, name, category, brand, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setString(4, brand);
            pstmt.setString(5, price);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting product: " + e.getMessage());
        }
    }

    private void updateProduct(String id, String name, String category, String brand, String price) {
        String sql = "UPDATE products SET name = ?, category = ?, brand = ?, price = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setString(3, brand);
            pstmt.setString(4, price);
            pstmt.setString(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating product: " + e.getMessage());
        }
    }

    private void deleteProduct(String id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage());
        }
    }

    private void loadFromDatabase() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM products";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("brand"),
                    rs.getString("price")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductManagement().setVisible(true));
    }
}
