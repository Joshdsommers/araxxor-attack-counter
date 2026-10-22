package com.araxxorattackcounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AraxxorAttackCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AraxxorAttackCounterPlugin.class);
		RuneLite.main(args);
	}
}
