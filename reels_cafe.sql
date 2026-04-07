-- ============================================================
-- Reels Café Database Schema
-- Database Management System: MySQL
-- ============================================================


DROP DATABASE IF EXISTS reels_cafe;
CREATE DATABASE reels_cafe;
USE reels_cafe;

-- ============================================================
-- TABLE: Employee
-- Café staff including managers, cashiers, and baristas
-- ============================================================
CREATE TABLE Employee (
    EmployeeID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Position VARCHAR(50) NOT NULL,
    Salary DECIMAL(10, 2) NOT NULL,
    HiringDate DATE NOT NULL,
    Hired boolean default true
);

-- ============================================================
-- TABLE: Supplier
-- All suppliers for coffee beans, cups, milk, pastries, etc.
-- ============================================================
CREATE TABLE Supplier (
    SupplierID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    ContactNumber VARCHAR(20) NOT NULL,
    City VARCHAR(50) NOT NULL
);

-- ============================================================
-- TABLE: Customer
-- People purchasing café products
-- ============================================================
CREATE TABLE Customer (
    CustomerID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    LoyaltyPoints INT DEFAULT 0
);

-- ============================================================
-- TABLE: Warehouse
-- Physical locations that hold stock and store inventory
-- ============================================================
CREATE TABLE Warehouse (
    WarehouseID INT PRIMARY KEY AUTO_INCREMENT,
    Type VARCHAR(50) NOT NULL, -- in-store, external, etc.
    Location VARCHAR(100) NOT NULL
);

-- ============================================================
-- TABLE: Item
-- Represents all items (superclass for Product and Ingredient)
-- ============================================================
CREATE TABLE Item (
    ItemID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Category VARCHAR(50) NOT NULL -- Coffee, Tea, Pastries, Supplies, etc.
);

