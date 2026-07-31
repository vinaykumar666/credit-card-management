# Java 21 Features in This Platform — What & Why

| Feature | Where used | Why required |
|---------|------------|--------------|
| **Records** | `ChannelClientContext`, `ErrorCodeDefinition`, `ErrorResponse`, event/DTO carriers | Immutable value objects without boilerplate; safer API contracts |
| **Sealed classes** | `BusinessException` permits only known subtypes | Exhaustive `switch` in handlers; prevents uncontrolled exception hierarchies |
| **Pattern matching for switch** | `ErrorResponseFactory`, exception handlers | Type-safe branching on sealed exceptions with guards (`when`) |
| **Sequenced collections** | `LinkedHashMap` for YAML error-code order | Predictable iteration order for config dumps and debugging |
| **Virtual threads** | `spring.threads.virtual.enabled=true` on servlet services | Scale blocking JDBC/HTTP calls without huge platform thread pools |
| **Text blocks** | SQL migrations / test fixtures where multi-line strings appear | Readable multi-line literals without escape noise |
| **`Optional` + records** | Error code lookups | Clear absence handling without null sprawl |

Java version is fixed at **21** in the parent POM (`java.version`) and Docker base images (`eclipse-temurin:21-*`).
