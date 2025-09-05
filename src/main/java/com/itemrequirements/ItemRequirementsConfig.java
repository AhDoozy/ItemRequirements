package com.itemrequirements;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("itemrequirements")
public interface ItemRequirementsConfig extends Config
{
    @ConfigSection(
        name = "Tooltip",
        description = "Settings related to the tooltip",
        position = 0
    )
    String tooltipSection = "tooltipSection";

    @ConfigSection(
        name = "Indicator",
        description = "Settings related to the triangle indicator",
        position = 1
    )
    String indicatorSection = "indicatorSection";
    @Range(min = 10, max = 24)
    @ConfigItem(
            keyName = "tooltipTextSize",
            name = "Tooltip text size",
            description = "Font size for the requirements tooltip (points)",
            position = 1,
            section = tooltipSection
    )
    default int tooltipTextSize()
    {
        return 15;
    }

    @ConfigItem(
        keyName = "noRequirementsColor",
        name = "No requirements met color",
        description = "Color of the triangle when no requirements are met",
        position = 2,
        section = indicatorSection
    )
    default Color noRequirementsColor()
    {
        return new Color(255, 23, 23); // default red
    }

    @ConfigItem(
        keyName = "partialRequirementsColor",
        name = "Partial requirements met color",
        description = "Color of the triangle when some but not all requirements are met",
        position = 3,
        section = indicatorSection
    )
    default Color partialRequirementsColor()
    {
        return new Color(255, 206, 0); // default yellow
    }

    @Range(min = 0, max = 100)
    @ConfigItem(
        keyName = "tooltipOpacityPercent",
        name = "Tooltip opacity",
        description = "Opacity of the tooltip background (0–100%)",
        position = 4,
        section = tooltipSection
    )
    default int tooltipOpacityPercent()
    {
        return 80;
    }

    @ConfigItem(
        keyName = "triangleCorner",
        name = "Triangle corner",
        description = "Which corner to place the triangle indicator",
        position = 5,
        section = indicatorSection
    )
    default TriangleCorner triangleCorner()
    {
        return TriangleCorner.TOP_RIGHT;
    }
}