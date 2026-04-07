package application;

public class Queries2 {
   // Total sales per day in the last 7 days
    public static String totalSalesPerDayLastWeek() {
        return 
           " SELECT DATE(Date) as SaleDate, "+
                  " COUNT(SaleID) as TotalTransactions, "+
                  " (SELECT COALESCE(SUM(Quantity * SellingPrice), 0) "+
                   " FROM SaleDetail "+
                   " WHERE SaleDetail.SaleID IN "+
                      "  (SELECT SaleID FROM Sale WHERE DATE(Date) = DATE(Sale.Date))) as TotalSales "+
           " FROM Sale "+
           " WHERE Date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) "+
           " GROUP BY DATE(Date) "+
           " ORDER BY SaleDate DESC "
        ;
    }
 // Top 5 customers who bought coffee 
    public static String top5CoffeeCustomers() {
        return 
           " SELECT CustomerID, "+
                  " (SELECT Name FROM Customer WHERE Customer.CustomerID = Sale.CustomerID) as Name, "+
                  " (SELECT Phone FROM Customer WHERE Customer.CustomerID = Sale.CustomerID) as Phone, "+
                  " COUNT(DISTINCT SaleID) as CoffeePurchases, "+
                  " (SELECT SUM(sd.Quantity * sd.SellingPrice) "+
                  "  FROM SaleDetail sd "+
                  "  WHERE sd.SaleID IN (SELECT s2.SaleID FROM Sale s2 WHERE s2.CustomerID = Sale.CustomerID) "+
                    "  AND sd.ItemID IN (SELECT ItemID FROM Item WHERE Category like '%Coffee%')) as TotalSpent "+
           " FROM Sale "+
           " WHERE CustomerID IN "+
             "   (SELECT DISTINCT s.CustomerID  "+
               "  FROM Sale s, SaleDetail sd, Item i "+ 
              "   WHERE s.SaleID = sd.SaleID "+
               "    AND sd.ItemID = i.ItemID "+
                "   AND i.Category like '%Coffee%') "+
           " GROUP BY CustomerID "+
           " ORDER BY TotalSpent DESC "
        ;
    }
// Average salary of all employees
    public static String averageSalaryByHiringDate() {
        return 
            " SELECT Name, Position, Salary, HiringDate, "+
                  " (SELECT AVG(Salary) FROM Employee e2 WHERE e2.HiringDate <= Employee.HiringDate) as AverageSalary "+
           " FROM Employee "+
           " ORDER BY HiringDate "
        ;
    }
 // Total sales and transactions per employee 
    public static String employeeSalesPerformance() {
        return 
           " SELECT EmployeeID, "+
                 "  Name, "+
                 "  Position, "+
                 "  (SELECT COUNT(SaleID) FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID) as TotalTransactions, "+
                 "  (SELECT COALESCE(SUM(sd.Quantity * sd.SellingPrice), 0) "+
                 "   FROM SaleDetail sd "+
                 "   WHERE sd.SaleID IN (SELECT SaleID FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID)) as TotalSalesValue "+
            " FROM Employee "+
           " ORDER BY EmployeeID "
        ;
    }
  // Products with the lowest sales
    public static String productsWithLowestSales() {
        return 
           " SELECT Name, "+
                  " Category, "+
                  " (SELECT SellingPrice FROM Product WHERE Product.ItemID = Item.ItemID) as SellingPrice, "+
                 "  (SELECT COALESCE(SUM(Quantity), 0) FROM SaleDetail WHERE SaleDetail.ItemID = Item.ItemID) as TotalSold, "+
                  " (SELECT COALESCE(SUM(Quantity * SellingPrice), 0) FROM SaleDetail WHERE SaleDetail.ItemID = Item.ItemID) as TotalRevenue "+
           " FROM Item "+
           " WHERE ItemID IN (SELECT ItemID FROM Product WHERE Active = true) "+
           " ORDER BY TotalSold ASC, Name ASC "
        ;
    }
 // Most expensive ingredient in stock
    public static String mostExpensiveIngredientInStock() {
        return 
             "SELECT Name, "+
                 "  Category, "+
                 "  (SELECT PurchasePrice FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as PurchasePrice, "+
                 "  (SELECT Quantity FROM Stock WHERE Stock.ItemID = Item.ItemID) as Quantity, "+
                 "  (SELECT Unit FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) as Unit, "+
                 "  (SELECT Location FROM Warehouse WHERE Warehouse.WarehouseID = "+
                   "     (SELECT WarehouseID FROM Stock WHERE Stock.ItemID = Item.ItemID)) as Warehouse, "+
                  " ((SELECT PurchasePrice FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) * "+
                 "   (SELECT Quantity FROM Stock WHERE Stock.ItemID = Item.ItemID)) as TotalValue "+
           " FROM Item "+
           " WHERE ItemID IN (SELECT ItemID FROM Ingredient) "+
            "  AND ItemID IN (SELECT ItemID FROM Stock) "+
           " ORDER BY (SELECT PurchasePrice FROM Ingredient WHERE Ingredient.ItemID = Item.ItemID) DESC "
        ;
    }
 // 3 oldest employees by hiring date 
    public static String top3OldestEmployees() {
        return 
         "   SELECT Name, Position, Salary, HiringDate, "+
                 "  DATEDIFF(CURDATE(), HiringDate) as DaysEmployed, "+
                "   ROUND(DATEDIFF(CURDATE(), HiringDate) / 365.25, 1) as YearsEmployed "+
           " FROM Employee "+
           " ORDER BY HiringDate ASC "
        ;
    }
 // Customers who did not purchase in last 2 weeks
    public static String customersWithoutRecentPurchases() {
        return 
           " SELECT CustomerID, Name, Phone, LoyaltyPoints, "+
                 "  (SELECT MAX(Date) FROM Sale WHERE Sale.CustomerID = Customer.CustomerID) as LastPurchaseDate, "+
                "   DATEDIFF(CURDATE(), (SELECT MAX(Date) FROM Sale WHERE Sale.CustomerID = Customer.CustomerID)) as DaysSinceLastPurchase "+
           " FROM Customer "+
           " WHERE (SELECT MAX(Date) FROM Sale WHERE Sale.CustomerID = Customer.CustomerID) IS NULL  "+
            "   OR (SELECT MAX(Date) FROM Sale WHERE Sale.CustomerID = Customer.CustomerID) < DATE_SUB(CURDATE(), INTERVAL 14 DAY) "+
           " ORDER BY Phone "
        ;
    }
 // Employee with most transactions 
    public static String employeeWithMostTransactions() {
        return 
          "  SELECT EmployeeID, Name, Position, "+
                  " (SELECT COUNT(SaleID) FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID) as TotalTransactions, "+
                  " (SELECT COALESCE(SUM(sd.Quantity * sd.SellingPrice), 0)  "+
                  "  FROM SaleDetail sd "+
                  "  WHERE sd.SaleID IN (SELECT SaleID FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID)) as TotalSalesValue, "+
                  " (SELECT COALESCE(AVG(sd.Quantity * sd.SellingPrice), 0) "+
                  "  FROM SaleDetail sd "+
                  "  WHERE sd.SaleID IN (SELECT SaleID FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID)) as AverageTransactionValue "+
           " FROM Employee "+
           " WHERE (SELECT COUNT(SaleID) FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID) > 0 "+
            " ORDER BY TotalTransactions DESC "
        ;
    }
 // Details of a specific transaction
    public static String getTransactionDetails(int saleID) {
        return String.format(
          "  SELECT SaleID, Date, PaymentMethod, "+
                  " (SELECT Name FROM Customer WHERE Customer.CustomerID = Sale.CustomerID) as Customer, "+
                  " (SELECT Phone FROM Customer WHERE Customer.CustomerID = Sale.CustomerID) as Phone, "+
                  " (SELECT Name FROM Employee WHERE Employee.EmployeeID = Sale.EmployeeID) as Employee, "+
                  " (SELECT LineNo FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as LineNo, "+
                  " (SELECT Name FROM Item WHERE Item.ItemID = (SELECT ItemID FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID)) as Item, "+
                  " (SELECT Quantity FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as Quantity, "+
                  " (SELECT SellingPrice FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as SellingPrice, "+
                  " (SELECT Quantity * SellingPrice FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as Subtotal, "+
                  " (SELECT SUM(Quantity * SellingPrice) FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as TotalPrice "+
           " FROM Sale "+
           " WHERE SaleID = %d "
        , saleID);
    }
// Last 3 sales of a customer 
    public static String last3SalesByCustomer(int customerID) {
        return String.format(
           " SELECT SaleID, Date, PaymentMethod, "+
                  " (SELECT Name FROM Employee WHERE Employee.EmployeeID = Sale.EmployeeID) as Employee, "+
                 "  (SELECT COUNT(*) FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as ItemsCount, "+
                "   (SELECT COALESCE(SUM(Quantity * SellingPrice), 0) FROM SaleDetail WHERE SaleDetail.SaleID = Sale.SaleID) as TotalPrice "+
           " FROM Sale "+
           " WHERE CustomerID = %d "+
            " ORDER BY Date DESC "
        , customerID);
    }

    public static String topProducts() {
        return 
           " SELECT (SELECT Name FROM Item WHERE Item.ItemID = SaleDetail.ItemID) as Name, "+
                 "  (SELECT Category FROM Item WHERE Item.ItemID = SaleDetail.ItemID) as Category, "+
                 "  SUM(Quantity) as TotalSold, "+
                 "  SUM(Quantity * SellingPrice) as TotalRevenue "+
           " FROM SaleDetail "+
           " GROUP BY ItemID "+
           " ORDER BY TotalRevenue DESC "
        ;
    }
  // Retrieves total sales made by each employee
    // sorted by total sales value.
    public static String employeeSales() {
        return 
           " SELECT Name, Position, "+
                  " (SELECT COUNT(SaleID) FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID) as Transactions, "+
                  " (SELECT COALESCE(SUM(sd.Quantity * sd.SellingPrice), 0) "+
                   " FROM SaleDetail sd  "+
                   " WHERE sd.SaleID IN (SELECT SaleID FROM Sale WHERE Sale.EmployeeID = Employee.EmployeeID)) as TotalSales "+
           "  FROM Employee "+
           " ORDER BY TotalSales DESC "
        ;
    }
}

