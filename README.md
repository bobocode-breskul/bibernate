# Bibernate Framework ![Bobocode Breskul Team](https://img.shields.io/badge/Bobocode%20Breskul%20Team-8A2BE2) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
![Maven Central](https://img.shields.io/maven-central/v/io.github.bobocode-breskul/bibernate)

## What is Bibernate Framework?

Bibernate ORM is a powerful object/relational mapping solution for Java, and makes it easy to develop persistence logic for applications, libraries, and frameworks.

Bibernate implements JPA, the standard API for object/relational persistence in Java, but also offers an extensive set of features and APIs which go beyond the specification.

## Table of Contents

- [Introduction](#introduction)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Features](#features)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Bibernate is an object-relational mapping (ORM) framework for Java that provides a programming
paradigm for mapping Java objects to relational database tables and vice versa. It is an open-source
framework that simplifies database programming in Java applications by handling the details of
database interactions, allowing developers to focus on writing Java code without having to deal
extensively with SQL and database-specific details.

## Requirements
Make sure that you are using Java 17

## Getting Started
Follow these steps to integrate Bring Framework into your project:

1. Open your project's `pom.xml` file.
2. Add the following Maven dependency:

```xml
<dependency>
  <groupId>io.github.bobocode-breskul</groupId>
  <artifactId>bibernate</artifactId>
  <version>1.0</version>
</dependency>
```
You can find the example of a Bibernate application [here](https://github.com/bobocode-breskul/bibernate-usage-example).

## Features

1. **Object-Relational Mapping (ORM):** Bibernate allows developers to map Java objects directly to database tables, eliminating the need for manual SQL queries and data manipulation.


2. **Database Independence:** Bibernate provides a database-independent framework. It supports multiple databases, and you can switch between them easily by changing the configuration.


3. **Automatic Table Generation:** Bibernate can automatically generate database tables based on the Java objects, reducing the need for manual database schema creation.


4. **Transparent Persistence:** Bibernate provides transparent persistence, meaning that developers can work with Java objects without being concerned about the underlying database operations. Bibernate takes care of managing the database operations behind the scenes.


5. **Caching:** Bibernate supports various caching mechanisms, such as first-level cache and second-level cache, to improve performance by reducing the number of database queries.


6. **Query Language (HQL):** Bibernate Query Language (HQL) is a powerful and database-independent query language similar to SQL. It allows developers to write queries using Java-like syntax and work with domain objects rather than database tables.


7. **Lazy Loading:** Bibernate supports lazy loading, which means that it can load data on demand, reducing the initial load time and improving application performance.


8. **Association Mapping:** Bibernate supports various types of associations between entities, such as one-to-one, one-to-many, and many-to-many relationships. It allows developers to model complex relationships between entities.


9. **Transaction Management:** Bibernate provides robust support for transaction management, ensuring that database operations are executed atomically and consistently.


10. **Annotations and XML Configuration:** Bibernate allows developers to configure entity mappings using either XML configuration files or annotations, providing flexibility in how entities are mapped to database tables.


11. **Integration with Java EE and Bring:** Bibernate can be seamlessly integrated with Java EE applications and Bring framework, making it a versatile choice for enterprise applications.


12. **Connection Pooling:** Bibernate can work with various connection pooling solutions, allowing efficient management of database connections to improve performance.


13. **Detached Objects:** Bibernate supports working with detached objects, allowing developers to work with persistent objects outside the context of an active database session.


14. **Batch Processing:** Bibernate supports batch processing, allowing efficient processing of multiple database operations in a single batch.


15. **Validation:** Bibernate provides support for data validation using constraints and annotations, ensuring that the data stored in the database meets specified criteria.

## Contributing
We welcome contributions!
If you'd like to contribute to Bring, please contact with the team Breskul.

## License
This project is licensed under the [Apache License](https://opensource.org/licenses/Apache-2.0).


