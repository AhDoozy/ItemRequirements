package com.itemrequirements;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ClientTick;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.eventbus.Subscribe;
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
    private final ItemRequirementsConfig config;
    private static final Font INFO_FONT = FontManager.getRunescapeSmallFont();
    private static final Color ICON_RED = new Color(255, 23, 23);
    private static final Color ICON_YELLOW = new Color(255, 206, 0);
    private static final Color OUTLINE_BLACK = Color.BLACK;

    @Inject
    public ItemRequirementsOverlay(
        Client client,
        ItemManager itemManager,
        ItemRequirementsPlugin plugin,
        ItemRequirementsConfig config
    )
    {
        this.client = client;
        this.itemManager = itemManager;
        this.plugin = plugin;
        this.config = config;
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
        int metCount = 0;

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
            else
            {
                metCount++;
            }
            lines.add(req.getMessage());
            metStatus.add(met);
        }
        int totalCount = requirements.size();

        // Only show icon if there are unmet requirements
        if (!unmet)
        {
            return;
        }

        // Determine triangle color from config: no-reqs vs partial-reqs
        Color triangleColor = (metCount == 0) ? config.noRequirementsColor() : config.partialRequirementsColor();

        // Enable antialiasing for smoother shapes
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Compute a small right-angle triangle in the configured corner
        int s = Math.max(6, Math.min(bounds.width, bounds.height) / 3); // size
        int[] xs;
        int[] ys;
        TriangleCorner corner = config.triangleCorner();
        int left = bounds.x + 1;
        int right = bounds.x + bounds.width - 1;
        int top = bounds.y + 1;
        int bottom = bounds.y + bounds.height - 1;

        switch (corner)
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

        // Fill triangle (opaque)
        graphics.setColor(triangleColor);
        graphics.fillPolygon(xs, ys, 3);

        // Outline triangle for contrast
        graphics.setColor(OUTLINE_BLACK);
        graphics.drawPolygon(xs, ys, 3);

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