# 🏢 ECMS: Employee & Customer Management System

## 🌟 Overview

The ECMS is a robust desktop application designed to streamline the management of internal employee records and external customer data. Built using **Java Swing** for the GUI and a **Service-Oriented Architecture (SOA)**, it features a secure, **Role-Based Access Control (RBAC)** system to ensure data integrity and separation of duties across five distinct user roles.

---

## ✨ System Features

### 🔐 Security & User Management

* **Role-Based Access Control (RBAC):** Access to tabs, CRUD operations (Create, Read, Update, Delete), and system tools is strictly enforced based on the logged-in user's role.
* **Secure Authentication:** User passwords are automatically hashed using a secure algorithm upon registration and login.
* **User Management Panel:** Dedicated panel (Admin only) for creating, viewing, and deleting system user accounts.
* **Initial Admin Setup:** A default 'admin' user is automatically created upon first run (credentials configurable).

### 📊 Data Management (CRUD Operations)

* **Employee Management:** Full CRUD interface for maintaining employee records (Name, Age, Salary, Job Title).
    * **Financial Security:** Access to salary data and modification is restricted to appropriate roles (Admin, Manager, HR Specialist).
* **Customer Management:** Full CRUD interface for managing customer profiles (Name, Age, Membership Level).
    * **Data Integrity:** Includes validation to prevent the entry of future dates for the "Last Purchase Date."
* **Data Refresh:** Dashboard metrics are automatically refreshed after any successful Employee or Customer save/delete operation.

### 📈 Reporting & Utility (Visualization Only)

* **Analytics Panel:** Dedicated read-only area for viewing key performance indicators (KPIs) and data summaries using JFreeChart. 
* **Modern UI:** Utilizes external libraries (`JCalendar`) for user-friendly date selection and features a clean, non-blocking status bar for user feedback.

---

## 🖼️ Dashboard & Access Visualization (RBAC Focus)

The dashboard uses the **Focused Quadrant Manager** design, which dynamically visualizes the user's precise access status for the core modules.

### 1. Admin Dashboard (Full Access View)

The Admin role sees **Full Access** or **Nominal** status across all key operational quadrants, indicating complete read/write permission.

![Admin Dashboard Full Access](admin-dashboard.png) (Width: 700px)

### 2. HR Specialist Dashboard (Limited Access View)

The HR Specialist role demonstrates the RBAC restrictions, showing **Full Access** only to their domain (Employee Management) and **Read Only** or **Restricted** access everywhere else.

![HR Specialist Dashboard Limited Access](hr-dashboard.png) (Width: 700px)

---

## 👥 Role-Based Access Control (RBAC) Policy

The system employs five distinct user roles, each with tightly defined permissions to ensure security and compliance.

| Role | User Management | Employee Management (CRUD) | Customer Management (CRUD) | Analytics Access |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | Full Control (CRUD) | Full Control (CRUD) | Full Control (CRUD) | Full Access |
| **Manager** | No Access | Full Control (CRUD) | Full Control (CRUD) | Full Access |
| **HR Specialist** | No Access | Full Control (CRUD) | Read Only | View (Employee Metrics Only) |
| **Cust. Service Agent**| No Access | No Access | Full Control (CRUD) | No Access |
| **Data Analyst** | No Access | Read Only | Read Only | Full Access |

---

## 🚀 Getting Started

### Prerequisites

* **Java Development Kit (JDK) 17+:** Required to compile and run the application.
* **Maven:** Version 3.8 or higher, required for dependency management and building the project.

### Installation and Run

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/vukona-dev/employee-customer-management-system.git](https://github.com/vukona-dev/employee-customer-management-system.git)
    cd employee-customer-management-system
    ```

2.  **Build and Install Dependencies (via Maven):**
    This command reads the `pom.xml`, downloads all required external JARs (JCalendar, JFreeChart, JCommon) from the Maven Central Repository, and compiles your source code.
    ```bash
    mvn clean install
    ```

3.  **Run the Application:**
    You can now run the compiled classes using the standard Java command. The entry point is the `gui.Main` class.
    ```bash
    java -cp target/classes gui.Main
    ```
*(Alternatively, you can run the `gui.Main.java` file directly from your IDE after the Maven build completes.)*

![MainFrame running after successful login](main-frame.png) (Width: 900px)

---

## 🔑 Initial Credentials

Upon the very first launch, the system automatically creates a secure administrative account. Use these to test the core functionality:

| Field | Value |
| :--- | :--- |
| **Username** | `admin` |
| **Password** | `password123` |
| **Role** | `Admin` |

**NOTE:** Change the initial password immediately upon successful login, and you can register more users.

---

## 📦 Project Structure

The project follows a standard Maven structure with a Service-Oriented Architecture:

| Directory | Purpose | Key Classes |
| :--- | :--- | :--- |
| `src/main/java/gui` | Contains all graphical user interface components and JFrame/JPanels. | `MainFrame.java`, `LoginFrame.java`, `EmployeePanel.java`, etc. |
| `src/main/java/model` | Contains plain old Java objects (POJOs) representing the core entities. | `User.java`, `Employee.java`, `Customer.java` |
| `src/main/java/service` | Business logic and service layer (orchestrates DAO/GUI interaction). | `AuthenticationService.java`, `ManagementService.java` |
| `src/main/java/dao` | Data Access Objects (DAO) for persistence logic (e.g., in-memory lists or database interaction). | `UserDAO.java`, `EmployeeDAO.java`, etc. |
| `src/main/java/util` | Utility classes, custom exceptions, and helpers. | `CustomExceptions.java`, `PasswordUtil.java` |
| `pom.xml` | **Maven Configuration File** (defines dependencies and build process). | |