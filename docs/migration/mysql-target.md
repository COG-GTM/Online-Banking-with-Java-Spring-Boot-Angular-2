# MySQL server target and Connector/J pin

Ticket: [COG-1148](https://linear.app/cog-gtm/issue/COG-1148/w0-confirm-mysql-server-version-and-pick-the-connectorj-target).
Read by the W3 pivot ([COG-1169](https://linear.app/cog-gtm/issue/COG-1169)) before it swaps the driver, and by the
Testcontainers harness ([COG-1141](https://linear.app/cog-gtm/issue/COG-1141)).

## Decision

**Pin `mysql-connector-j` 8.4.x. Blocked on the database owner confirming the production server version
(owner: unassigned, to be named by the migration lead; needed before COG-1169 merges).**

| Item | Decision |
| --- | --- |
| Driver in W3 (COG-1169) | `com.mysql:mysql-connector-j`, `<scope>runtime</scope>`, with `<mysql.version>8.4.0</mysql.version>` in `UserFront/pom.xml`. |
| When to unpin | When every environment below reports `SELECT VERSION()` >= 8.0: drop the property and use the Boot-managed version (9.7.0 in Boot 3.5.16, 4.0.8 and 4.1.1). |
| Do not use | Connector/J 26.x. It supports MySQL 8.4 and higher only, so it would break an 8.0 server. |
| Server target | **MySQL 8.4 LTS.** Hibernate 6.6 (Boot 3.5, W3) and Hibernate 7.4 (Boot 4.1, W4) both need MySQL 8.0 or later, and 8.0 moved to Oracle Sustaining Support on 2026-04-21. |
| Testcontainers image | **`mysql:8.4.11`**. The harness also needs one change for it; see [Testcontainers harness](#testcontainers-harness). |

Pinning the driver keeps W3 from breaking a 5.7 server at the driver layer. It does **not** make Boot 3.5 or
4.1 supported on MySQL 5.7, because Hibernate 6.6 and 7.x assume 8.0 or later. If production is on 5.x, the
server upgrade is a hard prerequisite for shipping W3 to production, and for the W4 final gate
([COG-1176](https://linear.app/cog-gtm/issue/COG-1176)).

## Environment inventory

| Environment | `SELECT VERSION()` | Source |
| --- | --- | --- |
| Production | **Unknown.** Needs owner confirmation. | Not reachable from the build VM, and the repo has no deployment config, Dockerfile, compose file, CI workflow or environment notes. |
| Developer default (`application.properties`) | Must be 5.x (inferred, see evidence 1) | `jdbc:mysql://localhost:3306/onlinebanking`, `MySQL5Dialect`, Boot-managed Connector/J 5.1.42 |
| Schema baseline, PR [#107](https://github.com/COG-GTM/Online-Banking-with-Java-Spring-Boot-Angular-2/pull/107) (COG-1146) | 8.0.46 | `mysql:8.0` container; the app needed `-Dmysql.version=5.1.49` to connect |
| Testcontainers harness (COG-1141 branch) | 8.0.46 (`mysql:8.0`, floating) | `AbstractIntegrationTest.DEFAULT_MYSQL_IMAGE`; the pom overrides the driver to 5.1.49 |

### Evidence that production was set up on MySQL 5.x

1. **The checked-in driver cannot connect to any MySQL 8 server.** Boot 1.5.4 manages
   `mysql-connector-java` 5.1.42, and `pom.xml` does not override it. On MySQL 8.0 that driver fails with
   `Unknown system variable 'query_cache_size'` (the variable was removed in 8.0.3). On 8.4 it fails with
   `Unable to load authentication plugin 'caching_sha2_password'`. Any environment running `master` unmodified
   must therefore be on 5.x, unless its build overrides `mysql.version`, and nothing in the repo does that.
2. `hibernate.dialect = org.hibernate.dialect.MySQL5Dialect` is configured explicitly.
3. The code dates from 2017, before MySQL 8.0 went GA (April 2018).

This is strong circumstantial evidence, not a measurement. Only the owner's `SELECT VERSION()` settles it.

## Driver/server compatibility (measured)

Each driver jar opened a JDBC connection and ran `SELECT VERSION()` against throwaway containers
(`mysql:5.7` = 5.7.44, `mysql:8.0` = 8.0.46, `mysql:8.4` = 8.4.11). URL flags:
`useSSL=false&allowPublicKeyRetrieval=true`. JDK 17; the 5.1 rows were repeated on Temurin 8u504.

| Connector/J | 5.7.44 | 8.0.46 | 8.4.11 | Vendor-supported servers |
| --- | --- | --- | --- | --- |
| 5.1.42 (Boot 1.5.4 default) | OK | FAIL `query_cache_size` | FAIL `caching_sha2_password` | 5.x |
| 5.1.49 (last 5.1) | OK | OK | OK | 5.x, plus 8.0 connectivity |
| 8.4.0 (last 8.x) | OK | OK | OK | 5.7, 8.0+ |
| 9.7.0 (Boot 3.5 / 4.0 / 4.1 managed) | OK (connects, unsupported) | OK | OK | 8.0 and later |
| 26.7.0 (latest) | OK (connects, unsupported) | OK | OK | 8.4 and later |

Connector/J 9.x does not refuse to connect to 5.7. Oracle stopped testing and supporting that pairing
(9.0.0 release notes: "can be used against MySQL Server version 8.0 and later"), so 9.x on 5.7 is a support
risk, not a guaranteed connection failure. 8.4.0 is the newest driver that officially supports every server
production could be running.

## Upgrade path if production is below 8.0

* Path: **5.7 → 8.0 → 8.4**. MySQL does not support skipping an LTS/bugfix series in an in-place upgrade (MySQL
  8.4 manual, *Upgrade Paths*). A logical dump and reload into a fresh 8.4 instance also works, and
  `V1__baseline.sql` (PR #107) applies cleanly to 8.4.11.
* Before upgrading, run MySQL Shell's upgrade checker against production:
  `mysqlsh -- util checkForServerUpgrade root@<host>:3306 --target-version=8.4.11`.
* 8.4 removes the `default_authentication_plugin` option, and new accounts default to `caching_sha2_password`.
  Until W3 lands, the app runs on Connector/J 5.1.49 (COG-1141 pom override). That driver needs
  `allowPublicKeyRetrieval=true` (or TLS) in the JDBC URL to authenticate against 8.4.
* Owner: **TBD.** Confirm `SELECT VERSION()` for every environment and record it in the inventory above. If any
  environment is below 8.0, schedule the upgrade before the COG-1169 merge date.

## Testcontainers harness

Pin `mysql:8.4.11`, a fully qualified tag, so the image cannot drift under the smoke suite.

That image does not start with the harness as written on the COG-1141 branch. The harness passes
`--default-authentication-plugin=mysql_native_password`, which MySQL 8.4 rejects
(`unknown variable 'default-authentication-plugin=mysql_native_password'`). The change the harness needs:

```diff
-	public static final String DEFAULT_MYSQL_IMAGE = "mysql:8.0";
+	public static final String DEFAULT_MYSQL_IMAGE = "mysql:8.4.11";
 ...
 			.withUrlParam("useSSL", "false")
-			.withCommand("--default-authentication-plugin=mysql_native_password");
+			.withUrlParam("allowPublicKeyRetrieval", "true");
```

Checked on the COG-1141 harness branch (`3672d0e`) with `mvn -B -f UserFront/pom.xml verify` on Temurin 8u504:

| Image | Harness | Result |
| --- | --- | --- |
| `mysql:8.0` (default today) | as-is | BUILD SUCCESS |
| `mysql:8.4.11` | as-is | FAIL: container never starts (`unknown variable 'default-authentication-plugin=...'`) |
| `mysql:8.4.11` | with the change above | BUILD SUCCESS |
| `mysql:5.7.44` | as-is | BUILD SUCCESS |

While production is unconfirmed, the harness's existing override also lets the suite run against a 5.7
server: `-Dtest.mysql.image=mysql:5.7.44`. Drop that run once the owner confirms 8.0 or later.

## Re-checking a server

```sql
SELECT VERSION();                    -- e.g. 5.7.44 / 8.0.46 / 8.4.11
SHOW VARIABLES LIKE 'version%';
SELECT user, host, plugin FROM mysql.user WHERE user = '<app user>';
```
