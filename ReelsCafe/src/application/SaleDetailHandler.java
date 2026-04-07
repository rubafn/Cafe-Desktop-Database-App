package application;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class SaleDetailHandler {
	
	 static void openSaleDetailsWindow(int saleId,DatabaseConnection dbConnection) {
        Stage stage = new Stage();
        stage.setTitle("Sale Details - SaleID: " + saleId);

        TableView<Map<String, Object>> table = new TableView<>();
        TableColumn<Map<String, Object>, Object> itemCol = new TableColumn<>("ItemID");
        itemCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("ItemID")));
        TableColumn<Map<String, Object>, Object> lineCol = new TableColumn<>("LineNo");
        lineCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("LineNo")));
        TableColumn<Map<String, Object>, Object> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("Quantity")));
        TableColumn<Map<String, Object>, Object> priceCol = new TableColumn<>("SellingPrice");
        priceCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("SellingPrice")));
        TableColumn<Map<String, Object>, Object> subtotalCol = new TableColumn<>("Subtotal");
        subtotalCol.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("Subtotal")));
        table.getColumns().addAll(itemCol,lineCol, qtyCol, priceCol, subtotalCol);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        Button addItem = new Button("Add Product");
        addItem.setOnAction(e -> addItemToSale(saleId, table,dbConnection));
        
        Button deleteItem = new Button("Delete Product");
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
            String sql = "DELETE FROM SaleDetail Where SaleID = "+saleId+" AND LineNo = " + selected.get("LineNo");
            try {
				dbConnection.executeUpdate(sql);
				table.getItems().setAll(dbConnection.executeQuery("SELECT SaleID, LineNo, ItemID, Quantity, SellingPrice, Quantity * SellingPrice AS Subtotal FROM SaleDetail WHERE SaleID=?", saleId));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
        });
        
        Button confirm = new Button("Confirm");
        confirm.setOnAction(e ->{
        	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Sale");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to confirm this sale? details cant be changed");
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

	 private static void addItemToSale(int saleId, TableView<Map<String, Object>> table, DatabaseConnection dbConnection) {
		    try {
		        // Get all products
		        List<Map<String, Object>> products = dbConnection.executeQuery(
		            "SELECT p.ItemID, i.Name, p.SellingPrice, p.ImagePath FROM Product p JOIN Item i ON p.ItemID=i.ItemID Where p.Active = true"
		        );

		        Stage dialogStage = new Stage();
		        dialogStage.setTitle("Select Product");

		        TilePane tilePane = new TilePane();
		        tilePane.setPadding(new Insets(10));
		        tilePane.setHgap(15);
		        tilePane.setVgap(15);
		        tilePane.setPrefColumns(3); //number of columns in the grid

		        int i = 0;
		        for (Map<String, Object> prod : products) {
		            VBox card = new VBox(5);
		            card.setPadding(new Insets(5));
		            card.setStyle("-fx-border-color: #ccc; -fx-border-radius:  5; -fx-background-radius: 5; -fx-background-color: #f9f9f9;");
		            card.setPrefWidth(120);
		            card.setAlignment(Pos.CENTER);

		            ImageView iv = new ImageView("file:"+prod.get("ImagePath").toString());
		            iv.setFitWidth(80);
		            iv.setFitHeight(80);

		            Label name = new Label(prod.get("Name").toString());
		            name.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");

		            Label price = new Label("$" + prod.get("SellingPrice").toString());
		            price.setStyle("-fx-text-fill: #2c3e50;");

		            card.getChildren().addAll(iv, name, price);

		            //store the product map for easy retrieval
		            card.setUserData(prod);

		            //click handler to select this product
		            card.setOnMouseClicked(e -> {
		                dialogStage.close();
		                Map<String, Object> product = (Map<String, Object>) card.getUserData();
		                askQuantityAndAdd(saleId, table, dbConnection, product);
		            });

		            tilePane.getChildren().add(card);
		            i++;
		        }

		        ScrollPane scrollPane = new ScrollPane(tilePane);
		        scrollPane.setFitToWidth(true);

		        VBox root = new VBox(scrollPane);
		        root.setPadding(new Insets(10));
		        dialogStage.setScene(new Scene(root, 450, 400));
		        dialogStage.show();

		    } catch (SQLException ex) {
		        showAlert("Error", ex.getMessage());
		    }
		}
	 private static void askQuantityAndAdd(int saleId, TableView<Map<String, Object>> table, DatabaseConnection dbConnection, Map<String, Object> product) {
		    TextInputDialog qtyDialog = new TextInputDialog("1");
		    qtyDialog.setTitle("Quantity");
		    qtyDialog.setHeaderText("Enter quantity for " + product.get("Name"));
		    qtyDialog.setContentText("Quantity:");
		    Optional<String> qtyResult = qtyDialog.showAndWait();
		    if (qtyResult.isEmpty()) return;

		    try {
		        int quantity = Integer.parseInt(qtyResult.get());
		        double price = Double.parseDouble(product.get("SellingPrice").toString());
		        double subtotal = quantity * price;

		        //finding next LineNo
		        List<Map<String, Object>> lines = dbConnection.executeQuery(
		            "SELECT IFNULL(MAX(LineNo),0)+1 AS NextLine FROM SaleDetail WHERE SaleID=" + saleId
		        );
		        int lineNo = Integer.parseInt(lines.get(0).get("NextLine").toString());

		        //check stock
		        List<Map<String, Object>> recipe = dbConnection.executeQuery(
		            "Select * From Recipe Where ProductID = ?", product.get("ItemID")
		        );
		        for (Map<String, Object> ing : recipe) {
		            double required = Double.parseDouble(ing.get("Quantity").toString()) * quantity;
		            int ingId = Integer.parseInt(ing.get("IngredientID").toString());

		            List<Map<String, Object>> stock = dbConnection.executeQuery(
		                "SELECT Quantity FROM Stock WHERE WarehouseID=1 AND ItemID=?", ingId
		            );
		            if (stock.isEmpty() || Double.parseDouble(stock.get(0).get("Quantity").toString()) < required) {
		                showAlert("Stock Error", "Not enough stock for ingredient ID " + ingId);
		                return;
		            }
		        }

		        //insert into SaleDetail
		        dbConnection.executeUpdate(
		            "INSERT INTO SaleDetail(SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES(?,?,?,?,?)",
		            saleId, lineNo, product.get("ItemID"), quantity, price
		        );

		        //add to TableView
		        Map<String, Object> row = new HashMap<>();
		        row.put("ItemID", product.get("ItemID"));
		        row.put("LineNo", lineNo);
		        row.put("Quantity", quantity);
		        row.put("SellingPrice", price);
		        row.put("Subtotal", subtotal);
		        table.getItems().add(row);
		        table.refresh();

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
