package com.leonardobishop.quests.quests.tasktypes.types;

import com.leonardobishop.quests.api.QuestsAPI;
import com.leonardobishop.quests.player.QPlayer;
import com.leonardobishop.quests.player.questprogressfile.QuestProgress;
import com.leonardobishop.quests.player.questprogressfile.QuestProgressFile;
import com.leonardobishop.quests.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.quests.tasktypes.ConfigValue;
import com.leonardobishop.quests.quests.tasktypes.TaskType;
import com.leonardobishop.quests.quests.tasktypes.TaskUtils;
import me.elapsed.plugin.harvester.events.HarvesterHoeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HarvesterHoeTaskType extends TaskType {


    private List<ConfigValue> creatorConfigValues;

    public HarvesterHoeTaskType() {
        super("harvesterhoe", "ElapsedDev", "Break a set amount of sugarcane with a harvester hoe.");
        creatorConfigValues = Arrays.asList(
                new ConfigValue("amount", true, "Amount of sugarcane to break."),
                new ConfigValue("worlds", false, "Permitted worlds the player must be in") // Wasn't sure if you wanted this, but other types have it so I suppose it can't hurt
        );
    }

    @Override
    public List<ConfigValue> getCreatorConfigValues() { return creatorConfigValues; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHarvesterHoeBreak(HarvesterHoeEvent event) {

        Player player = event.getPlayer();
        int amount = event.getAmountMined();
        QPlayer questPlayer = getQuestPlayer(player);
        QuestProgressFile progressFile = questPlayer.getQuestProgressFile();

        getRegisteredQuests()
                .stream()
                .filter(progressFile::hasStartedQuest)
                .forEach(quest -> {
                    QuestProgress progress = progressFile.getQuestProgress(quest);
                    quest.getTasksOfType(getType()).stream()
                            .filter(task -> TaskUtils.validateWorld(player, task))
                            .forEach(task -> {
                                TaskProgress taskProgress = progress.getTaskProgress(task.getId());
                                if(!taskProgress.isCompleted()) {
                                    int required = (int) task.getConfigValue("amount");
                                    int current = Optional.ofNullable(taskProgress.getProgress()).map(i -> (int) i).orElse(0);

                                    taskProgress.setProgress(current + event.getAmountMined());

                                    if(((int) taskProgress.getProgress()) >= required) { // TODO: It's probably not necessary to get the progress again, but in case there the state is changed somehow else (boosters? I'm not sure exactly how this plugin works), I'll follow what the other task types do.
                                        taskProgress.setCompleted(true);
                                    }
                                }
                            });

                });
    }

    private QPlayer getQuestPlayer(Player player) {
        return QuestsAPI.getPlayerManager().getPlayer(player.getUniqueId(), true); // TODO: Bad to load if null. File I/O should not be done like this.
    }
}
