# Employee & Customer Management System (ECMS)

A Java-based desktop application for managing employees and customers with full CRUD functionality, analytics, and multiple persistence formats.

## 🚀 Features
- Register, view, update, and delete employees/customers
- SQLite database integration with smart schema loader
- Validation and exception handling
- Analytics: average age, average salary, membership breakdown
- Export/import data in JSON and text formats
- Java Swing GUI

## 🛠️ Tech Stack
- Java (Swing)
- SQLite
- Gson
- Maven

## 📂 Project Structure
- `src/ecms/model` → Entity classes (Person, Employee, Customer)
- `src/ecms/dao` → Database access (JdbcEntityDAO, Db)
- `src/ecms/util` → Utilities (JsonExporter, SchemaLoader)
- `src/ecms/view` → GUI (MainFrame, RegisterForm, EntityTable)
- `resources/schema.sql` → Database schema

## ▶️ How to Run

