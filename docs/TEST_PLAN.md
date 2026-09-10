# Android functional test plan

## Objective and approach

Verify six core Trello Android workflows through the UI, using API checks for persisted data where implemented. API preparation keeps movement and completion focused on the action under test; board, list and card creation remain explicitly UI-driven.

This plan describes the existing automated checks, not additional assertions that have yet to be implemented. Setup and commands are in the [README](../README.md#running-tests).

## Preconditions

- Java 17, Android SDK/ADB, Appium and UiAutomator2 are available.
- One authorized device is selected with `android.udid` or `ANDROID_UDID` for a reproducible single-device run.
- Trello is installed, uses English UI and is signed into the account associated with the configured API token.
- The test account can create/delete boards and has sufficient workspace capacity; live Trello connectivity is available.
- Appium is running. No competing IDEA/Maven run or manual interaction is using the phone.
- Any deliberately inserted failure used to validate artifact capture has been removed.

Use test data only. These scenarios create real Trello resources and attempt to delete their test boards during teardown.

## Main scenario matrix

| ID / test class | Preparation and action | UI verification | API verification |
| --- | --- | --- | --- |
| A01 `AndroidBoardCreationTest` | Create and open a uniquely named board through UI | Expected board title and visible Add List control | No explicit result assertion; API used for cleanup |
| A02 `AndroidListCreationTest` | Create board and list through UI | Created list is visible | Matching open list exists on the created board |
| A03 `AndroidCardCreationTest` | Create board, list and card through UI | Created card is visible | Matching card exists in the created list |
| A04 `AndroidCardEditingTest` | Create board/list/card through UI; obtain original card ID through API; edit name and description; close and reopen card | Updated name and description persist; description is read from its editor | Original card ID is used to locate the updated card; name and description match |
| A05 `AndroidCardMovementTest` | Prepare board, TODO/Done lists and card through API; refresh app; move card through UI | Destination board/list location is verified and card name is preserved | Same card ID in Done, matching destination list ID, absent from TODO |
| A06 `AndroidCardCompletionTest` | Prepare board/list/card through API; refresh app; mark card complete through UI | Completion checkbox is checked | Same card ID and list, `dueComplete=true`, `closed=false` |

Every main scenario registers board cleanup, performed after the Appium session closes. Cleanup is attempted on failures as well; unavailable API access or interruption can still leave test data behind. Investigate leftovers before removing anything manually.

## Additional coverage

- `AndroidBoardsInteractionTest`: opens and cancels Quick Add and checks editor state. It is included in the full Android profile, but not the six-scenario stability checkpoint. It does not assert card absence through API.
- Default non-device suite: 13 mocked Trello API client tests, two Appium URL helper tests and nine failure-artifact tests (24 total).
- The mocked API tests validate client behavior, not the availability or correctness of the live Trello service.

## Execution and acceptance

1. Run the default suite with `./mvnw test`.
2. Run the six-class command from the README on the selected device.
3. For a local stability checkpoint, repeat that six-scenario run three times without changing code between runs. Record failures rather than counting only successful reruns.
4. A successful checkpoint means all 18 scenario executions passed with no failed teardown. It does not establish a statistical reliability guarantee.

The complete `./mvnw -Pandroid test` profile includes seven Android classes. Do not report the six-scenario checkpoint as validation of the entire profile.

## Recorded validation

| Checkpoint | Result | Evidence / qualification |
| --- | --- | --- |
| Non-device suite, 2026-09-11 | 24/24 passed | Local clean Maven run during artifact implementation; not a claim about a new remote CI run |
| Six main Android scenarios, recorded 2026-09-11 | 18/18 across three consecutive runs | Maintainer-reported real-device result; individual reports for all three runs are not committed |
| Deliberate failure on phone, 2026-09-11 | Artifact capture confirmed | `AndroidBoardCreationTest` assertion failure produced readable PNG showing the board and parseable XML; original assertion remained the failure reason |

Artifacts and credentials are intentionally not committed. The recorded device results do not imply validation across Android versions, screen sizes or multiple devices.

## Failure investigation

1. Preserve the original exception and the `Failure artifacts directory:` console path.
2. Inspect `screenshot.png` and `page-source.xml` under `artifacts/android/` before changing locators or navigation.
3. Check whether the app was on the expected screen and whether setup, synchronization, UI interaction, assertion or cleanup failed.
4. After understanding the failure, rerun the affected scenario and then the six-scenario group.

Capture is attempted before teardown for test-body and `@BeforeEach` failures. A missing/dead driver may prevent capture; failures arising only during teardown are not captured. Failure to save one artifact does not block the other or replace the original test exception. Successful and aborted tests do not capture artifacts.

PNG/XML may contain private card content. Review before sharing; do not upload credentials or unredacted private data.

## Execution boundaries

- GitHub-hosted CI: compiles test sources and runs only the default non-device suite; uploads Surefire reports.
- Android: local IDEA/Maven execution with a connected device. No personal-Mac self-hosted runner is required.
- Out of scope: implemented iOS tests, non-English UI, performance/security testing, full Trello feature coverage and guaranteed unattended device CI.
