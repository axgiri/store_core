package github.oldLab.oldLab.serviceImpl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Product;
import github.oldLab.oldLab.repository.ProductRepository;
import github.oldLab.oldLab.repository.ReviewRepository;
import github.oldLab.oldLab.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("accessControlService")
@RequiredArgsConstructor
@Slf4j
public class AccessControlService {

	private final PersonRepository personRepository;
	private final ProductRepository productRepository;
	private final ReviewRepository reviewRepository;

	public boolean isAdmin(Authentication authentication) {
		return hasRole(authentication, RoleEnum.ADMIN);
	}

	public boolean isModerator(Authentication authentication) {
		return hasRole(authentication, RoleEnum.MODERATOR);
	}

	private boolean hasRole(Authentication authentication, RoleEnum role) {
		if (authentication == null) return false;
		return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role.name()));
	}

	public boolean isSelf(Authentication authentication, Long personId) {
		
		if (authentication == null || personId == null) {
			return false;
		}
		
		Person current = getPersonByPhoneNumber(authentication.getName());
		boolean self = current.getId().equals(personId);
		return self || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isSelfByPhoneNumber(Authentication authentication, String phoneNumber) {
		if (authentication == null || phoneNumber == null) return false;
		boolean self = authentication.getName().equals(phoneNumber);
		return self || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isCompanyWorker(Authentication authentication, Long companyId) {
		if (authentication == null || companyId == null) return false;
		Person current = getPersonByPhoneNumber(authentication.getName());
		Long myCompany = current.getCompanyId();
		boolean match = myCompany != null && myCompany.equals(companyId);
		return match || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isCompanyWorkerByProduct(Authentication authentication, Long productId) {
		if (authentication == null || productId == null) return false;
		Person current = getPersonByPhoneNumber(authentication.getName());
		Long myCompany = current.getCompanyId();
		if (myCompany == null) return isAdmin(authentication) || isModerator(authentication);
		Long shopId = getShopByProductId(productId);
		boolean same = shopId != null && shopId.equals(myCompany);
		return same || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean hasCompany(Authentication authentication) {
		if (authentication == null) return false;
		Person current = getPersonByPhoneNumber(authentication.getName());
		return current != null && current.getCompanyId() != null;
	}

	public boolean isReviewOwner(Authentication authentication, Long reviewId) {
		if (authentication == null || reviewId == null) return false;
		Person current = getPersonByPhoneNumber(authentication.getName());
		if (current == null) return false;
		return reviewRepository.findById(reviewId)
			.map(r -> r.getAuthor() != null && r.getAuthor().getId().equals(current.getId()))
			.map(owner -> owner || isAdmin(authentication) || isModerator(authentication))
			.orElse(false);
	}

	@Cacheable(value = "personByPhoneNumber", key = "#phone", unless = "#result == null")
	@Transactional(readOnly = true)
	public Person getPersonByPhoneNumber(String phone) {
		log.debug("loading person by phone (cached): {}", phone);
		return personRepository.findByPhoneNumber(phone).orElse(null);
	}

	@Cacheable(value = "personById", key = "#id", unless = "#result == null")
	@Transactional(readOnly = true)
	public Person getPersonById(Long id) {
		log.debug("loading person by id (cached): {}", id);
		return personRepository.findById(id).orElse(null);
	}

	@Cacheable(value = "shopId", key = "#productId", unless = "#result == null")
	@Transactional(readOnly = true)
	public Long getShopByProductId(Long productId) {
		log.debug("loading product shop id (cached): {}", productId);
		return productRepository.findById(productId).map(p -> {
			Product prod = p;
			return prod.getShop() != null ? prod.getShop().getId() : null;
		}).orElse(null);
	}
}
