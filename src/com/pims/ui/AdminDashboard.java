package com.pims.ui;

import com.pims.util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    protected int adminId;
    protected String adminName;
    protected JTabbedPane tabbedPane;

    public AdminDashboard(int adminId, String adminName) {
        this.adminId = adminId;
        this.adminName = adminName;

        setTitle("PIMS - Admin Dashboard | User: " + adminName);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Suppliers", createSuppliersPanel());
        tabbedPane.addTab("Manage Users", createUsersPanel());

        add(tabbedPane);
    }

    protected JPanel createSuppliersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Contact", "Phone", "Email", "Address"}, 0);
        JTable table = new JTable(model);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM suppliers")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("supplier_id"), rs.getString("name"), rs.getString("contact_person"),
                    rs.getString("phone"), rs.getString("email"), rs.getString("address")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    protected JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Username", "Role", "Full Name"}, 0);
        JTable table = new JTable(model);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, username, role, full_name FROM users")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"), rs.getString("username"), rs.getString("role"), rs.getString("full_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}