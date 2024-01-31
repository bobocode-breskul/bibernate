package com.breskul.bibernate.util;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToMany;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.collection.LazyList;
import com.breskul.bibernate.collection.LazySet;
import com.breskul.bibernate.exception.EntityParseException;
import com.breskul.bibernate.persistence.Test;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Utility class for working with entity classes.
 */
public class EntityUtil {

  /**
   * Validates if the given class is marked with the 'Entity' annotation.
   *
   * @param cls - The class to validate
   * @throws EntityParseException if the class is not marked with the 'Entity' annotation
   */
  public static void validateIsEntity(Class<?> cls) {
    if (!cls.isAnnotationPresent(Entity.class)) {
      throw new EntityParseException("Class should be marked with 'Entity' annotation");
    }
  }

  /**
   * Validates if the given column exists in the entity class.
   * Throws an exception if the column does not exist.
   *
   * @param cls        - The entity class
   * @param columnName - The column name to validate
   *
   * @throws IllegalArgumentException if the column does not exist in the entity class
   */
  public static void validateColumnName(Class<?> cls, String columnName) {
    validateIsEntity(cls);
    var column = getClassColumnFields(cls).stream()
      .filter(field -> resolveColumnName(field).equals(columnName))
      .findFirst();
    if (column.isEmpty()) {
      throw new IllegalArgumentException("Entity [%s] does not have a column: [%s]."
        .formatted(cls, columnName));
    }
  }


  /**
   * Retrieves the table name for the given entity class. If the class is annotated with the
   * {@link Table} annotation, the table name from the annotation is returned. Otherwise, the
   * class name is returned.
   *
   * @param cls - The entity class
   * @return The table name for the entity class
   */
  public static String getEntityTableName(Class<?> cls) {
    return Optional.ofNullable(cls.getAnnotation(Table.class))
      .map(Table::name)
      .orElseGet(cls::getSimpleName);
  }


