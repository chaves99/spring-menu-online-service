package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.menuonline.entity.TokenAccess;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.CreateUserRequest;
import com.menuonline.payloads.LoginUserRequest;
import com.menuonline.payloads.ResetPasswordRequest;
import com.menuonline.payloads.UpdatePasswordRequest;
import com.menuonline.repository.TokenAccessRepository;
import com.menuonline.repository.UserRepository;
import com.menuonline.utils.CryptoUtil;
import com.menuonline.utils.TokenAccessUtil;
import com.menuonline.utils.TokenGeneratorUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TokenAccessRepository tokenAccessRepository;

    public UserEntity create(CreateUserRequest request) {
        validate(request);
        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(CryptoUtil.encrypt(request.password()));
        user.setEstablishmentName(request.establishmentName());
        UserEntity save = userRepository.save(user);

        return save;
    }

    public void updatePassword(UserEntity user, UpdatePasswordRequest request) {
        if (!CryptoUtil.validate(request.currentPassword(), user.getPassword())) {
            log.warn("updatePassword - user:{} current password not valid", user.getId());
            throw new HttpServiceException(
                    ErrorMessages.PASSWORD_INVALID, HttpStatus.UNAUTHORIZED);
        }
        String encryptedPassword = CryptoUtil.encrypt(request.newPassword());
        user.setPassword(encryptedPassword);
        userRepository.save(user);
    }

    private void validate(CreateUserRequest request) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(request.email());
        if (byEmail.isPresent()) {
            throw new HttpServiceException(ErrorMessages.EMAIL_EXISTS, HttpStatus.CONFLICT);
        }
        Optional<UserEntity> byEstablishmentName = userRepository.findByEstablishmentName(request.establishmentName());
        if (byEstablishmentName.isPresent()) {
            throw new HttpServiceException(ErrorMessages.ESTABLISHMENT_EXISTS, HttpStatus.CONFLICT);
        }
    }

    public TokenAccess login(LoginUserRequest request) {
        UserEntity user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new HttpServiceException(null, HttpStatus.UNAUTHORIZED));

        if (!CryptoUtil.validate(request.password(), user.getPassword())) {
            throw new HttpServiceException(null, HttpStatus.UNAUTHORIZED);
        }

        log.info("login - user tokens: {}", user.getTokenAccesses().size());

        Optional<TokenAccess> anyToken = user.getTokenAccesses().stream()
                .filter(TokenAccessUtil::validate).findAny();

        List<TokenAccess> invalid = user.getTokenAccesses().stream()
                .filter(t -> !TokenAccessUtil.validate(t)).toList();
        tokenAccessRepository.deleteAll(invalid);

        if (anyToken.isEmpty()) {
            return login(user);
        }

        TokenAccess tokenAccess = anyToken.get();
        tokenAccess.setExpirationDate(LocalDateTime.now().plusHours(TokenAccess.TOKEN_DURATION_HOURS));
        return tokenAccessRepository.save(tokenAccess);
    }

    public Optional<TokenAccess> get(String token) {
        log.debug("get - token:{}", token);
        return tokenAccessRepository.findById(token);
    }

    public TokenAccess login(UserEntity user) {
        LocalDateTime expiration = LocalDateTime.now().plusHours(TokenAccess.TOKEN_DURATION_HOURS);
        return tokenAccessRepository.save(TokenAccess.create(user, expiration));
    }

    public Optional<TokenAccess> generateTokenAccessByEmail(String email) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isEmpty())
            return Optional.empty();

        return byEmail.map(user -> {
            LocalDateTime expiration = LocalDateTime.now()
                    .plusMinutes(TokenAccess.TOKEN_DURATION_RECOVERY_PASSWORD_MIN);
            return tokenAccessRepository.save(TokenAccess.create(user, expiration));
        });
    }

    public String generateRecoveryToken(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpServiceException(null, HttpStatus.UNAUTHORIZED));

        if (!UserEntity.canGenerateRecoveryToken(user)) {
            throw new HttpServiceException(ErrorMessages.UNAUTHORIZED_TO_GENERATE_RECOVERY_CODE,
                    HttpStatus.UNAUTHORIZED);
        }

        String token = TokenGeneratorUtil.generate(TokenGeneratorUtil.DEFAULT_PASSWORD_RECOVERY_TOKEN_SIZE);
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenCreation(LocalDateTime.now());
        userRepository.save(user);
        log.info("generateToken - email:{}", token);

        return token;
    }

    public boolean validateRecoveryToken(String email, String token) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpServiceException(null, HttpStatus.UNAUTHORIZED));
        boolean canUpdatePassword = UserEntity.canUpdatePassword(user, token);
        log.info("validateRecoveryToken - email:{} canUpdatePassword:{}", email, canUpdatePassword);
        return canUpdatePassword;
    }

    public void resetPassword(ResetPasswordRequest request) {
        log.info("resetPassword - email:{}", request.email());
        UserEntity user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new HttpServiceException(null, HttpStatus.UNAUTHORIZED));
        if (!UserEntity.canUpdatePassword(user, request.token())) {
            throw new HttpServiceException(null, HttpStatus.UNAUTHORIZED);
        }

        user.setPassword(CryptoUtil.encrypt(request.newPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenCreation(null);
        userRepository.save(user);
    }

}
