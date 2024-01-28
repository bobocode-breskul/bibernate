package com.breskul.bibernate.demo.entity;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * todo:
 * - implement equals and hashCode based on identifier field
 *
 * - configure JPA entity
 * - specify table name: "photo_comment"
 * - configure auto generated identifier
 * - configure not nullable column: text
 *
 * - map relation between Photo and PhotoComment using foreign_key column: "photo_id"
 * - configure relation as mandatory (not optional)
 */
@Getter
@Setter()
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
