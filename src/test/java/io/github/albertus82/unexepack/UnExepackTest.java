package io.github.albertus82.unexepack;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import lombok.extern.java.Log;

@Log
class UnExepackTest {
	private static final String PROP_NAME = UnExepack.class.getPackage().getName() + ".maxUnpackedSizeBytes";
	private static final Map<String, String> digests = new HashMap<>();

	@BeforeAll
	static void beforeAll() {
		digests.put("A.EXE", "592744c31541044c673e4647cc75f853abbc508d7bc56dcbcae9578e07f2f39c");
		digests.put("B.EXE", "d5d3127e36ec942fd8f722714f6604f28a2fed5c305e5c91648d16285396cf2b");
		digests.put("C.EXE", "0a02d7e93da2b92914142daeea344c4b8d8adfce6dfae44118c46b65024ceaef");
		digests.put("D.EXE", "5fd858d7e7a4d10955d86496f7b19b1adebcb32eb0fb54f7733893ddca20a2d8");
		digests.put("E.EXE", "9402a6f4ca05cdbb26d612f3ab79b72bce2899da96259d1d042ef164d6f159a6");
		digests.put("F.EXE", "c82518469a2c2f06a24f8fb37a59459ae1ce19186dd71095e3ed5b3695d0f6ca");
		digests.put("G.EXE", "c91aa00c7de39088a2b5b92e89850ea8aaa3590eda2f1d729a5425c029673fcb");
		digests.put("H.EXE", "9402a6f4ca05cdbb26d612f3ab79b72bce2899da96259d1d042ef164d6f159a6");
		digests.put("I.EXE", "e9df83a3a7c4c47e544b1c3023986eb99be330ed779a3ede8ac61cdcfb22d046");
		digests.put("J.EXE", "ceb273d445168ca60dd5ac458f7207fe03aa21986521903a08c177346ea8a0f2");
		digests.put("K.EXE", "9b8dc9e4208ef1b4a2af3b516c23a64007f55e9b24ab4fd019f68d454d380c8a");
		digests.put("L.EXE", "f5923fa58a07523a8ca850684b6c96737ac9cde9e938f16f1d48105bc9e8267b");
		digests.put("M.EXE", "8f397594e14eaf2b3e6174c4c747366dde1ef864a82d292c08225af4452537eb");
		digests.put("N.EXE", "510da30c521872dd0a8442c0b52e676d8333dd0f9ffa3af0a1ddde19cb840c63");
	}

	@AfterEach
	void clearProperties() {
		System.clearProperty(PROP_NAME);
	}

	@Test
	void testUnpack() throws Exception {
		Assertions.assertThrows(NullPointerException.class, () -> UnExepack.unpack(null));
		final String propertyName = "testSecret";
		final String secret = System.getProperty(propertyName);
		if (secret == null) {
			log.log(Level.WARNING, "Missing system property ''{0}'', skipping unpacking test.", propertyName);
		}
		Assumptions.assumeTrue(secret != null);
		final byte[] bytes;
		try (final InputStream in = getClass().getResourceAsStream("/exepacked.7z")) {
			bytes = IOUtils.toByteArray(in);
		}
		try (final SeekableByteChannel c = new SeekableInMemoryByteChannel(bytes); final SevenZFile sevenZFile = SevenZFile.builder().setSeekableByteChannel(c).setPassword(secret.toCharArray()).get()) {
			SevenZArchiveEntry entry;
			while ((entry = sevenZFile.getNextEntry()) != null) {
				log.log(Level.INFO, "{0}", entry.getName());
				final byte[] buf = new byte[(int) entry.getSize()];
				Assertions.assertEquals(entry.getSize(), sevenZFile.read(buf));
				Assertions.assertEquals(-1, sevenZFile.read());
				Assertions.assertEquals(digests.get(entry.getName()), DigestUtils.sha256Hex(UnExepack.unpack(buf)), entry.getName());
			}
		}
	}

	@Test
	void testDecodeExeLen() {
		Assertions.assertEquals(0, UnExepack.decodeExeLen(0, 0));
		Assertions.assertEquals(1, UnExepack.decodeExeLen(1, 1));
		Assertions.assertEquals(511, UnExepack.decodeExeLen(511, 1));
		Assertions.assertEquals(512, UnExepack.decodeExeLen(0, 1));
		Assertions.assertEquals(513, UnExepack.decodeExeLen(1, 2));
		Assertions.assertEquals(0xFFFF * 512 - 1, UnExepack.decodeExeLen(511, 0xFFFF));
		Assertions.assertEquals(0xFFFF * 512, UnExepack.decodeExeLen(0, 0xFFFF));
		// When e_cp == 0, e_cblp must be 0, otherwise it would encode a negative length.
		Assertions.assertEquals(-1, UnExepack.decodeExeLen(1, 0));
		Assertions.assertEquals(-1, UnExepack.decodeExeLen(511, 0));
		// e_cblp must be <= 511.
		Assertions.assertEquals(-1, UnExepack.decodeExeLen(512, 1));
	}

