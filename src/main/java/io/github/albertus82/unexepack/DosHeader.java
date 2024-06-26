package io.github.albertus82.unexepack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * A representation of the DOS header.
 *
 * @see <a href="https://www.tavi.co.uk/phobos/exeformat.html">Notes on the
 *      format of DOS .EXE files - Phobos</a>
 * @see <a href="https://www.delorie.com/djgpp/doc/exe/">EXE Format - DJ
 *      Delorie</a>
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DosHeader {

	static final int SIGNATURE = 0x5A4D; // "MZ"
	static final int SIZE = 14 * Short.BYTES; // bytes

	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

	int eMagic; // "MZ"
	int eCblp; // Last page size in bytes
	int eCp; // Number of pages
	int eCrlc; // Number of entries in the relocation table
	int eCparhdr; // Size of the EXE header in paragraphs
	int eMinAlloc; // Minimum number of paragraphs allocated
	int eMaxAlloc; // Maximum number of paragraphs allocated
	int eSs; // Initial SS value
	int eSp; // Initial SP value
	int eCsum; // Checksum (rarely checked)
	int eIp; // Initial IP value
	int eCs; // Initial CS value
	int eLfarlc; // Relocation table offset
	int eOvno; // Overlay number

	DosHeader(@NonNull final byte[] bytes) throws InvalidDosHeaderException {
		if (bytes.length != SIZE) {
			throw new IllegalArgumentException("Invalid byte array size; expected: " + SIZE + " but was: " + bytes.length);
		}
		final ShortBuffer buf = ByteBuffer.wrap(bytes).order(BYTE_ORDER).asShortBuffer();
		eMagic = Short.toUnsignedInt(buf.get(0));
		eCblp = Short.toUnsignedInt(buf.get(1));
		eCp = Short.toUnsignedInt(buf.get(2));
		eCrlc = Short.toUnsignedInt(buf.get(3));
		eCparhdr = Short.toUnsignedInt(buf.get(4));
		eMinAlloc = Short.toUnsignedInt(buf.get(5));
		eMaxAlloc = Short.toUnsignedInt(buf.get(6));
		eSs = Short.toUnsignedInt(buf.get(7));
		eSp = Short.toUnsignedInt(buf.get(8));
		eCsum = Short.toUnsignedInt(buf.get(9));
		eIp = Short.toUnsignedInt(buf.get(10));
		eCs = Short.toUnsignedInt(buf.get(11));
		eLfarlc = Short.toUnsignedInt(buf.get(12));
		eOvno = Short.toUnsignedInt(buf.get(13));
		if (!validate()) {
			throw new InvalidDosHeaderException(bytes);
		}
	}

	private boolean validate() {
		if (eMagic != SIGNATURE) {
			return false;
		}
		/* at least one page */
		if (eCp == 0) {
			return false;
		}
		/* last page must not hold 0 bytes */
		if (eCblp == 0) {
			return false;
		}
		/* not even number of paragraphs */
		if (eCparhdr % 2 != 0) { // NOSONAR Replace this if-then-else statement by a single return statement. Return of boolean expressions should not be wrapped into an "if-then-else" statement (java:S1126)
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("eMagic = 0x%04X, eCblp = 0x%04X, eCp = 0x%04X, eCrlc = 0x%04X, eCparhdr = 0x%04X, eMinAlloc = 0x%04X, eMaxAlloc = 0x%04X, eSs = 0x%04X, eSp = 0x%04X, eCsum = 0x%04X, eIp = 0x%04X, eCs = 0x%04X, eLfarlc = 0x%04X, eOvno = 0x%04X", eMagic, eCblp, eCp, eCrlc, eCparhdr, eMinAlloc, eMaxAlloc, eSs, eSp, eCsum, eIp, eCs, eLfarlc, eOvno);
	}

	byte[] toByteArray() {
		final ByteBuffer buf = ByteBuffer.allocate(SIZE);
		buf.order(BYTE_ORDER);
		buf.putShort(0 * Short.BYTES, (short) eMagic);
		buf.putShort(1 * Short.BYTES, (short) eCblp);
		buf.putShort(2 * Short.BYTES, (short) eCp);
		buf.putShort(3 * Short.BYTES, (short) eCrlc);
		buf.putShort(4 * Short.BYTES, (short) eCparhdr);
		buf.putShort(5 * Short.BYTES, (short) eMinAlloc);
		buf.putShort(6 * Short.BYTES, (short) eMaxAlloc);
		buf.putShort(7 * Short.BYTES, (short) eSs);
		buf.putShort(8 * Short.BYTES, (short) eSp);
		buf.putShort(9 * Short.BYTES, (short) eCsum);
		buf.putShort(10 * Short.BYTES, (short) eIp);
		buf.putShort(11 * Short.BYTES, (short) eCs);
		buf.putShort(12 * Short.BYTES, (short) eLfarlc);
		buf.putShort(13 * Short.BYTES, (short) eOvno);
		return buf.array();
	}

}
