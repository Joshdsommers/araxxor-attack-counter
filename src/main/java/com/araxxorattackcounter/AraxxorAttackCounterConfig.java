package com.araxxorattackcounter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(AraxxorAttackCounterConfig.GROUP)
public interface AraxxorAttackCounterConfig extends Config
{
	String GROUP = "araxxorattackcounter";

	@ConfigItem(
		keyName = "warningAttack",
		name = "Warning attack",
		description = "Attack number that is highlighted before the special attack",
		position = 0
	)
	@Range(min = 1, max = 20)
	default int warningAttack()
	{
		return 6;
	}

	@ConfigItem(
		keyName = "showSpecialName",
		name = "Show special name",
		description = "On the warning attack, show which special is incoming instead of the count",
		position = 1
	)
	default boolean showSpecialName()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "normalColor",
		name = "Count color",
		description = "Color of the attack count",
		position = 2
	)
	default Color normalColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		keyName = "warningColor",
		name = "Warning color",
		description = "Color of the attack count on the warning attack",
		position = 3
	)
	default Color warningColor()
	{
		return Color.RED;
	}

	@Range(min = 8, max = 48)
	@ConfigItem(
		keyName = "fontSize",
		name = "Font size",
		description = "Size of the attack count text",
		position = 4
	)
	default int fontSize()
	{
		return 24;
	}

	@ConfigItem(
		keyName = "heightOffset",
		name = "Height offset",
		description = "Vertical offset of the count above Araxxor",
		position = 5
	)
	default int heightOffset()
	{
		return 150;
	}
}
