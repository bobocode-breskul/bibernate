package com.breskul.bibernate.metadata;

import static com.breskul.bibernate.metadata.PGprovider.DEFAULT_VARCHAR_LENGTH;
import static com.breskul.bibernate.util.EntityUtil.getClassEntityFields;
import static com.breskul.bibernate.util.EntityUtil.getEntityTableName;
import static com.breskul.bibernate.util.EntityUtil.resolveColumnName;

import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.metadata.dto.ForeignKey;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EntitiesMetadataPersistence {

  private PGprovider pGprovider = new PGprovider();

  private final List<Table> tables = new ArrayList<>();
  private volatile boolean initialized;

  public List<Table> getTables() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          init();
          initialized = true;
        }
      }
    }
    return tables;
  }


  private void init() {
    Set<Class<?>> entities = EntityUtil.getAllEntitiesClasses();
    for (Class<?> entity: entities) {
      Table table = new Table();
      table.setName(getEntityTableName(entity));
      if (entity.isAnnotationPresent(com.breskul.bibernate.annotation.Table.class)) {
        var tableAnnotation = entity.getAnnotation(com.breskul.bibernate.annotation.Table.class);
        table.setSchema(tableAnnotation.schema());
        table.setCatalog(tableAnnotation.schema());
      }
      setTableFullName(table);
      GeneratedColumnsData columnsData = generatedColumnsData(entity);
      table.setColumns(columnsData.columns());
      columnsData.foreignKeys().forEach(table::addForeignKey);
      tables.add(table);
    }
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
          if (oneToOne.mappedBy() == null) {
            column = generateOneToOneColumn(field);
            foreignKeys.add(generateForeignKey(entity, field));
          } else {
            // todo validate mappedBy
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
    column.setName(resolveColumnName(field));
    column.setSqlTypeName(PGprovider.getSQLType(field).getName());
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
