package github.oldLab.oldLab.dto.response;

import java.util.List;

import github.oldLab.oldLab.Enum.CategoryEnum;
import github.oldLab.oldLab.entity.Shop;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ShopResponse {
    private Long id;
    private Long version;
    private String name;
    private String address;
    private String phoneNumber;
    private String description;
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
