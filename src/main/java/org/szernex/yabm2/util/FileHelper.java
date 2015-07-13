package org.szernex.yabm2.util;

import org.szernex.yabm2.handler.ConfigHandler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileHelper
{
	protected static HashSet<PathMatcher> blacklistMatchers = new HashSet<>();

	private static class BackupFileVisitor extends SimpleFileVisitor<Path>
	{
		private HashSet<Path> files;

		public BackupFileVisitor()
		{
			files = new HashSet<>();
		}

		public HashSet<Path> getFiles()
		{
			return files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			for (PathMatcher matcher : FileHelper.blacklistMatchers)
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
		blacklistMatchers.clear();

		HashSet<String> patterns = new HashSet<>(Arrays.asList(ConfigHandler.blacklist)); // to make sure there are no duplicate patterns

		for (String pattern : patterns)
		{
			blacklistMatchers.add(FileSystems.getDefault().getPathMatcher("regex:" + pattern));
		}
	}

	public static Set<Path> gatherFiles(Path root)
	{
		HashSet<Path> output = new HashSet<>();
		BackupFileVisitor fileVisitor = new BackupFileVisitor();

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
}
