package org.szernex.yabm2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringHelper
{
	public static List<String> getWordsStartingWith(String beginning, Collection<String> list)
	{
		List<String> output = new ArrayList<>();

		for (String s : list)
		{
			if (s.toLowerCase().startsWith(beginning.toLowerCase()))
			{
				output.add(s);
			}
		}

		return output;
	}
}
