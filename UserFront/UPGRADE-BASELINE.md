# UserFront — Dependency Upgrade Baseline

This document captures the **"before" state** of the `UserFront` module's dependency
graph, prior to any framework/dependency upgrade. It exists so later upgrade sessions
have a reproducible baseline to diff against.

- **Module:** `UserFront` (`com.userFront:userFront:0.0.1-SNAPSHOT`)
- **Parent BOM:** `org.springframework.boot:spring-boot-starter-parent:1.5.4.RELEASE`
- **Declared `java.version`:** `1.8`
- **`pom.xml` note:** No explicit dependency versions are declared — every version
  below is *managed/resolved transitively* by the Spring Boot 1.5.4 parent.

## How this was captured

```bash
cd UserFront
mvn clean compile
mvn dependency:tree
mvn dependency:list
```

### Build environment used

| Tool  | Version |
| ----- | ------- |
| Maven | 3.6.3 |
| JDK (build/resolve) | Temurin/OpenJDK 8 (`1.8.0_492`) — matches the module's `java.version=1.8` |

> Note: `mvn dependency:tree`/`dependency:list` resolve versions from the Spring Boot
> 1.5.4 parent and are JDK-independent. Compilation, however, requires JDK 8 — the
> legacy `javax.persistence`/pre-JPMS source does **not** compile under a modern JDK
> (e.g. JDK 25), so JDK 8 was used to produce a clean `BUILD SUCCESS`.

## Notable resolved versions (summary)

| Area | Artifact | Resolved version |
| ---- | -------- | ---------------- |
| Spring Boot | `org.springframework.boot:*` | `1.5.4.RELEASE` |
| Spring Framework (core/web/etc.) | `org.springframework:spring-core`, `spring-web`, `spring-webmvc`, `spring-context`, `spring-beans`, `spring-jdbc`, `spring-tx`, `spring-orm`, `spring-aop`, `spring-expression`, `spring-aspects`, `spring-test` | `4.3.9.RELEASE` |
| Spring Security | `spring-security-config`, `spring-security-core`, `spring-security-web` | `4.2.3.RELEASE` |
| Spring Data JPA | `org.springframework.data:spring-data-jpa` | `1.11.4.RELEASE` |
| Spring Data Commons | `org.springframework.data:spring-data-commons` | `1.13.4.RELEASE` |
| Hibernate ORM | `org.hibernate:hibernate-core`, `hibernate-entitymanager` | `5.0.12.Final` |
| Hibernate Validator | `org.hibernate:hibernate-validator` | `5.3.5.Final` |
| JPA API | `org.hibernate.javax.persistence:hibernate-jpa-2.1-api` | `1.0.0.Final` |
| MySQL JDBC | `mysql:mysql-connector-java` | `5.1.42` |
| Thymeleaf | `org.thymeleaf:thymeleaf`, `thymeleaf-spring4` | `2.1.5.RELEASE` |
| Thymeleaf Layout Dialect | `nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect` | `1.4.0` |
| Jackson | `com.fasterxml.jackson.core:jackson-databind`, `jackson-core` | `2.8.8` |
| Jackson Annotations | `com.fasterxml.jackson.core:jackson-annotations` | `2.8.0` |
| Embedded Tomcat | `org.apache.tomcat.embed:tomcat-embed-core` (+ `-el`, `-websocket`), `org.apache.tomcat:tomcat-jdbc`/`tomcat-juli` | `8.5.15` |
| SLF4J | `org.slf4j:slf4j-api` (+ `jul-to-slf4j`, `log4j-over-slf4j`, `jcl-over-slf4j`) | `1.7.25` |
| Logback | `ch.qos.logback:logback-classic`, `logback-core` | `1.1.11` |
| AspectJ | `org.aspectj:aspectjweaver` | `1.8.10` |
| Validation API | `javax.validation:validation-api` | `1.1.0.Final` |
| SnakeYAML | `org.yaml:snakeyaml` | `1.17` |
| JUnit | `junit:junit` | `4.12` |
| Mockito | `org.mockito:mockito-core` | `1.10.19` |
| AssertJ | `org.assertj:assertj-core` | `2.6.0` |
| Hamcrest | `org.hamcrest:hamcrest-core`, `hamcrest-library` | `1.3` |

