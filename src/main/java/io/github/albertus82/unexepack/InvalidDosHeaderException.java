package io.github.albertus82.unexepack;

/** Thrown to indicate that an invalid DOS header has been detected. */
public class InvalidDosHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = 8446610602802043780L;

	InvalidDosHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}
