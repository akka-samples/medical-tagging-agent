# CLAUDE.md - Akka Java SDK Project Guidelines

## Build Commands
```
mvn compile                   # Compile the project
mvn test                      # Run all tests
mvn test -Dtest=IntegrationTest    # Run a specific test class
mvn test -Dtest=IntegrationTest#test    # Run a specific test method
mvn exec:java                 # Run the service locally
mvn clean install -DskipTests # Build container image
```

## Code Style Guidelines
- **Imports**: Organize imports with standard Java packages first, then third-party packages, then project-specific packages
- **Package Structure**: Follow `io.akka.[domain-module].[api|application|domain]` organization
- **Naming**: Use camelCase for methods/variables, PascalCase for classes
- **Async**: Return `CompletionStage<T>` for async operations
- **Error Handling**: Use CompletableFuture exception handling for async code
- **Comments**: Use JavaDoc for public classes and methods
- **Access Control**: Be mindful of Akka @Acl annotations for endpoints

## Domain Model and Component Structure
- Use record classes for immutable domain models
- Prefer Java Optional for nullable values
- Implement sealed interfaces for event types with @TypeName annotations
- Use EventSourcedEntity for stateful components (@ComponentId)
- Implement HTTP endpoints with @HttpEndpoint and path annotations
- Use ComponentClient for inter-component communication
- Key Value Entities, Event Sourced Entities, Workflows can accept only single method parameter, wrap multiple parameters in a record class
- applyEvent method should return never return null, return the current state or throw an exception

## Testing
- Extend `TestKitSupport` for integration tests
- Use JUnit 5 annotations (@Test, etc.)
- Use `componentClient` for testing components
- Use `await()` helper method for async test assertions