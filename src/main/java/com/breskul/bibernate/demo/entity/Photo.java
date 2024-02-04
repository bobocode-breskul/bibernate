package com.breskul.bibernate.demo.entity;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "photo")
@NoArgsConstructor
public class Photo {

  @Id
  private Long id;

  @Column(name = "url")
  private String url;

  @Column(name = "description")
  private String description;


  @Setter(AccessLevel.PRIVATE)
  @OneToMany
  private List<PhotoComment> comments = new ArrayList<>();

  public void addComment(PhotoComment comment) {
    this.comments.add(comment);
    comment.setPhoto(this);
  }

  public void removeComment(PhotoComment comment) {
    this.comments.remove(comment);
    comment.setPhoto(null);
  }
}
