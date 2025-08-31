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

public class SupplierManagement extends JFrame implements ActionListener {

    JLabel titleLabel;
    JButton btnAdd, btnEdit, btnDelete, btnBack, btnRefresh;
    JTable supplierTable;
    DefaultTableModel tableModel;

    Connection conn;

    public SupplierManagement() {
        setTitle("SPEC - Supplier Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        titleLabel = new JLabel("🚚 Supplier Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(40, 75, 99));
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        btnAdd = createButton("➕ Add Supplier", 30, 90);
        btnEdit = createButton("✏️ Edit Selected", 30, 150);
        btnDelete = createButton("🗑️ Delete Selected", 30, 210);
        btnBack = createButton("🔙 Back", 30, 270);
        btnRefresh = createButton("💾 Refresh", 30, 330);

        add(btnAdd);
        add(btnEdit);
        add(btnDelete);
        add(btnBack);
        add(btnRefresh);

        String[] columnNames = {"Supplier ID", "Supplier Name", "Contact Info", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0);
        supplierTable = new JTable(tableModel);
        supplierTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        supplierTable.setRowHeight(25);
        supplierTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(supplierTable);
        scrollPane.setBounds(240, 90, 720, 420);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane);

        connectToDatabase();
        createSupplierTable();
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
            String id = JOptionPane.showInputDialog(this, "Enter Supplier ID:");
            String name = JOptionPane.showInputDialog(this, "Enter Supplier Name:");
            String contact = JOptionPane.showInputDialog(this, "Enter Contact Info:");
            String address = JOptionPane.showInputDialog(this, "Enter Address:");
            if (id != null && name != null && contact != null && address != null) {
                insertSupplier(id, name, contact, address);
                loadFromDatabase();
            }
        } else if (e.getSource() == btnEdit) {
            int row = supplierTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                String name = JOptionPane.showInputDialog(this, "Edit Supplier Name:", tableModel.getValueAt(row, 1));
                String contact = JOptionPane.showInputDialog(this, "Edit Contact Info:", tableModel.getValueAt(row, 2));
                String address = JOptionPane.showInputDialog(this, "Edit Address:", tableModel.getValueAt(row, 3));
                if (name != null && contact != null && address != null) {
                    updateSupplier(id, name, contact, address);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        } else if (e.getSource() == btnDelete) {
            int row = supplierTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this supplier?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteSupplier(id);
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

    private void createSupplierTable() {
        String sql = "CREATE TABLE IF NOT EXISTS suppliers (" +
                     "id VARCHAR(255) PRIMARY KEY, " +
                     "name VARCHAR(255), " +
                     "contact VARCHAR(255), " +
                     "address VARCHAR(255))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage());
        }
    }

    private void insertSupplier(String id, String name, String contact, String address) {
        String sql = "INSERT INTO suppliers (id, name, contact, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, contact);
            pstmt.setString(4, address);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting supplier: " + e.getMessage());
        }
    }

    private void updateSupplier(String id, String name, String contact, String address) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, address = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, address);
            pstmt.setString(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating supplier: " + e.getMessage());
        }
    }

    private void deleteSupplier(String id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting supplier: " + e.getMessage());
        }
    }

    private void loadFromDatabase() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM suppliers";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("contact"),
                    rs.getString("address")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SupplierManagement().setVisible(true));
    }
}
