# Change Log

## 0.36 (2017-08-30)
* Added `TabularFileMetadataExtractor` (see [Issue #6](https://github.com/gbif/gbif-common/issues/6)).
* Fixed [Issue #9](https://github.com/gbif/gbif-common/issues/9): "Tabular lines with first column empty mistakenly skipped"

[Complete Changelog](https://github.com/gbif/gbif-common/compare/gbif-common-0.35...gbif-common-0.36)

## 0.35 (2017-06-16)
* Replaced internal implementation of `TabularDataFileReader` from SuperCsv to Jackson CSV.
* [TabularFiles](http://gbif.github.io/gbif-common/apidocs/org/gbif/utils/file/tabular/TabularFiles.html) methods are now accepting a `java.io.Reader`. It allows this class not to have to deal with charsets.

[Complete Changelog](https://github.com/gbif/gbif-common/compare/gbif-common-0.34...gbif-common-0.35)