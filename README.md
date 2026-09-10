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
- Appium server availability check

### Android UI

- Create a board and open it
- Create a list on a board and verify it is visible
- Create a card in a list and verify it through the Trello API
- Edit a card name and description, reopen it, and verify UI and API values (`AndroidCardEditingTest`)
- Move a card from TODO to Done with API setup, UI location verification and API identity/list checks (`AndroidCardMovementTest`; device validation pending)
- Open and cancel Quick Add without creating a card

Boards created by UI tests are removed through the Trello API after the Appium session is closed.

> Current platform coverage is Android. The Maven project contains an iOS configuration profile, but iOS page objects and tests are not implemented yet.

## Project structure

```text
src/test/java/io/github/denofo/mobilee2e/
├── api/trello/       Trello API client, models, and cleanup
├── app/android/      Android application state management
├── components/android/ Shared Android UI components
├── config/           System property and environment variable resolution
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
- Internet access is not required for the mocked Trello API client tests

For Android E2E tests:

- JDK 17+
- Android SDK and `adb` available on `PATH`
- An authorized Android device or emulator
- Trello Android application installed on the device
- Appium 2 available on `PATH`
- UiAutomator2 driver installed for Appium
- Trello API key and token

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

Configuration can be supplied through Maven system properties, environment variables, or `local.properties`. The priority is system property, environment variable, then local file.

| Purpose | Maven property | Environment variable | Required |
| --- | --- | --- | --- |
| Trello API key | `trello.apiKey` | `TRELLO_API_KEY` | Android board/list tests |
| Trello API token | `trello.apiToken` | `TRELLO_API_TOKEN` | Android board/list tests |
| Trello API base URL | `trello.apiBaseUrl` | `TRELLO_API_BASE_URL` | No; defaults to `https://api.trello.com/1/` |
| Appium URL | `appium.url` | `APPIUM_URL` | No; defaults to `http://127.0.0.1:4723` |
| Android device UDID | `android.udid` | `ANDROID_UDID` | No; auto-discovers one device |
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

The Android profile runs tests whose class names start with `Android`. The tests discover a single connected device by default. To target a specific device, set `ANDROID_UDID`.

Run a single Android scenario:

```bash
./mvnw -Pandroid -Dtest=AndroidListCreationTest test
```

## Test lifecycle and cleanup

Each Android test:

1. Resolves an Android device through ADB.
2. Verifies that the configured application is installed.
3. Creates an Appium session with UiAutomator2 capabilities.
4. Performs UI actions through Page Objects.
5. Closes the Appium session.
6. Deletes test boards through the Trello API.

The cleanup is registered as an `AfterEach` action and is executed even when the UI test fails after the board has been created.

## Roadmap

- Validate the Android card movement scenario on a device.
- Add a negative Trello API scenario.
- Add iOS driver, Page Objects, and test coverage.
- Add test reports and failure artifacts for device runs.

## License

This project is intended as a portfolio and learning project.
