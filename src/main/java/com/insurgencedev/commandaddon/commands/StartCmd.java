package com.insurgencedev.commandaddon.commands;

import org.bukkit.entity.Player;
import org.insurgencedev.insurgenceboosters.api.IBoosterAPI;
import org.insurgencedev.insurgenceboosters.api.contracts.IPlayer;
import org.insurgencedev.insurgenceboosters.data.BoosterData;
import org.insurgencedev.insurgenceboosters.data.BoosterFindResult;
import org.insurgencedev.insurgenceboosters.events.IBoosterStartEvent;
import org.insurgencedev.insurgenceboosters.libs.fo.Common;
import org.insurgencedev.insurgenceboosters.libs.fo.TimeUtil;
import org.insurgencedev.insurgenceboosters.libs.fo.command.SimpleSubCommand;
import org.insurgencedev.insurgenceboosters.libs.fo.model.Replacer;
import org.insurgencedev.insurgenceboosters.libs.fo.model.SimpleTime;
import org.insurgencedev.insurgenceboosters.libs.fo.remain.Remain;
import org.insurgencedev.insurgenceboosters.libs.fo.settings.Lang;
import org.insurgencedev.insurgenceboosters.models.booster.Booster;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StartCmd extends SimpleSubCommand {

    public StartCmd() {
        super(IBoosterAPI.INSTANCE.getMainCommandGroupInstance(), "startpersonal");
        setUsage("<type> <namespace> <multiplier> <time> <target>");
        setDescription("Start a personal booster for a player");
        setMinArguments(5);
    }

    @Override
    protected void onCommand() {
        Player target = findPlayer(args[4]);
        IPlayer cache = IBoosterAPI.INSTANCE.getCache(target);
        String type = args[0];
        String namespace = args[1];

        if (!IBoosterAPI.INSTANCE.getBoosterManager().isBoosterValid(type, namespace)) {
            returnTell(Replacer.replaceArray(Lang.of(
                            "Commands.Invalid_Booster"),
                    "{type}", type, "{namespace}", namespace
            ));
        }

        double multi = findNumber(Double.class, 2, "Please enter a valid number");
        Pattern pattern = Pattern.compile("(\\d+)([a-z]+)\\|?");
        String output = pattern.matcher(args[3]).replaceAll(matchResult -> matchResult.group(1) + " " + matchResult.group(2) + " ");
        long seconds = SimpleTime.from(output.trim()).getTimeSeconds();

        BoosterFindResult result = cache.getBoosterDataManager().findActiveBooster(type, namespace);
        if (result instanceof BoosterFindResult.NotFound) {
            BoosterData data = new BoosterData(type, namespace, "personal", true, seconds, seconds, multi);
            cache.getBoosterDataManager().addBooster(data);
            Common.callEvent(new IBoosterStartEvent(target, data));
            return;
        }

        if (result instanceof BoosterFindResult.Success) {
            cache.getBoosterDataManager().addBooster(new BoosterData(type, namespace, "personal",
                    false, seconds, seconds, multi));

            Common.tell(target, Replacer.replaceArray(Lang.of(
                            "Commands.Booster_Give.Target"),
                    "{type}", type, "{namespace}", namespace,
                    "{multi}", multi, "{time}", TimeUtil.formatTimeDays(seconds),
                    "{executor}", sender instanceof Player ? sender.getName() : "Server"
            ));
        }
    }

    @Override
    protected List<String> tabComplete() {
        return switch (args.length) {
            case 1 ->
                    IBoosterAPI.INSTANCE.getBoosterManager().getBoosters().stream().map(Booster::getTYPE).collect(Collectors.toList());
            case 2 ->
                    IBoosterAPI.INSTANCE.getBoosterManager().getBoosters().stream().map(Booster::getNAMESPACE).collect(Collectors.toList());
            case 3 -> Arrays.asList("1", "2.5", "3", "4.75", "5");
            case 4 -> Arrays.asList("1years", "1months", "1weeks", "1days", "1hours", "1minutes", "1seconds");
            case 5 -> Remain.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            default -> NO_COMPLETE;
        };
    }
}