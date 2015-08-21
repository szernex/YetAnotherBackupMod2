package org.szernex.yabm2.util;

import net.minecraftforge.common.DimensionManager;
import org.szernex.yabm2.handler.ConfigHandler;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupCreationHelper
{
	public static final String FILENAME_SEPARATOR = "_";
	public static final String TIMESTAMP_PATTERN = "YYYY-MM-dd" + FILENAME_SEPARATOR + "HH-mm-ss";
	public static final String TIMESTAMP_REGEX_PATTERN = "\\d{4}-\\d{2}-\\d{2}" + FILENAME_SEPARATOR + "\\d{2}-\\d{2}-\\d{2}";

	private static final HashSet<PathMatcher> blacklistMatchers = new HashSet<>();

	private static class BackupFileVisitor extends SimpleFileVisitor<Path>
	{
		private HashSet<Path> files = new HashSet<>();
		private Set<PathMatcher> matchers = new HashSet<>();

		public BackupFileVisitor(Set<PathMatcher> blacklist_matchers)
		{
			if (blacklist_matchers != null)
				matchers = blacklist_matchers;
		}

		public HashSet<Path> getFiles()
		{
			return files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			for (PathMatcher matcher : matchers)
			{
				if (matcher.matches(file))
				{
					return FileVisitResult.CONTINUE;
				}
			}

			LogHelper.trace("Including " + file);
			files.add(file);

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
		{
			LogHelper.warn("Could not visit file %s: %s", file, exc.getMessage());

			return super.visitFileFailed(file, exc);
		}
	}

	public static void init()
	{
		LogHelper.info("Initializing BackupCreationHelper");

		String separator = FileSystems.getDefault().getSeparator();
		blacklistMatchers.clear();

		// exclude saves directory by default in case of this being a single player instance, only the current save will get included
		blacklistMatchers.add(FileSystems.getDefault().getPathMatcher("regex:^.*\\" + separator + "?saves\\" + separator + ".*"));

		HashSet<String> patterns = new HashSet<>(Arrays.asList(ConfigHandler.blacklist)); // to make sure there are no duplicate patterns

		for (String pattern : patterns)
		{
			blacklistMatchers.add(FileSystems.getDefault().getPathMatcher("regex:" + pattern));
		}
	}

	public static Set<Path> gatherFiles(Path root, Set<PathMatcher> blacklist_matchers)
	{
		HashSet<Path> output = new HashSet<>();
		BackupFileVisitor fileVisitor = new BackupFileVisitor(blacklist_matchers);

		try
		{
			Files.walkFileTree(root, fileVisitor);
			output = fileVisitor.files;
		}
		catch (IOException ex)
		{
			LogHelper.error(ex.getMessage());
			ex.printStackTrace();
		}

		return output;
	}

	public static void createZipArchive(Path target, Path root, Path world_path) throws IOException
	{
		if (Files.exists(target))
		{
			LogHelper.error("Cannot create archive %s: file already exists", target);
			throw new IOException("Target file already exists");
		}

		FileOutputStream output = new FileOutputStream(target.toFile());
		ZipOutputStream zip = new ZipOutputStream(output);
		HashSet<Path> files = new HashSet<>(gatherFiles(root, blacklistMatchers));
		long last_break = System.currentTimeMillis();
		long pause_interval = ConfigHandler.pauseInterval * 1000;
		long pause_duration = ConfigHandler.pauseDuration * 1000;

		LogHelper.debug("Adding world save");
		files.addAll(gatherFiles(world_path, null));

		zip.setMethod(ZipOutputStream.DEFLATED);
		zip.setLevel(ConfigHandler.compressionLevel);

		for (Path file : files)
		{
			if (!Files.exists(file))
			{
				continue;
			}

			LogHelper.debug("Adding to archive: " + file);
			zip.putNextEntry(new ZipEntry(file.toString()));

			byte[] bytes = new byte[1024];
			int length;
			FileInputStream input = new FileInputStream(file.toFile());

			while ((length = input.read(bytes)) >= 0)
			{
				zip.write(bytes, 0, length);
			}

			zip.closeEntry();
			input.close();

			if (pause_interval > 0)
			{
				long current_time = System.currentTimeMillis();

				if (current_time >= (last_break + pause_interval))
				{
					LogHelper.debug("Taking a break for %dms", pause_duration);

					try
					{
						Thread.sleep(pause_duration);
					}
					catch (InterruptedException ex)
					{
						LogHelper.error("Thread got interrupted during backup creation!");
						ex.printStackTrace();
					}
					finally
					{
						last_break = System.currentTimeMillis();
					}
				}
			}
		}

		zip.close();
		output.close();
	}

	public static String generateArchiveFileName(boolean is_persistent)
	{
		String prefix = ConfigHandler.backupPrefix + FILENAME_SEPARATOR;
		String world_name = DimensionManager.getCurrentSaveRootDirectory().getName() + FILENAME_SEPARATOR;
		String persistent = (is_persistent ? "persistent" + FILENAME_SEPARATOR : "");
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));

		return String.format("%s%s%s%s.zip", prefix, world_name, persistent, timestamp).replace(" ", FILENAME_SEPARATOR);
	}

	public static String generateFileNameRegex()
	{
		String prefix = ConfigHandler.backupPrefix + FILENAME_SEPARATOR;
		String world_name = DimensionManager.getCurrentSaveRootDirectory().getName() + FILENAME_SEPARATOR;
		String persistent = "(persistent" + FILENAME_SEPARATOR + ")?";
		String timestamp = TIMESTAMP_REGEX_PATTERN;

		return String.format("%s%s%s%s\\.zip", prefix, world_name, persistent, timestamp).replace(" ", FILENAME_SEPARATOR);
	}
}