  /**
   * Retrieves a list of fields that represent columns in the given class.
   *
   * @param cls - The class for which to retrieve column fields
   * @return A list of fields representing columns in the class
   */
  public static List<Field> getClassColumnFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
      .filter(field -> !isCollectionEntityField(field))
      .toList();
  }


  /**
   * Determines if the given field is a collection entity field.
   *
   * @param field - The field to check
   * @return true if the field is a collection entity field, false otherwise
   */
  public static boolean isCollectionEntityField(Field field) {
    return field.isAnnotationPresent(OneToMany.class)
      || field.isAnnotationPresent(ManyToMany.class);
  }


  /**
   * Retrieves a list of fields that represent entity properties in the given class.
   *
   * @param cls - The class for which to retrieve entity property fields
   * @return A list of fields representing entity properties in the class
   */
  public static List<Field> getClassEntityFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
      .toList();
  }

  /**
   * Finds the entity ID field from a list of fields.
   *
   * @param fields - The list of fields to search
   * @return The entity ID field
   * @throws EntityParseException if the entity does not define an ID column or if multiple fields
   *         are marked with the 'Id' annotation
   */
  public static Field findEntityIdField(List<Field> fields) {
    List<Field> idFields = fields.stream()
        .filter(field -> field.isAnnotationPresent(Id.class))
        .toList();
    if (idFields.isEmpty()) {
      throw new EntityParseException("Entity should define ID column marked with 'Id' annotation.");
    }
    if (idFields.size() > 1) {
      throw new EntityParseException("Only one field should be marked with 'Id' annotation.");
    }
    return idFields.get(0);
  }

  /**
   * Finds the entity ID field for the given entity class.
   *
   * @param cls - The entity class
   * @return The entity ID field
   * @throws EntityParseException if the entity does not define an ID column or if multiple fields
   *         are marked with the 'Id' annotation
   */
  public static Field findEntityIdField(Class<?> cls) {
    validateIsEntity(cls);
    return findEntityIdField(getClassColumnFields(cls));
  }


  /**
   * Composes a SELECT block for the specified list of column names.
   *
   * @param columnNames - The list of column names
   * @return The composed SELECT block
   */
  public static String composeSelectBlockFromColumns(List<Field> columnNames) {
    return columnNames.stream()
        .map(EntityUtil::resolveColumnName)
        .collect(Collectors.joining(", "));
  }


  /**
   * Resolves the column name for the given field, from field annotations or return field name if
   * column annotation is absent.
   *
   * @param field - The field for which to resolve the column name
   * @return The resolved column name
   */
  public static String resolveColumnName(Field field) {
    if (field.isAnnotationPresent(Column.class)) {
      var columnName = field.getAnnotation(Column.class).name();
      return columnName.isBlank() ? field.getName() : columnName;
    } else if (field.isAnnotationPresent(JoinColumn.class)) {
      var columnName = field.getAnnotation(JoinColumn.class).name();
      return columnName.isBlank() ? field.getName() + "_id" : columnName;
    }
    return field.getName();
  }

  /**
   * Retrieves the join column name for joined entity.
   *
   * @param entityType    - The entity type
   * @param joinedEntity  - The joined entity
   * @return The join column name
   * @throws IllegalStateException if the related entity field cannot be found in the entity type
   */
  public static String getJoinColumnName(Class<?> entityType, Class<?> joinedEntity) {
    var joinField = Arrays.stream(entityType.getDeclaredFields())
      .filter(field -> field.getType().equals(joinedEntity))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("Can't find related entity [%s] field in [%s]."
        .formatted(joinedEntity, entityType)));

    return resolveColumnName(joinField);
  }


  /**
   * Retrieves the ID value of the given entity.
   *
   * @param entity - The entity object
   * @return The ID value of the entity object
   * @throws EntityParseException if the entity does not define an ID column or if multiple fields
   *         are marked with the 'Id' annotation
   */
  public static Object getEntityId(Object entity) {
    var idField = findEntityIdField(List.of(entity.getClass().getDeclaredFields()));
    return readFieldValue(entity, idField);
  }


  /**
   * Checks if the given field represents a primitive column in an entity.
   *
   * @param field - The field to check
   * @return true if the field is a primitive column, false otherwise
   */
  public static boolean isPrimitiveColumn(Field field) {
    return field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class);
  }


  /**
   * Retrieves the element type of generic collection field.
   *
   * @param field - The collection field
   * @return The element type of the collection
   */
  public static Class<?> getEntityCollectionElementType(Field field) {
    var parameterizedType = (ParameterizedType) field.getGenericType();
    var typeArguments = parameterizedType.getActualTypeArguments();
    var actualTypeArgument = typeArguments[0];
    return (Class<?>) actualTypeArgument;
  }


  /**
   * Retrieves an instance of the Collection class based on the given Field representing a collection.
   *
   * @param collectionField - The Field representing the collection
   * @return An instance of the Collection class
   * @throws IllegalArgumentException if the collection type is unsupported
   */
  public static Collection<Object> getCollectionInstance(Field collectionField) {
    return getCollectionInstance(collectionField, Collections.emptyList());
  }

  // todo: docs
  public static Collection<Object> getCollectionInstance(Field collectionField, Collection<?> source) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      return new ArrayList<>(source);
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      return new HashSet<>(source);
    }

    throw new IllegalArgumentException("Unsupported collection: " + collectionClass); // change exception and more clear msg?
  }

  // todo: docs
  public static Collection<Object> getLazyCollectionInstance(Field collectionField, Supplier<Collection<?>> delegateSupplier) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      return new LazyList<>(delegateSupplier);
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      return new LazySet<>(delegateSupplier);
    }

    throw new IllegalArgumentException("Unsupported collection: " + collectionClass); // change exception and more clear msg?
  }


  /**
   * Reads the value of a field on the given entity object.
   *
   * @param entity - The entity object
   * @param idField - The field to read the value from
   * @return The value of the field
   * @throws EntityParseException if failed to access the field due to illegal access
   */
  public static Object readFieldValue(Object entity, Field idField) {
    try {
      idField.setAccessible(true);
      return idField.get(entity);
    } catch (IllegalAccessException e) {
      throw new EntityParseException(
          "Failed to access field '" + idField.getName() + "' of entity type '" + entity.getClass()
              .getName() + "': Illegal access");
    }
  }

  private EntityUtil() {
  }
}
