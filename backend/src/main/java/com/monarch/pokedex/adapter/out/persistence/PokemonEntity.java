package com.monarch.pokedex.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "pokemon", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class PokemonEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String name;
    private String category;
    private Integer height;
    private Integer weight;
    @Column(length = 500)
    private String sprite;
    @Column(length = 1000)
    private String abilities;
    private String region;
    private String classification;
    @Column(length = 1000)
    private String notes;

    protected PokemonEntity() {}

    public PokemonEntity(String name, String category, Integer height, Integer weight, String sprite,
                         String abilities, String region, String classification, String notes) {
        this.name = name; this.category = category; this.height = height; this.weight = weight;
        this.sprite = sprite; this.abilities = abilities; this.region = region;
        this.classification = classification; this.notes = notes;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Integer getHeight() { return height; }
    public Integer getWeight() { return weight; }
    public String getSprite() { return sprite; }
    public String getAbilities() { return abilities; }
    public String getRegion() { return region; }
    public String getClassification() { return classification; }
    public String getNotes() { return notes; }
    public void setCategory(String value) { category = value; }
    public void setHeight(Integer value) { height = value; }
    public void setWeight(Integer value) { weight = value; }
    public void setSprite(String value) { sprite = value; }
    public void setAbilities(String value) { abilities = value; }
    public void setRegion(String value) { region = value; }
    public void setClassification(String value) { classification = value; }
    public void setNotes(String value) { notes = value; }
}
