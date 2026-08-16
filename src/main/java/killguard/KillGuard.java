package killguard;

import net.fabricmc.api.ModInitializer;
import killguard.config.ConfigManager;

public class KillGuard implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ConfigManager.load();
        CommandManager cmd = new CommandManager();
        cmd.registerCommands();
    }
}
