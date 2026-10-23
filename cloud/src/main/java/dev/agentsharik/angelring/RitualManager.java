package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Ритуал пробуждения. Игрок берёт «Пробуждённое кольцо полёта» в руку, имеет минимум {@value #LEVEL_COST} уровней опыта
 * и нажимает Shift + ПКМ по Чародейскому столу.
 *
 * Уровни списываются и кольцо забирается из руки мгновенно в момент старта (защита от прерывания и дюпов).
 * Сценарий (≈9,8 секунды):
 *   0 – 1 с     кольцо вылетает из рук и летит к столу;
 *   1 – 7,5 с   кольцо медленно поднимается над столом; от игрока в него течёт поток багровых сфер и искр
 *               (поглощение силы), всё гуще бьют лучи света, расходятся энергетические волны и лучи во все стороны;
 *   7,5 с       вспышка с клубами багрового дыма: кольцо превращается в «Кольцо ангела»;
 *   7,5 – 9,8 с новое кольцо плавно опускается к игроку, затем попадает в его инвентарь, выдаётся испытание.
 *
 * Защита от потери: если игрок вышел или сервер останавливается — ритуал завершается мгновенно и готовое кольцо
 * выдаётся (или лежит у стола неуязвимым и доступным только владельцу).
 */
@EventBusSubscriber(modid = AngelRingMod.MOD_ID)
public final class RitualManager {
    private RitualManager() {}

    public static final int LEVEL_COST = 50;

    private static final int FLY_TICKS = 20;
    private static final int FLASH_TICK = 150;
    private static final int END_TICK = 196;

    private static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "divine_wings");

    private static final class Ritual {
        final ResourceKey<Level> dimension;
        final BlockPos table;
        final UUID owner;
        final Vec3 start;
        final double riseHeight;
        int age = 0;
        ItemEntity ring;
        ItemStack finalStack = ItemStack.EMPTY;

        Ritual(ResourceKey<Level> dimension, BlockPos table, UUID owner, Vec3 start, double riseHeight) {
            this.dimension = dimension;
            this.table = table;
            this.owner = owner;
            this.start = start;
            this.riseHeight = riseHeight;
        }

        Vec3 top() {
            return new Vec3(table.getX() + 0.5, table.getY() + 1.15, table.getZ() + 0.5);
        }
    }

    private static final List<Ritual> RITUALS = new ArrayList<>();

    // ------------------------------------------------------------------ запуск

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return; // решает только сервер
        }
        ItemStack held = event.getItemStack();
        if (!held.is(ModItems.AWAKENED_RING.get()) || !player.isShiftKeyDown()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel world)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        if (!state.is(Blocks.ENCHANTING_TABLE)) {
            return;
        }

        // с этого момента клик принадлежит ритуалу: стол не открывается
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        for (Ritual r : RITUALS) {
            if (r.dimension.equals(world.dimension()) && r.table.equals(pos)) {
                player.displayClientMessage(Component.translatable("message.angelring.ritual_busy"), true);
                return;
            }
        }
        boolean free = player.isCreative();
        if (!free && player.experienceLevel < LEVEL_COST) {
            player.displayClientMessage(
                    Component.translatable("message.angelring.ritual_levels", LEVEL_COST, player.experienceLevel), true);
            return;
        }

        // списание: уровни и кольцо — мгновенно, в момент старта
        if (!free) {
            player.giveExperienceLevels(-LEVEL_COST);
        }
        ItemStack ringStack = held.copyWithCount(1);
        held.shrink(1);

        Vec3 start = player.getEyePosition().add(player.getLookAngle().scale(0.5)).subtract(0, 0.25, 0);
        Ritual ritual = new Ritual(world.dimension(), pos, player.getUUID(), start, freeHeight(world, pos));
        ritual.ring = spawnRing(world, start, ringStack);
        RITUALS.add(ritual);

        sound(world, ritual.top(), SoundEvents.ENCHANTMENT_TABLE_USE, 2.0f, 0.6f);
        sound(world, ritual.top(), SoundEvents.BEACON_ACTIVATE, 2.0f, 0.5f);
        player.displayClientMessage(Component.translatable("message.angelring.ritual_start"), true);
    }

    /** Сколько можно подняться над столом, не упираясь в потолок (не больше 3,6 блока). */
    private static double freeHeight(ServerLevel world, BlockPos table) {
        int free = 0;
        for (int dy = 1; dy <= 5; dy++) {
            BlockPos p = table.above(dy);
            if (!world.getBlockState(p).getCollisionShape(world, p).isEmpty()) {
                break;
            }
            free++;
        }
        return Mth.clamp(free - 1.2, 0.8, 3.6);
    }

    private static ItemEntity spawnRing(ServerLevel world, Vec3 pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, stack);
        entity.setNoGravity(true);
        entity.setNeverPickUp();
        entity.setUnlimitedLifetime();
        entity.setInvulnerable(true);
        entity.setDeltaMovement(Vec3.ZERO);
        world.addFreshEntity(entity);
        return entity;
    }

    // ------------------------------------------------------------------ ход ритуала

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (RITUALS.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        Iterator<Ritual> it = RITUALS.iterator();
        while (it.hasNext()) {
            Ritual r = it.next();
            ServerLevel world = server.getLevel(r.dimension);
            ServerPlayer owner = server.getPlayerList().getPlayer(r.owner);
            if (world == null) {
                if (owner != null) {
                    owner.getInventory().placeItemBackInInventory(makeFinalStack(owner.serverLevel(), r));
                }
                it.remove();
                continue;
            }
            tick(server, world, owner, r);
            if (r.age >= END_TICK) {
                finish(server, world, owner, r);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        Iterator<Ritual> it = RITUALS.iterator();
        while (it.hasNext()) {
            Ritual r = it.next();
            if (r.owner.equals(player.getUUID())) {
                ServerLevel world = server.getLevel(r.dimension);
                if (world != null) {
                    finish(server, world, null, r); // игрока уже нет: кольцо ляжет у стола под защитой
                }
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        for (Ritual r : RITUALS) {
            ServerLevel world = server.getLevel(r.dimension);
            if (world != null) {
                finish(server, world, server.getPlayerList().getPlayer(r.owner), r);
            }
        }
        RITUALS.clear();
    }

    private static void tick(MinecraftServer server, ServerLevel w, ServerPlayer owner, Ritual r) {
        int age = r.age;
        Vec3 pos = ringPos(r, owner, age);

        // кольцо: двигаем каждый тик, чтобы движение было плавным (и принудительно рассылаем позицию клиентам)
        if (r.ring == null || r.ring.isRemoved()) {
            ItemStack stack = age >= FLASH_TICK ? r.finalStack : new ItemStack(ModItems.AWAKENED_RING.get());
            if (!stack.isEmpty()) {
                r.ring = spawnRing(w, pos, stack);
            }
        }
        if (r.ring != null && !r.ring.isRemoved()) {
            r.ring.setPos(pos.x, pos.y, pos.z);
            r.ring.setDeltaMovement(Vec3.ZERO);
            r.ring.hasImpulse = true;
        }

        effects(w, owner, r, pos, age);
        if (age == FLASH_TICK) {
            transform(w, r, pos);
        }
        r.age++;
    }

    private static double smooth(double t) {
        double c = Mth.clamp(t, 0.0, 1.0);
        return c * c * (3 - 2 * c);
    }

    /** Положение кольца: вылет из рук → медленный подъём → снижение к игроку. */
    private static Vec3 ringPos(Ritual r, ServerPlayer owner, int age) {
        Vec3 top = r.top();
        if (age < FLY_TICKS) {
            double p = smooth(age / (double) FLY_TICKS);
            Vec3 pos = r.start.lerp(top, p);
            return pos.add(0, Math.sin(p * Math.PI) * 0.7, 0);
        }
        if (age < FLASH_TICK) {
            double p = smooth((age - FLY_TICKS) / (double) (FLASH_TICK - FLY_TICKS));
            double wobble = 0.05 * p;
            return top.add(Math.sin(age * 0.35) * wobble, r.riseHeight * p, Math.cos(age * 0.35) * wobble);
        }
        Vec3 high = top.add(0, r.riseHeight, 0);
        Vec3 goal = owner != null && owner.isAlive() ? owner.position().add(0, 1.0, 0) : top.add(0, 0.5, 0);
        return high.lerp(goal, smooth((age - FLASH_TICK) / (double) (END_TICK - FLASH_TICK)));
    }

    // ------------------------------------------------------------------ визуальные эффекты

    private static void spark(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        RelicFx.spark(w, x, y, z, vx, vy, vz);
    }

    private static void smoke(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        RelicFx.smoke(w, x, y, z, vx, vy, vz);
    }

    private static void sound(ServerLevel w, Vec3 p, SoundEvent sound, float volume, float pitch) {
        w.playSound(null, p.x, p.y, p.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void effects(ServerLevel w, ServerPlayer owner, Ritual r, Vec3 ring, int age) {
        Vec3 top = r.top();

        // след от кольца при вылете и снижении: редкие искры и лёгкий дымок
        if (age < FLY_TICKS || age > FLASH_TICK) {
            if (age % 2 == 0) {
                spark(w, ring.x, ring.y, ring.z, 0, 0.02, 0);
            }
            if (age % 5 == 0) {
                smoke(w, ring.x, ring.y, ring.z, 0, 0.02, 0);
            }
        }

        // звук: нарастающий гул, глухие удары
        if (age >= 10 && age < FLASH_TICK && age % 20 == 0) {
            float k = age / (float) FLASH_TICK;
            sound(w, ring, SoundEvents.BEACON_AMBIENT, 1.5f, 0.5f + k);
        }
        if (age == 30) {
            sound(w, ring, SoundEvents.ENDER_DRAGON_GROWL, 1.2f, 0.5f);
        }
        if (age == 120) {
            sound(w, ring, SoundEvents.WITHER_SPAWN, 0.8f, 1.6f);
        }

        // поток опыта: зелёно-жёлтые сферы вылетают из игрока и летят к кольцу; у кольца они вспыхивают багровым
        if (age >= 8 && age < FLASH_TICK) {
            if (owner != null && age % 2 == 0) {
                Vec3 from = owner.position().add(0, owner.getBbHeight() * 0.65, 0);
                // целимся туда, где кольцо будет к моменту прилёта сферы
                Vec3 target = ringPos(r, owner, Math.min(age + RelicFx.ORB_FLIGHT_TICKS, FLASH_TICK - 1));
                int count = 1 + age / 45;
                for (int i = 0; i < count; i++) {
                    Vec3 start = from.add((w.random.nextDouble() - 0.5) * 0.5,
                            (w.random.nextDouble() - 0.5) * 0.4, (w.random.nextDouble() - 0.5) * 0.5);
                    Vec3 end = target.add((w.random.nextDouble() - 0.5) * 0.2,
                            (w.random.nextDouble() - 0.5) * 0.2, (w.random.nextDouble() - 0.5) * 0.2);
                    RelicFx.orb(w, start, end);
                }
            }
            // поглощение кольцом: мелкие багровые искры вокруг него и редкий мягкий отблеск
            if (age >= 8 + RelicFx.ORB_FLIGHT_TICKS) {
                if (age % 3 == 0) {
                    double a = w.random.nextDouble() * Math.PI * 2;
                    spark(w, ring.x + Math.cos(a) * 0.15, ring.y + (w.random.nextDouble() - 0.5) * 0.2,
                            ring.z + Math.sin(a) * 0.15, Math.cos(a) * 0.05, 0.02, Math.sin(a) * 0.05);
                }
                if (age % 12 == 0) {
                    RelicFx.glow(w, ring.x, ring.y, ring.z);
                }
            }
        }

        // тонкие лучи света: столбы вокруг кольца и центральный луч над ним
        if (age >= 50 && age < FLASH_TICK && age % 4 == 0) {
            int beams = age < 110 ? 6 : 8;
            double height = r.riseHeight + 6.0;
            double len = 2.0 + (height - 2.0) * smooth((age - 50) / 40.0);
            for (int k = 0; k < beams; k++) {
                double a = Math.PI / 6 + k * (Math.PI * 2 / beams);
                RelicFx.beam(w, top.x + Math.cos(a) * 1.6, top.y - 0.3 + len / 2, top.z + Math.sin(a) * 1.6,
                        0, 1, 0, len);
            }
            RelicFx.beam(w, ring.x, ring.y + 3.3, ring.z, 0, 1, 0, 7.0);
        }

        // энергетические волны: тонкие расходящиеся кольца на высоте кольца
        if (age >= 100 && age < FLASH_TICK && age % 10 == 0) {
            RelicFx.sparkRing(w, ring, 0.5, 20, 0.2);
            RelicFx.sparkRing(w, ring, 0.5, 14, 0.12);
        }

        // лучи в стороны, как при гибели Дракона Края: несколько тонких лучей за раз
        if (age >= 118 && age < FLASH_TICK) {
            if (age % 3 == 0) {
                for (int ray = 0; ray < 3; ray++) {
                    double a = w.random.nextDouble() * Math.PI * 2;
                    double e = (w.random.nextDouble() - 0.35) * 1.2;
                    Vec3 dir = new Vec3(Math.cos(a) * Math.cos(e), Math.sin(e), Math.sin(a) * Math.cos(e));
                    double len = 4.0 + w.random.nextDouble() * 3.0;
                    double mid = 0.5 + len / 2;
                    RelicFx.beam(w, ring.x + dir.x * mid, ring.y + dir.y * mid, ring.z + dir.z * mid,
                            dir.x, dir.y, dir.z, len);
                }
            }
            if (age % 4 == 0) {
                smoke(w, ring.x, ring.y, ring.z, (w.random.nextDouble() - 0.5) * 0.1, 0.04, (w.random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    // ------------------------------------------------------------------ превращение и финал

    private static ItemStack makeFinalStack(ServerLevel w, Ritual r) {
        if (r.finalStack.isEmpty()) {
            ItemStack stack = new ItemStack(ModItems.ANGEL_RING.get());
            stack.set(ModComponents.RELIC_DATA.get(), RelicData.EMPTY.ensureRolled(w.getRandom()));
            r.finalStack = stack;
        }
        return r.finalStack.copy();
    }

    private static void transform(ServerLevel w, Ritual r, Vec3 pos) {
        if (r.ring != null) {
            r.ring.discard();
        }
        ItemStack stack = makeFinalStack(w, r);
        r.ring = spawnRing(w, pos, stack);

        // вспышка: мягкое свечение, умеренные клубы дыма, искры, ударные кольца и тонкие лучи
        RelicFx.glow(w, pos.x, pos.y, pos.z);
        RelicFx.glow(w, pos.x, pos.y + 0.2, pos.z);
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), pos.x, pos.y, pos.z, 26, 0.3, 0.3, 0.3, 0.1);
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), pos.x, pos.y, pos.z, 60, 0.2, 0.2, 0.2, 0.35);
        RelicFx.sparkRing(w, pos, 0.4, 28, 0.3);
        RelicFx.sparkRing(w, pos, 0.4, 20, 0.16);
        for (int k = 0; k < 10; k++) {
            double a = w.random.nextDouble() * Math.PI * 2;
            double e = (w.random.nextDouble() - 0.35) * 1.2;
            Vec3 dir = new Vec3(Math.cos(a) * Math.cos(e), Math.sin(e), Math.sin(a) * Math.cos(e));
            double len = 3.0 + w.random.nextDouble() * 3.0;
            double mid = 0.4 + len / 2;
            RelicFx.beam(w, pos.x + dir.x * mid, pos.y + dir.y * mid, pos.z + dir.z * mid, dir.x, dir.y, dir.z, len);
        }
        RelicFx.beam(w, pos.x, pos.y + 5.0, pos.z, 0, 1, 0, 10.0);
        RelicFx.beam(w, pos.x, pos.y - 1.5, pos.z, 0, 1, 0, 3.0);
        sound(w, pos, SoundEvents.ENDER_DRAGON_DEATH, 1.2f, 0.9f);
        sound(w, pos, SoundEvents.WARDEN_SONIC_BOOM, 2.5f, 0.7f);
        sound(w, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, 3.0f, 0.6f);
    }

    /** Завершение: новое кольцо попадает в инвентарь игрока (или лежит у стола под защитой) и выдаётся испытание. */
    private static void finish(MinecraftServer server, ServerLevel w, ServerPlayer owner, Ritual r) {
        Vec3 pos = r.ring != null && !r.ring.isRemoved() ? r.ring.position() : r.top();
        if (r.ring != null) {
            r.ring.discard();
            r.ring = null;
        }
        ItemStack stack = makeFinalStack(w, r);

        boolean delivered = owner != null && owner.isAlive() && owner.level() == w
                && owner.position().distanceToSqr(r.top()) < 24.0 * 24.0;
        if (delivered) {
            owner.getInventory().placeItemBackInInventory(stack);
            sound(w, owner.position(), SoundEvents.PLAYER_LEVELUP, 1.0f, 0.7f);
            sound(w, owner.position(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f, 0.8f);
        } else {
            // защита от пропадания: неуязвимый предмет, который может подобрать только владелец
            ItemEntity drop = new ItemEntity(w, r.top().x, r.top().y + 0.3, r.top().z, stack);
            drop.setUnlimitedLifetime();
            drop.setInvulnerable(true);
            drop.setTarget(r.owner);
            w.addFreshEntity(drop);
        }
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), pos.x, pos.y, pos.z, 16, 0.3, 0.3, 0.3, 0.3);

        if (owner != null) {
            AdvancementHolder advancement = server.getAdvancements().get(ADVANCEMENT_ID);
            if (advancement != null) {
                owner.getAdvancements().award(advancement, "ritual_complete");
            }
        }
    }
}
