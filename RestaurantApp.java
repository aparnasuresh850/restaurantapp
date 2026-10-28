import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class RestaurantApp extends JFrame {
    private ArrayList<Food> menu = new ArrayList<Food>();
    private ArrayList<CartItem> cart = new ArrayList<CartItem>();
    private int nextId = 1;

    private JPanel mainPanel;
    private JComboBox<Food> foodCombo;
    private JTextField customerField, tableField, quantityField;
    private JTable cartTable, adminTable;
    private DefaultTableModel cartModel, adminModel;
    private JLabel totalLabel;
    private JTextField foodNameField, categoryField, priceField;
    private String customerName = "", tableNumber = "";

    public RestaurantApp() {
        setTitle("Restaurant Self-Ordering System");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        loadMenu();
        showRoleSelection();
    }

    private void loadMenu() {
        menu.add(new Food(nextId++, "Burger", "Fast Food", 120));
        menu.add(new Food(nextId++, "Pizza", "Fast Food", 250));
        menu.add(new Food(nextId++, "Sandwich", "Snacks", 90));
        menu.add(new Food(nextId++, "Pasta", "Main Course", 180));
        menu.add(new Food(nextId++, "Lime Juice", "Drinks", 40));
        menu.add(new Food(nextId++, "Coffee", "Drinks", 60));
    }

    private void setMainPanel(JPanel panel) {
        mainPanel = panel;
        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private void showRoleSelection() {
        JPanel panel = new JPanel(new GridBagLayout());
        JPanel box = new JPanel(new GridLayout(0, 1, 10, 10));
        box.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        JLabel title = new JLabel("RESTAURANT SELF-ORDERING SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        box.add(title);
        box.add(new JLabel("Choose an option", SwingConstants.CENTER));
        JButton userButton = new JButton("User / Customer");
        JButton adminButton = new JButton("Admin");
        box.add(userButton);
        box.add(adminButton);
        panel.add(box);
        userButton.addActionListener(e -> showCustomerScreen());
        adminButton.addActionListener(e -> adminLogin());
        setMainPanel(panel);
    }

    private void adminLogin() {
        JPasswordField passwordField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(this, passwordField,
                "Enter Admin Password", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (new String(passwordField.getPassword()).equals("admin123")) {
                showAdminScreen();
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect password.");
            }
        }
    }

    private void showCustomerScreen() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel heading = new JLabel("Customer Food Ordering", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 20));

        customerField = new JTextField(customerName);
        tableField = new JTextField(tableNumber);
        foodCombo = new JComboBox<Food>();
        refreshFoodCombo();
        quantityField = new JTextField("1");

        JPanel fields = new JPanel(new GridLayout(2, 4, 8, 8));
        fields.add(new JLabel("Customer Name:"));
        fields.add(customerField);
        fields.add(new JLabel("Table Number:"));
        fields.add(tableField);
        fields.add(new JLabel("Food Item:"));
        fields.add(foodCombo);
        fields.add(new JLabel("Quantity:"));
        fields.add(quantityField);

        JButton addButton = new JButton("Add to Cart");
        JPanel north = new JPanel(new BorderLayout(5, 8));
        north.add(heading, BorderLayout.NORTH);
        JPanel inputArea = new JPanel(new BorderLayout(5, 5));
        inputArea.add(fields, BorderLayout.CENTER);
        inputArea.add(addButton, BorderLayout.SOUTH);
        north.add(inputArea, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);

        cartModel = new DefaultTableModel(new Object[]{"No.", "Food Item", "Price", "Quantity", "Subtotal"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartModel);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        totalLabel = new JLabel("Total: Rs. 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton updateButton = new JButton("Update Quantity");
        JButton removeButton = new JButton("Remove Selected");
        JButton clearButton = new JButton("Clear Cart");
        JButton confirmButton = new JButton("Confirm Order");
        JButton newButton = new JButton("New Order");
        JButton backButton = new JButton("Back to Role Selection");

        JPanel buttons = new JPanel(new GridLayout(2, 3, 6, 6));
        buttons.add(updateButton); buttons.add(removeButton); buttons.add(clearButton);
        buttons.add(confirmButton); buttons.add(newButton); buttons.add(backButton);
        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.add(totalLabel, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addToCart());
        updateButton.addActionListener(e -> updateCartQuantity());
        removeButton.addActionListener(e -> removeSelectedCartItem());
        clearButton.addActionListener(e -> clearCart());
        confirmButton.addActionListener(e -> confirmOrder());
        newButton.addActionListener(e -> startNewOrder());
        backButton.addActionListener(e -> showRoleSelection());
        refreshCartTable();
        setMainPanel(panel);
    }

    private void refreshFoodCombo() {
        if (foodCombo == null) return;
        foodCombo.removeAllItems();
        for (Food food : menu) foodCombo.addItem(food);
    }

    private void addToCart() {
        customerName = customerField.getText().trim();
        tableNumber = tableField.getText().trim();
        if (customerName.isEmpty() || tableNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter customer name and table number.");
            return;
        }
        Food selected = (Food) foodCombo.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "No food items available.");
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity greater than zero.");
            return;
        }
        CartItem existing = findCartItem(selected);
        if (existing == null) cart.add(new CartItem(selected, quantity));
        else existing.setQuantity(existing.getQuantity() + quantity);
        refreshCartTable();
        quantityField.setText("1");
    }

    private CartItem findCartItem(Food food) {
        for (CartItem item : cart)
            if (item.getFood().getId() == food.getId()) return item;
        return null;
    }

    private void updateCartQuantity() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a cart item first.");
            return;
        }
        String value = JOptionPane.showInputDialog(this, "Enter new quantity:");
        if (value == null) return;
        try {
            int quantity = Integer.parseInt(value.trim());
            if (quantity <= 0) throw new NumberFormatException();
            cart.get(row).setQuantity(quantity);
            refreshCartTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a positive whole number.");
        }
    }

    private void removeSelectedCartItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a cart item first.");
            return;
        }
        cart.remove(row);
        refreshCartTable();
    }

    private void clearCart() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is already empty.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Clear all cart items?",
                "Clear Cart", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            cart.clear();
            refreshCartTable();
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cart) total += item.getSubtotal();
        return total;
    }

    private void refreshCartTable() {
        if (cartModel == null) return;
        cartModel.setRowCount(0);
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            cartModel.addRow(new Object[]{i + 1, item.getFood().getName(),
                    String.format("%.2f", item.getFood().getPrice()), item.getQuantity(),
                    String.format("%.2f", item.getSubtotal())});
        }
        if (totalLabel != null)
            totalLabel.setText("Total: Rs. " + String.format("%.2f", calculateTotal()));
    }

    private void confirmOrder() {
        customerName = customerField.getText().trim();
        tableNumber = tableField.getText().trim();
        if (customerName.isEmpty() || tableNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter customer name and table number.");
            return;
        }
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }
        StringBuilder bill = new StringBuilder("ORDER CONFIRMATION\n\n");
        bill.append("Customer: ").append(customerName).append("\n");
        bill.append("Table: ").append(tableNumber).append("\n\n");
        for (CartItem item : cart) {
            bill.append(item.getFood().getName()).append(" x ")
                .append(item.getQuantity()).append(" = Rs. ")
                .append(String.format("%.2f", item.getSubtotal())).append("\n");
        }
        bill.append("\nTOTAL: Rs. ").append(String.format("%.2f", calculateTotal()));
        JOptionPane.showMessageDialog(this, bill.toString(), "Final Bill",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void startNewOrder() {
        cart.clear();
        customerName = "";
        tableNumber = "";
        showCustomerScreen();
    }

    private void showAdminScreen() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel heading = new JLabel("Admin - Manage Food Menu", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(heading, BorderLayout.NORTH);

        adminModel = new DefaultTableModel(new Object[]{"ID", "Food Name", "Category", "Price"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        adminTable = new JTable(adminModel);
        panel.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        foodNameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        JPanel fields = new JPanel(new GridLayout(2, 3, 8, 5));
        fields.add(new JLabel("Food Name:"));
        fields.add(new JLabel("Category:"));
        fields.add(new JLabel("Price (Rs.):"));
        fields.add(foodNameField);
        fields.add(categoryField);
        fields.add(priceField);

        JButton addButton = new JButton("Add Food");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton backButton = new JButton("Back to Role Selection");
        JPanel buttons = new JPanel(new GridLayout(1, 4, 6, 6));
        buttons.add(addButton); buttons.add(updateButton);
        buttons.add(deleteButton); buttons.add(backButton);
        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.add(fields, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        adminTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillAdminFields();
        });
        addButton.addActionListener(e -> addFood());
        updateButton.addActionListener(e -> updateFood());
        deleteButton.addActionListener(e -> deleteFood());
        backButton.addActionListener(e -> showRoleSelection());
        refreshAdminTable();
        setMainPanel(panel);
    }

    private void fillAdminFields() {
        int row = adminTable.getSelectedRow();
        if (row >= 0 && row < menu.size()) {
            Food food = menu.get(row);
            foodNameField.setText(food.getName());
            categoryField.setText(food.getCategory());
            priceField.setText(String.valueOf(food.getPrice()));
        }
    }

    private boolean validAdminFields() {
        if (foodNameField.getText().trim().isEmpty() || categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter food name and category.");
            return false;
        }
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid price greater than zero.");
            return false;
        }
        return true;
    }

    private void addFood() {
        if (!validAdminFields()) return;
        menu.add(new Food(nextId++, foodNameField.getText().trim(),
                categoryField.getText().trim(), Double.parseDouble(priceField.getText().trim())));
        refreshAdminTable();
        refreshFoodCombo();
        clearAdminFields();
        JOptionPane.showMessageDialog(this, "Food item added.");
    }

    private void updateFood() {
        int row = adminTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a food item to update.");
            return;
        }
        if (!validAdminFields()) return;
        Food food = menu.get(row);
        food.setName(foodNameField.getText().trim());
        food.setCategory(categoryField.getText().trim());
        food.setPrice(Double.parseDouble(priceField.getText().trim()));
        refreshAdminTable();
        refreshFoodCombo();
        refreshCartTable();
        clearAdminFields();
        JOptionPane.showMessageDialog(this, "Food item updated.");
    }

    private void deleteFood() {
        int row = adminTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a food item to delete.");
            return;
        }
        Food food = menu.get(row);
        if (JOptionPane.showConfirmDialog(this, "Delete " + food.getName() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            menu.remove(food);
            cart.removeIf(item -> item.getFood().getId() == food.getId());
            refreshAdminTable();
            refreshFoodCombo();
            refreshCartTable();
            clearAdminFields();
            JOptionPane.showMessageDialog(this, "Food item deleted.");
        }
    }

    private void refreshAdminTable() {
        if (adminModel == null) return;
        adminModel.setRowCount(0);
        for (Food food : menu)
            adminModel.addRow(new Object[]{food.getId(), food.getName(),
                    food.getCategory(), String.format("%.2f", food.getPrice())});
    }

    private void clearAdminFields() {
        foodNameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        adminTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantApp app = new RestaurantApp();
            app.setVisible(true);
        });
    }
}
