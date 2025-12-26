package com.auth.auth_app.controller;

import com.auth.auth_app.dtos.LoginRequest;
import com.auth.auth_app.dtos.RefreshTokenRequest;
import com.auth.auth_app.dtos.UserDto;
import com.auth.auth_app.dtos.TokenResponse;
import com.auth.auth_app.models.RefreshToken;
import com.auth.auth_app.models.User;
import com.auth.auth_app.repository.RefreshTokenRepository;
import com.auth.auth_app.repository.UserRepository;
import com.auth.auth_app.security.CookieService;
import com.auth.auth_app.security.JwtService;
import com.auth.auth_app.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authenticate = authenticate(loginRequest);
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid UserName and password"));
        if (!user.isEnable()) {
            throw new DisabledException("User is disable");
        }

        String jti = UUID.randomUUID().toString();
        var refreshToken = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .revoked(false)
                .expireAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSecond()))
                .build();

        refreshTokenRepository.save(refreshToken);


        String accessToken = jwtService.generateAccessToken(user);
        String refreshedToken = jwtService.refreshToken(user, refreshToken.getJti() );


        cookieService.attachRefreshCookie(response,refreshedToken,(int)jwtService.getRefreshTtlSecond());
        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshedToken, jwtService.getAccessTtlSecond(), modelMapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid UserName & Password");
        }
    }

//    access token and refresh token renew

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            HttpServletResponse response,
            HttpServletRequest request
    ){
        log.info("Start the postmapping contorller");

        String refreshToken = readRefreshTokenFromRequest(null, request).orElseThrow(() -> new BadCredentialsException("Refresh Token is Missing"));
        log.info("{}",refreshToken);
        if (!jwtService.isRefreshToken(refreshToken))
        {
            throw new BadCredentialsException("Invalid Refresh Token Type");
        }

        String jwtId = jwtService.getJwtId(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);
        RefreshToken storedRefreshToken  = refreshTokenRepository.findByJti(jwtId).orElseThrow(() -> new BadCredentialsException("Refresh Token is not recognized"));

        log.info("jwt id is : {}\n user uuid is {}\n sotred token {}",jwtId,userId,storedRefreshToken);
        if(storedRefreshToken.isRevoked())
        {
            throw new BadCredentialsException("Refresh Token is Revoked");
        }

        if(storedRefreshToken.getExpireAt().isBefore(Instant.now()))
        {
            throw new BadCredentialsException("Refresh token is expired");
        }

        if (!storedRefreshToken.getUser().getId().equals(userId))
        {
            throw new BadCredentialsException("Refresh token does not belong to this user");
        }

        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);

        User user = storedRefreshToken.getUser();
        log.info("stored token user {}",user);
        var newRefreshtoken = RefreshToken.builder()
                .jti(newJti)
                .user(user)
                .createdAt(Instant.now())
                .expireAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSecond()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshtoken);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.refreshToken(user, newJti);
        cookieService.attachRefreshCookie(response,newRefreshToken,(int) jwtService.getRefreshTtlSecond());
        cookieService.addNoStoreHeaders(response);
        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTtlSecond(), modelMapper.map(user,UserDto.class)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logOut(HttpServletRequest request, HttpServletResponse response)
    {
        readRefreshTokenFromRequest(null,request).ifPresent(token->
        {
            try{
                if (jwtService.isRefreshToken(token))
                {
                    String jwtId = jwtService.getJwtId(token);
                    refreshTokenRepository.findByJti(jwtId).ifPresent(rt->{
                       rt.setRevoked(true);
                       refreshTokenRepository.save(rt);
                    });
                }
            }catch (Exception ignored){

            }
        });

        cookieService.clearResponseCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    private Optional<String> readRefreshTokenFromRequest(
            RefreshTokenRequest body,
            HttpServletRequest request)
    {
        //reading refresh token from cookie
        log.info("{}", (Object) request.getCookies());
        if(request.getCookies()!=null)
        {
            Optional<String> fromCookie = Arrays.stream(
                            request.getCookies()
                    ).filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst();
            log.info("get cookies {}",fromCookie);
            if (fromCookie.isPresent())
            {
                return fromCookie;
            }
        }
        //reading refresh token from body
        if(body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank())
        {
            return Optional.of(body.refreshToken());
        }

        //reading refresh token from header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if(refreshHeader != null && !refreshHeader.isBlank())
        {
            return Optional.of(refreshHeader.trim());
        }

        //reading refresh token from authorization : bearer <Token>

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader !=null && authHeader.regionMatches(true,0,"Bearer ", 0,7))
        {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isBlank())
            {
                try
                {
                    if (jwtService.isRefreshToken(candidate))
                    {
                        return Optional.of(candidate);

                    }
                }catch (Exception ignored){

                }
            }
        }

        return Optional.empty();
    }
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        UserDto userDto1 = authService.registerUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto1);
    }
}
