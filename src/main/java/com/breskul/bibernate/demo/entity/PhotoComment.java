package com.breskul.bibernate.demo.entity;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.DynamicUpdate;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToOne;
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

//    @Column
//    private LocalDateTime createdOn;

  @ManyToOne
  @JoinColumn(name = "photo_id", nullable = false)
  private Photo photo;

//    public PhotoComment(String text, Photo photo) {
//        this.text = text;
//        this.photo = photo;
//    }
}
