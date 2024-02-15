package com.breskul.bibernate.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.DynamicUpdate;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import com.breskul.bibernate.exception.EntityParseException;
import com.breskul.bibernate.persistence.context.snapshot.EntityPropertySnapshot;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EntityUtilTest {

  @Test
  @DisplayName("When source and target entity are same then no changes are done")
  @Order(1)
  void whenCopyChangedValuesWithoutChanges_thenNoChangesDone() {
    // data
    TestEntity sourceEntity = new TestEntity();
    sourceEntity.id = 1;
    sourceEntity.name = "Source Entity";

    TestEntity targetEntity = new TestEntity();
    targetEntity.id = 1;
    targetEntity.name = "Source Entity";
    // given
    // when
    EntityUtil.copyChangedValues(sourceEntity, targetEntity);
    // verify
    assertThat(targetEntity.id).isEqualTo(1);
    assertThat(targetEntity.name).isEqualTo("Source Entity");
  }

  @Test
  @DisplayName("When source and target entity has some fields with different values then only that "
      + "values are copied")
  @Order(2)
  void whenCopyChangedValuesWithPartialChanges_thenSomeChangesDone() {
    // data
    TestEntity sourceEntity = new TestEntity();
    sourceEntity.id = 1;
    sourceEntity.name = "Updated Entity";

    TestEntity targetEntity = new TestEntity();
    targetEntity.id = 1;
    targetEntity.name = "Source Entity";
    // given
    // when
    EntityUtil.copyChangedValues(sourceEntity, targetEntity);
    // verify
    assertThat(targetEntity.id).isEqualTo(1);
    assertThat(targetEntity.name).isEqualTo("Updated Entity");
  }

  @Test
  @DisplayName("When source and target entity are completely different then all values are copied")
  @Order(3)
  void whenCopyChangedValuesWithAllChanges_thenAllChangesDone() {
    // data
    TestEntity sourceEntity = new TestEntity();
    sourceEntity.id = 2;
    sourceEntity.name = "Updated Entity";

    TestEntity targetEntity = new TestEntity();
    targetEntity.id = 1;
    targetEntity.name = "Source Entity";
    // when
    EntityUtil.copyChangedValues(sourceEntity, targetEntity);
    // verify
    assertThat(targetEntity.id).isEqualTo(2);
    assertThat(targetEntity.name).isEqualTo("Updated Entity");
  }

  @Test
  @DisplayName("When source entity has no nullable fields then created copy entity is same as source")
  @Order(4)
  void whenCopyEntityWithoutNulls_thenCopyIsSameAsSource() {
    // data
    TestEntity testEntity = new TestEntity();
    testEntity.id = 1;
    testEntity.name = "Test";
    // when
    TestEntity createdEntity = EntityUtil.copyEntity(testEntity);
    // verify
    assertThat(createdEntity)
        .usingRecursiveComparison()
        .isEqualTo(testEntity);
  }

  @Test
  @DisplayName("When source entity has nullable fields then created copy entity is same as source")
  @Order(5)
  void whenCopyEntityWithNulls_thenCopyIsSameAsSource() {
    // data
    TestEntity testEntity = new TestEntity();
    testEntity.id = 1;
    testEntity.name = null;
    // when
    TestEntity createdEntity = EntityUtil.copyEntity(testEntity);
    // verify
    assertThat(createdEntity)
        .usingRecursiveComparison()
        .isEqualTo(testEntity);
  }

  @Test
  @DisplayName("Throw IllegalArgumentException when source entity is 'null'")
  @Order(6)
  void whenCopyEntitySourceIsNull_thenThrowException() {
    TestEntity testEntity = null;
    Assertions.assertThatThrownBy(() -> EntityUtil.copyEntity(testEntity))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Source object should not be 'null'");
  }

  @Test
  @DisplayName("When source entity has ID then its copied to target entity")
  @Order(7)
  void whenCopyEntityId_thenIdIsCopied() {
    // data
    TestEntity sourceEntity = new TestEntity();
    sourceEntity.id = 42;
    sourceEntity.name = "Test";

    TestEntity targetEntity = new TestEntity();
    targetEntity.id = 0;
    // given
    // when
    EntityUtil.copyEntityId(sourceEntity, targetEntity);
    // verify
    assertThat(targetEntity.id).isEqualTo(42);
  }


  @Test
  @DisplayName("When entity annotated with @DynamicUpdate return true")
  @Order(8)
  void givenDynamicUpdateAnnotatedEntity_whenIsDynamicUpdate_thenReturnTrue() {
    // given
    DynamicUpdateEntity entity = new DynamicUpdateEntity();

    // when
    boolean result = EntityUtil.isDynamicUpdate(entity.getClass());

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("When entity is not annotated with @DynamicUpdate return false")
  @Order(9)
  void givenEntity_whenIsDynamicUpdate_thenReturnFalse() {
    // given
    TestEntity entity = new TestEntity();

    // when
    boolean result = EntityUtil.isDynamicUpdate(entity.getClass());

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("When get entity column values then all values returned including relation entity id")
  @Order(10)
  void givenEntityWithRelation_whenGetEntityColumnValues_ThenReturnAllValues() {
    // given
    RelatedEntity relatedEntity = new RelatedEntity(1, "test");
    TestEntity testEntity = new TestEntity(1, "John", 20, relatedEntity);

    //when
    Object[] values = EntityUtil.getEntityColumnValues(testEntity);

    //then
    assertThat(values).containsExactly(testEntity.getId(), testEntity.getName(), testEntity.getAge(),
        testEntity.getRelatedEntity().getId());
  }

  @Test
  @DisplayName("When get entity 'toOne' relation values for then return snapshot")
  @Order(11)
  void givenEntityWithRelation_whenGetEntityToOneRelationValues_ThenReturnToOneRelationSnapshots() {
    // given
    RelatedEntity relatedEntity = new RelatedEntity(1, "test");
    TestEntity testEntity = new TestEntity(1, "John", 20, relatedEntity);

    // when
    List<EntityRelationSnapshot> result =
        EntityUtil.getEntityToOneRelationValues(testEntity);

    // then
    var data = EntityRelationSnapshot.of(RelatedEntity.class, "related_entity_id", 1);
    assertThat(result).containsExactlyInAnyOrder(data);
  }

  @Test
  @DisplayName("When get entity 'toOne' relation values for nullable relation then return snapshot of nullable id")
  @Order(12)
  void givenEntityWithNullRelation_whenGetEntityToOneRelationValues_thenReturnToOneRelationSnapshotWithNullableId() {
    // given
    TestEntity entityWithRelation = new TestEntity(1, "Mike", 20, null);

    // when
    List<EntityRelationSnapshot> result =
        EntityUtil.getEntityToOneRelationValues(entityWithRelation);

    // then
    var data = EntityRelationSnapshot.of(RelatedEntity.class, "related_entity_id", null);
    assertThat(result).containsExactlyInAnyOrder(data);
  }


  @Test
  @DisplayName("When get enity columns names then all columns names returned")
  @Order(13)
  void givenEntity_whenGetEntityColumnNames_thenReturnColumnNames(){
    // given
    var entityClass= TestEntity.class;

    //when
    List<String> result = EntityUtil.getEntityColumnNames(entityClass);

    //then
    assertThat(result).containsExactlyInAnyOrder("id", "name", "age", "related_entity_id");
  }

  @Test
  void givenValidEntity_whenValidateIsEntity_thenShouldNotThrowError() {
    assertDoesNotThrow(() -> EntityUtil.validateIsEntity(TestEntity.class));
  }

  @Test
  void givenNotValidEntity_whenValidateIsEntity_thenShouldThrowEntityParseException() {
    assertThatThrownBy(() -> EntityUtil.validateIsEntity(String.class))
        .isInstanceOf(EntityParseException.class)
        .hasMessage("Class should be marked with 'Entity' annotation");
  }

  @Test
  void givenValidEntity_whenValidateColumnName_thenShouldNotThrowError() {
    assertDoesNotThrow(() -> EntityUtil.validateColumnName(TestEntity.class, "name"));
  }

  @Test
  void givenNotValidEntity_whenValidateColumnName_thenShouldThrowIllegalArgumentException() {
    assertThatThrownBy(() -> EntityUtil.validateColumnName(TestEntity.class, "nonexistent"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Entity [class com.breskul.bibernate.util.EntityUtilTest$TestEntity] does not have a column: [nonexistent].");
  }

  @Test
  void givenEntity_whenGetEntityTableName_thenhouldReturnEntityTableName() {
    assertThat(EntityUtil.getEntityTableName(TestEntity.class)).isEqualTo("test_entity");
  }

  @Test
  void givenEntityWithOutTableAnnotation_whenGetEntityTableName_thenhouldReturnEntityTableName() {
    assertThat(EntityUtil.getEntityTableName(RelatedEntity.class)).isEqualTo("Related_Entity");
  }

  @Test
  void givenEntity_whenGetEntitySimpleColumnValues_thenShouldReturnColumnValues() {
    TestEntity testEntity = new TestEntity();
    testEntity.setId(1);
    testEntity.setName("John Doe");
    testEntity.setAge(30);

    List<EntityPropertySnapshot> propertySnapshots = EntityUtil.getEntitySimpleColumnValues(
        testEntity);

    assertThat(propertySnapshots.size()).isEqualTo(3);

    assertThat(propertySnapshots.get(0).columnName()).isEqualTo("id");
    assertThat(propertySnapshots.get(0).columnValue()).isEqualTo(1);

    assertThat(propertySnapshots.get(1).columnName()).isEqualTo("name");
    assertThat(propertySnapshots.get(1).columnValue()).isEqualTo("John Doe");

    assertThat(propertySnapshots.get(2).columnName()).isEqualTo("age");
    assertThat(propertySnapshots.get(2).columnValue()).isEqualTo(30);
  }

  @Test
  void givenEntity_whenGetClassEntityFields_thenShouldReturnEntityFields() {
    List<Field> entityFields = EntityUtil.getClassEntityFields(TestEntity.class);

    assertThat(entityFields.size()).isEqualTo(4);

    assertThat(entityFields).anyMatch(field -> field.getName().equals("id"));
    assertThat(entityFields).anyMatch(field -> field.getName().equals("name"));
    assertThat(entityFields).anyMatch(field -> field.getName().equals("age"));
    assertThat(entityFields).anyMatch(field -> field.getName().equals("relatedEntity"));
  }

  @Test
  void givenPredicate_whenGetClassColumnFieldsWithPredicate_thenShouldReturnFilteredFields() {
    Predicate<Field> fieldPredicate = field -> !field.getName().equals("id");

    List<Field> columnFields = EntityUtil.getClassColumnFields(TestEntity.class, fieldPredicate);

    assertThat(columnFields).hasSize(3);

    assertThat(columnFields).anyMatch(field -> field.getName().equals("name"));
    assertThat(columnFields).anyMatch(field -> field.getName().equals("age"));
    assertThat(columnFields).anyMatch(field -> field.getName().equals("relatedEntity"));
    assertThat(columnFields).noneMatch(field -> field.getName().equals("id"));
  }

  @Test
  void givenNoIdEntity_whenFindEntityIdFieldWithNoIdField_thenShouldThrowEntityParseException() {
    List<Field> entityFields = List.of(NoIdEntity.class.getDeclaredFields());

    assertThatExceptionOfType(EntityParseException.class)
        .isThrownBy(() -> EntityUtil.findEntityIdField(entityFields))
        .withMessage("Entity should define ID column marked with 'Id' annotation.");
  }

  @Test
  void givenMultipleIdEntity_whenFindEntityIdFieldWithMultipleIdFields_thenShouldThrowEntityParseException() {
    List<Field> entityFields = List.of(MultipleIdEntity.class.getDeclaredFields());

    assertThatExceptionOfType(EntityParseException.class)
        .isThrownBy(() -> EntityUtil.findEntityIdField(entityFields))
        .withMessage("Only one field should be marked with 'Id' annotation.");
  }

  @Test
  void givenEntity_whenFindEntityIdFieldName_thenShouldReturnEntityIdFieldName() {
    String idFieldName = EntityUtil.findEntityIdFieldName(TestEntity.class);

    assertThat(idFieldName).isEqualTo("id");
  }

  @Test
  void givenEntity_whenGetEntityId_thenShouldReturnEntity() {
    TestEntity testEntity = new TestEntity();
    testEntity.setId(42);

    Object entityId = EntityUtil.getEntityId(testEntity);

    assertThat(entityId).isEqualTo(42);
  }

  @Test
  void givenEntity_whenComposeSelectBlockFromColumns_thenShouldReturnComposeSelectBlockFromColumns() {
    List<Field> columns = EntityUtil.getClassEntityFields(TestEntity.class);

    String selectBlock = EntityUtil.composeSelectBlockFromColumns(columns);

    assertThat(selectBlock).isEqualTo("id, name, age, related_entity_id");
  }

  @Test
  void testResolveColumnName() throws NoSuchFieldException {
    assertThat(EntityUtil.resolveColumnName(TestEntity.class.getDeclaredField("id")))
        .isEqualTo("id");
    assertThat(EntityUtil.resolveColumnName(TestEntity.class.getDeclaredField("name")))
        .isEqualTo("name");
    assertThat(EntityUtil.resolveColumnName(TestEntity.class.getDeclaredField("relatedEntity")))
        .isEqualTo("related_entity_id");
  }

  @Test
  void givenEntityWithRelatedEntity_whenGetJoinColumnName_thenShouldReturnJoinColumn() {
    assertThat(EntityUtil.getJoinColumnName(TestEntity.class, RelatedEntity.class))
        .isEqualTo("related_entity_id");
  }

  @Test
  void givenEntityWithNotRelatedEntity_whenGetJoinColumnName_thenShouldThrowIllegalStateException() {
    assertThatThrownBy(() -> EntityUtil.getJoinColumnName(TestEntity.class, NoIdEntity.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Can't find related entity [%s] field in [%s]."
            .formatted(NoIdEntity.class, TestEntity.class));
  }

  @Test
  void givenEntity_whenGetEntityCollectionElementType_thenShouldReturnCorrectElementType()
      throws NoSuchFieldException {
    Field stringListField = TestEntityCollection.class.getDeclaredField("stringList");
    Field integerListField = TestEntityCollection.class.getDeclaredField("integerList");

    assertThat(EntityUtil.getEntityCollectionElementType(stringListField)).isEqualTo(String.class);
    assertThat(EntityUtil.getEntityCollectionElementType(integerListField)).isEqualTo(
        Integer.class);
  }

  @Test
  void givenEntityWithRelation_whenHasToOneRelations_thenShouldReturnTrue() {
    assertThat(EntityUtil.hasToOneRelations(TestRelationEntity.class)).isTrue();
  }

  @Test
  void givenEntityClasses_whenGetAllEntitiesClasses_shouldReturnAllEntityClasses() {
    Set<Class<?>> entityClasses = EntityUtil.getAllEntitiesClasses();

    assertThat(entityClasses).contains(TestEntity.class, RelatedEntity.class);
  }

  /**
   * This class represents a test entity.
   */
  @AllArgsConstructor
  @Getter
  @Entity
  @Table(name = "test_entity")
  @Setter
  private static class TestEntity {

    @Id
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private int age;

    @ManyToOne
    @JoinColumn(name = "related_entity_id")
    private RelatedEntity relatedEntity;

    public TestEntity() {

    }
  }

  @Entity
  private static class RelatedEntity {
    @Id
    @Getter
    private int id;

    @Column(name = "description")
    private String description;

    public RelatedEntity() {

    }

    public RelatedEntity(int id, String description) {
      this.id = id;
      this.description = description;
    }
  }

  private static class NoIdEntity {

    private Long entityId;
    private String name;
  }

  private static class MultipleIdEntity {

    @Id
    private Long id1;

    @Id
    private Long id2;
  }

  private static class TestEntityCollection {

    private List<String> stringList;
    private List<Integer> integerList;
  }

  private static class TestRelationEntity {

    private String simpleField;
    @OneToMany
    private List<String> oneToManyList;
    @ManyToOne
    private String manyToOneField;
    @OneToOne
    private String oneToOneField;
  }

  @DynamicUpdate
  @Entity
  static class DynamicUpdateEntity {

  }
}