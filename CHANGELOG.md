# Change Log

## 0.35 (2017-06-16)

[Complete Changelog](https://github.com/gbif/gbif-common/compare/gbif-common-0.34...gbif-common-0.35)

* Replaced internal implementation of `TabularDataFileReader` from SuperCsv to Jackson CSV.
* `org.gbif.utils.file.tabular.TabularFiles` methods are now accepting a `java.io.Reader`. It allows this class not to have to deal with charsets.