## Full `mvn dependency:tree` output

```
com.userFront:userFront:jar:0.0.1-SNAPSHOT
+- org.springframework.boot:spring-boot-starter-web:jar:1.5.4.RELEASE:compile
|  +- org.springframework.boot:spring-boot-starter:jar:1.5.4.RELEASE:compile
|  |  +- org.springframework.boot:spring-boot:jar:1.5.4.RELEASE:compile
|  |  +- org.springframework.boot:spring-boot-autoconfigure:jar:1.5.4.RELEASE:compile
|  |  +- org.springframework.boot:spring-boot-starter-logging:jar:1.5.4.RELEASE:compile
|  |  |  +- ch.qos.logback:logback-classic:jar:1.1.11:compile
|  |  |  |  \- ch.qos.logback:logback-core:jar:1.1.11:compile
|  |  |  +- org.slf4j:jul-to-slf4j:jar:1.7.25:compile
|  |  |  \- org.slf4j:log4j-over-slf4j:jar:1.7.25:compile
|  |  \- org.yaml:snakeyaml:jar:1.17:runtime
|  +- org.springframework.boot:spring-boot-starter-tomcat:jar:1.5.4.RELEASE:compile
|  |  +- org.apache.tomcat.embed:tomcat-embed-core:jar:8.5.15:compile
|  |  +- org.apache.tomcat.embed:tomcat-embed-el:jar:8.5.15:compile
|  |  \- org.apache.tomcat.embed:tomcat-embed-websocket:jar:8.5.15:compile
|  +- org.hibernate:hibernate-validator:jar:5.3.5.Final:compile
|  |  +- javax.validation:validation-api:jar:1.1.0.Final:compile
|  |  +- org.jboss.logging:jboss-logging:jar:3.3.1.Final:compile
|  |  \- com.fasterxml:classmate:jar:1.3.3:compile
|  +- com.fasterxml.jackson.core:jackson-databind:jar:2.8.8:compile
|  |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.8.0:compile
|  |  \- com.fasterxml.jackson.core:jackson-core:jar:2.8.8:compile
|  +- org.springframework:spring-web:jar:4.3.9.RELEASE:compile
|  |  +- org.springframework:spring-beans:jar:4.3.9.RELEASE:compile
|  |  \- org.springframework:spring-context:jar:4.3.9.RELEASE:compile
|  \- org.springframework:spring-webmvc:jar:4.3.9.RELEASE:compile
|     \- org.springframework:spring-expression:jar:4.3.9.RELEASE:compile
+- org.springframework.boot:spring-boot-starter-thymeleaf:jar:1.5.4.RELEASE:compile
|  +- org.thymeleaf:thymeleaf-spring4:jar:2.1.5.RELEASE:compile
|  |  +- org.thymeleaf:thymeleaf:jar:2.1.5.RELEASE:compile
|  |  |  +- ognl:ognl:jar:3.0.8:compile
|  |  |  \- org.unbescape:unbescape:jar:1.1.0.RELEASE:compile
|  |  \- org.slf4j:slf4j-api:jar:1.7.25:compile
|  \- nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:jar:1.4.0:compile
|     \- org.codehaus.groovy:groovy:jar:2.4.11:compile
+- org.springframework.boot:spring-boot-starter-jdbc:jar:1.5.4.RELEASE:compile
|  +- org.apache.tomcat:tomcat-jdbc:jar:8.5.15:compile
|  |  \- org.apache.tomcat:tomcat-juli:jar:8.5.15:compile
|  \- org.springframework:spring-jdbc:jar:4.3.9.RELEASE:compile
|     \- org.springframework:spring-tx:jar:4.3.9.RELEASE:compile
+- org.springframework.boot:spring-boot-starter-data-jpa:jar:1.5.4.RELEASE:compile
|  +- org.springframework.boot:spring-boot-starter-aop:jar:1.5.4.RELEASE:compile
|  |  \- org.aspectj:aspectjweaver:jar:1.8.10:compile
|  +- org.hibernate:hibernate-core:jar:5.0.12.Final:compile
|  |  +- org.hibernate.javax.persistence:hibernate-jpa-2.1-api:jar:1.0.0.Final:compile
|  |  +- org.javassist:javassist:jar:3.21.0-GA:compile
|  |  +- antlr:antlr:jar:2.7.7:compile
|  |  +- org.jboss:jandex:jar:2.0.0.Final:compile
|  |  +- dom4j:dom4j:jar:1.6.1:compile
|  |  \- org.hibernate.common:hibernate-commons-annotations:jar:5.0.1.Final:compile
|  +- org.hibernate:hibernate-entitymanager:jar:5.0.12.Final:compile
|  +- javax.transaction:javax.transaction-api:jar:1.2:compile
|  +- org.springframework.data:spring-data-jpa:jar:1.11.4.RELEASE:compile
|  |  +- org.springframework.data:spring-data-commons:jar:1.13.4.RELEASE:compile
|  |  +- org.springframework:spring-orm:jar:4.3.9.RELEASE:compile
|  |  \- org.slf4j:jcl-over-slf4j:jar:1.7.25:compile
|  \- org.springframework:spring-aspects:jar:4.3.9.RELEASE:compile
+- mysql:mysql-connector-java:jar:5.1.42:compile
+- org.springframework.boot:spring-boot-starter-security:jar:1.5.4.RELEASE:compile
|  +- org.springframework:spring-aop:jar:4.3.9.RELEASE:compile
|  +- org.springframework.security:spring-security-config:jar:4.2.3.RELEASE:compile
|  |  \- org.springframework.security:spring-security-core:jar:4.2.3.RELEASE:compile
|  \- org.springframework.security:spring-security-web:jar:4.2.3.RELEASE:compile
\- org.springframework.boot:spring-boot-starter-test:jar:1.5.4.RELEASE:test
   +- org.springframework.boot:spring-boot-test:jar:1.5.4.RELEASE:test
   +- org.springframework.boot:spring-boot-test-autoconfigure:jar:1.5.4.RELEASE:test
   +- com.jayway.jsonpath:json-path:jar:2.2.0:test
   |  \- net.minidev:json-smart:jar:2.2.1:test
   |     \- net.minidev:accessors-smart:jar:1.1:test
   |        \- org.ow2.asm:asm:jar:5.0.3:test
   +- junit:junit:jar:4.12:test
   +- org.assertj:assertj-core:jar:2.6.0:test
   +- org.mockito:mockito-core:jar:1.10.19:test
   |  \- org.objenesis:objenesis:jar:2.1:test
   +- org.hamcrest:hamcrest-core:jar:1.3:test
   +- org.hamcrest:hamcrest-library:jar:1.3:test
   +- org.skyscreamer:jsonassert:jar:1.4.0:test
   |  \- com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test
   +- org.springframework:spring-core:jar:4.3.9.RELEASE:compile
   \- org.springframework:spring-test:jar:4.3.9.RELEASE:test
```

