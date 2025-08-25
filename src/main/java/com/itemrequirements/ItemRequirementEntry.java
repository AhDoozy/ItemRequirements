package com.itemrequirements;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ItemRequirementEntry
{
    private int id;
    private String name;

    @SerializedName("requirements")
    private JsonObject requirements;

    public int getId() { return id; }
    public String getName() { return name; }
    public JsonObject getRequirements() { return requirements; }

    @Override
    public String toString()
    {
        return "ItemRequirementEntry{id=" + id + ", name='" + name + "'}";
    }
}