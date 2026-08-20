package io.github.albertus82.unexepack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;

// Translated from the C code at https://github.com/w4kfu/unEXEPACK with some improvements.
// Many thanks to the original authors.

/**
 * Unpacker for Microsoft EXEPACK utility compressor.
 *
 * @see <a href="https://github.com/w4kfu/unEXEPACK">GitHub - w4kfu/unEXEPACK:
 *      unpacker for Microsoft EXEPACK</a>
 * @see <a href=
 *      "https://www.bamsoftware.com/software/exepack/">https://www.bamsoftware.com/software/exepack/</a>
 */
@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UnExepack {

	static final int MAX_INPUT_FILE_SIZE = 8 * 1024 * 1024; // 8 MiB, based on the info available at https://w4kfu.github.io/unEXEPACK/files/exepack_list.html

	/* buf is already reversed, because EXEPACK use backward processing */
	static byte[] unpackData(@NonNull final byte[] packedData, final int unpackedDataSize) throws UnExepackException {
		final String propName = UnExepack.class.getPackage().getName() + ".maxUnpackedSizeBytes";
		final long limit = Long.parseLong(System.getProperty(propName, Integer.toString(MAX_INPUT_FILE_SIZE)));
		if (unpackedDataSize > limit) {
			throw new UnExepackException(String.format("Unpacked data size %d exceeds configured maximum of %d bytes; increase the limit by setting system property '%s' if necessary;", unpackedDataSize, limit, propName));
		}
		final byte[] unpackedData = new byte[unpackedDataSize];
		int i = 0;
		long curUnpackedDataSize = 0;
		while (i < packedData.length && packedData[i] == (byte) 0xFF) { // skip all 0xFF bytes (they're just padding to make the packed exe's size a multiple of 16)
			i++;
		}
		while (true) {
			if (i + 2 >= packedData.length) {
				throw new UnExepackException("Unexpected end of packed data while reading opcode and count");
			}
			final int opcode = Byte.toUnsignedInt(packedData[i++]);
			final long count = Byte.toUnsignedLong(packedData[i]) * 0x100 + Byte.toUnsignedLong(packedData[i + 1]);
			i += 2;
			if ((opcode & 0xFE) == 0xB0) { // fill
				if (i >= packedData.length) {
					throw new UnExepackException("Unexpected end of packed data while reading fill byte");
				}
				final byte fillByte = packedData[i++];
				if (log.isLoggable(Level.FINE)) {
					log.fine(String.format("Fill operation: opcode=0x%02X, count=%d, unpacked offset=0x%X (%d), fill byte=0x%02X", opcode, count, curUnpackedDataSize, curUnpackedDataSize, fillByte));
				}
				if (curUnpackedDataSize + count > Integer.MAX_VALUE) {
					throw new UnExepackException("Unpacked size too large to index arrays safely");
				}
				if (curUnpackedDataSize + count > unpackedDataSize) {
					throw new UnExepackException("Unpacking overflow during fill: tried to write " + count + " bytes at offset " + curUnpackedDataSize + " but max unpacked size is " + unpackedDataSize);
				}
				Arrays.fill(unpackedData, (int) curUnpackedDataSize, (int) (curUnpackedDataSize + count), fillByte);
				curUnpackedDataSize += count;
			}
			else if ((opcode & 0xFE) == 0xB2) { // copy
				if (log.isLoggable(Level.FINE)) {
					log.fine(String.format("Copy operation: opcode=0x%02X, count=%d, src offset=0x%X, dest offset=0x%X", opcode, count, i, curUnpackedDataSize));
				}
				if (curUnpackedDataSize + count > Integer.MAX_VALUE) {
					throw new UnExepackException("Unpacked size too large to index arrays safely");
				}
				if (i + count > packedData.length) {
					throw new UnExepackException("Unpacking overflow during copy: tried to copy " + count + " bytes from packed data offset " + i + " but packed data length is " + packedData.length);
				}
				if (curUnpackedDataSize + count > unpackedDataSize) {
					throw new UnExepackException("Unpacking overflow during copy: tried to copy " + count + " bytes at unpacked offset " + curUnpackedDataSize + " but max unpacked size is " + unpackedDataSize);
				}
				System.arraycopy(packedData, i, unpackedData, (int) curUnpackedDataSize, (int) count);
				curUnpackedDataSize += count;
				if (i + count > Integer.MAX_VALUE) {
					throw new UnExepackException(String.format("Cannot advance packed data pointer by %d bytes from offset %d: resulting offset exceeds %d, unsafe for array indexing", count, i, Integer.MAX_VALUE));
				}
				i += Math.toIntExact(count);
			}
			else {
				throw new UnExepackException(String.format("Unknown opcode encountered during unpacking: 0x%02X at packed data offset 0x%X, unpacked data offset 0x%X", opcode, i - 1, curUnpackedDataSize));
			}
			if ((opcode & 1) == 1) {
				break;
			}
		}
		final long remainingPackedBytes = (long) packedData.length - i;
		final long remainingUnpackedSpace = unpackedDataSize - curUnpackedDataSize;
		if (remainingPackedBytes > remainingUnpackedSpace) {
			throw new UnExepackException("Unpacking overflow at final copy: " + remainingPackedBytes + " bytes remaining in packed data but only " + remainingUnpackedSpace + " bytes left in unpacked buffer");
		}
		System.arraycopy(packedData, i, unpackedData, (int) curUnpackedDataSize, (int) remainingPackedBytes);
		curUnpackedDataSize += remainingPackedBytes;
		return curUnpackedDataSize < unpackedDataSize ? Arrays.copyOf(unpackedData, (int) curUnpackedDataSize) : unpackedData;
	}

	static byte[] createRelocTable(@NonNull final byte[] packedExec, @NonNull final DosHeader dh, @NonNull final ExepackHeader eh) throws UnExepackException {
		final long exepackOffset = ((long) dh.getECparhdr() + dh.getECs()) * 16L;
		final byte[] haystack = Arrays.copyOfRange(packedExec, (int) exepackOffset, packedExec.length);
		final String message = "Packed file is corrupt";
		try {
			final long reloc = exepackOffset + ByteArrayUtils.memmem(haystack, message.getBytes(StandardCharsets.US_ASCII)).orElseGet(() -> {
				log.fine(() -> String.format("Cannot find string \"%s\", trying ASM pattern", message));
				final byte[] bytePattern = new byte[] { (byte) 0xcd, 0x21, (byte) 0xb8, (byte) 0xff, 0x4c, (byte) 0xcd, 0x21 }; // the byte pattern that precedes the error message, cd 21 b8 ff 4c cd 21, which encodes the instructions int 0x21; mov ax, 0x4cff; int 0x21
				return ByteArrayUtils.memmem(haystack, bytePattern).orElseThrow(() -> new RelocationTableException(String.format("Cannot compute relocation table location: failed to find ASM pattern %s starting at offset 0x%X; this pattern should precede the error message \"%s\" in the packed executable", ByteArrayUtils.bytesToHex(bytePattern, 8), exepackOffset, message))) + bytePattern.length;
			});
			final long relocLength = (long) eh.getExepackSize() - (reloc - exepackOffset) + message.length();
			final long nbReloc = (relocLength - 16 * Short.BYTES) / 2;
			final long relocTableSize = nbReloc * 2 * Short.BYTES;
			if (relocTableSize > Integer.MAX_VALUE) {
				throw new UnExepackException(String.format("Relocation table size %d exceeds maximum allowed array size %d", relocTableSize, Integer.MAX_VALUE));
			}
			final ByteBuffer buf = ByteBuffer.wrap(packedExec);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			buf.position((int) (reloc + message.length()));
			try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				for (int i = 0; i < 16; i++) {
					final int count = Short.toUnsignedInt(buf.getShort());
					if (log.isLoggable(Level.FINE)) {
						log.fine(String.format("Reloc table outer loop index i=%d: count=%d entries to read", i, count));
					}
					for (int j = 0; j < count; j++) {
						if (baos.size() >= relocTableSize) {
							throw new UnExepackException(String.format("Relocation table overflow at outer index i=%d, inner index j=%d: current buffer size (%d bytes) exceeds expected table size (%d bytes)", i, j, baos.size(), relocTableSize));
						}
						final int entry = Short.toUnsignedInt(buf.getShort());
						if (log.isLoggable(Level.FINE)) {
							log.fine(String.format("Reloc table entry at i=%d, j=%d: value=0x%04X", i, j, entry));
						}
						baos.write(new byte[] { (byte) entry, (byte) (entry >> 8) });
						if (baos.size() >= relocTableSize) {
							throw new UnExepackException(String.format("Relocation table overflow after writing entry 0x%04X at i=%d, j=%d: current buffer size (%d bytes) exceeds expected table size (%d bytes)", entry, i, j, baos.size(), relocTableSize));
						}
						final int segment = (i * 0x1000) & 0xFFFF;
						baos.write(new byte[] { (byte) segment, (byte) (segment >> 8) });
					}
				}
				return baos.toByteArray();
			}
			catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		catch (final RelocationTableException e) {
			throw new UnExepackException(e);
		}
	}

	static byte[] writeExe(@NonNull final DosHeader dh, @NonNull final byte[] unpackedData, @NonNull final byte[] reloc, final int padding) {
		final byte[] header = dh.toByteArray();
		final ByteBuffer buf = ByteBuffer.allocate(header.length + reloc.length + padding + unpackedData.length);
		buf.put(header);
		buf.put(reloc);
		buf.position(buf.position() + padding);
		buf.put(unpackedData);
		return buf.array();
	}

	static byte[] craftExec(@NonNull final DosHeader dh, @NonNull final ExepackHeader eh, @NonNull final byte[] unpackedData, @NonNull final byte[] reloc) throws UnExepackException {
		final long headerSize = (long) DosHeader.SIZE + reloc.length;
		final int eMagic = DosHeader.SIGNATURE;
		long eCparhdr = (headerSize / 16) & 0xFFFF;
		eCparhdr = (eCparhdr / 32 + 1) * 32;
		final long paddingLength = eCparhdr * 16 - headerSize;
		final long totalLength = headerSize + paddingLength + unpackedData.length;
		if (totalLength > Integer.MAX_VALUE) {
			throw new UnExepackException(String.format("Total executable size %d exceeds maximum allowed array size %d", totalLength, Integer.MAX_VALUE));
		}
		final int eSs = eh.getRealSs();
		final int eSp = eh.getRealSp();
		final int eIp = eh.getRealIp();
		final int eCs = eh.getRealCs();
		final int eMinAlloc = dh.getEMinAlloc();
		final int eMaxAlloc = 0xFFFF;
		final int eLfarlc = DosHeader.SIZE;
		final int eCrlc = (reloc.length / (2 * Short.BYTES)) & 0xFFFF;
		final int eCblp = (int) (totalLength % 512);
		final int eCp = (int) ((totalLength / 512) + 1) & 0xFFFF;
		final DosHeader dhead = new DosHeader(eMagic, eCblp, eCp, eCrlc, (int) eCparhdr, eMinAlloc, eMaxAlloc, eSs, eSp, 0, eIp, eCs, eLfarlc, 0);
		log.info(() -> String.format("Unpacked DOS header: (%s)", dhead));
		return writeExe(dhead, unpackedData, reloc, (int) paddingLength);
	}

	/**
	 * Unpacks an EXE file that was compressed using Microsoft EXEPACK.
	 * <p>
	 * This method reads the packed DOS header and EXEPACK header, reverses the
	 * packed data as required by the EXEPACK backward compression algorithm,
	 * unpacks it, reconstructs the relocation table, and crafts a new DOS
	 * executable containing the unpacked data.
	 * </p>
	 *
	 * @param packedExec the byte array containing the packed EXE file; must be a
	 *        valid MS-DOS executable with EXEPACK compression
	 *
	 * @return a byte array containing the unpacked EXE file; ready to execute
	 *
	 * @throws UnExepackException if the input file is corrupt, truncated,
	 *         malformed, or cannot be unpacked for any reason
	 */
	public static byte[] unpack(@NonNull final byte[] packedExec) throws UnExepackException {
		try {
			final DosHeader dh = new DosHeader(Arrays.copyOf(packedExec, DosHeader.SIZE));
			log.info(() -> String.format("Packed DOS header: (%s)", dh));

			final long exeLen = decodeExeLen(dh.getECblp(), dh.getECp());
			if (exeLen < packedExec.length) {
				log.warning(() -> String.format("EXE file size is %d bytes; ignoring %d trailing bytes.", exeLen, packedExec.length - exeLen));
			}

			final int exepackOffset = (dh.getECparhdr() + dh.getECs()) * 16;
			if (exepackOffset > packedExec.length) {
				throw new UnExepackException(String.format("EXEPACK header offset (computed as (eCparhdr + eCs) * 16 = 0x%X, %d) exceeds total EXE file size (%d bytes); DOS header fields: eCparhdr=0x%X (%d), eCs=0x%X (%d)", exepackOffset, exepackOffset, packedExec.length, dh.getECparhdr(), dh.getECparhdr(), dh.getECs(), dh.getECs()));
			}
			final ExepackHeader eh = new ExepackHeader(Arrays.copyOfRange(packedExec, exepackOffset, exepackOffset + ExepackHeader.SIZE));
			log.info(() -> String.format("Exepack header at 0x%X: (%s)", exepackOffset, eh));

			final int unpackedDataSize = eh.getDestLen() * 16;
			final int packedDataStart = dh.getECparhdr() * 16;
			final int packedDataEnd = exepackOffset;

			final byte[] packedData = Arrays.copyOfRange(packedExec, packedDataStart, packedDataEnd);
			ByteArrayUtils.reverse(packedData);
			final byte[] unpackedData = unpackData(packedData, unpackedDataSize);
			ByteArrayUtils.reverse(unpackedData);
			final byte[] reloc = createRelocTable(packedExec, dh, eh);
			return craftExec(dh, eh, unpackedData, reloc);
		}
		catch (final ArrayIndexOutOfBoundsException e) {
			throw new UnExepackException("Array index out of bounds while unpacking EXEPACK data", e);
		}
	}

	static long decodeExeLen(final int eCblp, final int eCp) {
		if (eCblp == 0) {
			return eCp * 512L;
		}
		else if (eCp == 0) {
			return -1;
		}
		else if (eCblp >= 1 && eCblp <= 511) {
			return (eCp - 1) * 512L + eCblp;
		}
		else {
			return -1;
		}
	}

}
