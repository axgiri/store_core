package github.oldLab.oldLab.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Shop;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShopResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("version")
    private Long version;

    @JsonProperty("name")
    private String name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private List<CategoryEnum> category;

    public static ShopResponse fromEntityToDto(Shop shop) {
        return new ShopResponse()
            .setId(shop.getId())
            .setVersion(shop.getVersion())
            .setName(shop.getName())
            .setAddress(shop.getAddress())
            .setPhoneNumber(shop.getPhoneNumber())
            .setDescription(shop.getDescription())
            .setCategory(shop.getCategory());
    }
}
