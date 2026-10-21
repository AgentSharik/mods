package dev.agentsharik.angelring.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Quaternionf;
import dev.agentsharik.angelring.*;
import dev.agentsharik.angelring.client.ui.*;
import dev.agentsharik.angelring.network.RelicActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Texture-aligned logical canvas; all text and tooltips are laid out on data changes, not every frame. */
public final class RelicScreen extends Screen {
    private static final int W = 340, H = 224;
    private static final int TEXT = 0xFFE8DCD8, DIM = 0xFFA99FA5, RED = 0xFFC0394A;
    private static final ResourceLocation UI = tex("relic_ui"), FOG = tex("relic_fog"),
            HAZE = tex("relic_haze"), GLOW = tex("relic_haze_glow"),
            ICONS = tex("ability_icons"), STARS = tex("rarity_stars");
    private static final float[] WRAP = {0.7f, 0.6f, 0.5f};
    private static ResourceLocation tex(String s) {
        return ResourceLocation.fromNamespaceAndPath(AngelRingMod.MOD_ID, "textures/gui/" + s + ".png");
    }
    private static Component tr(String s, Object... args) { return Component.translatable("gui.angelring." + s, args); }
    private static String num(float v) { return String.format(Locale.ROOT, "%.1f", v); }

    private final Screen previous;
    private final AbstractContainerMenu menu;
    private final int menuSlot;
    private final Quaternionf hazeRotation = new Quaternionf();
    private final Ease.Clock clock = new Ease.Clock();
    private final ParticlePool particles = new ParticlePool(128);
    private final List<TextBox> labels = new ArrayList<>();
    private final Widget[] cards = new Widget[3], plus = new Widget[3], plaques = new Widget[3];
    private final Widget reroll = new Widget(272, 152, 62, 20);
    private final float[] starAnim = new float[6], starAt = new float[6], upgradeFlash = new float[3];
    private final boolean[] starTarget = new boolean[6];
    private final int[] seconds = {-1, -1, -1};
    private final TextBox[] cooldownText = new TextBox[3];
    private final TextBox[] cardName = new TextBox[3], plusText = new TextBox[3], costText = new TextBox[3];
    private final TextBox[] plaqueName = new TextBox[3], plaqueValue = new TextBox[3];
    private final float[][] embers = new float[40][4];
    private TextBox rerollText, rerollCost, xpText, badge, pendingText;
    private RelicData data = RelicData.EMPTY;
    private ItemStack ring = ItemStack.EMPTY;
    private int cachedLevels = -1;
    private boolean cachedCreative, initialized, closing, pending, hold;
    private int pendingAction = -1, pendingArg;
    private boolean pendingAck, pendingChanged;
    private RelicData pendingData = RelicData.EMPTY;
    private float scale = 1, ox, oy, windowScale = 1, windowY, alpha, closeAt,
            holdTime, pendingAt, xpDisplay, badgeFlash, px, py, rerollAt = -10, rerollRelease = -10;
    private int rerollCounter;
    private boolean rerollWaiting;
    private Widget tooltipOwner;
    private float tooltipFade;

    private static final class Widget {
        final int x, y, w, h;
        float hover, press, disabled, shake;
        boolean enabled = true;
        TextBox[] tip = new TextBox[0];
        int tipWidth, tipHeight;
        Widget(int x, int y, int w, int h) { this.x=x; this.y=y; this.w=w; this.h=h; }
        boolean contains(float mx, float my) { return mx>=x && mx<x+w && my>=y && my<y+h; }
        void step(float mx, float my, float dt, boolean locked) {
            hover=Ease.approach(hover, contains(mx,my) ? 1:0, dt, 14);
            press=Ease.approach(press, 0, dt, 18);
            disabled=Ease.approach(disabled, !enabled || locked ? 1:0, dt, 12);
            shake=Math.max(0, shake-dt*3);
        }
        void begin(GuiGraphics g, float t) {
            float s=1+0.04f*Ease.smoothStep(hover)-0.05f*press;
            g.pose().pushPose();
            g.pose().translate(x+w/2f+(float)Math.sin(t*70)*shake*2, y+h/2f, 0);
            g.pose().scale(s,s,1);
            g.pose().translate(-x-w/2f,-y-h/2f,0);
        }
    }

