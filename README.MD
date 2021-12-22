1. Tests expect that app was already started(deployed on CI for example)
2. By default, they search app on the localhost. You can change the URL in tests.BaseTest
3. To run tests execute "./gradlew clean test"
4. 2 validation tests will fail due to validation issues