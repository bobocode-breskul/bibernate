package com.breskul.bibernate.metadata.tables;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.Table;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "ent_tree")
public class Entity3 {
  @Id
  private Integer id;



  @OneToMany
  List<Entity2> entity2List;
}
