unEXEPACK
=========

[![Maven Central](https://img.shields.io/maven-central/v/io.github.albertus82/unexepack)](https://mvnrepository.com/artifact/io.github.albertus82/unexepack)
[![Build](https://github.com/albertus82/unEXEPACK/actions/workflows/build.yml/badge.svg)](https://github.com/albertus82/unEXEPACK/actions)
[![Known Vulnerabilities](https://snyk.io/test/github/albertus82/unEXEPACK/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/albertus82/unEXEPACK?targetFile=pom.xml)

## Information

Unpacker for Microsoft EXEPACK utility compressor.

### EXEPACK layout

```
+--------------------------+
|      EXEPACK HEADER      |
+--------------------------+
|      UNPACKER  STUB      |
+--------------------------+
|       Error string       |
| "Packed file is corrupt" |
+--------------------------+
|     RELOCATION TABLE     |
+--------------------------+
```

### EXEPACK header

Header format:

```
+ 0x00 : REAL_IP       [WORD]  // Original initial IP value
+ 0x02 : REAL_CS       [WORD]  // Original initial (relative) CS value
+ 0x04 : MEM_START     [WORD]  // Start of executable in memory : not used by the unpacker
+ 0x06 : EXEPACK_SIZE  [WORD]  // sizeof (EXEPACK HEADER) + unpacker stub length + strlen(ERROR_STRING) + relocation table length
+ 0x08 : REAL_SP       [WORD]  // Original initial SP value
+ 0x0A : REAL_SS       [WORD]  // Original initial (relative) SS value
+ 0x0C : DEST_LEN      [WORD]  // Unpacked data length (in paragraphs)
+ 0x0E : SKIP_LEN      [WORD]  // field only present in specific version of EXEPACK : not used by the unpacker
+ 0x10 : SIGNATURE     [WORD]  // Magic number "RB"
```

### Algorithm

EXEPACK employs a fairly basic run-length encoding, commands are encoded on bits 1-7 (mask `0xFE`).

* Command `0xB0`, write `LENGTH` bytes with `VALUE`.

```
[LENGTH (WORD)][VALUE (BYTE)]
```

* Command `0xB2`, copy the next `LENGTH` bytes

```
[LENGTH (WORD)][BYTES ((BYTE) * LENGTH)]
```

### Relocation table

Relocation table is optimized too, for each segment (0-15), there is the following layout, where `entry` is relative to the start of the exe in memory.

```
+ 0x00 : NB_ENTRIES    [WORD]
+ 0x02 : ENTRY         [WORD] * NB_ENTRIES
```

## Usage

### Maven Central

```xml
<dependency>
    <groupId>io.github.albertus82</groupId>
    <artifactId>unexepack</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Command line

```
unexepack <EXEPACK_file> [OUTPUT_FILE]
    default output file is "unpacked"
```

`EXEPACK_file` : Specifies the input file to unpack

`OUTPUT_FILE` : Specifies the output file to which the unpacked executable results will be written to. Defaults to 'unpacked'.

## EXEPACK list

If you are wondering if an game/executable is using EXEPACK, a list of EXEPACK executable is available [here](http://w4kfu.github.io/unEXEPACK/files/exepack_list.html).

This list is based on the awesome [Total DOS Collection Release 14](https://archive.org/details/Total_DOS_Collection_Release_14) archive, thanks to the authors!

## Resources

* [File Format](http://www.shikadi.net/moddingwiki/Microsoft_EXEPACK#File_Format)
* [unexepack from openKB](https://sourceforge.net/p/openkb/code/ci/master/tree/src/tools/unexepack.c)
