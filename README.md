# Mobile E2E Automation

Mobile end-to-end automation framework for the Trello Android application.

The project demonstrates a hybrid test approach:

```text
Trello API setup/cleanup -> Android UI actions with Appium -> UI/API assertions
```

## Tech stack

- Java 17
- Maven Wrapper
- JUnit 5
- Appium Java Client
- Selenium WebDriver
- Jackson
- Android Debug Bridge (ADB)

## Covered scenarios

### API

- Trello API client request and response mapping
- OAuth authorization header construction
- Open-board lookup
- List and card lookup
- Board deletion
- Appium health-check URL construction (without a running Appium server)

### Android UI

| Main scenario | Test class | Verification |
| --- | --- | --- |
| Create and open a board through UI | `AndroidBoardCreationTest` | Board title and Add List control |
| Create a list through UI | `AndroidListCreationTest` | List visible in UI and found through API |
| Create a card through UI | `AndroidCardCreationTest` | Card visible in UI and found through API |
| Edit card name and description | `AndroidCardEditingTest` | Reopened card UI and API values, preserving card identity |
| Move a card from TODO to Done | `AndroidCardMovementTest` | UI location, same card ID, destination list and absence from source list through API |
| Mark a card complete | `AndroidCardCompletionTest` | UI checkbox and API `dueComplete=true`, `closed=false`, unchanged card ID and list |

The first four scenarios create their test data through UI. Movement and completion prepare their boards, lists and cards through API, then perform the tested action through UI.

Validation checkpoint: the maintainer confirmed all six main scenarios passing together on a real Android device. This is a successful run, not a guarantee against intermittent failures.

The Android profile also includes `AndroidBoardsInteractionTest`, which opens and cancels Quick Add and checks the editor state. This additional scenario is not included in the six-scenario checkpoint above.

Boards created by UI tests are removed through the Trello API after the Appium session is closed.

> Current platform coverage is Android. The Maven project contains an iOS configuration profile, but iOS page objects and tests are not implemented yet.

## Project structure

```text
src/test/java/io/github/denofo/mobilee2e/
├── api/trello/       Trello API client, models, and cleanup
├── app/android/      Android application state management
├── components/android/ Shared Android UI components
├── config/           System property, environment and local.properties resolution
├── device/android/   ADB-based device discovery and device context
├── driver/android/   Appium driver and capabilities factories
├── junit/             JUnit 5 Android test extension
├── pages/android/    Android Page Objects
├── server/            Appium server health check
└── tests/android/     Android E2E scenarios and lifecycle hooks
```

## Requirements

For API and framework tests:

- JDK 17+
- Internet access for the initial Maven/dependency download; the tests themselves use local mocks

For Android E2E tests:

- JDK 17+
- Android SDK and `adb` available on `PATH`
- An authorized Android device or emulator
- Trello Android application installed, with an English UI and logged into the same account as the API token
- Appium available on `PATH` (local device validation used Appium 3)
- UiAutomator2 driver installed for Appium
- Trello API key and token with permission to create and delete test boards
- Internet access for Trello and available board capacity in the test workspace

Check the device connection with:

```bash
adb devices
```

Start Appium in a separate terminal:

```bash
appium
```

## Configuration

For local development, copy the safe template once:

```bash
cp local.properties.example local.properties
```

Add your Trello API key and token to `local.properties`. The file is ignored by Git. After that, every Android test can also be started directly with the green Run button in IntelliJ IDEA; no per-test Run Configuration is required.

In IDEA, import the Maven project and select JDK 17. Run with the repository root as the working directory: `local.properties` is resolved relative to that directory.

Configuration can be supplied through Maven system properties, environment variables, or `local.properties`. The priority is system property, environment variable, then local file.

| Purpose | Maven property | Environment variable | Required |
| --- | --- | --- | --- |
| Trello API key | `trello.apiKey` | `TRELLO_API_KEY` | All six main Android scenarios |
| Trello API token | `trello.apiToken` | `TRELLO_API_TOKEN` | All six main Android scenarios |
| Trello API base URL | `trello.apiBaseUrl` | `TRELLO_API_BASE_URL` | No; defaults to `https://api.trello.com/1/` |
| Appium URL | `appium.url` | `APPIUM_URL` | No; defaults to `http://127.0.0.1:4723` |
| Android device UDID | `android.udid` | `ANDROID_UDID` | No; discovers all authorized connected devices |
| Android system port base | `android.systemPortBase` | `ANDROID_SYSTEM_PORT_BASE` | No; defaults to `8200` |
| Android package | `android.appPackage` | `ANDROID_APP_PACKAGE` | No; defaults to `com.trello` |
| Android activity | `android.appActivity` | `ANDROID_APP_ACTIVITY` | No; defaults to `com.trello.home.HomeActivity` |

Example:

```bash
export TRELLO_API_KEY="your-api-key"
export TRELLO_API_TOKEN="your-api-token"
export ANDROID_UDID="your-device-udid"
```

Never commit Trello credentials to the repository. Store them in `local.properties`, environment variables, or your local secret manager.

## Running tests

Run the default non-device test suite:

```bash
./mvnw test
```

This runs the Trello API client tests against a local mock HTTP server and the Appium server helper tests. It does not require a connected Android device or live Trello credentials.

Run Android E2E tests:

```bash
./mvnw -Pandroid test
```

The Android profile runs all seven test classes whose names start with `Android`, including Quick Add. The Android test extension discovers all authorized connected devices and supplies a test invocation for each device. To target a specific device, set `ANDROID_UDID` or `android.udid` in `local.properties`.

Run only the six main scenarios from the validation checkpoint:

```bash
./mvnw -Pandroid \
  -Dtest=AndroidBoardCreationTest,AndroidListCreationTest,AndroidCardCreationTest,AndroidCardEditingTest,AndroidCardMovementTest,AndroidCardCompletionTest \
  test
```

Run a single Android scenario:

```bash
./mvnw -Pandroid -Dtest=AndroidListCreationTest test
```

## Continuous integration

The GitHub Actions workflow in `.github/workflows/maven.yml` runs on pushes and pull requests to `main`, using Ubuntu and Java 17:

```bash
./mvnw --batch-mode --no-transfer-progress test
```

Maven compiles all test sources, but the default suite executes only the non-device tests: 13 mocked Trello API client tests and two Appium URL helper tests. It needs no Trello secrets, connected device or running Appium server. Surefire reports are uploaded as the `surefire-reports` artifact unless the workflow is cancelled.

A green CI run does not mean the Android E2E scenarios passed. Device runs are performed separately using the Android profile above.

## Test lifecycle and cleanup

Each Android test:

1. Resolves an Android device through ADB.
2. Verifies that the configured application is installed.
3. Creates an Appium session with UiAutomator2 capabilities.
4. Prepares scenario data through UI or API and performs UI actions through Page Objects.
5. Closes the Appium session.
6. Deletes test boards through the Trello API.

The cleanup is registered as an `AfterEach` action and is executed even when the UI test fails after the board has been created.

## Roadmap

- Extend negative Trello API coverage beyond the existing unauthorized-response check.
- Add iOS driver, Page Objects, and test coverage.
- Add screenshots and page-source artifacts for failed device runs, alongside Surefire reports.

## License

This project is intended as a portfolio and learning project.
