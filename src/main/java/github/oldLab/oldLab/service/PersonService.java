package github.oldLab.oldLab.service;

import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.dto.request.ContactRequest;
import github.oldLab.oldLab.dto.request.LoginRequest;
import github.oldLab.oldLab.dto.request.PersonRequest;
import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;

public interface PersonService {

    public void createAsync(PersonRequest personRequest);

    public AuthResponse authenticate(LoginRequest request);

    public PersonResponse findById(Long id);

    public PersonResponse findByPhoneNumber(String phoneNumber);

    public PersonResponse update(Long id, PersonRequest personRequest);

    public void delete(Long id);

    public void validateToken(String token);

    public CompletableFuture<Void> updatePasswordAsync(LoginRequest loginRequest, String oldPassword);

    // Reset Password Section
    public void requestPasswordReset(ContactRequest contactRequest);

    public void resetPassword(ResetPasswordRequest request);
    // End of Section

    // Centralized OAuth upsert to avoid duplication
    Person upsertFromOAuth(String email, String firstName, String lastName);
}
