package github.oldLab.oldLab.service;

import github.oldLab.oldLab.Enum.MessageChannelEnum;

public interface MessageSenderService {
    public void sendOtp(MessageChannelEnum channel, String destination, int otp);
    public void sendNotification(MessageChannelEnum channel, String destination, String message, boolean isHtml);

}
