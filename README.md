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
  <version>2.0</version>
</dependency>
```
3. Add `application.properties` file.
```properties
# Connection pool type: HikariCP, Apache, c3p0, or leave blank for default DataSource
bibernate.datasource.type=HikariCP

# Database connection details
bibernate.connection.url=jdbc:postgresql://localhost:5432/postgres
bibernate.connection.username=postgres
bibernate.connection.password=postgres
bibernate.connection.driver_class=org.postgresql.Driver
bibernate.dialect=com.breskul.bibernate.persistence.dialect.H2Dialect

# Option available to suppress notifications for executed SQL queries
bibernate.show_sql=true

# Enabling create_tables property will drop tables and create them from entities
bibernate.ddl.create_tables=true
```

Now you are ready to use Bibernate framework features.

## Demo examples
You can find the example of a Bibernate application [here](https://github.com/bobocode-breskul/bibernate-usage-example).

#### CRUD example:
```java
public class Main {

  public static void main(String[] args) {
    SessionFactory sessionFactory = Persistence.createSessionFactory();
    try (Session session = sessionFactory.openSession()) {

      // Create
      Person firstPerson = new Person("Ivan", "Franko", 59);
      Person secondPerson = new Person("Taras", "Shevchenko", 47);
      session.persist(firstPerson);
      session.persist(secondPerson);

      // Find by ID
      Person foundPerson = session.findById(Person.class, firstPerson.getId());

      // Update
      foundPerson.setAge(40);

      // Delete
      session.delete(foundPerson);
      session.flush();

      // Use @OneToMany @ManyToOne @OneToOne relations
      Note note1 = new Note("First note", secondPerson);
      Note note2 = new Note("Second note", secondPerson);
      session.persist(note1);
      session.persist(note2);

      // Execute queries using BiQL
      String biQLQuery = "from Person";
      List<Person> personList = session.executeBiQLQuery(biQLQuery, Person.class);
      for (Person person : personList) {
        System.out.println(person);
      }

      // Execute queries using native queries
      String nativeQuery = "SELECT * FROM notes";
      List<Note> notes = session.executeNativeQuery(nativeQuery, Note.class);
      for (Note note : notes) {
        System.out.println(note);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
```

### Entity declaration example:

```java
@Data
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "persons")
@ToString
public class Person {

  @Id
  @Column(columnDefinition = "BIGSERIAL")
  private Long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "age")
  private Integer age;

  @OneToMany
  private List<Note> noteList;

  public Person(String firstName, String lastName, Integer age) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }
}
```

```java
@Data
@Entity
@Table(name = "notes")
@NoArgsConstructor
public class Note {

  @Id
  @Column(columnDefinition = "BIGSERIAL")
  private Long id;

  private String note;

  @ManyToOne
  @ToString.Exclude
  private Person persons;

  public Note(String note, Person persons) {
    this.note = note;
    this.persons = persons;
  }
}
```

## Contributing
We welcome contributions!
If you'd like to contribute to Bibernate, please contact with the team Breskul.

## License
This project is licensed under the [Apache License](https://opensource.org/licenses/Apache-2.0).
