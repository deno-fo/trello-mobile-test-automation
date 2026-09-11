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
- Live Trello API integration tests for card creation, editing, moving and deletion
- Negative API checks for invalid list IDs and missing authorization

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

Validation checkpoint (2026-09-11): the maintainer reported three consecutive runs of the six main scenarios on a real Android device, with 18/18 successful scenario executions. This is a bounded local stability check, not a guarantee against intermittent failures or coverage of other devices.

See [Test plan](docs/TEST_PLAN.md) for preparation, UI/API assertions, validation evidence and scope limits.

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
├── diagnostics/      Failure screenshot and page-source collection
├── driver/android/   Appium driver and capabilities factories
├── junit/             JUnit 5 Android test extension
├── pages/android/    Android Page Objects
├── server/            Appium server health check
└── tests/android/     Android E2E scenarios and lifecycle hooks
```

## Requirements

For API and framework tests:

- JDK 17+
- Internet access for the initial Maven/dependency download
- Live API tests additionally require Trello API credentials and permission to create/delete test boards

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

This runs the Trello API client tests against a local mock HTTP server, Appium server helper tests, and failure-artifact tests. It does not require a connected Android device or live Trello credentials.

Run the live Trello API integration suite:

```bash
./mvnw -Papi test
```

This suite creates temporary private boards, exercises the card lifecycle through the real Trello API, and deletes each board in teardown. It does not require a device or Appium. The live suite is intentionally separate from the default and CI test runs.

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

Maven compiles all test sources, but the default suite executes only the non-device tests: 13 mocked Trello API client tests, two Appium URL helper tests and nine failure-artifact tests (24 total). It needs no Trello secrets, connected device or running Appium server. Surefire reports are uploaded as the `surefire-reports` artifact unless the workflow is cancelled.

A green CI run does not mean the Android E2E scenarios passed. Android runs locally through IDEA or the Maven profile above. GitHub-hosted CI runs only the non-device suite; a self-hosted runner on a personal Mac is not required or part of the current setup.

## Test lifecycle and cleanup

Each Android test:

1. Resolves an Android device through ADB.
2. Verifies that the configured application is installed.
3. Creates an Appium session with UiAutomator2 capabilities.
4. Prepares scenario data through UI or API and performs UI actions through Page Objects.
5. On a test or `@BeforeEach` failure, attempts to save failure artifacts while the session is still available.
6. Closes the Appium session.
7. Deletes test boards through the Trello API.

The cleanup is registered as an `AfterEach` action and is executed even when the UI test fails after the board has been created.

## Android failure artifacts

Tests inheriting `BaseAndroidTest` automatically attempt to save these files on failure:

```text
artifacts/android/<device-test-unique-suffix>/
├── screenshot.png
└── page-source.xml
```

The path is relative to the working directory (normally the repository root) and is printed in the test console. Each failure uses a new directory, so repeated or multi-device runs do not overwrite earlier captures. Screenshot and XML capture are independent; a failed capture logs a warning without replacing the original test failure or preventing normal cleanup.

Successful and aborted tests do not capture artifacts. If setup failed before a driver was created, or the Appium session is already unavailable, screen artifacts may be absent. Failures arising only during teardown/API cleanup are not captured: the UI session may already have been closed.

Artifact directories are ignored by Git. They can contain private board/card content; inspect them before sharing. They are not automatically uploaded by the non-device CI workflow.

Local tests exercise file saving, partial failures, unique paths and the real JUnit lifecycle using a device-free fixture. On 2026-09-11, a deliberate assertion failure in `AndroidBoardCreationTest` also verified actual Appium capture on a connected phone: the PNG opened and showed the created board, the XML parsed successfully, and the original assertion message remained the reported failure. The temporary assertion is not part of the test scenario.

## Scope and limitations

- Android and English UI only; the iOS profile has no implemented scenarios.
- Device runs depend on a live Trello account, network connectivity, app version and available workspace capacity.
- The 18/18 checkpoint covers six scenarios on the maintainer's device, not all seven Android classes, a device matrix or a long-running reliability study.
- Completion and movement are separate operations; completion is not card archiving.
- Tests are functional checks, not performance, security or full Trello regression coverage.
- Device artifacts remain local and are not automatically uploaded to GitHub.

## Roadmap

- Extend negative Trello API coverage beyond the existing unauthorized-response check.
- Add iOS driver, Page Objects, and test coverage.
- Broaden local device/version validation when additional test hardware is available.

## License

This project is intended as a portfolio and learning project.
