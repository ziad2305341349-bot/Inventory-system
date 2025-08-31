package umsSPEC;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

public class CategoryManagement extends JFrame implements ActionListener {

    JLabel titleLabel;
    JButton btnAdd, btnEdit, btnDelete, btnBack, btnSave;
    JTable categoryTable;
    DefaultTableModel tableModel;

    Connection conn;

    public CategoryManagement() {
        setTitle("SPEC - Category Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        titleLabel = new JLabel("\uD83D\uDCC2 Category Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(40, 75, 99));
        titleLabel.setBounds(30, 20, 500, 40);
        add(titleLabel);

        btnAdd = createButton("\u2795 Add Category", 30, 90);
        btnEdit = createButton("\u270F\uFE0F Edit Selected", 30, 150);
        btnDelete = createButton("\uD83D\uDDD1\uFE0F Delete Selected", 30, 210);
        btnBack = createButton("\uD83D\uDD19 Back", 30, 270);
        btnSave = createButton("\uD83D\uDCBE Refresh", 30, 330);

        add(btnAdd);
        add(btnEdit);
        add(btnDelete);
        add(btnBack);
        add(btnSave);

        String[] columnNames = {"Category ID", "Category Name", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0);
        categoryTable = new JTable(tableModel);
        categoryTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        categoryTable.setRowHeight(25);
        categoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBounds(240, 90, 720, 420);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane);

        connectToDatabase();
        createCategoryTable();
        loadFromDatabase();

        btnSave.addActionListener(e -> loadFromDatabase());
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

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(60, 120, 180));
            }
        });

        return button;
    }

    private void connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void createCategoryTable() {
        String sql = "CREATE TABLE IF NOT EXISTS categories (" +
                     "id VARCHAR(255) PRIMARY KEY, " +
                     "name VARCHAR(255), " +
                     "description VARCHAR(255))";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating table: " + e.getMessage());
        }
    }

    private void loadFromDatabase() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM categories";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd) {
            String id = JOptionPane.showInputDialog(this, "Enter Category ID:");
            String name = JOptionPane.showInputDialog(this, "Enter Category Name:");
            String desc = JOptionPane.showInputDialog(this, "Enter Description:");
            if (id != null && name != null && desc != null) {
                insertCategory(id, name, desc);
                loadFromDatabase();
            }
        } else if (e.getSource() == btnEdit) {
            int row = categoryTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                String name = JOptionPane.showInputDialog(this, "Edit Category Name:", tableModel.getValueAt(row, 1));
                String desc = JOptionPane.showInputDialog(this, "Edit Description:", tableModel.getValueAt(row, 2));
                if (name != null && desc != null) {
                    updateCategory(id, name, desc);
                    loadFromDatabase();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            }
        } else if (e.getSource() == btnDelete) {
            int row = categoryTable.getSelectedRow();
            if (row != -1) {
                String id = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Delete this category?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteCategory(id);
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

    private void insertCategory(String id, String name, String desc) {
        String sql = "INSERT INTO categories (id, name, description) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, desc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error inserting category: " + e.getMessage());
        }
    }

    private void updateCategory(String id, String name, String desc) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, desc);
            pstmt.setString(3, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating category: " + e.getMessage());
        }
    }

    private void deleteCategory(String id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CategoryManagement().setVisible(true));
    }
}
