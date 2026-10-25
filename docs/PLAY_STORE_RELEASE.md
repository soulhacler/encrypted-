# Releasing ENCRYPTED on Google Play

A step-by-step checklist for the first public release.

## 1. Before the first upload

- [ ] **Choose the final application ID.** It is `io.github.soulhacler.encrypted` in `app/build.gradle.kts` (`applicationId`). It can never change after the first upload, so if you'd rather publish under your own domain (for example `uk.co.yourcompany.encrypted`), change it now. You can leave the Kotlin `namespace` as it is.
- [ ] Set `versionCode` (an integer that must increase with every upload) and `versionName` (shown to users) in `app/build.gradle.kts`.
- [ ] Play-test the release build on at least one Android 9 device and one recent device, in portrait and landscape, with a phone call during a round.
- [ ] Create a [Google Play Console](https://play.google.com/console) developer account (one-off fee). New personal accounts must run a closed test with testers for a period before production access is granted, so plan for that.

## 2. Create an upload key

Generate a keystore once and keep it, and its passwords, somewhere safe and backed up. **Never commit it.**

```bash
keytool -genkeypair -v \
  -keystore encrypted-upload.jks \
  -alias encrypted-upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

Create `keystore.properties` in the project root. It is git-ignored.

```properties
storeFile=encrypted-upload.jks
storePassword=********
keyAlias=encrypted-upload
keyPassword=********
```

The release build type picks this up automatically. With **Play App Signing** (the default for new apps), Google holds the app signing key and this key is only the *upload* key. A lost upload key can be reset through Play Console support.

## 3. Build the App Bundle

```bash
./gradlew :core:test bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`.

Release builds are minified and resource-shrunk with R8. Install a release build to check nothing was stripped:

```bash
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

## 4. Store listing

| Item | Suggestion |
| --- | --- |
| App name | ENCRYPTED: Say It Out Loud |
| Short description (80 chars) | The phonetic party game: say nonsense out loud and decode the hidden phrase! |
| Full description | Explain the concept with two or three examples, the four modes, offline play, no ads. |
| Category | Games › Word (or Trivia) |
| Tags | Party, Word, Family |
| App icon | 512 × 512 PNG. Export from `ic_launcher_foreground.xml` on the `#0B0F19` background. |
| Feature graphic | 1024 × 500 PNG: the ENCRYPTED wordmark and an example card. |
| Screenshots | At least 2 phone screenshots (4–8 recommended): home, setup, a card, the answer reveal, results with confetti. Add 7" and 10" tablet screenshots if you want tablet visibility. |
| Contact email | Required. |
| Privacy policy URL | Required for all apps. A short page stating that the app collects and shares no data is enough. |

## 5. App content declarations (Policy › App content)

- **Privacy policy**: link from step 4.
- **Ads**: *No, my app does not contain ads.*
- **App access**: all functionality is available without special access.
- **Content rating**: complete the IARC questionnaire. There's no violence, gambling, user interaction or user-generated content; expect PEGI 3 / Everyone.
- **Target audience**: choose your age groups. Picking under-13s brings the app under the Families policy. It already has no ads, analytics or data collection, which makes compliance simple, but a 13+ audience avoids the extra review if families aren't the focus.
- **Data safety**: declare that the app **collects no data** and **shares no data**. It has no network access. Settings and best scores stay on the device and are only included in the user's own Android backup.
- **Government apps / financial features / health**: not applicable.
- **News app**: no.

## 6. Testing tracks and rollout

1. Upload the `.aab` to **Internal testing** first, add yourself and friends as testers, and install from the Play Store link.
2. Fix anything found, increase `versionCode`, and upload again.
3. Promote to **Closed testing** (required for new personal accounts before production), then **Production**.
4. Use a **staged rollout** (for example 20% → 50% → 100%) and watch *Android vitals* for crashes and ANRs.

## 7. Every update

- [ ] Increase `versionCode` and update `versionName`.
- [ ] `./gradlew :core:test bundleRelease`
- [ ] Write release notes ("What's new").
- [ ] Upload to Internal testing, check, then promote.

## Later: monetisation

When adding paid packs, integrate Google Play Billing, implement `Entitlements` from purchase state, and mark packs `"premium": true` (see README › *Adding content and future premium packs*). You will then need to set up the merchant account and in-app products in Play Console, and update the Data safety form (purchase history).
