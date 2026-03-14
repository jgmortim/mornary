# Mornary

This application is covered in much greater depth at https://mornary.com

[![Website](https://img.shields.io/website?url=https%3A%2F%2Fmornary.com)](https://mornary.com/)
![Java](https://img.shields.io/badge/java-17-blue)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/jgmortim/mornary/total)
![License](https://img.shields.io/github/license/jgmortim/mornary)

```
mornary -e "hello"
.-- .- . ... / -- . . - / . - .- -. --. / .. -- .--. ..- - . / - ---
```

Mornary is an open-source command-line application that encodes arbitrary binary data as Morse code that decodes into
English words. A standard Morse decoder sees ordinary text, while a Mornary decoder reconstructs the hidden binary 
payload. The result is a generative steganographic system where the carrier itself is a Morse signal rather than
a modified file.

The name Mornary is a portmanteau of Morse and Binary.

## Installing

Installers for the latest release can be found https://github.com/jgmortim/mornary/releases or 
https://mornary.com/downloads/. You can also download the source code if you want to build the app yourself.

### Windows

The Windows installer (.msi) will install Mornary under `C:\Program Files\Mornary`. 
Then you can either run the app from that directory or you can add `C:\Program Files\Mornary` to your path 
([instructions](https://www.architectryan.com/2018/03/17/add-to-the-path-on-windows-10/)) and run it anywhere.

## Building

```bash
git clone https://github.com/jgmortim/mornary.git
cd mornary
./gradlew shadowJar
```
### Gradle Commands
* `build` will create a standard JAR
* `shadowJar` will create a fat JAR
* `jpackageWindows` will create an MSI installer using jpackage

## CLI Guide

### Commands

The following is copy of `mornary --help`:
```
Usage: mornary [-hV] [-O=<file>] [-t=<int>] (-e=<text> | -E=<file> | -d=<text> | -D=<file>)
Generative steganography using morse code.
  -h, --help            Show this help message and exit.
  -V, --version         Print version information and exit.
  -e, --encode=<text>   Encodes the supplied text.
  -E, --Encode=<file>   Encodes the supplied file.
  -d, --decode=<text>   Decodes the supplied Mornary-encoded text.
  -D, --Decode=<file>   Decodes the Mornary-encoded contents of the supplied file.
  -O, --Output=<file>   Writes the output to the supplied file. If omitted, output will be printed to the console.
  -t, --threads=<int>   Sets the thread pool size. Only used when encoding files. Defaults to 10.
```

### Examples:

```
// Encoding text
mornary -e "Hello World!"

// Encoding a file
mornary -E input.txt -O output.txt

// Decoding text
mornary -d ".- .. .-. -.-- / --.. / . / .-- . ... - / .-- .- -. - / .-- / -.. . .. - -.-- / ...- / -.. / -.-"

// Decoding a text file
mornary -D input.txt -O output.txt

// View help
mornary -h
```

## Mechanism

This application takes advantage of the fact that binary and Morse code are both expressed with two characters.
`0` and `1` for binary; and `.` and `-` for Morse. The examples below demonstrate encoding ASCII data, but any
arbitrary file can be encoded with Mornary. Although, the effect does work best with small files — it would be
somewhat suspicious if you tried to distribute megabytes of Morse code.

### Encoding

To encode ASCII text, it is first converted to its binary representation. Then the zeros are replaced with dots, and
the ones are replaced with dashes. And finally, spaces and slashes are added to create letter and word breaks, thus
producing valid Morse code.

Take the string `Hello World!` for example.

1. Convert to ASCII binary:
   `010010000110010101101100011011000110111100100000010101110110111101110010011011000110010000100001`
2. Map to dots and dashes:
   `.-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-`
3. Intelligently add spaces and slashes throughout:
   `.-. . -.. . . -- . .-. / - . -- .--. . .- -. / --. .. --. --- - / . .-.. . ...- . -. / - -- . -- .- / --- .-- -. . -.. / -- .- -.. .-- . . -.. / .. -. ... -`

The result is perfectly valid Morse code. If you were to translate this, it would yield:

`REDEEMER TEMPEAN GIGOT ELEVEN TMEMA OWNED MADWEED INST`

By ensuring that the resulting Morse code translates to valid English words (for the most part), we better sell the
effect that this is Morse code.

### Decoding

Decoding follows the exact opposite operation. First, remove the spaces and slashes, then dots are replaced with zeros and the
dashes are replaced with ones. Then convert the resulting binary string to ASCII.

Given the output from the above example:

`.-. . -.. . . -- . .-. / - . -- .--. . .- -. / --. .. --. --- - / . .-.. . ...- . -. / - -- . -- .- / --- .-- -. . -.. / -- .- -.. .-- . . -.. / .. -. ... -`

1. Remove spaces and slashes:
   `.-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-`
2. Map to ones and zeros:
   `010010000110010101101100011011000110111100100000010101110110111101110010011011000110010000100001`
3. Convert to ASCII: `Hello World!`

## Credits

* 5,000 word English Dictionary sourced from https://github.com/MichaelWehar/Public-Domain-Word-Lists/blob/master/5000-more-common.txt
  * public domain
* Huge English dictionary sourced from https://github.com/dwyl/english-words/blob/master/words_alpha.txt
  * Unlicense; Copyright infochimps
* n-gram dictionaries sourced from https://github.com/orgtre/google-books-ngram-frequency
  * CC BY 3.0; orgtre