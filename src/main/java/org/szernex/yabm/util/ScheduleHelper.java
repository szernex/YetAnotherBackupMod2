package org.szernex.yabm.util;

import org.szernex.yabm.handler.ConfigHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

public class ScheduleHelper
{
	public static Date getNextSchedule()
	{
		Date output;

		// Determine type of schedule
		if (ConfigHandler.backupInterval > 0) // Interval
		{
			output = new Date(System.currentTimeMillis() + (ConfigHandler.backupInterval * 60 * 1000));
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
			LocalDateTime next_schedule;
			TreeSet<LocalTime> times = new TreeSet<>();

			for (String s : ConfigHandler.backupSchedule)
			{
				times.add(LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm")));
			}

			Iterator<LocalTime> iterator = times.iterator();

			while (iterator.hasNext()) // try to find next scheduled time for today
			{
				LocalTime t = iterator.next();

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

			next_schedule = LocalDateTime.of(day, next_time);
			output = Date.from(next_schedule.atZone(ZoneId.systemDefault()).toInstant());
		}

		return output;
	}
}
