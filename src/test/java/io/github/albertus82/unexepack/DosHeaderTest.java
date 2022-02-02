package io.github.albertus82.unexepack;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.java.Log;

@Log
class DosHeaderTest {

	@Test
	void test() throws InvalidDosHeaderException {
		final byte[] validHeader = { 0x4D, 0x5A, (byte) 0xB5, 0x00, (byte) 0x99, 0x00, 0x00, 0x00, 0x20, 0x00, (byte) 0xA9, 0x05, (byte) 0xA9, 0x05, (byte) 0xC5, 0x17, (byte) 0x80, 0x00, 0x00, 0x00, 0x12, 0x00, (byte) 0xD2, 0x12, 0x1E, 0x00, 0x00, 0x00 };
		final byte[] invalidHeader = "1234567890123456789012345678".getBytes(StandardCharsets.US_ASCII);
		Assertions.assertThrows(NullPointerException.class, () -> new DosHeader(null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new DosHeader(new byte[27]));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new DosHeader(new byte[29]));
		Assertions.assertThrows(InvalidDosHeaderException.class, () -> new DosHeader(invalidHeader));
		try {
			new DosHeader(invalidHeader);
			Assertions.assertFalse(true);
		}
		catch (final InvalidHeaderException e) {
			log.log(Level.INFO, e, () -> Arrays.toString(invalidHeader));
			Assertions.assertArrayEquals(invalidHeader, e.getHeaderBytes());
		}
		Assertions.assertDoesNotThrow(() -> new DosHeader(validHeader));
		final DosHeader dh = new DosHeader(validHeader);
		Assertions.assertArrayEquals(validHeader, dh.toByteArray());
	}

}
