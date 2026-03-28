# Wigell Shared Library

Detta bibliotek tillhandahåller gemensam infrastruktur för Wigell-mikrotjänster, inklusive säkerhetskonfiguration, loggning och felhantering. Biblioteket är byggt för Java 24 och Spring Boot 3.5.4.

## Innehåll
* **Säkerhet:** Förkonfigurerad Keycloak-integration och rollhantering.
* **Loggning:** Automatisk audit-loggning via AspectJ.
* **Klient:** Valutaklient med stöd för virtuella trådar (Project Loom).
* **Felhantering:** Global hantering av vanliga undantag.

---

## Integrationsguide

### 1. Installation
Lägg till biblioteket som en beroende i ditt projekts `pom.xml`:

```xml
<dependency>
    <groupId>com.groupc</groupId>
    <artifactId>wigell-shared-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

Biblioteket använder Spring Boots Auto-configuration för att automatiskt registrera nödvändiga beans.

### 2. Säkerhetskonfiguration
Biblioteket aktiverar automatiskt `SharedSecurityConfig` som sätter upp en stateless OAuth2 Resource Server. 
* **Roll-mappning:** Roller extraheras från JWT-claimet `realm_access.roles`.
* **Prefix:** Alla roller får automatiskt prefixet `ROLE_` och konverteras till versaler (t.ex. `admin` blir `ROLE_ADMIN`).
* **Endpoints:** Som standard är `/actuator/health` och `/actuator/info` öppna för alla.

### 3. Valutakonvertering
Använd `CurrencyClient` för att kommunicera med valutatjänsten. 
* **Konfiguration:** Ange URL till valutatjänsten i din `application.yml`:
  ```yaml
  wigell:
    currency:
      url: http://localhost:8080/currency
  ```
* Standardvärdet är annars `http://localhost:8580`.
* Klienten utför icke-blockerande nätverksanrop med hjälp av en executor för virtuella trådar.

### 4. Automatisk Audit-loggning
`AuditLoggerAspect` fångar automatiskt upp anrop till metoder i klasser som slutar på `Service`:
* Metoder som börjar med `create`, `update` eller `delete` loggas på `INFO`-nivå.
* Loggen inkluderar det aktuella användarnamnet (hämtat från `preferred_username` i JWT:n) eller "system" om autentisering saknas.

### 5. Global Felhantering
Biblioteket inkluderar en `GlobalExceptionHandler` som automatiskt formaterar svar för:
* `ResourceNotFoundException` (ger 404 Not Found).
* `AccessDeniedException` (ger 403 Forbidden med ett anpassat meddelande).

---
*Utvecklat av Grupp C*
