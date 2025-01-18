package com.example.chatAppServer.service;

import com.example.chatAppServer.token.TokenHelper;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
public class PushNotificationService {
    @Value("${vapid.public.key}")
    private String publicKey;
    @Value("${vapid.private.key}")
    private String privateKey;

    private PushService pushService;
    Map<Long, Subscription> subscriptions = new HashMap<>();

    @PostConstruct
    private void init() throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService(publicKey, privateKey);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void subscribe(String accessToken, Subscription subscription) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        System.out.println("Subscribed to " + subscription.endpoint);
        this.subscriptions.put(userId, subscription);
    }

    public void unsubscribe(String accessToken, String endpoint) {
        Long userId = TokenHelper.getUserIdFromToken(accessToken);
        System.out.println("Unsubscribed from " + endpoint);
        subscriptions.remove(userId);
    }

    public void sendNotification(Long userId, String messageJson) {
        try {
            Subscription subscription = this.subscriptions.get(userId);
            if (!Objects.isNull(subscription)) {
                pushService.send(new Notification(subscription, messageJson));
            }
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException
                 | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
