package com.breskul.bibernate.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.DynamicUpdate;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.persistence.context.snapshot.EntityRelationSnapshot;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    TestEntity testEntity = new TestEntity(1, "John");
    EntityWithRelation entityWithRelation = new EntityWithRelation(1L, "Mike", testEntity);

    //when
    Object[] values = EntityUtil.getEntityColumnValues(entityWithRelation);

    //then
    assertThat(values).containsExactly(entityWithRelation.getId(), entityWithRelation.getName(),
        entityWithRelation.getTestEntity().getId());
  }

  @Test
  @DisplayName("When get entity 'toOne' relation values for then return snapshot")
  @Order(11)
  void givenEntityWithRelation_whenGetEntityToOneRelationValues_ThenReturnToOneRelationSnapshots() {
    // given
    TestEntity testEntity = new TestEntity(1, "John");
    EntityWithRelation entityWithRelation = new EntityWithRelation(1L, "Mike", testEntity);

    // when
    List<EntityRelationSnapshot> result =
        EntityUtil.getEntityToOneRelationValues(entityWithRelation);

    // then
    var data = EntityRelationSnapshot.of(TestEntity.class, "testEntity_id", 1);
    assertThat(result).containsExactlyInAnyOrder(data);
  }

  @Test
  @DisplayName("When get entity 'toOne' relation values for nullable relation then return snapshot of nullable id")
  @Order(12)
  void givenEntityWithNullRelation_whenGetEntityToOneRelationValues_thenReturnToOneRelationSnapshotWithNullableId() {
    // given
    EntityWithRelation entityWithRelation = new EntityWithRelation(1L, "Mike", null);

    // when
    List<EntityRelationSnapshot> result =
        EntityUtil.getEntityToOneRelationValues(entityWithRelation);

    // then
    var data = EntityRelationSnapshot.of(TestEntity.class, "testEntity_id", null);
    assertThat(result).containsExactlyInAnyOrder(data);
  }


  @Test
  @DisplayName("When get enity columns names then all columns names returned")
  @Order(13)
  void givenEntity_whenGetEntityColumnNames_thenReturnColumnNames(){
    // given
    var entityClass= EntityWithRelation.class;

    //when
    List<String> result = EntityUtil.getEntityColumnNames(entityClass);

    //then
    assertThat(result).containsExactlyInAnyOrder("id", "name", "testEntity_id");
  }


  /**
   * This class represents a test entity.
   */
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Entity
  public static class TestEntity {

    @Id
    int id;
    String name;
  }

  @DynamicUpdate
  @Entity
  static class DynamicUpdateEntity {

  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Entity
  static class EntityWithRelation {

    @Id
    private Long id;

    @Column
    private String name;

    @ManyToOne
    private TestEntity testEntity;
  }
}