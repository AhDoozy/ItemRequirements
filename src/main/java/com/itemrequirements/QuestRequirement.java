package com.itemrequirements;

import net.runelite.api.Client;
import net.runelite.api.QuestState;

public class QuestRequirement implements Requirement
{
    private final Quest quest;

    public QuestRequirement(Quest quest)
    {
        this.quest = quest;
    }

    @Override
    public boolean isMet(Client client)
    {
        return quest.getState(client) == QuestState.FINISHED;
    }

    @Override
    public String getMessage()
    {
        return "Requires " + quest.getName();
    }

    public Quest getQuest()
    {
        return quest;
    }
}
