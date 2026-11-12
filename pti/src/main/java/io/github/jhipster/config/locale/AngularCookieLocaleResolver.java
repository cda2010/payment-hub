package io.github.jhipster.config.locale;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

/**
 * Le cookie AngularJS {@code NG_TRANSLATE_LANG_KEY} vaut {@code "fr"}, guillemets compris.
 * Le résolveur Boot 3 rejette ces caractères et interrompt la requête.
 */
public class AngularCookieLocaleResolver extends CookieLocaleResolver {

    @Override
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        return new SimpleLocaleContext(resolveLocale(request));
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (!"NG_TRANSLATE_LANG_KEY".equals(cookie.getName()) || cookie.getValue() == null) {
                    continue;
                }
                String language = cookie.getValue().replace("\"", "").trim();
                if (language.matches("[A-Za-z]{2}([_-][A-Za-z]{2})?")) {
                    return Locale.forLanguageTag(language.replace('_', '-'));
                }
            }
        }
        return Locale.FRENCH;
    }
}