-- ============================================================
-- TABLE: Ingredient
-- Raw materials with stock (overlapping generalization)
-- ============================================================
CREATE TABLE Ingredient (
    ItemID INT PRIMARY KEY,
    PurchasePrice DECIMAL(10, 2) NOT NULL,
    Unit varchar(20),
    FOREIGN KEY (ItemID) REFERENCES Item(ItemID) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: Product
-- Ready-to-sell items (overlapping generalization)
-- ============================================================
CREATE TABLE Product (
    ItemID INT PRIMARY KEY,
    SellingPrice DECIMAL(10, 2) NOT NULL,
    PreparationTime INT, -- in minutes
    Active boolean default true,
    ImagePath varchar(255) default "pictures/default_product.jpg",
    FOREIGN KEY (ItemID) REFERENCES Item(ItemID) ON DELETE CASCADE
);

CREATE TABLE Recipe (
	ProductID INT,
    IngredientID INT,
    Quantity DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (ProductID, IngredientID),
    FOREIGN KEY (ProductID) REFERENCES Product(ItemID) ON DELETE CASCADE,
    FOREIGN KEY (IngredientID) REFERENCES Ingredient(ItemID) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: Stock
-- Inventory tracking of ingredients in warehouses
-- ============================================================
CREATE TABLE Stock (
    WarehouseID INT,
    ItemID INT,
    Quantity DECIMAL(10, 2) NOT NULL,
    ExpirationDate DATE,
    PRIMARY KEY (WarehouseID, ItemID),
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID) ON DELETE CASCADE,
    FOREIGN KEY (ItemID) REFERENCES Ingredient(ItemID) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: Sale
-- Records every transaction made with customers
-- ============================================================
CREATE TABLE Sale (
    SaleID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerID INT,
    EmployeeID INT NOT NULL,
    Date DATE NOT NULL,
    PaymentMethod VARCHAR(50) NOT NULL, -- Cash, Credit Card, etc.
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE RESTRICT,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE RESTRICT
);

-- ============================================================
-- TABLE: SaleDetail
-- Represents all products individually in each sale
-- ============================================================
CREATE TABLE SaleDetail (
    SaleID INT,
    LineNo INT,
    ItemID INT NOT NULL,
    Quantity INT NOT NULL,
    SellingPrice DECIMAL(10, 2) NOT NULL, -- price per unit
    PRIMARY KEY (SaleID, LineNo),
    FOREIGN KEY (SaleID) REFERENCES Sale(SaleID) ON DELETE CASCADE,
    FOREIGN KEY (ItemID) REFERENCES Product(ItemID) ON DELETE RESTRICT
);

-- ============================================================
-- TABLE: Purchase
-- Records every purchase made with suppliers
-- ============================================================
CREATE TABLE Purchase (
    PurchaseID INT PRIMARY KEY AUTO_INCREMENT,
    SupplierID INT NOT NULL,
    EmployeeID INT NOT NULL,
    WarehouseID INT NOT NULL, -- Target warehouse
    Date DATE NOT NULL,
    Time TIME NOT NULL,
    FOREIGN KEY (SupplierID) REFERENCES Supplier(SupplierID) ON DELETE RESTRICT,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE RESTRICT,
    FOREIGN KEY (WarehouseID) REFERENCES Warehouse(WarehouseID) ON DELETE RESTRICT
);

-- ============================================================
-- TABLE: PurchaseDetail
-- Represents all stock items individually in each purchase
-- ============================================================
CREATE TABLE PurchaseDetail (
    PurchaseID INT,
    LineNo INT,
    ItemID INT NOT NULL,
    Quantity DECIMAL(10, 2) NOT NULL,
    PurchasePrice DECIMAL(10, 2) NOT NULL, -- price per unit
    PRIMARY KEY (PurchaseID, LineNo),
    FOREIGN KEY (PurchaseID) REFERENCES Purchase(PurchaseID) ON DELETE CASCADE,
    FOREIGN KEY (ItemID) REFERENCES Ingredient(ItemID) ON DELETE RESTRICT
);

-- ============================================================
-- TABLE: Transfer
-- Represents transfers between stock locations
-- ============================================================
CREATE TABLE Transfer (
    TransferID INT PRIMARY KEY AUTO_INCREMENT,
    SourceWarehouseID INT NOT NULL,
    DestinationWarehouseID INT NOT NULL,
    EmployeeID INT NOT NULL,
    TransferDate DATE NOT NULL,
    FOREIGN KEY (SourceWarehouseID) REFERENCES Warehouse(WarehouseID) ON DELETE RESTRICT,
    FOREIGN KEY (DestinationWarehouseID) REFERENCES Warehouse(WarehouseID) ON DELETE RESTRICT,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE RESTRICT,
    CHECK (SourceWarehouseID != DestinationWarehouseID)
);

-- ============================================================
-- TABLE: TransferDetail
-- Represents all ingredients individually in each transfer
-- ============================================================
CREATE TABLE TransferDetail (
    TransferID INT,
    LineNo INT,
    ItemID INT NOT NULL,
    Quantity DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (TransferID, LineNo),
    FOREIGN KEY (TransferID) REFERENCES Transfer(TransferID) ON DELETE CASCADE,
    FOREIGN KEY (ItemID) REFERENCES Ingredient(ItemID) ON DELETE RESTRICT
);

-- ============================================================
-- SAMPLE DATA INSERTION
-- ============================================================

-- Insert Employees
INSERT INTO Employee (Name, Position, Salary, HiringDate,Hired) VALUES
('Ahmad Hassan', 'Manager', 3500.00, '2020-01-15', true),
('Sara Mahmoud', 'Cashier', 1800.00, '2021-03-20',true),
('Omar Khalil', 'Barista', 2000.00, '2021-06-10',true),
('Layla Nasser', 'Barista', 2100.00, '2022-02-14',true),
('Youssef Ali', 'Cashier', 1850.00, '2023-05-01',true),
('Mona Fares', 'Barista', 2000.00, '2023-06-15', true),
('Ali Samir', 'Cleaning', 1900.00, '2023-07-01', true);

-- Insert Suppliers
INSERT INTO Supplier (Name, ContactNumber, City) VALUES
('Palestine Coffee Co.', '022951234', 'Ramallah'),
('Fresh Bakery Ltd.', '022987654', 'Jerusalem'),
('Dairy Farms Supply', '022945678', 'Nablus'),
('Global Coffee Beans', '022912345', 'Ramallah'),
('Sweet Pastries Inc.', '022923456', 'Bethlehem');

-- Insert Customers
INSERT INTO Customer (Name, Phone, LoyaltyPoints) VALUES
('Khaled Ahmad', '0599123456', 150),
('Nour Salem', '0598234567', 85),
('Rami Yousef', '0597345678', 200),
('Dina Khalil', '0596456789', 120),
('Sami Hassan', '0595567890', 45),
('Lina Nasser', '0594678901', 180),
('Tariq Ali', '0593789012', 95),
('Maya Saleh', '0592890123', 250),
('Fadi Omar', '0591901234', 60),
('Hiba Mahmoud', '0590012345', 110),
('Rana Jaber','0591122334', 50),
('Tamer Ziad','0592233445', 120),
('Layan Fathi','0593344556', 200),
('Bilal Karim','0594455667', 30),
('Maha Salim','0595566778', 180),
('Firas Naji','0596677889', 90),
('Dalia Hani','0597788990', 250),
('Omar Rami','0598899001', 60),
('Rola Sami','0599900112', 130),
('Yara Tarek','0591011121', 170);

-- Insert Warehouses
INSERT INTO Warehouse (Type, Location) VALUES
('in-store', 'Reels Café Main Counter'),
('external', 'Ramallah Storage Facility A'),
('external', 'Ramallah Cold Storage');

-- Insert Items (Categories: Coffee, Tea, Pastries, Supplies)
INSERT INTO Item (Name, Category) VALUES
-- Products
('Americano', 'Hot Coffee'), -- ----1
('Latte', 'Hot Coffee'),-- 2
('Cappuccino', 'Hot Coffee'),-- 3
('Espresso', 'Hot Coffee'),-- 4
('Green Tea', 'Tea'),-- 5
('Black Tea', 'Tea'),-- 6
('Croissant', 'Pastries'),-- 7
('Chocolate Cake', 'Pastries'),-- 8
('Blueberry Muffin', 'Pastries'),-- 9
-- Ingredients
('Coffee Beans', 'Supplies'),-- 10
('Milk', 'Supplies'),-- 11
('Sugar', 'Supplies'),-- 12
('Paper Cups', 'Supplies'),-- 13
('Tea Bags', 'Supplies'),-- 14
('Chocolate', 'Supplies'),-- 15
-- More products
('Mocha', 'Hot Coffee'),-- 16
('Hot Cocoa', 'Hot Drink'),-- 17
('Iced Latte', 'Cold Coffee'),-- 18
('Iced Coffee', 'Cold Coffee'), -- 19
('Iced Spanish Latte', 'Cold Coffee'), -- 20
('Hot Hazelnut', 'Hot Drink'), -- 21
('Sahlab', 'Hot Drink'), -- 22
('French Vanilla', 'Hot Drink'), -- 23
('Oreo Milkshake', 'Cold Drink'), -- 24
('Chocolate Milkshake', 'Cold Drink'),-- 25
('Vanilla Milkshalke', 'Cold Drink'),-- 26
('Strawberry Milkshake', 'Cold Drink'), -- 27
('Brownie', 'Pastries'),-- 28
-- more ingredients
('Hazelnut Powder', 'Supplies'),-- 29
('Sahlab Powder', 'Supplies'), -- 30
('French Vanilla Powder', 'Supplies'), -- 31
('Oreo', 'Supplies'),-- 32
('Chocolate Icecream','Supplies'),-- 33
('Vanilla Icecream', 'Supplies'),-- 34
('Strawberry Icecream', 'Supplies'), -- 35
('Condensed milk', 'Supplies'), -- 36
('Plastic Cups', 'Supplies'), -- 37
('Straws', 'Supplies'); -- 38

-- Insert Products
INSERT INTO Product (ItemID, SellingPrice, PreparationTime,ImagePath) VALUES
(1, 12.00, 3,"pictures/CaffeAmericano.jpg"),
(2, 15.00, 5,"pictures/images.jpg"),
(3, 14.00, 5,"pictures/capp.jpg"),
(4, 10.00, 2,"pictures/coffee-in-cup.jpg"),
(5, 8.00, 3,"pictures/green.jpg"),
(6, 7.00, 3,"pictures/black.jpg"),
(7, 6.00, 1,"pictures/French-Croissants-SM-2363.jpg"),
(8, 18.00, 1,"pictures/Chocolate-cake.jpg"),
(9, 9.00, 1,"pictures/vegan-blueberry-muffins-1-1.jpg"),
(16, 17.00, 5,"pictures/mocha.jpg"),
(17, 15.00, 3,"pictures/Hot-Chocolate-3.jpg"),
(18, 16.00, 3,"pictures/cold-brew-latte-7.jpg"),
(19, 18.00, 3,"pictures/icecoffee.jpg"),
(20,17.00, 3,"pictures/spanish.jpg"),
(21, 14.00, 3,"pictures/Creme-Calde-Nocciola.jpg"),
(22, 14.00, 3,"pictures/Sahlab.jpg"),
(23, 14.00, 3,"pictures/french.jpg"),
(24, 16.00, 4,"pictures/oreo-milkshake-recipe.jpg"),
(25 , 16.00, 4,"pictures/cmilkshake.jpg"),
(26, 16.00, 4,"pictures/vanilla.jpg"),
(27, 16.00, 4,"pictures/strawberry-milkshake-4.jpg"),
(28, 20.00, 2,"pictures/brownie.jpg");

-- Insert Ingredients
INSERT INTO Ingredient (ItemID, PurchasePrice,Unit) VALUES
(10, 85.00,'kg'), -- Coffee Beans per kg
(11, 4.50,'liter'),  -- Milk per liter
(12, 3.00,'kg'),  -- Sugar per kg
(13, 1.20,'pieces'),  -- Paper Cups per piece
(7, 2.50,'pieces'),  -- Croissant per piece
(14, 2.00,'bag'), -- Tea bags per bag
(15, 25.00,'kg'), -- Chocolate per kg
(8, 9.00,'pieces'), -- Chocolate cake per piece
(9, 4.00,'pieces'), -- muffin bluberry per piece
(29, 60.00,'kg'), -- hazelnut powder per kg(one bag)
(30, 60.00,'kg'), -- sahlab powder per kg(one bag)
(31, 60.00,'kg'), -- fv powder per kg(one bag)
(32, 20.00,'kg'), -- oreo per kg (one carton (each carton has 10 packets))
(33, 20.00,'liter'), -- chocolate icecream per liter (one gallon)
(34, 20.00,'liter'), -- vanialla icecream per liter (one gallon)
(35, 20.00,'liter'), -- strawberry icecream per liter (one gallon)
(36, 20.00,'liter'), -- condensed milk per liter (3 cans)
(37, 1.5,'pieces'), -- plastic cup per piece
(38, 1.00,'pieces'), -- straw per piece
(28,15.00,'pieces'); -- brownie


INSERT INTO Recipe(ProductID,IngredientID,Quantity) VALUES
-- 1: Americano (20 grams coffee, 1 cup)
(1,10,0.02),
(1,13,1),
-- 2: Latte (20 grams coffee, 200 ml milk, 1 cup)
(2,10,0.02),
(2,11,0.2),
(2,13,1),
-- Cappucino: same as latte
(3,10,0.02),
(3,11,0.2),
(3,13,1),
-- espresso (20gm coffee, 1 cup)
(4,10,0.02),
(4,13,1),
-- green tea: (tea bag, cup)
(5,14,1),
(5,13,1),
-- black tea same as green
(6,14,1),
(6,13,1),
-- croissant: 1 croissant
(7,7,1),
-- cake: 1 cake
(8,8,1),
-- muffin: 1 muffin
(9,9,1),
-- mocha: 20gm coffee, 200 ml milk, 30 gm chocolate, 1 cup
(16,10,0.02),
(16,11, 0.2),
(16,13,1),
(16,15,0.03),
-- hot cocoa: 50 gm chocolate, 10 gm sugar, 1 cup
(17,15,0.05),
(17,12,0.01),
-- Iced latte: 20gm coffee, 300 ml milk, 1 plastic cup, 1 straw
(18,10,0.02),
(18,11,0.3),
(18,37,1),
(18,38,1),
-- Iced Coffee: 20gm coffee, 300 ml milk, 20gm sugar, 1 plastic cup, 1 straw
(19,10,0.02),
(19,11,0.3),
(19,12,0.02),
(19,37,1),
(19,38,1),
-- Iced spanish latte: 20gm coffee, 300 ml milk, 50ml condenced milk, 1 plastic cup, 1 straw
(20,10,0.02),
(20,11,0.3),
(20,36,0.5),
(20,37,1),
(20,38,1),
-- hot hazelnut: 30gm hazelnut powder, 200 ml milk, 1 cup
(21,29,0.03),
(21,11,0.2),
(21,13,1),
-- sahlab: 30gm sahlab powder, 200 ml milk, 1 cup
(22,30,0.03),
(22,11,0.2),
(22,13,1),
-- french vanilla : 30 gm french vanilla, 200 ml milk, 1 cup
(23,31,0.03),
(23,11,0.2),
(23,13,1),
-- oreo: 4 oreos(100gm), 200 ml vanilla icecream (2 large scoops), 200 ml milk, 1 plastic cup, 1 straw
(24,32,0.1),
(24,34,0.2),
(24,11,0.2),
(24,37,1),
(24,38,1),
-- chocolate:200 ml chocolate icecream (2 large scoops), 200 ml milk, 1 plastic cup, 1 straw
(25,33,0.2),
(25,11,0.2),
(25,37,1),
(25,38,1),
-- vanilla: 200 ml vanilla icecream (2 large scoops), 200 ml milk, 1 plastic cup, 1 straw
(26,34,0.2),
(26,11,0.2),
(26,37,1),
(26,38,1),
-- strawberry:200 ml strawberry icecream (2 large scoops), 200 ml milk, 1 plastic cup, 1 straw
(27,35,0.2),
(27,11,0.2),
(27,37,1),
(27,38,1),
-- brownie: one brownie heated
(28,28,1);

-- Insert Stock
INSERT INTO Stock (WarehouseID, ItemID, Quantity, ExpirationDate) VALUES
-- In-store stock
(1, 10, 5.5, '2026-12-31'),
(1, 11, 20.0, '2026-12-25'),
(1, 12, 10.0,'2026-06-30'),
(1, 13, 35, NULL),
(1, 7, 30, '2026-12-20'),
(1, 14, 100, '2026-12-31'),
(1, 15, 1.5, '2027-10-30'),
(1, 8, 10, '2026-2-20'),
(1, 9, 10, '2026-3-2'),
(1,29, 15,'2027-10-30'), 
(1,30, 20, '2026-10-30'),
(1,31, 10,'2027-8-30'),
(1,32, 3,'2028-07-13'),
(1,33, 4,'2026-02-20'),
(1,34, 5,'2026-02-20'),
(1,35, 3,'2026-02-20'),
(1,36, 6,'2026-5-30'),
(1,37, 150,NULL),
(1,38, 150,NULL), 
-- External storage
(2, 10, 50.0, '2026-12-31'),
(2, 12, 100.0, '2026-06-30'),
(2, 13, 2000, NULL),
(2, 7, 200, '2026-01-15'),
(2, 15, 15.0, '2026-03-30'),
(2, 9, 0, '2027-3-2'),
(2,29, 0,'2027-10-30'), 
(2,30, 0, '2027-10-30'),
(2,31, 0,'2027-9-30'),
(2,32, 0,'2028-08-13'),
(2,33, 4,'2026-03-20'),
(2,34, 3,'2026-03-20'),
(2,35, 2,'2026-03-20'),
(2,36, 2,'2026-6-30'),
(2,37, 150,NULL),
(2,38, 150,NULL),
-- Cold storage
(3,36, 6,'2026-9-19'),
(3, 11, 150.0, '2025-12-30');


-- Insert Sales
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1, 2, '2025-12-10', 'Cash'),
(2, 2, '2025-12-10', 'Credit Card'),
(3, 5, '2025-12-11', 'Cash'),
(1, 2, '2025-12-11', 'Cash'),
(4, 5, '2025-12-12', 'Credit Card'),
(5, 2, '2025-12-12', 'Cash'),
(6, 5, '2025-12-13', 'Credit Card'),
(7, 2, '2025-12-14', 'Cash'),
(8, 5, '2025-12-15', 'Credit Card'),
(3, 2, '2025-12-15', 'Cash'),
(1, 2, '2026-01-13', 'Credit Card'),
(9, 5, '2025-12-17', 'Cash'),
(4, 2, '2025-12-17', 'Cash'),
(6, 5, '2025-12-17',  'Credit Card'),
(2, 3, '2026-01-10', 'Cash'),
(5, 6, '2026-01-06', 'Credit Card'),
(1, 4, '2026-01-09', 'Cash'),
(7, 2, '2026-01-08', 'Credit Card'),
(2, 3, '2026-01-10', 'Cash'),
(5, 6, '2026-01-11', 'Credit Card'),
(1, 4, '2026-01-12', 'Cash'),
(7, 2, '2026-01-13', 'Credit Card'),
(3, 5, '2026-01-14', 'Cash'),
(6, 3, '2026-01-15', 'Credit Card'),
(4, 2, '2026-01-16', 'Cash'),
(8, 4, '2026-01-16', 'Credit Card'),
(9, 6, '2026-01-15', 'Cash'),
(10, 5, '2026-01-16', 'Credit Card'),
(11, 2, '2026-01-17', 'Cash'),
(12, 3, '2026-01-17', 'Credit Card'),
(13, 4, '2026-01-17', 'Cash'),
(14, 5, '2026-01-17', 'Cash'),
(15, 2, '2026-01-17', 'Credit Card'),
(16, 3, '2026-01-18', 'Cash'),
(17, 4, '2026-01-18', 'Credit Card'),
(18, 5, '2026-01-18', 'Cash'),
(19, 2, '2026-01-18', 'Cash'),
(20, 3, '2026-01-18', 'Credit Card'),
(11, 2, '2026-01-19', 'Cash'),
(12, 3, '2026-01-19', 'Credit Card'),
(13, 4, '2026-01-19', 'Cash'),
(14, 5, '2026-01-19', 'Cash'),
(15, 2, '2026-01-19', 'Credit Card'),
(16, 3, '2026-01-20', 'Cash'),
(17, 4, '2026-01-20', 'Credit Card'),
(18, 5, '2026-01-20', 'Cash'),
(19, 2, '2026-01-20', 'Cash'),
(20, 3, '2026-01-20', 'Credit Card'),
(11, 2, '2026-01-21', 'Cash'),
(12, 3, '2026-01-21', 'Credit Card'),
(13, 4, '2026-01-21', 'Cash'),
(14, 5, '2026-01-21', 'Cash'),
(15, 2, '2026-01-21', 'Credit Card'),
(16, 3, '2026-01-22', 'Cash'),
(17, 4, '2026-01-22', 'Credit Card'),
(18, 5, '2026-01-22', 'Cash'),
(19, 2, '2026-01-22', 'Cash'),
(20, 3, '2026-01-22', 'Credit Card'),
-- 2024
(1, 2, '2024-03-12', 'Cash'),
(2, 3, '2024-04-05', 'Credit Card'),
(3, 1, '2024-05-20', 'Cash'),
(4, 2, '2024-06-18', 'Cash'),
(5, 5, '2024-07-10', 'Credit Card'),
(6, 4, '2024-08-15', 'Cash'),
(7, 3, '2024-09-22', 'Credit Card'),
(8, 2, '2024-10-05', 'Cash'),
(9, 1, '2024-11-11', 'Credit Card'),
(10, 5, '2024-12-20', 'Cash'),

-- 2025
(11, 2, '2025-01-05', 'Credit Card'),
(12, 3, '2025-02-14', 'Cash'),
(13, 4, '2025-03-18', 'Cash'),
(14, 5, '2025-04-22', 'Credit Card'),
(15, 1, '2025-05-15', 'Cash'),
(16, 2, '2025-06-10', 'Cash'),
(17, 3, '2025-07-08', 'Credit Card'),
(18, 4, '2025-08-19', 'Cash'),
(19, 5, '2025-09-12', 'Credit Card'),
(20, 1, '2025-10-07', 'Cash'),
(1, 2, '2025-11-11', 'Credit Card'),
(2, 3, '2025-12-05', 'Cash'),
(3, 4, '2025-12-15', 'Credit Card'),
(4, 5, '2025-12-20', 'Cash'),
(5, 1, '2025-12-24', 'Credit Card'),
(6, 2, '2025-12-13', 'Cash'),
(7, 3, '2025-12-08', 'Credit Card'),
(8, 4, '2025-12-18', 'Cash'),
(9, 5, '2025-12-23', 'Credit Card'),
(10, 1, '2025-12-25', 'Cash'),

-- 2026
(11, 2, '2026-01-01', 'Cash'),
(12, 3, '2026-01-02', 'Credit Card'),
(13, 4, '2026-01-03', 'Cash'),
(14, 5, '2026-01-04', 'Credit Card'),
(15, 1, '2026-01-05', 'Cash'),
(16, 2, '2026-01-06', 'Cash'),
(17, 3, '2026-01-07', 'Credit Card'),
(18, 4, '2026-01-08', 'Cash'),
(19, 5, '2026-01-09', 'Credit Card'),
(20, 1, '2026-01-10', 'Cash'),
(1, 2, '2026-01-11', 'Credit Card'),
(2, 3, '2026-01-12', 'Cash'),
(3, 4, '2026-01-13', 'Credit Card'),
(4, 5, '2026-01-14', 'Cash'),
(5, 1, '2026-01-15', 'Credit Card'),
(6, 2, '2026-01-16', 'Cash'),
(7, 3, '2026-01-17', 'Credit Card'),
(8, 4, '2026-01-18', 'Cash'),
(9, 5, '2026-01-19', 'Credit Card'),
(10, 1, '2026-01-20', 'Cash'),
(11, 2, '2026-01-21', 'Credit Card'),
(12, 3, '2026-01-22', 'Cash'),
(13, 4, '2026-01-23', 'Credit Card'),
(14, 5, '2026-01-24', 'Cash'),
(15, 1, '2026-01-25', 'Credit Card'),
(16, 2, '2026-01-15', 'Cash'),
(17, 3, '2026-01-05', 'Credit Card'),
(18, 4, '2026-01-07', 'Cash'),
(19, 5, '2026-01-10', 'Credit Card'),
(20, 1, '2026-01-12', 'Cash');

-- Insert Sale Details
INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 1
(1, 1, 1, 2, 12.00),
(1, 2, 7, 3, 6.00),
-- Sale 2
(2, 1, 2, 1, 15.00),
(2, 2, 5, 1, 8.00),
-- Sale 3
(3, 1, 1, 3, 12.00),
(3, 2, 8, 1, 18.00),
-- Sale 4
(4, 1, 3, 2, 14.00),
(4, 2, 9, 1, 9.00),
-- Sale 5
(5, 1, 2, 2, 15.00),
(5, 2, 9, 1, 9.00),
-- Sale 6
(6, 1, 1, 1, 12.00),
(6, 2, 7, 1, 6.00),
-- Sale 7
(7, 1, 1, 3, 12.00),
(7, 2, 2, 1, 15.00),
(7, 3, 9, 1, 9.00),
-- Sale 8
(8, 1, 3, 1, 14.00),
(8, 2, 5, 1, 8.00),
(8, 3, 6, 1, 7.00),
-- Sale 9
(9, 1, 1, 4, 12.00),
(9, 2, 8, 1, 18.00),
(9, 3, 9, 1, 9.00),
-- Sale 10
(10, 1, 2, 2, 15.00),
(10, 2, 7, 1, 6.00),
-- Sale 11
(11, 1, 1, 3, 12.00),
(11, 2, 9, 1, 9.00),
-- Sale 12
(12, 1, 3, 1, 14.00),
(12, 2, 6, 1, 7.00),
-- Sale 13
(13, 1, 2, 2, 15.00),
(13, 2, 5, 1, 8.00),
-- Sale 14
(14, 1, 1, 2, 12.00),
(14, 2, 2, 1, 15.00),
(14, 3, 9, 1, 9.00),
-- Sale 15
(15, 1, 1, 2, 12.00),
(15, 2, 7, 1, 6.00),
-- Sale 16
(16, 1, 3, 1, 14.00),
(16, 2, 5, 2, 8.00),
-- Sale 17
(17, 1, 2, 1, 15.00),
(17, 2, 18, 1, 16.00),
-- Sale 18
(18, 1, 4, 1, 10.00),
(18, 2, 9, 2, 9.00),
-- Sale 19
(19, 1, 1, 2, 12.00),
(19, 2, 7, 1, 6.00),
-- Sale 20
(20, 1, 2, 1, 15.00),
(20, 2, 5, 2, 8.00),
-- Sale 21
(21, 1, 3, 1, 14.00),
(21, 2, 9, 1, 9.00),
-- Sale 22
(22, 1, 4, 1, 10.00),
(22, 2, 18, 1, 16.00),
-- Sale 23
(23, 1, 1, 3, 12.00),
(23, 2, 19, 1, 18.00),
-- Sale 24
(24, 1, 2, 2, 15.00),
(24, 2, 7, 1, 6.00),
-- Sale 25
(25, 1, 3, 2, 14.00),
(25, 2, 8, 1, 18.00),
-- Sale 26
(26, 1, 4, 1, 10.00),
(26, 2, 9, 2, 9.00),
-- Sale 27
(27, 1, 16, 1, 17.00),
(27, 2, 20, 2, 17.00),
-- Sale 28
(28, 1, 17, 1, 15.00),
(28, 2, 22, 1, 17.00),
-- Sale 29
(29,1,1,2,12.00),(29,2,7,1,6.00),
-- Sale 30
(30,1,2,1,15.00),(30,2,8,2,18.00),
-- Sale 31
(31,1,3,2,14.00),(31,2,9,1,9.00),
-- Sale 32
(32,1,16,1,17.00),(32,2,18,1,16.00),
-- Sale 33
(33,1,17,2,15.00),(33,2,19,1,18.00),
-- Sale 34
(34,1,1,3,12.00),(34,2,7,2,6.00),
-- Sale 35
(35,1,2,2,15.00),(35,2,8,1,18.00),
-- Sale 36
(36,1,3,1,14.00),(36,2,9,2,9.00),
-- Sale 37
(37,1,16,2,17.00),(37,2,18,2,16.00),
-- Sale 38
(38,1,17,1,15.00),(38,2,19,2,18.00),
-- Sale 39
(39,1,1,2,12.00),(39,2,7,1,6.00),
-- Sale 40
(40,1,2,1,15.00),(40,2,8,2,18.00),
-- Sale 41
(41,1,3,2,14.00),(41,2,9,1,9.00),
-- Sale 42
(42,1,16,1,17.00),(42,2,18,1,16.00),
-- Sale 43
(43,1,17,2,15.00),(43,2,19,1,18.00),
-- Sale 44
(44,1,1,3,12.00),(44,2,7,2,6.00),
-- Sale 45
(45,1,2,2,15.00),(45,2,8,1,18.00),
-- Sale 46
(46,1,3,1,14.00),(46,2,9,2,9.00),
-- Sale 47
(47,1,16,2,17.00),(47,2,18,2,16.00),
-- Sale 48
(48,1,17,1,15.00),(48,2,19,2,18.00),
-- Sale 49
(49,1,1,2,12.00),(49,2,7,1,6.00),
-- Sale 50
(50,1,2,1,15.00),(50,2,8,2,18.00),
-- Sale 51
(51,1,3,2,14.00),(51,2,9,1,9.00),
-- Sale 52
(52,1,16,1,17.00),(52,2,18,1,16.00),
-- Sale 53
(53,1,17,2,15.00),(53,2,19,1,18.00),
-- Sale 54
(54,1,1,3,12.00),(54,2,7,2,6.00),
-- Sale 55
(55,1,2,2,15.00),(55,2,8,1,18.00),
-- Sale 56
(56,1,3,1,14.00),(56,2,9,2,9.00),
-- Sale 57
(57,1,16,2,17.00),(57,2,18,2,16.00),
-- Sale 58
(58,1,17,1,15.00),(58,2,19,2,18.00),
-- Sale 59
(59,1,1,2,12.00),(59,2,7,1,6.00),
-- Sale 60
(60,1,2,1,15.00),(60,2,8,2,18.00),
-- Sale 61
(61,1,3,2,14.00),(61,2,9,1,9.00),
-- Sale 62
(62,1,16,1,17.00),(62,2,18,1,16.00),
-- Sale 63
(63,1,17,2,15.00),(63,2,19,1,18.00),
-- Sale 64
(64,1,1,3,12.00),(64,2,7,2,6.00),
-- Sale 65
(65,1,2,2,15.00),(65,2,8,1,18.00),
-- Sale 66
(66,1,3,1,14.00),(66,2,9,2,9.00),
-- Sale 67
(67,1,16,2,17.00),(67,2,18,2,16.00),
-- Sale 68
(68,1,17,1,15.00),(68,2,19,2,18.00),
-- Sale 69
(69,1,1,2,12.00),(69,2,7,1,6.00),
-- Sale 70
(70,1,2,1,15.00),(70,2,8,2,18.00),
-- Sale 71
(71,1,3,2,14.00),(71,2,9,1,9.00),
-- Sale 72
(72,1,16,1,17.00),(72,2,18,1,16.00),
-- Sale 73
(73,1,17,2,15.00),(73,2,19,1,18.00),
-- Sale 74
(74,1,1,3,12.00),(74,2,7,2,6.00),
-- Sale 75
(75,1,2,2,15.00),(75,2,8,1,18.00),
-- Sale 76
(76,1,3,1,14.00),(76,2,9,2,9.00),
-- Sale 77
(77,1,16,2,17.00),(77,2,18,2,16.00),
-- Sale 78
(78,1,17,1,15.00),(78,2,19,2,18.00),
-- Sale 79
(79,1,1,2,12.00),(79,2,7,1,6.00),
-- Sale 80
(80,1,2,1,15.00),(80,2,8,2,18.00),
-- Sale 81
(81,1,3,2,14.00),(81,2,9,1,9.00),
-- Sale 82
(82,1,16,1,17.00),(82,2,18,1,16.00),
-- Sale 83
(83,1,17,2,15.00),(83,2,19,1,18.00),
-- Sale 84
(84,1,1,3,12.00),(84,2,7,2,6.00),
-- Sale 85
(85,1,2,2,15.00),(85,2,8,1,18.00),
-- Sale 86
(86,1,3,1,14.00),(86,2,9,2,9.00),
-- Sale 87
(87,1,16,2,17.00),(87,2,18,2,16.00),
-- Sale 88
(88,1,17,1,15.00),(88,2,19,2,18.00),
-- Sale 89
(89,1,1,2,12.00),(89,2,7,1,6.00),
-- Sale 90
(90,1,2,1,15.00),(90,2,8,2,18.00),
-- Sale 91
(91,1,3,2,14.00),(91,2,9,1,9.00),
-- Sale 92
(92,1,16,1,17.00),(92,2,18,1,16.00),
-- Sale 93
(93,1,17,2,15.00),(93,2,19,1,18.00),
-- Sale 94
(94,1,1,3,12.00),(94,2,7,2,6.00),
-- Sale 95
(95,1,2,2,15.00),(95,2,8,1,18.00),
-- Sale 96
(96,1,3,1,14.00),(96,2,9,2,9.00),
-- Sale 97
(97,1,16,2,17.00),(97,2,18,2,16.00),
-- Sale 98
(98,1,17,1,15.00),(98,2,19,2,18.00),
-- Sale 99
(99,1,1,2,12.00),(99,2,7,1,6.00),
-- Sale 100
(100,1,2,1,15.00),(100,2,8,2,18.00);



-- Insert Purchases
INSERT INTO Purchase (SupplierID, EmployeeID, WarehouseID, Date, Time) VALUES
(1, 1, 2, '2025-11-15', '09:00:00'),
(3, 1, 3, '2025-11-20', '10:30:00'),
(2, 1, 2, '2025-12-01', '08:00:00'),
(4, 1, 2, '2025-12-05', '09:30:00'),
(5, 1, 2, '2025-12-10', '11:00:00'),
(1, 1, 2, '2026-01-05', '09:00:00'),
(2, 2, 2, '2026-01-07', '10:30:00'),
(3, 1, 3, '2026-01-09', '08:45:00'),
(4, 2, 1, '2026-01-11', '11:15:00'),
(5, 1, 1, '2026-01-13', '12:00:00'),
(1, 3, 2, '2026-01-15', '09:30:00'),
(2, 4, 2, '2026-01-14', '10:00:00'),
(1, 1, 2, '2025-11-15', '09:00:00'),
(3, 1, 3, '2025-11-20', '10:30:00'),
(2, 2, 1, '2025-11-25', '11:15:00'),
(4, 3, 2, '2025-12-01', '08:45:00'),
(5, 2, 1, '2025-12-05', '14:20:00'),
(1, 1, 3, '2025-12-10', '09:00:00'),
(3, 4, 2, '2025-12-15', '10:00:00'),
(2, 5, 1, '2025-12-18', '11:45:00'),
(4, 3, 2, '2025-12-20', '09:30:00'),
(5, 2, 1, '2025-12-22', '13:00:00');

INSERT INTO PurchaseDetail (PurchaseID, LineNo, ItemID, Quantity, PurchasePrice) VALUES
-- Purchase 1
(1, 1, 10, 50.0, 85.00),

-- Purchase 2
(2, 1, 11, 150.0, 4.50),

-- Purchase 3
(3, 1, 7, 200, 2.50),

-- Purchase 4
(4, 1, 10, 20.0, 85.00),

-- Purchase 5
(5, 1, 15, 15.0, 25.00),
-- Purchase 6
(6, 1, 10, 60.0, 85.00),
(6, 2, 11, 100.0, 4.50),
-- Purchase 7
(7, 1, 7, 200, 2.50),
(7, 2, 12, 50.0, 3.00),
-- Purchase 8
(8, 1, 15, 20, 25.00),
-- Purchase 9
(9, 1, 13, 500, 1.20),
(9, 2, 14, 100, 2.00),
-- Purchase 10
(10, 1, 10, 40, 85.00),
(10, 2, 11, 50, 4.50),
-- Purchase 11
(11, 1, 12, 80, 3.00),
-- Purchase 12
(12, 1, 15, 25, 25.00),
(12, 2, 7, 100, 2.50),

(13, 1, 10, 20, 85.00),  -- Coffee Beans 20kg
(13, 2, 11, 50, 4.50),   -- Milk 50L
(13, 3, 12, 30, 3.00),   -- Sugar 30kg
-- Purchase 2
(14, 1, 13, 500, 1.20),  -- Paper Cups
(14, 2, 14, 50, 2.00),   -- Tea Bags
-- Purchase 3
(15, 1, 7, 100, 2.50),   -- Croissants
(15, 2, 8, 50, 9.00),    -- Chocolate Cake
(15, 3, 9, 75, 4.00),    -- Blueberry Muffin
-- Purchase 4
(16, 1, 15, 10, 25.00),  -- Chocolate
(16, 2, 29, 20, 60.00),  -- Hazelnut Powder
-- Purchase 5
(17, 1, 30, 15, 60.00),  -- Sahlab Powder
(17, 2, 31, 10, 60.00),  -- French Vanilla Powder
-- Purchase 6
(18, 1, 32, 5, 20.00),   -- Oreo
(18, 2, 33, 10, 20.00),  -- Chocolate Icecream
(18, 3, 34, 10, 20.00),  -- Vanilla Icecream
(18, 4, 35, 8, 20.00),   -- Strawberry Icecream
-- Purchase 7
(19, 1, 36, 12, 20.00),  -- Condensed Milk
(19, 2, 37, 1000, 1.50), -- Plastic Cups
(19, 3, 38, 1000, 1.00), -- Straws
-- Purchase 8
(20, 1, 10, 15, 85.00),  -- Coffee Beans
(20, 2, 11, 40, 4.50),   -- Milk
(20, 3, 12, 20, 3.00),   -- Sugar
-- Purchase 9
(21, 1, 13, 300, 1.20),  -- Paper Cups
(21, 2, 14, 40, 2.00),   -- Tea Bags
-- Purchase 10
(22,1, 15, 5, 25.00),   -- Chocolate
(22,2, 29, 10, 60.00); 


-- Insert Transfers
INSERT INTO Transfer (SourceWarehouseID, DestinationWarehouseID, EmployeeID, TransferDate) VALUES
(2, 1, 3, '2025-12-08'),
(3, 1, 3, '2025-12-12'),
(2, 1, 4, '2025-12-15');

-- Insert Transfer Details
INSERT INTO TransferDetail (TransferID, LineNo, ItemID, Quantity) VALUES
-- Transfer 1
(1, 1, 10, 5.0),
(1, 2, 13, 500),
-- Transfer 2
(2, 1, 11, 20.0),
-- Transfer 3
(3, 1, 7, 30),
(3, 2, 12, 10.0);
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1, 2, '2026-01-01', 'Cash'),
(2, 3, '2026-01-01', 'Credit Card'),
(3, 1, '2026-01-01', 'Cash'),
(4, 2, '2026-01-01', 'Credit Card'),
(5, 3, '2026-01-01', 'Cash'),
(6, 4, '2026-01-01', 'Cash'),
(7, 5, '2026-01-01', 'Credit Card'),
(8, 6, '2026-01-01', 'Cash'),
(9, 7, '2026-01-01', 'Credit Card'),
(10, 2, '2026-01-01', 'Cash'),
(11, 3, '2026-01-01', 'Cash'),
(12, 4, '2026-01-01', 'Credit Card'),
(13, 5, '2026-01-01', 'Cash'),
(14, 1, '2026-01-01', 'Credit Card'),
(15, 2, '2026-01-01', 'Cash'),
(16, 3, '2026-01-01', 'Credit Card'),
(17, 4, '2026-01-01', 'Cash'),
(18, 5, '2026-01-01', 'Credit Card'),
(19, 6, '2026-01-01', 'Cash'),
(20, 7, '2026-01-01', 'Credit Card'),
(1, 2, '2026-01-01', 'Cash'),
(2, 3, '2026-01-01', 'Credit Card'),
(3, 4, '2026-01-01', 'Cash'),
(4, 5, '2026-01-01', 'Credit Card'),
(5, 6, '2026-01-01', 'Cash'),
(6, 1, '2026-01-01', 'Credit Card'),
(7, 2, '2026-01-01', 'Cash'),
(8, 3, '2026-01-01', 'Credit Card'),
(9, 4, '2026-01-01', 'Cash'),
(10, 5, '2026-01-01', 'Credit Card');

INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 101
(101, 1, 1, 2, 12.00),
(101, 2, 7, 1, 6.00),
-- Sale 102
(102, 1, 2, 1, 15.00),
(102, 2, 5, 1, 8.00),
-- Sale 103
(103, 1, 3, 2, 14.00),
-- Sale 104
(104, 1, 1, 1, 12.00),
(104, 2, 9, 2, 9.00),
-- Sale 105
(105, 1, 2, 2, 15.00),
-- Sale 106
(106, 1, 4, 1, 10.00),
(106, 2, 8, 1, 18.00),
-- Sale 107
(107, 1, 5, 1, 8.00),
(107, 2, 6, 1, 7.00),
-- Sale 108
(108, 1, 1, 3, 12.00),
-- Sale 109
(109, 1, 2, 1, 15.00),
(109, 2, 7, 2, 6.00),
-- Sale 110
(110, 1, 3, 2, 14.00),
-- Sale 111
(111, 1, 1, 2, 12.00),
(111, 2, 5, 1, 8.00),
-- Sale 112
(112, 1, 2, 1, 15.00),
-- Sale 113
(113, 1, 4, 1, 10.00),
(113, 2, 9, 1, 9.00),
-- Sale 114
(114, 1, 1, 2, 12.00),
-- Sale 115
(115, 1, 3, 1, 14.00),
(115, 2, 7, 2, 6.00),
-- Sale 116
(116, 1, 2, 2, 15.00),
-- Sale 117
(117, 1, 1, 1, 12.00),
(117, 2, 8, 1, 18.00),
-- Sale 118
(118, 1, 3, 2, 14.00),
-- Sale 119
(119, 1, 2, 1, 15.00),
(119, 2, 9, 1, 9.00),
-- Sale 120
(120, 1, 1, 2, 12.00),
(120, 2, 7, 1, 6.00),
-- Sale 121
(121, 1, 3, 1, 14.00),
-- Sale 122
(122, 1, 2, 2, 15.00),
(122, 2, 5, 1, 8.00),
-- Sale 123
(123, 1, 1, 1, 12.00),
-- Sale 124
(124, 1, 4, 2, 10.00),
-- Sale 125
(125, 1, 2, 1, 15.00),
(125, 2, 6, 1, 7.00),
-- Sale 126
(126, 1, 3, 2, 14.00),
-- Sale 127
(127, 1, 1, 2, 12.00),
(127, 2, 5, 1, 8.00),
-- Sale 128
(128, 1, 2, 1, 15.00),
-- Sale 129
(129, 1, 3, 1, 14.00),
(129, 2, 7, 2, 6.00),
-- Sale 130
(130, 1, 1, 2, 12.00),
(130, 2, 8, 1, 18.00);

