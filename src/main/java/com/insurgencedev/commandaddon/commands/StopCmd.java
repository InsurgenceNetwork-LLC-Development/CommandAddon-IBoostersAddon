package com.insurgencedev.commandaddon.commands;

import org.bukkit.entity.Player;
import org.insurgencedev.insurgenceboosters.api.IBoosterAPI;
import org.insurgencedev.insurgenceboosters.api.contracts.IPlayer;
import org.insurgencedev.insurgenceboosters.data.BoosterData;
import org.insurgencedev.insurgenceboosters.data.BoosterFindResult;
import org.insurgencedev.insurgenceboosters.events.IBoosterEndEvent;
import org.insurgencedev.insurgenceboosters.libs.fo.Common;
import org.insurgencedev.insurgenceboosters.libs.fo.TimeUtil;
import org.insurgencedev.insurgenceboosters.libs.fo.command.SimpleSubCommand;
import org.insurgencedev.insurgenceboosters.libs.fo.model.Replacer;
import org.insurgencedev.insurgenceboosters.libs.fo.remain.Remain;
import org.insurgencedev.insurgenceboosters.libs.fo.settings.Lang;
import org.insurgencedev.insurgenceboosters.models.booster.Booster;

import java.util.List;
import java.util.stream.Collectors;

public class StopCmd extends SimpleSubCommand {

    public StopCmd() {
        super("stoppersonal");
        setUsage("<type> <namespace> <target>");
        setDescription("Stop a player's personal booster");
        setMinArguments(3);
    }

    @Override
    protected void onCommand() {
        Player target = findPlayer(args[2]);
        IPlayer cache = IBoosterAPI.INSTANCE.getCache(target);
        String type = args[0];
        String namespace = args[1];

        if (!IBoosterAPI.INSTANCE.getBoosterManager().isBoosterValid(type, namespace)) {
            returnTell(Replacer.replaceArray(Lang.of(
                            "Commands.Invalid_Booster"),
                    "{type}", type, "{namespace}", namespace
            ));
        }

        BoosterFindResult result = cache.getBoosterDataManager().findActiveBooster(type, namespace);
        if (result instanceof BoosterFindResult.Success r) {
            BoosterData o = r.getBoosterData();
            BoosterData n = new BoosterData(o.getType(), o.getNamespace(),
                    "personal", true, o.getTime(), o.getTimeLeft(), o.getMultiplier());

            cache.getBoosterDataManager().removeBooster(o);
            Common.callEvent(new IBoosterEndEvent(target, n));
            Common.tell(target, Replacer.replaceArray(Lang.of(
                            "Commands.Booster_Stop.Success"),
                    "{type}", type, "{namespace}", namespace,
                    "{multi}", o.getMultiplier(), "{time}", TimeUtil.formatTimeDays(o.getTime())
            ));

            return;
        }

        if (result instanceof BoosterFindResult.NotFound) {
            Common.tell(target, Replacer.replaceArray(Lang.of(
                            "Commands.Booster_Stop.Not_Active"),
                    "{type}", type, "{namespace}", namespace
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
            case 3 -> Remain.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            default -> NO_COMPLETE;
        };
    }
}
