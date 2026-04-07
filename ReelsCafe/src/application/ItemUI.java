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

public class ItemUI {
    private DatabaseConnection db;
    private TableView<Map<String, Object>> tableView;
    private Label statusLabel;
    private ComboBox<String> categoryFilter;
    CheckBox showActiveCheck;

    public ItemUI(DatabaseConnection db) {
        this.db = db;
    }

    public VBox createItemPane() {
        categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All", "Product", "Ingredient");
        categoryFilter.setValue("All");
        categoryFilter.setOnAction(e -> loadItems());

        Button addProductBtn = createButton("Add Product", "#27ae60");
        Button addIngredientBtn = createButton("Add Ingredient", "#16a085");
        Button updateBtn = createButton("Update", "#3498db");
        Button deleteBtn = createButton("Delete", "#e74c3c");
        Button viewRecipeBtn = createButton("View Recipe", "#9b59b6");
        Button refreshBtn = createButton("Refresh", "#95a5a6");
        showActiveCheck = new CheckBox("Show only active products");
        showActiveCheck.setSelected(true);
        showActiveCheck.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        addProductBtn.setOnAction(e -> openItemForm(true, null));
        addIngredientBtn.setOnAction(e -> openItemForm(false, null));
        updateBtn.setOnAction(e -> updateItem());
        deleteBtn.setOnAction(e -> deleteItem());
        viewRecipeBtn.setOnAction(e -> viewRecipe());
        refreshBtn.setOnAction(e -> loadItems());
        showActiveCheck.setOnAction(e -> loadItems());

        HBox controls = new HBox(10, new Label("Filter:"), categoryFilter, showActiveCheck,addProductBtn, addIngredientBtn, updateBtn, deleteBtn, viewRecipeBtn, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        VBox layout = new VBox(15, controls, tableView, statusLabel);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        loadItems();
        return layout;
    }

    private void loadItems() {
        try {
            String filter = categoryFilter.getValue();
            boolean activeOnly = showActiveCheck.isSelected();
            String activeCondition = activeOnly ? " AND Active = true" : "";
            String sql;

            if (filter.equals("Product")) {
                sql = 
                  "  SELECT ItemID, Name, Category, 'Product' as Type, "+
                        "   (SELECT SellingPrice FROM Product WHERE Product.ItemID = Item.ItemID) as SellingPrice, "+
                        "   (SELECT PreparationTime FROM Product WHERE Product.ItemID = Item.ItemID) as PreparationTime "+
                   " FROM Item "+
                   " WHERE ItemID IN (SELECT ItemID FROM Product " + (activeOnly ? "WHERE Active = true" : "") + ")" +
                   " ORDER BY ItemID";
                
            } else if (filter.equals("Ingredient")) {
                sql = 
                  "  SELECT ItemID, Name, Category, 'Ingredient' as Type, "+
                          " (SELECT PurchasePrice FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as PurchasePrice, "+
                          " (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as Unit "+
                   " FROM Item "+
                   " WHERE ItemID IN (SELECT ItemID FROM Ingredient) "+
                   " ORDER BY ItemID ";
                
            } else {
                sql = 
                   " SELECT ItemID, Name, Category, "+
                        "   CASE "+
                            "   WHEN ItemID IN (SELECT ItemID FROM Product) THEN 'Product' "+
                             "  WHEN ItemID IN (SELECT ItemID FROM Ingredient) THEN 'Ingredient' "+
                             "  ELSE 'Unknown' "+
                          " END as Type,  "+
                          " (SELECT SellingPrice FROM Product WHERE Product.ItemID = Item.ItemID) as SellingPrice, "+
                          " (SELECT PreparationTime FROM Product WHERE Product.ItemID = Item.ItemID) as PreparationTime, "+
                          " (SELECT PurchasePrice FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as PurchasePrice, "+
                          " (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as Unit "+
                   " FROM Item "+
                   " ORDER BY ItemID";
                
            }

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

            statusLabel.setText("Total Items: " + data.size());

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load items: " + e.getMessage());
        }
    }

    private void openItemForm(boolean isProduct, Map<String, Object> itemData) {
        Stage formStage = new Stage();
        formStage.setTitle((itemData == null ? "Add New " : "Update ") + (isProduct ? "Product" : "Ingredient"));

        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label((itemData == null ? (isProduct ? "➕ Add New Product" : "➕ Add New Ingredient") :
                                                         (isProduct ? "✏️ Edit Product" : "✏️ Edit Ingredient")));
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

        TextField nameField = new TextField();
        nameField.setPromptText("Enter product/ingredient name");
        nameField.setStyle(fieldStyle);

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Coffee", "Tea", "Pastries", "Supplies", "Beverages", "Snacks", "Other");
        categoryCombo.setPromptText("Select category");
        categoryCombo.setStyle(fieldStyle);
        categoryCombo.setPrefWidth(450);

        int row = 0;

        Label nameLabel = new Label("Item Name: *");
        nameLabel.setStyle(labelStyle);
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);

        Label categoryLabel = new Label("Category: *");
        categoryLabel.setStyle(labelStyle);
        grid.add(categoryLabel, 0, row);
        grid.add(categoryCombo, 1, row++);

        TextField sellingPriceField = null;
        TextField prepTimeField = null;
        TextField purchasePriceField = null;
        ComboBox<String> unitCombo = null;
        
        //adding image
        Label imageLabel = new Label("Product Image:");
        imageLabel.setStyle(labelStyle);

        Label selectedImageLabel = new Label("No file chosen");
        selectedImageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        Button chooseImageBtn = new Button("Choose Image...");
        chooseImageBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        chooseImageBtn.setCursor(javafx.scene.Cursor.HAND);

        chooseImageBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            java.io.File selectedFile = fileChooser.showOpenDialog(grid.getScene().getWindow());
            if (selectedFile != null) {
                selectedImageLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        if (isProduct) {
            sellingPriceField = new TextField();
            sellingPriceField.setPromptText("0.00");
            sellingPriceField.setStyle(fieldStyle);

            prepTimeField = new TextField();
            prepTimeField.setPromptText("Minutes (optional)");
            prepTimeField.setStyle(fieldStyle);

            Label priceLabel = new Label("Selling Price (₪): *");
            priceLabel.setStyle(labelStyle);
            grid.add(priceLabel, 0, row);
            grid.add(sellingPriceField, 1, row++);

            Label prepLabel = new Label("Prep Time (min):");
            prepLabel.setStyle(labelStyle);
            grid.add(prepLabel, 0, row);
            grid.add(prepTimeField, 1, row++);
            
            //adding image button
            grid.add(imageLabel, 0, row);
            grid.add(new HBox(10, chooseImageBtn, selectedImageLabel), 1, row++);

        } else {
            purchasePriceField = new TextField();
            purchasePriceField.setPromptText("0.00");
            purchasePriceField.setStyle(fieldStyle);

            unitCombo = new ComboBox<>();
            unitCombo.getItems().addAll("kg", "liter", "pieces", "bag", "box", "carton", "gallon", "can");
            unitCombo.setPromptText("Select unit");
            unitCombo.setStyle(fieldStyle);
            unitCombo.setPrefWidth(450);

            Label priceLabel = new Label("Purchase Price (₪): *");
            priceLabel.setStyle(labelStyle);
            grid.add(priceLabel, 0, row);
            grid.add(purchasePriceField, 1, row++);

            Label unitLabel = new Label("Unit:");
            unitLabel.setStyle(labelStyle);
            grid.add(unitLabel, 0, row);
            grid.add(unitCombo, 1, row++);
        }

        if (itemData != null) {
            nameField.setText(itemData.get("Name").toString());
            categoryCombo.setValue(itemData.get("Category").toString());

            if (isProduct && sellingPriceField != null) {
                if (itemData.get("SellingPrice") != null)
                    sellingPriceField.setText(itemData.get("SellingPrice").toString());
                if (itemData.get("PreparationTime") != null && prepTimeField != null)
                    prepTimeField.setText(itemData.get("PreparationTime").toString());
            } else if (!isProduct && purchasePriceField != null) {
                if (itemData.get("PurchasePrice") != null)
                    purchasePriceField.setText(itemData.get("PurchasePrice").toString());
                if (itemData.get("Unit") != null && unitCombo != null)
                    unitCombo.setValue(itemData.get("Unit").toString());
            }
        }

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #e1e8ed;");

        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 30, 25, 30));
        buttonBar.setStyle("-fx-background-color: white;");

