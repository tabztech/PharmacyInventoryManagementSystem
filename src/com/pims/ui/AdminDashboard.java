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
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Medicines", createMedicinesPanel());
        tabbedPane.addTab("Manage Suppliers", createSuppliersPanel());
        tabbedPane.addTab("Manage Users", createUsersPanel());
        tabbedPane.addTab("Sales Reports", createSalesReportPanel());
        tabbedPane.addTab("Inventory Alerts & Expiry", createAlertsPanel());

        add(tabbedPane);
    }

    protected JPanel createMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Company", "Type", "Price", "Stock", "Reorder Level", "Expiry Date", "Supplier ID"}, 0);
        JTable table = new JTable(model);

        Runnable loadMedicines = () -> {
            model.setRowCount(0);
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM medicines")) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("medicine_id"), rs.getString("name"), rs.getString("company"),
                        rs.getString("medicine_type"), rs.getBigDecimal("price"),
                        rs.getInt("quantity_in_stock"), rs.getInt("reorder_level"),
                        rs.getDate("expiry_date"), rs.getInt("supplier_id")
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        loadMedicines.run();

        JPanel btnPanel = new JPanel();
        JButton btnRefresh = new JButton("Refresh Stock");
        btnRefresh.addActionListener(e -> loadMedicines.run());
        btnPanel.add(btnRefresh);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
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

    protected JPanel createSalesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Sale ID", "Date/Time", "Total Amount (R)", "Cashier Name"}, 0);
        JTable table = new JTable(model);

        JLabel lblRevenue = new JLabel("Total Revenue: R 0.00  ");
        lblRevenue.setFont(new Font("Arial", Font.BOLD, 14));

        Runnable loadSales = () -> {
            model.setRowCount(0);
            double revenue = 0.0;
            String sql = "SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name " +
                         "FROM sales s LEFT JOIN users u ON s.user_id = u.user_id " +
                         "ORDER BY s.sale_date DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    double amt = rs.getDouble("total_amount");
                    revenue += amt;
                    model.addRow(new Object[]{
                        rs.getInt("sale_id"), rs.getTimestamp("sale_date"),
                        String.format("%.2f", amt), rs.getString("full_name")
                    });
                }
                lblRevenue.setText(String.format("Total Revenue Generated: R %.2f  ", revenue));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };

        loadSales.run();

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh Sales");
        btnRefresh.addActionListener(e -> loadSales.run());
        topBar.add(lblRevenue);
        topBar.add(btnRefresh);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    protected JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));

        
        JPanel lowStockPanel = new JPanel(new BorderLayout());
        lowStockPanel.setBorder(BorderFactory.createTitledBorder("Low Stock Warnings"));
        DefaultTableModel lowModel = new DefaultTableModel(new String[]{"Medicine", "Stock", "Reorder Level"}, 0);
        JTable lowTable = new JTable(lowModel);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, quantity_in_stock, reorder_level FROM medicines WHERE quantity_in_stock <= reorder_level")) {
            while (rs.next()) {
                lowModel.addRow(new Object[]{
                    rs.getString("name"), rs.getInt("quantity_in_stock"), rs.getInt("reorder_level")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lowStockPanel.add(new JScrollPane(lowTable), BorderLayout.CENTER);

        
        JPanel expiryPanel = new JPanel(new BorderLayout());
        expiryPanel.setBorder(BorderFactory.createTitledBorder("30-Day Expiry Risk"));
        DefaultTableModel expModel = new DefaultTableModel(new String[]{"Medicine", "Expiry Date", "Stock"}, 0);
        JTable expTable = new JTable(expModel);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, expiry_date, quantity_in_stock FROM medicines WHERE expiry_date <= DATE_ADD(CURDATE(), INTERVAL 1 MONTH)")) {
            while (rs.next()) {
                expModel.addRow(new Object[]{
                    rs.getString("name"), rs.getDate("expiry_date"), rs.getInt("quantity_in_stock")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        expiryPanel.add(new JScrollPane(expTable), BorderLayout.CENTER);

        panel.add(lowStockPanel);
        panel.add(expiryPanel);
        return panel;
    }
}