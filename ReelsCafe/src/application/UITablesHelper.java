package application;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class UITablesHelper {
	static void loadTable(Tables table,ReelsCafeApp app) {
        try {
        	String sql;

            if (table.getTableName().equalsIgnoreCase("Sale")) {
                sql = "SELECT s.*, SUM(sd.Quantity * sd.SellingPrice) AS TotalPrice "+
                    "FROM Sale s "+
                    "LEFT JOIN SaleDetail sd ON s.SaleID = sd.SaleID "+
                    "GROUP BY s.SaleID ";
            }
            else if (table.getTableName().equalsIgnoreCase("Purchase")) {
                sql = "SELECT p.*, SUM(pd.Quantity * pd.PurchasePrice) AS TotalPrice "+
                    "FROM Purchase p "+
                    "LEFT JOIN PurchaseDetail pd ON p.PurchaseID = pd.PurchaseID "+
                    "GROUP BY p.PurchaseID ";
            }
            else {
                sql = "SELECT * FROM " + table.getTableName();
            }

            app.data = app.dbConnection.executeQuery(sql.toString());
            buildTable(app);

            app.statusLabel.setText("Rows: " + app.data.size());

        } catch (SQLException e) {
            app.showAlert("Load Error", e.getMessage());
        }
    }
	  
    private static void buildTable(ReelsCafeApp app) {

        app.tableView.getColumns().clear();
        app.tableView.getItems().clear();

        if (app.data.isEmpty()) return;

        for (String colName : app.currentTable.getColumns()) {
            TableColumn<Map<String, Object>, Object> col = new TableColumn<>(colName);
            col.setCellValueFactory(r ->
                    new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get(colName)));
            app.tableView.getColumns().add(col);
        }
        if (app.currentTable.getTableName().equalsIgnoreCase("Sale") ||app.currentTable.getTableName().equalsIgnoreCase("Purchase")) {
        	TableColumn<Map<String, Object>, Object> col = new TableColumn<>("TotalPrice");
            col.setCellValueFactory(r ->
                    new javafx.beans.property.SimpleObjectProperty<>(r.getValue().get("TotalPrice")));
            app.tableView.getColumns().add(col);
        }

        app.tableView.getItems().addAll(app.data);
    }

    static void buildTableFor(TableView<Map<String, Object>> tv, List<Map<String, Object>> data) {
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
    }
}
