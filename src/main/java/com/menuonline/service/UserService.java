package com.menuonline.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.menuonline.entity.TokenAccess;
import com.menuonline.entity.UserEntity;
import com.menuonline.exceptions.ErrorHandlerResponse.ErrorMessages;
import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.CreateUserRequest;
import com.menuonline.payloads.LoginUserRequest;
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
        TokenAccess token = new TokenAccess();
        token.setUser(user);
        String uuidToken = UUID.randomUUID().toString();
        token.setToken(uuidToken);
        LocalDateTime expiration = LocalDateTime.now().plusHours(TokenAccess.TOKEN_DURATION_HOURS);
        token.setExpirationDate(expiration);
        return tokenAccessRepository.save(token);
    }

}
