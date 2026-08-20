package io.github.albertus82.unexepack;

/**
 * Base class for exceptions indicating that a packed file's header is invalid
 * or malformed.
 * <p>
 * This exception is thrown during unpacking when the header of the packed file
 * does not conform to the expected format, preventing safe processing of the
 * file.
 * </p>
 * <p>
 * Subclasses can provide more specific reasons for the invalid header.
 * </p>
 */
public abstract class InvalidHeaderException extends UnExepackException {

	private static final long serialVersionUID = -7116701588107760281L;

	/**
	 * The header bytes that triggered this exception.
	 */
	private final byte[] headerBytes;

	InvalidHeaderException(final byte[] headerBytes) {
		this.headerBytes = headerBytes;
	}

	/**
	 * Returns the header bytes that triggered this exception.
	 *
	 * @return a byte array containing the invalid header
	 */
	public byte[] getHeaderBytes() {
		return headerBytes;
	}

}
