# Hunter Pipeline — Security Remediation Report

**Repo:** COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2
**Stack:** Java 8, Spring Boot 1.5.4, Spring Security 4.2, Thymeleaf, MySQL
**Base branch:** master
**Remediation branch:** devin/1780594490-security-remediation
**Build:** `cd UserFront && mvn clean package -DskipTests` → BUILD SUCCESS (JDK 8)

This report documents minimal, scoped fixes for the dynamically-confirmed findings
from PR #25. No Spring Boot upgrade was performed and no existing security control
was weakened. Each fix below lists the vulnerability, the change, the file/lines,
and before/after snippets.

---

## CRITICAL

### 1. Password hash leak (sensitive field serialized in JSON)
**File:** `UserFront/src/main/java/com/userFront/domain/User.java`

`User` is returned directly by the admin REST endpoint `/api/user/all`
(`UserResource.userList`). Because `password` had no Jackson annotation, the BCrypt
hash was serialized into the JSON response. The `toString()` override also printed
the password into logs.

**Before**
```java
private String username;
private String password;
...
", password='" + password + '\'' +
```
**After**
```java
private String username;

@JsonIgnore
private String password;
...
// password removed from toString()
```
`@JsonIgnore` suppresses serialization of the property while `getPassword()` is still
available to Spring Security (`UserDetails`). The `import com.fasterxml.jackson.annotation.JsonIgnore`
was already present. No `ssn` field exists in the domain model, so `password` was the
only sensitive field.

---

### 2. Negative / over-balance amount exploit (deposit, withdraw, transfers)
**Files:**
- `UserFront/src/main/java/com/userFront/service/UserServiceImpl/AccountServiceImpl.java` (`deposit`, `withdraw`)
- `UserFront/src/main/java/com/userFront/service/UserServiceImpl/TransactionServiceImpl.java` (`betweenAccountsTransfer`, `toSomeoneElseTransfer`)
- `UserFront/src/main/java/com/userFront/controller/AccountController.java` (`depositPOST`, `withdrawPOST`)
- `UserFront/src/main/java/com/userFront/controller/TransferController.java` (`betweenAccountsPost`, `toSomeoneElsePost`)

No amount validation existed. A negative deposit/transfer let a user inflate or drain
balances (e.g. depositing `-100` to another account, or transferring more than the
available balance / a negative amount to manufacture funds).

Authoritative validation was added in the service layer (rejecting `amount <= 0` and
withdrawals/transfers that exceed the source balance), with fast-fail guards in the
controllers that redirect back to the form on a non-positive amount.

**Before (`AccountServiceImpl.withdraw`)**
```java
public void withdraw(String accountType, double amount, Principal principal) {
    User user = userService.findByUsername(principal.getName());
    if (accountType.equalsIgnoreCase("Primary")) {
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
```
**After**
```java
public void withdraw(String accountType, double amount, Principal principal) {
    if (amount <= 0) {
        throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
    }
    User user = userService.findByUsername(principal.getName());
    if (accountType.equalsIgnoreCase("Primary")) {
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        if (primaryAccount.getAccountBalance().compareTo(new BigDecimal(amount)) < 0) {
            throw new IllegalArgumentException("Insufficient funds in Primary account");
        }
        primaryAccount.setAccountBalance(primaryAccount.getAccountBalance().subtract(new BigDecimal(amount)));
```
`deposit` rejects `amount <= 0`. `betweenAccountsTransfer` and `toSomeoneElseTransfer`
reject non-positive amounts and amounts exceeding the source account balance
(using `BigDecimal.compareTo`). Controllers add a guard, e.g.:
```java
double withdrawAmount = Double.parseDouble(amount);
if (withdrawAmount <= 0) {
    return "redirect:/account/withdraw";
}
```

---

### 3. CSRF protection disabled
**File:** `UserFront/src/main/java/com/userFront/config/SecurityConfig.java`

CSRF was globally disabled, exposing all browser-form state-changing endpoints
(transfers, deposits, withdrawals, profile edits, recipient management) to
cross-site request forgery.

