package io.github.albertus82.unexepack;

/**
 * Base exception type for errors occurring during unpacking of EXEPACK-packed
 * files.
 * <p>
 * This exception and its subclasses are thrown when an operation in the
 * unpacking process cannot be safely completed, such as encountering invalid
 * headers, malformed data, or buffer overflows.
 * </p>
 * <p>
 * Subclasses may provide more specific error contexts.
 * </p>
 */
public class UnExepackException extends Exception {

	private static final long serialVersionUID = 1267587806619969202L;

	UnExepackException() {}

	UnExepackException(final String message, final Throwable cause) {
		super(message, cause);
	}

	UnExepackException(final String message) {
		super(message);
	}

	UnExepackException(final Throwable cause) {
		super(cause);
	}

}
