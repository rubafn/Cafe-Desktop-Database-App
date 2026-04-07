package application;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.*;

public class StockUI {
    private DatabaseConnection db;
    private TableView<Map<String, Object>> tableView;
    private Label statusLabel;
    private ComboBox<String> warehouseFilter;

    public StockUI(DatabaseConnection db) {
        this.db = db;
    }

    public VBox createStockPane() {
        warehouseFilter = new ComboBox<>();
        loadWarehouseFilter();
        warehouseFilter.setOnAction(e -> loadStock());

        Button addBtn = createButton("Add Stock", "#27ae60");
        Button updateBtn = createButton("Update", "#3498db");
        Button deleteBtn = createButton("Delete", "#e74c3c");
        Button refreshBtn = createButton("Refresh", "#95a5a6");
        Button lowStockBtn = createButton("Low Stock Alert", "#e67e22");

        addBtn.setOnAction(e -> openStockForm(null));
        updateBtn.setOnAction(e -> {
            Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openStockForm(selected);
            } else {
                showAlert("Selection Error", "Please select a stock item to update.");
            }
        });
        deleteBtn.setOnAction(e -> deleteStock());
        refreshBtn.setOnAction(e -> {
            loadWarehouseFilter();
            loadStock();
        });
        lowStockBtn.setOnAction(e -> showLowStockAlert());

        HBox controls = new HBox(10, new Label("Warehouse:"), warehouseFilter,
                                 addBtn, updateBtn, deleteBtn, refreshBtn, lowStockBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        VBox layout = new VBox(15, controls, tableView, statusLabel);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        loadStock();
        return layout;
    }

    private void loadWarehouseFilter() {
        try {
            String sql = "SELECT WarehouseID, Location FROM Warehouse ORDER BY WarehouseID";
            List<Map<String, Object>> warehouses = db.executeQuery(sql);

            warehouseFilter.getItems().clear();
            warehouseFilter.getItems().add("All Warehouses");

            for (Map<String, Object> wh : warehouses) {
                String item = wh.get("WarehouseID") + " - " + wh.get("Location");
                warehouseFilter.getItems().add(item);
            }

            if (warehouseFilter.getValue() == null) {
                warehouseFilter.setValue("All Warehouses");
            }

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load warehouses: " + e.getMessage());
        }
    }

    private void loadStock() {
        try {
            String selectedWarehouse = warehouseFilter.getValue();
            String sql;
            List<Map<String, Object>> data;

            if (selectedWarehouse == null || selectedWarehouse.equals("All Warehouses")) {
                sql = 
                  "  SELECT (SELECT Location FROM Warehouse WHERE Warehouse.WarehouseID = Stock.WarehouseID) as Warehouse, "+
                         "  (SELECT Name FROM Item WHERE Item.ItemID = Stock.ItemID) as Item, "+
                         "  (SELECT Category FROM Item WHERE Item.ItemID = Stock.ItemID) as Category, "+
                          " Quantity, "+
                          " (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Stock.ItemID) as Unit, "+
                          " ExpirationDate, "+
                          " WarehouseID, "+
                          " ItemID "+
                   " FROM Stock "+
                   " ORDER BY Warehouse, Item "
                ;
                data = db.executeQuery(sql);
            } else {
                int warehouseID = Integer.parseInt(selectedWarehouse.split(" - ")[0]);
                sql = 
                  "  SELECT (SELECT Location FROM Warehouse WHERE Warehouse.WarehouseID = Stock.WarehouseID) as Warehouse, "+
                         "  (SELECT Name FROM Item WHERE Item.ItemID = Stock.ItemID) as Item, "+
                         "  (SELECT Category FROM Item WHERE Item.ItemID = Stock.ItemID) as Category, "+
                        "   Quantity, "+
                         "  (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Stock.ItemID) as Unit, "+
                          " ExpirationDate, "+
                          " WarehouseID, "+
                          " ItemID "+
                   " FROM Stock "+
                   " WHERE WarehouseID = ? "+
                   " ORDER BY Item "
               ;
                data = db.executeQuery(sql, warehouseID);
            }

            boolean columnsChanged = tableView.getColumns().isEmpty() ||
                                    (!data.isEmpty() && tableView.getColumns().size() != (data.get(0).keySet().size() - 2));

            if (columnsChanged) {
                tableView.getColumns().clear();
                if (!data.isEmpty()) {
                    for (String key : data.get(0).keySet()) {
                        if (key.equals("WarehouseID") || key.equals("ItemID")) {
                            continue;
                        }
                        TableColumn<Map<String, Object>, Object> col = new TableColumn<>(key);
                        col.setCellValueFactory(r ->
                                new SimpleObjectProperty<>(r.getValue().get(key)));
                        col.setMinWidth(120);
                        col.setPrefWidth(150);
                        col.setMaxWidth(300);
                        col.setResizable(true);
                        col.setStyle("-fx-alignment: CENTER-LEFT;");
                        tableView.getColumns().add(col);
                    }
                }
            }

            tableView.getItems().clear();
            tableView.getItems().addAll(data);

            statusLabel.setText("Total Stock Items: " + data.size());

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load stock: " + e.getMessage());
        }
    }

