package github.oldLab.oldLab.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import github.oldLab.oldLab.dto.request.LoginRequest;
import github.oldLab.oldLab.dto.request.PersonRequest;
import github.oldLab.oldLab.dto.request.ResetPasswordRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;
import github.oldLab.oldLab.dto.response.PersonResponse;
import github.oldLab.oldLab.exception.NotImplementedException;

public interface PersonService {
    
    public PersonResponse create(PersonRequest personRequest);

    public void createAsync(PersonRequest personRequest);

    public AuthResponse authenticate(LoginRequest request);

    public PersonResponse findById(Long id);

    public PersonResponse findByPhoneNumber(String phoneNumber);

    public CompletableFuture<PersonResponse> update(Long id, PersonRequest personRequest);

    public void delete(Long id);

    public void validateToken(String token);

    public String getRole(String token);

    public void updatePasswordAsync(LoginRequest loginRequest, String oldPassword);

    public List<PersonResponse> getColleaguesAsync(String token, int page, int size);

    public void sendOtp(String email) throws NotImplementedException;

    // Reset Password Section
    public void requestPasswordReset(String phoneNumber);

    public void resetPassword(ResetPasswordRequest request);
    // End of Section
}
