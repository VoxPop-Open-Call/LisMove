package net.nextome.lismove.models;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @SequenceGenerator(name = "articlesseq", sequenceName = "articles_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "articlesseq")
    private Long id;
    private String title;
    private String description;
    private BigDecimal points;
    @Column(columnDefinition = "varchar")
    private String image;
    private LocalDate expirationDate;
    private Integer numberArticles;

    @ManyToOne
    private Shop shop;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Integer getNumberArticles() {
        return numberArticles;
    }

    public void setNumberArticles(Integer numberArticles) {
        this.numberArticles = numberArticles;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }
}
