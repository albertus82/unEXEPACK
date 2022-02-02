package io.github.albertus82.unexepack;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.java.Log;

@Log
class ExepackHeaderTest {

	@Test
	void test() throws InvalidDosHeaderException {
		final byte[] validHeader = { (byte) 0x94, (byte) 0xBB, 0x00, 0x00, 0x00, 0x00, (byte) 0x95, 0x01, 0x00, 0x08, 0x14, 0x18, (byte) 0xAB, 0x17, 0x01, 0x00, 0x52, 0x42 };
		final byte[] invalidHeader = "123456789012345678".getBytes(StandardCharsets.US_ASCII);
		Assertions.assertThrows(NullPointerException.class, () -> new ExepackHeader(null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ExepackHeader(new byte[17]));
		Assertions.assertThrows(IllegalArgumentException.class, () -> new ExepackHeader(new byte[19]));
		Assertions.assertThrows(InvalidExepackHeaderException.class, () -> new ExepackHeader(invalidHeader));
		try {
			new ExepackHeader(invalidHeader);
			Assertions.assertFalse(true);
		}
		catch (final InvalidHeaderException e) {
			log.log(Level.INFO, e, () -> Arrays.toString(invalidHeader));
			Assertions.assertArrayEquals(invalidHeader, e.getHeaderBytes());
		}
		Assertions.assertDoesNotThrow(() -> new ExepackHeader(validHeader));
	}

}
