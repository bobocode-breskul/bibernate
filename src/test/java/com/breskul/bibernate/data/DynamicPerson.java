package com.breskul.bibernate.data;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.DynamicUpdate;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@DynamicUpdate
@Entity
@Table(name = "persons")
public class DynamicPerson {

  @Id
  private Long id;

  @Column(name = "first_name")
  private String firstName;
  @Column(name = "last_name")
  private String lastName;

  public DynamicPerson() {

  }

  public DynamicPerson(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public DynamicPerson(Long id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