**Before**
```java
http
    .csrf().disable().cors().disable()
    .formLogin()...
```
**After**
```java
http
    .csrf().ignoringAntMatchers("/api/**")
    .and()
    .cors().disable()
    .formLogin()...
```
CSRF is now enabled for all Thymeleaf form flows. Spring Security's Thymeleaf
integration auto-injects the `_csrf` hidden token into every form that uses
`th:action` (verified: all 9 templates use `th:action` with `method="post"`), so the
browser app continues to work. The Angular admin portal calls the cross-origin REST
API under `/api/**`; those endpoints are excluded from CSRF (they remain protected by
`@PreAuthorize("hasRole('ADMIN')")` and the authenticated admin session). The custom
`/logout` matcher accepts the existing GET logout link, so logout is unaffected.

---

### 4. Profile IDOR (mass account takeover via request-supplied username)
**File:** `UserFront/src/main/java/com/userFront/controller/UserController.java` (`profilePost`)

The profile update loaded and saved the user identified by the **request-supplied**
`username`, allowing any authenticated user to overwrite another user's profile.

**Before**
```java
public String profilePost(@ModelAttribute("user") User newUser, Model model) {
    User user = userService.findByUsername(newUser.getUsername());
    user.setUsername(newUser.getUsername());
    user.setFirstName(newUser.getFirstName());
    ...
}
```
**After**
```java
public String profilePost(@ModelAttribute("user") User newUser, Principal principal, Model model) {
    User user = userService.findByUsername(principal.getName());
    user.setFirstName(newUser.getFirstName());
    ...
}
```
The user is now loaded from the authenticated `Principal`. The `setUsername(...)` call
was removed so the account identity can no longer be changed/hijacked via the form.

---

### 5. Global recipient IDOR
**Files:**
- `UserFront/src/main/java/com/userFront/service/TransactionService.java` (interface)
- `UserFront/src/main/java/com/userFront/service/UserServiceImpl/TransactionServiceImpl.java` (`findRecipientByName`, `deleteRecipientByName`)
- `UserFront/src/main/java/com/userFront/controller/TransferController.java` (`recipientEdit`, `recipientDelete`, `toSomeoneElsePost`)

`findRecipientByName` / `deleteRecipientByName` looked recipients up by name globally,
with no owner check. Any user could view, edit, delete, or transfer to another user's
recipient by guessing the name.

**Before**
```java
public Recipient findRecipientByName(String recipientName) {
    return recipientDao.findByName(recipientName);
}
public void deleteRecipientByName(String recipientName) {
    recipientDao.deleteByName(recipientName);
}
```
**After**
```java
public Recipient findRecipientByName(String recipientName, Principal principal) {
    Recipient recipient = recipientDao.findByName(recipientName);
    if (recipient != null && recipient.getUser() != null
            && principal.getName().equals(recipient.getUser().getUsername())) {
        return recipient;
    }
    return null;
}
public void deleteRecipientByName(String recipientName, Principal principal) {
    Recipient recipient = findRecipientByName(recipientName, principal);
    if (recipient != null) {
        recipientDao.delete(recipient);
    }
}
```
Both methods now take the `Principal` and only return/delete a recipient that belongs
to the current user. The controller endpoints pass the `Principal` and guard against a
`null` (non-owned) recipient by redirecting back instead of acting or rendering a page
with a null model attribute.

---

### 6. Hardcoded database credentials
**File:** `UserFront/src/main/resources/application.properties`

The DB username and password were committed in plaintext.

**Before**
```properties
spring.datasource.username = root
spring.datasource.password = avengers1993
```
**After**
```properties
spring.datasource.username = ${DB_USERNAME:root}
spring.datasource.password = ${DB_PASSWORD:}
```
Credentials are now read from `DB_USERNAME` / `DB_PASSWORD` environment variables, with
safe local-dev defaults. The previously committed password should be rotated.

---

## HIGH

### 7. Admin IDOR / missing parameter validation
**File:** `UserFront/src/main/java/com/userFront/resource/UserResource.java`

The admin endpoints accept an arbitrary `username` with no validation, and
`enable`/`disable`/transaction lookups would NPE on an unknown user
(`findByUsername` returns `null`).

**Change:** A `validateUsername(...)` guard is invoked by
`getPrimaryTransactionList`, `getSavingsTransactionList`, `enableUser`, and
`disableUser`. It rejects blank usernames and usernames that don't resolve to an
existing user, returning HTTP 400 via a `@ResponseStatus`-annotated exception
(`ResponseStatusException` is Spring 5-only, so a small annotated exception class is
used for Spring 4 compatibility).
```java
private void validateUsername(String username) {
    if (username == null || username.trim().isEmpty()) {
        throw new InvalidUserRequestException("A non-empty username is required");
    }
    if (userService.findByUsername(username) == null) {
        throw new InvalidUserRequestException("Unknown username: " + username);
    }
}
```
The class-level `@PreAuthorize("hasRole('ADMIN')")` authorization control was retained.

