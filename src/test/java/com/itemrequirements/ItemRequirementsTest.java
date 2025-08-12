package com.itemrequirements;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ItemRequirementsTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ItemRequirementsPlugin.class);
		RuneLite.main(args);
	}
}