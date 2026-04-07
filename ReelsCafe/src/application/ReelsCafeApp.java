package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ReelsCafeApp extends Application {

    DatabaseConnection dbConnection;

    TableView<Map<String, Object>> tableView;
    Label statusLabel;

    List<Map<String, Object>> data = new ArrayList<>();
    ComboBox<Map<String, Object>> employees = new ComboBox<>();
    ComboBox<Map<String, Object>> customers= new ComboBox<>();
    ComboBox<Map<String, Object>> suppliers= new ComboBox<>();
    ComboBox<Map<String, Object>> warehouses = new ComboBox<>();
    ComboBox<Map<String, Object>> products = new ComboBox<>();
    ComboBox<Map<String, Object>> ingredients = new ComboBox<>();
    ComboBox<Map<String, Object>> destwarehouses = new ComboBox<>();
    ComboBox<Map<String, Object>> srcw = new ComboBox<>();
   
    
    Map<String, Tables> tables = new HashMap<>();
     Tables currentTable;
    
    DatePicker datePicker= null;
    Spinner<Integer> hourSpinner;
    Spinner<Integer> minuteSpinner;
    Spinner<Integer> secondSpinner;
    RadioButton cash;
    RadioButton credit;
    ToggleGroup group;

    boolean parentInserted = false;
    int nextId = -1;
    double finalprice = 0;
    Object Sourceid = null;
    

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        dbConnection = new DatabaseConnection();

        if (!dbConnection.testConnection()) {
            showAlert("Database Error", "Cannot connect to database");
            return;
        }
        
        initiateTables();

        BorderPane root = new BorderPane();
        root.setTop(createTopSection());
        root.setCenter(Tabs.createTabs(this));

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        stage.setTitle("Reels Café System");
        stage.setScene(scene);
        stage.show();
        
    }
    
    public void initiateTables() {
    	Tables Customer = new Tables("Customer").addPK("CustomerID").addColumn("Name").addColumn("Phone").addColumn("LoyaltyPoints");
    	Tables Employee = new Tables("Employee").addPK("EmployeeID").addColumn("Name").addColumn("Position").addColumn("Salary").addColumn("HiringDate").addColumn("Hired");
    	Tables Sale = new Tables("Sale").addPK("SaleID").addColumn("CustomerID").addColumn("EmployeeID").addColumn("Date").addColumn("PaymentMethod");
    	Tables SaleDetail = new Tables("SaleDetail").addPK("SaleID").addColumn("LineNo").addColumn("ItemID").addColumn("Quantity").addColumn("SellingPrice");
    	Tables Purchase = new Tables("Purchase").addPK("PurchaseID").addColumn("SupplierID").addColumn("EmployeeID").addColumn("WarehouseID").addColumn("Date").addColumn("Time");
    	Tables PurchaseDetail = new Tables("PurchaseDetail").addPK("PurchaseID").addPK("LineNo").addColumn("ItemID").addColumn("Quantity").addColumn("PurchasePrice");
    	Tables Transfer = new Tables("Transfer").addPK("TransferID").addColumn("SourceWarehouseID").addColumn("DestinationWarehouseID").addColumn("EmployeeID").addColumn("TransferDate");
    	Tables TransferDetail = new Tables("TransferDetail").addPK("TransferID").addPK("LineNo").addColumn("ItemID").addColumn("Quantity");
    
    	Sale.setDetailTable(SaleDetail);
    	Purchase.setDetailTable(PurchaseDetail);
    	Transfer.setDetailTable(TransferDetail);
    	
    	tables.put("Customer", Customer);
    	tables.put("Sale", Sale);
    	tables.put("Purchase", Purchase);
    	tables.put("Employee", Employee);
    	tables.put("Transfer", Transfer);
    	
    	currentTable = Customer;
    	
    	try {
	    	employees.getItems().addAll(dbConnection.executeQuery("Select EmployeeID, Name FROM Employee Where Hired = true"));
	    	employees.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	customers.getItems().addAll(dbConnection.executeQuery("Select CustomerID, Name FROM Customer"));
	    	customers.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	suppliers.getItems().addAll(dbConnection.executeQuery("Select SupplierID, Name FROM Supplier"));
	    	suppliers.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	warehouses.getItems().addAll(dbConnection.executeQuery("Select WarehouseID,Type FROM Warehouse"));
	    	warehouses.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	products.getItems().addAll(dbConnection.executeQuery("Select P.ItemID, I.Name FROM Product P, Item I Where I.ItemID = P.ItemID and P.Active = true"));
	    	products.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	ingredients.getItems().addAll(dbConnection.executeQuery("Select I.ItemID,M.Name FROM Ingredient I, Item M Where I.ItemID = M.ItemID"));
	    	ingredients.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	destwarehouses.getItems().setAll(warehouses.getItems());
	    	destwarehouses.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	srcw.getItems().setAll(warehouses.getItems());
	    	srcw.setStyle("-fx-background-color: #ffffff;-color: #bdc3c7;-fx-border-radius: 5px;-fx-font-size: 14px;-fx-text-fill: #2c3e50;");
	    	
    	}catch(SQLException e) {
    		showAlert("Error",e.getMessage());
    	}
    }
    
    //Top
    private VBox createTopSection() {
        Label title = new Label("Reels Café Database System");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Complete Database Solution for Café Operations");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #ecf0f1; -fx-font-weight: 400;");

        VBox titleBox = new VBox(5, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        VBox box = new VBox(titleBox);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50 0%, #34495e 100%); -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");
        return box;
    }

    void openDetailsWindow(Tables parent, Map<String, Object> parentRow) {

        Tables detail = parent.getDetailTable();
        String parentPK = parent.getPrimaryKey().get(0);
        Object parentId = parentRow.get(parentPK);

        Stage stage = new Stage();
        stage.setTitle(parent.getTableName() + " Details");

        TableView<Map<String, Object>> detailTableView = new TableView<>();
        Label status = new Label();

        Runnable reloadDetails = () -> {
            try {
            	String sql = "";
            	if(detail.getTableName().equalsIgnoreCase("SaleDetail")) {
                 sql ="SELECT SaleID, LineNo, ItemID, Quantity, SellingPrice, " +
                		    "Quantity * SellingPrice AS Subtotal " +
                		    "FROM SaleDetail WHERE SaleID = ?";
            	}else if(detail.getTableName().equalsIgnoreCase("PurchaseDetail")) {
            		sql = "SELECT PurchaseID, LineNo, ItemID, Quantity, PurchasePrice, " +
            			    "Quantity * PurchasePrice AS Subtotal " +
            			    "FROM PurchaseDetail WHERE PurchaseID = ?";
            	}else {
            		sql = "SELECT TransferID, LineNo, ItemID, Quantity "+
            			    "FROM TransferDetail WHERE TransferID = ?";
            	}

                List<Map<String, Object>> rows =
                        dbConnection.executeQuery(sql, parentId);

                UITablesHelper.buildTableFor(detailTableView, rows);
                status.setText("Rows: " + rows.size());

            } catch (SQLException e) {
                showAlert("Load Error", e.getMessage());
            }
        };
        reloadDetails.run();

        VBox root = new VBox(10, detailTableView, status);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 900, 500));
        stage.show();
    }
   
    //form window
    void openForm(Map<String, Object> data) {
    	
    	try {
	    	employees.getItems().setAll(dbConnection.executeQuery("Select EmployeeID, Name FROM Employee Where Hired = true"));
	    	customers.getItems().setAll(dbConnection.executeQuery("Select CustomerID, Name FROM Customer"));
	    	suppliers.getItems().setAll(dbConnection.executeQuery("Select SupplierID, Name FROM Supplier"));
	    	warehouses.getItems().setAll(dbConnection.executeQuery("Select WarehouseID,Type FROM Warehouse"));
	    	products.getItems().setAll(dbConnection.executeQuery("Select P.ItemID, I.Name FROM Product P, Item I Where I.ItemID = P.ItemID and P.Active = true"));
	    	ingredients.getItems().setAll(dbConnection.executeQuery("Select I.ItemID,M.Name FROM Ingredient I, Item M Where I.ItemID = M.ItemID"));
	    	destwarehouses.getItems().setAll(warehouses.getItems());
	    	srcw.getItems().setAll(warehouses.getItems());
	    	
    	}catch(SQLException e) {
    		
    	}
    	 if (data==null && (currentTable.getTableName().equals("Sale") || currentTable.getTableName().equals("Purchase")||currentTable.getTableName().equals("Transfer"))) {
    	        try {
    	            String pk = currentTable.getPrimaryKey().get(0);
    	            nextId = Integer.parseInt(dbConnection.executeQuery("SELECT MAX(" + pk + ")+1 AS NextID FROM " + currentTable.getTableName()).get(0).get("NextID").toString());

    	            //insert placeholder row
    	            String sql = "";
    	            if (currentTable.getTableName().equals("Sale")) {
    	                sql = "INSERT INTO Sale(SaleID, CustomerID, EmployeeID, Date, PaymentMethod) " +
    	                        "VALUES (?, 1, 1, CURDATE(), 'N/A')";//this is a temp placeholder for sale,if we proceed it updated the values and if we dont it removes it later
    	            } else if (currentTable.getTableName().equals("Purchase")) {
    	                sql = "INSERT INTO Purchase(PurchaseID, SupplierID, EmployeeID, WarehouseID, Date, Time) " +
    	                        "VALUES (?, 1, 1, 1, CURDATE(), CURTIME())";
    	            }
    	            else{
    	            	sql = "INSERT INTO Transfer(TransferID,SourceWarehouseID,DestinationWarehouseID,EmployeeID,TransferDate) "+
    	            			"VALUES (?,1,2,1,CURDATE())";
    	            }
    	            dbConnection.executeUpdate(sql, nextId);
    	            if (currentTable.getTableName().equals("Sale")) {
    	                SaleDetailHandler.openSaleDetailsWindow(nextId,dbConnection); //SaleDetail window
    	                tableView.refresh();
    	            } else if(currentTable.getTableName().equals("Purchase")){
    	                PurchaseDetailHandler.openPurchaseDetailsWindow(nextId,dbConnection);
    	                tableView.refresh();//PurchaseDetail window
    	            }

    	        } catch (SQLException e) {
    	            showAlert("Insert Error", "Could not create placeholder: " + e.getMessage());
    	            return;
    	        }
    	    }

        Stage stage = new Stage();
        stage.setTitle(data == null ? "Add" : "Update");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Map<String, TextField> fields = new HashMap<>();
        Map<String, ComboBox> boxes = new HashMap<>();

        int row = 0;

        for (String c : currentTable.getColumns()) {
        	
            Label l = new Label(c);
            l.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;-fx-text-fill: #2c3e50;");
            
            if(c.equalsIgnoreCase(currentTable.getPrimaryKey().get(0))||c.equalsIgnoreCase("Hired")) {
            	continue;
            }
            if(c.equalsIgnoreCase("Time")) {
            	hourSpinner = new Spinner<>(0, 23, 12);
                minuteSpinner = new Spinner<>(0, 59, 0);
                secondSpinner = new Spinner<>(0, 59, 0);

                hourSpinner.setEditable(true);
                minuteSpinner.setEditable(true);
                secondSpinner.setEditable(true);
                
                if(data == null) {
                	hourSpinner.getValueFactory().setValue(LocalTime.now().getHour());
                	minuteSpinner.getValueFactory().setValue(LocalTime.now().getMinute());
                	secondSpinner.getValueFactory().setValue(LocalTime.now().getSecond());
                }else {
                	String[] time = data.get(c).toString().split(":");
                	int hour = Integer.parseInt(time[0].trim());
                	int min = Integer.parseInt(time[1].trim());
                	int sec = Integer.parseInt(time[2].trim());
                	hourSpinner.getValueFactory().setValue(hour);
                	minuteSpinner.getValueFactory().setValue(min);
                	secondSpinner.getValueFactory().setValue(sec);
                }
                grid.add(l, 0, row);
                HBox timebox = new HBox(5,hourSpinner,minuteSpinner,secondSpinner);
            	grid.add(timebox, 1, row++);
                continue;
            }
            if(c.equalsIgnoreCase("PaymentMethod")) {
            	cash = new RadioButton("Cash");
            	credit = new RadioButton("Credit Card");
            	group = new ToggleGroup();
            	group.getToggles().addAll(cash,credit);
            	cash.setToggleGroup(group);
            	credit.setToggleGroup(group);
            	HBox payment = new HBox(5,cash,credit);
            	if(data != null) {
            		if(data.get(c).toString().equals("Cash")) {
            			group.selectToggle(cash);
            		}else if(data.get(c).toString().equals("Credit Card")) {
            			group.selectToggle(credit);
            		}
            	}
            	grid.add(l, 0, row);
            	grid.add(payment, 1, row++);
            	continue;
            }
            if(c.equalsIgnoreCase("Date")||c.equalsIgnoreCase("HiringDate")||c.equalsIgnoreCase("TransferDate")) {
            	datePicker = new DatePicker();
            	if(data == null) {
            		datePicker.setValue(LocalDate.now());
            	}else {
            		LocalDate date = LocalDate.parse(data.get(c).toString());
            		datePicker.setValue(date);
            	}
            	grid.add(l, 0, row);
            	grid.add(datePicker, 1, row++);
            }
            else if(!c.equalsIgnoreCase("CustomerID")&&!c.equalsIgnoreCase("SupplierID")&&!c.equalsIgnoreCase("EmployeeID")&&!c.equalsIgnoreCase("WarehouseID")&&!c.equalsIgnoreCase("SourceWarehouseID")&&!c.equalsIgnoreCase("DestinationWarehouseID")) {
            	TextField text = new TextField();
            	text.setStyle("-fx-font-size: 14px; -fx-padding: 5 10 5 10;-fx-border-color: #2c3e50; -fx-border-radius: 5px; ");

                if (data != null) text.setText(data.get(c).toString());
                
                fields.put(c, text);
                grid.add(l, 0, row);
                grid.add(text, 1, row++);
            }
            //combo boxes
            else {
            	grid.add(l, 0, row);
            	//handling customers box
            	if(c.equalsIgnoreCase("CustomerID")) {
            		if(data != null) {
            			Object id = data.get("CustomerID");
            			for (Map<String,Object> item : customers.getItems()) {
            			    if (item.get("CustomerID").equals(id)) {
            			        customers.getSelectionModel().select(item);
            			        break;
            			    }
            			}

            		}
            		//add to grid
                    grid.add(customers, 1, row++);
                    boxes.put(c, customers);
                    
            	}if(c.equalsIgnoreCase("EmployeeID")) {
            		if(data != null) {
            			Object id = data.get("EmployeeID");
            			for (Map<String,Object> item : employees.getItems()) {
            			    if (item.get("EmployeeID").equals(id)) {
            			        employees.getSelectionModel().select(item);
            			        break;
            			    }
            			}
            		}
            		//add to grid
                    grid.add(employees, 1, row++);
                    boxes.put(c, employees);
            	}if(c.equalsIgnoreCase("SupplierID")) {
            		if(data != null) {
            			Object id = data.get(c); //SupplierID

            			for (Map<String, Object> item : suppliers.getItems()) {
            			    if (item.get("SupplierID").equals(id)) {
            			        suppliers.getSelectionModel().select(item);
            			        break;
            			    }
            			}
            		}
            		//add to grid
                    grid.add(suppliers, 1, row++);
                    boxes.put(c, suppliers);
            	}if(c.equalsIgnoreCase("WarehouseID")) {
            		if(data != null) {
            			Object id = data.get(c);
            			for (Map<String,Object> item : warehouses.getItems()) {
            			    if (item.get("WarehouseID").equals(id)) {
            			        warehouses.getSelectionModel().select(item);
            			        break;
            			    }
            			}
            		}
            		//add to grid
                    grid.add(warehouses, 1, row++);
                    boxes.put(c, warehouses);
            	}
            	if(c.equals("SourceWarehouseID")) {
            		if(data != null) {
            			Object id = data.get(c);
            			
            			for (Map<String,Object> item : srcw.getItems()) {
            			    if (item.get("WarehouseID").equals(id)) {
            			        srcw.getSelectionModel().select(item);
            			        break;
            			    }
            			}
            		}
            		//add to grid
                    grid.add(srcw, 1, row++);
                    boxes.put(c, srcw);
            	}
            	if(c.equals("DestinationWarehouseID")) {
            		if(data != null) {
            			Object id = data.get(c);
            			
            			for (Map<String,Object> item : destwarehouses.getItems()) {
            			    if (item.get("WarehouseID").equals(id)) {
            			        destwarehouses.getSelectionModel().select(item);
            			        break;
            			    }
            			}
            		}
            		//add to grid
                    grid.add(destwarehouses, 1, row++);
                    boxes.put(c, destwarehouses);
            	}
            }
        }
        srcw.setOnAction(e -> {
            Map<String, Object> selected =
                (Map<String, Object>) srcw.getSelectionModel().getSelectedItem();

			if (selected == null) {
                Sourceid = null;
                return;
            }

            Sourceid = ((Number) selected.get("WarehouseID")).intValue();
            TransferDetailHandler.openTransferDetailsWindow(nextId,dbConnection,Sourceid);
            tableView.refresh();//TransferDetail window
        });


        Button save = createStyledButton("Save");
        save.setOnAction(e -> {
            try {
            	//validating and checking for nulls in text fields
            	if (data==null && (currentTable.getTableName().equals("Sale") || currentTable.getTableName().equals("Purchase")|| currentTable.getTableName().equals("Transfer"))) {
	
	               if (!hasDetails(currentTable, nextId)) {
	                   showAlert("Save Error", "You must add at least one item to the cart.");
	                   return;
	               }
	            }
            	if(fields.containsKey("Name")) {
            		boolean correct = validateName(fields.get("Name").getText());
            		if(!correct) {
            			showAlert("Save Error", "Invalid value for Name");
 	                    return;
            		}
            	}
            	if(fields.containsKey("Phone")) {
            		boolean correct = validatePhone(fields.get("Phone").getText());
            		if(!correct) {
            			showAlert("Save Error", "Invalid value for phone number");
 	                    return;
            		}
            	}
            	if(fields.containsKey("Salary")) {
            		boolean correct = validateNum(fields.get("Salary").getText());
            		if(!correct) {
            			showAlert("Save Error", "Invalid value for salary");
 	                    return;
            		}
            	}
            	if(fields.containsKey("LoyaltyPoints")) {
            		boolean correct = validateNum(fields.get("LoyaltyPoints").getText());
            		if(!correct) {
            			showAlert("Save Error", "Invalid value for points");
 	                    return;
            		}
            	}
            	if(fields.containsKey("Position")) {
            		boolean correct = validatePosition(fields.get("Position").getText());
            		if(!correct) {
            			showAlert("Save Error", "Invalid value for position");
 	                    return;
            		}
            	}
            	//getting values of other items than text fields
            	//lists
            	for (String key : boxes.keySet()) {
                    Map<String, Object> selected = (Map<String, Object>) boxes.get(key).getSelectionModel().getSelectedItem();
                    if (selected == null) {
                        showAlert("Insert Error", "Please select a value for " + key);
                        return;
                    }
                    Object value;
                    if (key.equals("SourceWarehouseID") || key.equals("DestinationWarehouseID")) {
                        value = selected.get("WarehouseID");
                    } else {
                        value = selected.get(key);
                    }
                    if (value == null) {
                        showAlert("Insert Error", "Invalid selection for " + key);
                        return;
                    }

                    fields.put(key, new TextField(value.toString()));
                }
            	//date and time
            	if(datePicker != null) {
            		if(datePicker.getValue().isAfter(LocalDate.now())) {
            			showAlert("Insert Error", "Date Can't be in the future!");
	                    return;
            		}
            		if(currentTable.getTableName().equalsIgnoreCase("Employee")) {
            			fields.put("HiringDate", new TextField(datePicker.getValue().toString()));
            		}
            		else if(currentTable.getTableName().equalsIgnoreCase("Transfer")) {
            			fields.put("TransferDate", new TextField(datePicker.getValue().toString()));
            		}else{
            			fields.put("Date", new TextField(datePicker.getValue().toString()));
            		}
            	}
            	if(currentTable.getColumns().contains("Time")&&hourSpinner != null && minuteSpinner != null && secondSpinner != null) {
            		LocalTime selectedTime = LocalTime.of(hourSpinner.getValue(),minuteSpinner.getValue(),secondSpinner.getValue());
            		fields.put("Time", new TextField(selectedTime.toString()));
            	}
            	//payment
            	if(currentTable.getColumns().contains("PaymentMethod")&& group != null && group.getSelectedToggle() != null) {
            		if(group.getSelectedToggle()== null) {
            			showAlert("Insert Error", "Please select payment method");
                        return;
            		}
            		fields.put("PaymentMethod", new TextField(group.getSelectedToggle()==cash?"Cash":"Credit Card"));
            	}
            	
            	//inserting or updating
            	if (data==null) {
            		if(currentTable.getTableName().equals("Sale")|| currentTable.getTableName().equals("Purchase")||currentTable.getTableName().equals("Transfer")) {
	                    //here we update the place holder
            			StringBuilder sql = new StringBuilder("UPDATE " + currentTable.getTableName() + " SET ");
	                    for (String col : fields.keySet()) sql.append(col).append("=?, ");
	                    sql.setLength(sql.length() - 2); // remove last comma
	                    sql.append(" WHERE ").append(currentTable.getPrimaryKey().get(0)).append("=?");
	
	                    List<Object> params = new ArrayList<>();
	                    fields.values().forEach(tf -> {
	                    	params.add(tf.getText());
	                    });
	                    params.add(nextId);
	
	                    dbConnection.executeUpdate(sql.toString(), params.toArray());
	                    
	                    //update stock
	                    if(currentTable.getTableName().equalsIgnoreCase("Sale")) {
	                    	//we get the items in sale
	                    	List<Map<String,Object>> products = dbConnection.executeQuery("Select * From SaleDetail Where SaleID = ?",nextId);
	                    	for(Map<String,Object> item : products) {
	                    		int quantity = ((Number)item.get("Quantity")).intValue();
	                    		//get recipe for each item
	                    		List<Map<String,Object>> recipe = dbConnection.executeQuery("Select * From Recipe Where ProductID = ?",item.get("ItemID"));
		                        for(Map<String,Object> ing : recipe) {
		                            double used = Double.parseDouble(ing.get("Quantity").toString()) * quantity;
		                            int ingId = ((Number) ing.get("IngredientID")).intValue();
		                            //finally we update the stock in store
		                            dbConnection.executeUpdate("UPDATE Stock SET Quantity = Quantity - ? WHERE WarehouseID = 1 AND ItemID=?",used, ingId);
		                        }
	                    	}
	                        
	                    }
	                    else if(currentTable.getTableName().equalsIgnoreCase("Purchase")) {
	                    	Object warehouse = dbConnection.executeQuery("Select * From Purchase Where PurchaseID = ?",nextId).get(0).get("WarehouseID");
	                    	
	                    	//we get ingredients in purchase
	                    	List<Map<String,Object>> ings = dbConnection.executeQuery("Select * From PurchaseDetail Where PurchaseID = ?",nextId);
	                    	for (Map<String, Object> ing : ings) {
	
	                    	    Number qtyNum = (Number) ing.get("Quantity");
	                    	    double quantity = qtyNum.doubleValue();
	
	                    	    int ingId = ((Number) ing.get("ItemID")).intValue();
	                    	    //check if item already has a stock record or not
	                    	    if(dbConnection.executeQuery("Select Quantity From Stock Where WarehouseID = ? And ItemID = ?", warehouse,ingId).isEmpty()) {
	                    	    	dbConnection.executeUpdate("Insert Into Stock (WarehouseID,ItemID,Quantity) Values (?,?,?)", warehouse,ingId,quantity);
	                    	    }else {
		                    	    dbConnection.executeUpdate(
		                    	        "UPDATE Stock SET Quantity = Quantity + ? WHERE WarehouseID = ? AND ItemID=?",
		                    	        quantity, warehouse, ingId
		                    	    );
	                    	    }
	                    	}
	                    }
	                    else {
	                    	Map<String,Object> warehouses  = dbConnection.executeQuery("Select SourceWarehouseID, DestinationWarehouseID From Transfer Where TransferID = ?",nextId).get(0);
	                    	Object source = warehouses.get("SourceWarehouseID");
	                    	Object dest = warehouses.get("DestinationWarehouseID");
	                    	if(source.equals(dest)) {
	                    		showAlert("Transfer Error!","Source cant be the same as Destination");
	                    		return;
	                    	}
	                    	List<Map<String,Object>> ings = dbConnection.executeQuery("Select * From TransferDetail Where TransferID = ?",nextId);
	                    	for (Map<String, Object> ing : ings) {
	                    		
	                    	    Number qtyNum = (Number) ing.get("Quantity");
	                    	    double quantity = qtyNum.doubleValue();
	                    	    
	                    	    int ingId = ((Number) ing.get("ItemID")).intValue();
	                    	    
	                    	    Object expdate = dbConnection.executeQuery("Select * From Stock Where WarehouseId = ? And ItemID = ? ", source,ingId).get(0).get("ExpirationDate");
	
	                    	  //check if item already has a stock record or not
	                    	    if(dbConnection.executeQuery("Select Quantity From Stock Where WarehouseID = ? And ItemID = ?", dest,ingId).isEmpty()) {
	                    	    	dbConnection.executeUpdate("Insert Into Stock (WarehouseID,ItemID,Quantity,ExpirationDate) Values (?,?,?,?)", dest,ingId,quantity,expdate);
	                    	    }
	                    	    else {
	                    	    	dbConnection.executeUpdate("UPDATE Stock SET Quantity = Quantity + ? WHERE WarehouseID = ? AND ItemID=?", quantity, dest, ingId);
	                    	    }
	                    	    dbConnection.executeUpdate(
		                    	        "UPDATE Stock SET Quantity = Quantity - ? WHERE WarehouseID = ? AND ItemID=?",
		                    	        quantity, source, ingId
		                    	 );
	                    	}
	                    }
	                    //other tables
            		}else {
            	        StringBuilder sql = new StringBuilder(
            	                "INSERT INTO " + currentTable.getTableName() + " ("
            	            );

            	        for (String col : fields.keySet()) sql.append(col).append(",");

            	        sql.setLength(sql.length() - 1);
            	        sql.append(") VALUES (");

            	        for (int i = 0; i < fields.size(); i++) sql.append("?,");

            	        sql.setLength(sql.length() - 1);
            	        sql.append(")");

            	        List<Object> params = new ArrayList<>();
            	        fields.values().forEach(tf -> params.add(tf.getText()));

            	        dbConnection.executeUpdate(sql.toString(), params.toArray());
            	    }
                } else {
                    updateRow(fields, data);
                }

            	UITablesHelper.loadTable(currentTable,this);
                statusLabel.setText("Sale Added!");
                stage.close();
            } catch (Exception ex) {
                showAlert("Form Error", ex.getMessage());
            }
        });
        stage.setOnCloseRequest(e -> {
            if (data == null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel this record?");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        String sql = "DELETE FROM " + currentTable.getTableName() + " WHERE " +currentTable.getPrimaryKey().get(0) + "=?";
                        dbConnection.executeUpdate(sql, nextId);
                    } catch (SQLException ex) {
                        showAlert("Delete Error", ex.getMessage());
                    }
                } else {
                    e.consume(); //cancel closing
                }
            }
        });

        VBox root = new VBox(15, grid, save);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15));

        stage.setScene(new Scene(root, 700, 400));
        stage.show();
    }
    //helpers----------------------------------------------------------------------------------
    private void updateRow(Map<String, TextField> fields, Map<String, Object> data)
            throws SQLException {

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(currentTable.getTableName()).append(" SET ");

        for (String c : fields.keySet())
            sql.append(c).append("=?, ");

        sql.setLength(sql.length() - 2);//to remove last comma
        sql.append(" WHERE ").append(currentTable.getPrimaryKey().get(0)).append("=?");

        List<Object> params = new ArrayList<>();
        fields.values().forEach(tf -> params.add(tf.getText()));
        params.add(data.get(currentTable.getPrimaryKey().get(0)));

        dbConnection.executeUpdate(sql.toString(), params.toArray());
        UITablesHelper.loadTable(currentTable,this);
    }

    void searchByPK() {

    	String pk = currentTable.getPrimaryKey().get(0);

        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Enter " + pk);

        d.showAndWait().ifPresent(id -> {
            for (int i = 0; i < data.size(); i++) {

                Object value = data.get(i).get(pk);

                if (value != null && value.toString().equals(id)) {
                    tableView.getSelectionModel().select(i);
                    tableView.scrollTo(i);
                    return;
                }
            }
            statusLabel.setText("Not found");
        });
    }
    private boolean hasDetails(Tables parent, int id) throws Exception {
        String sql = parent.getTableName().equals("Sale")
                ? "SELECT COUNT(*) C FROM SaleDetail WHERE SaleID=?"
                : (parent.getTableName().equals("Purchase")?"SELECT COUNT(*) C FROM PurchaseDetail WHERE PurchaseID=?":"SELECT COUNT(*) C FROM TransferDetail WHERE TransferID=?");

        return Integer.parseInt(
                dbConnection.executeQuery(sql, id).get(0).get("C").toString()) > 0;
    }

    Button createStyledButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:#2C3E50;-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        b.setCursor(javafx.scene.Cursor.HAND);
        return b;
    }

    void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
    
    private boolean validateName(String input) {
    	return input.matches("[A-Za-z ]+");
    }
    private boolean validateNum(String input) {
    	return input.matches("\\d+(\\.\\d{1,2})?");
    }
    private boolean validatePhone(String input) {
    	return input.matches("\\d{10}");
    }
    private boolean validatePosition(String input){
    	return input.matches("Manager")||input.matches("Cashier")||input.matches("Barista")||input.matches("Cleaning");
    }
}
