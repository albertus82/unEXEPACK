package io.github.albertus82.unexepack;

/** Thrown to indicate that an invalid EXEPACK header has been detected. */
public class InvalidExepackHeaderException extends InvalidHeaderException {

	private static final long serialVersionUID = -1424734787160373924L;

	InvalidExepackHeaderException(final byte[] headerBytes) {
		super(headerBytes);
	}

}
