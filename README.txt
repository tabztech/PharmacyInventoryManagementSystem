PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)

1. SYSTEM DESCRIPTION
---------------------
PIMS is a Java Swing and MySQL database application designed to 
manage pharmacy inventories, sales transactions, supplier records, 
and analytics reporting.

2. DEFAULT SYSTEM LOGIN CREDENTIALS
------------------------------------
- Administrator Account:
  Username: admin
  Password: admin123

- Cashier Account:
  Username: cashier
  Password: cash123

3. DATABASE SETUP & INITIALIZATION
----------------------------------
- Database Name: pims_db
- Engine: MySQL 8.0+
- Host: localhost:3306
- Import the provided 'database.sql' file using MySQL Workbench 
  to set up tables and default seed data.

4. BUILD & RUNNING INSTRUCTIONS
-------------------------------
1. Ensure MySQL Server is running on port 3306.
2. Open the project in Apache NetBeans IDE.
3. Attach mysql-connector-j driver to Libraries.
4. Run com.pims.ui.LoginFrame to launch the application.