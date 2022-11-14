package io.hochya.models;

import lombok.Data;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity(name = "Mythfits")
public class Mythfit {
    @Id
    @Column(name = "id", length = 40)
    private String mysfitId;
    @Column
    private String name;
    @Column
    private String species;
    @Column
    private Integer age;
    @Column
    private String description;
    @Column
    private String goodevil;
    @Column
    private String lawchaos;
    @Column
    private String thumbImageUri;
    @Column
    private String profileImageUri;
}
