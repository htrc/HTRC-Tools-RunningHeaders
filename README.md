# HTRC-Tools-RunningHeaders
Utility library that can be used for performing header/body/footer identification over a set of pages from a volume. The algorithm uses fuzzy string matching (using the Levenshtein distance metric) to cluster similar lines across pages within a configurable window. The algorithm is optimized to use as little memory-copying as possible, both for performance reasons and to be able to process large amounts of text.

# Build
* To generate a package that can be referenced from other projects:
  `sbt test package`  
  then find the result in `target/scala-2.11/` (or similar) folder.

# Usage

To use via Maven:
```
<dependency>
    <groupId>org.hathitrust.htrc</groupId>
    <artifactId>running-headers_2.11</artifactId>
    <version>0.7-SNAPSHOT</version>
</dependency>
```

To use via SBT:
`libraryDependencies += "org.hathitrust.htrc" %% "running-headers" % "0.7-SNAPSHOT"`
