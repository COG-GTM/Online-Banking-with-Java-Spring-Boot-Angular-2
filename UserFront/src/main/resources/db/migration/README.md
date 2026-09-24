# Database migrations

`V1__baseline.sql` is a recorded snapshot of the `onlinebanking` schema as produced by the
Boot 1.5.4 / Hibernate 5.0 application. It is the reference point for the Hibernate 7 type
diff in the upgrade work (`boolean` -> `bit(1)` vs `tinyint`, `Date` precision, ...), so the
file is committed as data, not regenerated per environment.

Flyway is **not** wired up yet; this directory only holds the baseline SQL for now.

All commands below are run from the repository root.

```bash
BASELINE=UserFront/src/main/resources/db/migration/V1__baseline.sql
```

## Regenerating the baseline

```bash
mysqldump --no-data --skip-add-drop-table --routines onlinebanking > "$BASELINE"
```

Then strip the environment-specific bits in place so the file is portable:

```bash
sed -E -i -e '/^-- MySQL dump/d' \
          -e '/^-- Host:/d' \
          -e '/^-- Server version/d' \
          -e '/^-- Dump completed/d' \
          -e 's/ AUTO_INCREMENT=[0-9]+//g' \
          -e 's/DEFINER=`[^`]*`@`[^`]*` //g' \
          "$BASELINE"
```

## Verifying the baseline

Apply the file to an empty MySQL 8 schema and start the unmodified Boot 1.5.4 app against it
with `ddl-auto=validate`:

```bash
# 1. empty MySQL 8 schema
docker run -d --name ob-mysql -e MYSQL_ROOT_PASSWORD=avengers1993 -p 3306:3306 \
  mysql:8.0 --default-authentication-plugin=mysql_native_password
mysql -h 127.0.0.1 -uroot -p -e "CREATE DATABASE onlinebanking_verify;"

# 2. apply the baseline
mysql -h 127.0.0.1 -uroot -p onlinebanking_verify < "$BASELINE"

# 3. build and start the app with schema validation
mvn -f UserFront/pom.xml -DskipTests -Dmysql.version=5.1.49 package
java -jar UserFront/target/userFront-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url="jdbc:mysql://127.0.0.1:3306/onlinebanking_verify?useSSL=false" \
  --spring.jpa.hibernate.ddl-auto=validate
```

The app logs `Started UserFrontApplication` when validation passes; a mismatch fails startup
with a `Schema-validation` error instead.

Two harness-only overrides are used above and neither changes the checked-in build:

* `-Dmysql.version=5.1.49` — the Boot 1.5.4 managed driver (5.1.42) cannot talk to MySQL 8
  (`Unknown system variable 'query_cache_size'`). The property is supplied on the command
  line; `pom.xml` is untouched.
* `useSSL=false` — the 5.1 driver negotiates TLS versions that modern JDKs disable.