-- ============================================================
-- 30 Sales for 2026-01-02
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1, 3, '2026-01-02', 'Cash'),
(2, 4, '2026-01-02', 'Credit Card'),
(3, 1, '2026-01-02', 'Cash'),
(4, 2, '2026-01-02', 'Credit Card'),
(5, 5, '2026-01-02', 'Cash'),
(6, 6, '2026-01-02', 'Credit Card'),
(7, 7, '2026-01-02', 'Cash'),
(8, 1, '2026-01-02', 'Credit Card'),
(9, 2, '2026-01-02', 'Cash'),
(10, 3, '2026-01-02', 'Credit Card'),
(11, 4, '2026-01-02', 'Cash'),
(12, 5, '2026-01-02', 'Credit Card'),
(13, 6, '2026-01-02', 'Cash'),
(14, 7, '2026-01-02', 'Credit Card'),
(15, 1, '2026-01-02', 'Cash'),
(16, 2, '2026-01-02', 'Credit Card'),
(17, 3, '2026-01-02', 'Cash'),
(18, 4, '2026-01-02', 'Credit Card'),
(19, 5, '2026-01-02', 'Cash'),
(20, 6, '2026-01-02', 'Credit Card'),
(1, 7, '2026-01-02', 'Cash'),
(2, 1, '2026-01-02', 'Credit Card'),
(3, 2, '2026-01-02', 'Cash'),
(4, 3, '2026-01-02', 'Credit Card'),
(5, 4, '2026-01-02', 'Cash'),
(6, 5, '2026-01-02', 'Credit Card'),
(7, 6, '2026-01-02', 'Cash'),
(8, 7, '2026-01-02', 'Credit Card'),
(9, 1, '2026-01-02', 'Cash'),
(10, 2, '2026-01-02', 'Credit Card');

-- Sale Details for these 30 sales
INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 131
(131, 1, 2, 1, 15.00),
(131, 2, 5, 1, 8.00),
-- Sale 132
(132, 1, 1, 2, 12.00),
-- Sale 133
(133, 1, 3, 1, 14.00),
(133, 2, 7, 2, 6.00),
-- Sale 134
(134, 1, 4, 1, 10.00),
-- Sale 135
(135, 1, 1, 2, 12.00),
(135, 2, 8, 1, 18.00),
-- Sale 136
(136, 1, 2, 1, 15.00),
-- Sale 137
(137, 1, 3, 2, 14.00),
(137, 2, 5, 1, 8.00),
-- Sale 138
(138, 1, 1, 1, 12.00),
(138, 2, 7, 1, 6.00),
-- Sale 139
(139, 1, 2, 2, 15.00),
-- Sale 140
(140, 1, 3, 1, 14.00),
(140, 2, 9, 2, 9.00),
-- Sale 141
(141, 1, 1, 2, 12.00),
-- Sale 142
(142, 1, 4, 1, 10.00),
(142, 2, 5, 1, 8.00),
-- Sale 143
(143, 1, 2, 1, 15.00),
-- Sale 144
(144, 1, 3, 2, 14.00),
(144, 2, 7, 1, 6.00),
-- Sale 145
(145, 1, 1, 1, 12.00),
(145, 2, 8, 1, 18.00),
-- Sale 146
(146, 1, 2, 2, 15.00),
-- Sale 147
(147, 1, 3, 1, 14.00),
(147, 2, 9, 1, 9.00),
-- Sale 148
(148, 1, 1, 2, 12.00),
-- Sale 149
(149, 1, 2, 1, 15.00),
(149, 2, 5, 1, 8.00),
-- Sale 150
(150, 1, 3, 2, 14.00),
(150, 2, 7, 1, 6.00);
-- ============================================================
-- 30 Sales for 2026-01-03
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1, 1, '2026-01-03', 'Cash'),
(2, 2, '2026-01-03', 'Credit Card'),
(3, 3, '2026-01-03', 'Cash'),
(4, 4, '2026-01-03', 'Credit Card'),
(5, 5, '2026-01-03', 'Cash'),
(6, 6, '2026-01-03', 'Credit Card'),
(7, 7, '2026-01-03', 'Cash'),
(8, 1, '2026-01-03', 'Credit Card'),
(9, 2, '2026-01-03', 'Cash'),
(10, 3, '2026-01-03', 'Credit Card'),
(11, 4, '2026-01-03', 'Cash'),
(12, 5, '2026-01-03', 'Credit Card'),
(13, 6, '2026-01-03', 'Cash'),
(14, 7, '2026-01-03', 'Credit Card'),
(15, 1, '2026-01-03', 'Cash'),
(16, 2, '2026-01-03', 'Credit Card'),
(17, 3, '2026-01-03', 'Cash'),
(18, 4, '2026-01-03', 'Credit Card'),
(19, 5, '2026-01-03', 'Cash'),
(20, 6, '2026-01-03', 'Credit Card'),
(1, 7, '2026-01-03', 'Cash'),
(2, 1, '2026-01-03', 'Credit Card'),
(3, 2, '2026-01-03', 'Cash'),
(4, 3, '2026-01-03', 'Credit Card'),
(5, 4, '2026-01-03', 'Cash'),
(6, 5, '2026-01-03', 'Credit Card'),
(7, 6, '2026-01-03', 'Cash'),
(8, 7, '2026-01-03', 'Credit Card'),
(9, 1, '2026-01-03', 'Cash'),
(10, 2, '2026-01-03', 'Credit Card');

-- SaleDetails for 30 Sales of Jan 3
INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 151
(151, 1, 1, 2, 12.00),
(151, 2, 7, 1, 6.00),
-- Sale 152
(152, 1, 2, 1, 15.00),
(152, 2, 5, 1, 8.00),
-- Sale 153
(153, 1, 3, 2, 14.00),
-- Sale 154
(154, 1, 1, 1, 12.00),
(154, 2, 9, 2, 9.00),
-- Sale 155
(155, 1, 2, 2, 15.00),
-- Sale 156
(156, 1, 4, 1, 10.00),
(156, 2, 8, 1, 18.00),
-- Sale 157
(157, 1, 5, 1, 8.00),
(157, 2, 6, 1, 7.00),
-- Sale 158
(158, 1, 1, 3, 12.00),
-- Sale 159
(159, 1, 2, 1, 15.00),
(159, 2, 7, 2, 6.00),
-- Sale 160
(160, 1, 3, 2, 14.00),
-- Sale 161
(161, 1, 1, 2, 12.00),
(161, 2, 5, 1, 8.00),
-- Sale 162
(162, 1, 2, 1, 15.00),
-- Sale 163
(163, 1, 4, 1, 10.00),
(163, 2, 9, 1, 9.00),
-- Sale 164
(164, 1, 1, 2, 12.00),
-- Sale 165
(165, 1, 3, 1, 14.00),
(165, 2, 7, 2, 6.00),
-- Sale 166
(166, 1, 2, 2, 15.00),
-- Sale 167
(167, 1, 1, 1, 12.00),
(167, 2, 8, 1, 18.00),
-- Sale 168
(168, 1, 3, 2, 14.00),
-- Sale 169
(169, 1, 2, 1, 15.00),
(169, 2, 9, 1, 9.00),
-- Sale 170
(170, 1, 1, 2, 12.00),
(170, 2, 7, 1, 6.00),
-- Sale 171
(171, 1, 3, 1, 14.00),
-- Sale 172
(172, 1, 2, 2, 15.00),
(172, 2, 5, 1, 8.00),
-- Sale 173
(173, 1, 1, 1, 12.00),
-- Sale 174
(174, 1, 4, 2, 10.00),
-- Sale 175
(175, 1, 2, 1, 15.00),
(175, 2, 6, 1, 7.00),
-- Sale 176
(176, 1, 3, 2, 14.00),
-- Sale 177
(177, 1, 1, 2, 12.00),
(177, 2, 5, 1, 8.00),
-- Sale 178
(178, 1, 2, 1, 15.00),
-- Sale 179
(179, 1, 3, 1, 14.00),
(179, 2, 7, 2, 6.00),
-- Sale 180
(180, 1, 1, 2, 12.00),
(180, 2, 8, 1, 18.00);
-- ============================================================
-- 30 Sales for 2026-01-04
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,2,'2026-01-04','Cash'),(2,3,'2026-01-04','Credit Card'),
(3,1,'2026-01-04','Cash'),(4,2,'2026-01-04','Credit Card'),
(5,3,'2026-01-04','Cash'),(6,4,'2026-01-04','Credit Card'),
(7,5,'2026-01-04','Cash'),(8,6,'2026-01-04','Credit Card'),
(9,7,'2026-01-04','Cash'),(10,1,'2026-01-04','Credit Card'),
(11,2,'2026-01-04','Cash'),(12,3,'2026-01-04','Credit Card'),
(13,4,'2026-01-04','Cash'),(14,5,'2026-01-04','Credit Card'),
(15,6,'2026-01-04','Cash'),(16,7,'2026-01-04','Credit Card'),
(17,1,'2026-01-04','Cash'),(18,2,'2026-01-04','Credit Card'),
(19,3,'2026-01-04','Cash'),(20,4,'2026-01-04','Credit Card'),
(1,5,'2026-01-04','Cash'),(2,6,'2026-01-04','Credit Card'),
(3,7,'2026-01-04','Cash'),(4,1,'2026-01-04','Credit Card'),
(5,2,'2026-01-04','Cash'),(6,3,'2026-01-04','Credit Card'),
(7,4,'2026-01-04','Cash'),(8,5,'2026-01-04','Credit Card'),
(9,6,'2026-01-04','Cash'),(10,7,'2026-01-04','Credit Card');

