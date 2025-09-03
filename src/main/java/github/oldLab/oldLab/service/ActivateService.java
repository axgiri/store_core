package github.oldLab.oldLab.service;

import java.util.Optional;

import github.oldLab.oldLab.dto.request.ActivateRequest;
import github.oldLab.oldLab.dto.response.AuthResponse;

public interface ActivateService {
    
    public void setActive(ActivateRequest request);

    public int setOtp();

    public void saveForRegister(String phoneNumber);

    public void save(String phoneNumber, Optional<Boolean> isLogin);

    public void saveForLogin(String phoneNumber);

    public int getOtp(String phoneNumber);

    public void sendOtp(String phoneNumber);


    public void resendOtp(String phoneNumber);

    public AuthResponse login(String phoneNumber, int OTP);
    public void delete(String phoneNumber);

    public void sendLoginOtp(String phoneNumber);

    // Methods for reset password
    public void saveOtpReset(String phoneNumber, int otp, boolean isForLogin);

    public void validateOtpReset(String phoneNumber, int otp);
    // End
    public void cleanupOldRecords();
}