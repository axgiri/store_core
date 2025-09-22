package github.oldLab.oldLab.entity;

import java.util.List;

import github.oldLab.oldLab.Enum.CategoryEnum;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Version;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "shops")
public class Shop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull(message = "name cannot be null")
    @Column(name = "name")
    private String name;

    private String address;

    @Column(name = "phone_number", unique = true, nullable = true)
    private String phoneNumber;

    @Column(name = "email", unique = true, nullable = false)
    @NotNull(message = "email cannot be null")
    private String email;

    private String photoHeader;

    private String description;

    @ElementCollection
    @CollectionTable(name = "shop_categories", joinColumns = @JoinColumn(name = "shop_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private List<CategoryEnum> category;

    @NotNull(message = "owner cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Person ownerId;
}