---

### 8. State-changing operations exposed over GET
**Files:**
- `UserFront/src/main/java/com/userFront/controller/TransferController.java` (`recipientDelete`)
- `UserFront/src/main/resources/templates/recipient.html`
- `UserFront/src/main/java/com/userFront/resource/AppointmentResource.java` (`confirmAppointment`)
- `AdminPortal/src/app/appointment.service.ts`

Recipient deletion and appointment confirmation were `GET` requests, making them
trivially CSRF-able and cacheable/prefetchable.

**Before**
```java
@RequestMapping(value = "/recipient/delete", method = RequestMethod.GET)
...
@RequestMapping("/{id}/confirm")  // any method, used as GET
```
**After**
```java
@RequestMapping(value = "/recipient/delete", method = RequestMethod.POST)
...
@RequestMapping(value = "/{id}/confirm", method = RequestMethod.POST)
```
The recipient list `delete` link was converted from an `<a href>` GET link to an inline
`POST` form (CSRF token auto-injected by Thymeleaf). The Angular admin portal's
`confirmAppointment` was switched from `http.get` to `http.post`.

---

### 9. Appointment mass-assignment
**File:** `UserFront/src/main/java/com/userFront/controller/AppointmentController.java` (`createAppointmentPost`)

The endpoint bound the full `Appointment` entity via `@ModelAttribute`, so a crafted
request could set `id`, `confirmed`, or `user` directly (e.g. self-confirming an
appointment or assigning it to another user).

**Before**
```java
public String createAppointmentPost(@ModelAttribute("appointment") Appointment appointment,
        @ModelAttribute("dateString") String date, Model model, Principal principal) {
    ...
    appointment.setDate(d1);
    appointment.setUser(userService.findByUsername(principal.getName()));
    appointmentService.createAppointment(appointment);
}
```
**After**
```java
public String createAppointmentPost(@RequestParam("location") String location,
        @RequestParam("description") String description,
        @RequestParam("dateString") String date, Model model, Principal principal) {
    Appointment appointment = new Appointment();
    appointment.setDate(d1);
    appointment.setLocation(location);
    appointment.setDescription(description);
    appointment.setConfirmed(false);
    appointment.setUser(userService.findByUsername(principal.getName()));
    appointmentService.createAppointment(appointment);
}
```
Only the safe fields are bound via `@RequestParam`; `confirmed` is forced to `false`,
`user` is set from the authenticated principal, and `id` can no longer be supplied.

---

### 10. Overly permissive CORS
**File:** `UserFront/src/main/java/com/userFront/config/RequestFilter.java`

CORS headers (including `Access-Control-Allow-Credentials: true`) were emitted on
**every** response regardless of the request `Origin`.

**Before**
```java
response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
response.setHeader("Access-Control-Max-Age", "3600");
response.setHeader("Access-Control-Allow-Credentials", "true");
```
**After**
```java
String origin = request.getHeader("Origin");
boolean originAllowed = ALLOWED_ORIGIN.equals(origin);
if (originAllowed) {
    response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Vary", "Origin");
}
response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
response.setHeader("Access-Control-Allow-Headers", "x-requested-with, authorization, content-type");
response.setHeader("Access-Control-Max-Age", "3600");
```
The credentialed CORS grant is now emitted **only** to the explicit allow-listed origin
(`http://localhost:4200`) via an exact `Origin` match, with `Vary: Origin` for correct
caching. `Access-Control-Allow-Credentials` is retained for the trusted origin because
the admin portal authenticates with cookies (`withCredentials: true`); removing it
outright would break admin auth, so a properly scoped config was used instead.

---

## Build verification
```
cd UserFront && mvn clean package -DskipTests
[INFO] BUILD SUCCESS
```
Compiled with JDK 8 (matching `java.version=1.8` in `pom.xml`). Tests were skipped per
instructions (they require a live MySQL instance).

## Notes / follow-ups
- Rotate the previously committed DB password (`avengers1993`).
- The Angular `AdminPortal` is a separate project and is not part of the Maven build;
  the one-line `confirmAppointment` change keeps the admin "Confirm" action working
  with the new POST endpoint.
