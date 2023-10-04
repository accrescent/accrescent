# Contributing Guidelines

Thank you for your interest in contributing to Accrescent! Here are a few
resources to help you get started.

The [public roadmap] outlines the project's current plans and priorities. If you
want to help but aren't sure what needs to be worked on, this is a great place
to look.

For now, the [#accrescent:matrix.org] Matrix room is the best place to get help
with development. [@lberrymage:matrix.org] is the lead developer and you are
welcome to DM him as well.

There currently isn't any documentation for how Accrescent is structured or how it
functions under the hood, but some will be added in the future.

The project's coding style and conventions are outlined below. Please check your
branch against them before making a PR to expeditide the review process.

## Building

Accrescent uses [Gradle dependency verification] for its dependencies. If you
run into a build error related to dependency verification and you aren't on
Linux, temporarily delete `gradle/verification-metadata.xml` and try again. Only
the Linux version of aapt2 is represented in the verification metadata, so it
won't verify if you're on another OS.

You currently need to use Android Studio Beta to build Accrescent because it
uses a beta version of the Android Gradle Plugin.

## Code style

- Wrap lines at 100 columns. This isn't a hard limit, but will be enforced
  unless wrapping a line looks uglier than extending it by a few columns.
- Don't use glob imports. You can have Android Studio create single name imports
  automatically by going to `File -> Settings -> Editor -> Code Style -> Kotlin`
  and enabling "Use single name import."
- Format via Android Studio's formatter. You can do this by navigating to `Code
  -> Reformat Code` and checking "Rearrange entries" and "Cleanup code" before
  clicking "Run."

## Code conventions

- Use Kotlin for all Accrescent code. Java will not be accepted.
- Use [Jetpack Compose] for UI where possible. UI using XML Views will only be
  accepted if no viable Compose alternative exists. Since Jetpack Compose is
  still fairly new, it's okay to use alpha/beta/rc libraries if necessary
  (provided they meet the other contributing guidelines).
- Avoid unnecessary third-party libraries. When a third-party library is needed,
  it should be well-maintained, widely used, and ideally written in a memory
  safe language.
- Prefer higher-level languages like Kotlin where possible. If lower-level code
  is required for performance or other reasons, use Rust if feasible, but keep
  in mind that Accrescent is a high-level application and shouldn't require much
  low-level code, much less memory unsafe code.

## Bug reports, feature suggestions, and questions

Please use each repository's respective issue tracker for bug reports and
suggestions, but feel free to discuss them in the appropriate Matrix room(s)
first if you like.

If your issue pertains to multiple repositories, none of them (e.g.
infrastructure), or you don't know where it would be most appropriate, create it
in the [meta repository]. Your issue will be transferred to the appropriate
repository if necessary.

The [#accrescent:matrix.org] Matrix room is the preferred medium for questions.
Questions in the form of GitHub issues won't be ignored, but they will naturally
have longer response times than on Matrix and the answer won't be visible to as
many people.

## Vulnerability reports

Report all vulnerabilities in accordance with Accrescent's [security policy].

[#accrescent:matrix.org]: https://matrix.to/#/#accrescent:matrix.org
[@lberrymage:matrix.org]: https://matrix.to/#/@lberrymage:matrix.org
[Jetpack Compose]: https://developer.android.com/jetpack/compose
[Android Jetpack]: https://developer.android.com/jetpack/
[Gradle dependency verification]: https://docs.gradle.org/current/userguide/dependency_verification.html
[meta repository]: https://github.com/accrescent/meta
[public roadmap]: https://github.com/orgs/accrescent/projects/4/views/2
[security policy]: SECURITY.md
