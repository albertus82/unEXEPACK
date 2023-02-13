package io.github.albertus82.unexepack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnExepackCommandTest {

	@Test
	void testUnExepackCommandWithInvalidInputFile() throws IOException {
		Path tempFile = null;
		try {
			tempFile = Files.createTempFile(null, null); // create a temporary file for testing
			Assertions.assertTrue(Files.isRegularFile(tempFile));

			final UnExepackCommand cmd = new UnExepackCommand();
			cmd.setInputFile(tempFile);
			Assertions.assertEquals(1, cmd.call());
		}
		finally { // clean up the temporary files
			if (tempFile != null) {
				Files.deleteIfExists(tempFile);
			}
		}
	}

	@Test
	void testUnExepackCommandWithNonExistingInputFile() throws IOException {
		final Path tempFile = Paths.get(UUID.randomUUID().toString());
		Assertions.assertTrue(Files.notExists(tempFile));

		final UnExepackCommand cmd = new UnExepackCommand();
		cmd.setInputFile(tempFile);
		Assertions.assertEquals(1, cmd.call());
	}

}
