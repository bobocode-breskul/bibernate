package com.breskul.bibernate.metadata;


import static org.assertj.core.api.Assertions.assertThat;

import com.breskul.bibernate.metadata.dto.ForeignKey;
import com.breskul.bibernate.metadata.tables.Entity1;
import com.breskul.bibernate.metadata.tables.Entity2;
import com.breskul.bibernate.metadata.tables.Entity3;
import com.breskul.bibernate.metadata.tables.Entity4;
import com.breskul.bibernate.util.EntityUtil;
import java.lang.reflect.Field;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EntitiesMetadataPersistenceTest {
  private static final EntitiesMetadataPersistence entitiesMetadataPersistence = EntitiesMetadataPersistence.createInstance(
      () -> Set.of(Entity1.class, Entity2.class, Entity3.class, Entity4.class));

  private static final String TABLE_ONE_NAME = "ent_one";
  private static final String TABLE_TWO_NAME = "ent_two";
  private static final String TABLE_THREE_NAME = "ent_tree";
  private static final String TABLE_FOUR_NAME = "ent_four";


  @Test
  public void given_threeClasses_when_allAnnotatedEntity_then_preparedThreeTableMetadata() {
    assertThat(entitiesMetadataPersistence.getTables().size()).isEqualTo(4);
  }

  @Test
  public void given_annotatedColumn_when_convertEntityToTable_then_setAllPropertiesCorrect() {
    Table table = entitiesMetadataPersistence.getTable(TABLE_ONE_NAME);
    String columnWithOverridePropertiesName = "name_from_col";
    Column column = table.getColumn(columnWithOverridePropertiesName);

    assertThat(column).isNotNull();
    assertThat(column.isNullable()).isFalse();
    assertThat(column.getLength()).isEqualTo(20);
    assertThat(column.isPrimaryKey()).isFalse();
    assertThat(column.isUnique()).isTrue();
    assertThat(column.getPrecision()).isEqualTo(10);
    assertThat(column.getScale()).isEqualTo(5);
  }

  @Test
  public void given_notAnnotatedColumn_when_convertEntityToTable_then_setDefaultProperties() {
    Table table = entitiesMetadataPersistence.getTable(TABLE_ONE_NAME);
    String fieldWithoutColumn = "fieldWithoutColumn";
    Column column = table.getColumn(fieldWithoutColumn);

    assertThat(column).isNotNull();
    assertThat(column.isNullable()).isTrue();
    assertThat(column.getLength()).isEqualTo(255);
    assertThat(column.isPrimaryKey()).isFalse();
    assertThat(column.isUnique()).isFalse();
    assertThat(column.getPrecision()).isEqualTo(0);
    assertThat(column.getScale()).isEqualTo(0);
  }

  @Test
  public void given_notAnnotatedPrimitiveColumn_when_convertEntityToTable_then_setUniqueTrue() {
    Table table = entitiesMetadataPersistence.getTable(TABLE_ONE_NAME);
    String basicClassWithoutColumn = "basicClassWithoutColumn";
    Column column = table.getColumns()
        .stream()
        .filter(col -> col.getName().equals(basicClassWithoutColumn))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Can't find field by column name "
            + basicClassWithoutColumn));

    assertThat(column.isNullable()).isFalse();
  }

  @Test
  public void given_manyToOneColumn_when_convertEntityToTable_then_createCorrectColumnAndFK()
      throws NoSuchFieldException {
    Table manyToOneTable = entitiesMetadataPersistence.getTable(TABLE_TWO_NAME);
    Table oneToManyTable = entitiesMetadataPersistence.getTable(TABLE_THREE_NAME);
    Class<?> oneToManyTableIdType = Entity3.class.getDeclaredField("id").getType();
    String manyToOneFieldName = "entity3";
    Field manyToOneField = Entity2.class.getDeclaredField(manyToOneFieldName);
    String joinColumnName = EntityUtil.resolveColumnName(manyToOneField);
    Column joinColumn = manyToOneTable.getColumn(joinColumnName);

    String foreignKeyFieldName = EntityUtil.getJoinColumnName(Entity2.class, Entity3.class);
    ForeignKey foreignKey = manyToOneTable.getForeignKey(foreignKeyFieldName);

    assertThat(oneToManyTable.getForeignKeys()).isEmpty();
    assertThat(oneToManyTable.getColumns().size()).isEqualTo(1);
    assertThat(oneToManyTable.getColumn("id")).isNotNull(); //contains id field only.

    assertThat(joinColumn).isNotNull();
    assertThat(joinColumn.getJavaType()).isEqualTo(oneToManyTableIdType);
    assertThat(foreignKey.getFieldName()).isEqualTo(foreignKeyFieldName);
    assertThat(foreignKey.getTableName()).isEqualTo(TABLE_TWO_NAME);
    assertThat(foreignKey.getRelatedTableName()).isEqualTo(TABLE_THREE_NAME);
    assertThat(foreignKey.getConstraintId()).startsWith("FK");
  }

  @Test
  public void given_oneToOneColumnWithMappedBY_when_convertEntityToTable_then_createJoinColumnInOneTableAndFKey()
      throws NoSuchFieldException {
    Table secondTable = entitiesMetadataPersistence.getTable(TABLE_TWO_NAME);
    Table firstTable = entitiesMetadataPersistence.getTable(TABLE_ONE_NAME);
    Class<?> secondTableIdType = Entity2.class.getDeclaredField("id").getType();
    Field firstEntityJoinField = Entity1.class.getDeclaredField("entity2");
    String firstEntityJoinColumnName = EntityUtil.resolveColumnName(firstEntityJoinField);
    Column mappedJoinColumn = firstTable.getColumn(firstEntityJoinColumnName);
    ForeignKey foreignKey = firstTable.getForeignKey(firstEntityJoinColumnName);

    assertThat(mappedJoinColumn).isNotNull();
    assertThat(mappedJoinColumn.getJavaType()).isEqualTo(secondTableIdType);
    assertThat(foreignKey).isNotNull();
    assertThat(foreignKey.getTableName()).isEqualTo(TABLE_ONE_NAME);
    assertThat(foreignKey.getRelatedTableName()).isEqualTo(TABLE_TWO_NAME);
    assertThat(foreignKey.getConstraintId()).startsWith("FK");

    Field secondEntityJoinField = Entity2.class.getDeclaredField("entity1");
    String secondEntityJoinColumnName = EntityUtil.resolveColumnName(secondEntityJoinField);
    Column secondJoinColumn = secondTable.getColumn(secondEntityJoinColumnName);
    ForeignKey foreignKeyOpposite = secondTable.getForeignKey(secondEntityJoinColumnName);

    assertThat(secondJoinColumn).isNull();
    assertThat(foreignKeyOpposite).isNull();
  }

  @Test
  public void given_OneToOneColumnWithoutMappedBy_when_convertEntityToTable_then_createJoinColumnAndFKInBothTables()
      throws NoSuchFieldException {
    Table secondTable = entitiesMetadataPersistence.getTable(TABLE_TWO_NAME);
    Table fourTable = entitiesMetadataPersistence.getTable(TABLE_FOUR_NAME);
    Class<?> secondTableIdType = Entity2.class.getDeclaredField("id").getType();

    Field fourEntityJoinField = Entity1.class.getDeclaredField("entity2");
    String fourEntityJoinColumnName = EntityUtil.resolveColumnName(fourEntityJoinField);
    Column joinColumn = fourTable.getColumn(fourEntityJoinColumnName);
    ForeignKey foreignKey = fourTable.getForeignKey(fourEntityJoinColumnName);

    assertThat(joinColumn).isNotNull();
    assertThat(joinColumn.getJavaType()).isEqualTo(secondTableIdType);
    assertThat(foreignKey).isNotNull();
    assertThat(foreignKey.getFieldName()).isEqualTo(fourEntityJoinColumnName);
    assertThat(foreignKey.getTableName()).isEqualTo(TABLE_FOUR_NAME);
    assertThat(foreignKey.getRelatedTableName()).isEqualTo(TABLE_TWO_NAME);
    assertThat(foreignKey.getConstraintId()).startsWith("FK");


    Class<?> fourTableIdType = Entity4.class.getDeclaredField("id").getType();
    Field secondEntityJoinField = Entity2.class.getDeclaredField("entity4");
    String secondEntityJoinColumnName = EntityUtil.resolveColumnName(secondEntityJoinField);
    Column secondJoinColumn = secondTable.getColumn(secondEntityJoinColumnName);

    assertThat(secondJoinColumn).isNotNull();
    assertThat(secondJoinColumn.getJavaType()).isEqualTo(fourTableIdType);
    ForeignKey foreignKeyOpposite = secondTable.getForeignKey(secondEntityJoinColumnName);

    assertThat(foreignKeyOpposite).isNotNull();
    assertThat(foreignKeyOpposite.getFieldName()).isEqualTo(secondEntityJoinColumnName);
    assertThat(foreignKeyOpposite.getTableName()).isEqualTo(TABLE_TWO_NAME);
    assertThat(foreignKeyOpposite.getRelatedTableName()).isEqualTo(TABLE_FOUR_NAME);
    assertThat(foreignKeyOpposite.getConstraintId()).startsWith("FK");
  }

}