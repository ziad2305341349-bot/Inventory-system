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

public class CustomerManagement extends JFrame implements ActionListener {

    JLabel hlbl;
    JButton btnAddCustomer, btnEditCustomer, btnDeleteCustomer, btnBack, btnSave;
    JTable customerTable;
    DefaultTableModel tableModel;

    Connection conn;

    public CustomerManagement() {
        super("Customer Management");

        // Frame settings
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(null);

        // Title
        hlbl = new JLabel("Customer Management");
        hlbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        hlbl.setForeground(new Color(40, 75, 99));
        hlbl.setBounds(30, 20, 400, 40);
        add(hlbl);

        // Buttons
        btnAddCustomer = createButton("➕ Add Customer", 30, 90);
        btnEditCustomer = createButton("✏️ Edit Selected", 30, 150);
        btnDeleteCustomer = createButton("🗑️ Delete Selected", 30, 210);
        btnBack = createButton("🔙 Back", 30, 270);
        btnSave = createButton("💾 Refresh", 30, 330);
        add(btnAddCustomer);
        add(btnEditCustomer);
        add(btnDeleteCustomer);
        add(btnBack);
        add(btnSave);

        // Table
        String[] columnNames = {"ID", "Name", "Email", "Phone"};
        tableModel = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(tableModel);
        customerTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        customerTable.setRowHeight(25);
        customerTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        customerTable.setGridColor(new Color(220, 220, 220));
        customerTable.setShowHorizontalLines(true);
        customerTable.setShowVerticalLines(false);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBounds(240, 90, 720, 420);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane);

        // Database setup
        connectToDatabase();
        createCustomerTable();
        loadFromDatabase();

        btnSave.addActionListener(e -> loadFromDatabase());
    }

    private JButton createButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 180, 40);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(60, 120, 180));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddCustomer) {
            String id = JOptionPane.showInputDialog(this, "Enter Customer ID:");
            String name = JOptionPane.showInputDialog(this, "Enter Name:");
            String email = JOptionPane.showInputDialog(this, "Enter Email:");
            String phone = JOptionPane.showInputDialog(this, "Enter Phone:");
            if (id != null && name != null && email != null && phone != null) {
                insertCustomer(id, name, email, phone);
                loadFromDatabase();
            }
        } else if (e.getSource() == btnEditCustomer) {
            int row = customerTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                String name = JOptionPane.showInputDialog(this, "Edit Name:", tableModel.getValueAt(row, 1));
                String email = JOptionPane.showInputDialog(this, "Edit Email:", tableModel.getValueAt(row, 2));
                String phone = JOptionPane.showInputDialog(this, "Edit Phone:", tableModel.getValueAt(row, 3));
                if (name != null && email != null && phone != null) {
                    updateCustomer(id, name, email, phone);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        } else if (e.getSource() == btnDeleteCustomer) {
            int row = customerTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteCustomer(id);
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

    private void createCustomerTable() {
        String sql = "CREATE TABLE IF NOT EXISTS customers (" +
                     "id VARCHAR(255) PRIMARY KEY, " +
                     "name VARCHAR(255), " +
                     "email VARCHAR(255), " +
                     "phone VARCHAR(100))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage());
        }
    }

    private void insertCustomer(String id, String name, String email, String phone) {
        String sql = "INSERT INTO customers (id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, phone);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting customer: " + e.getMessage());
        }
    }

    private void updateCustomer(String id, String name, String email, String phone) {
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, phone);
            pstmt.setString(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating customer: " + e.getMessage());
        }
    }

    private void deleteCustomer(String id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage());
        }
    }

    private void loadFromDatabase() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM customers";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerManagement().setVisible(true));
    }
}
