/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.itemstatsimproved;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PluginDescriptor(
	name = "Item Stats Improved",
	description = "Show information about food and potion effects",
	tags = {"food", "inventory", "overlay", "potion"}
)
public class ItemStatsImprovedPlugin extends Plugin
{
	private static final int ORANGE_TEXT = JagexColors.DARK_ORANGE_INTERFACE_TEXT.getRGB();
	private static final int YELLOW_TEXT = JagexColors.YELLOW_INTERFACE_TEXT.getRGB();
	private static final int TEXT_HEIGHT = 11;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemStatsImprovedOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ItemStatsImprovedConfig config;

	@Inject
	private ClientThread clientThread;
	@Inject
	private KeyManager keyManager;

	@Inject
	private PluginManager pluginManager;
	private Plugin targetPlugin = null;

	private Widget itemInformationTitle;

	// -----------------------------------------------------------------------
	// Entry Point
	// -----------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		net.runelite.client.externalplugins.ExternalPluginManager.loadBuiltin(ItemStatsImprovedPlugin.class);
		net.runelite.client.RuneLite.main(args);
	}

	@Provides
	ItemStatsImprovedConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ItemStatsImprovedConfig.class);
	}

	@Override
	public void configure(Binder binder)
	{
		binder.bind(ItemStatChangesService.class).to(com.itemstatsimproved.ItemStatChangesServiceImpl.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(overlay);
		overlayManager.add(overlay);

		SwingUtilities.invokeLater(() -> {
			for (Plugin plugin : pluginManager.getPlugins()) {
				// "ItemStatPlugin" is the exact class name for Item Stats
				if (plugin.getClass().getSimpleName().equals("ItemStatPlugin")) {
					targetPlugin = plugin;
					break;
				}
			}

			if (targetPlugin != null && pluginManager.isPluginEnabled(targetPlugin)) {
				try {
					pluginManager.setPluginEnabled(targetPlugin, false);
					pluginManager.stopPlugin(targetPlugin); // Forces the plugin to physically stop
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(overlay);
		overlayManager.remove(overlay);
		clientThread.invokeLater(this::resetGEInventory);

		SwingUtilities.invokeLater(() -> {
			if (targetPlugin != null && !pluginManager.isPluginEnabled(targetPlugin)) {
				try {
					pluginManager.setPluginEnabled(targetPlugin, true);
					pluginManager.startPlugin(targetPlugin); // Forces the plugin to physically restart
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getKey().equals("geStats"))
		{
			clientThread.invokeLater(this::resetGEInventory);
		}
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		Plugin changedPlugin = event.getPlugin();

		// 1. Check if the plugin that just changed is "ItemStatPlugin"
		if (changedPlugin.getClass().getSimpleName().equals("ItemStatPlugin"))
		{
			// 2. Check if it was ENABLED while your plugin is also running
			if (event.isLoaded() && pluginManager.isPluginEnabled(this))
			{
				// 3. Deflect the shutdown to the next frame to prevent internal thread deadlocks
				SwingUtilities.invokeLater(() ->
				{
					try
					{
						// Turn off your own plugin
						pluginManager.setPluginEnabled(this, false);
						pluginManager.stopPlugin(this);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				});
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (itemInformationTitle != null && config.geStats()
			&& (client.getWidget(InterfaceID.GeOffers.UNIVERSE) == null
			|| client.getWidget(InterfaceID.GeOffers.UNIVERSE).isHidden()))
		{
			resetGEInventory();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == VarPlayerID.TRADINGPOST_SEARCH && config.geStats())
		{
			resetGEInventory();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.GE_OFFERS_SETUP_BUILD && config.geStats())
		{
			int currentGeItem = client.getVarpValue(VarPlayerID.TRADINGPOST_SEARCH);
			if (currentGeItem != -1 && client.getVarbitValue(VarbitID.GE_NEWOFFER_TYPE) == 0)
			{
				createItemInformation(currentGeItem);
			}
		}
	}

	private void createItemInformation(int id)
	{
		final ItemStats itemStats = itemManager.getItemStats(id);

		if (itemStats == null || !itemStats.isEquipable())
		{
			return;
		}

		final ItemEquipmentStats equipmentStats = itemStats.getEquipment();

		if (equipmentStats == null)
		{
			return;
		}

		final Widget geInv = client.getWidget(InterfaceID.GeOffersSide.ITEMS);

		if (geInv == null)
		{
			return;
		}

		final Widget invContainer = getInventoryContainer();

		if (invContainer == null)
		{
			return;
		}

		invContainer.deleteAllChildren();
		geInv.setHidden(true);

		int yPos = 0;

		final FontMetrics smallFM = client.getCanvas().getFontMetrics(FontManager.getRunescapeSmallFont());

		// HEADER

		itemInformationTitle = createText(invContainer, "Item Information", FontID.BOLD_12, ORANGE_TEXT,
			8, 8, invContainer.getWidth(), 16);
		itemInformationTitle.setYTextAlignment(WidgetTextAlignment.CENTER);

		Widget closeButton = invContainer.createChild(-1, WidgetType.GRAPHIC);
		closeButton.setOriginalY(8);
		closeButton.setOriginalX(invContainer.getWidth() - 24);
		closeButton.setOriginalHeight(16);
		closeButton.setOriginalWidth(16);
		closeButton.setSpriteId(SpriteID.V2StoneCloseButton.BUTTON);
		closeButton.setAction(0, "Close");
		closeButton.setOnMouseOverListener((JavaScriptCallback) (ev) ->
		{
			closeButton.setSpriteId(SpriteID.V2StoneCloseButton.HOVERED);
		});
		closeButton.setOnMouseLeaveListener((JavaScriptCallback) (ev) ->
		{
			closeButton.setSpriteId(SpriteID.V2StoneCloseButton.BUTTON);
		});
		closeButton.setOnOpListener((JavaScriptCallback) (ev) -> resetGEInventory());
		closeButton.setHasListener(true);
		closeButton.revalidate();

		yPos += 15;

		createSeparator(invContainer, yPos);

		// ICON AND TITLE

		yPos += 25;

		Widget icon = invContainer.createChild(-1, WidgetType.GRAPHIC);
		icon.setOriginalX(8);
		icon.setOriginalY(yPos);
		icon.setOriginalWidth(Constants.ITEM_SPRITE_WIDTH);
		icon.setOriginalHeight(Constants.ITEM_SPRITE_HEIGHT);
		icon.setItemId(id);
		icon.setItemQuantityMode(0);
		icon.setBorderType(1);
		icon.revalidate();

		Widget itemName = createText(invContainer, itemManager.getItemComposition(id).getName(), FontID.PLAIN_12, ORANGE_TEXT,
			50, yPos, invContainer.getWidth() - 40, 30);
		itemName.setYTextAlignment(WidgetTextAlignment.CENTER);

		yPos += 20;

		createSeparator(invContainer, yPos);

		// STATS HEADER

		yPos += 25;

		createText(invContainer, "Attack", FontID.PLAIN_11, ORANGE_TEXT, 5, yPos, 50, -1);

		int defenceXPos = invContainer.getWidth() - (smallFM.stringWidth("Defence") + 5);
		createText(invContainer, "Defence", FontID.PLAIN_11, ORANGE_TEXT, defenceXPos, yPos, 50, -1);

		// STYLE BONUSES

		final Set<String> stats = ImmutableSet.of(
			"Stab",
			"Slash",
			"Crush",
			"Magic",
			"Ranged"
		);

		final List<Integer> attackStats = ImmutableList.of(
			equipmentStats.getAstab(),
			equipmentStats.getAslash(),
			equipmentStats.getAcrush(),
			equipmentStats.getAmagic(),
			equipmentStats.getArange()
		);

		final List<Integer> defenceStats = ImmutableList.of(
			equipmentStats.getDstab(),
			equipmentStats.getDslash(),
			equipmentStats.getDcrush(),
			equipmentStats.getDmagic(),
			equipmentStats.getDrange()
		);

		int index = 0;

		for (final String stat : stats)
		{
			yPos += TEXT_HEIGHT + 2;

			// Style label
			final Widget styleText = createText(invContainer, stat, FontID.PLAIN_11, ORANGE_TEXT,
				0, yPos, invContainer.getWidth(), -1);
			styleText.setXTextAlignment(WidgetTextAlignment.CENTER);

			// Attack bonus
			createText(invContainer, attackStats.get(index).toString(), FontID.PLAIN_11, YELLOW_TEXT,
				5, yPos, 50, -1);

			// Defence bonus
			final int defenceX = invContainer.getWidth() - (smallFM.stringWidth(defenceStats.get(index).toString()) + 5);
			createText(invContainer, defenceStats.get(index).toString(), FontID.PLAIN_11, YELLOW_TEXT,
				defenceX, yPos, 50, -1);

			index++;
		}

		// MISC BONUSES

		yPos += TEXT_HEIGHT + 8;

		final Map<String, Object> miscStats = ImmutableMap.of(
			"Strength", equipmentStats.getStr(),
			"Ranged Strength", equipmentStats.getRstr(),
			"Magic Damage", equipmentStats.getMdmg(),
			"Prayer Bonus", equipmentStats.getPrayer()
		);

		for (final Map.Entry<String, Object> miscStat : miscStats.entrySet())
		{
			final String name = miscStat.getKey();
			final String value = miscStat.getValue().toString();

			// Stat label
			createText(invContainer, name, FontID.PLAIN_11, ORANGE_TEXT, 5, yPos, 50, -1);

			// Stat bonus
			int valueXPos = invContainer.getWidth() - (smallFM.stringWidth(value) + 5);
			createText(invContainer, value, FontID.PLAIN_11, YELLOW_TEXT, valueXPos, yPos, 50, -1);

			yPos += TEXT_HEIGHT + 2;
		}

		// COINS

		createSeparator(invContainer, invContainer.getHeight() - 40);

		final String coinText = "You have " + QuantityFormatter.quantityToStackSize(getCurrentGP())
			+ (getCurrentGP() == 1 ? " coin." : " coins.");

		final Widget coinWidget = createText(invContainer, coinText, FontID.PLAIN_12, ORANGE_TEXT,
			0, invContainer.getHeight() - 18, invContainer.getWidth(), -1);

		coinWidget.setXTextAlignment(WidgetTextAlignment.CENTER);
	}

	private static Widget createText(Widget parent, String text, int fontId, int textColor,
								int x, int y, int width, int height)
	{
		final Widget widget = parent.createChild(-1, WidgetType.TEXT);
		widget.setText(text);
		widget.setFontId(fontId);
		widget.setTextColor(textColor);
		widget.setTextShadowed(true);
		widget.setOriginalHeight(height == -1 ? TEXT_HEIGHT : height);
		widget.setOriginalWidth(width);
		widget.setOriginalY(y);
		widget.setOriginalX(x);
		widget.revalidate();
		return widget;
	}

	private static void createSeparator(Widget parent, int y)
	{
		Widget separator = parent.createChild(-1, WidgetType.GRAPHIC);
		separator.setOriginalWidth(parent.getWidth());
		separator.setOriginalY(y);
		separator.setOriginalHeight(32);
		separator.setSpriteId(SpriteID.V2BordersSlim.HORIZONTAL_C);
		separator.revalidate();
	}

	private void resetGEInventory()
	{
		final Widget invContainer = getInventoryContainer();

		if (invContainer == null)
		{
			return;
		}

		if (itemInformationTitle != null && invContainer.getChild(0) == itemInformationTitle)
		{
			invContainer.deleteAllChildren();
			itemInformationTitle = null;
		}

		final Widget geInv = client.getWidget(InterfaceID.GeOffersSide.ITEMS);
		if (geInv != null)
		{
			geInv.setHidden(false);
		}
	}

	private int getCurrentGP()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INV);

		if (inventory == null)
		{
			return 0;
		}

		return inventory.count(ItemID.COINS);
	}

	private Widget getInventoryContainer()
	{
		if (client.isResized())
		{
			if (client.getVarbitValue(VarbitID.RESIZABLE_STONE_ARRANGEMENT) == 1)
			{
				return client.getWidget(InterfaceID.ToplevelPreEoc.SIDE3);
			}
			else
			{
				return client.getWidget(InterfaceID.ToplevelOsrsStretch.SIDE3);
			}
		}
		else
		{
			return client.getWidget(InterfaceID.Toplevel.SIDE3);
		}
	}

	private void disableTargetPlugin(String pluginName) {
		for (Plugin plugin : pluginManager.getPlugins()) {
			if (plugin.getClass().getSimpleName().equalsIgnoreCase(pluginName)) {
				pluginManager.setPluginEnabled(plugin, false);
			}
		}
	}

	private void enableTargetPlugin(String pluginName) {
		for (Plugin plugin : pluginManager.getPlugins()) {
			if (plugin.getClass().getSimpleName().equalsIgnoreCase(pluginName)) {
				pluginManager.setPluginEnabled(plugin, true);
			}
		}
	}
}
