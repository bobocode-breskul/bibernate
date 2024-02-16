# Bibernate Framework ![Bobocode Breskul Team](https://img.shields.io/badge/Bobocode%20Breskul%20Team-8A2BE2) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
![Maven Central](https://img.shields.io/maven-central/v/io.github.bobocode-breskul/bibernate)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=bobocode-breskul_bibernate&metric=coverage)](https://sonarcloud.io/summary/new_code?id=bobocode-breskul_bibernate)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=bobocode-breskul_bibernate&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=bobocode-breskul_bibernate)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=bobocode-breskul_bibernate&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=bobocode-breskul_bibernate)

## What is Bibernate Framework?

Bibernate ORM is a powerful object/relational mapping solution for Java, and makes it easy to develop persistence logic for applications, libraries, and frameworks.

Bibernate implements JPA, the standard API for object/relational persistence in Java, but also offers an extensive set of features and APIs which go beyond the specification.

## Table of Contents

- [Introduction](#introduction)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Contributing](#contributing)
- [License](#license)

## Introduction

Bibernate is a conceptual Object/Relational Mapping (ORM) framework tailored for Java applications, designed to streamline the interaction between Java objects and relational databases. It abstracts the complexity of database access, allowing developers to work with database entities as if they were Java objects. Here's a more detailed look at its features, with an emphasis on language support:

- **[Action Queue](https://github.com/bobocode-breskul/bibernate/wiki/Action-Queue)** Manages the order of database operations (inserts, updates, deletes) to ensure transactional integrity.
- **[Annotations:](https://github.com/bobocode-breskul/bibernate/wiki/Annotations)** Used to define how Java classes and fields map to database tables and columns without verbose XML configuration files.
- **[Connection Pool:](https://github.com/bobocode-breskul/bibernate/wiki/Connection-pool)** A cache of database connections that allows for reuse, improving performance by reducing the overhead of establishing connections.
- **[CRUD Operations:](https://github.com/bobocode-breskul/bibernate/wiki/CRUD-Operations)** Simplifies Create, Read, Update, and Delete operations with straightforward methods, abstracting the underlying SQL.
- **[Dirty Checking:](https://github.com/bobocode-breskul/bibernate/wiki/Dirty-Checking)** Automatically detects changes in entities since the last synchronization with the database, minimizing unnecessary updates.
- **[First Level Cache:](https://github.com/bobocode-breskul/bibernate/wiki/First-Level-Cache)** A per-session cache that reduces database hits by storing entities retrieved during the session's lifetime.
- **[Lazy Loading:](https://github.com/bobocode-breskul/bibernate/wiki/Lazy-Loading)** Delays the loading of certain properties or collections of an entity until they are explicitly accessed, optimizing resource usage.
- **[Pessimistic Locking:](https://github.com/bobocode-breskul/bibernate/wiki/Pessimistic-Locking)** Locks data at the database level to prevent concurrent modifications, ensuring data consistency in high-concurrency environments.
- **[Query Language (Native Query BiQL):](https://github.com/bobocode-breskul/bibernate/wiki/Query-Language-(Native-Query---BiQL))** A custom query language or API designed for efficient and flexible database querying beyond standard SQL capabilities.
- **[Transaction:](https://github.com/bobocode-breskul/bibernate/wiki/Transaction)** Supports transactions to ensure data integrity, allowing multiple operations to be executed as a single atomic action.
- **[DDL table creation:]([https://github.com/bobocode-breskul/bibernate/wiki/Transaction](https://github.com/bobocode-breskul/bibernate/wiki/DDL-table-creation-on-startup))** Supports automates table creation for entities annotated with `@Entity`, managing SQL types and constraints, and supports relationships except for `@ManyToMany`, enhancing database schema management with a simple property configuration.

Bibernate aims to reduce the boilerplate associated with database programming in Java and potentially other JVM languages, offering a developer-friendly API and improving application performance through efficient data management practices. Its design reflects a commitment to flexibility, performance, and ease of use in the development of Java-based applications and beyond.

## Requirements
Make sure that you are using Java 17

## Getting Started
Follow these steps to integrate Bibernate Framework into your project:
1. Open your project's `pom.xml` file.
2. Add the following Maven dependency:
```xml
<dependency>
  <groupId>io.github.bobocode-breskul</groupId>
  <artifactId>bibernate</artifactId>
  <version>1.0</version>
</dependency>
```
Now you are ready to use Bibernate framework features.

## Demo examples
### Entity declaration example:

```java
@Data
@NoArgsConstructor
@Entity
@Table(name = "persons")
public class Person {

  @Id
  private Long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "age")
  private Integer age;

  public Person(String firstName, String lastName, Integer age) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }
  @Override
  public String toString() {
    return "Person{" +
        "id=" + id +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", age=" + age +
        '}';
  }
}
```

#### CRUD example:
```java
public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession()) {

      // Create
      Person person = new Person("Ivan", "Franko", 59);
      Person person2 = new Person("Taras", "Shevchenko", 47);
      session.persist(person);
      session.persist(person2);

      // Find
      Person foundPerson = session.findById(Person.class, person.getId());
      
      // Update
      foundPerson.setAge(40);
    
      // Delete
      session.delete(foundPerson);
      session.flush();
      
      printPersons(session);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
```

You can find the example of a Bibernate application [here](https://github.com/bobocode-breskul/bibernate-usage-example).
## Contributing
We welcome contributions!
If you'd like to contribute to Bibernate, please contact with the team Breskul.

## License
This project is licensed under the [Apache License](https://opensource.org/licenses/Apache-2.0).
