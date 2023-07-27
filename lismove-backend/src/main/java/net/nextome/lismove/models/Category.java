package net.nextome.lismove.models;

import javax.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @SequenceGenerator(name = "categoriesseq", sequenceName = "categories_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "categoriesseq")
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
