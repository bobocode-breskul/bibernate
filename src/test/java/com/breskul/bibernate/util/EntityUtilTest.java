package com.breskul.bibernate.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
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

  /**
   * This class represents a test entity.
   */
  @Entity
  public static class TestEntity {

    @Id
    int id;
    String name;
  }
}