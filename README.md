<div align="center">

<img src=".icon-round.png" alt="Accrescent" width="144" height="144">

</div>

<div align="center">

[![Build](https://github.com/accrescent/accrescent/actions/workflows/build.yaml/badge.svg)](https://github.com/accrescent/accrescent/actions/workflows/build.yaml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=accrescent_accrescent&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=accrescent_accrescent)
[![Weblate](https://hosted.weblate.org/widgets/accrescent/-/client/svg-badge.svg)](https://hosted.weblate.org/engage/accrescent/)

# Accrescent

A novel Android app store focused on security, privacy, and usability.

</div>

**Note: Accrescent is not yet ready for production usage. Consider all software
and services run by this organization as in a "pre-alpha" stage and fit only for
development and preliminary testing. The apps Accrescent contains are there only
for testing purposes and will be removed (except for Accrescent itself) when
Accrescent enters public alpha.**

## About

Accrescent is a private and secure Android app store built with modern features
in mind. It aims to provide a developer-friendly platform and pleasant user
experience while enforcing modern security and privacy practices and offering
robust validity guarantees for installed apps. It does this through the
following features:

- App signing key pinning
- Signed repository metadata
- Automatic, unprivileged, unattended updates
- First-class support for split APKs
- No remote APK signing
- Meaningful quality control for submitted apps

...and more. See the [features page] on the website for details.

Accrescent currently runs on Android 12 and up.

Contributions are welcome! If you're interested in helping out, be sure to check
out the [contributing guidelines] for tips on getting started.

### Signing certificate hash

Accrescent's SHA-256 signing certificate hash is as follows:

```
067a40c4193aad51ac87f9ddfdebb15e24a1850babfa4821c28c5c25c3fdc071
```

Be sure to check it against the hashes on [our website] and [Twitter] to verify
its legitimacy.

## Translations

Accrescent has a project on [Hosted Weblate] if you would like to help
translate.

## Trademark

The name "Accrescent" and the Accrescent logo are common law trademarks owned by
the Accrescent project. All other parties are forbidden from using Accrescent's
name and branding, as are derivatives of Accrescent. Derivatives include, but
are not limited to forks and unofficial builds.

[contributing guidelines]: CONTRIBUTING.md
[our website]: https://accrescent.app/faq#verifying
[features page]: https://accrescent.app/features
[Hosted Weblate]: https://hosted.weblate.org/engage/accrescent/
[Play App Signing]: https://developer.android.com/studio/publish/app-signing#app-signing-google-play
[Twitter]: https://twitter.com/accrescentapp/status/1555439120519835650
