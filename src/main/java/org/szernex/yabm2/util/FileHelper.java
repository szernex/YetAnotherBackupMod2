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

public class FileHelper
{
	public static final String FILENAME_SEPARATOR = "_";
	public static final String DATETIMEFORMAT_PATTERN = "YYYY-MM-dd" + FILENAME_SEPARATOR + "HH-mm-ss";

	protected static HashSet<PathMatcher> blacklistMatchers = new HashSet<>();

	private static class BackupFileVisitor extends SimpleFileVisitor<Path>
	{
		private HashSet<Path> files = new HashSet<>();
		private Set<PathMatcher> matchers = new HashSet<>();

		public BackupFileVisitor()
		{
			this(null);
		}

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

			LogHelper.info("Including " + file);
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
		String separator = FileSystems.getDefault().getSeparator();
		blacklistMatchers.clear();
		// exclude saves directory by default in case of this being a single player instance, only the current save will get included
		blacklistMatchers.add(FileSystems.getDefault().getPathMatcher("regex:.*\\" + separator + "saves\\" + separator + ".*"));

		HashSet<String> patterns = new HashSet<>(Arrays.asList(ConfigHandler.blacklist)); // to make sure there are no duplicate patterns

		for (String pattern : patterns)
		{
			blacklistMatchers.add(FileSystems.getDefault().getPathMatcher("regex:" + pattern));
		}
	}

	public static Set<Path> gatherFiles(Path root, boolean use_blacklist)
	{
		HashSet<Path> output = new HashSet<>();
		BackupFileVisitor fileVisitor = new BackupFileVisitor((use_blacklist ? blacklistMatchers : null));

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

	public static boolean createZipArchive(Path target, Path root, Path world_path) throws IOException
	{
		if (Files.exists(target))
		{
			LogHelper.error("Cannot create archive %s: file already exists", target);
			return false;
		}

		FileOutputStream output = new FileOutputStream(target.toFile());
		ZipOutputStream zip = new ZipOutputStream(output);
		HashSet<Path> files = new HashSet<>(gatherFiles(root, true));

		files.addAll(gatherFiles(world_path, false));

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
		}

		zip.close();
		output.close();

		return true;
	}

	public static String generateArchiveFileName()
	{
		String prefix = ConfigHandler.backupPrefix;
		String world_name = DimensionManager.getCurrentSaveRootDirectory().getName();
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIMEFORMAT_PATTERN));

		return String.format("%2$s%1$s%3$s%1$s%4$s.zip", FILENAME_SEPARATOR, prefix, world_name, timestamp).replace(" ", FILENAME_SEPARATOR);
	}
}
