package github.oldLab.oldLab.serviceImpl;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.repository.ProductRepository;
import github.oldLab.oldLab.dto.response.ReviewResponse;
import github.oldLab.oldLab.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("accessControlService")
@RequiredArgsConstructor
@Slf4j
public class AccessControlService {

	private final PersonRepository personRepository;
	private final ProductRepository productRepository;
	private final NotificationReportsServiceImpl notificationReportsServiceImpl;

	public boolean isAdmin(Authentication authentication) {
		return hasRole(authentication, RoleEnum.ADMIN);
	}

	public boolean isModerator(Authentication authentication) {
		return hasRole(authentication, RoleEnum.MODERATOR);
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

	public boolean isProductOwnerByProduct(Authentication authentication, Long productId) {
		if (authentication == null || productId == null) {
			throw new IllegalArgumentException("authentication or productId is null");
		}

		Person current = getPersonByEmail(authentication.getName());

		if (current == null) {
			return isAdmin(authentication) || isModerator(authentication);
		}

		Long personId = getPersonByProductId(productId);
		boolean same = personId != null && personId.equals(current.getId());
		return same || isAdmin(authentication) || isModerator(authentication);
	}

	public boolean isReviewOwner(Authentication authentication, Long reviewId) {
		if (authentication == null || reviewId == null) {
			throw new IllegalArgumentException("authentication or reviewId is null");
		}

		Person current = getPersonByEmail(authentication.getName());
		if (current == null){
			throw new IllegalArgumentException("current user not found");
		}

		ReviewResponse review = notificationReportsServiceImpl.getReviewById(reviewId);
		boolean owner = review != null && review.getAuthorId() != null &&
			review.getAuthorId().equals(current.getId());
		return owner || isAdmin(authentication) || isModerator(authentication);
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

	@Cacheable(value = "personId", key = "#productId", unless = "#result == null")
	@Transactional(readOnly = true)
	public Long getPersonByProductId(Long productId) {
		log.debug("loading product person id (cached): {}", productId);
		return productRepository.findById(productId).map(p -> {
			return p.getPerson() != null ? p.getPerson().getId() : null;
		}).orElse(null);
	}
}