package application;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AdminStats {
	public static GridPane stats(DatabaseConnection con) {
		
		//1: monthly sales
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Day");
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Revenue");
		LineChart<String, Number> salesChart = new LineChart<>(xAxis, yAxis);
		salesChart.setTitle("📈 Sales Chart for month: "+LocalDate.now().getMonth()+" "+LocalDate.now().getYear());//standard this month
		DatePicker datePicker = new DatePicker(LocalDate.now());


		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Daily Revenue For this month");

		try {
			List<Map<String,Object>> data = monthlySales(con,datePicker.getValue().getMonthValue(),datePicker.getValue().getYear());
			for (Map<String,Object> row : data) {
			    series.getData().add(new XYChart.Data<>(row.get("Day").toString(),(Number) row.get("Total")));
			}
		}catch(Exception e) {
			showAlert("Error in loading",e.getMessage());
		}
		salesChart.getData().add(series);
		datePicker.setOnAction(e->{
			series.getData().clear();

			LocalDate d = datePicker.getValue();
		    int month = d.getMonthValue();
		    int year = d.getYear();

		    salesChart.setTitle("📈 Sales Chart for month:" + d.getMonth() + " " + year);

		    try {
		        for (Map<String,Object> row : monthlySales(con, month, year)) {
		            series.getData().add(
		                new XYChart.Data<>(row.get("Day").toString(),(Number) row.get("Total"))
		            );
		        }
		    } catch (Exception ex) {
		        showAlert("Error", ex.getMessage());
		    }
		});
		VBox salesBox = new VBox(5, datePicker, salesChart);
		salesBox.setStyle("-fx-border-color: grey;-fx-border-width:2");

		
		//2: urgent stock----------------------------------------------------------------------------
		TableView<Map<String,Object>> alertTable = new TableView<>();
		alertTable.setPlaceholder(new Label("No critical stock 🚀"));
		TableColumn<Map<String,Object>, Object> itemCol = new TableColumn<>("Item");
		itemCol.setCellValueFactory(d ->new SimpleObjectProperty<>(d.getValue().get("Name")));
		TableColumn<Map<String,Object>, Object> qtyCol = new TableColumn<>("Qty");
		qtyCol.setCellValueFactory(d ->new SimpleObjectProperty<>(d.getValue().get("Quantity")));
		TableColumn<Map<String,Object>, Object> whCol = new TableColumn<>("Warehouse");
		whCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().get("Warehouse")));

		alertTable.getColumns().addAll(itemCol, qtyCol, whCol);
		
		Runnable reloadStock = () -> {
		    try {
		        alertTable.getItems().setAll(UrgentStock(con));
		    } catch (Exception e) {
		        showAlert("Error", e.getMessage());
		    }
		    if(alertTable.getItems().isEmpty()) {
				alertTable.setStyle("-fx-border-color: green;-fx-border-width: 2;-fx-background-color: #fff5f5");
			}
			else {
				alertTable.setStyle("-fx-border-color: red;-fx-border-width: 2;-fx-background-color: #fff5f5");
			}
		};
		reloadStock.run();
		Button refreshBtn = new Button("🔄 Refresh");
		refreshBtn.setOnAction(e-> reloadStock.run());
		
		Label alertTitle = new Label("🚨 Critical Stock Alerts 🚨");
		alertTitle.setStyle("-fx-text-fill: red;-fx-font-size:14px;");

		VBox alertBox = new VBox(8, alertTitle,refreshBtn, alertTable);
		alertBox.setAlignment(Pos.CENTER);
		
		
		//3:most sold products--------------------------------------------------------------------------------------------
		BarChart<String, Number> topProductsChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
		topProductsChart.setTitle("Top Selling Products");
		DatePicker topProductsPicker = new DatePicker(LocalDate.now());


		XYChart.Series<String, Number> Series = new XYChart.Series<>();
		try {
			for (Map<String,Object> row : topProducts(con,topProductsPicker.getValue())) {
			    Series.getData().add(new XYChart.Data<>(row.get("Name").toString(),(Number) row.get("Sold")));
			}
		} catch (Exception e) {
			showAlert("Error in loading",e.getMessage());
		}
		topProductsChart.getData().add(Series);
		topProductsPicker.setOnAction(e->{
			 Series.getData().clear();

			    LocalDate date = topProductsPicker.getValue();
			    topProductsChart.setTitle("Top Products – " + date);

			    try {
			        for (Map<String,Object> row : topProducts(con, date)) {
			            Series.getData().add(new XYChart.Data<>(row.get("Name").toString(),(Number) row.get("Sold")));
			        }
			    } catch (Exception ex) {
			        showAlert("Error", ex.getMessage());
			    }
		});
		VBox topProductsBox = new VBox(8, topProductsPicker, topProductsChart);
		topProductsBox.setAlignment(Pos.CENTER);

		
		//all sales per category(to see most ordered categories)------------------------------------------------------
		CategoryAxis x = new CategoryAxis();
		x.setLabel("Category");
		NumberAxis y = new NumberAxis();
		y.setLabel("Revenue");

		BarChart<String, Number> categoryChart = new BarChart<>(x, y);
		categoryChart.setTitle("Revenue by Category");

		XYChart.Series<String, Number> s = new XYChart.Series<>();
		s.setName("Total Revenue");

		try {
			List<Map<String, Object>> data = categorySales(con);
			for (Map<String, Object> row : data) {
			    s.getData().add(new XYChart.Data<>(row.get("Category").toString(),((Number) row.get("Revenue")).doubleValue()));
			}
		}
		catch(Exception e) {
			showAlert("Error in loading",e.getMessage());
		}
		categoryChart.getData().add(s);
		
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(15));
		grid.setHgap(15);
		grid.setVgap(15);

		grid.add(salesBox, 0, 0);
		grid.add(alertBox, 1, 0);
		grid.add(topProductsBox, 0, 1);
		grid.add(categoryChart, 1, 1);

		return grid;
	}
	public static List<Map<String,Object>> monthlySales(DatabaseConnection con, int month, int year) throws Exception{
		String sql = "SELECT s.Date AS Day, SUM(sd.Quantity * sd.SellingPrice) AS Total "
				+ "FROM Sale s JOIN SaleDetail sd ON s.SaleID = sd.SaleID "
				+ "WHERE MONTH(s.Date) = ? "
				+ " AND YEAR(s.Date) = ? "
				+ "GROUP BY s.Date "
				+ "ORDER BY Day ";
		return con.executeQuery(sql, month,year);
	}
	public static List<Map<String,Object>> UrgentStock(DatabaseConnection con) throws Exception{
		String sql = "SELECT i.Name, S.Quantity, w.Location AS Warehouse "
				+ "FROM Stock S "
				+ "JOIN Item i ON S.ItemID = i.ItemID "
				+ "JOIN Warehouse w ON S.WarehouseID = w.WarehouseID "
				+ "JOIN Ingredient Ing ON Ing.ItemID = i.ItemID "
				+ "WHERE (S.Quantity <= 3 AND Ing.Unit like '%kg%') OR "
				+ "		 (S.Quantity <= 50 AND Ing.Unit like '%pieces%' AND (i.Name like '%Paper Cups%' OR i.Name like '%Plastic Cups%' OR i.Name like '%Straws%' )) OR "
				+ "		 (S.Quantity <= 7 AND Ing.Unit like '%pieces%' AND (i.Name not like '%Paper Cups%' AND i.Name not like '%Plastic Cups%' AND i.Name not like '%Straws%' )) OR "
				+ "		 (S.Quantity <= 5 AND Ing.Unit like '%liter%') "
				+ "ORDER BY S.Quantity";
		return con.executeQuery(sql, null);
	}
	public static List<Map<String,Object>> topProducts(DatabaseConnection con,LocalDate date) throws Exception{
		String sql =  "SELECT i.Name, SUM(sd.Quantity) AS Sold " +
		        "FROM Sale s " +
		        "JOIN SaleDetail sd ON s.SaleID = sd.SaleID " +
		        "JOIN Item i ON sd.ItemID = i.ItemID " +
		        "WHERE s.Date = ? " +
		        "GROUP BY i.ItemID " +
		        "ORDER BY Sold DESC " +
		        "LIMIT 5";
		return con.executeQuery(sql, date);
	}
	public static List<Map<String,Object>> categorySales(DatabaseConnection con) throws Exception{
		String sql = "SELECT i.Category, SUM(sd.Quantity * sd.SellingPrice) AS Revenue "
				+ "FROM SaleDetail sd "
				+ "JOIN Item i ON sd.ItemID = i.ItemID "
				+ "WHERE i.Category not like '%Supplies%' "
				+ "GROUP BY i.Category; ";
		return con.executeQuery(sql, null);
	}
	
	static void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
