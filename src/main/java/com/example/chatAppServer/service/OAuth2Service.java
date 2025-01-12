package com.example.chatAppServer.service;

import com.example.chatAppServer.common.Common;
import com.example.chatAppServer.dto.user.ResponseToken;
import com.example.chatAppServer.entity.UserEntity;
import com.example.chatAppServer.repository.UserRepository;
import com.example.chatAppServer.token.TokenHelper;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
public class OAuth2Service {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String client_id;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String client_secret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirect_uri;
    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String authorization_grant_type;
    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authorization_uri;
    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String user_info_uri;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String token_uri;
    // URL để lấy JWKS của Google
    private static final String GOOGLE_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String EXPECTED_ISSUER = "https://accounts.google.com";
    private static final String EXPECTED_AUDIENCE = "805787417017-f8do34oaa5972u966uuaun468jg7ipik.apps.googleusercontent.com";

    @Autowired
    private final UserRepository userRepository;

    public OAuth2Service(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public String generateAuthUrl() {
        return authorization_uri +
                "?response_type=" + authorization_grant_type +
                "&client_id=" + client_id +
                "&redirect_uri=" + redirect_uri +
                "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8);
    }

    @Transactional
    public ResponseToken logIn(String code) throws ParseException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String authorization = client_id + ":" + client_secret;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
        httpHeaders.set("Authorization", basicAuth);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", redirect_uri);
        params.add("grant_type", "authorization_code");
        params.add("scope", "openid profile email");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);
        String url = token_uri;
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);
        Map<String, Object> body = responseEntity.getBody();

        SignedJWT signedJWT = SignedJWT.parse((String) body.get("id_token"));
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        if (Boolean.TRUE.equals(verifyIdToken(signedJWT))) {
            UserEntity userEntity = processGoogleUser(claims);
            return ResponseToken.builder()
                    .accessTokenOP((String) body.get("access_token"))
                    .accessTokenRP(TokenHelper.generateToken(userEntity))
                    .build();
        } else {
            throw new RuntimeException(Common.ACTION_FAIL);
        }
    }

    private UserEntity processGoogleUser(JWTClaimsSet claims) {
        String googleId = claims.getSubject();
        String fullName = (String) claims.getClaim("name");
        String imageUrl = (String) claims.getClaim("picture");
        String email = (String) claims.getClaim("email");

        UserEntity userEntity = userRepository.findByGoogleId(googleId);
        if (Objects.isNull(userEntity)) {
            userEntity = UserEntity.builder()
                    .googleId(googleId)
                    .fullName(fullName)
                    .imageUrl(imageUrl)
                    .email(email)
                    .backgroundUrl(imageUrl)
                    .build();
            userRepository.save(userEntity);
        }
        return userEntity;
    }


    public static boolean verifyIdToken(SignedJWT signedJWT) {
        try {
            // Lấy 'kid' từ header để tìm khóa phù hợp
            String keyId = signedJWT.getHeader().getKeyID();
            if (keyId == null || keyId.isEmpty()) {
                throw new RuntimeException("Không tìm thấy 'kid' trong header của token");
            }

            // Tải JWKS từ Google
            JWKSet jwkSet = JWKSet.load(new URL(GOOGLE_JWKS_URL));

            // Tìm JWK tương ứng với 'kid'
            JWK jwk = jwkSet.getKeyByKeyId(keyId);
            if (jwk == null) {
                throw new RuntimeException("Không tìm thấy khóa phù hợp với key ID: " + keyId);
            }

            // Chuyển đổi JWK thành RSAPublicKey
            if (!(jwk instanceof RSAKey)) {
                throw new RuntimeException("Khóa không phải loại RSA");
            }
            RSAPublicKey publicKey = ((RSAKey) jwk).toRSAPublicKey();

            // Tạo verifier với public key
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            // Xác minh chữ ký
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Chữ ký của token không hợp lệ");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!EXPECTED_ISSUER.equals(claims.getIssuer())) {
                throw new RuntimeException("Issuer không hợp lệ: " + claims.getIssuer());
            }

            if (claims.getAudience() == null || !claims.getAudience().contains(EXPECTED_AUDIENCE)) {
                throw new RuntimeException("Audience không hợp lệ: " + claims.getAudience());
            }

            Date expirationTime = claims.getExpirationTime();
            if (expirationTime == null || expirationTime.before(new Date())) {
                throw new RuntimeException("Token đã hết hạn vào: " + expirationTime);
            }

            Date issuedAt = claims.getIssueTime();
            if (issuedAt == null || issuedAt.after(new Date())) {
                throw new RuntimeException("Thời gian phát hành token không hợp lệ: " + issuedAt);
            }

            System.out.println("Token hợp lệ!");
            return true;

        } catch (ParseException e) {
            System.err.println("Lỗi khi phân tích token: " + e.getMessage());
            throw new RuntimeException("Lỗi khi phân tích token", e);
        } catch (IOException e) {
            System.err.println("Lỗi khi tải JWKS từ Google: " + e.getMessage());
            throw new RuntimeException("Lỗi khi tải JWKS", e);
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
            return false;
        }
    }
}
