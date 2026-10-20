package com.pims.ui;

import com.pims.util.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("PIMS - System Login");
        setSize(380, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("  Username:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("  Password:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        btnLogin = new JButton("Login");
        add(new JLabel()); // Placeholder
        add(btnLogin);

        btnLogin.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Username and Password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT user_id, username, role, full_name FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                String fullName = rs.getString("full_name");

                JOptionPane.showMessageDialog(this, "Welcome, " + fullName + " (" + role + ")!");
                this.dispose();

                if ("Admin".equalsIgnoreCase(role)) {
                    // We will create AdminDashboard in the next step
                    JOptionPane.showMessageDialog(this, "Admin Dashboard placeholder. Moving to next commit.");
                } else {
                    // We will create CashierDashboard later
                    JOptionPane.showMessageDialog(this, "Cashier Dashboard placeholder. Moving to next commit.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}