package io.github.albertus82.unexepack;

/**
 * Exception thrown to indicate that the EXEPACK-specific header of a packed
 * file is invalid.
 * <p>
 * This occurs during unpacking when the EXEPACK header does not conform to the
 * expected format or contains inconsistent data, preventing safe unpacking.
 * </p>
 */
public class InvalidExepackHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = -1424734787160373924L;

	InvalidExepackHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}
