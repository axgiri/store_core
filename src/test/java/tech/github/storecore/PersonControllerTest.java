package tech.github.storecore;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// class PersonControllerTest {
//     @Mock PersonService personService;
//     @InjectMocks PersonController controller;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void create_returnsCreated() {
//         PersonRequest req = new PersonRequest();
//         PersonResponse resp = new PersonResponse();
//         when(personService.create(any())).thenReturn(resp);
//         ResponseEntity<PersonResponse> result = controller.create(req);
//         assertEquals(HttpStatus.CREATED, result.getStatusCode());
//         assertEquals(resp, result.getBody());
//     }
// } 