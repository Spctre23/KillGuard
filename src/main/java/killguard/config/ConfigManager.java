package killguard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.minecraft.core.RegistryAccess.LOGGER;

public class ConfigManager
{
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("killguard.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config config = new Config();

    public static Config get()
    {
        return config;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH))
        {
            config = new Config();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH))
        {
            Config parsed = GSON.fromJson(reader, Config.class);
            config = (parsed != null) ? parsed : new Config();
        }
        catch (IOException | JsonSyntaxException e)
        {
            LOGGER.error("Failed to read killguard.json", e);
            config = new Config();
        }
    }

    public static void save()
    {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH))
        {
            GSON.toJson(config, writer);
        }
        catch (IOException | JsonSyntaxException e)
        {
            LOGGER.error("Failed to save killguard.json", e);
        }
    }
}

