package github.oldLab.oldLab.service;

import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.dto.request.LoginRequest;
import github.oldLab.oldLab.dto.request.PersonRequest;
import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.exception.NotImplementedException;

public interface PersonService {

    public void createAsync(PersonRequest personRequest);

    public AuthResponse authenticate(LoginRequest request);

    public PersonResponse findById(Long id);

    public PersonResponse findByPhoneNumber(String phoneNumber);

    public PersonResponse update(Long id, PersonRequest personRequest);

    public void delete(Long id);

    public void validateToken(String token);

    public CompletableFuture<Void> updatePasswordAsync(LoginRequest loginRequest, String oldPassword);

    public void sendOtp(String email) throws NotImplementedException;

    // Reset Password Section
    public void requestPasswordReset(String email);

    public void resetPassword(ResetPasswordRequest request);
    // End of Section
}
