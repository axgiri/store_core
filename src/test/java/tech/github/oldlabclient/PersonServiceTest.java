package tech.github.oldlabclient;

// import github.oldLab.oldLab.dto.request.PersonRequest;
// import github.oldLab.oldLab.entity.Person;
// import github.oldLab.oldLab.exception.UserNotFoundException;
// import github.oldLab.oldLab.repository.PersonRepository;
// import github.oldLab.oldLab.service.ActivateService;
// import github.oldLab.oldLab.service.PersonService;
// import github.oldLab.oldLab.service.TokenService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.core.task.TaskExecutor;

// import java.util.Optional;


// class PersonServiceTest {
//     @Mock PersonRepository repository;
//     @Mock PasswordEncoder passwordEncoder;
//     @Mock AuthenticationManager authenticationManager;
//     @Mock TokenService tokenService;
//     @Mock ActivateService activateService;
//     @Mock TaskExecutor taskExecutor;

//     @InjectMocks PersonService personService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void create_throwsIfPhoneExists() {
//         PersonRequest req = new PersonRequest();
//         req.setPhoneNumber("+61123456789");
//         when(repository.findByPhoneNumber(anyString())).thenReturn(Optional.of(new Person()));
//         assertThrows(UserNotFoundException.class, () -> personService.create(req));
//     }
// } 