package com.breskul.bibernate.data;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.DynamicUpdate;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@DynamicUpdate
@Getter
@Setter
@ToString
@Entity
@Table(name = "photo_comment")
@NoArgsConstructor
public class PhotoComment {

  @Id
  private Long id;

  @Column(name = "text")
  private String text;

  @Column(name = "photo_id")
  private int photo;
}
