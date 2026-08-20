# Open Questions — Facts the Repository Cannot Settle

Each question is addressed to the role that owns the answer. IDs are referenced by
`target-state.md` and `migration-plan.md`. Questions marked **BLOCKING** must be answered before
design sign-off; the rest before the workstream that depends on them starts.

---

## Programme

| ID | Question | Owner | Why it cannot be answered from the repo |
|---|---|---|---|
| **OQ-PROG-01** | **BLOCKING.** What is the actual approved ("blessed") AWS service list, and does it include ECS Fargate, Aurora PostgreSQL, RDS Proxy, ElastiCache for Redis, CloudFront, WAF, Cognito, Secrets Manager, DMS and SCT? | Platform Engineering | No list was supplied. The list in `target-state.md` §1 is an assumption authored for this package and every target-state row depends on it |
| **OQ-PROG-02** | **BLOCKING.** Is this repository what is actually deployed today, and if so where does it run — a data centre VM, a managed host, or nowhere (development only)? Is there a production instance with real customer data? | Application Owner | The repo contains no deployment artefact of any kind: no Dockerfile, no manifest, no IaC, no pipeline. The release path in `current-state.md` §2 documents that absence, not an observed process |
| OQ-PROG-03 | What is the lead time for Control Tower account vending, and what evidence does the platform team require from a running dev account before QA is granted? | Platform Engineering | Guardrail G4 makes vending the critical path; only the platform team knows its own cycle time |
| OQ-PROG-04 | Are there other repositories, batch jobs, reports or downstream consumers that read the `onlinebanking` MySQL schema directly? | Application Owner | Direct schema consumers are invisible from inside this codebase and would each be broken by the PostgreSQL move |
| OQ-PROG-05 | Which of the eight recorded artefact contradictions (`current-state.md` §10) reflect the deployed reality, in particular C1 (README claims Log4j, build has none) and C2 (README claims two servers, build has one)? | Application Owner | Documentation and manifests disagree; only the owner can say which the running system follows |

## Database

| ID | Question | Owner | Why it cannot be answered from the repo |
|---|---|---|---|
| **OQ-DB-01** | **BLOCKING.** What are the RTO and RPO for this workload? | Application Owner (with Risk) | Determines whether Aurora Multi-AZ suffices or Aurora Global Database with a warm standby region is mandatory, and therefore a large share of the run cost |
| OQ-DB-02 | What is the production data volume per table (users, primary/savings transactions, recipients, appointments) and the peak write rate? | DBA | Sets the DMS full-load and CDC window, and whether a short read-only freeze is viable at cutover |
| OQ-DB-03 | What is the exact MySQL server version, character set and collation in production, and are there objects in the schema not represented by the JPA entities — views, triggers, procedures, or tables written by another system? | DBA | The repo has no DDL: the schema is generated at runtime by `spring.jpa.hibernate.ddl-auto = update` (`UserFront/src/main/resources/application.properties:31`), so the real shape of production is unknown |
| OQ-DB-04 | What value is the account-number sequence currently at in production, and what is the lowest safe starting value for the replacement PostgreSQL sequence? | DBA | The code seeds a per-JVM counter at a literal (`UserFront/src/main/java/com/userFront/service/UserServiceImpl/AccountServiceImpl.java:24`) that bears no defined relationship to the persisted rows |
| OQ-DB-05 | Is any data in scope for residency, retention or data-classification rules that constrain the target region or backup destination? | Data Governance / Risk | Nothing in the code expresses a classification |

## Platform

| ID | Question | Owner | Why it cannot be answered from the repo |
|---|---|---|---|
| **OQ-PLAT-01** | **BLOCKING.** Do the ten default guardrails in `target-state.md` §2 match the organisation's real platform guardrails, or should they be replaced? | Platform Engineering | They are the playbook's defaults, used because none were supplied, and are labelled as assumptions throughout |
| OQ-PLAT-02 | Is the assumption that HTTP sessions are held in the servlet container's in-memory store correct for the deployed system, and is there sticky-session load balancing today? | Platform Engineering / Application Owner | No session store is configured in the properties file and no cache client exists in the dependency tree, so this is an inference (`current-state.md` §8) |
| OQ-PLAT-03 | Which Java LTS and Spring Boot versions are the organisation's supported baseline for new container workloads? | Platform Engineering | The upgrade in `target-state.md` row 2 assumes Java 21 and Spring Boot 3.x |
| OQ-PLAT-04 | For Terraform state, is DynamoDB-based locking available (it is off the assumed list), or should S3 native locking or a managed Terraform backend be used? | Platform Engineering | This is the only `OFF-LIST?` item in the design |
| OQ-PLAT-05 | What VPC, subnet, CIDR and connectivity standards apply — is there an existing transit/network account, and is internet egress via a central NAT or per-account? | Network Engineering | Determines VPC endpoint scope and whether self-hosting the public CDN assets (`target-state.md` row 16) is mandatory or merely preferred |
| OQ-PLAT-06 | Which AWS regions are approved for primary and secondary, given the multi-region prod posture? | Platform Engineering | Affects Aurora Global Database topology and cost |

