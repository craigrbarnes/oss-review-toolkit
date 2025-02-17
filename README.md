![OSS Review Toolkit Logo](./logos/ort.png)

&nbsp;

| Linux (OpenJDK 10)             | Windows (Oracle JDK 9)          | JitPack (OpenJDK 8)             |
| :----------------------------- | :------------------------------ | :------------------------------ |
| [![Linux build status][1]][2]  | [![Windows build status][3]][4] | [![JitPack build status][5]][6] |
| [![Linux code coverage][7]][8] |                                 |                                 |

| Interact with us!              |
| :----------------------------- |
| [![ort-talk][9]][10]            |

[1]: https://travis-ci.com/heremaps/oss-review-toolkit.svg?branch=master
[2]: https://travis-ci.com/heremaps/oss-review-toolkit
[3]: https://ci.appveyor.com/api/projects/status/hbc1mn5hpo9a4hcq/branch/master?svg=true
[4]: https://ci.appveyor.com/project/heremaps/oss-review-toolkit/branch/master
[5]: https://jitpack.io/v/heremaps/oss-review-toolkit.svg
[6]: https://jitpack.io/#heremaps/oss-review-toolkit
[7]: https://codecov.io/gh/heremaps/oss-review-toolkit/branch/master/graph/badge.svg
[8]: https://codecov.io/gh/heremaps/oss-review-toolkit/
[9]: https://img.shields.io/badge/slack-ort--talk-blue.svg?longCache=true&logo=slack
[10]: https://join.slack.com/t/ort-talk/shared_invite/enQtMzk3MDU5Njk0Njc1LThiNmJmMjc5YWUxZTU4OGI5NmY3YTFlZWM5YTliZmY5ODc0MGMyOWIwYmRiZWFmNGMzOWY2NzVhYTI0NTJkNmY

# Introduction

The OSS Review Toolkit (ORT) assists with verifying Free and Open Source Software license compliance by checking a
project's source code and its dependencies.

From a bird's eye, it works by analyzing the project's build system for dependencies, downloading the source code of the
dependencies, scanning all source code for license information, and summarizing the results.

The different tools that make up ORT are designed as libraries (for programmatic use), with a minimal command line
interface (for scripted use).

The toolkit consists of the following tools:

