# Mornary

Mornary is a generative steganography application that disguises ASCII text and binary file data as Morse code.

The name Mornary is a portmanteau of Morse and Binary.

## Mechanism

This application takes advantage of the fact that binary and Morse code are both expressed with two characters.
`0` and `1` for binary; and `.` and `-` for Morse.

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
   `. / - . . - .... / -- . . - / . / -. / -- .- -.. . / -- .- -.. . / --. / --- -. / . - .... . .-. / - .- -- . / --. / - --- .-- -. / .-. .- -. --. . / .--. .-.. . .- ... . / -`

The result is perfectly valid Morse code. If you were to translate this, it would yield:

`MEET E N MADE MADE G ON ETHER TAME G TOWN RANGE PLEASE T`

By ensuring that the resulting Morse code translates to valid English words (for the most part), we better sell the
effect that this is morse code.

### Decoding

Decoding follows the exact opposite operation. First, remove the spaces and slashes, then dots are replaced with zeros and the
dashes are replaced with ones. Then convert the resulting binary string to ASCII.

Given the output from the above example: 

`. / - . . - .... / -- . . - / . / -. / -- .- -.. . / -- .- -.. . / --. / --- -. / . - .... . .-. / - .- -- . / --. / - --- .-- -. / .-. .- -. --. . / .--. .-.. . .- ... . / -`

1. Remove spaces and slashes: 
   `.-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-`
2. Map to ones and zeros:
   `010010000110010101101100011011000110111100100000010101110110111101110010011011000110010000100001`
3. Convert to ASCII: `Hello World!` 

## Usage Examples:

* Encoding text: `mornary /e "Hello World!"`
* Encoding a file: `mornary /E "input.txt"`
* Decoding text: `mornary /d ". / - . . - .... / -- . . - / . / -. / -- .- -.. . / -- .- -.. . / --. / --- -. / . - .... . .-. / - .- -- . / --. / - --- .-- -. / .-. .- -. --. . / .--. .-.. . .- ... . / -"`
* Decoding a text file: `mornary /D "input.txt"`

## Credits

* 5,000 word English Dictionary sourced from https://github.com/MichaelWehar/Public-Domain-Word-Lists/blob/master/5000-more-common.txt
  * public domain
* Huge English dictionary sourced from https://github.com/dwyl/english-words/blob/master/words_alpha.txt
  * Copyright infochimps