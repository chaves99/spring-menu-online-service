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
import com.menuonline.payloads.UpdatePasswordRequest;
import com.menuonline.repository.TokenAccessRepository;
import com.menuonline.repository.UserRepository;
import com.menuonline.utils.CryptoUtil;
import com.menuonline.utils.TokenAccessUtil;

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

    public void resetPassword(UserEntity user, UpdatePasswordRequest request) {
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

    public Optional<TokenAccess> generateByEmail(String email) {
        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isEmpty())
            return Optional.empty();

        return byEmail.map(user -> {
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(TokenAccess.TOKEN_DURATION_RECOVERY_PASSWORD_MIN);
            return tokenAccessRepository.save(TokenAccess.create(user, expiration));
        });
    }

}
