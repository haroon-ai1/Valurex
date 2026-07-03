# Valurex

A native Android personal finance app for tracking spending, splitting bills, and staying on top of savings — fully offline, no backend, no accounts, no ads.

Built with Kotlin and Jetpack Compose.

## Features

- **Home dashboard** — running balance, savings bar, and a scrollable transaction feed with inline category icons
- **Smart transaction entry** — lightweight text parser that tokenizes free-form input (e.g. "chai 150") and matches it against user-defined keyword mappings to auto-categorize and auto-type (income/expense) transactions
- **Bill Split** — split a shared bill across multiple people
- **Divide Helper** — quick calculator for dividing an amount into parts
- **Recurring Expenses** — define expenses that repeat on a schedule; a background scheduler checks and auto-generates them on app start
- **Loans** — track money lent or borrowed
- **Wishlist** — save items you're saving up for, with a home-screen widget to track progress
- **Wallet Check-in** — a nightly check-in flow to log/reconcile your actual cash balance
- **Reminders** — configurable daily nudges and a nightly wallet check-in notification, powered by WorkManager
- **Stats** — spending charts and breakdowns (via Vico)
- **Categories** — full CRUD for custom categories and icons
- **Home screen widgets** — Money widget and Wishlist widget, built with Glance
- **Settings & Account** — budget configuration and app preferences

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** — local persistence, no network calls
- **Navigation Compose** — single-activity navigation graph
- **WorkManager** — scheduled reminders and nightly check-ins
- **Glance** — home screen widgets
- **Vico** — charts on the Stats screen
- **Coil** — image loading

Min SDK 26 · Target/Compile SDK 36

## Project Structure

```
app/src/main/java/com/nobody/valurex/
├── data/
│   ├── db/            # Room database, DAOs, entities
│   └── repo/           # Repositories mediating between DAOs and UI
├── notification/       # Reminder workers and scheduling
├── parser/             # Free-text transaction tokenizer/parser
├── scheduler/           # Recurring expense generation
├── ui/
│   ├── components/     # Shared composables
│   ├── navigation/      # NavGraph
│   ├── screen/          # Each app screen
│   ├── theme/           # Color, typography, shape
│   └── viewmodel/        # Per-screen ViewModels
└── widget/             # Glance home screen widgets
```

## Getting Started

1. Clone the repo
2. Open in Android Studio (Ladybug or newer recommended)
3. Let Gradle sync — no API keys or `.env` setup required, the app is fully offline
4. Run on a device/emulator with API 26+

## Building a Signed Release

This repo does not include a signing keystore (as it shouldn't — keep yours out of version control). To build a signed release locally:

1. Generate or place your own `.jks` keystore outside the repo
2. Create a `keystore.properties` file in the project root (already gitignored):
   ```properties
   storeFile=/path/to/your.jks
   storePassword=...
   keyAlias=...
   keyPassword=...
   ```
3. Wire it into `app/build.gradle.kts` under `signingConfigs` if not already set up

## License

MIT — see [LICENSE](LICENSE).
