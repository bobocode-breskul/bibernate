package com.breskul.bibernate.metadata;

import static com.breskul.bibernate.metadata.CommonSQLTypesConverter.DEFAULT_VARCHAR_LENGTH;
import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;

import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.metadata.dto.ForeignKey;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;


/**
 * The EntitiesMetadataPersistence class represents the persistence mechanism for storing and retrieving metadata about entities.
 * It initializes and maintains a collection of Table objects that represent the entity tables in the database.
 */
public class EntitiesMetadataPersistence {

  private final Map<String, Table> tables = new HashMap<>();

  private final Supplier<Set<Class<?>>> entitiesSupplier;

  private EntitiesMetadataPersistence(Supplier<Set<Class<?>>> entitiesSupplier) {
    this.entitiesSupplier = entitiesSupplier;
  }

  /**
   * Creates an instance of EntitiesMetadataPersistence by using the provided entitiesSupplier to retrieve the set of entity classes.
   * The method initializes the EntitiesMetadataPersistence object by populating the metadata for each entity class.
   *
   * @param entitiesSupplier the supplier function to retrieve the set of entity classes
   * @return the initialized EntitiesMetadataPersistence object
   */
  public static EntitiesMetadataPersistence createInstance(Supplier<Set<Class<?>>> entitiesSupplier) {
    return new EntitiesMetadataPersistence(entitiesSupplier).init();
  }
  /**
   * Retrieves all the Table objects in the EntitiesMetadataPersistence.
   *
   * @return a Set of Table objects
   */
  public Set<Table> getTables() {
    return new HashSet<>(tables.values());
  }

  /**
   * Retrieves the Table object corresponding to the given table name.
   *
   * @param tableName the name of the table
   * @return the Table object corresponding to the given table name, null if no match is found
   */
  public Table getTable(String tableName) {
    return tables.get(tableName);
  }

  /**
   * Initializes the EntitiesMetadataPersistence object by populating the metadata for each entity class.
   *
   * @return this, initialized EntitiesMetadataPersistence object.
   */
  private EntitiesMetadataPersistence init() {
    Set<Class<?>> entities = entitiesSupplier.get();
    for (Class<?> entity: entities) {
      Table table = new Table();
      String tableName = getEntityTableName(entity);
      table.setName(tableName);
      if (entity.isAnnotationPresent(com.breskul.bibernate.annotation.Table.class)) {
        var tableAnnotation = entity.getAnnotation(com.breskul.bibernate.annotation.Table.class);
        table.setSchema(tableAnnotation.schema());
        table.setCatalog(tableAnnotation.schema());
      }
      setTableFullName(table);
      GeneratedColumnsData columnsData = generatedColumnsData(entity);
      table.setColumns(columnsData.columns());
      columnsData.foreignKeys().forEach(table::addForeignKey);
      tables.put(tableName, table);
    }
    return this;
  }


  private void setTableFullName(Table table) {
    String fullName = "";
    if (table.getCatalog() != null && !table.getCatalog().isBlank()) {
      fullName += table.getCatalog() + ".";
    }
    if (table.getSchema() != null && !table.getSchema().isBlank()) {
      fullName += table.getSchema() + ".";
    }
    fullName += table.getName();
    table.setFullName(fullName);
  }

  private GeneratedColumnsData generatedColumnsData(Class<?> entity) {
    List<Field> simpleFields = getClassEntityFields(entity);
    Map<String, Column> columns = new LinkedHashMap<>();
    Set<ForeignKey> foreignKeys = new LinkedHashSet<>();
    for (Field field: simpleFields) {
      Column column;
      if (EntityUtil.isSimpleColumn(field)) {
        column = createColumn(field);
        columns.put(column.getName(), column);
      } else {
        if (field.isAnnotationPresent(OneToOne.class)) {
          OneToOne oneToOne = field.getAnnotation(OneToOne.class);
          if (oneToOne.mappedBy().isBlank()) {
            column = generateOneToOneColumn(field);
            foreignKeys.add(generateForeignKey(entity, field));
          } else {
            validateMappedByExists(field, oneToOne.mappedBy());
            continue;
          }
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
          column = generateManyToOneColumn(field);
          foreignKeys.add(generateForeignKey(entity, field));
        } else {
          continue;
        }
      }

      if (field.isAnnotationPresent(Id.class)) {
        column.setPrimaryKey(true);
      }
      columns.put(column.getName(), column);
    }
    return new GeneratedColumnsData(columns, foreignKeys);
  }

  private void validateMappedByExists(Field field, String mappedByField) {
    boolean isPresent = Arrays.stream(field.getType().getDeclaredFields())
        .anyMatch(f -> mappedByField.equals(f.getName()));

    if (!isPresent) {
      throw new IllegalStateException("mappedBy %s field is not exists in %s"
          .formatted(mappedByField, field.getType()));
    }
  }

  private Column generateManyToOneColumn(Field field) {
    Field relatedEntityIdField = EntityUtil.findEntityIdField(field.getType());
    Column column = createColumn(relatedEntityIdField);
    column.setName(resolveColumnName(field));
    return column;
  }

  private Column generateOneToOneColumn(Field field) {
    Field relatedEntityIdField = EntityUtil.findEntityIdField(field.getType());
    Column column = createColumn(relatedEntityIdField);
    column.setUnique(true);
    column.setName(resolveColumnName(field));
    return column;
  }

  private ForeignKey generateForeignKey(Class<?> cls, Field field) {
    String tableName = getEntityTableName(cls);
    String fieldName = resolveColumnName(field);
    String relatedTableName = getEntityTableName(field.getType());
    String constraintId = "FK" + UUID.randomUUID().toString().replace("-", "");
    return new ForeignKey(tableName, fieldName, relatedTableName, constraintId);
  }

  private Column createColumn(Field field) {
    Column column = new Column();
    var columnAnnotation = field.getAnnotation(com.breskul.bibernate.annotation.Column.class);
    if (columnAnnotation != null) {
      column.setNullable(columnAnnotation.nullable());
      column.setLength(columnAnnotation.length());
      column.setUnique(columnAnnotation.unique());
      column.setPrecision(columnAnnotation.precision());
      column.setScale(columnAnnotation.scale());
    } else {
      column.setNullable(!isBasicType(field));
      column.setLength(DEFAULT_VARCHAR_LENGTH);
      column.setUnique(false);
      column.setPrecision(0);
      column.setScale(0);
    }
    column.setJavaType(field.getType());
    column.setName(resolveColumnName(field));
    column.setSqlTypeName(CommonSQLTypesConverter.getSQLType(field).getName());
    return column;
  }

  private boolean isBasicType(Field field) {
    Class<?> fieldType = field.getType();
    return fieldType.equals(int.class)
        || fieldType.equals(long.class)
        || fieldType.equals(boolean.class)
        || fieldType.equals(double.class)
        || fieldType.equals(float.class)
        || fieldType.equals(char.class)
        || fieldType.equals(byte.class);
  }

  private record GeneratedColumnsData(Map<String, Column> columns, Set<ForeignKey> foreignKeys){

  }
}
