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


public class WarehouseUI {
    private DatabaseConnection db;
    private TableView<Map<String, Object>> tableView;
    private Label statusLabel;

    public WarehouseUI(DatabaseConnection db) {
        this.db = db;
    }

    public VBox createWarehousePane() {
        Button addBtn = createButton("Add Warehouse", "#27ae60");
        Button updateBtn = createButton("Update", "#3498db");
        Button deleteBtn = createButton("Delete", "#e74c3c");
        Button refreshBtn = createButton("Refresh", "#95a5a6");
        Button viewStockBtn = createButton("View Stock", "#f39c12");

        addBtn.setOnAction(e -> openWarehouseForm(null));
        updateBtn.setOnAction(e -> {
            Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openWarehouseForm(selected);
            } else {
                showAlert("Selection Error", "Please select a warehouse to update.");
            }
        });
        deleteBtn.setOnAction(e -> deleteWarehouse());
        refreshBtn.setOnAction(e -> loadWarehouses());
        viewStockBtn.setOnAction(e -> viewWarehouseStock());

        HBox controls = new HBox(10, addBtn, updateBtn, deleteBtn, refreshBtn, viewStockBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        VBox layout = new VBox(15, controls, tableView, statusLabel);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        loadWarehouses();
        return layout;
    }

    private void loadWarehouses() {
        try {
            String sql = "SELECT WarehouseID, Type, Location FROM Warehouse ORDER BY WarehouseID";
            List<Map<String, Object>> data = db.executeQuery(sql);

            boolean columnsChanged = tableView.getColumns().isEmpty() ||
                                    (!data.isEmpty() && tableView.getColumns().size() != data.get(0).keySet().size());

            if (columnsChanged) {
                tableView.getColumns().clear();
                if (!data.isEmpty()) {
                    for (String key : data.get(0).keySet()) {
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

            statusLabel.setText("Total Warehouses: " + data.size());

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load warehouses: " + e.getMessage());
        }
    }

    private void openWarehouseForm(Map<String, Object> warehouseData) {
        Stage formStage = new Stage();
        formStage.setTitle(warehouseData == null ? "Add New Warehouse" : "Update Warehouse");

        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(warehouseData == null ? "➕ Add New Warehouse" : "✏️ Edit Warehouse");
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

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("in-store", "external");
        typeCombo.setPromptText("Select warehouse type");
        typeCombo.setStyle(fieldStyle);
        typeCombo.setPrefWidth(450);

        TextField locationField = new TextField();
        locationField.setPromptText("Enter warehouse location");
        locationField.setStyle(fieldStyle);

        if (warehouseData != null) {
            typeCombo.setValue(warehouseData.get("Type").toString());
            locationField.setText(warehouseData.get("Location").toString());
        } else {
            typeCombo.setValue("external");
        }

        Label typeLabel = new Label("Warehouse Type: *");
        typeLabel.setStyle(labelStyle);
        grid.add(typeLabel, 0, 0);
        grid.add(typeCombo, 1, 0);

        Label locationLabel = new Label("Location: *");
        locationLabel.setStyle(labelStyle);
        grid.add(locationLabel, 0, 1);
        grid.add(locationField, 1, 1);

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #e1e8ed;");

        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 30, 25, 30));
        buttonBar.setStyle("-fx-background-color: white;");

        Button saveBtn = createButton(warehouseData == null ? "✓ Save" : "✓ Update", "#27ae60");
        Button cancelBtn = createButton("✕ Cancel", "#95a5a6");

        saveBtn.setPrefWidth(120);
        cancelBtn.setPrefWidth(120);

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        saveBtn.setOnAction(e -> {
            try {
                String type = typeCombo.getValue();
                String location = locationField.getText().trim();

                if (type == null || location.isEmpty()) {
                    showAlert("Validation Error", "All fields are required.");
                    return;
                }

                if (warehouseData == null) {
                    String sql = "INSERT INTO Warehouse (Type, Location) VALUES (?, ?)";
                    db.executeUpdate(sql, type, location);
                    showAlert("Success", "✓ Warehouse added successfully!");
                } else {
                    int warehouseID = Integer.parseInt(warehouseData.get("WarehouseID").toString());
                    String sql = "UPDATE Warehouse SET Type=?, Location=? WHERE WarehouseID=?";
                    db.executeUpdate(sql, type, location, warehouseID);
                    showAlert("Success", "✓ Warehouse updated successfully!");
                }

                loadWarehouses();
                formStage.close();

            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to save warehouse: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> formStage.close());

        mainContainer.getChildren().addAll(titleLabel, separator1, grid, separator2, buttonBar);

        Scene scene = new Scene(mainContainer, 800, 450);
        formStage.setScene(scene);
        formStage.setResizable(false);
        formStage.show();
    }

    private void deleteWarehouse() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a warehouse to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Warehouse");
        confirm.setContentText("Are you sure you want to delete warehouse at: " + selected.get("Location") + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                int warehouseID = Integer.parseInt(selected.get("WarehouseID").toString());
                String sql = "DELETE FROM Warehouse WHERE WarehouseID=?";
                db.executeUpdate(sql, warehouseID);
                showAlert("Success", "Warehouse deleted successfully!");
                loadWarehouses();
            } catch (SQLException e) {
                String errorMsg = e.getMessage().toLowerCase();
                if (errorMsg.contains("foreign key") || errorMsg.contains("constraint")) {
                    showAlert("Cannot Delete Warehouse",
                        "This warehouse cannot be deleted because it has related records:\n\n" +
                        "• Stock inventory\n" +
                        "• Purchase orders\n" +
                        "• Transfer records\n\n" +
                        "Please move or delete the inventory and related records first.");
                } else {
                    showAlert("Delete Error", "Failed to delete warehouse: " + e.getMessage());
                }
            }
        }
    }

    private void viewWarehouseStock() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a warehouse to view its stock.");
            return;
        }

        int warehouseID = Integer.parseInt(selected.get("WarehouseID").toString());
        String location = selected.get("Location").toString();

        Stage stockStage = new Stage();
        stockStage.setTitle("Stock in: " + location);

        TableView<Map<String, Object>> stockTable = new TableView<>();
        stockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            String sql =
              "  SELECT (SELECT Name FROM Item WHERE Item.ItemID = Stock.ItemID) as Item, "+
                     "  (SELECT Category FROM Item WHERE Item.ItemID = Stock.ItemID) as Category, "+
                      " Quantity, "+
                      " (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Stock.ItemID) as Unit, "+
                     "  ExpirationDate "+
               " FROM Stock "+
               " WHERE WarehouseID = ? "+
               " ORDER BY Item "
            ;
            List<Map<String, Object>> data = db.executeQuery(sql, warehouseID);

            if (!data.isEmpty()) {
                for (String key : data.get(0).keySet()) {
                    TableColumn<Map<String, Object>, Object> col = new TableColumn<>(key);
                    col.setCellValueFactory(r ->
                            new SimpleObjectProperty<>(r.getValue().get(key)));
                    col.setMinWidth(120);
                    col.setPrefWidth(150);
                    col.setStyle("-fx-alignment: CENTER-LEFT;");
                    stockTable.getColumns().add(col);
                }
                stockTable.getItems().addAll(data);
            }

            Label stockStatus = new Label("📊 Items in stock: " + data.size());
            stockStatus.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-font-weight: 500;");

            VBox root = new VBox(15, stockTable, stockStatus);
            root.setPadding(new Insets(15));
            VBox.setVgrow(stockTable, Priority.ALWAYS);

            stockStage.setScene(new Scene(root, 800, 550));
            stockStage.show();

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load stock: " + e.getMessage());
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

