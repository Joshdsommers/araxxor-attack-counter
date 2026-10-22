package com.araxxorattackcounter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class AraxxorAttackCounterOverlay extends Overlay
{
	private final AraxxorAttackCounterPlugin plugin;
	private final AraxxorAttackCounterConfig config;

	@Inject
	AraxxorAttackCounterOverlay(AraxxorAttackCounterPlugin plugin, AraxxorAttackCounterConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		NPC araxxor = plugin.getAraxxor();
		if (araxxor == null || araxxor.isDead() || plugin.isEnraged())
		{
			return null;
		}

		int count = plugin.getAttackCount();
		boolean warning = count >= config.warningAttack();
		AraxxorSpecial special = plugin.getSpecial();
		String text = warning && special != null && config.showSpecialName()
			? special.getDisplayName() + " incoming"
			: String.valueOf(count);
		Color color = warning ? config.warningColor() : config.normalColor();

		Font previous = graphics.getFont();
		graphics.setFont(FontManager.getRunescapeBoldFont().deriveFont((float) config.fontSize()));

		Point location = araxxor.getCanvasTextLocation(graphics, text, araxxor.getLogicalHeight() + config.heightOffset());
		if (location != null)
		{
			OverlayUtil.renderTextLocation(graphics, location, text, color);
		}

		graphics.setFont(previous);
		return null;
	}
}
