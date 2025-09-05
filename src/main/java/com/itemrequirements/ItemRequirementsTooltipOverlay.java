package com.itemrequirements;

import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.FontManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.List;

@Singleton
public class ItemRequirementsTooltipOverlay extends Overlay
{
	private final Client client;
	private final ItemRequirementsPlugin plugin;
	private final ItemRequirementsConfig config;

	private WidgetItem hoveredItem;
	private List<String> hoveredTooltipLines;
	private List<Boolean> hoveredTooltipMetStatus;

	@Inject
	public ItemRequirementsTooltipOverlay(Client client, ItemRequirementsPlugin plugin, ItemRequirementsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPosition(OverlayPosition.DYNAMIC);
	}

	public WidgetItem getHoveredItem()
	{
		return hoveredItem;
	}

	public List<String> getHoveredTooltipLines()
	{
		return hoveredTooltipLines;
	}

	public List<Boolean> getHoveredTooltipMetStatus()
	{
		return hoveredTooltipMetStatus;
	}

	public void setHoveredTooltip(WidgetItem item, List<String> lines, List<Boolean> met)
	{
		this.hoveredItem = item;
		this.hoveredTooltipLines = lines;
		this.hoveredTooltipMetStatus = met;
	}

	public void clearHoveredTooltip()
	{
		this.hoveredItem = null;
		this.hoveredTooltipLines = null;
		this.hoveredTooltipMetStatus = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		WidgetItem hovered = this.hoveredItem;
		if (hovered == null)
		{
			return null;
		}

		List<String> lines = this.hoveredTooltipLines;
		List<Boolean> metStatus = this.hoveredTooltipMetStatus;
		if (lines == null || lines.isEmpty() || metStatus == null || metStatus.isEmpty())
		{
			return null;
		}
		int count = Math.min(lines.size(), metStatus.size());
		if (count == 0)
		{
			return null;
		}

		Font base = FontManager.getRunescapeSmallFont();
		Font sized = base.deriveFont((float) config.tooltipTextSize());
		graphics.setFont(sized);
		FontMetrics fm = graphics.getFontMetrics();
		int lineHeight = fm.getHeight();

		int maxWidth = 0;
		for (int i = 0; i < count; i++)
		{
			maxWidth = Math.max(maxWidth, fm.stringWidth(lines.get(i)));
		}

		int paddingX = 8;
		int paddingY = 6;
		int boxWidth = maxWidth + 2 * paddingX;
		int boxHeight = lineHeight * count + 2 * paddingY;

		Point mouse = new Point(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY());
		int tooltipX = mouse.x - (boxWidth / 2);
		int tooltipY = mouse.y - (boxHeight / 2) - 25;

		// Clamp to screen
		int canvasWidth = client.getCanvasWidth();
		int canvasHeight = client.getCanvasHeight();
		if (tooltipX + boxWidth > canvasWidth) tooltipX = canvasWidth - boxWidth - 2;
		if (tooltipY + boxHeight > canvasHeight) tooltipY = canvasHeight - boxHeight - 2;
		if (tooltipX < 2) tooltipX = 2;
		if (tooltipY < 2) tooltipY = 2;

		int pct = Math.max(0, Math.min(100, config.tooltipOpacityPercent()));
		int alpha = (int) Math.round(255 * (pct / 100.0));
		graphics.setColor(new Color(60, 52, 41, alpha));
		graphics.fillRect(tooltipX, tooltipY, boxWidth, boxHeight);

		graphics.setColor(new Color(90, 82, 71));
		graphics.drawRect(tooltipX, tooltipY, boxWidth, boxHeight);

		int yOffset = tooltipY + paddingY + fm.getAscent();
		for (int i = 0; i < count; i++)
		{
			String line = lines.get(i);
			boolean met = metStatus.get(i);

			// Outline
			graphics.setColor(Color.BLACK);
			graphics.drawString(line, tooltipX + paddingX - 1, yOffset - 1);
			graphics.drawString(line, tooltipX + paddingX + 1, yOffset - 1);
			graphics.drawString(line, tooltipX + paddingX, yOffset - 2);
			graphics.drawString(line, tooltipX + paddingX, yOffset);

			// Text
			graphics.setColor(met ? new Color(0, 220, 0) : new Color(255, 65, 65));
			graphics.drawString(line, tooltipX + paddingX, yOffset - 1);

			yOffset += lineHeight;
		}

		return null;
	}

	public void renderItemOverlay(WidgetItem item, Point mouse, List<String> lines, List<Boolean> metStatus)
	{
		Rectangle bounds = item.getCanvasBounds();

		int s = Math.max(6, Math.min(bounds.width, bounds.height) / 3);
		int left = bounds.x + 1;
		int right = bounds.x + bounds.width - 1;
		int top = bounds.y + 1;
		int bottom = bounds.y + bounds.height - 1;
		int[] xs;
		int[] ys;
		switch (config.triangleCorner())
		{
			case TOP_LEFT:
				xs = new int[] { left, left + s, left };
				ys = new int[] { top,  top,      top + s };
				break;
			case BOTTOM_LEFT:
				xs = new int[] { left, left + s, left };
				ys = new int[] { bottom, bottom, bottom - s };
				break;
			case BOTTOM_RIGHT:
				xs = new int[] { right, right - s, right };
				ys = new int[] { bottom, bottom,   bottom - s };
				break;
			case TOP_RIGHT:
			default:
				xs = new int[] { right, right - s, right };
				ys = new int[] { top,   top,       top + s };
		}
		Polygon triangle = new Polygon(xs, ys, 3);

		if (triangle.contains(mouse))
		{
			this.setHoveredTooltip(item, lines, metStatus);
			plugin.markTooltipSetThisFrame();
			plugin.updateHoveredItem(item);
			return;
		}
	}
}
