package com.breskul.bibernate.util;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.DynamicUpdate;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToMany;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.exception.EntityParseException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
   * Validates if the given column exists in the entity class. Throws an exception if the column
   * does not exist.
   *
   * @param cls        - The entity class
   * @param columnName - The column name to validate
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
   * {@link Table} annotation, the table name from the annotation is returned. Otherwise, the class
   * name is returned.
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
   * Retrieves a list of column names from the fields of the provided entity class.
   * The method filters out fields representing collections of other entities.
   *
   * @param entityClass - The entity class for which to retrieve column names
   * @param <T> - The type of the entity class
   * @return A list of column names representing non-collection fields in the entity class
   * @see EntityUtil#isCollectionEntityField(Field)
   * @see EntityUtil#resolveColumnName(Field)
   */
  public static <T> List<String> getEntityColumnNames(Class<? extends T> entityClass) {
    return Arrays.stream(entityClass.getDeclaredFields())
        .filter(field -> !EntityUtil.isCollectionEntityField(field))
        .map(EntityUtil::resolveColumnName)
        .collect(Collectors.toList());
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
   * Retrieves a list of fields that represent columns without relations tp other entities in the
   * given class.
   *
   * @param cls - The class for which to retrieve column fields
   * @return A list of fields representing simple columns (without relations) in the class
   */
  public static List<Field> getEntitySimpleColumnFields(Class<?> cls) {
    return Arrays.stream(cls.getDeclaredFields())
        .filter(field -> !isCollectionEntityField(field) && !isToOneRelation(field))
        .toList();
  }

  /**
   * Retrieves a list of pairs containing column names and their corresponding values from the provided entity.
   * The method filters out fields representing relations to other entities, returning only simple columns.
   *
   * @param entity - The entity object from which to retrieve simple column values
   * @param <T> - The type of the entity
   * @return A list of pairs, where each pair consists of a column name and its corresponding value
   * @see #getEntitySimpleColumnFields(Class)
   * @see #resolveColumnName(Field)
   * @see #readEntityColumnValue(Object, Field)
   */
  public static <T> List<Pair<String, Object>> getEntitySimpleColumnValues(T entity) {
    return getEntitySimpleColumnFields(entity.getClass()).stream()
        .map(field -> Pair.of(resolveColumnName(field), readEntityColumnValue(entity, field)))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves a list of simple column names from the fields of the provided entity class.
   * Simple columns are those that do not represent relations to other entities or collections.
   *
   * @param entityClass - The entity class for which to retrieve simple column names
   * @param <T> - The type of the entity class
   * @return A list of simple column names representing non-relation and non-collection fields in the entity class
   * @see #getEntitySimpleColumnFields(Class)
   * @see EntityUtil#resolveColumnName(Field)
   */
  public static <T> List<String> getEntitySimpleColumnNames(Class<T> entityClass) {
    return getEntitySimpleColumnFields(entityClass).stream()
        .map(EntityUtil::resolveColumnName)
        .collect(Collectors.toList());
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
   * Determines if the given field is a @OneToOne or @ManyToOne relation entity field.
   *
   * @param field - The field to check
   * @return true if the field is a 'toOne' relation entity field, false otherwise
   */
  public static boolean isToOneRelation(Field field) {
    return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
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

  public static List<Field> getClassColumnFields(Class<?> cls, Predicate<Field> fieldPredicate) {
    return Arrays.stream(cls.getDeclaredFields())
        .filter(fieldPredicate)
        .toList();
  }

  /**
   * Finds the entity ID field from a list of fields.
   *
   * @param fields - The list of fields to search
   * @return The entity ID field
   * @throws EntityParseException if the entity does not define an ID column or if multiple fields
   *                              are marked with the 'Id' annotation
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
   *                              are marked with the 'Id' annotation
   */
  public static Field findEntityIdField(Class<?> cls) {
    validateIsEntity(cls);
    return findEntityIdField(getClassColumnFields(cls));
  }

  /**
   * Finds and returns the name of the entity ID field for the given entity class. The entity ID
   * field is determined based on the presence of the 'Id' annotation.
   *
   * @param entityClass - The entity class for which to find the ID field name
   * @param <T>         - The type of the entity class
   * @return The name of the entity ID field
   * @throws EntityParseException if the entity does not define an ID column or if multiple fields
   *                              are marked with the 'Id' annotation
   */
  public static <T> String findEntityIdFieldName(Class<T> entityClass) {
    return findEntityIdField(entityClass).getName();
  }

  /**
   * Retrieves the ID value of the given entity.
   *
   * @param entity - The entity object
   * @return The ID value of the entity object
   * @throws EntityParseException if the entity does not define an ID column or if multiple fields
   *                              are marked with the 'Id' annotation
   */
  public static Object getEntityId(Object entity) {
    var idField = findEntityIdField(entity.getClass());
    return readFieldValue(entity, idField);
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
   * @param entityType   - The entity type
   * @param joinedEntity - The joined entity
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
   * Checks if the given field represents a simple column in an entity.
   *
   * @param field - The field to check
   * @return true if the field is a primitive column, false otherwise
   */
  public static boolean isSimpleColumn(Field field) {
    return !field.isAnnotationPresent(OneToMany.class)
        && !field.isAnnotationPresent(ManyToOne.class)
        && !field.isAnnotationPresent(ManyToMany.class)
        && !field.isAnnotationPresent(OneToOne.class);
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
   * Retrieves an instance of the Collection class based on the given Field representing a
   * collection.
   *
   * @param collectionField - The Field representing the collection
   * @return An instance of the Collection class
   * @throws IllegalArgumentException if the collection type is unsupported
   */
  public static Collection<Object> getCollectionInstance(Field collectionField) {
    var collectionClass = collectionField.getType();

    if (collectionClass.isAssignableFrom(List.class)) {
      return new ArrayList<>();
    }

    if (collectionClass.isAssignableFrom(Set.class)) {
      return new HashSet<>();
    }

    throw new IllegalArgumentException(
        "Unsupported collection: " + collectionClass); // change exception and more clear msg?
  }


  /**
   * Reads the value of a field on the given entity object.
   *
   * @param entity  - The entity object
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

  /**
   * Checks if the specified entity class has any @OneToOne or @ManyToOne relations.
   * This method examines the declared fields of the class to identify if any field
   * is marked as a 'toOne' relation based on the presence of @OneToOne or @ManyToOne annotations.
   *
   * @param cls - The entity class to check for 'toOne' relations
   * @param <T> - The type of the entity class
   * @return true if the entity class has at least one 'toOne' relation, false otherwise
   */
  public static <T> boolean hasToOneRelations(Class<T> cls) {
    return Arrays.stream(cls.getDeclaredFields())
        .anyMatch(EntityUtil::isToOneRelation);
  }

  /**
   * Checks if the specified entity class is marked with the {@link DynamicUpdate} annotation,
   * indicating that dynamic update behavior is enabled for this entity.
   *
   * @param entityClass - The entity class to check for dynamic update annotation
   * @param <T> - The type of the entity class
   * @return true if the entity class is annotated with {@link DynamicUpdate}, false otherwise
   */
  public static <T> boolean isDynamicUpdate(Class<T> entityClass) {
    return entityClass.isAnnotationPresent(DynamicUpdate.class);
  }

  /**
   * Retrieves an array of column values from the provided entity object including toOne relation
   * columns. The method extracts values from all the columns of the entity based on its class
   * definition.
   *
   * @param entity - The entity object from which to retrieve column values
   * @param <T>    - The type of the entity
   * @return An array of column values corresponding to the entity's columns
   */
  public static <T> Object[] getEntityColumnValues(T entity) {
    return getClassColumnFields(entity.getClass()).stream()
        .map(field -> readEntityColumnValue(entity, field))
        .toArray();
  }

  /**
   * Retrieves a list of pairs containing the types and associated ID values of the 'toOne' relation
   * fields from the provided entity.
   *
   * @param entity - The entity object from which to retrieve 'toOne' relation values
   * @param <T>    - The type of the entity
   * @return A list of pairs, where each pair consists of the 'toOne' relation field type and its
   * associated ID value (or null if the field value is null)
   * @see EntityUtil#isToOneRelation(Field)
   * @see #readFieldValue(Object, Field)
   * @see #getEntityId(Object)
   */
  public static <T> List<Triple<? extends Class<?>, String, Object>> getEntityToOneRelationValues(T entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(EntityUtil::isToOneRelation)
        .map(field -> {
          Object relatedIdField = readFieldValue(entity, field);
          return Triple.of(field.getType(), resolveColumnName(field),
              relatedIdField != null ? getEntityId(relatedIdField) : null);
        })
        .collect(Collectors.toList());
  }

  private static <T> Object readEntityColumnValue(T entity, Field field) {
    return isToOneRelation(field) ? readToOneRelatedEntityId(entity, field)
        : readFieldValue(entity, field);
  }

  private static <T> Object readToOneRelatedEntityId(T entity, Field field) {
    Object relatedEntity = readFieldValue(entity, field);
    Field relatedEntityIdField = findEntityIdField(field.getType());
    return relatedEntity != null ? readFieldValue(relatedEntity, relatedEntityIdField) : null;
  }
}
