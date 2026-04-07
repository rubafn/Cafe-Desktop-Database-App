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

public class RecipeUI {
    private DatabaseConnection db;
    private TableView<Map<String, Object>> tableView;
    private Label statusLabel;
    private ComboBox<String> productFilter;

    public RecipeUI(DatabaseConnection db) {
        this.db = db;
    }

    public VBox createRecipePane() {
        productFilter = new ComboBox<>();
        productFilter.setPromptText("All Products");
        loadProductFilter();
        productFilter.setOnAction(e -> loadRecipes());

        Button addBtn = createButton("Add Ingredient to Recipe", "#27ae60");
        Button deleteBtn = createButton("Delete", "#e74c3c");
        Button refreshBtn = createButton("Refresh", "#95a5a6");
        Button viewAllBtn = createButton("View All Recipes", "#3498db");

        addBtn.setOnAction(e -> openRecipeForm(null));
        deleteBtn.setOnAction(e -> deleteRecipe());
        refreshBtn.setOnAction(e -> {
            loadProductFilter();
            loadRecipes();
        });
        viewAllBtn.setOnAction(e -> {
            productFilter.setValue(null);
            loadRecipes();
        });

        HBox controls = new HBox(10, new Label("Filter by Product:"), productFilter,
                                 addBtn, deleteBtn, refreshBtn, viewAllBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        VBox layout = new VBox(15, controls, tableView, statusLabel);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        loadRecipes();
        return layout;
    }

    private void loadProductFilter() {
        try {
            String sql = "SELECT ItemID, Name FROM Item WHERE ItemID IN (SELECT ItemID FROM Product) ORDER BY Name";
            List<Map<String, Object>> products = db.executeQuery(sql);

            productFilter.getItems().clear();
            for (Map<String, Object> prod : products) {
                productFilter.getItems().add(prod.get("ItemID") + " - " + prod.get("Name"));
            }
        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void loadRecipes() {
        try {
            String sql;
            List<Map<String, Object>> data;

            if (productFilter.getValue() == null || productFilter.getValue().isEmpty()) {
                sql = 
                   " SELECT (SELECT Name FROM Item WHERE Item.ItemID = Recipe.ProductID) as Product, "+
                         "  (SELECT Name FROM Item WHERE Item.ItemID = Recipe.IngredientID) as Ingredient, "+
                         "  Quantity, "+
                         "  (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Recipe.IngredientID) as Unit, "+
                         "  ProductID, "+
                          " IngredientID "+
                   " FROM Recipe "+
                  "  ORDER BY Product, Ingredient"
                ;
                data = db.executeQuery(sql);
            } else {
                int productID = Integer.parseInt(productFilter.getValue().split(" - ")[0]);
                sql = 
                  "  SELECT (SELECT Name FROM Item WHERE Item.ItemID = Recipe.ProductID) as Product,  "+
                         "  (SELECT Name FROM Item WHERE Item.ItemID = Recipe.IngredientID) as Ingredient, "+
                         "  Quantity, "+
                         "  (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Recipe.IngredientID) as Unit, "+
                         "  ProductID, "+
                          " IngredientID "+
                   " FROM Recipe "+
                   " WHERE ProductID = ? "+
                   " ORDER BY Ingredient "
                ;
                data = db.executeQuery(sql, productID);
            }

            boolean columnsChanged = tableView.getColumns().isEmpty() ||
                                    (!data.isEmpty() && tableView.getColumns().size() != (data.get(0).keySet().size() - 2));

            if (columnsChanged) {
                tableView.getColumns().clear();
                if (!data.isEmpty()) {
                    for (String key : data.get(0).keySet()) {
                        if (key.equals("ProductID") || key.equals("IngredientID")) {
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

            statusLabel.setText("Total Recipe Entries: " + data.size());

        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load recipes: " + e.getMessage());
        }
    }

    private void openRecipeForm(Map<String, Object> recipeData) {
        Stage formStage = new Stage();
        formStage.setTitle(recipeData == null ? "Add Ingredient to Recipe" : "Update Recipe");

        VBox mainContainer = new VBox(0);
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(recipeData == null ? "➕ Add Ingredient to Recipe" : "✏️ Edit Recipe");
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

        ComboBox<String> productCombo = new ComboBox<>();
        productCombo.setPromptText("Select product");
        productCombo.setStyle(fieldStyle);
        productCombo.setPrefWidth(450);

        ComboBox<String> ingredientCombo = new ComboBox<>();
        ingredientCombo.setPromptText("Select ingredient");
        ingredientCombo.setStyle(fieldStyle);
        ingredientCombo.setPrefWidth(450);

        TextField quantityField = new TextField();
        quantityField.setPromptText("0.00");
        quantityField.setStyle(fieldStyle);

        Label unitLabel = new Label("");
        unitLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        try {
            String sql = "SELECT ItemID, Name FROM Item WHERE ItemID IN (SELECT ItemID FROM Product) ORDER BY Name";
            List<Map<String, Object>> products = db.executeQuery(sql);
            for (Map<String, Object> prod : products) {
                productCombo.getItems().add(prod.get("ItemID") + " - " + prod.get("Name"));
            }
        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load products: " + e.getMessage());
        }

        try {
            String sql = "SELECT ItemID, Name, (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as Unit FROM Item WHERE ItemID IN (SELECT ItemID FROM Ingredient) ORDER BY Name";
            List<Map<String, Object>> ingredients = db.executeQuery(sql);
            for (Map<String, Object> ing : ingredients) {
                ingredientCombo.getItems().add(ing.get("ItemID") + " - " + ing.get("Name") + " (" + ing.get("Unit") + ")");
            }
        } catch (SQLException e) {
            showAlert("Load Error", "Failed to load ingredients: " + e.getMessage());
        }

        ingredientCombo.setOnAction(e -> {
            if (ingredientCombo.getValue() != null) {
                String selected = ingredientCombo.getValue();
                String unit = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")"));
                unitLabel.setText("Unit: " + unit);
            }
        });

        if (recipeData != null) {
            productCombo.setValue(recipeData.get("ProductID") + " - " + recipeData.get("Product"));
            ingredientCombo.setValue(recipeData.get("IngredientID") + " - " + recipeData.get("Ingredient"));
            quantityField.setText(recipeData.get("Quantity").toString());
            unitLabel.setText("Unit: " + recipeData.get("Unit").toString());

            productCombo.setDisable(true);
            ingredientCombo.setDisable(true);
            productCombo.setStyle(fieldStyle + "-fx-opacity: 0.6;");
            ingredientCombo.setStyle(fieldStyle + "-fx-opacity: 0.6;");
        }

        int row = 0;

        Label productLabel = new Label("Product: *");
        productLabel.setStyle(labelStyle);
        grid.add(productLabel, 0, row);
        grid.add(productCombo, 1, row++);

        Label ingredientLabel = new Label("Ingredient: *");
        ingredientLabel.setStyle(labelStyle);
        grid.add(ingredientLabel, 0, row);
        grid.add(ingredientCombo, 1, row++);

        Label quantityLabel = new Label("Quantity: *");
        quantityLabel.setStyle(labelStyle);
        grid.add(quantityLabel, 0, row);
        grid.add(quantityField, 1, row++);

        if (!unitLabel.getText().isEmpty()) {
            grid.add(unitLabel, 1, row++);
        }

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: #e1e8ed;");

        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 30, 25, 30));
        buttonBar.setStyle("-fx-background-color: white;");

        Button saveBtn = createButton(recipeData == null ? "✓ Save" : "✓ Update", "#27ae60");
        Button cancelBtn = createButton("✕ Cancel", "#95a5a6");

        saveBtn.setPrefWidth(120);
        cancelBtn.setPrefWidth(120);

        buttonBar.getChildren().addAll(cancelBtn, saveBtn);

        saveBtn.setOnAction(e -> {
            try {
                if (productCombo.getValue() == null || ingredientCombo.getValue() == null ||
                    quantityField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Product, Ingredient, and Quantity are required fields.");
                    return;
                }

                int productID = Integer.parseInt(productCombo.getValue().split(" - ")[0]);
                int ingredientID = Integer.parseInt(ingredientCombo.getValue().split(" - ")[0]);
                double quantity = Double.parseDouble(quantityField.getText().trim());

                if (recipeData == null) {
                    String sql = "INSERT INTO Recipe (ProductID, IngredientID, Quantity) VALUES (?, ?, ?)";
                    db.executeUpdate(sql, productID, ingredientID, quantity);
                    showAlert("Success", "✓ Ingredient added to recipe successfully!");
                } else {
                    String sql = "UPDATE Recipe SET Quantity=? WHERE ProductID=? AND IngredientID=?";
                    db.executeUpdate(sql, quantity, productID, ingredientID);
                    showAlert("Success", "✓ Recipe updated successfully!");
                }

                loadRecipes();
                formStage.close();

            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Invalid quantity format. Please enter a valid number.");
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to save recipe: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> formStage.close());

        mainContainer.getChildren().addAll(titleLabel, separator1, grid, separator2, buttonBar);

        Scene scene = new Scene(mainContainer, 800, 550);
        formStage.setScene(scene);
        formStage.setResizable(false);
        formStage.show();
    }

    private void deleteRecipe() {
        Map<String, Object> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a recipe entry to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Recipe Entry");
        confirm.setContentText("Remove " + selected.get("Ingredient") + " from " + selected.get("Product") + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                int productID = Integer.parseInt(selected.get("ProductID").toString());
                int ingredientID = Integer.parseInt(selected.get("IngredientID").toString());
                String sql = "DELETE FROM Recipe WHERE ProductID=? AND IngredientID=?";
                db.executeUpdate(sql, productID, ingredientID);
                showAlert("Success", "Recipe entry deleted successfully!");
                loadRecipes();
            } catch (SQLException e) {
                showAlert("Delete Error", "Failed to delete recipe: " + e.getMessage());
            }
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

