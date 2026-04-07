package application;

public class Queries {
	//top 5 most sold products
	public static String q1() {
		return """
	            SELECT i.Name, i.Category,
                SUM(sd.Quantity) Sold,
                SUM(sd.Quantity * sd.SellingPrice) Revenue
         FROM SaleDetail sd
         JOIN Item i ON sd.ItemID=i.ItemID
         GROUP BY i.ItemID
         LIMIT 5
     """;
	}
	//Retrieve total amount of sales handled by each employee, sorted by employee name 
	public static String q2() {
		return "SELECT e.EmployeeID,e.Name, COUNT(DISTINCT s.SaleID) AS Transactions,SUM(sd.Quantity * sd.SellingPrice) AS Total "+
			"FROM Employee e "+
			"LEFT JOIN Sale s ON e.EmployeeID = s.EmployeeID "+
			"LEFT JOIN SaleDetail sd ON s.SaleID = sd.SaleID "+
			"GROUP BY e.EmployeeID, e.Name "+
			"ORDER BY e.Name";
	}
	//List all customers with loyalty points above 100, sorted by id.  
	public static String q3() {
		return """
        SELECT *
        FROM Customer WHERE LoyaltyPoints > 100
        Order By CustomerID
		""";
	}
	//Retrieve details of all suppliers in a specific city, sorted by name 
	public static String q4(String city) {
		return "SELECT * FROM Supplier Where City like '%"+ city +"%' ORDER BY Name";
	}
	//Retrieve top 5 products with highest profit margin
	public static String q5() {
		return "SELECT p.ItemID, i.Name, (p.SellingPrice - SUM(r.Quantity * ing.PurchasePrice)) AS Profit "+
		       "FROM Product p "+
		        "JOIN Item i ON p.ItemID = i.ItemID "+
		        "JOIN Recipe r ON p.ItemID = r.ProductID "+
		        "JOIN Ingredient ing ON r.IngredientID = ing.ItemID "+
		        "GROUP BY p.ItemID "+
		        "ORDER BY Profit DESC "+
		        "LIMIT 5 ";
	}
	//Retrieve all ingredients’ names with their available stock quantity(in-store), sorted by id
	public static String q6() {
		return  "SELECT I.Name, S.Quantity, Ing.Unit "+
				"From Item I, Stock S, Ingredient Ing "+
				"Where I.ItemID = S.ItemID AND I.ItemID = Ing.ItemID AND S.WarehouseID = 1 "+
				"ORDER BY I.ItemID";
	}
	//Retrieve all ingredients with their category that are supplied by a specific supplier, sorted by name 
	public static String q7(String supplier) {
		return  "SELECT Distinct I.Name, I.Category "+
				"FROM Item I, Purchase P, Supplier S, PurchaseDetail PD "+
				"Where I.ItemID = PD.ItemID AND S.SupplierID = P.SupplierID AND PD.PurchaseID = P.PurchaseID "+
				"AND S.Name like '%"+ supplier +"%' ORDER BY I.Name";
	}
	//retrieve ingredients used in the most popular products (most used ingredients)
	public static String q8() {
		return  "SELECT i.Name AS Ingredient, COUNT(*) AS Used "+
		        "FROM Recipe r "+
		        "JOIN Item i ON r.IngredientID = i.ItemID "+
		        "JOIN SaleDetail sd ON r.ProductID = sd.ItemID "+
		        "GROUP BY i.ItemID "+
		        "ORDER BY Used DESC "+
		        "LIMIT 5 ";
	}
	//Retrieve all sales made on a specific date, sorted by price descending
	public static String q9(String date) {
		return "SELECT * From Sale Where Date = '"+date+"' "+
				"ORDER BY ("+
						  "SELECT SUM(sd.Quantity * sd.SellingPrice) "+
						  "FROM SaleDetail sd "+
						  "WHERE sd.SaleID = Sale.SaleID "+
						") DESC";
	}
	//Retrieve the top 5 daily customers with the highest total number of purchases in the last week 
	public static String q10() {
		return 	"SELECT C.Name, COUNT(S.SaleID) AS TotalSales "+
				"FROM Customer C, Sale S WHERE S.CustomerID = C.CustomerID "+
				"AND S.Date >= CURDATE() - INTERVAL 7 DAY "+
				"GROUP BY C.CustomerID "+
				"ORDER BY TotalSales DESC LIMIT 5";
	}
	//List ingredients that need restocking (low quantity), sorted by quantity.
	public static String q11(String warehouseid) {
		return 	"SELECT I.ItemID, I.Name, S.WarehouseID,  S.Quantity, Ing.Unit FROM Item I, Stock S, Ingredient Ing "+
				"WHERE S.ItemID = I.ItemID AND Ing.ItemID = I.ItemID "+
				"AND S.WarehouseID = "+warehouseid+" AND ( "+
				"(S.Quantity <= 3 AND Ing.Unit like '%kg%') OR "+
				"(S.Quantity <= 50 AND Ing.Unit like '%pieces%' AND I.Name like '%Paper Cups%') OR "+
				"(S.Quantity <= 7 AND Ing.Unit like '%pieces%' AND I.Name not like '%Paper Cups%') OR "+
				"(S.Quantity <= 5 AND Ing.Unit like '%liter%'))"+
				"ORDER BY S.Quantity";
	}
}
