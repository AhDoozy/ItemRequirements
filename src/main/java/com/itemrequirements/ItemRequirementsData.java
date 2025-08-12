package com.itemrequirements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRequirementsData
{
    private static final Logger log = LoggerFactory.getLogger(ItemRequirementsData.class);
    public static final Map<String, List<Requirement>> ITEM_REQUIREMENTS = new HashMap<>();
    public static final Map<Integer, List<Requirement>> ITEM_REQUIREMENTS_BY_ID = new HashMap<>();

    private static class SkillEntry {
        int id;
        String skill;
        int level;
    }

    static {
        loadFromJson();
    }

    public static void loadFromJson() {
        log.info("Loading item requirements from Items-Information.json");
        ITEM_REQUIREMENTS.clear();
        ITEM_REQUIREMENTS_BY_ID.clear();
        Gson gson = new Gson();

        InputStream infoStream = ItemRequirementsData.class.getResourceAsStream("/Items-Information.json");
        if (infoStream == null) {
            infoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Items-Information.json");
        }
        if (infoStream == null) {
            infoStream = ItemRequirementsData.class.getResourceAsStream("/com/itemrequirements/Items-Information.json");
        }
        if (infoStream == null) {
            infoStream = ItemRequirementsData.class.getResourceAsStream("/com/equipmentrequirements/Items-Information.json");
            if (infoStream != null) {
                log.warn("Loaded Items-Information.json from legacy path /com/equipmentrequirements/. Please move it to /com/itemrequirements/.");
            }
        }
        if (infoStream == null) {
            throw new RuntimeException("Resource Items-Information.json not found in classpath");
        }

        try (InputStreamReader reader = new InputStreamReader(infoStream)) {
            JsonElement rootElement = gson.fromJson(reader, JsonElement.class);
            JsonArray itemsArray;
            if (rootElement.isJsonArray()) {
                itemsArray = rootElement.getAsJsonArray();
            } else if (rootElement.isJsonObject()) {
                JsonObject rootObj = rootElement.getAsJsonObject();
                if (rootObj.has("items") && rootObj.get("items").isJsonArray()) {
                    itemsArray = rootObj.getAsJsonArray("items");
                } else {
                    itemsArray = new JsonArray();
                    for (Map.Entry<String, JsonElement> entry : rootObj.entrySet()) {
                        if (!entry.getValue().isJsonObject()) {
                            continue;
                        }
                        JsonObject itemObjEntry = entry.getValue().getAsJsonObject();
                        try {
                            int implicitId = Integer.parseInt(entry.getKey());
                            itemObjEntry.addProperty("id", implicitId);
                        } catch (NumberFormatException ex) {
                            // Non-numeric key, skip attaching id
                        }
                        itemsArray.add(itemObjEntry);
                    }
                }
            } else {
                throw new RuntimeException("Unexpected JSON format: expected array or object at root");
            }
            for (JsonElement elem : itemsArray) {
                JsonObject itemObj = elem.getAsJsonObject();
                int id = itemObj.get("id").getAsInt();
                String name = itemObj.get("name").getAsString();
                List<Requirement> reqList = new ArrayList<>();
                if (itemObj.has("requirements") && itemObj.get("requirements").isJsonObject()) {
                    JsonObject reqObj = itemObj.getAsJsonObject("requirements");
                    for (Map.Entry<String, JsonElement> entry : reqObj.entrySet()) {
                        String skillKey = entry.getKey().toUpperCase();
                        if (skillKey.equalsIgnoreCase("quests") && entry.getValue().isJsonArray()) {
                            JsonArray questArray = entry.getValue().getAsJsonArray();
                            for (JsonElement questElem : questArray) {
                                if (questElem.isJsonPrimitive()) {
                                    String questName = questElem.getAsString();
                                    try
                                    {
                                        Quest q = Quest.fromName(questName);
                                        reqList.add(new QuestRequirement(q));
                                    }
                                    catch (IllegalArgumentException ex)
                                    {
                                        log.warn("Unknown quest requirement: {}", questName);
                                    }
                                }
                            }
                            continue; // Skip further processing for this key
                        }
                        int level = entry.getValue().getAsInt();
                        try {
                            Skill skill = Skill.valueOf(skillKey);
                            reqList.add(new SkillRequirement(skill, level));
                        } catch (IllegalArgumentException e) {
                            // Unknown skill key, skip this entry
                        }
                    }
                }
                ITEM_REQUIREMENTS.put(name, reqList);
                ITEM_REQUIREMENTS_BY_ID.put(id, reqList);
                log.info("Loaded requirements for item {} (ID {}): {}", name, id, reqList);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load item requirements: " + e.getMessage(), e);
        }
    }
    /**
     * Returns the loaded item requirements mapped by item name.
     */
    public static Map<String, List<Requirement>> getRequirements() {
        return ITEM_REQUIREMENTS;
    }

    /**
     * Returns the loaded item requirements mapped by item ID.
     */
    public static Map<Integer, List<Requirement>> getRequirementsById() {
        return ITEM_REQUIREMENTS_BY_ID;
    }

    /**
     * Reloads the item requirements from the Items-Information.json file.
     */
    public static void reloadRequirements()
    {
        loadFromJson();
    }

}