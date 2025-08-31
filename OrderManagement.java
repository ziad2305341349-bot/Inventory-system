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

public class OrderManagement extends JFrame implements ActionListener {

    JLabel titleLabel;
    JButton btnAdd, btnEdit, btnView, btnDelete, btnBack, btnSaveToFile;
    JTable orderTable;
    DefaultTableModel tableModel;

    Connection conn;

    public OrderManagement() {
        setTitle("SPEC - Order Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        titleLabel = new JLabel("📦 Order Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(40, 75, 99));
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        btnAdd = createButton("➕ Add New Order", 30, 90);
        btnEdit = createButton("✏️ Edit Selected Order", 30, 150);
        btnView = createButton("👁️ View Order Details", 30, 210);
        btnDelete = createButton("🗑️ Delete Selected Order", 30, 270);
        btnBack = createButton("🔙 Back", 30, 330);
        btnSaveToFile = createButton("💾 Refresh", 30, 390);

        add(btnAdd);
        add(btnEdit);
        add(btnView);
        add(btnDelete);
        add(btnBack);
        add(btnSaveToFile);

        String[] columnNames = {"Order ID", "Product Name", "Customer", "Quantity", "Price", "Order Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        orderTable = new JTable(tableModel);
        orderTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        orderTable.setRowHeight(25);
        orderTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBounds(240, 90, 720, 420);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane);

        connectToDatabase();
        createOrderTable();
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
            String id = JOptionPane.showInputDialog(this, "Enter Order ID:");
            String product = JOptionPane.showInputDialog(this, "Enter Product Name:");
            String customer = JOptionPane.showInputDialog(this, "Enter Customer Name:");
            String quantity = JOptionPane.showInputDialog(this, "Enter Quantity:");
            String price = JOptionPane.showInputDialog(this, "Enter Price:");
            String date = JOptionPane.showInputDialog(this, "Enter Order Date:");
            if (id != null && product != null && customer != null && quantity != null && price != null && date != null) {
                insertOrder(id, product, customer, quantity, price, date);
                loadFromDatabase();
            }
        } else if (e.getSource() == btnEdit) {
            int row = orderTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                String product = JOptionPane.showInputDialog(this, "Edit Product Name:", tableModel.getValueAt(row, 1));
                String customer = JOptionPane.showInputDialog(this, "Edit Customer Name:", tableModel.getValueAt(row, 2));
                String quantity = JOptionPane.showInputDialog(this, "Edit Quantity:", tableModel.getValueAt(row, 3));
                String price = JOptionPane.showInputDialog(this, "Edit Price:", tableModel.getValueAt(row, 4));
                String date = JOptionPane.showInputDialog(this, "Edit Order Date:", tableModel.getValueAt(row, 5));
                if (product != null && customer != null && quantity != null && price != null && date != null) {
                    updateOrder(id, product, customer, quantity, price, date);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        } else if (e.getSource() == btnView) {
            int row = orderTable.getSelectedRow();
            if (row != -1) {
                StringBuilder details = new StringBuilder();
                for (int i = 0; i < orderTable.getColumnCount(); i++) {
                    details.append(orderTable.getColumnName(i)).append(": ")
                           .append(tableModel.getValueAt(row, i)).append("\n");
                }
                JOptionPane.showMessageDialog(this, details.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to view.");
            }
        } else if (e.getSource() == btnDelete) {
            int row = orderTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this order?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteOrder(id);
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

    private void createOrderTable() {
        String sql = "CREATE TABLE IF NOT EXISTS orders (" +
                     "id VARCHAR(255) PRIMARY KEY, " +
                     "product VARCHAR(255), " +
                     "customer VARCHAR(255), " +
                     "quantity VARCHAR(50), " +
                     "price VARCHAR(50), " +
                     "date VARCHAR(50))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage());
        }
    }

    private void insertOrder(String id, String product, String customer, String quantity, String price, String date) {
        String sql = "INSERT INTO orders (id, product, customer, quantity, price, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, product);
            pstmt.setString(3, customer);
            pstmt.setString(4, quantity);
            pstmt.setString(5, price);
            pstmt.setString(6, date);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting order: " + e.getMessage());
        }
    }

    private void updateOrder(String id, String product, String customer, String quantity, String price, String date) {
        String sql = "UPDATE orders SET product=?, customer=?, quantity=?, price=?, date=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product);
            pstmt.setString(2, customer);
            pstmt.setString(3, quantity);
            pstmt.setString(4, price);
            pstmt.setString(5, date);
            pstmt.setString(6, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating order: " + e.getMessage());
        }
    }

    private void deleteOrder(String id) {
        String sql = "DELETE FROM orders WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting order: " + e.getMessage());
        }
    }

    private void loadFromDatabase() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM orders";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("product"),
                    rs.getString("customer"),
                    rs.getString("quantity"),
                    rs.getString("price"),
                    rs.getString("date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderManagement().setVisible(true));
    }
}
