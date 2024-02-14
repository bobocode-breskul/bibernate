package com.breskul.bibernate.data;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import lombok.Setter;
import lombok.ToString;

@Setter
@ToString
@Entity
@Table(name = "notes")
public class Note {

  @Id
  private Long id;

  @Column(name = "title")
  private String title;
  @Column(name = "body")
  private String body;

}

