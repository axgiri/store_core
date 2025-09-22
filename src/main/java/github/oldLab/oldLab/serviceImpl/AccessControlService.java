package github.oldLab.oldLab.serviceImpl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
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
		if (hasRole(authentication, RoleEnum.ADMIN)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isModerator(Authentication authentication) {
		if( hasRole(authentication, RoleEnum.MODERATOR)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean hasRole(Authentication authentication, RoleEnum role) {
		if (authentication == null) {
			throw new IllegalArgumentException("authentication is null");
		}
		
		final String required = role.name();
		return authentication.getAuthorities().stream()
			.map(a -> a.getAuthority())
			.map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
			.anyMatch(auth -> auth.equals(required));
	}

	public boolean isSelf(Authentication authentication, Long personId) {
		
		if (authentication == null || personId == null) {
			throw new IllegalArgumentException("authentication or personId is null");
		}

		Person current = getPersonByEmail(authentication.getName());
		boolean self = current.getId().equals(personId);
		return self || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isSelfByEmail(Authentication authentication, String email) {
		if (authentication == null || email == null) {
			throw new IllegalArgumentException("authentication or email is null");
		}

		boolean self = authentication.getName().equals(email);
		return self || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isCompanyWorker(Authentication authentication, Long companyId) {
		if (authentication == null || companyId == null) {
			throw new IllegalArgumentException("authentication or companyId is null");
		}

		Person current = getPersonByEmail(authentication.getName());
		Long myCompany = current.getCompanyId();
		boolean match = myCompany != null && myCompany.equals(companyId);
		return match || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isCompanyWorkerByProduct(Authentication authentication, Long productId) {
		if (authentication == null || productId == null) {
			throw new IllegalArgumentException("authentication or productId is null");
		}

		Person current = getPersonByEmail(authentication.getName());
		Long myCompany = current.getCompanyId();
		
		if (myCompany == null) {
			return isAdmin(authentication) || isModerator(authentication);
		}

		Long shopId = getShopByProductId(productId);
		boolean same = shopId != null && shopId.equals(myCompany);
		return same || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean hasCompany(Authentication authentication) {
		if (authentication == null) {
			throw new IllegalArgumentException("authentication is null");
		}
		Person current = getPersonByEmail(authentication.getName());
		return current != null && current.getCompanyId() != null;
	}

	public boolean isReviewOwner(Authentication authentication, Long reviewId) {
		if (authentication == null || reviewId == null) {
			throw new IllegalArgumentException("authentication or reviewId is null");
		}

		Person current = getPersonByEmail(authentication.getName());
		if (current == null){
			throw new IllegalArgumentException("current user not found");
		}

		return reviewRepository.findById(reviewId)
			.map(r -> r.getAuthor() != null && r.getAuthor().getId().equals(current.getId()))
			.map(owner -> owner || isAdmin(authentication) || isModerator(authentication))
			.orElse(false);
	}

	@Cacheable(value = "personByEmail", key = "#email", unless = "#result == null")
	@Transactional(readOnly = true)
	public Person getPersonByEmail(String email) {
		log.debug("loading person by email (cached): {}", email);
		return personRepository.findByEmail(email).orElse(null);
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
			return p.getShop() != null ? p.getShop().getId() : null;
		}).orElse(null);
	}
}
