# OOP-x-DBMS

Final Project requirement for Object Oriented Programming and Database Management System BSU Alangilan


Library Management System
This project is a Java-based Library Management System designed to efficiently manage a library's resources, including books and user accounts. The system is built with a focus on enhancing the management of library items and user interactions while adhering to key principles of Object-Oriented Programming (OOP).

Functionalities
User Authentication and Authorization
LoginManager: Manages user authentication by verifying user credentials against a database and provides role-based access (Admin/Reader) to the system.
Library Catalog Management
LibraryCatalog: Handles the addition and retrieval of books, maintaining a catalog of available resources.
Book Management
Book: Represents a book entity with properties such as title, author, genre, quantity, and availability. It supports operations like adding, updating, and searching books.
User Management
User: Represents a library system user with basic authentication information (username, password, role).
Additional Features
Polymorphism: Utilizes polymorphism to enable customized behavior for displaying item details based on the item type. For instance, the displayItem() method in the LibraryItem class enables different implementations for various library items (e.g., books, journals, DVDs) to display their specific details.
Encapsulation: Achieves encapsulation by controlling access to class attributes through private access modifiers and providing public methods (getters/setters) for interacting with these attributes. For instance, the Book class encapsulates book details like title, author, genre, and quantity, allowing controlled access and modification.
Inheritance: Demonstrates inheritance by extending the LibraryItem class, creating specialized entities like Book that inherit common properties and behaviors from the parent class.
Abstraction: Implements abstraction to display essential item details while hiding complex internal implementations. The displayItem() method in the LibraryItem class provides an abstraction layer that allows different subclasses to implement their specific display logic while keeping the interface consistent.
SDG 4: Quality Education
This Library Management System aligns with Sustainable Development Goal 4 (SDG 4) - Quality Education by providing a platform that promotes efficient learning and resource utilization:

Accessible Learning Resources: Facilitates easy access to educational materials through a well-organized library catalog, promoting a conducive learning environment.
User Management: Supports role-based access control (Admin/Reader), ensuring secure and tailored access to educational resources.
Efficient Library Operations: Optimizes library resource management, enhancing the overall quality of library services and educational experiences.
