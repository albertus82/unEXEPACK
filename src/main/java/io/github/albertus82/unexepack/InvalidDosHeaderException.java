package io.github.albertus82.unexepack;

/**
 * Exception thrown to indicate that the DOS header of a packed file is invalid.
 * <p>
 * This occurs during unpacking when the DOS header does not match the expected
 * format or contains invalid values, which makes it impossible to safely
 * process the file.
 * </p>
 */
public class InvalidDosHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = 8446610602802043780L;

	InvalidDosHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}
