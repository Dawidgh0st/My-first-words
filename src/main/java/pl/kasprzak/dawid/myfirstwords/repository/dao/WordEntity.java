package pl.kasprzak.dawid.myfirstwords.repository.dao;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDate;

@Entity
@Data
@Table(name = "words")
public class WordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String word;
    private LocalDate dateAchieve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private ChildEntity child;

}
