# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - Upcoming

### Added

* Progress printing now includes time elapsed and estimated time remaining
* low memory encoding flag

### Changed

* Now using a weighted dictionary approach for finding Morse code words. Each dictionary has a multiplier that can be
  applied when scoring words so that certain dictionaries can be favored over others.
* Dictionaries are loaded into memory as prefix tree for greatly improved processing times
* Now using n-gram dictionaries to improve logical chaining of words
* Number of "matching word to find before selection" now scales with the number of work units
* More elaborate word scoring
  * New scoring factors include the dictionary the word comes from and whether it's an acronym
* Progress printing limited to no more than one print per second
* Upgraded Java from version `17` to `21`
* Upgraded Gradle from version `8.13` to `8.14.3`

### Dependency Updates

* lombok from `1.18.42` to `1.18.44`
* Removed morse-code-translator dependency

## [1.0.0-beta.1] - 2026-03-08

### Added

* Progress percentage now prints to the console when doing a file-to-file encode or decode

### Changed

* Now using a two dictionary approach for finding Morse code words. If no suitable words are found in the common
  word dictionary, then the rare word dictionary is searched. This has multiple benefits:
  * Common words are favored over rare words
  * Faster than only searching the rare word dictionary
  * Fewer single letter "words" in the output than when using the common word dictionary
* Scoring for potential words now reduces the score of a word if it was selected as one of the previous 3 words. This
  reduces the likelihood of repeat words.
* Improved CLI experience with better help message and argument descriptions

### Dependency Upgrades

* jackson-databind from `2.17.0` to `2.21.1`

## [1.0.0-alpha.2] - 2026-03-03

### Added

* Text input encoding and decoding can now output to a file if specified
* Text input decoding handling for non-text output
* App icon

### Changed

* Much more robust file decode method to add support for large files
  * Utilizes input and output streaming and limits the amount of input/output data that exists in memory at a time

## [1.0.0-alpha.1] - 2026-02-26

Initial working concept

[1.0.0]: https://github.com/jgmortim/mornary/compare/v1.0.0-beta.1...v1.0.0
[1.0.0-beta.1]: https://github.com/jgmortim/mornary/compare/v1.0.0-alpha.2...v1.0.0-beta.1
[1.0.0-alpha.2]: https://github.com/jgmortim/mornary/compare/v1.0.0-alpha.1...v1.0.0-alpha.2
[1.0.0-alpha.1]: https://github.com/jgmortim/mornary/releases/tag/v1.0.0-alpha.1
