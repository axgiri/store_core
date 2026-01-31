// package github.oldLab.oldLab;

// import github.oldLab.oldLab.controller.PersonController;
// import github.oldLab.oldLab.dto.request.PersonRequest;
// import github.oldLab.oldLab.dto.response.PersonResponse;
// import github.oldLab.oldLab.service.PersonService;
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