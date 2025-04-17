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


## UI colors
 - White - #f1f1f1 - in its pure form, like the text you’re reading, is used for most text. Icons and lines are often white, too. There is also an off-white option for rare usage.
 - Yellow - #ffce4a - is our primary accent color. Our brand is black with yellow-forward, which means we will use yellow as the primary accent to the overall black aesthetic. Use yellow for labels and to emphasize key words, outlines in diagrams, icons, etc.
 - Blue - #00dbdd - is our CTA (call-to-action) color, which is used for hyperlinks in slides, and as CTA buttons on the website. Use this very sparingly as an accent color.
 - Green - #72d35b - is to be used for highlighting really important positive ideas, benefits, and GOOD things. Use as an accent color, and always use less than yellow.
 - Red - #ff5400 - this is to be used for highlighting negative ideas, problems, warnings, and BAD things. Use as an accent color, and always use less than yellow.
 - Grey - #a6a6a6 - and Black - #000000 - are used in the background designs of the slide layouts. Sometimes you will use a pure black or grey box to highlight an area.