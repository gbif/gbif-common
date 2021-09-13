# gbif-common

The gbif-common shared library provides:
 * Utility classes for files (Compression, Charset, Iterator, Properties, ...)
 * Utility classes for collections (Arrays, compact HashSet, ...)
 * Utility classes for text (String, Email, line, ...)

## To build the project
```
mvn clean install
```

## Note on Jackson 2

This project will shade Jackson 2 into its own artifact.
`gbif-common` is used in projects where other third-party dependencies
are compiled against previous version of Jackson 2.
We decided to shade it to avoid those conflicts knowing
that the final jar will be larger (still < 4 MB).

## Change Log
[Change Log](CHANGELOG.md)

## Documentation
[JavaDocs](https://gbif.github.io/gbif-common/apidocs/)