-- SaleDetails for 30 Sales of Jan 4
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(181,1,1,2,12.00),(181,2,7,1,6.00),
(182,1,2,1,15.00),(182,2,5,1,8.00),
(183,1,3,2,14.00),
(184,1,1,1,12.00),(184,2,9,2,9.00),
(185,1,2,2,15.00),
(186,1,4,1,10.00),(186,2,8,1,18.00),
(187,1,5,1,8.00),(187,2,6,1,7.00),
(188,1,1,3,12.00),
(189,1,2,1,15.00),(189,2,7,2,6.00),
(190,1,3,2,14.00),
(191,1,1,2,12.00),(191,2,5,1,8.00),
(192,1,2,1,15.00),
(193,1,4,1,10.00),(193,2,9,1,9.00),
(194,1,1,2,12.00),
(195,1,3,1,14.00),(195,2,7,2,6.00),
(196,1,2,2,15.00),
(197,1,1,1,12.00),(197,2,8,1,18.00),
(198,1,3,2,14.00),
(199,1,2,1,15.00),(199,2,9,1,9.00),
(200,1,1,2,12.00),(200,2,7,1,6.00),
(201,1,3,1,14.00),
(202,1,2,2,15.00),(202,2,5,1,8.00),
(203,1,1,1,12.00),
(204,1,4,2,10.00),
(205,1,2,1,15.00),(205,2,6,1,7.00),
(206,1,3,2,14.00),
(207,1,1,2,12.00),(207,2,5,1,8.00),
(208,1,2,1,15.00),
(209,1,3,1,14.00),(209,2,7,2,6.00),
(210,1,1,2,12.00),(210,2,8,1,18.00);

-- ============================================================
-- 30 Sales for 2026-01-05
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,2,'2026-01-05','Cash'),(2,3,'2026-01-05','Credit Card'),
(3,4,'2026-01-05','Cash'),(4,5,'2026-01-05','Credit Card'),
(5,6,'2026-01-05','Cash'),(6,7,'2026-01-05','Credit Card'),
(7,1,'2026-01-05','Cash'),(8,2,'2026-01-05','Credit Card'),
(9,3,'2026-01-05','Cash'),(10,4,'2026-01-05','Credit Card'),
(11,5,'2026-01-05','Cash'),(12,6,'2026-01-05','Credit Card'),
(13,7,'2026-01-05','Cash'),(14,1,'2026-01-05','Credit Card'),
(15,2,'2026-01-05','Cash'),(16,3,'2026-01-05','Credit Card'),
(17,4,'2026-01-05','Cash'),(18,5,'2026-01-05','Credit Card'),
(19,6,'2026-01-05','Cash'),(20,7,'2026-01-05','Credit Card'),
(1,1,'2026-01-05','Cash'),(2,2,'2026-01-05','Credit Card'),
(3,3,'2026-01-05','Cash'),(4,4,'2026-01-05','Credit Card'),
(5,5,'2026-01-05','Cash'),(6,6,'2026-01-05','Credit Card'),
(7,7,'2026-01-05','Cash'),(8,1,'2026-01-05','Credit Card'),
(9,2,'2026-01-05','Cash'),(10,3,'2026-01-05','Credit Card');

-- SaleDetails for 30 Sales of Jan 5
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(211,1,1,2,12.00),(211,2,7,1,6.00),
(212,1,2,1,15.00),(212,2,5,1,8.00),
(213,1,3,2,14.00),
(214,1,1,1,12.00),(214,2,9,2,9.00),
(215,1,2,2,15.00),
(216,1,4,1,10.00),(216,2,8,1,18.00),
(217,1,5,1,8.00),(217,2,6,1,7.00),
(218,1,1,3,12.00),
(219,1,2,1,15.00),(219,2,7,2,6.00),
(220,1,3,2,14.00),
(221,1,1,2,12.00),(221,2,5,1,8.00),
(222,1,2,1,15.00),
(223,1,4,1,10.00),(223,2,9,1,9.00),
(224,1,1,2,12.00),
(225,1,3,1,14.00),(225,2,7,2,6.00),
(226,1,2,2,15.00),
(227,1,1,1,12.00),(227,2,8,1,18.00),
(228,1,3,2,14.00),
(229,1,2,1,15.00),(229,2,9,1,9.00),
(230,1,1,2,12.00),(230,2,7,1,6.00),
(231,1,3,1,14.00),
(232,1,2,2,15.00),(232,2,5,1,8.00),
(233,1,1,1,12.00),
(234,1,4,2,10.00),
(235,1,2,1,15.00),(235,2,6,1,7.00),
(236,1,3,2,14.00),
(237,1,1,2,12.00),(237,2,5,1,8.00),
(238,1,2,1,15.00),
(239,1,3,1,14.00),(239,2,7,2,6.00),
(240,1,1,2,12.00),(240,2,8,1,18.00);

-- ============================================================
-- 30 Sales for 2026-01-06
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,3,'2026-01-06','Cash'),(2,4,'2026-01-06','Credit Card'),
(3,5,'2026-01-06','Cash'),(4,6,'2026-01-06','Credit Card'),
(5,7,'2026-01-06','Cash'),(6,1,'2026-01-06','Credit Card'),
(7,2,'2026-01-06','Cash'),(8,3,'2026-01-06','Credit Card'),
(9,4,'2026-01-06','Cash'),(10,5,'2026-01-06','Credit Card'),
(11,6,'2026-01-06','Cash'),(12,7,'2026-01-06','Credit Card'),
(13,1,'2026-01-06','Cash'),(14,2,'2026-01-06','Credit Card'),
(15,3,'2026-01-06','Cash'),(16,4,'2026-01-06','Credit Card'),
(17,5,'2026-01-06','Cash'),(18,6,'2026-01-06','Credit Card'),
(19,7,'2026-01-06','Cash'),(20,1,'2026-01-06','Credit Card'),
(1,2,'2026-01-06','Cash'),(2,3,'2026-01-06','Credit Card'),
(3,4,'2026-01-06','Cash'),(4,5,'2026-01-06','Credit Card'),
(5,6,'2026-01-06','Cash'),(6,7,'2026-01-06','Credit Card'),
(7,1,'2026-01-06','Cash'),(8,2,'2026-01-06','Credit Card'),
(9,3,'2026-01-06','Cash'),(10,4,'2026-01-06','Credit Card');

-- SaleDetails for 30 Sales of Jan 6
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(241,1,1,2,12.00),(241,2,7,1,6.00),
(242,1,2,1,15.00),(242,2,5,1,8.00),
(243,1,3,2,14.00),
(244,1,1,1,12.00),(244,2,9,2,9.00),
(245,1,2,2,15.00),
(246,1,4,1,10.00),(246,2,8,1,18.00),
(247,1,5,1,8.00),(247,2,6,1,7.00),
(248,1,1,3,12.00),
(249,1,2,1,15.00),(249,2,7,2,6.00),
(250,1,3,2,14.00),
(251,1,1,2,12.00),(251,2,5,1,8.00),
(252,1,2,1,15.00),
(253,1,4,1,10.00),(253,2,9,1,9.00),
(254,1,1,2,12.00),
(255,1,3,1,14.00),(255,2,7,2,6.00),
(256,1,2,2,15.00),
(257,1,1,1,12.00),(257,2,8,1,18.00),
(258,1,3,2,14.00),
(259,1,2,1,15.00),(259,2,9,1,9.00),
(260,1,1,2,12.00),(260,2,7,1,6.00),
(261,1,3,1,14.00),
(262,1,2,2,15.00),(262,2,5,1,8.00),
(263,1,1,1,12.00),
(264,1,4,2,10.00),
(265,1,2,1,15.00),(265,2,6,1,7.00),
(266,1,3,2,14.00),
(267,1,1,2,12.00),(267,2,5,1,8.00),
(268,1,2,1,15.00),
(269,1,3,1,14.00),(269,2,7,2,6.00),
(270,1,1,2,12.00),(270,2,8,1,18.00);

-- ============================================================
-- 20 Sales for 2026-01-07
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-07','Cash'),(2,2,'2026-01-07','Credit Card'),
(3,3,'2026-01-07','Cash'),(4,4,'2026-01-07','Credit Card'),
(5,5,'2026-01-07','Cash'),(6,6,'2026-01-07','Credit Card'),
(7,7,'2026-01-07','Cash'),(8,1,'2026-01-07','Credit Card'),
(9,2,'2026-01-07','Cash'),(10,3,'2026-01-07','Credit Card'),
(11,4,'2026-01-07','Cash'),(12,5,'2026-01-07','Credit Card'),
(13,6,'2026-01-07','Cash'),(14,7,'2026-01-07','Credit Card'),
(15,1,'2026-01-07','Cash'),(16,2,'2026-01-07','Credit Card'),
(17,3,'2026-01-07','Cash'),(18,4,'2026-01-07','Credit Card'),
(19,5,'2026-01-07','Cash'),(20,6,'2026-01-07','Credit Card');