        Button saveBtn = createButton(itemData == null ? "✓ Save" : "✓ Update", "#27ae60");
        Button cancelBtn = createButton("✕ Cancel", "#95a5a6");

        saveBtn.setPrefWidth(120);
        cancelBtn.setPrefWidth(120);

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        TextField finalSellingPriceField = sellingPriceField;
        TextField finalPrepTimeField = prepTimeField;
        TextField finalPurchasePriceField = purchasePriceField;
        ComboBox<String> finalUnitCombo = unitCombo;

        saveBtn.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                String category = categoryCombo.getValue();

                if (name.isEmpty() || category == null || category.isEmpty()) {
                    showAlert("Validation Error", "Name and Category are required fields.");
                    return;
                }


                int itemID;
                if (itemData == null) {
                    String itemSql = "INSERT INTO Item (Name, Category) VALUES (?, ?)";
                    itemID = db.executeInsertAndGetId(itemSql, name, category);
                } else {
                    itemID = Integer.parseInt(itemData.get("ItemID").toString());
                    String itemSql = "UPDATE Item SET Name=?, Category=? WHERE ItemID=?";
                    db.executeUpdate(itemSql, name, category, itemID);
                }

                if (isProduct) {
                    String priceStr = finalSellingPriceField.getText().trim();
                    String prepTimeStr = finalPrepTimeField.getText().trim();

                    if (priceStr.isEmpty()) {
                        showAlert("Validation Error", "Selling Price is required for products.");
                        return;
                    }

                    double sellingPrice = Double.parseDouble(priceStr);
                    Integer prepTime = prepTimeStr.isEmpty() ? null : Integer.parseInt(prepTimeStr);
                    //getting the image selected
                    String imagePath = selectedImageLabel.getText();
                	if (imagePath.equals("No file chosen")) imagePath = null;
                    if (itemData == null) {
                        String productSql = "INSERT INTO Product (ItemID, SellingPrice, PreparationTime, ImagePath) VALUES (?, ?, ?,?)";
                        db.executeUpdate(productSql, itemID, sellingPrice, prepTime, imagePath);
                    } else {
                        String productSql = "UPDATE Product SET SellingPrice=?, PreparationTime=?, ImagePath=? WHERE ItemID=?";
                        db.executeUpdate(productSql, sellingPrice, prepTime, imagePath, itemID);
                    }
                } else {
                    String priceStr = finalPurchasePriceField.getText().trim();
                    String unit = finalUnitCombo.getValue();

                    if (priceStr.isEmpty()) {
                        showAlert("Validation Error", "Purchase Price is required for ingredients.");
                        return;
                    }

                    double purchasePrice = Double.parseDouble(priceStr);

                    if (itemData == null) {
                        String ingredientSql = "INSERT INTO Ingredient (ItemID, PurchasePrice, Unit) VALUES (?, ?, ?)";
                        db.executeUpdate(ingredientSql, itemID, purchasePrice, unit);
                    } else {
                        String ingredientSql = "UPDATE Ingredient SET PurchasePrice=?, Unit=? WHERE ItemID=?";
                        db.executeUpdate(ingredientSql, purchasePrice, unit, itemID);
                    }
                }

