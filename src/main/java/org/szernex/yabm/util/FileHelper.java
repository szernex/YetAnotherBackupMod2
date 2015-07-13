package org.szernex.yabm.util;

import org.szernex.yabm.handler.ConfigHandler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class FileHelper
{
	public static class WhitelistFileVisitor extends SimpleFileVisitor<Path>
	{
		private String filePattern;
		private Path rootDir;
		private HashSet<Path> files;
		private PathMatcher pathMatcher;

		public  WhitelistFileVisitor(Path root, String pattern)
		{
			rootDir = root;
			files = new HashSet<>();
			pathMatcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);
		}

		public HashSet<Path> getFiles()
		{
			return files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			LogHelper.info(file);
			if (pathMatcher.matches(file))
				files.add(file);

			return FileVisitResult.CONTINUE;
		}
	}

	public static Set<Path> gatherFiles(Path root)
	{
		HashSet<Path> output = new HashSet<>();

		String whitelist_pattern = String.format("^.*((%s))$", String.join(")|(", ConfigHandler.whitelist));
		WhitelistFileVisitor whitelist_visitor = new WhitelistFileVisitor(root, whitelist_pattern);

		LogHelper.info(whitelist_pattern);

		try
		{
			Files.walkFileTree(root, whitelist_visitor);
			output = whitelist_visitor.files;
		}
		catch (IOException ex)
		{
			LogHelper.error(ex.getMessage());
			ex.printStackTrace();
		}

		LogHelper.info(output.toString());

		return output;
	}
}
