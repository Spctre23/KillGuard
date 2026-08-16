package killguard;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import killguard.config.Config;
import killguard.config.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandManager
{
    public void registerCommands()
    {
        registerKillguardCommand();
        registerForceKillCommand();
    }

    private void registerKillguardCommand()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, _) ->
                dispatcher.register(net.minecraft.commands.Commands.literal("killguard")
                        .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(net.minecraft.commands.Commands.literal("reload")
                                .executes(ctx ->
                                {
                                    ConfigManager.load();
                                    ctx.getSource().sendSuccess(() -> Component.literal("KillGuard config reloaded."), true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(net.minecraft.commands.Commands.literal("addtag")
                                .then(net.minecraft.commands.Commands.argument("tag", StringArgumentType.string())
                                        .executes(ctx ->
                                        {
                                            String entry = StringArgumentType.getString(ctx, "tag");
                                            addEntry(ctx, entry, ConfigManager.get().protectedEntityTags);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(net.minecraft.commands.Commands.literal("addtype")
                                .then(net.minecraft.commands.Commands.argument("type", ResourceArgument.resource(registry, Registries.ENTITY_TYPE))
                                        .executes(ctx ->
                                        {
                                            String entry = ResourceArgument.getResource(ctx, "type", Registries.ENTITY_TYPE).key().identifier().toString();
                                            addEntry(ctx, entry, ConfigManager.get().protectedEntityTypes);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(net.minecraft.commands.Commands.literal("removetag")
                                .then(net.minecraft.commands.Commands.argument("tag", StringArgumentType.string())
                                        .suggests((context, builder) ->
                                                suggestCommands(builder, ConfigManager.get().protectedEntityTags))
                                        .executes(ctx ->
                                        {
                                            String entry = StringArgumentType.getString(ctx, "tag");
                                            removeEntry(ctx, entry, ConfigManager.get().protectedEntityTags);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(net.minecraft.commands.Commands.literal("removetype")
                                .then(net.minecraft.commands.Commands.argument("type", StringArgumentType.greedyString())
                                        .suggests((ctx, builder) ->
                                                suggestCommands(builder, ConfigManager.get().protectedEntityTypes))
                                        .executes(ctx ->
                                        {
                                            String entry = StringArgumentType.getString(ctx, "type");
                                            removeEntry(ctx, entry, ConfigManager.get().protectedEntityTypes);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(net.minecraft.commands.Commands.literal("clear")
                                .executes(ctx ->
                                {
                                    Config config = ConfigManager.get();
                                    config.protectedEntityTypes.clear();
                                    config.protectedEntityTags.clear();
                                    ConfigManager.save();

                                    ctx.getSource().sendSuccess(() -> Component.literal("KillGuard config saved."), true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(net.minecraft.commands.Commands.literal("list")
                                .executes(ctx ->
                                {
                                    Config config = ConfigManager.get();
                                    ctx.getSource().sendSuccess(() -> Component.literal("Protected Entity Tags: " + config.protectedEntityTags).withColor(0xFFFF00), false);
                                    ctx.getSource().sendSuccess(() -> Component.literal("Protected Entity Types: " + config.protectedEntityTypes).withColor(0xFFFF00), false);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private void registerForceKillCommand()
    {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) ->
                dispatcher.register(net.minecraft.commands.Commands.literal("forcekill")
                        .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(net.minecraft.commands.Commands.argument("targets", EntityArgument.entities())
                                .executes(ctx ->
                                {
                                    Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
                                    ServerLevel level = ctx.getSource().getLevel();
                                    targets.forEach(e -> e.kill(level));
                                    ctx.getSource().sendSuccess(() -> Component.literal("Force-killed " + targets.size() + " entities"), true);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private void addEntry(CommandContext<CommandSourceStack> ctx, String entry, List<String> list)
    {
        if (!list.contains(entry))
        {
            list.add(entry);
            ConfigManager.save();
            ctx.getSource().sendSuccess(() -> Component.literal("KillGuard config saved"), true);
        }
        else
            ctx.getSource().sendSuccess(() -> Component.literal("Config already contains specified entry"), true);
    }

    private void removeEntry(CommandContext<CommandSourceStack> ctx, String entry, List<String> list)
    {
        if (list.contains(entry))
        {
            list.remove(entry);
            ConfigManager.save();
            ctx.getSource().sendSuccess(() -> Component.literal("KillGuard config saved"), true);
        }
        else
            ctx.getSource().sendSuccess(() -> Component.literal("Config does not contain specified entry"), true);
    }

    private CompletableFuture<Suggestions> suggestCommands(SuggestionsBuilder builder, List<String> list)
    {
        return SharedSuggestionProvider.suggest(list, builder);
    }
}
