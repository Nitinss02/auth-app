package com.auth.auth_app.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieSecure;
    private final boolean httpOnly;
    private final String cookieDomain;
    private final String sameSameSite;

    public CookieService(
            @Value("${security.jwt.cookie.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie.secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie.http-only}") boolean httpOnly,
            @Value("${security.jwt.cookie.domain}") String cookieDomain,
            @Value("${security.jwt.cookie.same-site}") String sameSameSite) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieSecure = cookieSecure;
        this.httpOnly = httpOnly;
        this.cookieDomain = cookieDomain;
        this.sameSameSite = sameSameSite;
    }

    public void attachRefreshCookie(HttpServletResponse response,String value, int maxAge)
    {
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
                .httpOnly(httpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSameSite);

        if (cookieDomain!=null && !cookieDomain.isEmpty())
        {
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookies = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookies.toString());
    }

    public  void clearResponseCookie(HttpServletResponse response)
    {
        ResponseCookie.ResponseCookieBuilder responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, "")
                .path("/")
                .maxAge(0)
                .sameSite(sameSameSite)
                .httpOnly(httpOnly)
                .secure(cookieSecure);

        if (cookieDomain!=null && !cookieDomain.isEmpty())
        {
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookies = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookies.toString());
    }

    public void addNoStoreHeaders(HttpServletResponse response){
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("pragma","no-cache");
    }
}
