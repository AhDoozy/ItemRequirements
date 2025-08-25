package com.itemrequirements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRequirementsData
{
    private static final Logger log = LoggerFactory.getLogger(ItemRequirementsData.class);
    public static final Map<String, List<Requirement>> ITEM_REQUIREMENTS = new HashMap<>();
    public static final Map<Integer, List<Requirement>> ITEM_REQUIREMENTS_BY_ID = new HashMap<>();



    public static void loadFromJson(Gson gson) {
        log.debug("Loading item requirements from Items-Information.json");
        ITEM_REQUIREMENTS.clear();
        ITEM_REQUIREMENTS_BY_ID.clear();

        InputStream infoStream = ItemRequirementsData.class.getResourceAsStream("/Items-Information.json");
        if (infoStream == null) {
            throw new RuntimeException("Resource Items-Information.json not found in classpath");
        }

        try (InputStreamReader reader = new InputStreamReader(infoStream, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<HashMap<Integer, ItemRequirementEntry>>() {}.getType();
            Map<Integer, ItemRequirementEntry> loaded = gson.fromJson(reader, type);
            for (Map.Entry<Integer, ItemRequirementEntry> e : loaded.entrySet()) {
                int id = e.getKey();
                ItemRequirementEntry entry = e.getValue();
                List<Requirement> reqList = new ArrayList<>();
                if (entry.getRequirements() != null) {
                    for (Map.Entry<String, JsonElement> requirement : entry.getRequirements().entrySet()) {
                        String skillKey = requirement.getKey().toUpperCase();
                        if (skillKey.equalsIgnoreCase("quests") && requirement.getValue().isJsonArray()) {
                            JsonArray questArray = requirement.getValue().getAsJsonArray();
                            for (JsonElement questElem : questArray)
                            {
                                if (!questElem.isJsonPrimitive())
                                {
                                    continue;
                                }
                                String text = questElem.getAsString();

                                // Known quest?
                                try
                                {
                                    Quest q = Quest.fromName(text);
                                    reqList.add(new QuestRequirement(q));
                                    continue;
                                }
                                catch (IllegalArgumentException ignored)
                                {
                                    // Unknown quest string; warn during development so it can be fixed
                                    log.warn("Ignoring unknown quest requirement string: {}", text);
                                }
                            }
                            continue;
                        }
                        int level = requirement.getValue().getAsInt();
                        try {
                            Skill skill = Skill.valueOf(skillKey);
                            reqList.add(new SkillRequirement(skill, level));
                        } catch (IllegalArgumentException ex) {
                            // skip
                        }
                    }
                }
                ITEM_REQUIREMENTS.put(entry.getName(), reqList);
                ITEM_REQUIREMENTS_BY_ID.put(id, reqList);
                log.debug("Loaded requirements for item {} (ID {}): {}", entry.getName(), id, reqList);
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
    public static void reloadRequirements(Gson gson)
    {
        loadFromJson(gson);
    }

    /**
     * Clears all loaded item requirements.
     */
    public static void clear()
    {
        ITEM_REQUIREMENTS.clear();
        ITEM_REQUIREMENTS_BY_ID.clear();
    }

}