## Full `mvn dependency:list` output (flat, sorted)

```
antlr:antlr:jar:2.7.7:compile
ch.qos.logback:logback-classic:jar:1.1.11:compile
ch.qos.logback:logback-core:jar:1.1.11:compile
com.fasterxml.jackson.core:jackson-annotations:jar:2.8.0:compile
com.fasterxml.jackson.core:jackson-core:jar:2.8.8:compile
com.fasterxml.jackson.core:jackson-databind:jar:2.8.8:compile
com.fasterxml:classmate:jar:1.3.3:compile
com.jayway.jsonpath:json-path:jar:2.2.0:test
com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test
dom4j:dom4j:jar:1.6.1:compile
javax.transaction:javax.transaction-api:jar:1.2:compile
javax.validation:validation-api:jar:1.1.0.Final:compile
junit:junit:jar:4.12:test
mysql:mysql-connector-java:jar:5.1.42:compile
net.minidev:accessors-smart:jar:1.1:test
net.minidev:json-smart:jar:2.2.1:test
nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:jar:1.4.0:compile
ognl:ognl:jar:3.0.8:compile
org.apache.tomcat.embed:tomcat-embed-core:jar:8.5.15:compile
org.apache.tomcat.embed:tomcat-embed-el:jar:8.5.15:compile
org.apache.tomcat.embed:tomcat-embed-websocket:jar:8.5.15:compile
org.apache.tomcat:tomcat-jdbc:jar:8.5.15:compile
org.apache.tomcat:tomcat-juli:jar:8.5.15:compile
org.aspectj:aspectjweaver:jar:1.8.10:compile
org.assertj:assertj-core:jar:2.6.0:test
org.codehaus.groovy:groovy:jar:2.4.11:compile
org.hamcrest:hamcrest-core:jar:1.3:test
org.hamcrest:hamcrest-library:jar:1.3:test
org.hibernate.common:hibernate-commons-annotations:jar:5.0.1.Final:compile
org.hibernate.javax.persistence:hibernate-jpa-2.1-api:jar:1.0.0.Final:compile
org.hibernate:hibernate-core:jar:5.0.12.Final:compile
org.hibernate:hibernate-entitymanager:jar:5.0.12.Final:compile
org.hibernate:hibernate-validator:jar:5.3.5.Final:compile
org.javassist:javassist:jar:3.21.0-GA:compile
org.jboss.logging:jboss-logging:jar:3.3.1.Final:compile
org.jboss:jandex:jar:2.0.0.Final:compile
org.mockito:mockito-core:jar:1.10.19:test
org.objenesis:objenesis:jar:2.1:test
org.ow2.asm:asm:jar:5.0.3:test
org.skyscreamer:jsonassert:jar:1.4.0:test
org.slf4j:jcl-over-slf4j:jar:1.7.25:compile
org.slf4j:jul-to-slf4j:jar:1.7.25:compile
org.slf4j:log4j-over-slf4j:jar:1.7.25:compile
org.slf4j:slf4j-api:jar:1.7.25:compile
org.springframework.boot:spring-boot-autoconfigure:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-aop:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-data-jpa:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-jdbc:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-logging:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-security:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-test:jar:1.5.4.RELEASE:test
org.springframework.boot:spring-boot-starter-thymeleaf:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-tomcat:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter-web:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-starter:jar:1.5.4.RELEASE:compile
org.springframework.boot:spring-boot-test-autoconfigure:jar:1.5.4.RELEASE:test
org.springframework.boot:spring-boot-test:jar:1.5.4.RELEASE:test
org.springframework.boot:spring-boot:jar:1.5.4.RELEASE:compile
org.springframework.data:spring-data-commons:jar:1.13.4.RELEASE:compile
org.springframework.data:spring-data-jpa:jar:1.11.4.RELEASE:compile
org.springframework.security:spring-security-config:jar:4.2.3.RELEASE:compile
org.springframework.security:spring-security-core:jar:4.2.3.RELEASE:compile
org.springframework.security:spring-security-web:jar:4.2.3.RELEASE:compile
org.springframework:spring-aop:jar:4.3.9.RELEASE:compile
org.springframework:spring-aspects:jar:4.3.9.RELEASE:compile
org.springframework:spring-beans:jar:4.3.9.RELEASE:compile
org.springframework:spring-context:jar:4.3.9.RELEASE:compile
org.springframework:spring-core:jar:4.3.9.RELEASE:compile
org.springframework:spring-expression:jar:4.3.9.RELEASE:compile
org.springframework:spring-jdbc:jar:4.3.9.RELEASE:compile
org.springframework:spring-orm:jar:4.3.9.RELEASE:compile
org.springframework:spring-test:jar:4.3.9.RELEASE:test
org.springframework:spring-tx:jar:4.3.9.RELEASE:compile
org.springframework:spring-web:jar:4.3.9.RELEASE:compile
org.springframework:spring-webmvc:jar:4.3.9.RELEASE:compile
org.thymeleaf:thymeleaf-spring4:jar:2.1.5.RELEASE:compile
org.thymeleaf:thymeleaf:jar:2.1.5.RELEASE:compile
org.unbescape:unbescape:jar:1.1.0.RELEASE:compile
org.yaml:snakeyaml:jar:1.17:runtime
```
