package com.groupc.shared.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * {@code AuditLoggerAspect} är en AspectJ-aspekt som tillhandahåller auditloggning
 * för datamuterande operationer inom tjänstlager (service layer) i applikationen.
 * Denna aspekt fångar upp anrop till metoder i klasser som slutar på "Service"
 * och vars namn börjar med "create", "update" eller "delete".
 *
 * <p>När en sådan metod har exekverats framgångsrikt, loggas en audit-händelse
 * som indikerar vilken användare som utförde operationen och vilken typ av entitet
 * som påverkades. Användarnamnet hämtas från Spring Security-kontexten.
 * Om ingen användare är autentiserad, loggas "system" som användare.
 *
 * <p>Denna aspekt är konfigurerad som en Spring-komponent och aktiveras automatiskt
 * när AspectJ-weaving är konfigurerat i Spring-applikationen.
 *
 * <p><b>Funktionalitet:</b>
 * <ul>
 *     <li><b>Pointcut:</b> {@code mutatingServiceMethods()} definierar vilka metoder
 *         som ska avlyssnas. Den matchar metoder som börjar med {@code create}, {@code update}
 *         eller {@code delete} i klasser vars namn slutar på {@code Service} inom paketet
 *         {@code com.groupc} och dess subpaket.</li>
 *     <li><b>Advice:</b> {@code logAfterMutation()} är en {@code @AfterReturning}-rådgivning
 *         som exekveras efter att en matchande metod har returnerat utan att kasta ett undantag.
 *         Den extraherar metodnamnet, härleder entitetstypen och hämtar det aktuella användarnamnet
 *         för att sedan logga händelsen.</li>
 *     <li><b>Användaridentifiering:</b> Använder {@link SecurityContextHolder} för att få
 *         tag på den autentiserade användarens namn.</li>
 * </ul>
 *
 * <p><b>Exempel på loggmeddelanden:</b>
 * <pre>{@code
 * INFO  [main] c.g.s.l.AuditLoggerAspect - username created entityname
 * INFO  [main] c.g.s.l.AuditLoggerAspect - username updated entityname
 * INFO  [main] c.g.s.l.AuditLoggerAspect - username deleted entityname
 * }</pre>
 */
@Aspect
@Component
public class AuditLoggerAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggerAspect.class);

    @Pointcut(  "execution(* com..*Service.create*(..)) || " +
                "execution(* com..*Service.update*(..)) || " +
                "execution(* com..*Service.delete*(..)) || " +
                "execution(* edu..*Service.create*(..)) || " +
                "execution(* edu..*Service.update*(..)) || " +
                "execution(* edu..*Service.delete*(..))")
    public void mutatingServiceMethods() {
    }

    @AfterReturning("mutatingServiceMethods()")
    public void logAfterMutation(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String entityType = extractEntityName(methodName);
        String username = getCurrentUsername();

        if (methodName.startsWith("create")) {
            log.info("{} created {}", username, entityType);
        } else if (methodName.startsWith("update")) {
            log.info("{} updated {}", username, entityType);
        } else if (methodName.startsWith("delete")) {
            log.info("{} deleted {}", username, entityType);
        }
    }

    private String extractEntityName(String methodName) {
        if (methodName.startsWith("create")) return methodName.substring(6).toLowerCase();
        if (methodName.startsWith("update")) return methodName.substring(6).toLowerCase();
        if (methodName.startsWith("delete")) return methodName.substring(6).toLowerCase();
        return methodName.toLowerCase();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getTokenAttributes().getOrDefault("preferred_username", auth.getName()).toString();
        }
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
