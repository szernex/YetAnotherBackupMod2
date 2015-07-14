package org.szernex.yabm2.util;

import org.szernex.yabm2.handler.ConfigHandler;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TreeSet;

public class ScheduleHelper
{
	public static ZonedDateTime getNextSchedule()
	{
		ZonedDateTime next_schedule;

		// Determine type of schedule
		if (ConfigHandler.backupInterval > 0) // Interval
		{
			next_schedule = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + (ConfigHandler.backupInterval * 60 * 1000)), ZoneId.systemDefault());
		}
		else // Schedule
		{
			if (ConfigHandler.backupSchedule.length == 0)
			{
				return null;
			}

			LocalTime now = LocalTime.now();
			LocalTime next_time = null;
			LocalDate day = LocalDate.now();
			TreeSet<LocalTime> times = new TreeSet<>();

			for (String s : ConfigHandler.backupSchedule)
			{
				times.add(LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm")));
			}

			for (LocalTime t : times) // try to find next scheduled time for today
			{
				if (t.compareTo(now) == 1)
				{
					next_time = t;
					break;
				}
			}

			if (next_time == null) // if we couldn't find one for today take the first schedule time for tomorrow
			{
				day = day.plusDays(1);
				next_time = times.first();
			}

			next_schedule = ZonedDateTime.of(day, next_time, ZoneId.systemDefault());
		}

		return next_schedule;
	}
}