    private void openStockForm(Map<String, Object> stockData) {
        Stage formStage = new Stage();
        formStage.setTitle(stockData == null ? "Add Stock Item" : "Update Stock");

        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(stockData == null ? "➕ Add Stock Item" : "✏️ Update Stock");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #2c3e50; -fx-padding: 25 30 20 30;");

        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #e1e8ed;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: #f8f9fa;");

        String labelStyle = "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #34495e;";
        String fieldStyle = "-fx-font-size: 13px; -fx-pref-width: 450; -fx-padding: 10;";

        ComboBox<String> warehouseCombo = new ComboBox<>();
        warehouseCombo.setPromptText("Select warehouse");
        warehouseCombo.setStyle(fieldStyle);
        warehouseCombo.setPrefWidth(450);

        ComboBox<String> itemCombo = new ComboBox<>();
        itemCombo.setPromptText("Select ingredient");
        itemCombo.setStyle(fieldStyle);
        itemCombo.setPrefWidth(450);

        TextField quantityField = new TextField();
        quantityField.setPromptText("0.00");
        quantityField.setStyle(fieldStyle);

        Label unitDisplayLabel = new Label("");
        unitDisplayLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        DatePicker expirationPicker = new DatePicker();
        expirationPicker.setPromptText("Select expiration date (optional)");
        expirationPicker.setStyle(fieldStyle);
        expirationPicker.setPrefWidth(450);

        try {
            String sql = "SELECT WarehouseID, Location FROM Warehouse ORDER BY Location";
            List<Map<String, Object>> warehouses = db.executeQuery(sql);
            for (Map<String, Object> wh : warehouses) {
                warehouseCombo.getItems().add(wh.get("WarehouseID") + " - " + wh.get("Location"));
            }
        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load warehouses: " + e.getMessage());
        }

        try {
            String sql = "SELECT ItemID, Name, (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as Unit FROM Item WHERE ItemID IN (SELECT ItemID FROM Ingredient) ORDER BY Name";
            List<Map<String, Object>> items = db.executeQuery(sql);
            for (Map<String, Object> item : items) {
                itemCombo.getItems().add(item.get("ItemID") + " - " + item.get("Name") + " (" + item.get("Unit") + ")");
            }
        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load ingredients: " + e.getMessage());
        }

        if (stockData != null) {
            warehouseCombo.setValue(stockData.get("WarehouseID") + " - " + stockData.get("Warehouse"));
            itemCombo.setValue(stockData.get("ItemID") + " - " + stockData.get("Item"));
            quantityField.setText(stockData.get("Quantity").toString());
            if (stockData.get("Unit") != null) {
                unitDisplayLabel.setText("Unit: " + stockData.get("Unit").toString());
            }
            if (stockData.get("ExpirationDate") != null) {
                expirationPicker.setValue(java.time.LocalDate.parse(stockData.get("ExpirationDate").toString()));
            }

            warehouseCombo.setDisable(true);
            itemCombo.setDisable(true);
            warehouseCombo.setStyle(fieldStyle + "-fx-opacity: 0.6;");
            itemCombo.setStyle(fieldStyle + "-fx-opacity: 0.6;");
        }

        int row = 0;

        Label warehouseLabel = new Label("Warehouse: *");
        warehouseLabel.setStyle(labelStyle);
        grid.add(warehouseLabel, 0, row);
        grid.add(warehouseCombo, 1, row++);

        Label itemLabel = new Label("Ingredient: *");
        itemLabel.setStyle(labelStyle);
        grid.add(itemLabel, 0, row);
        grid.add(itemCombo, 1, row++);

        Label quantityLabel = new Label("Quantity: *");
        quantityLabel.setStyle(labelStyle);
        grid.add(quantityLabel, 0, row);
        grid.add(quantityField, 1, row++);

        if (stockData != null && !unitDisplayLabel.getText().isEmpty()) {
            grid.add(unitDisplayLabel, 1, row++);
        }

        Label expirationLabel = new Label("Expiration Date:");
        expirationLabel.setStyle(labelStyle);
        grid.add(expirationLabel, 0, row);
        grid.add(expirationPicker, 1, row++);

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #e1e8ed;");

        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 30, 25, 30));
        buttonBar.setStyle("-fx-background-color: white;");

        Button saveBtn = createButton(stockData == null ? "✓ Save" : "✓ Update", "#27ae60");
        Button cancelBtn = createButton("✕ Cancel", "#95a5a6");

        saveBtn.setPrefWidth(120);
        cancelBtn.setPrefWidth(120);

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        saveBtn.setOnAction(e -> {
            try {
                if (warehouseCombo.getValue() == null || itemCombo.getValue() == null ||
                    quantityField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Warehouse, Ingredient, and Quantity are required fields.");
                    return;
                }

                int warehouseID = Integer.parseInt(warehouseCombo.getValue().split(" - ")[0]);
                int itemID = Integer.parseInt(itemCombo.getValue().split(" - ")[0]);
                double quantity = Double.parseDouble(quantityField.getText().trim());
                String expirationDate = expirationPicker.getValue() != null ?
                                        expirationPicker.getValue().toString() : null;

                if (stockData == null) {
                    String sql = "INSERT INTO Stock (WarehouseID, ItemID, Quantity, ExpirationDate) VALUES (?, ?, ?, ?)";
                    db.executeUpdate(sql, warehouseID, itemID, quantity, expirationDate);
                    showAlert("Success", "✓ Stock added successfully!");
                } else {
                    String sql = "UPDATE Stock SET Quantity=?, ExpirationDate=? WHERE WarehouseID=? AND ItemID=?";
                    db.executeUpdate(sql, quantity, expirationDate, warehouseID, itemID);
                    showAlert("Success", "✓ Stock updated successfully!");
                }

                loadStock();
                formStage.close();

            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Invalid quantity format. Please enter a valid number.");
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to save stock: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> formStage.close());

        mainContainer.getChildren().addAll(titleLabel, separator1, grid, separator2, buttonBar);

        Scene scene = new Scene(mainContainer, 800, 550);
        formStage.setScene(scene);
        formStage.setResizable(false);
        formStage.show();
    }

    private void deleteStock() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a stock item to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Stock");
        confirm.setContentText("Are you sure you want to delete this stock item?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                int warehouseID = Integer.parseInt(selected.get("WarehouseID").toString());
                int itemID = Integer.parseInt(selected.get("ItemID").toString());
                String sql = "DELETE FROM Stock WHERE WarehouseID=? AND ItemID=?";
                db.executeUpdate(sql, warehouseID, itemID);
                showAlert("Success", "Stock deleted successfully!");
                loadStock();
            } catch (SQLException e) {
                showAlert("Delete Error", "Failed to delete stock: " + e.getMessage());
            }
        }
    }

    private void showLowStockAlert() {
        Stage alertStage = new Stage();
        alertStage.setTitle("Low Stock Alert");

        TableView<Map<String, Object>> alertTable = new TableView<>();
        alertTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            String sql =
              "  SELECT (SELECT Location FROM Warehouse WHERE Warehouse.WarehouseID = Stock.WarehouseID) as Warehouse, "+
                      " (SELECT Name FROM Item WHERE Item.ItemID = Stock.ItemID) as Item, "+
                      " Quantity, "+
                     " (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Stock.ItemID) as Unit,  "+
                     "  ExpirationDate "+
               " FROM Stock "+
               " WHERE Quantity < 10 "+
              "  ORDER BY Quantity ASC "
            ;
            List<Map<String, Object>> data = db.executeQuery(sql);

            if (!data.isEmpty()) {
                for (String key : data.get(0).keySet()) {
                    TableColumn<Map<String, Object>, Object> col = new TableColumn<>(key);
                    col.setCellValueFactory(r ->
                            new SimpleObjectProperty<>(r.getValue().get(key)));
                    col.setMinWidth(120);
                    col.setPrefWidth(150);
                    col.setStyle("-fx-alignment: CENTER-LEFT;");
                    alertTable.getColumns().add(col);
                }
                alertTable.getItems().addAll(data);
            }

            Label status = new Label("Low Stock Items: " + data.size());
            status.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

            VBox root = new VBox(15, status, alertTable);
            root.setPadding(new Insets(15));
            VBox.setVgrow(alertTable, Priority.ALWAYS);

            alertStage.setScene(new Scene(root, 700, 500));
            alertStage.show();

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to check stock levels: " + e.getMessage());
        }
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        return btn;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

