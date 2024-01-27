package com.insurgencedev.commandaddon;

import com.insurgencedev.commandaddon.commands.StartCmd;
import com.insurgencedev.commandaddon.commands.StopCmd;
import org.insurgencedev.insurgenceboosters.api.addon.IBoostersAddon;
import org.insurgencedev.insurgenceboosters.api.addon.InsurgenceBoostersAddon;

@IBoostersAddon(name = "CommandAddon", version = "1.0.0", author = "InsurgenceDev", description = "Some extra comamnds!")
public class CommandAddon extends InsurgenceBoostersAddon {

    @Override
    public void onAddonStart() {
        registerSubCommand(new StartCmd());
        registerSubCommand(new StopCmd());
    }
}
