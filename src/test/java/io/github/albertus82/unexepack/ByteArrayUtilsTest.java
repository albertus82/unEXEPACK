package io.github.albertus82.unexepack;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.TreeSet;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import lombok.extern.java.Log;

@Log
class ByteArrayUtilsTest {

	@Test
	void testReverse() {
		Assertions.assertThrows(NullPointerException.class, () -> ByteArrayUtils.reverse(null));

		final byte[] a1 = { 1, 2, -3 };
		ByteArrayUtils.reverse(a1);
		Assertions.assertArrayEquals(new byte[] { -3, 2, 1 }, a1);

		final byte[] a2 = { 2, 2, 2 };
		ByteArrayUtils.reverse(a2);
		Assertions.assertArrayEquals(new byte[] { 2, 2, 2 }, a2);

		final byte[] a3 = { -5, -5 };
		ByteArrayUtils.reverse(a3);
		Assertions.assertArrayEquals(new byte[] { -5, -5 }, a3);

		final byte[] a4 = { 127 };
		ByteArrayUtils.reverse(a4);
		Assertions.assertArrayEquals(new byte[] { 127 }, a4);

		final byte[] a5 = { -128 };
		ByteArrayUtils.reverse(a5);
		Assertions.assertArrayEquals(new byte[] { -128 }, a5);

		final byte[] a6 = {};
		ByteArrayUtils.reverse(a6);
		Assertions.assertArrayEquals(new byte[0], a6);

		final byte[] a7 = { -1, 2, 3, -4 };
		ByteArrayUtils.reverse(a7);
		Assertions.assertArrayEquals(new byte[] { -4, 3, 2, -1 }, a7);
	}

	@Test
	void testMemmem() {
		for (final String haystack : new TreeSet<String>(Arrays.asList("lorem ipsum dolor sit amet", ""))) {
			for (final String needle : new TreeSet<String>(Arrays.asList("sit", "lore", "amet", "olo", " ", "lorem ipsum dolor sit amet", "lorem ipsum dolor sit amet ", " lorem ipsum dolor sit amet", "orem ipsum dolor sit amet", "lorem ipsum dolor sit ame", "consectetur adipiscing elit"))) {
				final int index = haystack.indexOf(needle);
				log.log(Level.INFO, "memmem(\"{0}\", \"{1}\")", new String[] { haystack, needle });
				Assertions.assertEquals(index == -1 ? OptionalInt.empty() : OptionalInt.of(index), ByteArrayUtils.memmem(haystack.getBytes(StandardCharsets.US_ASCII), needle.getBytes(StandardCharsets.US_ASCII)), needle);
			}
		}
		Assertions.assertThrows(NullPointerException.class, () -> ByteArrayUtils.memmem(null, new byte[1]));
		Assertions.assertThrows(NullPointerException.class, () -> ByteArrayUtils.memmem(new byte[1], null));
		Assertions.assertThrows(NullPointerException.class, () -> ByteArrayUtils.memmem(null, null));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ByteArrayUtils.memmem(new byte[0], new byte[0]));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ByteArrayUtils.memmem(new byte[1], new byte[0]));
		Assertions.assertThrows(IllegalArgumentException.class, () -> ByteArrayUtils.memmem(new byte[2], new byte[0]));
		Assertions.assertEquals(OptionalInt.empty(), ByteArrayUtils.memmem(new byte[0], new byte[1]));
		Assertions.assertEquals(OptionalInt.empty(), ByteArrayUtils.memmem(new byte[0], new byte[2]));
	}

}
