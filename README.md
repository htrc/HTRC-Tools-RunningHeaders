[![Scala CI](https://github.com/htrc/HTRC-Tools-RunningHeaders/actions/workflows/ci.yml/badge.svg)](https://github.com/htrc/HTRC-Tools-RunningHeaders/actions/workflows/ci.yml)
[![codecov](https://codecov.io/github/htrc/HTRC-Tools-RunningHeaders/branch/develop/graph/badge.svg?token=EL908DMVWS)](https://codecov.io/github/htrc/HTRC-Tools-RunningHeaders)
[![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/htrc/HTRC-Tools-RunningHeaders?include_prereleases&sort=semver)](https://github.com/htrc/HTRC-Tools-RunningHeaders/releases/latest)

# HTRC-Tools-RunningHeaders
Utility library that can be used for performing header/body/footer identification over a set of pages from a volume. The algorithm uses fuzzy string matching (using the Levenshtein distance metric) to cluster similar lines across pages within a configurable window. The algorithm is optimized to use as little memory-copying as possible, both for performance reasons and to be able to process large amounts of text.

# Build
* To generate a package that can be referenced from other projects:  
  `sbt test package`  
  then find the result in `target/scala-2.13/` (or similar) folder.

# Usage

## SBT  
`libraryDependencies += "org.hathitrust.htrc" %% "running-headers" % VERSION`

## Maven
**Scala 2.12.x**
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>running-headers_2.12</artifactId>
    <version>VERSION</version>
</dependency>
```

**Scala 2.13.x**
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>running-headers_2.13</artifactId>
    <version>VERSION</version>
</dependency>
```