                showAlert("Success", "✓ " + (isProduct ? "Product" : "Ingredient") + " saved successfully!");
                loadItems();
                formStage.close();

            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Invalid number format. Please enter valid prices.");
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to save item: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> formStage.close());

        mainContainer.getChildren().addAll(titleLabel, separator1, grid, separator2, buttonBar);

        Scene scene = new Scene(mainContainer, 800, isProduct ? 500 : 520);
        formStage.setScene(scene);
        formStage.setResizable(false);
        formStage.show();
    }

    private void updateItem() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select an item to update.");
            return;
        }

        String type = selected.get("Type").toString();
        boolean isProduct = type.equals("Product");

        try {
            int itemID = Integer.parseInt(selected.get("ItemID").toString());
            String sql;

            if (isProduct) {
                sql = 
                  "  SELECT ItemID, Name, Category, "+
                         "  (SELECT SellingPrice FROM Product WHERE Product.ItemID = Item.ItemID) as SellingPrice, "+
                         "  (SELECT PreparationTime FROM Product WHERE Product.ItemID = Item.ItemID) as PreparationTime "+
                   " FROM Item "+
                  "  WHERE ItemID = ?";
                
            } else {
                sql = 
                  "  SELECT ItemID, Name, Category, "+
                           "(SELECT PurchasePrice FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as PurchasePrice,  "+
                           "(SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as Unit "+
                   " FROM Item "+
                   " WHERE ItemID = ? ";
                
            }

            List<Map<String, Object>> result = db.executeQuery(sql, itemID);
            if (!result.isEmpty()) {
                openItemForm(isProduct, result.get(0));
            }

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load item details: " + e.getMessage());
        }
    }

    private void deleteItem() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select an item to deactivate.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Item");
        confirm.setContentText("Are you sure you want to deactivate: " + selected.get("Name") + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
            	int itemID = Integer.parseInt(selected.get("ItemID").toString());
                String type = selected.get("Type").toString();

                if (type.equals("Product")) {
                    //only set Active = false
                    String sql = "UPDATE Product SET Active = false WHERE ItemID = ?";
                    db.executeUpdate(sql, itemID);
                } else if (type.equals("Ingredient")) {
                    String sql = "DELETE FROM Ingredient WHERE ItemID = ?";
                    db.executeUpdate(sql, itemID);
                }

                showAlert("Success", "Item removed successfully!");
                loadItems();
            } catch (SQLException e) {
            	showAlert("Error", "Failed to remove item: " + e.getMessage());
            }
        }
    }

    private void viewRecipe() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a product to view its recipe.");
            return;
        }

        String type = selected.get("Type").toString();
        if (!type.equals("Product")) {
            showAlert("Invalid Selection", "Only products have recipes. Please select a product.");
            return;
        }

        Stage recipeStage = new Stage();
        recipeStage.setTitle("Recipe for " + selected.get("Name"));

        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("🧾 Recipe: " + selected.get("Name"));
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 700; -fx-text-fill: #2c3e50; -fx-padding: 25 30 20 30;");

        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: #e1e8ed;");

        TableView<Map<String, Object>> recipeTable = new TableView<>();
        recipeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            int itemID = Integer.parseInt(selected.get("ItemID").toString());
            String sql = 
               " SELECT (SELECT Name FROM Item WHERE Item.ItemID = Recipe.IngredientID) as Ingredient, "+
                     "  Quantity, "+
                     "  (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Recipe.IngredientID) as Unit "+
              "  FROM Recipe "+
               " WHERE ProductID = ?"+
              "  ORDER BY Ingredient ";
            
            List<Map<String, Object>> recipeData = db.executeQuery(sql, itemID);

            if (!recipeData.isEmpty()) {
                for (String key : recipeData.get(0).keySet()) {
                    TableColumn<Map<String, Object>, Object> col = new TableColumn<>(key);
                    col.setCellValueFactory(r ->
                            new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get(key)));
                    col.setMinWidth(120);
                    col.setStyle("-fx-alignment: CENTER-LEFT;");
                    recipeTable.getColumns().add(col);
                }
                recipeTable.getItems().addAll(recipeData);
            } else {
                Label noRecipe = new Label("No recipe defined for this product yet.");
                noRecipe.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 30;");
                mainContainer.getChildren().addAll(titleLabel, separator1, noRecipe);
                Scene scene = new Scene(mainContainer, 700, 250);
                recipeStage.setScene(scene);
                recipeStage.show();
                return;
            }

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load recipe: " + e.getMessage());
            return;
        }

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #e1e8ed;");

        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 30, 25, 30));
        buttonBar.setStyle("-fx-background-color: white;");

        Button closeBtn = createButton("✕ Close", "#95a5a6");
        closeBtn.setPrefWidth(120);
        closeBtn.setOnAction(e -> recipeStage.close());

        buttonBar.getChildren().add(closeBtn);

        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(20));
        tableContainer.getChildren().add(recipeTable);
        VBox.setVgrow(recipeTable, Priority.ALWAYS);

        mainContainer.getChildren().addAll(titleLabel, separator1, tableContainer, separator2, buttonBar);

        Scene scene = new Scene(mainContainer, 800, 500);
        recipeStage.setScene(scene);
        recipeStage.show();
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