	@Test
	void decodeExeLen_whenCblpIsZero() {
		assertEquals(1024L, UnExepack.decodeExeLen(0, 2));
	}

	@Test
	void decodeExeLen_whenCpIsZero() {
		assertEquals(-1L, UnExepack.decodeExeLen(1, 0));
	}

	@Test
	void decodeExeLen_whenCblpIsValid() {
		assertEquals(1025L, UnExepack.decodeExeLen(1, 3));
	}

	@Test
	void decodeExeLen_whenCblpIsInvalid() {
		assertEquals(-1L, UnExepack.decodeExeLen(512, 1));
	}

	@Test
	void unpackData_fillOperation() throws Exception {
		/*
		 * Encoded stream: B1 00 03 7A
		 *
		 * opcode B1 -> fill + terminate count -> 3 fill byte -> 0x7A
		 */
		final byte[] packed = { (byte) 0xB1, 0x00, 0x03, 0x7A };
		final byte[] result = UnExepack.unpackData(packed, 3);
		assertArrayEquals(new byte[] { 0x7A, 0x7A, 0x7A }, result);
	}

	@Test
	void unpackData_copyOperation() throws Exception {
		/*
		 * B3 00 03 11 22 33
		 *
		 * opcode B3 -> copy + terminate count -> 3 copied -> 11 22 33
		 */
		final byte[] packed = { (byte) 0xB3, 0x00, 0x03, 0x11, 0x22, 0x33 };
		final byte[] result = UnExepack.unpackData(packed, 3);
		assertArrayEquals(new byte[] { 0x11, 0x22, 0x33 }, result);
	}

	@Test
	void unpackData_skipsLeadingPaddingBytes() throws Exception {
		final byte[] packed = { (byte) 0xFF, (byte) 0xFF, (byte) 0xB1, 0x00, 0x02, 0x55 };
		final byte[] result = UnExepack.unpackData(packed, 2);
		assertArrayEquals(new byte[] { 0x55, 0x55 }, result);
	}

	@Test
	void unpackData_finalTrailingCopy() throws Exception {
		/*
		 * B1 00 02 44 55 66
		 *
		 * B1 = fill + terminate fill count = 2 fill byte = 44
		 *
		 * remaining packed bytes copied automatically
		 */
		final byte[] packed = { (byte) 0xB1, 0x00, 0x02, 0x44, 0x55, 0x66 };
		final byte[] result = UnExepack.unpackData(packed, 4);
		assertArrayEquals(new byte[] { 0x44, 0x44, 0x55, 0x66 }, result);
	}

