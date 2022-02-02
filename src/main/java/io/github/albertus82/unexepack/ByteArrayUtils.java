package io.github.albertus82.unexepack;

import java.util.OptionalInt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/** Operations on byte arrays. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ByteArrayUtils {

	/**
	 * Reverses the order of the given array.
	 *
	 * @param array the array to reverse
	 */
	public static void reverse(@NonNull final byte[] array) {
		for (int i = 0, j = array.length - 1; i < j; i++, j--) {
			final byte c = array[i];
			array[i] = array[j];
			array[j] = c;
		}
	}

	/**
	 * Finds the start of the first occurrence of the byte array <em>needle</em> in
	 * the byte array <em>haystack</em>.
	 *
	 * @param haystack the array to be scanned
	 * @param needle the array containing the sequence of bytes to match
	 *
	 * @return an {@code OptionalInt} describing the index of the beginning of the
	 *         needle, or an empty {@code OptionalInt} if the needle is not found
	 *
	 * @throws IllegalArgumentException if {@code needle} is empty
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

}
