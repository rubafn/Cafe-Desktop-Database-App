package application;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PurchaseDetailHandler {
	static void openPurchaseDetailsWindow(int purchaseId,DatabaseConnection dbConnection) {
        Stage stage = new Stage();
        stage.setTitle("Purchase Details - PurchaseID: " + purchaseId);

        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, Object> itemCol = new TableColumn<>("ItemID");
        itemCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("ItemID")));
        TableColumn<Map<String, Object>, Object> lineCol = new TableColumn<>("LineNo");
        lineCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("LineNo")));
        TableColumn<Map<String, Object>, Object> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("Quantity")));
        TableColumn<Map<String, Object>, Object> priceCol = new TableColumn<>("PurchasePrice");
        priceCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("PurchasePrice")));
        TableColumn<Map<String, Object>, Object> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("Subtotal")));
        table.getColumns().addAll(itemCol,lineCol,qtyCol, priceCol, subtotalCol);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        Button addItem = new Button("Add Ingredient");
        addItem.setOnAction(e -> addItemToPurchase(purchaseId, table,dbConnection));

        Button deleteItem = new Button("Delete Ingredient");
        deleteItem.setOnAction(e ->{
        	Map<String,Object> selected = table.getSelectionModel().getSelectedItem();
        	if(selected == null) {
        		showAlert("Error","Choose detail first");
        		return;
        	}
        	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to delete this detail?");
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
            String sql = "DELETE FROM PurchaseDetail Where PurchaseID = "+purchaseId+" AND LineNo = " + selected.get("LineNo");
            try {
				dbConnection.executeUpdate(sql);
				table.getItems().setAll(dbConnection.executeQuery("SELECT PurchaseID, LineNo, ItemID, Quantity, PurchasePrice, Quantity * PurchasePrice AS Subtotal FROM PurchaseDetail WHERE PurchaseID=?", purchaseId));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
        });
        
        Button confirm = new Button("Confirm");
        confirm.setOnAction(e ->{
        	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Purchase");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to confirm this purchase? details cant be changed");
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        	stage.close();
        });
        HBox buttons = new HBox(10,addItem,deleteItem,confirm);

        VBox root = new VBox(10, table, buttons);
        root.setPadding(new Insets(10));
        stage.setScene(new Scene(root, 700, 600));
        stage.show();

    }

    private static void addItemToPurchase(int purchaseId, TableView<Map<String, Object>> table,DatabaseConnection dbConnection) {
        try {
            // Get all ingredients
            List<Map<String, Object>> ingredients = dbConnection.executeQuery(
                "SELECT ing.ItemID, i.Name, ing.PurchasePrice FROM Ingredient ing JOIN Item i ON ing.ItemID=i.ItemID"
            );

            ChoiceDialog<Map<String, Object>> dialog = new ChoiceDialog<>(ingredients.get(0), ingredients);
            dialog.setTitle("Select Ingredient");
            dialog.setHeaderText("Choose an ingredient to add to the purchase");
            dialog.setContentText("Ingredient:");
            Optional<Map<String, Object>> result = dialog.showAndWait();

            if (result.isPresent()) {
                Map<String, Object> ingredient = result.get();

                TextInputDialog qtyDialog = new TextInputDialog("1");
                qtyDialog.setTitle("Quantity");
                qtyDialog.setHeaderText("Enter quantity for " + ingredient.get("ItemID"));
                qtyDialog.setContentText("Quantity:");
                Optional<String> qtyResult = qtyDialog.showAndWait();
                if (qtyResult.isEmpty()) return;

                double quantity = Double.parseDouble(qtyResult.get());
                double price = Double.parseDouble(ingredient.get("PurchasePrice").toString());
                double subtotal = quantity * price;

                // Determine next LineNo
                List<Map<String, Object>> lines = dbConnection.executeQuery("SELECT IFNULL(MAX(LineNo),0)+1 AS NextLine FROM PurchaseDetail WHERE PurchaseID=" + purchaseId);
                int lineNo = Integer.parseInt(lines.get(0).get("NextLine").toString());

                // Insert into PurchaseDetail
                dbConnection.executeUpdate(
                    "INSERT INTO PurchaseDetail(PurchaseID, LineNo, ItemID, Quantity, PurchasePrice) VALUES(?,?,?,?,?)",
                    purchaseId, lineNo, ingredient.get("ItemID"), quantity, price);

                // Add to TableView
                Map<String, Object> row = new HashMap<>();
                row.put("ItemID", ingredient.get("ItemID"));
                row.put("LineNo", lineNo);
                row.put("Quantity", quantity);
                row.put("PurchasePrice", price);
                row.put("Subtotal", subtotal);
                table.getItems().add(row); 
            }

        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    
    static void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
