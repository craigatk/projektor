# example-data

Contains some projects that generate XML JUnit test reports
for use in testing.

These projects are decoupled from the main Gradle build so you have
to run them by hand if you add more examples and want to create
updated test reports.

For example, to run an individual test:

```
./gradlew test --tests "*ReallyLongOutput100kSpec"
```

Then the output XML file will be available in `build/test-results/test`