	@Test
	void unpackData_respectsConfiguredLimit() throws Exception {
		System.setProperty(PROP_NAME, "2");
		final byte[] packed = { (byte) 0xB1, 0x00, 0x03, 0x01 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 3));
		assertTrue(ex.getMessage().contains("exceeds configured maximum"));
	}

	@Test
	void unpackData_unknownOpcode() {
		final byte[] packed = { (byte) 0x99, 0x00, 0x01 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 1));
		assertTrue(ex.getMessage().contains("Unknown opcode"));
	}

	@Test
	void unpackData_truncatedOpcode() {
		final byte[] packed = { (byte) 0xB1 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 1));
		assertTrue(ex.getMessage().contains("Unexpected end of packed data"));
	}

	@Test
	void unpackData_missingFillByte() {
		final byte[] packed = { (byte) 0xB1, 0x00, 0x01 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 1));
		assertTrue(ex.getMessage().contains("fill byte"));
	}

	@Test
	void unpackData_fillOverflow() {
		final byte[] packed = { (byte) 0xB1, 0x00, 0x05, 0x11 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 2));
		assertTrue(ex.getMessage().contains("Unpacking overflow during fill"));
	}

	@Test
	void unpackData_copyOverflowPackedData() {
		final byte[] packed = { (byte) 0xB3, 0x00, 0x05, 0x01 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 5));
		assertTrue(ex.getMessage().contains("packed data length"));
	}

	@Test
	void unpackData_copyOverflowUnpackedBuffer() {
		final byte[] packed = { (byte) 0xB3, 0x00, 0x05, 0x01, 0x02, 0x03, 0x04, 0x05 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 2));
		assertTrue(ex.getMessage().contains("max unpacked size"));
	}

	@Test
	void unpackData_finalCopyOverflow() {
		final byte[] packed = { (byte) 0xB1, 0x00, 0x01, 0x55, 0x66, 0x77 };
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpackData(packed, 2));
		assertTrue(ex.getMessage().contains("final copy"));
	}

	@Test
	void unpack_invalidExe_throwsWrappedException() {
		final byte[] invalid = new byte[8];
		assertThrows(UnExepackException.class, () -> UnExepack.unpack(invalid));
	}

	@Test
	void writeExe_combinesAllSections() throws Exception {
		final DosHeader dh = new DosHeader(DosHeader.SIGNATURE, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, DosHeader.SIZE, 0);
		final byte[] unpacked = { 0x11, 0x22 };
		final byte[] reloc = { 0x33, 0x44 };
		final byte[] result = UnExepack.writeExe(dh, unpacked, reloc, 2);
		assertEquals(DosHeader.SIZE + reloc.length + 2 + unpacked.length, result.length);
		assertArrayEquals(reloc, Arrays.copyOfRange(result, DosHeader.SIZE, DosHeader.SIZE + reloc.length));
	}

	private static DosHeader validDosHeader(int eCparhdr, int eCs, int eMinAlloc) throws Exception {
		final ByteBuffer buf = ByteBuffer.allocate(DosHeader.SIZE).order(ByteOrder.LITTLE_ENDIAN);
		buf.putShort((short) DosHeader.SIGNATURE); // eMagic
		buf.putShort((short) 1); // eCblp
		buf.putShort((short) 1); // eCp
		buf.putShort((short) 0); // eCrlc
		buf.putShort((short) eCparhdr); // eCparhdr
		buf.putShort((short) eMinAlloc); // eMinAlloc
		buf.putShort((short) 0xFFFF); // eMaxAlloc
		buf.putShort((short) 0); // eSs
		buf.putShort((short) 0); // eSp
		buf.putShort((short) 0); // eCsum
		buf.putShort((short) 0); // eIp
		buf.putShort((short) eCs); // eCs
		buf.putShort((short) DosHeader.SIZE); // eLfarlc
		buf.putShort((short) 0); // eOvno
		return new DosHeader(buf.array());
	}

	private static ExepackHeader validExepackHeader(int exepackSize, int realIp, int realCs, int realSp, int realSs, int destLen) throws Exception {
		final ByteBuffer buf = ByteBuffer.allocate(ExepackHeader.SIZE).order(ByteOrder.LITTLE_ENDIAN);
		buf.putShort((short) realIp);
		buf.putShort((short) realCs);
		buf.putShort((short) 0); // memStart
		buf.putShort((short) exepackSize);
		buf.putShort((short) realSp);
		buf.putShort((short) realSs);
		buf.putShort((short) destLen);
		buf.putShort((short) 0); // skipLen
		buf.putShort((short) ExepackHeader.SIGNATURE);
		return new ExepackHeader(buf.array());
	}

	@Test
	void createRelocTable_whenMessageIsMissing_throwsException() throws Exception {
		final DosHeader dh = validDosHeader(2, 0, 0);
		final ExepackHeader eh = validExepackHeader(32, 0, 0, 0, 0, 1);
		final byte[] packedExec = new byte[128];
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.createRelocTable(packedExec, dh, eh));
		assertTrue(ex.getMessage().contains("Cannot compute relocation table location"));
	}

	@Test
	void craftExec_createsExecutable() throws Exception {
		final DosHeader dh = validDosHeader(2, 0, 0x10);
		final ExepackHeader eh = validExepackHeader(32, 0x1111, 0x2222, 0x3333, 0x4444, 1);
		final byte[] unpacked = { 0x11, 0x22, 0x33 };
		final byte[] reloc = { 0x55, 0x66, 0x77, 0x00 };
		final byte[] exe = UnExepack.craftExec(dh, eh, unpacked, reloc);
		assertNotNull(exe);
		assertTrue(exe.length > unpacked.length);
		assertEquals('M', exe[0]);
		assertEquals('Z', exe[1]);
	}

	@Test
	void unpack_whenExepackOffsetExceedsInput_throwsException() throws Exception {
		final ByteBuffer buf = ByteBuffer.allocate(DosHeader.SIZE).order(ByteOrder.LITTLE_ENDIAN);
		buf.putShort((short) DosHeader.SIGNATURE);
		buf.putShort((short) 1); // eCblp
		buf.putShort((short) 1); // eCp
		buf.putShort((short) 0);
		buf.putShort((short) 0x7FFE); // eCparhdr (must be even)
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0);
		buf.putShort((short) 0x7FFF); // eCs
		buf.putShort((short) DosHeader.SIZE);
		buf.putShort((short) 0);
		final byte[] packed = buf.array();
		final UnExepackException ex = assertThrows(UnExepackException.class, () -> UnExepack.unpack(packed));
		assertTrue(ex.getMessage().contains("EXEPACK header offset"));
	}
}
