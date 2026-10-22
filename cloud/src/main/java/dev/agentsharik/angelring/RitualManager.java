package dev.agentsharik.angelring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.joml.Vector3f;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    private static void smoke(ServerLevel w, double x, double y, double z, double vx, double vy, double vz) {
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), x, y, z, 0, vx, vy, vz, 1.0);
    }

    private static void sound(ServerLevel w, Vec3 p, SoundEvent sound, float volume, float pitch) {
        w.playSound(null, p.x, p.y, p.z, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void effects(ServerLevel w, ServerPlayer owner, Ritual r, Vec3 ring, int age) {
        Vec3 top = r.top();

        // след от кольца при вылете и снижении
        if (age < FLY_TICKS || age > FLASH_TICK) {
            spark(w, ring.x, ring.y, ring.z, 0, 0.02, 0);
            smoke(w, ring.x, ring.y, ring.z, 0, 0.02, 0);
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

        // поток поглощения: сферы и искры от игрока в кольцо (густеет со временем)
        if (age >= 8 && age < FLASH_TICK) {
            Vec3 from = owner != null ? owner.position().add(0, owner.getBbHeight() * 0.65, 0) : top;
            int count = 2 + age / 18;
            for (int i = 0; i < count; i++) {
                double s = (age * 0.09 + i / (double) count) % 1.0;
                double ease = s * s * (3 - 2 * s);
                Vec3 p = from.lerp(ring, ease);
                double sway = Math.sin(s * Math.PI) * 0.6;
                double ang = age * 0.4 + i * 2.1;
                double px = p.x + Math.cos(ang) * sway;
                double py = p.y + Math.sin(ang * 0.8) * sway * 0.5;
                double pz = p.z + Math.sin(ang) * sway;
                w.sendParticles(new DustParticleOptions(new Vector3f(0.78f, 0.08f, 0.12f), 1.7f), px, py, pz, 1, 0, 0, 0, 0);
                if (i % 2 == 0) {
                    spark(w, px, py, pz, (ring.x - px) * 0.05, (ring.y - py) * 0.05, (ring.z - pz) * 0.05);
                }
            }
        }

        // объёмные лучи света вокруг кольца и столб над ним
        if (age >= 50 && age < FLASH_TICK) {
            double spin = age * 0.06;
            int beams = 8;
            int perBeam = age < 110 ? 3 : 6;
            for (int k = 0; k < beams; k++) {
                double a = spin + k * (Math.PI * 2 / beams);
                double x = top.x + Math.cos(a) * 1.6;
                double z = top.z + Math.sin(a) * 1.6;
                for (int h = 0; h < perBeam; h++) {
                    double y = top.y - 0.3 + w.random.nextDouble() * (r.riseHeight + 6.0);
                    spark(w, x, y, z, 0, 0.18, 0);
                }
            }
            for (int h = 0; h < 4; h++) {
                spark(w, ring.x, ring.y + w.random.nextDouble() * 9.0, ring.z, 0, 0.25, 0);
            }
        }

        // энергетические волны: расходящиеся кольца на высоте кольца
        if (age >= 100 && age < FLASH_TICK && age % 8 == 0) {
            for (int step = 1; step <= 6; step++) {
                double rr = step * 0.9;
                int points = 24 + step * 4;
                for (int k = 0; k < points; k++) {
                    double a = k * (Math.PI * 2 / points);
                    double dx = Math.cos(a);
                    double dz = Math.sin(a);
                    spark(w, ring.x + dx * rr, ring.y, ring.z + dz * rr, dx * 0.12, 0.02, dz * 0.12);
                }
            }
        }

        // лучи в стороны, как при гибели Дракона Края
        if (age >= 118 && age < FLASH_TICK) {
            for (int ray = 0; ray < 6; ray++) {
                double a = w.random.nextDouble() * Math.PI * 2;
                double e = (w.random.nextDouble() - 0.35) * 1.2;
                Vec3 dir = new Vec3(Math.cos(a) * Math.cos(e), Math.sin(e), Math.sin(a) * Math.cos(e));
                for (int s = 1; s <= 10; s++) {
                    spark(w, ring.x + dir.x * s * 0.8, ring.y + dir.y * s * 0.8, ring.z + dir.z * s * 0.8,
                            dir.x * 0.25, dir.y * 0.25, dir.z * 0.25);
                }
            }
            smoke(w, ring.x, ring.y, ring.z, (w.random.nextDouble() - 0.5) * 0.2, 0.08, (w.random.nextDouble() - 0.5) * 0.2);
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

        // вспышка: клубы багрового дыма, фонтан искр, разрыв пространства, гром
        w.sendParticles(ModParticles.CRIMSON_SMOKE.get(), pos.x, pos.y, pos.z, 180, 0.4, 0.4, 0.4, 0.4);
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), pos.x, pos.y, pos.z, 260, 0.3, 0.3, 0.3, 1.0);
        w.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.x, pos.y, pos.z, 120, 0.5, 0.5, 0.5, 0.6);
        w.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        w.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 60, 0.4, 0.4, 0.4, 0.15);
        w.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y, pos.z, 60, 0.4, 0.4, 0.4, 0.2);
        for (int h = 0; h < 50; h++) {
            spark(w, pos.x, pos.y + h * 0.4, pos.z, 0, 0.35, 0);
        }
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
        w.sendParticles(ModParticles.CRIMSON_SPARK.get(), pos.x, pos.y, pos.z, 40, 0.3, 0.3, 0.3, 0.3);

        if (owner != null) {
            AdvancementHolder advancement = server.getAdvancements().get(ADVANCEMENT_ID);
            if (advancement != null) {
                owner.getAdvancements().award(advancement, "ritual_complete");
            }
        }
    }
}
