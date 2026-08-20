package io.github.albertus82.unexepack;

import java.util.OptionalInt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Utility methods for working with byte arrays.
 * <p>
 * This class provides low-level helper methods for common byte array operations
 * such as subsequence search, in-place reversal, and hexadecimal string
 * conversion.
 * <p>
 * This is a utility class and cannot be instantiated.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ByteArrayUtils {

	/**
	 * Searches for the first occurrence of a byte sequence within another byte
	 * array.
	 * <p>
	 * This method performs a linear search equivalent to the C {@code memmem}
	 * function. If the {@code needle} sequence is found inside {@code haystack},
	 * the zero-based index of the first matching byte is returned.
	 *
	 * @param haystack the byte array to search in
	 * @param needle the byte sequence to search for; must not be empty
	 * @return an {@link OptionalInt} containing the index of the first occurrence
	 *         of {@code needle} in {@code haystack}, or {@link OptionalInt#empty()}
	 *         if no match is found
	 * @throws IllegalArgumentException if {@code needle} is empty
	 * @throws NullPointerException if {@code haystack} or {@code needle} is
	 *         {@code null}
	 */
	public static OptionalInt memmem(@NonNull final byte[] haystack, @NonNull final byte[] needle) {
		if (needle.length == 0) {
			throw new IllegalArgumentException("needle must not be empty");
		}
		for (int i = 0; i < haystack.length - needle.length + 1; ++i) {
			boolean found = true;
			for (int j = 0; j < needle.length; ++j) {
				if (haystack[i + j] != needle[j]) {
					found = false;
					break;
				}
			}
			if (found)
				return OptionalInt.of(i);
		}
		return OptionalInt.empty();
	}

	static void reverse(@NonNull final byte[] array) {
		for (int i = 0, j = array.length - 1; i < j; i++, j--) {
			final byte c = array[i];
			array[i] = array[j];
			array[j] = c;
		}
	}

	static String bytesToHex(@NonNull final byte[] bytes, final int maxLength) {
		final int len = Math.min(bytes.length, maxLength);
		final StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			sb.append(String.format("%02X", bytes[i]));
			if (i < len - 1)
				sb.append(' ');
		}
		if (bytes.length > maxLength) {
			sb.append(" ...");
		}
		return sb.toString();
	}

}
