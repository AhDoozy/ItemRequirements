package com.itemrequirements;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ClientTick;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Slf4j
public class ItemRequirementsOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final ItemManager itemManager;
	private final ItemRequirementsPlugin plugin;
	private static final Font INFO_FONT = FontManager.getRunescapeSmallFont();
	private static final Color ICON_RED = new Color(255, 23, 23);
	private static final Color OUTLINE_BLACK = Color.BLACK;

	@Inject
	public ItemRequirementsOverlay(
		Client client,
		ItemManager itemManager,
		ItemRequirementsPlugin plugin
	)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.plugin = plugin;
		showOnInventory();
		showOnBank();
		showOnInterfaces(InterfaceID.SHOP, InterfaceID.GRAND_EXCHANGE);
	}

    /**
     * Clamp the tooltip position to ensure the tooltip box stays within the canvas.
     */
    private Point clampTooltipPosition(int x, int y, int boxWidth, int boxHeight)
    {
        int canvasWidth = client.getCanvasWidth();
        int canvasHeight = client.getCanvasHeight();

        if (x + boxWidth > canvasWidth)
        {
            x = canvasWidth - boxWidth - 2;
        }
        if (x < 0)
        {
            x = 2;
        }

        if (y < 0)
        {
            y = 2;
        }
        if (y + boxHeight > canvasHeight)
        {
            y = canvasHeight - boxHeight - 2;
        }

        return new Point(x, y);
    }

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem item)
	{
		net.runelite.api.Point mousePos = client.getMouseCanvasPosition();
		if (mousePos == null)
		{
			return;
		}
		Point mouse = new Point(mousePos.getX(), mousePos.getY());

		if (item.getWidget() == null)
		{
			return;
		}

		if (client.getWidget(320) != null || client.getWidget(1212) != null || client.getWidget(210) != null)
		{
			return; // Skip overlay if Skill Guide is open
		}

		Rectangle bounds = item.getCanvasBounds();
		int lookupId = itemManager.canonicalize(itemId);
		String itemName = Text.removeTags(itemManager.getItemComposition(lookupId).getName());

		List<Requirement> requirements = ItemRequirementsData.ITEM_REQUIREMENTS_BY_ID.get(lookupId);
		if (requirements == null || requirements.isEmpty())
		{
			requirements = ItemRequirementsData.ITEM_REQUIREMENTS.get(itemName);
			if (requirements == null || requirements.isEmpty())
			{
				return;
			}
		}
		boolean unmet = false;

		// Prepare lines and their met status
		List<String> lines = new ArrayList<>();
		List<Boolean> metStatus = new ArrayList<>();

		for (Requirement req : requirements)
		{
			boolean met = req.isMet(client);
			if (!met)
			{
				unmet = true;
			}
			lines.add(req.getMessage());
			metStatus.add(met);
		}

		// Only show icon if there are unmet requirements
		if (!unmet)
		{
			return;
		}

		Font scaledInfo = INFO_FONT.deriveFont(java.awt.geom.AffineTransform.getScaleInstance(2.25, 2.25));
		graphics.setFont(scaledInfo);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		FontMetrics fm = graphics.getFontMetrics(scaledInfo);
		int textWidth = fm.stringWidth("i");
		int x = bounds.x + bounds.width - textWidth - 4;
		int y = bounds.y + bounds.height - 4;

		// Draw outline
		graphics.setColor(OUTLINE_BLACK);
		graphics.drawString("i", x - 1, y);
		graphics.drawString("i", x + 1, y);
		graphics.drawString("i", x, y - 1);
		graphics.drawString("i", x, y + 1);

		// Draw main "i" with color based on unmet status
		graphics.setColor(ICON_RED);
		graphics.drawString("i", x, y);

		if (item.getCanvasBounds().contains(mouse))
		{
			plugin.getTooltipOverlay().renderItemOverlay(item, mouse, lines, metStatus);
			plugin.markTooltipSetThisFrame();
			plugin.updateHoveredItem(item);
		}

	}
    @Subscribe
    public void onBeforeRender(BeforeRender event)
    {
        plugin.resetTooltipFlag();
    }

    @Subscribe
    public void onClientTick(ClientTick event)
    {
        if (!plugin.wasTooltipSetThisFrame())
        {
            plugin.getTooltipOverlay().clearHoveredTooltip();
        }
    }
}