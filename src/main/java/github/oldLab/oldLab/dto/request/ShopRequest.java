package github.oldLab.oldLab.dto.request;

import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Shop;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@DynamicUpdate
public class ShopRequest {
    
    @NotNull(message = "name cannot be null")
    private String name;

    @NotNull(message = "address cannot be null")
    private String address;

    @NotNull(message = "phone number cannot be null")
    private String phoneNumber;

    @NotNull(message = "photo header cannot be null")
    private String photoHeader;

    @NotNull(message = "description cannot be null")
    private String description;

    @NotNull(message = "category cannot be null")
    private List<CategoryEnum> category;

    public Shop toEntity() {
        return new Shop()
            .setName(name)
            .setAddress(address)
            .setPhoneNumber(phoneNumber)
            .setPhotoHeader(photoHeader)
            .setDescription(description)
            .setCategory(category);
    }
}
