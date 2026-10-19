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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
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
    public static final String MARKER_PREFIX = "<link:";
    public static final String MARKER_SUFFIX = "</link>";

    static final Pattern LINK = Pattern.compile(
            "<link:(recipe|item):([^>]+)>\\[([^\\]]*)\\]</link>");

    /** Exact stacks registered behind item-link tokens, with an expiry stamp. */
    private static final Map<String, TokenEntry> TOKEN_STACKS = new java.util.concurrent.ConcurrentHashMap<>();

    private record TokenEntry(ItemStack stack, long expiresAt) {}

    static void registerToken(String token, ItemStack stack) {
        if (TOKEN_STACKS.size() > 512) {
            long now = System.currentTimeMillis();
            TOKEN_STACKS.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);
        }
        TOKEN_STACKS.put(token, new TokenEntry(stack.copy(), System.currentTimeMillis() + 600_000L));
    }

    @org.jetbrains.annotations.Nullable
    private static ItemStack tokenStack(String token) {
        TokenEntry entry = TOKEN_STACKS.get(token);
        if (entry == null || entry.expiresAt() < System.currentTimeMillis()) {
            return null;
        }
        return entry.stack();
    }

    private JeiLinkerServer() {}

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        String raw = event.getMessage().getString();
        if (raw == null || !raw.contains(MARKER_PREFIX)) {
            return;
        }
        event.setMessage(decorate(raw, JeiLinkerServer::tokenStack));
    }

    /**
     * Turns raw markers into coloured, clickable [Name] components. Shared with the
     * client-side fallback so links look the same even on servers without the mod.
     */
    static Component decorate(String raw, java.util.function.Function<String, ItemStack> tokenLookup) {
        Matcher matcher = LINK.matcher(raw);
        MutableComponent result = Component.empty();
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                result.append(Component.literal(raw.substring(last, matcher.start())));
            }
            last = matcher.end();
            String kind = matcher.group(1);
            String target = matcher.group(2);
            String label = matcher.group(3);
            ItemStack stack;
            ResourceLocation id;
            if (kind.equals("item")) {
                stack = tokenLookup.apply(target);
                id = stack == null ? null : BuiltInRegistries.ITEM.getKey(stack.getItem());
            } else {
                id = ResourceLocation.tryParse(target);
                stack = id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
            }
            if (stack == null || stack.isEmpty()) {
                result.append(Component.literal("[" + label + "]"));
                continue;
            }
            // COMMON would render white = "no colour at all", so links of common
            // items borrow the yellow link colour; other rarities keep their own.
            net.minecraft.network.chat.TextColor nameColor = stack.getHoverName().getStyle().getColor();
            net.minecraft.network.chat.TextColor linkColor = net.minecraft.network.chat.TextColor.fromLegacyFormat(
                    stack.getRarity() == net.minecraft.world.item.Rarity.COMMON
                            ? net.minecraft.ChatFormatting.YELLOW
                            : stack.getRarity().color());
            result.append(Component.literal("[" + label + "]").withStyle(style -> style
                    .withColor(nameColor != null ? nameColor : linkColor)
                    .withClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/fallenrelics jei_recipe " + (id == null ? "minecraft:air" : id)))
                    .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_ITEM,
                            new HoverEvent.ItemStackInfo(stack)))));
        }
        if (last < raw.length()) {
            result.append(Component.literal(raw.substring(last)));
        }
        return result;
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
        PacketDistributor.sendToPlayer(player, new JeiRecipePayload(item));
        return 1;
    }
}
