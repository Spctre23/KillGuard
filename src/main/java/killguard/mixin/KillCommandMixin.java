package killguard.mixin;

import killguard.config.Config;
import killguard.config.ConfigManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;
import java.util.List;

@Mixin(KillCommand.class)
public class KillCommandMixin
{
	@ModifyVariable(at = @At("HEAD"), method = "kill", argsOnly = true)
	private static Collection<? extends Entity> filterEntities(Collection<? extends Entity> all, CommandSourceStack source)
	{
		List<? extends Entity> filtered = all.stream()
				.filter(e -> !hasProtectedTag(e) && !isProtectedEntityType(e))
				.toList();

		int skippedCount = all.size() - filtered.size();
		if (skippedCount > 0)
		{
			source.sendSuccess(() -> Component.literal("Skipped " + skippedCount + " protected entities")
					.withStyle(Style.EMPTY.withColor(0xFFFF00)), false);
		}
		return filtered;
	}

	@Unique
    private static boolean hasProtectedTag(Entity entity)
	{
		Config config = ConfigManager.get();
		boolean hasTag = false;
		for (String tag : entity.entityTags())
		{
			hasTag = config.protectedEntityTags.stream().anyMatch(s -> s.equals(tag));
		}
		return hasTag;
	}

	@Unique
    private static boolean isProtectedEntityType(Entity entity)
	{
		Config config = ConfigManager.get();
		String id = EntityType.getKey(entity.getType()).toString();
        return config.protectedEntityTypes.contains(id);
	}
}


