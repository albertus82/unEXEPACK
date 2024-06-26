package io.github.albertus82.unexepack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Thrown to indicate that an invalid header has been detected. */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class InvalidHeaderException extends Exception {

	private static final long serialVersionUID = -7116701588107760281L;

	private final byte[] headerBytes;

}
