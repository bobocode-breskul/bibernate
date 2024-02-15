package com.breskul.bibernate.metadata.tables;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "ent_four")
public class Entity4 {
  @Id
  private Integer id;

  @OneToOne
  private Entity2 entity2;
}
