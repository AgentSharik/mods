package dev.agentsharik.fallenrelics;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server half of the Chat & Recipe Linker: turns the raw {@code <link:recipe:id>[Name]}
 * markers typed into chat into coloured, clickable [Name] components whose tooltip
 * shows the real item and whose click asks the clicking client to open JEI recipes.
 */
@EventBusSubscriber(modid = FallenRelicsMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class JeiLinkerServer {
    /** Chat marker shared with the client side; a compile-time constant on both. */
    public static final String MARKER_PREFIX = "<link:recipe:";
    public static final String MARKER_SUFFIX = "</link>";

    private static final Pattern LINK = Pattern.compile(
            "<link:recipe:([a-z0-9_.-]+:[a-z0-9_/.-]+)>\\[([^\\]]*)\\]</link>");

    private JeiLinkerServer() {}

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        String raw = event.getRawMessage();
        if (raw == null || !raw.contains(MARKER_PREFIX)) {
            return;
        }
        Matcher matcher = LINK.matcher(raw);
        Component result = Component.empty();
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                result.append(Component.literal(raw.substring(last, matcher.start())));
            }
            last = matcher.end();
            ResourceLocation id = ResourceLocation.tryParse(matcher.group(1));
            String label = matcher.group(2);
            ItemStack stack = id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
            if (stack.isEmpty()) {
                result.append(Component.literal("[" + label + "]"));
                continue;
            }
            result.append(Component.literal("[" + label + "]").withStyle(style -> style
                    .withColor(stack.getRarity().color)
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/fallenrelics jei_recipe " + id))
                    .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_ITEM,
                            new HoverEvent.ItemStackInfo(stack)))));
        }
        if (last < raw.length()) {
            result.append(Component.literal(raw.substring(last)));
        }
        event.setMessage(result);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("fallenrelics")
                .then(Commands.literal("jei_recipe")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .executes(context -> sendToClicker(
                                        context.getSource(),
                                        ResourceLocationArgument.getId(context, "item"))))));
    }

    private static int sendToClicker(CommandSourceStack source, ResourceLocation item) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PacketDistributor.sendToPlayer(player, JeiRecipePayload.TYPE, new JeiRecipePayload(item));
        return 1;
    }
}
