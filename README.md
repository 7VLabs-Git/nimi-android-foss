# Nimi — Android UI prototype

**Nimi is a working name, not a final brand decision.** This project is a visual and interaction prototype for a women’s rights, awareness, support and digital-evidence application in India.

## What is implemented

- Four-tab Android interface: Home, Learn, Evidence and Support.
- Editorial visual style: warm ivory, plum, muted rose, lilac and sage; serif display headings with sans-serif utility text.
- Interactive learning scenario with selectable responses.
- Quick Leave switches to a neutral in-app reading screen.
- File picker for chat exports and images.
- Streaming SHA-256 hashing on the device. The app does not upload or make a second copy of the selected file.
- User-entered public profile/post URLs are stored as text for the current session; the app does not fetch remote page contents.
- Local PDF export of the evidence inventory.
- Emergency and cyber-fraud contact actions open the phone dialler, rather than placing calls automatically.

## What is deliberately not implemented

This is not yet a production forensic application or legal service. The prototype does **not**:

- extract EXIF, message headers, platform-internal sender IDs or a verified event timeline;
- guarantee that a selected file is original, authentic or complete;
- classify conduct into BNS or IT Act offences;
- generate a police complaint, Section 63 certificate or a claim of BSA compliance;
- submit anything to the National Cyber Crime Reporting Portal or to police;
- provide automated AI legal advice or a verified state-wise support directory;
- persist evidence records in encrypted storage after the app closes.

The PDF is explicitly labelled an **evidence inventory**, not a legal certificate. A SHA-256 digest is useful for later integrity comparisons, but it does not alone establish authenticity or legal admissibility. BSA Section 63 has additional statutory conditions and certificate requirements. Have Indian legal counsel and a qualified digital-forensics practitioner review any production workflow before describing it as compliant.

## Privacy and safety design requirements before a public release

1. Keep processing on-device by default. Do not upload evidence to an AI provider, cloud bucket, analytics service or crash-reporting payload without a separate, informed opt-in.
2. Do not preview, transmit or ask users to forward intimate images. Do not encourage anyone to contact a harasser, negotiate, or delay getting help to collect material. Special safeguards are required for minors; never ask a minor to download, retain or forward sexual imagery.
3. Use Android's Storage Access Framework and least-privilege access. Add encrypted local storage, key management, deletion controls, app-lock options and secure backup/export only after a threat model and usability review.
4. Make missing metadata explicit. Never infer timestamps, sender identities, EXIF fields or message authorship. Record where each field came from and whether it was user-supplied, extracted or unavailable.
5. Separate file integrity, provenance, chain of custody and legal admissibility. Record every action, tool version, time source and export step. Obtain specialist review for any Section 63 certificate workflow.
6. Treat quick-exit as a convenience only. This sample does not remove the task from Android Recents, suppress notifications or guarantee concealment from someone with access to the device.
7. Verify helplines and local services by jurisdiction and review date. This prototype surfaces 112 for emergencies, 181 for the National Women Helpline, and 1930 for cyber financial fraud; 1930 is not a general women’s safety helpline. Do not hardcode local-only numbers as nationwide services.
8. Publish legal explainers with jurisdiction, statute/section, official source link, last-reviewed date, plain-language caveats and a route to qualified human help.

## Build setup

- Android Studio with Android Gradle Plugin 9.3.1 support.
- JDK 17.
- Gradle 9.5.0.
- Kotlin 2.4.21 Compose compiler plugin.
- Compose BOM 2026.09.00.
- `compileSdk = 37`, `targetSdk = 36`, `minSdk = 26`.

Open the `NimiAndroid` folder in Android Studio and sync Gradle. The Gradle wrapper scripts are not bundled in this starter archive. If Gradle is installed locally, generate them from the project root with `gradle wrapper --gradle-version 9.6.0`, then use the generated `gradlew` wrapper.

## Suggested next engineering milestones

1. Product discovery with women, college students, support organisations, lawyers and forensic practitioners. Test wording, discretion, and emergency discoverability.
2. Design and threat-model the evidence vault: encrypted index, URI permission lifecycle, tamper-evident audit trail, optional device-bound encryption and explicit export confirmation.
3. Add parsers for user-exported chat formats. Preserve original source files and store parsed data separately with parser version and extraction confidence.
4. Build a reviewed legal content catalogue sourced from India Code and official government portals. Start with citations and guided information rather than automated offence conclusions.
5. Build a verified, location-aware support directory and a transparent reporting handoff. Never imply that the app itself has filed a complaint unless a confirmed integration exists.
6. Conduct accessibility, privacy, security and legal reviews before any real victim evidence is used.

## Project structure

```text
NimiAndroid/
  app/
    src/main/AndroidManifest.xml
    src/main/java/com/nimi/app/MainActivity.kt
    src/main/res/values/styles.xml
    build.gradle.kts
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
```
