package org.szernex.yabm2.util;

import org.szernex.yabm2.handler.ConfigHandler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class BackupManagingHelper
{
	private static class BackupFileVisitor extends SimpleFileVisitor<Path>
	{
		private HashSet<Path> files = new HashSet<>();
		private PathMatcher matcher;

		public BackupFileVisitor(String pattern)
		{
			matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);
		}

		public HashSet<Path> getFiles()
		{
			return files;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			if (matcher.matches(file.getFileName()))
				files.add(file);


			return FileVisitResult.CONTINUE;
		}
	}

	private static class LastModifiedComparator implements Comparator<Path>
	{
		@Override
		public int compare(Path o1, Path o2)
		{
			try
			{
				LocalDateTime dt_o1 = LocalDateTime.ofInstant(Files.getLastModifiedTime(o1).toInstant(), ZoneId.systemDefault());
				LocalDateTime dt_o2 = LocalDateTime.ofInstant(Files.getLastModifiedTime(o2).toInstant(), ZoneId.systemDefault());

				return (dt_o1.compareTo(dt_o2));
			}
			catch (IOException ex)
			{
				return 0;
			}
		}
	}

	public static boolean isPersistent()
	{
		Path backup_dir = Paths.get(ConfigHandler.backupPath).toAbsolutePath().normalize();
		Path persistent_dir = Paths.get(ConfigHandler.persistentPath).toAbsolutePath().normalize();
		String pattern = BackupCreationHelper.generateFileNameRegex();
		BackupFileVisitor fileVisitor = new BackupFileVisitor(pattern);
		HashSet<Path> files = new HashSet<>();

		LogHelper.info("Checking directories for backups created today: %s %s", backup_dir, persistent_dir);
		LogHelper.debug("Pattern used: " + pattern);

		try
		{
			Files.walkFileTree(backup_dir, new HashSet<>(), 1, fileVisitor);
			Files.walkFileTree(persistent_dir, new HashSet<>(), 1, fileVisitor);
			files.addAll(fileVisitor.getFiles());
		}
		catch (IOException ex)
		{
			LogHelper.error(ex.getMessage());
			ex.printStackTrace();
			return false;
		}

		LocalDate today = LocalDate.now();

		for (Path file : files)
		{
			try
			{
				LocalDate last_modified = LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault()).toLocalDate();

				if (today.equals(last_modified))
					return false;
			}
			catch (IOException ex)
			{
				LogHelper.warn("Could not read file attributes of %s: %s", file, ex.getMessage());
			}
		}

		return true;
	}

	public static void consolidateBackups(int max_count, Path backup_dir)
	{
		if (max_count == 0)
			return;

		backup_dir = backup_dir.toAbsolutePath().normalize();
		String pattern = BackupCreationHelper.generateFileNameRegex();
		BackupFileVisitor fileVisitor = new BackupFileVisitor(pattern);
		TreeSet<Path> files = new TreeSet<>(new LastModifiedComparator());

		LogHelper.info("Gathering files for consolidation from backup directory " + backup_dir);
		LogHelper.debug("Pattern used: " + pattern);

		try
		{
			Files.walkFileTree(backup_dir, new HashSet<>(), 1, fileVisitor);
			files.addAll(fileVisitor.getFiles());
		}
		catch (IOException ex)
		{
			LogHelper.error(ex.getMessage());
			ex.printStackTrace();
		}

		int delete_count = 0;

		while (files.size() > max_count)
		{
			Path file = files.pollFirst();

			LogHelper.debug("Deleting old backup: " + file);

			try
			{
				Files.deleteIfExists(file);
				delete_count++;
			}
			catch (IOException ex)
			{
				LogHelper.error("Could not delete backup %s: %s", file, ex.getMessage());
			}
		}

		LogHelper.info("Deleted %d old backup(s) in %s", delete_count, backup_dir);
	}
}
