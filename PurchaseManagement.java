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

public class PurchaseManagement extends JFrame implements ActionListener {

    JLabel titleLabel;
    JButton btnAdd, btnEdit, btnView, btnDelete, btnBack, btnSaveToFile;
    JTable purchaseTable;
    DefaultTableModel tableModel;

    Connection conn;

    public PurchaseManagement() {
        setTitle("SPEC - Purchase Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        titleLabel = new JLabel("🛒 Purchase Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(40, 75, 99));
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        btnAdd = createButton("➕ Add New Purchase", 30, 90);
        btnEdit = createButton("✏️ Edit Selected Purchase", 30, 150);
        btnView = createButton("👁️ View Purchase Details", 30, 210);
        btnDelete = createButton("🗑️ Delete Selected Purchase", 30, 270);
        btnBack = createButton("🔙 Back", 30, 330);
        btnSaveToFile = createButton("💾 Refresh", 30, 390);

        add(btnAdd);
        add(btnEdit);
        add(btnView);
        add(btnDelete);
        add(btnBack);
        add(btnSaveToFile);

        String[] columnNames = {"Purchase ID", "Product Name", "Quantity", "Price", "Supplier", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        purchaseTable = new JTable(tableModel);
        purchaseTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        purchaseTable.setRowHeight(25);
        purchaseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(purchaseTable);
        scrollPane.setBounds(240, 90, 720, 420);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane);

        connectToDatabase();
        createPurchaseTable();
        loadFromDatabase();

        btnSaveToFile.addActionListener(e -> loadFromDatabase());
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
            String id = JOptionPane.showInputDialog(this, "Enter Purchase ID:");
            String productName = JOptionPane.showInputDialog(this, "Enter Product Name:");
            String quantity = JOptionPane.showInputDialog(this, "Enter Quantity:");
            String price = JOptionPane.showInputDialog(this, "Enter Price:");
            String supplier = JOptionPane.showInputDialog(this, "Enter Supplier:");
            String date = JOptionPane.showInputDialog(this, "Enter Date of Purchase:");
            if (id != null && productName != null && quantity != null && price != null && supplier != null && date != null) {
                insertPurchase(id, productName, quantity, price, supplier, date);
                loadFromDatabase();
            }
        } else if (e.getSource() == btnEdit) {
            int row = purchaseTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                String productName = JOptionPane.showInputDialog(this, "Edit Product Name:", tableModel.getValueAt(row, 1));
                String quantity = JOptionPane.showInputDialog(this, "Edit Quantity:", tableModel.getValueAt(row, 2));
                String price = JOptionPane.showInputDialog(this, "Edit Price:", tableModel.getValueAt(row, 3));
                String supplier = JOptionPane.showInputDialog(this, "Edit Supplier:", tableModel.getValueAt(row, 4));
                String date = JOptionPane.showInputDialog(this, "Edit Date:", tableModel.getValueAt(row, 5));
                if (productName != null && quantity != null && price != null && supplier != null && date != null) {
                    updatePurchase(id, productName, quantity, price, supplier, date);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        } else if (e.getSource() == btnView) {
            int row = purchaseTable.getSelectedRow();
            if (row != -1) {
                StringBuilder details = new StringBuilder();
                for (int i = 0; i < purchaseTable.getColumnCount(); i++) {
                    details.append(purchaseTable.getColumnName(i)).append(": ")
                           .append(tableModel.getValueAt(row, i)).append("\n");
                }
                JOptionPane.showMessageDialog(this, details.toString(), "Purchase Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to view.");
            }
        } else if (e.getSource() == btnDelete) {
            int row = purchaseTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this purchase?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePurchase(id);
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

    private void createPurchaseTable() {
        String sql = "CREATE TABLE IF NOT EXISTS purchases (" +
                     "id VARCHAR(255) PRIMARY KEY, " +
                     "product VARCHAR(255), " +
                     "quantity VARCHAR(50), " +
                     "price VARCHAR(50), " +
                     "supplier VARCHAR(255), " +
                     "date VARCHAR(50))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage());
        }
    }

    private void insertPurchase(String id, String product, String quantity, String price, String supplier, String date) {
        String sql = "INSERT INTO purchases (id, product, quantity, price, supplier, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, product);
            pstmt.setString(3, quantity);
            pstmt.setString(4, price);
            pstmt.setString(5, supplier);
            pstmt.setString(6, date);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting purchase: " + e.getMessage());
        }
    }

    private void updatePurchase(String id, String product, String quantity, String price, String supplier, String date) {
        String sql = "UPDATE purchases SET product=?, quantity=?, price=?, supplier=?, date=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product);
            pstmt.setString(2, quantity);
            pstmt.setString(3, price);
            pstmt.setString(4, supplier);
            pstmt.setString(5, date);
            pstmt.setString(6, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating purchase: " + e.getMessage());
        }
    }

    private void deletePurchase(String id) {
        String sql = "DELETE FROM purchases WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting purchase: " + e.getMessage());
        }
    }

    private void loadFromDatabase() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM purchases";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("product"),
                    rs.getString("quantity"),
                    rs.getString("price"),
                    rs.getString("supplier"),
                    rs.getString("date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading purchases: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PurchaseManagement().setVisible(true));
    }
}