-- SaleDetails for 20 Sales of Jan 7
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(271,1,1,2,12.00),(271,2,7,1,6.00),
(272,1,2,1,15.00),(272,2,5,1,8.00),
(273,1,3,2,14.00),
(274,1,1,1,12.00),(274,2,9,2,9.00),
(275,1,2,2,15.00),
(276,1,4,1,10.00),(276,2,8,1,18.00),
(277,1,5,1,8.00),(277,2,6,1,7.00),
(278,1,1,3,12.00),
(279,1,2,1,15.00),(279,2,7,2,6.00),
(280,1,3,2,14.00),
(281,1,1,2,12.00),(281,2,5,1,8.00),
(282,1,2,1,15.00),
(283,1,4,1,10.00),(283,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-08
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-08','Cash'),(2,2,'2026-01-08','Credit Card'),
(3,3,'2026-01-08','Cash'),(4,4,'2026-01-08','Credit Card'),
(5,5,'2026-01-08','Cash'),(6,6,'2026-01-08','Credit Card'),
(7,7,'2026-01-08','Cash'),(8,1,'2026-01-08','Credit Card'),
(9,2,'2026-01-08','Cash'),(10,3,'2026-01-08','Credit Card'),
(11,4,'2026-01-08','Cash'),(12,5,'2026-01-08','Credit Card'),
(13,6,'2026-01-08','Cash'),(14,7,'2026-01-08','Credit Card'),
(15,1,'2026-01-08','Cash'),(16,2,'2026-01-08','Credit Card'),
(17,3,'2026-01-08','Cash'),(18,4,'2026-01-08','Credit Card'),
(19,5,'2026-01-08','Cash'),(20,6,'2026-01-08','Credit Card');

-- SaleDetails for 20 Sales of Jan 8
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(284,1,1,2,12.00),(284,2,7,1,6.00),
(285,1,2,1,15.00),(285,2,5,1,8.00),
(286,1,3,2,14.00),
(287,1,1,1,12.00),(287,2,9,2,9.00),
(288,1,2,2,15.00),
(289,1,4,1,10.00),(289,2,8,1,18.00),
(290,1,5,1,8.00),(290,2,6,1,7.00),
(291,1,1,3,12.00),
(292,1,2,1,15.00),(292,2,7,2,6.00),
(293,1,3,2,14.00),
(294,1,1,2,12.00),(294,2,5,1,8.00),
(295,1,2,1,15.00),
(296,1,4,1,10.00),(296,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-09
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-09','Cash'),(2,2,'2026-01-09','Credit Card'),
(3,3,'2026-01-09','Cash'),(4,4,'2026-01-09','Credit Card'),
(5,5,'2026-01-09','Cash'),(6,6,'2026-01-09','Credit Card'),
(7,7,'2026-01-09','Cash'),(8,1,'2026-01-09','Credit Card'),
(9,2,'2026-01-09','Cash'),(10,3,'2026-01-09','Credit Card'),
(11,4,'2026-01-09','Cash'),(12,5,'2026-01-09','Credit Card'),
(13,6,'2026-01-09','Cash'),(14,7,'2026-01-09','Credit Card'),
(15,1,'2026-01-09','Cash'),(16,2,'2026-01-09','Credit Card'),
(17,3,'2026-01-09','Cash'),(18,4,'2026-01-09','Credit Card'),
(19,5,'2026-01-09','Cash'),(20,6,'2026-01-09','Credit Card');

-- SaleDetails for 20 Sales of Jan 9
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(297,1,1,2,12.00),(297,2,7,1,6.00),
(298,1,2,1,15.00),(298,2,5,1,8.00),
(299,1,3,2,14.00),
(300,1,1,1,12.00),(300,2,9,2,9.00),
(301,1,2,2,15.00),
(302,1,4,1,10.00),(302,2,8,1,18.00),
(303,1,5,1,8.00),(303,2,6,1,7.00),
(304,1,1,3,12.00),
(305,1,2,1,15.00),(305,2,7,2,6.00),
(306,1,3,2,14.00),
(307,1,1,2,12.00),(307,2,5,1,8.00),
(308,1,2,1,15.00),
(309,1,4,1,10.00),(309,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-10
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-10','Cash'),(2,2,'2026-01-10','Credit Card'),
(3,3,'2026-01-10','Cash'),(4,4,'2026-01-10','Credit Card'),
(5,5,'2026-01-10','Cash'),(6,6,'2026-01-10','Credit Card'),
(7,7,'2026-01-10','Cash'),(8,1,'2026-01-10','Credit Card'),
(9,2,'2026-01-10','Cash'),(10,3,'2026-01-10','Credit Card'),
(11,4,'2026-01-10','Cash'),(12,5,'2026-01-10','Credit Card'),
(13,6,'2026-01-10','Cash'),(14,7,'2026-01-10','Credit Card'),
(15,1,'2026-01-10','Cash'),(16,2,'2026-01-10','Credit Card'),
(17,3,'2026-01-10','Cash'),(18,4,'2026-01-10','Credit Card'),
(19,5,'2026-01-10','Cash'),(20,6,'2026-01-10','Credit Card');

-- SaleDetails for 20 Sales of Jan 10
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(310,1,1,2,12.00),(310,2,7,1,6.00),
(311,1,2,1,15.00),(311,2,5,1,8.00),
(312,1,3,2,14.00),
(313,1,1,1,12.00),(313,2,9,2,9.00),
(314,1,2,2,15.00),
(315,1,4,1,10.00),(315,2,8,1,18.00),
(316,1,5,1,8.00),(316,2,6,1,7.00),
(317,1,1,3,12.00),
(318,1,2,1,15.00),(318,2,7,2,6.00),
(319,1,3,2,14.00),
(320,1,1,2,12.00),(320,2,5,1,8.00),
(321,1,2,1,15.00),
(322,1,4,1,10.00),(322,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-11
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-11','Cash'),(2,2,'2026-01-11','Credit Card'),
(3,3,'2026-01-11','Cash'),(4,4,'2026-01-11','Credit Card'),
(5,5,'2026-01-11','Cash'),(6,6,'2026-01-11','Credit Card'),
(7,7,'2026-01-11','Cash'),(8,1,'2026-01-11','Credit Card'),
(9,2,'2026-01-11','Cash'),(10,3,'2026-01-11','Credit Card'),
(11,4,'2026-01-11','Cash'),(12,5,'2026-01-11','Credit Card'),
(13,6,'2026-01-11','Cash'),(14,7,'2026-01-11','Credit Card'),
(15,1,'2026-01-11','Cash'),(16,2,'2026-01-11','Credit Card'),
(17,3,'2026-01-11','Cash'),(18,4,'2026-01-11','Credit Card'),
(19,5,'2026-01-11','Cash'),(20,6,'2026-01-11','Credit Card');

-- SaleDetails for 20 Sales of Jan 11
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(323,1,1,2,12.00),(323,2,7,1,6.00),
(324,1,2,1,15.00),(324,2,5,1,8.00),
(325,1,3,2,14.00),
(326,1,1,1,12.00),(326,2,9,2,9.00),
(327,1,2,2,15.00),
(328,1,4,1,10.00),(328,2,8,1,18.00),
(329,1,5,1,8.00),(329,2,6,1,7.00),
(330,1,1,3,12.00),
(331,1,2,1,15.00),(331,2,7,2,6.00),
(332,1,3,2,14.00),
(333,1,1,2,12.00),(333,2,5,1,8.00),
(334,1,2,1,15.00),
(335,1,4,1,10.00),(335,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-12
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-12','Cash'),(2,2,'2026-01-12','Credit Card'),
(3,3,'2026-01-12','Cash'),(4,4,'2026-01-12','Credit Card'),
(5,5,'2026-01-12','Cash'),(6,6,'2026-01-12','Credit Card'),
(7,7,'2026-01-12','Cash'),(8,1,'2026-01-12','Credit Card'),
(9,2,'2026-01-12','Cash'),(10,3,'2026-01-12','Credit Card'),
(11,4,'2026-01-12','Cash'),(12,5,'2026-01-12','Credit Card'),
(13,6,'2026-01-12','Cash'),(14,7,'2026-01-12','Credit Card'),
(15,1,'2026-01-12','Cash'),(16,2,'2026-01-12','Credit Card'),
(17,3,'2026-01-12','Cash'),(18,4,'2026-01-12','Credit Card'),
(19,5,'2026-01-12','Cash'),(20,6,'2026-01-12','Credit Card');

-- SaleDetails for 20 Sales of Jan 12
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(336,1,1,2,12.00),(336,2,7,1,6.00),
(337,1,2,1,15.00),(337,2,5,1,8.00),
(338,1,3,2,14.00),
(339,1,1,1,12.00),(339,2,9,2,9.00),
(340,1,2,2,15.00),
(341,1,4,1,10.00),(341,2,8,1,18.00),
(342,1,5,1,8.00),(342,2,6,1,7.00),
(343,1,1,3,12.00),
(344,1,2,1,15.00),(344,2,7,2,6.00),
(345,1,3,2,14.00),
(346,1,1,2,12.00),(346,2,5,1,8.00),
(347,1,2,1,15.00),
(348,1,4,1,10.00),(348,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-13
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-13','Cash'),(2,2,'2026-01-13','Credit Card'),
(3,3,'2026-01-13','Cash'),(4,4,'2026-01-13','Credit Card'),
(5,5,'2026-01-13','Cash'),(6,6,'2026-01-13','Credit Card'),
(7,7,'2026-01-13','Cash'),(8,1,'2026-01-13','Credit Card'),
(9,2,'2026-01-13','Cash'),(10,3,'2026-01-13','Credit Card'),
(11,4,'2026-01-13','Cash'),(12,5,'2026-01-13','Credit Card'),
(13,6,'2026-01-13','Cash'),(14,7,'2026-01-13','Credit Card'),
(15,1,'2026-01-13','Cash'),(16,2,'2026-01-13','Credit Card'),
(17,3,'2026-01-13','Cash'),(18,4,'2026-01-13','Credit Card'),
(19,5,'2026-01-13','Cash'),(20,6,'2026-01-13','Credit Card');

-- SaleDetails for 20 Sales of Jan 13
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(349,1,1,2,12.00),(349,2,7,1,6.00),
(350,1,2,1,15.00),(350,2,5,1,8.00),
(351,1,3,2,14.00),
(352,1,1,1,12.00),(352,2,9,2,9.00),
(353,1,2,2,15.00),
(354,1,4,1,10.00),(354,2,8,1,18.00),
(355,1,5,1,8.00),(355,2,6,1,7.00),
(356,1,1,3,12.00),
(357,1,2,1,15.00),(357,2,7,2,6.00),
(358,1,3,2,14.00),
(359,1,1,2,12.00),(359,2,5,1,8.00),
(360,1,2,1,15.00),
(361,1,4,1,10.00),(361,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-14
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-14','Cash'),(2,2,'2026-01-14','Credit Card'),
(3,3,'2026-01-14','Cash'),(4,4,'2026-01-14','Credit Card'),
(5,5,'2026-01-14','Cash'),(6,6,'2026-01-14','Credit Card'),
(7,7,'2026-01-14','Cash'),(8,1,'2026-01-14','Credit Card'),
(9,2,'2026-01-14','Cash'),(10,3,'2026-01-14','Credit Card'),
(11,4,'2026-01-14','Cash'),(12,5,'2026-01-14','Credit Card'),
(13,6,'2026-01-14','Cash'),(14,7,'2026-01-14','Credit Card'),
(15,1,'2026-01-14','Cash'),(16,2,'2026-01-14','Credit Card'),
(17,3,'2026-01-14','Cash'),(18,4,'2026-01-14','Credit Card'),
(19,5,'2026-01-14','Cash'),(20,6,'2026-01-14','Credit Card');

-- SaleDetails for 20 Sales of Jan 14
INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(362,1,1,2,12.00),(362,2,7,1,6.00),
(363,1,2,1,15.00),(363,2,5,1,8.00),
(364,1,3,2,14.00),
(365,1,1,1,12.00),(365,2,9,2,9.00),
(366,1,2,2,15.00),
(367,1,4,1,10.00),(367,2,8,1,18.00),
(368,1,5,1,8.00),(368,2,6,1,7.00),
(369,1,1,3,12.00),
(370,1,2,1,15.00),(370,2,7,2,6.00),
(371,1,3,2,14.00),
(372,1,1,2,12.00),(372,2,5,1,8.00),
(373,1,2,1,15.00),
(374,1,4,1,10.00),(374,2,9,1,9.00);
-- ============================================================
-- 20 Sales for 2026-01-15
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-15','Cash'),(2,2,'2026-01-15','Credit Card'),
(3,3,'2026-01-15','Cash'),(4,4,'2026-01-15','Credit Card'),
(5,5,'2026-01-15','Cash'),(6,6,'2026-01-15','Credit Card'),
(7,7,'2026-01-15','Cash'),(8,1,'2026-01-15','Credit Card'),
(9,2,'2026-01-15','Cash'),(10,3,'2026-01-15','Credit Card'),
(11,4,'2026-01-15','Cash'),(12,5,'2026-01-15','Credit Card'),
(13,6,'2026-01-15','Cash'),(14,7,'2026-01-15','Credit Card'),
(15,1,'2026-01-15','Cash'),(16,2,'2026-01-15','Credit Card'),
(17,3,'2026-01-15','Cash'),(18,4,'2026-01-15','Credit Card'),
(19,5,'2026-01-15','Cash'),(20,6,'2026-01-15','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(375,1,1,2,12.00),(375,2,7,1,6.00),
(376,1,2,1,15.00),(376,2,5,1,8.00),
(377,1,3,2,14.00),
(378,1,1,1,12.00),(378,2,9,2,9.00),
(379,1,2,2,15.00),
(380,1,4,1,10.00),(380,2,8,1,18.00),
(381,1,5,1,8.00),(381,2,6,1,7.00),
(382,1,1,3,12.00),
(383,1,2,1,15.00),(383,2,7,2,6.00),
(384,1,3,2,14.00),
(385,1,1,2,12.00),(385,2,5,1,8.00),
(386,1,2,1,15.00),
(387,1,4,1,10.00),(387,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-16
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-16','Cash'),(2,2,'2026-01-16','Credit Card'),
(3,3,'2026-01-16','Cash'),(4,4,'2026-01-16','Credit Card'),
(5,5,'2026-01-16','Cash'),(6,6,'2026-01-16','Credit Card'),
(7,7,'2026-01-16','Cash'),(8,1,'2026-01-16','Credit Card'),
(9,2,'2026-01-16','Cash'),(10,3,'2026-01-16','Credit Card'),
(11,4,'2026-01-16','Cash'),(12,5,'2026-01-16','Credit Card'),
(13,6,'2026-01-16','Cash'),(14,7,'2026-01-16','Credit Card'),
(15,1,'2026-01-16','Cash'),(16,2,'2026-01-16','Credit Card'),
(17,3,'2026-01-16','Cash'),(18,4,'2026-01-16','Credit Card'),
(19,5,'2026-01-16','Cash'),(20,6,'2026-01-16','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(388,1,1,2,12.00),(388,2,7,1,6.00),
(389,1,2,1,15.00),(389,2,5,1,8.00),
(390,1,3,2,14.00),
(391,1,1,1,12.00),(391,2,9,2,9.00),
(392,1,2,2,15.00),
(393,1,4,1,10.00),(393,2,8,1,18.00),
(394,1,5,1,8.00),(394,2,6,1,7.00),
(395,1,1,3,12.00),
(396,1,2,1,15.00),(396,2,7,2,6.00),
(397,1,3,2,14.00),
(398,1,1,2,12.00),(398,2,5,1,8.00),
(399,1,2,1,15.00),
(400,1,4,1,10.00),(400,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-17
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-17','Cash'),(2,2,'2026-01-17','Credit Card'),
(3,3,'2026-01-17','Cash'),(4,4,'2026-01-17','Credit Card'),
(5,5,'2026-01-17','Cash'),(6,6,'2026-01-17','Credit Card'),
(7,7,'2026-01-17','Cash'),(8,1,'2026-01-17','Credit Card'),
(9,2,'2026-01-17','Cash'),(10,3,'2026-01-17','Credit Card'),
(11,4,'2026-01-17','Cash'),(12,5,'2026-01-17','Credit Card'),
(13,6,'2026-01-17','Cash'),(14,7,'2026-01-17','Credit Card'),
(15,1,'2026-01-17','Cash'),(16,2,'2026-01-17','Credit Card'),
(17,3,'2026-01-17','Cash'),(18,4,'2026-01-17','Credit Card'),
(19,5,'2026-01-17','Cash'),(20,6,'2026-01-17','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(401,1,1,2,12.00),(401,2,7,1,6.00),
(402,1,2,1,15.00),(402,2,5,1,8.00),
(403,1,3,2,14.00),
(404,1,1,1,12.00),(404,2,9,2,9.00),
(405,1,2,2,15.00),
(406,1,4,1,10.00),(406,2,8,1,18.00),
(407,1,5,1,8.00),(407,2,6,1,7.00),
(408,1,1,3,12.00),
(409,1,2,1,15.00),(409,2,7,2,6.00),
(410,1,3,2,14.00),
(411,1,1,2,12.00),(411,2,5,1,8.00),
(412,1,2,1,15.00),
(413,1,4,1,10.00),(413,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-18
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-18','Cash'),(2,2,'2026-01-18','Credit Card'),
(3,3,'2026-01-18','Cash'),(4,4,'2026-01-18','Credit Card'),
(5,5,'2026-01-18','Cash'),(6,6,'2026-01-18','Credit Card'),
(7,7,'2026-01-18','Cash'),(8,1,'2026-01-18','Credit Card'),
(9,2,'2026-01-18','Cash'),(10,3,'2026-01-18','Credit Card'),
(11,4,'2026-01-18','Cash'),(12,5,'2026-01-18','Credit Card'),
(13,6,'2026-01-18','Cash'),(14,7,'2026-01-18','Credit Card'),
(15,1,'2026-01-18','Cash'),(16,2,'2026-01-18','Credit Card'),
(17,3,'2026-01-18','Cash'),(18,4,'2026-01-18','Credit Card'),
(19,5,'2026-01-18','Cash'),(20,6,'2026-01-18','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(414,1,1,2,12.00),(414,2,7,1,6.00),
(415,1,2,1,15.00),(415,2,5,1,8.00),
(416,1,3,2,14.00),
(417,1,1,1,12.00),(417,2,9,2,9.00),
(418,1,2,2,15.00),
(419,1,4,1,10.00),(419,2,8,1,18.00),
(420,1,5,1,8.00),(420,2,6,1,7.00),
(421,1,1,3,12.00),
(422,1,2,1,15.00),(422,2,7,2,6.00),
(423,1,3,2,14.00),
(424,1,1,2,12.00),(424,2,5,1,8.00),
(425,1,2,1,15.00),
(426,1,4,1,10.00),(426,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-19
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-19','Cash'),(2,2,'2026-01-19','Credit Card'),
(3,3,'2026-01-19','Cash'),(4,4,'2026-01-19','Credit Card'),
(5,5,'2026-01-19','Cash'),(6,6,'2026-01-19','Credit Card'),
(7,7,'2026-01-19','Cash'),(8,1,'2026-01-19','Credit Card'),
(9,2,'2026-01-19','Cash'),(10,3,'2026-01-19','Credit Card'),
(11,4,'2026-01-19','Cash'),(12,5,'2026-01-19','Credit Card'),
(13,6,'2026-01-19','Cash'),(14,7,'2026-01-19','Credit Card'),
(15,1,'2026-01-19','Cash'),(16,2,'2026-01-19','Credit Card'),
(17,3,'2026-01-19','Cash'),(18,4,'2026-01-19','Credit Card'),
(19,5,'2026-01-19','Cash'),(20,6,'2026-01-19','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(427,1,1,2,12.00),(427,2,7,1,6.00),
(428,1,2,1,15.00),(428,2,5,1,8.00),
(429,1,3,2,14.00),
(430,1,1,1,12.00),(430,2,9,2,9.00),
(431,1,2,2,15.00),
(432,1,4,1,10.00),(432,2,8,1,18.00),
(433,1,5,1,8.00),(433,2,6,1,7.00),
(434,1,1,3,12.00),
(435,1,2,1,15.00),(435,2,7,2,6.00),
(436,1,3,2,14.00),
(437,1,1,2,12.00),(437,2,5,1,8.00),
(438,1,2,1,15.00),
(439,1,4,1,10.00),(439,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-20
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-20','Cash'),(2,2,'2026-01-20','Credit Card'),
(3,3,'2026-01-20','Cash'),(4,4,'2026-01-20','Credit Card'),
(5,5,'2026-01-20','Cash'),(6,6,'2026-01-20','Credit Card'),
(7,7,'2026-01-20','Cash'),(8,1,'2026-01-20','Credit Card'),
(9,2,'2026-01-20','Cash'),(10,3,'2026-01-20','Credit Card'),
(11,4,'2026-01-20','Cash'),(12,5,'2026-01-20','Credit Card'),
(13,6,'2026-01-20','Cash'),(14,7,'2026-01-20','Credit Card'),
(15,1,'2026-01-20','Cash'),(16,2,'2026-01-20','Credit Card'),
(17,3,'2026-01-20','Cash'),(18,4,'2026-01-20','Credit Card'),
(19,5,'2026-01-20','Cash'),(20,6,'2026-01-20','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(440,1,1,2,12.00),(440,2,7,1,6.00),
(441,1,2,1,15.00),(441,2,5,1,8.00),
(442,1,3,2,14.00),
(443,1,1,1,12.00),(443,2,9,2,9.00),
(444,1,2,2,15.00),
(445,1,4,1,10.00),(445,2,8,1,18.00),
(446,1,5,1,8.00),(446,2,6,1,7.00),
(447,1,1,3,12.00),
(448,1,2,1,15.00),(448,2,7,2,6.00),
(449,1,3,2,14.00),
(450,1,1,2,12.00),(450,2,5,1,8.00),
(451,1,2,1,15.00),
(452,1,4,1,10.00),(452,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-21
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-21','Cash'),(2,2,'2026-01-21','Credit Card'),
(3,3,'2026-01-21','Cash'),(4,4,'2026-01-21','Credit Card'),
(5,5,'2026-01-21','Cash'),(6,6,'2026-01-21','Credit Card'),
(7,7,'2026-01-21','Cash'),(8,1,'2026-01-21','Credit Card'),
(9,2,'2026-01-21','Cash'),(10,3,'2026-01-21','Credit Card'),
(11,4,'2026-01-21','Cash'),(12,5,'2026-01-21','Credit Card'),
(13,6,'2026-01-21','Cash'),(14,7,'2026-01-21','Credit Card'),
(15,1,'2026-01-21','Cash'),(16,2,'2026-01-21','Credit Card'),
(17,3,'2026-01-21','Cash'),(18,4,'2026-01-21','Credit Card'),
(19,5,'2026-01-21','Cash'),(20,6,'2026-01-21','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(453,1,1,2,12.00),(453,2,7,1,6.00),
(454,1,2,1,15.00),(454,2,5,1,8.00),
(455,1,3,2,14.00),
(456,1,1,1,12.00),(456,2,9,2,9.00),
(457,1,2,2,15.00),
(458,1,4,1,10.00),(458,2,8,1,18.00),
(459,1,5,1,8.00),(459,2,6,1,7.00),
(460,1,1,3,12.00),
(461,1,2,1,15.00),(461,2,7,2,6.00),
(462,1,3,2,14.00),
(463,1,1,2,12.00),(463,2,5,1,8.00),
(464,1,2,1,15.00),
(465,1,4,1,10.00),(465,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-22
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-22','Cash'),(2,2,'2026-01-22','Credit Card'),
(3,3,'2026-01-22','Cash'),(4,4,'2026-01-22','Credit Card'),
(5,5,'2026-01-22','Cash'),(6,6,'2026-01-22','Credit Card'),
(7,7,'2026-01-22','Cash'),(8,1,'2026-01-22','Credit Card'),
(9,2,'2026-01-22','Cash'),(10,3,'2026-01-22','Credit Card'),
(11,4,'2026-01-22','Cash'),(12,5,'2026-01-22','Credit Card'),
(13,6,'2026-01-22','Cash'),(14,7,'2026-01-22','Credit Card'),
(15,1,'2026-01-22','Cash'),(16,2,'2026-01-22','Credit Card'),
(17,3,'2026-01-22','Cash'),(18,4,'2026-01-22','Credit Card'),
(19,5,'2026-01-22','Cash'),(20,6,'2026-01-22','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(466,1,1,2,12.00),(466,2,7,1,6.00),
(467,1,2,1,15.00),(467,2,5,1,8.00),
(468,1,3,2,14.00),
(469,1,1,1,12.00),(469,2,9,2,9.00),
(470,1,2,2,15.00),
(471,1,4,1,10.00),(471,2,8,1,18.00),
(472,1,5,1,8.00),(472,2,6,1,7.00),
(473,1,1,3,12.00),
(474,1,2,1,15.00),(474,2,7,2,6.00),
(475,1,3,2,14.00),
(476,1,1,2,12.00),(476,2,5,1,8.00),
(477,1,2,1,15.00),
(478,1,4,1,10.00),(478,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-23
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-23','Cash'),(2,2,'2026-01-23','Credit Card'),
(3,3,'2026-01-23','Cash'),(4,4,'2026-01-23','Credit Card'),
(5,5,'2026-01-23','Cash'),(6,6,'2026-01-23','Credit Card'),
(7,7,'2026-01-23','Cash'),(8,1,'2026-01-23','Credit Card'),
(9,2,'2026-01-23','Cash'),(10,3,'2026-01-23','Credit Card'),
(11,4,'2026-01-23','Cash'),(12,5,'2026-01-23','Credit Card'),
(13,6,'2026-01-23','Cash'),(14,7,'2026-01-23','Credit Card'),
(15,1,'2026-01-23','Cash'),(16,2,'2026-01-23','Credit Card'),
(17,3,'2026-01-23','Cash'),(18,4,'2026-01-23','Credit Card'),
(19,5,'2026-01-23','Cash'),(20,6,'2026-01-23','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(479,1,1,2,12.00),(479,2,7,1,6.00),
(480,1,2,1,15.00),(480,2,5,1,8.00),
(481,1,3,2,14.00),
(482,1,1,1,12.00),(482,2,9,2,9.00),
(483,1,2,2,15.00),
(484,1,4,1,10.00),(484,2,8,1,18.00),
(485,1,5,1,8.00),(485,2,6,1,7.00),
(486,1,1,3,12.00),
(487,1,2,1,15.00),(487,2,7,2,6.00),
(488,1,3,2,14.00),
(489,1,1,2,12.00),(489,2,5,1,8.00),
(490,1,2,1,15.00),
(491,1,4,1,10.00),(491,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-24
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-24','Cash'),(2,2,'2026-01-24','Credit Card'),
(3,3,'2026-01-24','Cash'),(4,4,'2026-01-24','Credit Card'),
(5,5,'2026-01-24','Cash'),(6,6,'2026-01-24','Credit Card'),
(7,7,'2026-01-24','Cash'),(8,1,'2026-01-24','Credit Card'),
(9,2,'2026-01-24','Cash'),(10,3,'2026-01-24','Credit Card'),
(11,4,'2026-01-24','Cash'),(12,5,'2026-01-24','Credit Card'),
(13,6,'2026-01-24','Cash'),(14,7,'2026-01-24','Credit Card'),
(15,1,'2026-01-24','Cash'),(16,2,'2026-01-24','Credit Card'),
(17,3,'2026-01-24','Cash'),(18,4,'2026-01-24','Credit Card'),
(19,5,'2026-01-24','Cash'),(20,6,'2026-01-24','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(492,1,1,2,12.00),(492,2,7,1,6.00),
(493,1,2,1,15.00),(493,2,5,1,8.00),
(494,1,3,2,14.00),
(495,1,1,1,12.00),(495,2,9,2,9.00),
(496,1,2,2,15.00),
(497,1,4,1,10.00),(497,2,8,1,18.00),
(498,1,5,1,8.00),(498,2,6,1,7.00),
(499,1,1,3,12.00),
(500,1,2,1,15.00),(500,2,7,2,6.00),
(501,1,3,2,14.00),
(502,1,1,2,12.00),(502,2,5,1,8.00),
(503,1,2,1,15.00),
(504,1,4,1,10.00),(504,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-25
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-25','Cash'),(2,2,'2026-01-25','Credit Card'),
(3,3,'2026-01-25','Cash'),(4,4,'2026-01-25','Credit Card'),
(5,5,'2026-01-25','Cash'),(6,6,'2026-01-25','Credit Card'),
(7,7,'2026-01-25','Cash'),(8,1,'2026-01-25','Credit Card'),
(9,2,'2026-01-25','Cash'),(10,3,'2026-01-25','Credit Card'),
(11,4,'2026-01-25','Cash'),(12,5,'2026-01-25','Credit Card'),
(13,6,'2026-01-25','Cash'),(14,7,'2026-01-25','Credit Card'),
(15,1,'2026-01-25','Cash'),(16,2,'2026-01-25','Credit Card'),
(17,3,'2026-01-25','Cash'),(18,4,'2026-01-25','Credit Card'),
(19,5,'2026-01-25','Cash'),(20,6,'2026-01-25','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(505,1,1,2,12.00),(505,2,7,1,6.00),
(506,1,2,1,15.00),(506,2,5,1,8.00),
(507,1,3,2,14.00),
(508,1,1,1,12.00),(508,2,9,2,9.00),
(509,1,2,2,15.00),
(510,1,4,1,10.00),(510,2,8,1,18.00),
(511,1,5,1,8.00),(511,2,6,1,7.00),
(512,1,1,3,12.00),
(513,1,2,1,15.00),(513,2,7,2,6.00),
(514,1,3,2,14.00),
(515,1,1,2,12.00),(515,2,5,1,8.00),
(516,1,2,1,15.00),
(517,1,4,1,10.00),(517,2,9,1,9.00);

-- ============================================================
-- 20 Sales for 2026-01-26
-- ============================================================
INSERT INTO Sale (CustomerID, EmployeeID, Date, PaymentMethod) VALUES
(1,1,'2026-01-26','Cash'),(2,2,'2026-01-26','Credit Card'),
(3,3,'2026-01-26','Cash'),(4,4,'2026-01-26','Credit Card'),
(5,5,'2026-01-26','Cash'),(6,6,'2026-01-26','Credit Card'),
(7,7,'2026-01-26','Cash'),(8,1,'2026-01-26','Credit Card'),
(9,2,'2026-01-26','Cash'),(10,3,'2026-01-26','Credit Card'),
(11,4,'2026-01-26','Cash'),(12,5,'2026-01-26','Credit Card'),
(13,6,'2026-01-26','Cash'),(14,7,'2026-01-26','Credit Card'),
(15,1,'2026-01-26','Cash'),(16,2,'2026-01-26','Credit Card'),
(17,3,'2026-01-26','Cash'),(18,4,'2026-01-26','Credit Card'),
(19,5,'2026-01-26','Cash'),(20,6,'2026-01-26','Credit Card');

INSERT INTO SaleDetail (SaleID,LineNo,ItemID,Quantity,SellingPrice) VALUES
(518,1,1,2,12.00),(518,2,7,1,6.00),
(519,1,2,1,15.00),(519,2,5,1,8.00),
(520,1,3,2,14.00),
(521,1,1,1,12.00),(521,2,9,2,9.00),
(522,1,2,2,15.00),
(523,1,4,1,10.00),(523,2,8,1,18.00),
(524,1,5,1,8.00),(524,2,6,1,7.00),
(525,1,1,3,12.00),
(526,1,2,1,15.00),(526,2,7,2,6.00),
(527,1,3,2,14.00),
(528,1,1,2,12.00),(528,2,5,1,8.00),
(529,1,2,1,15.00);

INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 530
(530,1,1,2,12.00),(530,2,7,1,6.00),
-- Sale 531
(531,1,2,1,15.00),(531,2,5,1,8.00),
-- Sale 532
(532,1,3,2,14.00),
-- Sale 533
(533,1,4,1,10.00),(533,2,9,2,9.00),
-- Sale 534
(534,1,16,1,17.00),(534,2,18,1,16.00),
-- Sale 535
(535,1,17,2,15.00),(535,2,19,1,18.00),
-- Sale 536
(536,1,1,3,12.00),(536,2,2,1,15.00),
-- Sale 537
(537,1,3,2,14.00),(537,2,5,1,8.00),
-- Sale 538
(538,1,4,1,10.00),(538,2,6,1,7.00),
-- Sale 539
(539,1,7,2,6.00),(539,2,8,1,18.00),
-- Sale 540
(540,1,9,1,9.00),
-- Sale 541
(541,1,16,1,17.00),(541,2,20,2,17.00),
-- Sale 542
(542,1,17,1,15.00),(542,2,22,1,14.00),
-- Sale 543
(543,1,23,1,14.00),(543,2,24,1,16.00),
-- Sale 544
(544,1,25,1,16.00),(544,2,26,1,16.00),
-- Sale 545
(545,1,27,1,16.00),
-- Sale 546
(546,1,28,1,20.00),
-- Sale 547
(547,1,1,2,12.00),(547,2,7,1,6.00),
-- Sale 548
(548,1,2,1,15.00),(548,2,8,2,18.00),
-- Sale 549
(549,1,3,2,14.00),(549,2,9,1,9.00),
-- Sale 550
(550,1,4,1,10.00),(550,2,6,1,7.00),
-- Sale 551
(551,1,1,2,12.00),(551,2,5,1,8.00),
-- Sale 552
(552,1,2,1,15.00),(552,2,7,2,6.00),
-- Sale 553
(553,1,3,2,14.00),
-- Sale 554
(554,1,8,1,18.00),(554,2,9,1,9.00),
-- Sale 555
(555,1,16,1,17.00),(555,2,17,1,15.00),
-- Sale 556
(556,1,18,1,16.00),(556,2,19,1,18.00),
-- Sale 557
(557,1,20,1,17.00),(557,2,21,2,14.00),
-- Sale 558
(558,1,22,1,15.00),
-- Sale 559
(559,1,23,1,16.00),(559,2,24,2,16.00),
-- Sale 560
(560,1,25,1,16.00),(560,2,26,1,16.00),
-- Sale 561
(561,1,27,1,16.00),
-- Sale 562
(562,1,28,1,20.00),
-- Sale 563
(563,1,1,2,12.00),(563,2,2,1,15.00),
-- Sale 564
(564,1,3,2,14.00),(564,2,4,1,10.00),
-- Sale 565
(565,1,5,1,8.00),(565,2,6,1,7.00),
-- Sale 566
(566,1,7,1,6.00),(566,2,8,2,18.00),
-- Sale 567
(567,1,9,1,9.00),
-- Sale 568
(568,1,16,1,17.00),(568,2,17,1,15.00),
-- Sale 569
(569,1,18,1,16.00),(569,2,19,2,18.00),
-- Sale 570
(570,1,20,1,17.00),
-- Sale 571
(571,1,21,1,14.00),(571,2,22,1,15.00),
-- Sale 572
(572,1,23,2,16.00),
-- Sale 573
(573,1,24,1,16.00),(573,2,25,1,16.00),
-- Sale 574
(574,1,26,1,16.00),(574,2,27,1,16.00),
-- Sale 575
(575,1,28,1,20.00),
-- Sale 576
(576,1,1,2,12.00),(576,2,2,1,15.00),
-- Sale 577
(577,1,3,2,14.00),(577,2,4,1,10.00),
-- Sale 578
(578,1,5,1,8.00),(578,2,6,1,7.00),
-- Sale 579
(579,1,7,1,6.00),(579,2,8,2,18.00);

-- ============================================================
-- Corrected SaleDetail from SaleID 580 to 629 (ItemIDs 1-9,16-28)
-- ============================================================

INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 580
(580,1,9,1,9.00),(580,2,16,2,17.00),
-- Sale 581
(581,1,17,1,15.00),(581,2,18,1,16.00),
-- Sale 582
(582,1,19,1,18.00),
-- Sale 583
(583,1,20,1,17.00),(583,2,21,2,14.00),
-- Sale 584
(584,1,22,1,15.00),(584,2,23,1,16.00),
-- Sale 585
(585,1,24,1,16.00),(585,2,25,1,16.00),
-- Sale 586
(586,1,26,1,16.00),
-- Sale 587
(587,1,27,1,16.00),(587,2,28,1,20.00),
-- Sale 588
(588,1,1,2,12.00),(588,2,2,1,15.00),
-- Sale 589
(589,1,3,2,14.00),(589,2,4,1,10.00),
-- Sale 590
(590,1,5,1,8.00),(590,2,6,1,7.00),
-- Sale 591
(591,1,7,1,6.00),(591,2,8,2,18.00),
-- Sale 592
(592,1,9,1,9.00),
-- Sale 593
(593,1,16,1,17.00),(593,2,17,1,15.00),
-- Sale 594
(594,1,18,1,16.00),(594,2,19,2,18.00),
-- Sale 595
(595,1,20,1,17.00),
-- Sale 596
(596,1,21,1,14.00),(596,2,22,1,15.00),
-- Sale 597
(597,1,23,2,16.00),
-- Sale 598
(598,1,24,1,16.00),(598,2,25,1,16.00),
-- Sale 599
(599,1,26,1,16.00),(599,2,27,1,16.00),
-- Sale 600
(600,1,28,1,20.00),
-- Sale 601
(601,1,1,2,12.00),(601,2,2,1,15.00),
-- Sale 602
(602,1,3,2,14.00),(602,2,4,1,10.00),
-- Sale 603
(603,1,5,1,8.00),(603,2,6,1,7.00),
-- Sale 604
(604,1,7,1,6.00),(604,2,8,2,18.00),
-- Sale 605
(605,1,9,1,9.00),
-- Sale 606
(606,1,16,1,17.00),(606,2,17,1,15.00),
-- Sale 607
(607,1,18,1,16.00),(607,2,19,2,18.00),
-- Sale 608
(608,1,20,1,17.00),
-- Sale 609
(609,1,21,1,14.00),(609,2,22,1,15.00),
-- Sale 610
(610,1,23,2,16.00),
-- Sale 611
(611,1,24,1,16.00),(611,2,25,1,16.00),
-- Sale 612
(612,1,26,1,16.00),(612,2,27,1,16.00),
-- Sale 613
(613,1,28,1,20.00),
-- Sale 614
(614,1,1,2,12.00),(614,2,2,1,15.00),
-- Sale 615
(615,1,3,2,14.00),(615,2,4,1,10.00),
-- Sale 616
(616,1,5,1,8.00),(616,2,6,1,7.00),
-- Sale 617
(617,1,7,1,6.00),(617,2,8,2,18.00),
-- Sale 618
(618,1,9,1,9.00),
-- Sale 619
(619,1,16,1,17.00),(619,2,17,1,15.00),
-- Sale 620
(620,1,18,1,16.00),(620,2,19,2,18.00),
-- Sale 621
(621,1,20,1,17.00),
-- Sale 622
(622,1,21,1,14.00),(622,2,22,1,15.00),
-- Sale 623
(623,1,23,2,16.00),
-- Sale 624
(624,1,24,1,16.00),(624,2,25,1,16.00),
-- Sale 625
(625,1,26,1,16.00),(625,2,27,1,16.00),
-- Sale 626
(626,1,28,1,20.00),
-- Sale 627
(627,1,1,2,12.00),(627,2,2,1,15.00),
-- Sale 628
(628,1,3,2,14.00),(628,2,4,1,10.00),
-- Sale 629
(629,1,5,1,8.00),(629,2,6,1,7.00);
-- ============================================================
-- Corrected SaleDetail from SaleID 630 to 689 (ItemIDs 1-9,16-28)
-- ============================================================

INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 630
(630,1,7,1,6.00),(630,2,8,2,18.00),
-- Sale 631
(631,1,9,1,9.00),
-- Sale 632
(632,1,16,1,17.00),(632,2,17,1,15.00),
-- Sale 633
(633,1,18,1,16.00),(633,2,19,2,18.00),
-- Sale 634
(634,1,20,1,17.00),
-- Sale 635
(635,1,21,1,14.00),(635,2,22,1,15.00),
-- Sale 636
(636,1,23,2,16.00),
-- Sale 637
(637,1,24,1,16.00),(637,2,25,1,16.00),
-- Sale 638
(638,1,26,1,16.00),(638,2,27,1,16.00),
-- Sale 639
(639,1,28,1,20.00),
-- Sale 640
(640,1,1,2,12.00),(640,2,2,1,15.00),
-- Sale 641
(641,1,3,2,14.00),(641,2,4,1,10.00),
-- Sale 642
(642,1,5,1,8.00),(642,2,6,1,7.00),
-- Sale 643
(643,1,7,1,6.00),(643,2,8,2,18.00),
-- Sale 644
(644,1,9,1,9.00),
-- Sale 645
(645,1,16,1,17.00),(645,2,17,1,15.00),
-- Sale 646
(646,1,18,1,16.00),(646,2,19,2,18.00),
-- Sale 647
(647,1,20,1,17.00),
-- Sale 648
(648,1,21,1,14.00),(648,2,22,1,15.00),
-- Sale 649
(649,1,23,2,16.00),
-- Sale 650
(650,1,24,1,16.00),(650,2,25,1,16.00),
-- Sale 651
(651,1,26,1,16.00),(651,2,27,1,16.00),
-- Sale 652
(652,1,28,1,20.00),
-- Sale 653
(653,1,1,2,12.00),(653,2,2,1,15.00),
-- Sale 654
(654,1,3,2,14.00),(654,2,4,1,10.00),
-- Sale 655
(655,1,5,1,8.00),(655,2,6,1,7.00),
-- Sale 656
(656,1,7,1,6.00),(656,2,8,2,18.00),
-- Sale 657
(657,1,9,1,9.00),
-- Sale 658
(658,1,16,1,17.00),(658,2,17,1,15.00),
-- Sale 659
(659,1,18,1,16.00),(659,2,19,2,18.00),
-- Sale 660
(660,1,20,1,17.00),
-- Sale 661
(661,1,21,1,14.00),(661,2,22,1,15.00),
-- Sale 662
(662,1,23,2,16.00),
-- Sale 663
(663,1,24,1,16.00),(663,2,25,1,16.00),
-- Sale 664
(664,1,26,1,16.00),(664,2,27,1,16.00),
-- Sale 665
(665,1,28,1,20.00),
-- Sale 666
(666,1,1,2,12.00),(666,2,2,1,15.00),
-- Sale 667
(667,1,3,2,14.00),(667,2,4,1,10.00),
-- Sale 668
(668,1,5,1,8.00),(668,2,6,1,7.00),
-- Sale 669
(669,1,7,1,6.00),(669,2,8,2,18.00),
-- Sale 670
(670,1,9,1,9.00),
-- Sale 671
(671,1,16,1,17.00),(671,2,17,1,15.00),
-- Sale 672
(672,1,18,1,16.00),(672,2,19,2,18.00),
-- Sale 673
(673,1,20,1,17.00),
-- Sale 674
(674,1,21,1,14.00),(674,2,22,1,15.00),
-- Sale 675
(675,1,23,2,16.00),
-- Sale 676
(676,1,24,1,16.00),(676,2,25,1,16.00),
-- Sale 677
(677,1,26,1,16.00),(677,2,27,1,16.00),
-- Sale 678
(678,1,28,1,20.00),
-- Sale 679
(679,1,1,2,12.00),(679,2,2,1,15.00),
-- Sale 680
(680,1,3,2,14.00),(680,2,4,1,10.00),
-- Sale 681
(681,1,5,1,8.00),(681,2,6,1,7.00),
-- Sale 682
(682,1,7,1,6.00),(682,2,8,2,18.00),
-- Sale 683
(683,1,9,1,9.00),
-- Sale 684
(684,1,16,1,17.00),(684,2,17,1,15.00),
-- Sale 685
(685,1,18,1,16.00),(685,2,19,2,18.00),
-- Sale 686
(686,1,20,1,17.00),
-- Sale 687
(687,1,21,1,14.00),(687,2,22,1,15.00),
-- Sale 688
(688,1,23,2,16.00),
-- Sale 689
(689,1,24,1,16.00),(689,2,25,1,16.00);
-- ============================================================
-- SaleDetail from SaleID 690 to 698 (ItemIDs 1-9,16-28)
-- ============================================================

INSERT INTO SaleDetail (SaleID, LineNo, ItemID, Quantity, SellingPrice) VALUES
-- Sale 690
(690,1,26,1,16.00),(690,2,27,1,16.00),
-- Sale 691
(691,1,28,1,20.00),
-- Sale 692
(692,1,1,2,12.00),(692,2,2,1,15.00),
-- Sale 693
(693,1,3,2,14.00),(693,2,4,1,10.00),
-- Sale 694
(694,1,5,1,8.00),(694,2,6,1,7.00),
-- Sale 695
(695,1,7,1,6.00),(695,2,8,2,18.00),
-- Sale 696
(696,1,9,1,9.00),
-- Sale 697
(697,1,16,1,17.00),(697,2,17,1,15.00),
-- Sale 698
(698,1,18,1,16.00),(698,2,19,2,18.00);

Select * from Stock