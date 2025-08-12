package com.equipmentrequirements;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
	name = "Equipment Requirements",
	description = "Shows skill and quest requirements for items in overlays and tooltips."
)
public class EquipmentRequirementsPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private EquipmentRequirementsOverlay overlay;

	@Inject
	private EquipmentRequirementsTooltipOverlay tooltipOverlay;

	private boolean tooltipSetThisFrame = false;

	private WidgetItem lastHoveredItem = null;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Equipment Requirements started!");
		EquipmentRequirementsData.loadFromJson();
		overlayManager.add(overlay);
		overlayManager.add(tooltipOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Equipment Requirements stopped!");
		overlayManager.remove(overlay);
		overlayManager.remove(tooltipOverlay);
	}


	public void reloadRequirements()
	{
		EquipmentRequirementsData.loadFromJson();
		log.info("Equipment requirements reloaded.");
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		String command = event.getCommand();
		if ("reloadreq".equalsIgnoreCase(command))
		{
			reloadRequirements();
		}
	}

	@Provides
	EquipmentRequirementsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EquipmentRequirementsConfig.class);
	}
    public EquipmentRequirementsTooltipOverlay getTooltipOverlay()
    {
        return tooltipOverlay;
    }

    public void markTooltipSetThisFrame()
    {
        this.tooltipSetThisFrame = true;
    }

    public void resetTooltipFlag()
    {
        this.tooltipSetThisFrame = false;
    }

    public boolean wasTooltipSetThisFrame()
    {
        return this.tooltipSetThisFrame;
    }

    public void updateHoveredItem(WidgetItem currentItem)
    {
        if (lastHoveredItem != null && currentItem != lastHoveredItem)
        {
            log.debug("Stopped hovering item: {}", lastHoveredItem.getId());
        }

        lastHoveredItem = currentItem;
    }

    @Subscribe
    public void onClientTick(ClientTick tick)
    {
        if (!tooltipSetThisFrame)
        {
            tooltipOverlay.clearHoveredTooltip();
        }
        resetTooltipFlag();
    }
}