* [Analyzer](#analyzer) - determines dependencies of a project. Supports multiple package managers and sub-projects. No
  changes to the projects are required.
* [Downloader](#downloader) - fetches the source code referred to by the Analyzer result.
* [Scanner](#scanner) - wraps existing license / copyright scanners to detect findings in local source code directories.
* [Evaluator](#evaluator) - evaluates license findings an created customizable results or follow-up actions using a
   rules DSL based on Kotlin.
* [Reporter](#reporter) - presents results in various formats like visual reports, compliance documents or
  Bill-Of-Materials (BOMs) to easily identify dependencies, licenses, copyrights or policy violations.

The following tools are [planned](https://github.com/heremaps/oss-review-toolkit/projects/1) but not yet available:

* *Advisor* - retrieves security advisories based on the Analyzer result.
* *Documenter* - generates the final outcome of the review process incl. legal conclusions, e.g. annotated
  [SPDX](https://spdx.org/) files that can be included into the distribution.

# Installation

## From binaries

Preliminary binary artifacts for ORT are currently available via [JitPack](https://jitpack.io/#heremaps/oss-review-toolkit).
Please note that due to limitations with the JitPack build environment, the reporter is not able to create the Web App
report.

## From sources

Install the following basic prerequisites:

* Git (any recent version will do).

Then clone this repository. If you intend to run tests, you need to clone with submodules by running
`git clone --recurse-submodules`. If you have already cloned non-recursively, you can initialize submodules afterwards
by running `git submodule update --init --recursive`.

### Build using Docker

Install the following basic prerequisites:

* Docker (and ensure its daemon is running).

Change into the created directory and run `docker/build.sh`.

### Build natively

Install these additional prerequisites:

* OpenJDK 8 or Oracle JDK 8u161 or later (not the JRE as you need the `javac` compiler); also remember to set the
  `JAVA_HOME` environment variable accordingly.
* For the Web App reporter:
    * [Node.js](https://nodejs.org) 8.*
    * [Yarn](https://yarnpkg.com) 1.9.* - 1.17.*

Change into the created directory and run `./gradlew installDist` (on the first run this will bootstrap Gradle and
download all required dependencies).

## Basic usage

ORT can now be run using

    ./cli/build/install/ort/bin/ort --help

Note that if you make any changes to ORT's source code, you would have to regenerate the distribution using either the
[Build using Docker](#build-using-docker) or [Build natively](#build-natively) steps above.

To avoid that, you can also build and run ORT in one go (if you have the prerequisites from the
[Build natively](#build-natively) section installed):

    ./gradlew cli:run --args="--help"

Note that in this case the working directory used by ORT is that of the `cli` project, not directory `gradlew` is
located in (see https://github.com/gradle/gradle/issues/6074).

# Running the tools

Like for building ORT from sources you have the option to run ORT from a Docker image (which comes with all runtime
dependencies) or to run ORT natively (in which case some additional requirements need to be fulfilled).

## Run using Docker

Run `docker/run.sh "<DOCKER_ARGS>" <ORT_ARGS>` where `<DOCKER_ARGS>` are passed to `docker run` (and need to be quoted
if spaces are contained) and `<ORT_ARGS>` are passed to ORT. You typically use `<DOCKER_ARGS>` to mount the project
directory to scan into the running container to let ORT access it, for example:

    docker/run.sh "-v /workspace:/project" --info analyze -f JSON -i /project -o /project/ort/analyzer

## Run natively

First of all, make sure that the locale of your system is set to `en_US.UTF-8` as using other locales might lead to
issues with parsing the output of some external tools.

Then install any missing external command line tools as listed by

    ./cli/build/install/ort/bin/ort requirements

or

    ./gradlew cli:run --args="requirements"

Then run ORT like

    ./cli/build/install/ort/bin/ort --info analyze -f JSON -i /project -o /project/ort/analyzer

or

    ./gradlew cli:run --args="--info analyze -f JSON -i /project -o /project/ort/analyzer"

## Running on CI

A basic ORT pipeline (using the analyzer, scanner and reporter) can easily be run on [Jenkins CI](https://jenkins.io/)
by using the [Jenkinsfile](./Jenkinsfile) in a (declarative) [pipeline](https://jenkins.io/doc/book/pipeline/) job.

## Getting started

Please see [GettingStarted.md](./docs/GettingStarted.md) for an introduction to the individual tools.

## Configuration

Please see [Configuration.md](./docs/Configuration.md) for details about the ORT configuration.

# Details on the tools

<a name="analyzer"></a>

[![Analyzer](./logos/analyzer.png)](./analyzer/src/main/kotlin)

The Analyzer is a Software Composition Analysis (SCA) tool that determines the dependencies of software projects inside
the specified input directory (`-i`). It does so by querying the detected package managers; **no modifications** to your
existing project source code, like applying build system plugins, are necessary for that to work. The tree of transitive
dependencies per project is written out as part of an
[OrtResult](https://github.com/heremaps/oss-review-toolkit/blob/master/model/src/main/kotlin/OrtResult.kt) in YAML (or
JSON, see `-f`) format to a file named `analyzer-result.yml` in the specified output directory (`-o`). The output file
exactly documents the status quo of all package-related meta-data. It can be further processed or manually edited before
passing it to one of the other tools.

Currently, the following package managers are supported:

* [Bower](http://bower.io/) (JavaScript)
* [Bundler](http://bundler.io/) (Ruby)
* [Cargo](https://doc.rust-lang.org/cargo/) (Rust)
* [Conan](https://conan.io/) (C / C++)
* [dep](https://golang.github.io/dep/) (Go)
* [DotNet](https://docs.microsoft.com/en-us/dotnet/core/tools/) (.NET, with currently some [limitations](https://github.com/heremaps/oss-review-toolkit/pull/1303#issue-253860146))
* [Glide](https://glide.sh/) (Go)
* [Godep](https://github.com/tools/godep) (Go)
* [Gradle](https://gradle.org/) (Java)
* [Maven](http://maven.apache.org/) (Java)
* [NPM](https://www.npmjs.com/) (Node.js)
* [NuGet](https://www.nuget.org/) (.NET, with currently some [limitations](https://github.com/heremaps/oss-review-toolkit/pull/1303#issue-253860146))
* [Composer](https://getcomposer.org/) (PHP)
* [PIP](https://pip.pypa.io/) (Python)
* [Pub](https://pub.dev/) (Dart / Flutter)
* [SBT](http://www.scala-sbt.org/) (Scala)
* [Stack](http://haskellstack.org/) (Haskell)
* [Yarn](https://yarnpkg.com/) (Node.js)

<a name="downloader">&nbsp;</a>

[![Downloader](./logos/downloader.png)](./downloader/src/main/kotlin)

Taking an ORT result file with an analyzer result as the input (`-a`), the Downloader retrieves the source code of all
contained packages to the specified output directory (`-o`). The Downloader takes care of things like normalizing URLs
and using the [appropriate VCS tool](./downloader/src/main/kotlin/vcs) to checkout source code from version control.

Currently, the following Version Control Systems are supported:

* [CVS](https://en.wikipedia.org/wiki/Concurrent_Versions_System)
* [Git](https://git-scm.com/)
* [Git-Repo](https://source.android.com/setup/develop/repo)
* [Mercurial](https://www.mercurial-scm.org/)
* [Subversion](https://subversion.apache.org/)

<a name="scanner">&nbsp;</a>

[![Scanner](./logos/scanner.png)](./scanner/src/main/kotlin)

This tool wraps underlying license / copyright scanners with a common API so all supported scanners can be used in the
same way to easily run them and compare their results. If passed an ORT result file with an analyzer result (`-a`), the
Scanner will automatically download the sources of the dependencies via the Downloader and scan them afterwards.

Currently, the following license scanners are supported:

* [Askalono](https://github.com/amzn/askalono)
* [lc](https://github.com/boyter/lc)
* [Licensee](https://github.com/benbalter/licensee)
* [ScanCode](https://github.com/nexB/scancode-toolkit)

For a comparison of some of these, see this [Bachelor Thesis](https://osr.cs.fau.de/2019/08/07/final-thesis-a-comparison-study-of-open-source-license-crawler/).

## Storage Backends

In order to not download or scan any previously scanned sources again, the Scanner can use a storage backend to store
scan results for later reuse.

### Local File Storage

By default the Scanner stores scan results on the local file system in the current user's home directory (i.e.
`~/.ort/scanner/scan-results`) for later reuse. The storage directory can be customized by passing an ORT configuration
file (`-c`) that contains a respective local file storage configuration:

```hocon
ort {
  scanner {
    fileBasedStorage {
      backend {
        localFileStorage {
          directory = "/tmp/ort/scan-results"
        }
      }
    }
  }
}
```

### HTTP Storage

Any HTTP file server can be used to store scan results. Custom headers can be configured to provide authentication
credentials. For example, to use Artifactory to store scan results, use the following configuration:

```hocon
ort {
  scanner {
    fileBasedStorage {
      backend {
        httpFileStorage {
          url = "https://artifactory.domain.com/artifactory/repository/scan-results"
          headers {
            X-JFrog-Art-Api = "api-token"
          }
        }
      }
    }
  }
}
```

### PostgreSQL Storage

To use PostgreSQL to store scan results, use the following configuration:

```hocon
ort {
  scanner {
    postgresStorage {
      url = "jdbc:postgresql://example.com:5444/database"
      schema = "schema"
      username = "username"
      password = "password"
    }
  }
}
```

The scanner creates a table called `scan_results` and stores the data in a
[jsonb](https://www.postgresql.org/docs/current/datatype-json.html) column.

<a name="evaluator">&nbsp;</a>

[![Evaluator](./logos/evaluator.png)](./evaluator/src/main/kotlin)

The Evaluator is used to perform custom license policy checks on scan results. The rules to check against are implemented
via scripting. Currently, Kotlin script with a dedicated DSL is used for that, but support for other scripting languages
can be added as well. See [no_gpl_declared.kts](./evaluator/src/main/resources/rules/no_gpl_declared.kts) for a very
simple example of a rule written in Kotlin script which verifies that no dependencies that declare the GPL are used.

<a name="reporter">&nbsp;</a>

[![Reporter](./logos/reporter.png)](./reporter/src/main/kotlin)

The reporter generates human-readable reports from the scan result file generated by the scanner (`-s`). It is designed
to support multiple output formats.

Currently, the following report formats are supported (reporter names are case-insensitive):

* [CycloneDX](https://cyclonedx.org/) BOM (`-f CycloneDx`)
* [Excel](https://products.office.com/excel) sheet (`-f Excel`)
* [NOTICE](http://www.apache.org/dev/licensing-howto.html) file (`-f Notice`)
* Static HTML (`-f StaticHtml`)
* Web App (`-f WebApp`)

# Development

ORT is written in [Kotlin](https://kotlinlang.org/) and uses [Gradle](https://gradle.org/) as the build system, with
[Kotlin script](https://docs.gradle.org/current/userguide/kotlin_dsl.html) instead of Groovy as the DSL.

When developing on the command line, use the committed [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
to bootstrap Gradle in the configured version and execute any given tasks. The most important tasks for this project are:

| Task        | Purpose                                                           |
| ----------- | ----------------------------------------------------------------- |
| assemble    | Build the JAR artifacts for all projects                          |
| detekt      | Run static code analysis on all projects                          |
| test        | Run unit tests for all projects                                   |
| funTest     | Run functional tests for all projects                             |
| installDist | Build all projects and install the start scripts for distribution |

All contributions need to pass the `detekt`, `test` and `funTest` checks before they can be merged.

For IDE development we recommend the [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/) which
can directly import the Gradle build files. After cloning the project's source code recursively, simply run IDEA and use
the following steps to import the project.

1. From the wizard dialog: Select *Import Project*. 

   From a running IDEA instance: Select *File* -> *New* -> *Project from Existing Sources...*
 
2. Browse to ORT's source code directory and select either the `build.gradle.kts` or the `settings.gradle.kts` file.

3. In the *Import Project from Gradle* dialog select *Use auto-import* and leave all other settings at their defaults.

To set up a basic run configuration for debugging, navigate to `Main.kt` in the `cli` module and look for the
`fun main(args: Array<String>)` function. In the gutter next to it, a green "Play" icon should be displayed. Click on it
and select `Run 'com.here.ort.Main'` to run the entry point, which implicitly creates a run configuration. Double-check
that running ORT without any arguments will simply show the command line help in IDEA's *Run* tool window. Finally, edit
the created run configuration to your needs, e.g. by adding an argument and options to run a specific ORT sub-command.

# License

Copyright (C) 2017-2019 HERE Europe B.V.

See the [LICENSE](./LICENSE) file in the root of this project for license details.
