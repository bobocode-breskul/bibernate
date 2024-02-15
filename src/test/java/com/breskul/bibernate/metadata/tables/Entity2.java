package com.breskul.bibernate.metadata.tables;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToOne;
import com.breskul.bibernate.annotation.Table;
import java.math.BigInteger;
import lombok.Data;

@Data
@Entity
@Table(name = "ent_two")
public class Entity2 {
  @Id
  private BigInteger id;

  @OneToOne(mappedBy = "entity2")
  private Entity1 entity1;

  @OneToOne
  private Entity4 entity4;

  @ManyToOne
  private Entity3 entity3;


}
