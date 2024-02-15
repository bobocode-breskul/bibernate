package com.breskul.bibernate.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.breskul.bibernate.persistence.GenericDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActionTest {

  @Mock
  private GenericDao dao;
  @Mock
  private Object entity;

  @Test
  void givenDeleteAction_whenExecute_thenShouldCallGenericDao() {
    new DeleteAction(dao, entity).execute();

    verify(dao, times(1)).delete(entity);
  }

  @Test
  void givenInsertAction_whenExecute_thenShouldCallGenericDao() {
    new InsertAction(dao, entity).execute();

    verify(dao, times(1)).save(entity);
  }

  @Test
  void givenUpdateAction_whenExecute_thenShouldCallGenericDao() {
    new UpdateAction<>(dao, null, null).execute();

    verify(dao, times(1)).executeUpdate(any(), any());
  }

  @Test
  void givenDeleteAction_whenPriority_thenShouldReturnPriority() {
    int priority = new DeleteAction(dao, entity).priority();

    assertThat(priority).isEqualTo(3);
  }

  @Test
  void givenInsertAction_whenPriority_thenShouldReturnPriority() {
    int priority = new InsertAction(dao, entity).priority();

    assertThat(priority).isEqualTo(1);
  }

  @Test
  void givenUpdateAction_whenPriority_thenShouldReturnPriority() {
    int priority = new UpdateAction<>(dao, null, null).priority();

    assertThat(priority).isEqualTo(2);
  }
}