    public RelicScreen(Screen previous, AbstractContainerMenu menu, int menuSlot) {
        super(Component.translatable("item.angelring.angel_ring"));
        this.previous=previous; this.menu=menu; this.menuSlot=menuSlot;
        int[] cx={35,105,175}, qx={272,276,272}, qy={60,90,120};
        for(int i=0;i<3;i++) {
            cards[i]=new Widget(cx[i],134,62,48); plus[i]=new Widget(cx[i]+48,137,12,12);
            plaques[i]=new Widget(qx[i],qy[i],62,26); cooldownText[i]=new TextBox();
        }
        for(float[] e:embers) {
            e[0]=particles.rand()*460-60; e[1]=particles.rand()*330-50;
            e[2]=6+particles.rand()*12; e[3]=particles.rand()*6.28f;
        }
    }

    @Override protected void init() {
        float fit=Math.min((width-16f)/W,(height-16f)/H);
        scale=fit<1 ? Math.max(0.01f,fit) : Math.max(1, (float)Math.floor(Math.min(fit,0.75f*height/H)*2)/2);
        ox=(width-W*scale)/2; oy=(height-H*scale)/2;
        refresh(true);
        if(!initialized) {
            initialized=true; xpDisplay=progress(data);
            for(int i=0;i<6;i++) { starTarget[i]=i<data.ringStars(); starAnim[i]=starTarget[i]?1:0; }
            UiSounds.open();
        }
    }
    private static float progress(RelicData d) { return d.level()+(d.maxed()?0:d.xp()/(float)d.xpToNext()); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() {
        if(!closing) { closing=true; closeAt=clock.time; hold=false; }
    }
    @Override public void tick() {
        refresh(false);
        if(ring.isEmpty()) onClose();
    }
    private void refresh(boolean force) {
        if(minecraft.player==null) return;
        ring=RingLookup.resolve(minecraft.player,menu,menuSlot).orElse(ItemStack.EMPTY);
        RelicData next=ring.isEmpty()?RelicData.EMPTY:ring.getOrDefault(ModComponents.RELIC_DATA.get(),RelicData.EMPTY);
        int levels=minecraft.player.experienceLevel;
        boolean creative=minecraft.player.getAbilities().instabuild;
        if(!force && next.equals(data) && levels==cachedLevels && creative==cachedCreative) return;
        if(initialized) {
            if(next.level()>data.level()) badgeFlash=1; // world level-up sound already comes from server
            for(int i=0;i<3;i++) if(next.abilityLevel(i)>data.abilityLevel(i)) upgradeFlash[i]=1;
            if(next.rerolls()!=data.rerolls() || next.ringStars()!=data.ringStars()) {
                for(int i=0;i<6;i++) {
                    starTarget[i]=i<next.ringStars(); starAt[i]=Math.max(clock.time,rerollWaiting?rerollAt+0.6f:clock.time)+0.07f*i;
                }
                if(rerollWaiting && next.rerolls()!=rerollCounter) {
                    rerollWaiting=false; rerollRelease=Math.max(clock.time,rerollAt+0.6f);
                }
            }
        }
        if (pending && (pendingAction==RelicData.ACTION_REROLL
                ? next.rerolls()!=pendingData.rerolls()
                : next.abilityLevel(pendingArg)>pendingData.abilityLevel(pendingArg))) {
            pendingChanged=true;
            if(pendingAck) { pending=false; pendingAction=-1; }
        }
        if (!next.equals(data)) { hold=false; holdTime=0; }
        data=next; cachedLevels=levels; cachedCreative=creative;
        rebuild();
    }
    private TextBox line(Component c,float x,float y,float w,float s,int col,int align) {
        return new TextBox().line(font,c,x,y,w,s,col,true,align);
    }
    private void add(Component c,float x,float y,float w,float s,int col,int align) { labels.add(line(c,x,y,w,s,col,align)); }
    private void rebuild() {
        labels.clear();
        add(getTitle(),94,32,146,1.1f,0xFFF2D9D3,0);
        add(tr("subtitle"),94,44,146,0.6f,0xFFD0606A,0);
        labels.add(new TextBox().wrap(font,Component.translatable("item.angelring.angel_ring.desc"),94,54,146,25,4,WRAP,0xFFC4BABF,true,0));
        add(tr("chip_level",cachedLevels),130,84,66,0.8f,TEXT,1);
        add(tr("chip_xp",data.xp()),206,84,66,0.8f,TEXT,1);
        badge=line(Component.literal(""+data.level()),59,28,20,0.8f,TEXT,1);
        xpText=line(data.maxed()?tr("max"):Component.literal(Math.round(100f*data.xp()/data.xpToNext())+"%"),136,103,198,0.8f,TEXT,1);
        add(tr("abilities"),34,124,70,0.6f,TEXT,0);
        add(tr("abilities_hint",ClientModEvents.ABILITY_1.getTranslatedKeyMessage(),ClientModEvents.ABILITY_2.getTranslatedKeyMessage()),238,124,132,0.6f,0xFFE09098,2);
        add(Component.literal("F A L L E N"),304,21,64,0.7f,0xFFFFA090,1);
        add(Component.literal("RELICS"),304,31,64,1.5f,0xFFFF6A58,1);
        add(tr("hint"),140,204,224,0.6f,TEXT,1);
        add(tr("hint2"),140,213,224,0.6f,0xFFE09098,1);
        labels.add(new TextBox().wrap(font,tr("rarity_note"),274,179,58,22,3,WRAP,DIM,true,0));
        String[] names={"level","flight","rarity"};
        String[] values={data.level()+" / 10","+"+Math.round((data.flightMultiplier()-1)*100)+"%",data.ringStars()+" / 6"};
        for(int i=0;i<3;i++) {
            Widget p=plaques[i];
            plaqueName[i]=line(tr("stat."+names[i]),p.x+6,p.y+5,50,0.6f,DIM,0);
            plaqueValue[i]=line(Component.literal(values[i]),p.x+6,p.y+13,50,0.9f,TEXT,0);
            setTip(p,List.of(tr("stat."+names[i]),i==1?tr("stat.flight.tip"):Component.literal(values[i])));
            RelicAbility a=RelicAbility.byId(i); int l=data.abilityLevel(i), cost=data.upgradeCost(i);
            cardName[i]=line(tr("ability."+a.key+".name"),cards[i].x+31,171,56,0.65f,TEXT,1);
            plusText[i]=line(Component.literal(l>=4?"•":"+"),plus[i].x+6,plus[i].y+2,10,0.9f,TEXT,1);
            costText[i]=line(cost<0?Component.empty():Component.literal(""+cost),plus[i].x+6,plus[i].y+15,12,0.7f,0xFFE09098,1);
            plus[i].enabled=data.canUpgrade(i)&&(cachedCreative||cachedLevels>=cost);
            setTip(cards[i],abilityTip(a));
            ArrayList<Component> tip=new ArrayList<>();
            tip.add(tr(l==0?"learn":"improve",Math.min(4,l+1)).copy().withStyle(ChatFormatting.GOLD));
            preview(tip,a,data.abilityStars(i),l,Math.min(4,l+1),false);
            if(l>=4) tip.add(tr("max_level"));
            else {
                tip.add(tr("cost_levels",cost));
                if(l>=data.abilityCap()) {
                    int target=RelicData.ringLevelFor(l+1);
                    tip.add(tr("need_ring_xp",target,data.ringXpUntil(target)).copy().withStyle(ChatFormatting.RED));
                }
                if(!cachedCreative && cachedLevels<cost) tip.add(tr("missing_levels",cost-cachedLevels).copy().withStyle(ChatFormatting.RED));
            }
            setTip(plus[i],tip);
        }
        reroll.enabled=cachedCreative||cachedLevels>=RelicData.REROLL_COST;
        pendingText=line(tr("pending"),136,184,204,0.6f,DIM,1);
        rerollText=line(tr("reroll"),303,156,58,0.85f,TEXT,1);
        rerollCost=line(tr("reroll.cost",RelicData.REROLL_COST),303,164,58,0.6f,DIM,1);
        ArrayList<Component> rt=new ArrayList<>();
        rt.add(tr("reroll.tip1").copy().withStyle(ChatFormatting.GOLD));
        rt.add(tr("reroll.tip2")); rt.add(tr("reroll.preview"));
        rt.add(tr("preview.stars",data.ringStars(),0,6));
        for(int i=0;i<3;i++) {
            RelicAbility a=RelicAbility.byId(i);
            rt.add(tr("ability."+a.key+".name").copy().withStyle(ChatFormatting.GOLD));
            preview(rt,a,data.ringStars(),data.abilityLevel(i),data.abilityLevel(i),true);
        }
        rt.add(tr("reroll.odds",Math.round(100*RelicData.rerollOdds(data.ringStars(),1)),Math.round(100*RelicData.rerollOdds(data.ringStars(),0)),Math.round(100*RelicData.rerollOdds(data.ringStars(),-1))));
        rt.add(tr("reroll.tip3",RelicData.REROLL_COST));
        if(data.rerollNeedsConfirm()) rt.add(tr("hold_confirm").copy().withStyle(ChatFormatting.RED));
        if(!reroll.enabled) rt.add(tr("missing_levels",RelicData.REROLL_COST-cachedLevels).copy().withStyle(ChatFormatting.RED));
        setTip(reroll,rt);
    }
    private List<Component> abilityTip(RelicAbility a) {
        int l=data.abilityLevel(a.id), s=data.abilityStars(a.id);
        ArrayList<Component> lines=new ArrayList<>();
        lines.add(tr("ability."+a.key+".name").copy().withStyle(ChatFormatting.GOLD));
        lines.add(tr(a.active?"type_active":"type_passive"));
        lines.add(tr("level_of",l,4)); lines.add(tr("rarity_line","★".repeat(s)+"☆".repeat(6-s)));
        lines.add(switch(a) {
            case BONDS -> tr("ability.bonds.desc",num(a.radius(s)),num(a.durationSeconds(s,Math.max(1,l))));
            case SANCTUARY -> tr("ability.sanctuary.desc",num(a.durationSeconds(s,Math.max(1,l))),num(a.radius(s)));
            case BLOOD_WARD -> tr("ability.blood_ward.desc",num(100*RelicAbility.wardChance(s,Math.max(1,l))),num(RelicAbility.wardHeal(Math.max(1,l))));
        });
        if(l==0) lines.add(tr("not_learned").copy().withStyle(ChatFormatting.GRAY));
        return lines;
    }
    private float stat(RelicAbility a,int index,int stars,int level) {
        return switch(index) {
            case 0 -> a.durationSeconds(stars,level);
            case 1 -> a.cooldownSeconds(stars,level);
            case 2 -> a.radius(stars);
            case 3 -> 100*RelicAbility.wardChance(stars,level);
            default -> RelicAbility.wardHeal(level);
        };
    }
    private void preview(List<Component> out,RelicAbility a,int stars,int current,int next,boolean random) {
        String[] keys={"duration","cooldown","radius","chance","heal"};
        String[] units={"sec","sec","blocks","percent","health"};
        int start=a.active?0:3, end=a.active?3:5;
        for(int i=start;i<end;i++) {
            float old=stat(a,i,stars,Math.max(1,current)), n=stat(a,i,stars,Math.max(1,next));
            Component before=current==0?Component.literal("—"):tr("unit."+units[i],num(old));
            Component after=random?tr("preview.range",tr("unit."+units[i],num(Math.min(stat(a,i,0,Math.max(1,next)),stat(a,i,6,Math.max(1,next))))),tr("unit."+units[i],num(Math.max(stat(a,i,0,Math.max(1,next)),stat(a,i,6,Math.max(1,next)))))):tr("unit."+units[i],num(n));
            ChatFormatting color=random?ChatFormatting.RED:(current==0 || (i==1?n<old:n>old))?ChatFormatting.GREEN:ChatFormatting.GRAY;
            out.add(tr("preview.line",tr("stat."+keys[i]),before,after).copy().withStyle(color));
        }
    }
    private void setTip(Widget w,List<Component> lines) {
        // Wrapped just once. 252 logical pixels wide, also bounded by the actual viewport at tiny GUI sizes.
        int maxW=Math.max(40,Math.min(252,(int)(width/scale)-16));
        ArrayList<TextBox> boxes=new ArrayList<>(); float y=5;
        for(Component c:lines) {
            TextBox b=new TextBox().wrap(font,c,6,y,maxW-12,200,30,new float[]{0.75f},TEXT,true,0);
            boxes.add(b); y+=b.height()+2;
        }
        w.tip=boxes.toArray(TextBox[]::new); w.tipWidth=maxW; w.tipHeight=(int)Math.ceil(y+3);
    }

    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick) {
        clock.frame(); float dt=clock.dt, t=clock.time;
        float open=Ease.clamp01(t/0.35f), close=closing?Ease.outCubic((t-closeAt)/0.15f):0;
        alpha=Ease.outCubic(open)*(1-close); windowScale=(0.94f+0.06f*Ease.outBack(open))*(1-0.04f*close);
        windowY=8*(1-Ease.outCubic(open))+5*close;
        if(closing && close>=1) { minecraft.setScreen(previous); return; }
        float mx=logicalX(mouseX), my=logicalY(mouseY);
        px=Ease.approach(px,Math.max(-1,Math.min(1,(mx-170)/170)),dt,5);
        py=Ease.approach(py,Math.max(-1,Math.min(1,(my-112)/112)),dt,5);
        if(pending && t-pendingAt>2.5f) { pending=false; }
        if(rerollWaiting && t-rerollAt>2.5f) { rerollWaiting=false; rerollRelease=t; }
        if(hold) {
            if(!reroll.contains(mx,my)||!reroll.enabled||closing) { hold=false; holdTime=0; }
            else if((holdTime+=dt)>=0.9f) { hold=false; send(RelicData.ACTION_REROLL,0,RelicActions.FLAG_CONFIRMED,reroll); }
        }
        reroll.step(mx,my,dt,pending);
        for(int i=0;i<3;i++) { cards[i].step(mx,my,dt,false); plus[i].step(mx,my,dt,pending); plaques[i].step(mx,my,dt,false); }
        particles.update(dt);
        xpDisplay=Ease.approach(xpDisplay,progress(data),dt,6);
        badgeFlash=Math.max(0,badgeFlash-dt*1.3f);
        for(int i=0;i<6;i++) if(t>=starAt[i]) starAnim[i]=Ease.approach(starAnim[i],starTarget[i]?1:0,dt,10);
        for(int i=0;i<3;i++) upgradeFlash[i]=Math.max(0,upgradeFlash[i]-dt);
        g.fill(0,0,width,height,argb(alpha*0.62f,0x050306));
        g.pose().pushPose();
        g.pose().translate(ox+W*scale/2,oy+H*scale/2+windowY*scale,0);
        g.pose().scale(scale*windowScale,scale*windowScale,1);
        g.pose().translate(-W/2f,-H/2f,0);
        RenderSystem.enableBlend();
        haze(g,t);
        tint(alpha); g.blit(UI,0,0,0,0,W,214,512,256); tint(1);
        if(open>=0.55f && !ring.isEmpty()) {
            g.pose().pushPose(); g.pose().translate(39,37,0); g.pose().scale(2.5f,2.5f,1);
            g.renderItem(ring,0,0); g.pose().popPose();
        }
        for(TextBox b:labels) b.draw(g,font,alpha);
        badge.draw(g,font,alpha);
        if(pending) pendingText.draw(g,font,alpha);
        if(badgeFlash>0) g.renderOutline(46,26,25,12,argb(badgeFlash*alpha,0xFFD3A0));
        float bar=data.maxed()?1:Ease.clamp01(xpDisplay-(float)Math.floor(xpDisplay));
        g.fillGradient(34,103,34+Math.round(204*bar),111,argb(alpha,0xE0524A),argb(alpha,0x7A1424));
        xpText.draw(g,font,alpha);
        for(int i=0;i<6;i++) {
            float k=starAnim[i], flash=Math.abs((starTarget[i]?1:0)-k);
            g.pose().pushPose(); g.pose().translate(39+i*8,90,320); float s=1+0.3f*flash; g.pose().scale(s,s,1);
            tint(alpha); g.blit(STARS,-4,-4,8,0,8,8,16,8);
            tint(alpha*k); g.blit(STARS,-4,-4,0,0,8,8,16,8); tint(1); g.pose().popPose();
        }
        for(int i=0;i<3;i++) {
            Widget p=plaques[i]; p.begin(g,t); widgetSkin(g,p); plaqueName[i].draw(g,font,alpha); plaqueValue[i].draw(g,font,alpha); g.pose().popPose();
            drawCard(g,i,t);
        }
        reroll.begin(g,t); widgetSkin(g,reroll); rerollText.draw(g,font,alpha); rerollCost.draw(g,font,alpha); g.pose().popPose();
        if(hold) g.fill(273,170,273+Math.round(60*Ease.clamp01(holdTime/0.9f)),172,argb(alpha,0xFF8070));
        float charge=rerollWaiting?Ease.outCubic((t-rerollAt)/0.6f):Math.max(0,1-(t-rerollRelease)/0.65f);
        if(charge>0) {
            g.pose().pushPose(); g.pose().translate(0,0,300);
            blob(g,GLOW,59,59,0.25f+0.06f*(float)Math.sin(t*9),t*130,charge*alpha,1,0.35f,0.35f,true);
            g.pose().popPose();
        }
        g.pose().pushPose(); g.pose().translate(0,0,350); particles.render(g,alpha); g.pose().popPose();
        g.pose().popPose();
        tooltip(g,mouseX,mouseY,hit(mx,my),dt);
        tint(1); RenderSystem.defaultBlendFunc();
    }
    private void widgetSkin(GuiGraphics g,Widget w) {
        tint(alpha); g.blit(UI,w.x,w.y,w.x,w.y,w.w,w.h,512,256); tint(1);
        if(w.disabled>0.01f) g.fill(w.x,w.y,w.x+w.w,w.y+w.h,argb(alpha*0.6f*w.disabled,0x28252C));
        if(w.hover>0.01f) g.renderOutline(w.x,w.y,w.w,w.h,argb(alpha*Ease.smoothStep(w.hover)*(1-0.5f*w.disabled),0xE65064));
    }
    private void drawCard(GuiGraphics g,int id,float t) {
        Widget c=cards[id]; int level=data.abilityLevel(id); c.begin(g,t); widgetSkin(g,c);
        g.pose().pushPose(); g.pose().translate(c.x+15,c.y+4,0); g.pose().scale(2,2,1);
        float k=level==0?0.3f:1; RenderSystem.setShaderColor(k,k,k,alpha);
        g.blit(ICONS,0,0,id*16,0,16,16,48,16); tint(1); g.pose().popPose();
        for(int p=0;p<4;p++) g.fill(c.x+4,c.y+33-p*6,c.x+9,c.y+37-p*6,argb(alpha,p<level?0xEF5767:0x3F2932));
        cardName[id].draw(g,font,alpha);
        for(int s=0;s<6;s++) {
            g.pose().pushPose(); g.pose().translate(c.x+16+s*5,c.y+43,0); g.pose().scale(0.5f,0.5f,1);
            tint(alpha); g.blit(STARS,0,0,8,0,8,8,16,8); tint(alpha*starAnim[s]); g.blit(STARS,0,0,0,0,8,8,16,8); tint(1); g.pose().popPose();
        }
        if(id<2 && minecraft.level!=null && level>0) {
            double now=minecraft.level.getGameTime()+minecraft.getTimer().getGameTimeDeltaPartialTick(false);
            double left=Math.max(0,data.cooldownReadyAt(id)-now);
            if(left>0) {
                float ratio=Ease.clamp01((float)(left/Math.max(1,20*RelicAbility.byId(id).cooldownSeconds(data.ringStars(),level))));
                int hh=Math.round(32*ratio); g.fill(c.x+14,c.y+36-hh,c.x+47,c.y+36,argb(alpha*0.65f,0));
                int sec=(int)Math.ceil(left/20);
                if(sec!=seconds[id]) { seconds[id]=sec; cooldownText[id].line(font,Component.literal(sec+"s"),c.x+31,c.y+16,32,1.1f,TEXT,true,1); }
                cooldownText[id].draw(g,font,alpha);
            }
        }
        if(upgradeFlash[id]>0) g.renderOutline(c.x-1,c.y-1,c.w+2,c.h+2,argb(alpha*upgradeFlash[id],0xFF9B83));
        plus[id].begin(g,t); widgetSkin(g,plus[id]); plusText[id].draw(g,font,alpha); costText[id].draw(g,font,alpha); g.pose().popPose();
        g.pose().popPose();
    }
    private void haze(GuiGraphics g,float t) {
        blob(g,FOG,170+px*3,106+py*3,1.04f,(float)Math.sin(t*0.12f)*3,alpha*0.65f,0.55f,0.55f,0.6f,false);
        blob(g,HAZE,170+px*8,106+py*8,1+0.025f*(float)Math.sin(t*0.6f),(float)Math.sin(t*0.3f)*3,alpha*0.9f,1,1,1,false);
        blob(g,GLOW,170+px*8,106+py*8,1.04f+0.025f*(float)Math.sin(t*0.8f),-t*0.5f,alpha*0.5f,1,0.65f,0.65f,true);
        for(float[] e:embers) {
            e[1]-=e[2]*clock.dt; if(e[1]<-45) e[1]=285;
            int x=Math.round(e[0]+(float)Math.sin(t+e[3])*5+px*11), y=Math.round(e[1]+py*11);
            float a=alpha*(0.25f+0.2f*(float)Math.sin(t*2+e[3]));
            g.fill(x,y,x+1,y+2,argb(a,0xF67B72));
        }
    }
    private void blob(GuiGraphics g,ResourceLocation tex,float x,float y,float s,float rot,float a,float r,float green,float b,boolean additive) {
        // Flush around blend changes: queued GUI buffers must not inherit the next layer's blend state.
        g.flush(); RenderSystem.enableBlend();
        if(additive) RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,GlStateManager.DestFactor.ONE);
        else RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(r,green,b,a);
        g.pose().pushPose(); g.pose().translate(x,y,-10); g.pose().mulPose(hazeRotation.rotationZ(rot * ((float)Math.PI / 180f))); g.pose().scale(s,s,1);
        g.blit(tex,-256,-192,0,0,512,384,512,384); g.pose().popPose();
        g.flush(); tint(1); RenderSystem.defaultBlendFunc();
    }
    private Widget hit(float x,float y) {
        for(int i=0;i<3;i++) if(plus[i].contains(x,y)) return plus[i];
        if(reroll.contains(x,y)) return reroll;
        for(int i=0;i<3;i++) { if(cards[i].contains(x,y)) return cards[i]; if(plaques[i].contains(x,y)) return plaques[i]; }
        return null;
    }
    private void tooltip(GuiGraphics g,int mx,int my,Widget w,float dt) {
        if(w!=tooltipOwner) {
            tooltipFade=Ease.approach(tooltipFade,0,dt,25);
            if(tooltipFade<0.05f) { tooltipOwner=w; tooltipFade=0; }
        } else tooltipFade=Ease.approach(tooltipFade,w==null?0:1,dt,14);
        if(tooltipOwner==null || tooltipFade<0.02f) return;
        Widget tw=tooltipOwner;
        float ts=Math.min(scale,1.5f);
        // Long reroll previews remain on-screen even on small GUI-scale viewports.
        ts=Math.min(ts,(height-12f)/Math.max(1,tw.tipHeight));
        float x=Math.max(4,Math.min(mx+12,width-tw.tipWidth*ts-4));
        float y=Math.max(4,Math.min(my+12,height-tw.tipHeight*ts-4))+4*(1-Ease.outCubic(tooltipFade));
        g.pose().pushPose(); g.pose().translate(x,y,400); g.pose().scale(ts,ts,1);
        float a=alpha*Ease.outCubic(tooltipFade);
        g.fill(0,0,tw.tipWidth,tw.tipHeight,argb(a*0.97f,0x10090F));
        g.renderOutline(0,0,tw.tipWidth,tw.tipHeight,argb(a,0x893747));
        for(TextBox b:tw.tip) b.draw(g,font,a);
        g.pose().popPose();
    }
    private float logicalX(double x) { return (float)((x-ox-W*scale/2)/(scale*windowScale)+W/2f); }
    private float logicalY(double y) { return (float)((y-oy-H*scale/2-windowY*scale)/(scale*windowScale)+H/2f); }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(button!=0 || closing) return super.mouseClicked(x,y,button);
        Widget w=hit(logicalX(x),logicalY(y));
        if(w==null) return false;
        if(w==reroll || w==plus[0] || w==plus[1] || w==plus[2]) {
            if(!w.enabled || pending) { w.shake=1; UiSounds.error(); return true; }
            w.press=1; UiSounds.press(1);
            particles.burstRect(w.x,w.y,w.w,w.h,14,RED,0xFFFF9B83);
            if(w==reroll) {
                if(data.rerollNeedsConfirm()) { hold=true; holdTime=0; }
                else send(RelicData.ACTION_REROLL,0,0,w);
            } else for(int i=0;i<3;i++) if(w==plus[i]) send(RelicData.ACTION_UPGRADE,i,0,w);
        }
        return true;
    }
    @Override public boolean mouseReleased(double x,double y,int button) {
        if(button==0 && hold) { hold=false; holdTime=0; return true; }
        return super.mouseReleased(x,y,button);
    }
    private void send(int action,int arg,int flags,Widget w) {
        pending=true; pendingAck=false; pendingChanged=false; pendingData=data;
        pendingAt=clock.time; pendingAction=action; pendingArg=arg;
        if(action==RelicData.ACTION_REROLL) {
            rerollAt=clock.time; rerollCounter=data.rerolls(); rerollWaiting=true; rerollRelease=-10;
        }
        PacketDistributor.sendToServer(new RelicActionPayload(action,arg,menuSlot,menu==null?0:menu.containerId,flags));
    }
    public void onServerResult(int action,int arg,int code) {
        if(action==RelicData.ACTION_WARD_PROC) { AbilityHud.onWardProc(); return; }
        if(action!=pendingAction || arg!=pendingArg) return;
        if(code==RelicActions.Result.OK) {
            pendingAck=true;
            if(pendingChanged) { pending=false; pendingAction=-1; }
            if(action==RelicData.ACTION_REROLL) UiSounds.reroll(); else UiSounds.chord();
        } else {
            pending=false; pendingAction=-1;
            UiSounds.error();
            if(action==RelicData.ACTION_REROLL) { reroll.shake=1; rerollWaiting=false; rerollRelease=clock.time; }
            else if(arg>=0 && arg<3) plus[arg].shake=1;
        }
    }
    private static int argb(float a,int rgb) { return ((int)(255*Ease.clamp01(a))<<24)|(rgb&0xFFFFFF); }
    private static void tint(float a) { RenderSystem.setShaderColor(1,1,1,a); }
}
