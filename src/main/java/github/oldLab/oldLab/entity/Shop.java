package github.oldLab.oldLab.entity;

import java.util.List;

import github.oldLab.oldLab.Enum.CategoryEnum;
import jakarta.persistence.*;
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

    @NotNull(message = "phone number cannot be null")
    @Column(name = "phone_number")
    private String phoneNumber;

    private String photoHeader;

    private String description;

    private List<CategoryEnum> category;
}
