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
- [Features](#features)
- [Annotations](#annotations)
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

You can find the example of a Bibernate application [here](https://github.com/bobocode-breskul/bibernate-usage-example).

## Features
-   CRUD Operations
-   Query Language
-   LazyLoading
-   Caching
    - Level 1
    - Level 2
-   Write Behind Cache actionQueue
-   DDL by annotations/xml
-   DirtyChecking
-   Concurrency Control
-   Cascade Operations

## Annotations
<details>
  <summary>@Entity</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@Table(String name)</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@Id</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@GenerationStratagy</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@Column(String name, Constraints)</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@OneToMany</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@ManyToOne</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@OneToOne</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@JoinColumn</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@MapsId</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@Embadded</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary> @Enumerated</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@ManyToMany</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

<details>
  <summary>@JoinTable</summary>
  
  ### Example
  ```java
  public void example() {
    // TODO: add code example
  }
  ```
</details>

## Contributing
We welcome contributions!
If you'd like to contribute to Bibernate, please contact with the team Breskul.

## License
This project is licensed under the [Apache License](https://opensource.org/licenses/Apache-2.0).
