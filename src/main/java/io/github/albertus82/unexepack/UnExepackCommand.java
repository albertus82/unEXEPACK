package io.github.albertus82.unexepack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.Callable;

import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Parameters;

@Log
@Command(description = "Unpacker for Microsoft EXEPACK utility compressor.")
class UnExepackCommand implements Callable<Integer> {

	private static final int MAX_INPUT_FILE_SIZE = 0x800000; // 8 MiB, based on the info available at https://w4kfu.github.io/unEXEPACK/files/exepack_list.html
	private static final String LOGGING_FORMAT_PROPERTY = "java.util.logging.SimpleFormatter.format";

	@Parameters(index = "0", paramLabel = "<EXEPACK_file>")
	private Path inputFile;

	@Parameters(index = "1", paramLabel = "OUTPUT_FILE", defaultValue = "unpacked", showDefaultValue = Visibility.ALWAYS, arity = "0..1")
	private Path outputFile;

	public static void main(final String... args) {
		if (System.getProperty(LOGGING_FORMAT_PROPERTY) == null) {
			System.setProperty(LOGGING_FORMAT_PROPERTY, "%4$s: %5$s%6$s%n");
		}
		System.exit(new CommandLine(new UnExepackCommand()).setCommandName(UnExepack.class.getSimpleName().toLowerCase(Locale.ROOT)).execute(args));
	}

	@Override
	public Integer call() throws IOException {
		if (!inputFile.toFile().exists() || Files.isDirectory(inputFile)) {
			log.severe("The input file does not exist.");
			return ExitCode.SOFTWARE;
		}
		if (Files.size(inputFile) > MAX_INPUT_FILE_SIZE) {
			log.severe("The input file is too large.");
			return ExitCode.SOFTWARE;
		}
		final byte[] packedExec = Files.readAllBytes(inputFile);
		final byte[] unpackedExec;
		try {
			unpackedExec = UnExepack.unpack(packedExec);
		}
		catch (final InvalidDosHeaderException e) {
			log.severe("The input file is not a valid MS-DOS executable.");
			return ExitCode.SOFTWARE;
		}
		catch (final InvalidExepackHeaderException e) {
			log.severe("The input file is not a valid EXEPACK executable.");
			return ExitCode.SOFTWARE;
		}
		Files.write(outputFile, unpackedExec);
		return ExitCode.OK;
	}

}
