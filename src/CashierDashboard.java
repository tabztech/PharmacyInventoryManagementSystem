package com.pims.ui;

import com.pims.util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CashierDashboard extends JFrame {
    private int cashierId;
    private String cashierName;
    private JComboBox<String> cmbMedicines;
    private JTextField txtQuantity;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblTotal;
    private double totalAmount = 0.0;
    private List<Integer> medicineIds = new ArrayList<>();

    public CashierDashboard(int cashierId, String cashierName) {
        this.cashierId = cashierId;
        this.cashierName = cashierName;

        setTitle("PIMS - Cashier POS Terminal | Cashier: " + cashierName);
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Select Medicine:"));
        cmbMedicines = new JComboBox<>();
        loadMedicineDropdown();
        topPanel.add(cmbMedicines);

        topPanel.add(new JLabel("Quantity:"));
        txtQuantity = new JTextField("1", 5);
        topPanel.add(txtQuantity);

        JButton btnAddToCart = new JButton("Add to Cart");
        btnAddToCart.addActionListener(e -> addToCart());
        topPanel.add(btnAddToCart);

        add(topPanel, BorderLayout.NORTH);

        
        cartModel = new DefaultTableModel(new String[]{"Medicine ID", "Name", "Price", "Qty", "Subtotal"}, 0);
        cartTable = new JTable(cartModel);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        lblTotal = new JLabel("Total: R 0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(lblTotal);

        JButton btnCheckout = new JButton("Complete Sale");
        btnCheckout.addActionListener(e -> processCheckout());
        bottomPanel.add(btnCheckout);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadMedicineDropdown() {
        cmbMedicines.removeAllItems();
        medicineIds.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT medicine_id, name, price, quantity_in_stock FROM medicines WHERE quantity_in_stock > 0")) {
            while (rs.next()) {
                medicineIds.add(rs.getInt("medicine_id"));
                cmbMedicines.addItem(rs.getString("name") + " (R" + rs.getBigDecimal("price") + " - Stock: " + rs.getInt("quantity_in_stock") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        int selectedIndex = cmbMedicines.getSelectedIndex();
        if (selectedIndex == -1) return;

        int medId = medicineIds.get(selectedIndex);
        int qty;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive quantity.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, price, quantity_in_stock FROM medicines WHERE medicine_id = ?")) {
            stmt.setInt(1, medId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("quantity_in_stock");

                if (qty > stock) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock available! Current stock: " + stock, "Stock Limit", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double subtotal = price * qty;
                cartModel.addRow(new Object[]{medId, name, price, qty, subtotal});
                totalAmount += subtotal;
                lblTotal.setText(String.format("Total: R %.2f", totalAmount));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Checkout Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 

            String insertSale = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
            PreparedStatement stmtSale = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS);
            stmtSale.setDouble(1, totalAmount);
            stmtSale.setInt(2, cashierId);
            stmtSale.executeUpdate();

            ResultSet rsKeys = stmtSale.getGeneratedKeys();
            int saleId = 0;
            if (rsKeys.next()) {
                saleId = rsKeys.getInt(1);
            }

            String insertItem = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtItem = conn.prepareStatement(insertItem);

            String updateStock = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";
            PreparedStatement stmtStock = conn.prepareStatement(updateStock);

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int medId = (int) cartModel.getValueAt(i, 0);
                double price = (double) cartModel.getValueAt(i, 2);
                int qty = (int) cartModel.getValueAt(i, 3);

                stmtItem.setInt(1, saleId);
                stmtItem.setInt(2, medId);
                stmtItem.setInt(3, qty);
                stmtItem.setDouble(4, price);
                stmtItem.addBatch();

                stmtStock.setInt(1, qty);
                stmtStock.setInt(2, medId);
                stmtStock.addBatch();
            }

            stmtItem.executeBatch();
            stmtStock.executeBatch();

            conn.commit();

            JOptionPane.showMessageDialog(this, "Sale completed successfully! Sale ID: " + saleId, "Success", JOptionPane.INFORMATION_MESSAGE);
            cartModel.setRowCount(0);
            totalAmount = 0.0;
            lblTotal.setText("Total: R 0.00");
            loadMedicineDropdown();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}