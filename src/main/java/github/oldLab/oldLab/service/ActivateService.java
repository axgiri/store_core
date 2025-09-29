package github.oldLab.oldLab.service;

import java.util.Optional;

import github.oldLab.oldLab.dto.request.ActivateRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;

public interface ActivateService {
    
    public void setActive(ActivateRequest request);

    public int setOtp();

    public void saveForRegister(String email);

    public void save(String email, Optional<Boolean> isLogin);

    public void saveForLogin(String email);

    public int getOtp(String email);

    public void sendOtp(String email);

    public void sendOtpReset(String email);

    public void resendOtp(String email);

    public AuthResponse login(String email, int OTP);
    public void delete(String email);

    public void sendLoginOtp(String email);

    // Methods for reset password
    public void saveOtpReset(String email, int otp, boolean isForLogin);

    public void validateOtpReset(String email, int otp);
    // End
    public void cleanupOldRecords();
}