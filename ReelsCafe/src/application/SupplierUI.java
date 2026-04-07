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

public class SupplierUI {
    private DatabaseConnection db;
    private TableView<Map<String, Object>> tableView;
    private Label statusLabel;

    public SupplierUI(DatabaseConnection db) {
        this.db = db;
    }

    public VBox createSupplierPane() {
        Button addBtn = createStyledButton("➕ Add Supplier", "#27ae60");
        Button updateBtn = createStyledButton("✏️ Update", "#3498db");
        Button deleteBtn = createStyledButton("🗑️ Delete", "#e74c3c");
        Button refreshBtn = createStyledButton("🔄 Refresh", "#95a5a6");

        addBtn.setOnAction(e -> openSupplierForm(null));
        updateBtn.setOnAction(e -> {
            Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openSupplierForm(selected);
            } else {
                showAlert("Selection Required", "Please select a supplier to update.");
            }
        });
        deleteBtn.setOnAction(e -> deleteSupplier());
        refreshBtn.setOnAction(e -> loadSuppliers());

        HBox controls = new HBox(15, addBtn, updateBtn, deleteBtn, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(20, 25, 15, 25));
        controls.setStyle("-fx-background-color: white; -fx-border-color: #e1e8ed; -fx-border-width: 0 0 1 0;");

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle("-fx-font-size: 13px;");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-font-weight: 500;");
        statusLabel.setPadding(new Insets(12, 25, 12, 25));

        VBox layout = new VBox(0, controls, tableView, statusLabel);
        layout.setStyle("-fx-background-color: #f5f7fa;");
        VBox.setVgrow(tableView, Priority.ALWAYS);
        VBox.setMargin(tableView, new Insets(20, 25, 15, 25));

        loadSuppliers();
        return layout;
    }

    private void loadSuppliers() {
        try {
            String sql = "SELECT SupplierID, Name, ContactNumber, City FROM Supplier ORDER BY SupplierID";
            List<Map<String, Object>> data = db.executeQuery(sql);

            boolean columnsChanged = tableView.getColumns().isEmpty() ||
                                    (!data.isEmpty() && tableView.getColumns().size() != data.get(0).keySet().size());

            if (columnsChanged) {
                tableView.getColumns().clear();
                if (!data.isEmpty()) {
                    for (String key : data.get(0).keySet()) {
                        TableColumn<Map<String, Object>, Object> col = new TableColumn<>(key);
                        col.setCellValueFactory(r -> new SimpleObjectProperty<>(r.getValue().get(key)));
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

            statusLabel.setText("📊 Total Suppliers: " + data.size());

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load suppliers: " + e.getMessage());
        }
    }

    private void openSupplierForm(Map<String, Object> supplierData) {
        Stage formStage = new Stage();
        formStage.setTitle(supplierData == null ? "Add New Supplier" : "Update Supplier");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(18);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(supplierData == null ? "➕ New Supplier" : "✏️ Edit Supplier");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");
        GridPane.setColumnSpan(titleLabel, 2);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter supplier name");
        nameField.setPrefWidth(450);

        TextField contactField = new TextField();
        contactField.setPromptText("Enter contact number");
        contactField.setPrefWidth(450);

        TextField cityField = new TextField();
        cityField.setPromptText("Enter city");
        cityField.setPrefWidth(450);

        if (supplierData != null) {
            nameField.setText(supplierData.get("Name").toString());
            contactField.setText(supplierData.get("ContactNumber").toString());
            cityField.setText(supplierData.get("City").toString());
        }

        Label nameLabel = new Label("Name:");
        Label contactLabel = new Label("Contact Number:");
        Label cityLabel = new Label("City:");

        String labelStyle = "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #34495e;";
        nameLabel.setStyle(labelStyle);
        contactLabel.setStyle(labelStyle);
        cityLabel.setStyle(labelStyle);

        grid.add(titleLabel, 0, 0);
        grid.add(nameLabel, 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(contactLabel, 0, 2);
        grid.add(contactField, 1, 2);
        grid.add(cityLabel, 0, 3);
        grid.add(cityField, 1, 3);

        Button saveBtn = createStyledButton(supplierData == null ? "✓ Add Supplier" : "✓ Update", "#27ae60");
        Button cancelBtn = createStyledButton("✕ Cancel", "#95a5a6");

        saveBtn.setPrefWidth(140);
        cancelBtn.setPrefWidth(140);

        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String contact = contactField.getText().trim();
                String city = cityField.getText().trim();

                if (name.isEmpty() || contact.isEmpty() || city.isEmpty()) {
                    showAlert("Validation Error", "All fields are required.");
                    return;
                }

                if (supplierData == null) {
                    String sql = "INSERT INTO Supplier (Name, ContactNumber, City) VALUES (?, ?, ?)";
                    db.executeUpdate(sql, name, contact, city);
                    showAlert("Success", "✓ Supplier added successfully!");
                } else {
                    int supplierID = Integer.parseInt(supplierData.get("SupplierID").toString());
                    String sql = "UPDATE Supplier SET Name=?, ContactNumber=?, City=? WHERE SupplierID=?";
                    db.executeUpdate(sql, name, contact, city, supplierID);
                    showAlert("Success", "✓ Supplier updated successfully!");
                }

                loadSuppliers();
                formStage.close();

            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to save supplier: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> formStage.close());

        HBox buttons = new HBox(15, saveBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(20, 0, 0, 0));
        GridPane.setColumnSpan(buttons, 2);
        grid.add(buttons, 0, 4);

        VBox root = new VBox(grid);

        Scene scene = new Scene(root, 750, 500);
        formStage.setScene(scene);
        formStage.show();
    }

    private void deleteSupplier() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a supplier to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Supplier");
        confirm.setContentText("Are you sure you want to delete: " + selected.get("Name") + "?\nThis action cannot be undone.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                int supplierID = Integer.parseInt(selected.get("SupplierID").toString());
                String sql = "DELETE FROM Supplier WHERE SupplierID=?";
                db.executeUpdate(sql, supplierID);
                showAlert("Success", "✓ Supplier deleted successfully!");
                loadSuppliers();
            } catch (SQLException e) {
                String errorMsg = e.getMessage().toLowerCase();
                if (errorMsg.contains("foreign key") || errorMsg.contains("constraint")) {
                    showAlert("Cannot Delete Supplier",
                        "This supplier cannot be deleted because they have related records:\n\n" +
                        "• Purchase orders\n\n" +
                        "Please delete these purchase records first before deleting the supplier.");
                } else {
                    showAlert("Delete Error", "Failed to delete supplier: " + e.getMessage());
                }
            }
        }
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: 600; " +
            "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; " +
            "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);",
            color
        ));

        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: derive(%s, -10%%); -fx-text-fill: white; -fx-font-weight: 600; " +
            "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; " +
            "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 3);",
            color
        )));

        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: 600; " +
            "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 6; " +
            "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);",
            color
        )));

        return btn;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-font-size: 13px;");
        alert.showAndWait();
    }
}

