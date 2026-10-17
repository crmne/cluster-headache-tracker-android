# Google Play submission

Everything the Play Console asks for, kept next to the code so it changes with
it. The folders follow fastlane's `supply` layout, so `fastlane supply` could
upload them, but nothing here needs fastlane: paste the text into the Play
Console by hand if you prefer.

- `metadata/android/<locale>/`: title, short and full description in English
  (`en-US`) and German (`de-DE`).
- `metadata/android/en-US/images/`: the 512×512 icon, the 1024×500 feature
  graphic, phone screenshots (1080×2424) and 10-inch tablet screenshots
  (2560×1600). They show the real app on an emulator, taken by
  `scripts/store-screenshots.py` from a local server holding a synthetic
  account. Never take them from a real account.

## Before each release

1. Reset the reviewers' account on production, from the Rails app:
   `bin/kamal app exec -i --reuse "bin/rails demo:reset"`, and give it the
   password entered under App content › App access (the account is
   `appreview`). Reviewers may delete it: resetting recreates it.
2. Commit `packaging/release-notes/vX.Y.Z.md`, tag `vX.Y.Z` and push the tag.
   The release workflow signs the APK (GitHub) and the bundle (Google Play)
   with the same key and keeps the bundle as a workflow artifact,
   `cluster-headache-tracker-X.Y.Z-aab`. Upload that `.aab` in the Play
   Console.

## Signing

Let Google Play sign with the key the GitHub APKs already use ("Use my own
key" when enrolling in Play App Signing, exporting it with Google's PEPK
tool), so people can move between the GitHub APK and Google Play without
uninstalling. With a key Google generates, a GitHub install can't update from
Google Play.

## Store settings

- App name: Cluster Headache Tracker. Default language: English (United
  States); add German.
- App or game: App. Free. Category: Medical. Tags: the closest to health
  tracking and medication.
- Contact email: hello@clusterheadachetracker.com. Website:
  https://clusterheadachetracker.com. Privacy policy:
  https://clusterheadachetracker.com/privacy-policy.
- Ads: the app contains no ads.

## App content

- **App access**: all functionality needs an account. Give the `appreview`
  username and its password, and add: "Usernames and passwords only, no email.
  The account holds made-up data. Account › Delete account deletes it."
- **Target audience**: 18 and over (the privacy policy says the service isn't
  meant for children under 16; choosing adults only keeps the Families policy
  out of it). Not designed to appeal to children.
- **Content rating** (IARC): category "All other app types". Answer no to
  violence, sexuality, language, controlled substances, gambling, user
  interaction or sharing of content between users (share links are read-only
  reports a person sends deliberately), location sharing and digital
  purchases. Expect Everyone / PEGI 3 / USK 0.
- **Health apps**: declare it. Pick the categories that describe a symptom
  and medication diary (medication and treatment management, and disease
  or condition management if offered), not clinical decision support. It
  isn't a medical device and isn't regulated as one; the description says so.
- **Financial features, government, news, COVID-19**: none.
- **Data safety**: see below.
- **Account deletion**: in-app (Account › Delete account) and on the web at
  https://clusterheadachetracker.com/delete-account.

## Data safety

- Does the app collect or share user data? Yes, collects. Shares: no. Our
  host, error tracker and email provider (Forward Email, for sign-up and
  feedback notices) process data on our behalf, which Google doesn't count as
  sharing.
- Is all collected data encrypted in transit? Yes (HTTPS only; release builds
  allow no cleartext traffic).
- Can people ask for their data to be deleted? Yes: in the app, on the website,
  or by email.

| Category › type | Collected | Required? | Purposes |
|---|---|---|---|
| Health and fitness › Health info | Yes | Required (it's what the app is for) | App functionality |
| Personal info › User IDs | Yes (the username) | Required | App functionality, Account management |
| Photos and videos › Photos | Yes | Optional | App functionality |
| App activity › Other user-generated content | Yes (notes, feedback answers) | Optional | App functionality |
| App activity › App interactions | Yes (page views, self-hosted Plausible, no cookies, no stored IP) | Required | Analytics |
| App info and performance › Diagnostics | Yes (server error reports naming the account by number) | Required | App functionality |

Not collected: location, contacts, messages, email address, name, phone
number, financial info, web history, device or advertising IDs, files, audio,
calendar, crash logs from the app (it has no crash SDK; reports Google
collects itself don't count).

Keep this in step with the Rails app's privacy policy and the iOS app's
privacy answers (`cluster-headache-tracker-ios`, `fastlane/README.md`).

## Policies worth knowing

- Updates: Google Play builds report `store=play` in their user agent, and the
  web app never offers them the GitHub APK (Device and Network Abuse policy).
- Payments: the GitHub Sponsors links are hidden in Google Play builds.
- New personal developer accounts must run a closed test with at least 12
  testers for 14 days before they can publish to production.
