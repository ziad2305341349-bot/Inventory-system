package umsSPEC;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

    JTextField tf1;
    JPasswordField pf1;
    JButton btn1, btn2;
    Connection conn;

    public Login() {
        super("Inventory Management System");

        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(new GradientPanel());

        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false);
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel heading = new JLabel("🔐 Login to Inventory System", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heading.setForeground(Color.WHITE);
        loginPanel.add(heading, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lb1 = new JLabel("Username:");
        lb1.setForeground(Color.WHITE);
        lb1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginPanel.add(lb1, gbc);

        gbc.gridx = 1;
        tf1 = new JTextField();
        tf1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf1.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        tf1.setPreferredSize(new Dimension(150, 30));
        loginPanel.add(tf1, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lb2 = new JLabel("Password:");
        lb2.setForeground(Color.WHITE);
        lb2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginPanel.add(lb2, gbc);

        gbc.gridx = 1;
        pf1 = new JPasswordField();
        pf1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf1.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        pf1.setPreferredSize(new Dimension(150, 30));
        loginPanel.add(pf1, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        btn1 = createStyledButton("Login");
        btn2 = createStyledButton("Exit");

        buttonPanel.add(btn1);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btn2);
        loginPanel.add(buttonPanel, gbc);

        add(loginPanel);
        connectToDatabase();

        btn1.addActionListener(e -> attemptLogin());
        btn2.addActionListener(e -> System.exit(0));
    }

    private void attemptLogin() {
        String uName = tf1.getText().trim();
        String pass = new String(pf1.getPassword());

        if (uName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username can't be empty");
        } else if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password can't be empty");
        } else {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, uName);
                pstmt.setString(2, pass);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        switchScene();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid username or password");
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Login failed: " + e.getMessage());
            }
        }

        tf1.setText("");
        pf1.setText("");
    }

    private void connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to database: " + e.getMessage());
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(44, 62, 80));
        button.setBorder(BorderFactory.createLineBorder(new Color(44, 62, 80), 2));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(200, 230, 250));
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = new Color(58, 123, 213);
            Color color2 = new Color(142, 84, 233);
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public void switchScene() {
        this.setVisible(false);
        new HomePage().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