## Delivery

| ID | Question | Owner | Why it cannot be answered from the repo |
|---|---|---|---|
| OQ-DEL-01 | Which CI/CD tool is the approved one, who operates the runners, and can they reach the target AWS accounts through an OIDC role rather than static keys? | Release Manager / Platform Engineering | The repository contains no CI configuration whatsoever, so there is no incumbent to infer from |
| OQ-DEL-02 | Which artifact sources are approved — is there an internal Maven proxy, an npm registry mirror, and an approved container base-image catalogue? | Platform Engineering | Today the Maven wrapper pulls its distribution straight from `repo1.maven.org` (`UserFront/.mvn/wrapper/maven-wrapper.properties:1`), which guardrail G9 would not permit |
| OQ-DEL-03 | What are the change-management requirements for a production deployment — approval gates, change windows, freeze periods? | Release Manager | Shapes WS7 cutover sequencing |
| OQ-DEL-04 | What quality gates must the pipeline enforce (coverage threshold, SAST/DAST, dependency scanning, licence policy)? | Release Manager / Security | The project has one non-asserting test (`UserFront/src/test/java/com/userFront/UserFrontApplicationTests.java:10-14`) and no enforced lint or scan, so the gate has to be defined rather than inherited |

## Security

| ID | Question | Owner | Why it cannot be answered from the repo |
|---|---|---|---|
| **OQ-SEC-01** | **BLOCKING.** The database credential committed at `UserFront/src/main/resources/application.properties:11-12` must be treated as compromised. Has it ever been used against a production database, and who owns rotation and revocation? | Security / DBA | Git history shows the value is present in the working tree; it cannot show where it was used |
| OQ-SEC-02 | Is there an enterprise IdP for staff access (Entra ID, Okta, Ping) that the admin plane should federate to instead of standing up Cognito? | Security / Identity | Admin login currently reuses the customer application's form login and session cookie (`AdminPortal/src/app/login.service.ts:10-19`) |
| OQ-SEC-03 | Who accepts the risk on the application-level security defects — CSRF disabled with state-changing `GET`s (`UserFront/src/main/java/com/userFront/config/SecurityConfig.java:60`, `UserFront/src/main/java/com/userFront/resource/UserResource.java:45,50`), unscoped recipient access (`UserFront/src/main/java/com/userFront/controller/TransferController.java:79,93`), and password-bearing `toString()` (`UserFront/src/main/java/com/userFront/domain/User.java:161-174`) — and are they accepted as prod-cutover blockers? | Security / Application Owner | Risk acceptance is a decision, not a code fact |
| OQ-SEC-04 | Is a penetration test or independent security review required before the workload may take production traffic? | Security | Guardrail G7 states resilience criteria but is silent on security testing |
| OQ-SEC-05 | Are customer PII fields (name, email, phone in `UserFront/src/main/java/com/userFront/domain/User.java:32-39`) subject to encryption, masking or logging restrictions beyond the standard baseline? | Data Governance / Security | No classification metadata exists in the code |

## Operations

| ID | Question | Owner | Why it cannot be answered from the repo |
|---|---|---|---|
| OQ-OPS-01 | What is the mandatory tag set (exact keys and allowed values), and is CloudWatch the central observability platform or is telemetry forwarded to a third-party tool? | Platform Engineering / Operations | Guardrail G10 requires both; the specifics are organisational |
| OQ-OPS-02 | Who is on call for this workload after cutover, and what alert thresholds and escalation path apply? | Operations / Application Owner | There is no monitoring configuration in the repo to infer intent from |
| OQ-OPS-03 | What are the expected concurrent-user and transaction volumes, so Fargate task sizing, Aurora capacity and the load-test target can be set? | Application Owner | No load profile exists anywhere in the codebase |
| OQ-OPS-04 | What is the maintenance-window and acceptable-downtime policy for the cutover itself? | Operations / Release Manager | Determines whether DMS CDC with near-zero downtime is required or a scheduled outage is acceptable |
| OQ-OPS-05 | What is the retention requirement for application logs and audit trails? | Operations / Risk | Sets CloudWatch Logs retention and archive-to-S3 policy |
