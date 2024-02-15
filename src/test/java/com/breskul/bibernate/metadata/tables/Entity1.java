package com.breskul.bibernate.metadata.tables;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ent_one")
public class Entity1 {
  @Id
  private Long id;

  private long basicClassWithoutColumn;

  private Long fieldWithoutColumn;

  @Column
  private boolean basicClassWithColumn;

  @Column(name = "name_from_col", unique = true, nullable = false, length = 20, precision = 10, scale = 5, columnDefinition = "boolean")
  private boolean columnWithOverrideProperties;

  @OneToOne
  private Entity2 entity2;
}
