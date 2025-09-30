# Mornary

Mornary is a generative steganography application that disguises ASCII text as Morse code.

The name Mornary is a portmanteau of Morse and Binary.

## Mechanism

This application takes advantage of the fact that binary and Morse code are both expressed with two characters.
`0` and `1` for binary; and `.` and `-` for Morse.

### Encoding

To encode ASCII text, it is first converted to its binary representation. Then the zeros are replaced with dots, and 
the ones are replaced with dashes. And finally, spaces are added at sudo-random intervals to establish character
breaks, thus producing valid Morse code.

Take the string `Hello World!` for example. 

1. Convert to ASCII binary: 
   `010010000110010101101100011011000110111100100000010101110110111101110010011011000110010000100001`
2. Map to dots and dashes:
   `.-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-`
3. Add spaces at arbitrary points (being careful to ensure all words are valid Morse encodings):
   `.- . .-. ...-- ..- .-. --.- -.. .- - .- -... --.- -- - .. -.. . .. .- .-. - --.- -.- --- .-- -..- ..- -.-- ...- -.. - .. ..- . ...-`

The result is perfectly valid Morse code. If you were to translate this, it would yield:

`AER3URQDATABQMTIDEIARTQKOWXUYVDTIUEV`

### Decoding

Decoding follows the exact opposite operation. First, remove the spaces, then dots are replaced with zeros and the
dashes are replaced with ones. Then convert the resulting binary string to ASCII.

Given the output from the above example: 

`.- . .-. ...-- ..- .-. --.- -.. .- - .- -... --.- -- - .. -.. . .. .- .-. - --.- -.- --- .-- -..- ..- -.-- ...- -.. - .. ..- . ...-`

1. Remove spaces: 
   `.-..-....--..-.-.--.--...--.--...--.----..-......-.-.---.--.----.---..-..--.--...--..-....-....-`
2. Map to ones and zeros:
   `010010000110010101101100011011000110111100100000010101110110111101110010011011000110010000100001`
3. Convert to ASCII: `Hello World!` 
