package application;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Tabs {

	 //Tabs
    static TabPane createTabs(ReelsCafeApp app) {

        TabPane pane = new TabPane();

        Tab home = new Tab("Home");
        home.setClosable(false);
        home.setContent(createHomeTab(app));
        home.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");
        
        Tab supplierTab = new Tab("Suppliers");
        supplierTab.setClosable(false);
        supplierTab.setContent(new SupplierUI(app.dbConnection).createSupplierPane());
        supplierTab.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");

        Tab itemTab = new Tab("Items");
        itemTab.setClosable(false);
        itemTab.setContent(new ItemUI(app.dbConnection).createItemPane());
        itemTab.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");

        Tab recipeTab = new Tab("Recipes");
        recipeTab.setClosable(false);
        recipeTab.setContent(new RecipeUI(app.dbConnection).createRecipePane());
        recipeTab.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");

        Tab warehouseTab = new Tab("Warehouses");
        warehouseTab.setClosable(false);
        warehouseTab.setContent(new WarehouseUI(app.dbConnection).createWarehousePane());
        warehouseTab.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");

        Tab stockTab = new Tab("Stock");
        stockTab.setClosable(false);
        stockTab.setContent(new StockUI(app.dbConnection).createStockPane());
        stockTab.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");

        Tab queries = new Tab("Queries");
        queries.setClosable(false);
        queries.setContent(createQueriesTab(app));
        queries.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");
        
        Tab queries2 = new Tab("Queries2");
        queries2.setClosable(false);
        queries2.setContent(createQueries2Tab(app));
        queries2.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");
        
        Tab admin = new Tab("Admin");
        admin.setClosable(false);
        admin.setContent(AdminStats.stats(app.dbConnection));
        admin.setStyle("-fx-background-color: #2c3e50;-fx-text-base-color: white;-fx-font-weight: bold;-fx-font-size: 14px;-fx-padding: 5 10 5 10;");

        pane.getTabs().addAll(admin,home,supplierTab, itemTab, recipeTab, warehouseTab, stockTab,  queries,queries2);
        return pane;
    }

    //Home Tab
    private static VBox createHomeTab(ReelsCafeApp app) {

        ComboBox<String> tableBox = new ComboBox<>();
        tableBox.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
        tableBox.getItems().addAll(app.tables.keySet());
        tableBox.setValue("Customer");

        Button add = app.createStyledButton("🆕 Add");
        Button search = app.createStyledButton("🔍 Search");
        Button update = app.createStyledButton("📝 Update");
        Button delete = app.createStyledButton("🗃 Unhire");
        Button details = app.createStyledButton("📋 Details");
        details.setVisible(false);

        add.setOnAction(e -> app.openForm(null));

        update.setOnAction(e -> {
            Map<String, Object> row = app.tableView.getSelectionModel().getSelectedItem();
            if (row != null) app.openForm(row);
        });

        if(app.currentTable.getTableName().equalsIgnoreCase("Employee")) {
        	delete.setVisible(true);
        }else {
        	delete.setVisible(false);
        }
        delete.setOnAction(e ->{
        	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to unhire this employee?");
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        	Map<String, Object> emp = app.tableView.getSelectionModel().getSelectedItem();
        	if(emp == null) {
        		app.showAlert("Deletion Error", "Select a row first");
                return;
        	}
        	int empid = Integer.parseInt(emp.get("EmployeeID").toString());
        	try {
				app.dbConnection.executeUpdate("UPDATE Employee Set Hired = false Where EmployeeID = "+empid);
				UITablesHelper.loadTable(app.currentTable,app);
				app.statusLabel.setText("Employee Unhired");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
        });
        search.setOnAction(e -> app.searchByPK());

        details.setOnAction(e -> {
            Map<String, Object> parent = app.tableView.getSelectionModel().getSelectedItem();

            if (parent == null) {
                app.showAlert("Details Error", "Select a row first");
                return;
            }

            app.openDetailsWindow(app.currentTable, parent);
        });
        
        tableBox.setOnAction(e -> {
        	app.currentTable = app.tables.get(tableBox.getValue());
        	UITablesHelper.loadTable(app.currentTable,app);
        	delete.setVisible(app.currentTable.getTableName().equalsIgnoreCase("Employee"));
        	details.setVisible(app.currentTable.hasDetails());
        });

        VBox controls = new VBox(10, tableBox,
                new HBox(10, add, search, update, delete,details));
        controls.setAlignment(Pos.CENTER_LEFT);

        app.tableView = new TableView<>(); 
        app.tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        app.statusLabel = new Label("Ready");

        VBox layout = new VBox(15, controls, app.tableView, app.statusLabel);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(app.tableView, Priority.ALWAYS);

        UITablesHelper.loadTable(app.currentTable,app);
        return layout;
    }
    
    private static VBox createQueriesTab(ReelsCafeApp app) {

    	TextArea queryArea = new TextArea();
        queryArea.setEditable(false);
        queryArea.setPrefHeight(80);
        try {
			app.suppliers.getItems().setAll(app.dbConnection.executeQuery("Select SupplierID, Name FROM Supplier"));
			app.warehouses.getItems().setAll(app.dbConnection.executeQuery("Select WarehouseID,Type FROM Warehouse"));
		} catch (SQLException e) {
			showAlert("Error!",e.getMessage());
		}
    	

        TableView<Map<String, Object>> queryTable = new TableView<>();
        Label qStatus = new Label("Ready");

        Button q1 = app.createStyledButton("📈Top Products");
        Button q2 = app.createStyledButton("💼 Employee Sales");
        Button q3 = app.createStyledButton("Loyal Customers");
        Button q4 = app.createStyledButton("👥 Suppliers in City");
        Button q5 = app.createStyledButton("Highest Profit");
        Button q6 = app.createStyledButton("🔍 In-Store Stock");
        Button q7 = app.createStyledButton("📨 Items from Supplier");
        Button q8 = app.createStyledButton("Most Used Ingredients");
        Button q9 = app.createStyledButton("📅 Sales on Date");
        Button q10 = app.createStyledButton("Top Customers last week");
        Button q11 = app.createStyledButton("📦 Items to Restock");
        VBox q4box = new VBox(q4);
        VBox q7box = new VBox(q7);
        VBox q9box = new VBox(q9);
        VBox q11box = new VBox(q11);
        
        
        q1.setOnAction(e -> runQuery(Queries.q1(), queryTable, qStatus,app ,queryArea));

        q2.setOnAction(e -> runQuery(Queries.q2(), queryTable, qStatus,app,queryArea));

        q3.setOnAction(e -> runQuery(Queries.q3(), queryTable, qStatus,app,queryArea));
        
        
        q4box.setSpacing(5);
        q4.setOnAction(e -> {
        	Button find = new Button("find");
        	TextField choice = new TextField();
        	q4box.getChildren().addAll(choice,find);
        	find.setOnAction(ev ->{
        		if(!choice.getText().isEmpty()) {
        			String city = choice.getText();
        			runQuery(Queries.q4(city), queryTable, qStatus,app,queryArea);
        			q4box.getChildren().removeAll(choice,find);
        		}else {
        			showAlert("Error","Enter a city");
        		}
        	});
        });
        
        q5.setOnAction(e -> runQuery(Queries.q5(), queryTable, qStatus,app,queryArea));
        
        q6.setOnAction(e -> runQuery(Queries.q6(), queryTable, qStatus,app,queryArea));
        
        
        q7box.setSpacing(5);
        q7.setOnAction(e -> {
        	Button find = new Button("find");
        	q7box.getChildren().addAll(app.suppliers,find);
        	find.setOnAction(ev ->{
        		if(!app.suppliers.getSelectionModel().getSelectedItem().isEmpty()) {
        			String supplier = app.suppliers.getSelectionModel().getSelectedItem().get("Name").toString();
        			runQuery(Queries.q7(supplier), queryTable, qStatus,app,queryArea);
        			q7box.getChildren().removeAll(app.suppliers,find);
        		}else {
        			showAlert("Error","choose a supplier");
        		}
        	});
        });
        
        q8.setOnAction(e -> runQuery(Queries.q8(), queryTable, qStatus,app,queryArea));
        
        
        q9box.setSpacing(5);
        q9.setOnAction(e -> {
        	Button find = new Button("find");
        	DatePicker picker = new DatePicker();
        	picker.setValue(LocalDate.now());
        	q9box.getChildren().addAll(picker,find);
        	find.setOnAction(ev ->{
        		if(!picker.getValue().equals(null)) {
        			String date = picker.getValue().toString();
        			runQuery(Queries.q9(date), queryTable, qStatus,app,queryArea);
        			q9box.getChildren().removeAll(picker,find);
        		}else {
        			showAlert("Error","choose a date");
        		}
        	});
        });
        
        q10.setOnAction(e -> runQuery(Queries.q10(), queryTable, qStatus,app,queryArea));
        
        
        q11box.setSpacing(5);
        q11.setOnAction(e -> {
        	Button find = new Button("find");
        	q11box.getChildren().addAll(app.warehouses,find);
        	find.setOnAction(ev ->{
        		if(!app.warehouses.getSelectionModel().getSelectedItem().isEmpty()) {
        			String id = app.warehouses.getSelectionModel().getSelectedItem().get("WarehouseID").toString();
        			runQuery(Queries.q11(id), queryTable, qStatus,app,queryArea);
        			q11box.getChildren().removeAll(app.warehouses,find);
        		}else {
        			showAlert("Error","choose a warehouse");
        		}
        	});
        });
        HBox part1 = new HBox(q1,q2,q3,q4box,q5);
        part1.setSpacing(7);
        HBox part2 = new HBox(q6,q7box,q8,q9box,q10,q11box);
        part2.setSpacing(7);

        Label sectionTitle = new Label("📊 Query Reports");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");
        
        VBox root = new VBox(10,sectionTitle,queryArea,part1,part2,queryTable,qStatus);
        root.setPadding(new Insets(10));
        VBox.setVgrow(queryTable, Priority.ALWAYS);
        return root;
    }
    private static VBox createQueries2Tab(ReelsCafeApp app) {

        Label sectionTitle = new Label("📊 Advanced Query Reports");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        TextArea queryArea = new TextArea();
        queryArea.setEditable(false);
        queryArea.setPrefHeight(100);
        queryArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");

        TableView<Map<String, Object>> queryTable = new TableView<>();
        queryTable.setStyle("-fx-font-size: 13px;");

        Label qStatus = new Label("Ready");
        qStatus.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-font-weight: 500;");

        Label salesLabel = new Label("💰 Sales Analytics");
        salesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        Button q1 = createStyledButton("📅 Sales Last Week", "#27ae60");
        Button q2 = createStyledButton("📈 Top Products", "#27ae60");
        Button q3 = createStyledButton("📉 Lowest Sales Products", "#e74c3c");

        q1.setOnAction(e -> runQuery2(Queries2.totalSalesPerDayLastWeek(), qStatus,app,queryArea));
        q2.setOnAction(e -> runQuery2(Queries2.topProducts(), qStatus,app,queryArea));
        q3.setOnAction(e -> runQuery2(Queries2.productsWithLowestSales(), qStatus,app,queryArea));

        HBox salesButtons = new HBox(15, q1, q2, q3);
        salesButtons.setAlignment(Pos.CENTER_LEFT);

        Label customerLabel = new Label("👥 Customer Analytics");
        customerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        Button q4 = createStyledButton("☕ Top Coffee Customers", "#3498db");
        Button q6 = createStyledButton("😴 Inactive Customers", "#95a5a6");
        Button q7 = createStyledButton("🔍 Customer History", "#9b59b6");

        q4.setOnAction(e -> runQuery2(Queries2.top5CoffeeCustomers(), qStatus,app,queryArea));
        q6.setOnAction(e -> runQuery2(Queries2.customersWithoutRecentPurchases(), qStatus,app,queryArea));
        q7.setOnAction(e -> openCustomerHistoryDialog(qStatus,app,queryArea));

        HBox customerButtons = new HBox(15, q4, q6, q7);
        customerButtons.setAlignment(Pos.CENTER_LEFT);

        Label employeeLabel = new Label("👔 Employee Analytics");
        employeeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        Button q8 = createStyledButton("💼 Sales Performance", "#16a085");
        Button q9 = createStyledButton("💰 Average Salary", "#16a085");
        Button q10 = createStyledButton("🏆 Top Employee", "#f39c12");
        Button q11 = createStyledButton("👴 Oldest Employees", "#34495e");

        q8.setOnAction(e -> runQuery2(Queries2.employeeSalesPerformance(), qStatus,app,queryArea));
        q9.setOnAction(e -> runQuery2(Queries2.averageSalaryByHiringDate(), qStatus,app,queryArea));
        q10.setOnAction(e -> runQuery2(Queries2.employeeWithMostTransactions(), qStatus,app,queryArea));
        q11.setOnAction(e -> runQuery2(Queries2.top3OldestEmployees(), qStatus,app,queryArea));

        HBox employeeButtons = new HBox(15, q8, q9, q10, q11);
        employeeButtons.setAlignment(Pos.CENTER_LEFT);

        Label inventoryLabel = new Label("📦 Inventory Analytics");
        inventoryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2c3e50;");

        Button q12 = createStyledButton("💎 Most Expensive Ingredient", "#e67e22");
        Button q13 = createStyledButton("🔍 Transaction Details", "#9b59b6");

        q12.setOnAction(e -> runQuery2(Queries2.mostExpensiveIngredientInStock(), qStatus,app,queryArea));
        q13.setOnAction(e -> openTransactionDetailsDialog(qStatus,app,queryArea));

        HBox inventoryButtons = new HBox(15, q12, q13);
        inventoryButtons.setAlignment(Pos.CENTER_LEFT);

        Label queryLabel = new Label("📝 Current Query:");
        queryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #34495e;");

        VBox querySection = new VBox(10, queryLabel, queryArea);

        VBox buttonSection = new VBox(20,
            sectionTitle,
            salesLabel, salesButtons,
            customerLabel, customerButtons,
            employeeLabel, employeeButtons,
            inventoryLabel, inventoryButtons
        );
        buttonSection.setPadding(new Insets(20, 25, 15, 25));
        buttonSection.setStyle("-fx-background-color: white; -fx-border-color: #e1e8ed; -fx-border-width: 0 0 1 0;");

        VBox root = new VBox(0, buttonSection, querySection, queryTable, qStatus);
        root.setStyle("-fx-background-color: #f5f7fa;");
        VBox.setVgrow(queryTable, Priority.ALWAYS);
        VBox.setMargin(querySection, new Insets(20, 25, 15, 25));
        VBox.setMargin(queryTable, new Insets(0, 25, 15, 25));
        qStatus.setPadding(new Insets(12, 25, 12, 25));

        return root;
    }


    private static void runQuery(String sql, TableView<Map<String, Object>> tv, Label status,ReelsCafeApp app,TextArea queryArea) {
        try {
            queryArea.setText(sql);
            List<Map<String, Object>> data = app.dbConnection.executeQuery(sql);
            UITablesHelper.buildTableFor(tv, data);
            status.setText("Rows: " + data.size());
        } catch (SQLException e) {
            showAlert("Query Error", e.getMessage());
        }
    }


    static void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
    private static Button createStyledButton(String text, String color) {
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
    private static void openCustomerHistoryDialog(Label status,ReelsCafeApp app,TextArea queryArea) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Customer History");
        dialog.setHeaderText("View Last 3 Sales by Customer");
        dialog.setContentText("Enter Customer ID:");

        dialog.showAndWait().ifPresent(customerID -> {
            try {
                int id = Integer.parseInt(customerID);
                runQuery2(Queries2.last3SalesByCustomer(id), status,app,queryArea);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid Customer ID number.");
            }
        });
    }

    private static void openTransactionDetailsDialog(Label status,ReelsCafeApp app,TextArea queryArea) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Transaction Details");
        dialog.setHeaderText("View Detailed Transaction Information");
        dialog.setContentText("Enter Sale ID:");

        dialog.showAndWait().ifPresent(saleID -> {
            try {
                int id = Integer.parseInt(saleID);
                runQuery2(Queries2.getTransactionDetails(id), status,app,queryArea);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid Sale ID number.");
            }
        });
    }
    private static void runQuery2(String sql, Label status,ReelsCafeApp app,TextArea queryArea) {
        try {
            queryArea.setText(sql);
            List<Map<String, Object>> data = app.dbConnection.executeQuery(sql);
            showTable(data);
            status.setText("Rows: " + data.size());
        } catch (SQLException e) {
            showAlert("Query Error", e.getMessage());
        }
    }
    private static void showTable(List<Map<String, Object>> data) {
    	Stage stage = new Stage();
    	TableView<Map<String, Object>> tv = new TableView<Map<String, Object>>();
    	tv.getColumns().clear();
        tv.getItems().clear();
        if (data.isEmpty()) return;

        for (String k : data.get(0).keySet()) {
            TableColumn<Map<String, Object>, Object> c = new TableColumn<>(k);
            c.setCellValueFactory(r -> new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get(k)));
            c.setPrefWidth(150);
            tv.getColumns().add(c);
        }
        tv.getItems().addAll(data);
        stage.setTitle("Query Result");
        stage.setScene(new Scene(new VBox(tv),1000,400));
        stage.show();
    }
    
}
