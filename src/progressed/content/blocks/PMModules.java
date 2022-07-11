package progressed.content.blocks;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import progressed.content.*;
import progressed.content.bullets.*;
import progressed.content.effects.*;
import progressed.entities.bullet.energy.*;
import progressed.entities.bullet.physical.*;
import progressed.entities.bullet.physical.DelayBulletType.*;
import progressed.graphics.*;
import progressed.world.blocks.defence.turret.modular.ModularTurret.*;
import progressed.world.blocks.defence.turret.modular.modules.BaseModule.*;
import progressed.world.blocks.defence.turret.modular.modules.*;
import progressed.world.blocks.defence.turret.modular.modules.turret.*;
import progressed.world.blocks.defence.turret.modular.mounts.*;
import progressed.world.blocks.payloads.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

public class PMModules{
    public static float maxClip = 0;

    public static Block cons;

    public static ModulePayload

    //Region Small

    shrapnel, pinpoint, froth, bifurcation, iris, bandage, overclocker,

    //Region Medium

    blunderbuss, airburst, vulcan, lotus, gravity, ambrosia, vigilance,

    //Region Large

    rebound, trifecta, ares, jupiter;

    public static void load(){
        //Proxy block used by modules for consume filters
        cons = new Block("module-consumes"){
            {
                requirements(Category.turret, BuildVisibility.hidden, empty);
            }

            @Override
            public boolean isHidden(){
                return true;
            }
        };

        //Region small
        shrapnel = new ModulePayload("shrapnel"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 40
            ));
            module = new ItemTurretModule("shrapnel"){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopper,
                    Items.graphite, ModuleBullets.shotgunDense,
                    Items.titanium, ModuleBullets.shotgunTitanium,
                    Items.thorium, ModuleBullets.shotgunThorium
                );

                reload = 90f;
                shootCone = 30;
                range = 120f;
                shoot.shots = 5;
                inaccuracy = 25;
                velocityRnd = 0.2f;
                rotateSpeed = 9f;

                limitRange(6f);

                coolant = consumeCoolant(0.2f);
            }};
        }};

        pinpoint = new ModulePayload("pinpoint"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 80,
                Items.silicon, 40,
                PMItems.tenelium, 30
            ));
            module = new ItemTurretModule("pinpoint"){
                {
                    ammo(
                        Items.surgeAlloy, ModuleBullets.pinpointPin
                    );

                    range = 34.5f * tilesize;
                    targetBlocks = canOverdrive = false;
                    logicControl = playerControl = false;
                    single = true;
                    reload = 5.5f * 60f;
                    heatColor = Color.red;

                    unitSort = (u, x, y) -> -u.health + Mathf.dst2(u.x, u.y, x, y) / 6400f + (u.hasEffect(PMStatusEffects.pinpointTarget) ? 69420 : 0);

                    limitRange(3f);

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                protected void handleBullet(ModularTurretBuild parent, TurretMount mount, Bullet bullet, float offsetX, float offsetY, float angleOffset){
                    super.handleBullet(parent, mount, bullet, offsetX, offsetY, angleOffset);
                    bullet.data = mount.target;
                }
            };
        }};

        froth = new ModulePayload("froth"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.metaglass, 35f,
                Items.lead, 60f
            ));
            module = new LiquidTurretModule("froth"){{
                ammo(
                    Liquids.water, ModuleBullets.waterShotMini,
                    Liquids.slag, ModuleBullets.slagShotMini,
                    Liquids.cryofluid, ModuleBullets.cryoShotMini,
                    Liquids.oil, ModuleBullets.oilShotMini
                );

                reload = 5f;
                range = 96f;
                targetAir = false;
                inaccuracy = 8;
                recoil = 0f;
                shootCone = 20f;
                shootEffect = Fx.shootLiquid;
            }};
        }};

        bifurcation = new ModulePayload("bifurcation"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 70,
                Items.lead, 60
            ));
            module = new PowerTurretModule("bifurcation"){{
                shootType = new LightningBulletType(){{
                    damage = 12;
                    lightningLength = 20;
                    collidesAir = false;
                    displayAmmoMultiplier = false;
                }};

                reload = 35f;
                shootCone = 40f;
                powerUse = 3.5f;
                targetAir = false;
                range = 100f;
                shootEffect = Fx.lightningShoot;
                heatColor = Color.red;
                shootSound = Sounds.spark;

                shoot = new ShootBarrel(){{
                    shots = 2;
                    barrels = new float[]{
                        -1.5f, 0f, 0f,
                        1.5f, 0f, 0f
                    };
                }};

                coolant = consumeCoolant(0.2f);
            }};
        }};

        iris = new ModulePayload("iris"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 60,
                Items.lead, 30,
                Items.silicon, 40
            ));
            module = new PowerTurretModule("iris"){
                {
                    powerUse = 1f;
                    reload = 30f;
                    minRange = 4f * 8f;
                    range = 26f * 8f;
                    shootY = 0f;
                    rotate = false;
                    inaccuracy = 360f;
                    shootSound = Sounds.lasershoot;

                    shootType = ModuleBullets.irisOrb;

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                protected void handleBullet(ModularTurretBuild parent, TurretMount mount, Bullet bullet, float offsetX, float offsetY, float angleOffset){
                    super.handleBullet(parent, mount, bullet, offsetX, offsetY, angleOffset);

                    Tmp.v1.set(mount.targetPos).sub(mount.x, mount.y);
                    if(Tmp.v1.len() < minRange) Tmp.v1.setLength(minRange);
                    Tmp.v1.add(mount.x, mount.y);

                    bullet.data = new DelayBulletData(Tmp.v1.x, Tmp.v1.y, 30f);
                }
            };
        }};

        //amazing name lmao
        bandage = new ModulePayload("bandage"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.lead, 20,
                Items.copper, 15
            ));
            module = new ChargeModule("bandage"){
                final float healPercent = 5f;

                {
                    powerUse = 0.75f;
                    reload = 150f;

                    activate = (p, m) -> {
                        if(p.damaged()){
                            p.heal(p.maxHealth() * healPercent / 100f * edelta(p));
                            Fx.healBlockFull.at(p.x, p.y, p.block.size, PMPal.heal);
                        }
                    };

                    setStats = () -> stats.add(Stat.repairTime, (int)(100f / healPercent * reload / 60f), StatUnit.seconds);
                }

                @Override
                public boolean isActive(ModularTurretBuild parent, BaseMount mount){
                    return super.isActive(parent, mount) && parent.damaged();
                }
            };
        }};

        overclocker = new ModulePayload("mini-overdrive"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.lead, 40,
                Items.titanium, 20,
                Items.silicon, 20
            ));
            module = new ChargeModule("mini-overdrive"){
                final float speedBoost = 1.25f;

                {
                    powerUse = 0.75f;
                    single = true;

                    activate = (p, m) -> {
                        m.charge = 0f;
                        p.applyBoost(speedBoost * p.efficiency, 61f);
                    };

                    setStats = () -> stats.add(Stat.speedIncrease, "+" + (int)(speedBoost * 100f - 100) + "%");
                }
            };
        }};
        //endregion

        //Region medium
        blunderbuss = new ModulePayload("blunderbuss"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 130,
                Items.graphite, 70,
                Items.titanium, 80
            ));
            module = new ItemTurretModule("blunderbuss", ModuleSize.medium){{
                ammo(
                    Items.copper, ModuleBullets.shotgunCopperCrit,
                    Items.graphite, ModuleBullets.shotgunDenseCrit,
                    Items.titanium, ModuleBullets.shotgunTitaniumCrit,
                    Items.thorium, ModuleBullets.shotgunThoriumCrit
                );

                reload = 75f;
                shootCone = 25;
                range = 200f;
                shoot.shots = 6;
                inaccuracy = 12;
                velocityRnd = 0.2f;
                rotateSpeed = 9f;
                shootSound = Sounds.shootBig;

                limitRange(5f);

                coolant = consumeCoolant(0.2f);
            }};
        }};

        airburst = new ModulePayload("airburst"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 130,
                Items.titanium, 80,
                Items.graphite, 90
            ));
            module = new ItemTurretModule("airburst", ModuleSize.medium){{
                ammo(
                    Items.pyratite, ModuleBullets.swarmIncendiary,
                    Items.blastCompound, ModuleBullets.swarmBlast
                );

                reload = 60f;
                range = 200f;
                shootCone = 45;
                inaccuracy = 7f;
                velocityRnd = 0.2f;
                rotateSpeed = 3f;
                shootSound = Sounds.missile;

                shoot.shots = 7;
                shoot.shotDelay = 2f;
                xRand = 4f;

                coolant = consumeCoolant(0.2f);
            }};
        }};

        vulcan = new ModulePayload("vulcan"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 100,
                Items.lead, 130,
                Items.silicon, 60
            ));
            module = new PowerTurretModule("vulcan", ModuleSize.medium){{
                reload = 43f;
                powerUse = 10f;
                range = 116;
                rotateSpeed = 6.5f;
                shootSound = Sounds.shotgun;
                heatColor = Color.red;

                float brange = range + 10f;

                shootType = new ShrapnelBulletType(){{
                    damage = 67f;
                    length = brange;
                    width = 12f;
                    toColor = Pal.remove;
                    displayAmmoMultiplier = false;
                    shootEffect = smokeEffect = ModuleFx.flameShoot;
                    makeFire = true;
                    status = StatusEffects.burning;
                }};

                coolant = consumeCoolant(0.2f);
            }};
        }};

        lotus = new ModulePayload("lotus"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.titanium, 80,
                Items.lead, 90,
                Items.silicon, 70
            ));
            module = new PowerTurretModule("lotus", ModuleSize.medium){
                final float delay = 30f;
                final Effect waveEffect = ModuleFx.lotusWave;

                {
                    reload = 60f;
                    minRange = 4f * 8f;
                    range = 29f * 8f;
                    shootSound = Sounds.flame2;
                    shootEffect = ModuleFx.lotusShoot;
                    smokeEffect = ModuleFx.lotusShootSmoke;
                    powerUse = 12f;
                    rotate = false;

                    shootType = ModuleBullets.lotusLance;

                    shootY = 0f;
                    shoot = new ShootBarrel(){{
                        shots = 8;
                        shotDelay = 2f;

                        float
                            d1 = size() * tilesize / 2f,
                            d2 = Tmp.v1.trns(45f, d1).x;
                        barrels = new float[]{
                            0f, d1, 0f,
                            d2, d2, -45f,
                            d1, 0f, -90f,
                            d2, -d2, -135f,
                            0f, -d1, 180f,
                            -d2, -d2, 135f,
                            -d1, 0f, 90f,
                            -d2, d2, 45f
                        };
                    }};

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                public void shoot(ModularTurretBuild parent, TurretMount mount, BulletType type){
                    super.shoot(parent, mount, type);

                    Time.run(delay + shoot.firstShotDelay, () -> {
                        if(mount.valid(parent)) waveEffect.at(mount);
                    });
                }

                @Override
                protected void handleBullet(ModularTurretBuild parent, TurretMount mount, Bullet bullet, float offsetX, float offsetY, float angleOffset){
                    bullet.data = new DelayBulletData(mount.targetPos.x, mount.targetPos.y, delay - (shoot.shots - mount.queuedBullets - 1) * shoot.shotDelay);
                }
            };
        }};

        gravity = new ModulePayload("gravity"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.silicon, 140,
                PMItems.tenelium, 110,
                Items.graphite, 40
            ));
            module = new ImpulseModule("gravity", ModuleSize.medium){{
                range = 180f;
                damage = 0.2f;
                powerUse = 2f;
                radius = 5f;
                laserWidth = 0.3f;
                maxTargets = 6;
                force = 30f;
                scaledForce = 22f;
            }};
        }};

        ambrosia = new ModulePayload("ambrosia"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.silicon, 80,
                Items.thorium, 60,
                Items.plastanium, 60
            ));
            module = new PowerTurretModule("ambrosia", ModuleSize.medium){
                {
                    reload = 12f * 60f;
                    range = 23f * tilesize;
                    inaccuracy = 5f;
                    velocityRnd = 0.1f;
                    powerUse = 4f;
                    playerControl = logicControl = false;

                    shootType = ModuleBullets.ambrosiaPotion;

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                public void setStats(Stats stats){
                    super.setStats(stats);

                    stats.remove(Stat.ammo);
                    stats.add(Stat.repairSpeed, ((HealFieldBulletType)(shootType.fragBullet)).healing * 60f, StatUnit.perSecond);
                }

                @Override
                protected boolean validateTarget(ModularTurretBuild parent, TurretMount mount){
                    Unit u = (Unit)mount.target;
                    return u != null && u.team == parent.team && u.damaged() && u.within(mount, range + u.hitSize / 2f);
                }

                @Override
                public void findTarget(ModularTurretBuild parent, BaseTurretMount mount){
                    mount.target = Units.closest(parent.team, mount.x, mount.y, range, u -> u.damaged() && u.checkTarget(targetGround, targetAir));
                }
            };
        }};

        vigilance = new ModulePayload("dispel"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.silicon, 140,
                PMItems.tenelium, 120,
                Items.phaseFabric, 50
            ));
            module = new PointDefenceModule("dispel", ModuleSize.medium){{
                powerUse = 5f;
                range = 200f;
                shootY = 0;
                reload = 8f;
                bulletDamage = 40f;
                pierceCap = 6;

                coolant = consumeCoolant(0.2f);
            }};
        }};
        //endregion

        //Region Large
        rebound = new ModulePayload("rebound"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 320,
                Items.graphite, 200,
                Items.plastanium, 200,
                Items.thorium, 170
            ));
            module = new ItemTurretModule("rebound", ModuleSize.large){
                {
                    ammo(
                        Items.titanium, ModuleBullets.reboundTitanium,
                        Items.surgeAlloy, ModuleBullets.reboundSurge
                    );

                    range = 21f * tilesize;
                    reload = 75f;
                    shootY = 1f / 4f;
                    recoil = 1;
                    topLayerOffset = 0.3f;
                    shootEffect = ModuleFx.reboundShoot;

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                public void handleItem(Item item, BaseMount mount){
                    BulletType type = ammoTypes.get(item);
                    if(type != null) ((TurretMount)mount).reloadCounter = 0f;

                    super.handleItem(item, mount);
                }

                @Override
                public void draw(ModularTurretBuild parent, BaseMount m){
                    TurretMount mount = (TurretMount)m;
                    super.draw(parent, mount);

                    if(hasAmmo(mount)){
                        BoomerangBulletType b = (BoomerangBulletType)peekAmmo(mount);
                        float r = mount.reloadCounter / reload,
                            width = b.width * r,
                            height = b.height * r,
                            spin = mount.rotation + Time.time * b.spin;
                        shootOffset.trns(mount.rotation, -mount.curRecoil + shootY);
                        Draw.z(b.layer);
                        Draw.color(b.backColor);
                        Draw.rect(b.backRegion, mount.x + shootOffset.x, mount.y + shootOffset.y, width, height, spin);
                        Draw.color(b.frontColor);
                        Draw.rect(b.frontRegion, mount.x + shootOffset.x, mount.y + shootOffset.y, width, height, spin);
                    }
                }
            };
        }};

        trifecta = new ModulePayload("trifecta"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 280,
                Items.graphite, 270,
                Items.plastanium, 180,
                PMItems.tenelium, 50
            ));
            module = new ItemTurretModule("trifecta", ModuleSize.large){
                {
                    ammo(
                        Items.blastCompound, ModuleBullets.trifectaMissile
                    );

                    range = 34f * tilesize;
                    reload = 120f;
                    maxAmmo = 12;
                    moveWhileShooting = false;
                    shootY -= 6f;
                    topLayerOffset = 0.30f;
                    shootSound = Sounds.artillery;

                    shoot = new ShootBarrel(){{
                        shots = 3;
                        shotDelay = 15f;
                        barrels = new float[]{
                            -6f, 0f, 0f,
                            0f, 0f, 0f,
                            6f, 0f, 0f
                        };
                    }};

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                public void createIcons(MultiPacker packer){
                    Outliner.outlineRegion(
                        packer,
                        Core.atlas.find("prog-mats-trifecta-missile"),
                        outlineColor,
                        "prog-mats-trifecta-missile-outline"
                    );
                    super.createIcons(packer);
                }
            };
        }};

        ares = new ModulePayload("ares"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 360,
                Items.lead, 320,
                PMItems.tenelium, 200,
                Items.surgeAlloy, 120,
                Items.silicon, 270
            ));
            module = new PowerTurretModule("ares", ModuleSize.large){{
                powerUse = 12f;
                range = 38f * tilesize;
                reload = 75f;
                shake = 3f;
                rotateSpeed = 3.5f;
                shootSound = Sounds.laser;
                heatColor = Color.red;

                shootType = ModuleBullets.aresOrb;

                coolant = consumeCoolant(0.2f);
            }};
        }};

        jupiter = new ModulePayload("jupiter"){{
            requirements(Category.turret, BuildVisibility.sandboxOnly, with(
                Items.copper, 290,
                Items.lead, 230,
                PMItems.tenelium, 180,
                Items.surgeAlloy, 80,
                Items.silicon, 230
            ));
            module = new PowerTurretModule("jupiter", ModuleSize.large){
                final float baseRad = 7.5f, jointRadMin = 9.5f, jointRadMax = 13f, endRadMin = 5f, endRadMax = 11.5f;
                TextureRegion jointBaseRegion, armBaseRegion, jointRegion, armRegion, endRegion, fullArm;

                {
                    reload = 2.5f * 60f;
                    range = 38f * 8f;
                    powerUse = 4f;
                    recoil = shootY = 0;
                    rotate = false;
                    shootSound = Sounds.laser;

                    shootType = ModuleBullets.jupiterOrb;

                    shoot.firstShotDelay = ModuleFx.jupiterCharge.lifetime;

                    coolant = consumeCoolant(0.2f);
                }

                @Override
                public void load(){
                    super.load();
                    jointBaseRegion = Core.atlas.find(name + "-joint-base");
                    armBaseRegion = Core.atlas.find(name + "-arm-base");
                    jointRegion = Core.atlas.find(name + "-joint");
                    armRegion = Core.atlas.find(name + "-arm");
                    endRegion = Core.atlas.find(name + "-end");
                    fullArm = Core.atlas.find(name + "-arm-full");
                }

                @Override
                public void updateCharging(ModularTurretBuild parent, TurretMount mount){
                    if(!charging(mount)){
                        mount.charge = Mathf.approachDelta(mount.charge, 0f, 3f);
                    }else{
                        mount.charge = Mathf.approachDelta(mount.charge, shoot.firstShotDelay, 1f);

                        if(mount.charge >= shoot.firstShotDelay){
                            mount.charge = shoot.firstShotDelay;
                        }
                    }
                }

                @Override
                public boolean shouldTurn(TurretMount mount){
                    return true;
                }

                @Override
                public boolean shouldReload(ModularTurretBuild parent, TurretMount mount){
                    return super.shouldReload(parent, mount) && mount.charge == 0;
                }

                @Override
                public void draw(ModularTurretBuild parent, BaseMount m){
                    TurretMount mount = (TurretMount)m;
                    float x = mount.x,
                        y = mount.y;

                    if(mount.progress < deployTime){
                        drawDeploy(parent, m);
                        return;
                    }

                    applyColor(parent, mount);
                    Draw.rect(region, x, y);

                    Lines.stroke(6f);
                    float charge = Interp.pow2Out.apply(mount.charge / shoot.firstShotDelay);
                    for(int i = 0; i < 4; i++){
                        float rot = i * 90 - 45f,
                            joint = Mathf.lerp(jointRadMin, jointRadMax, charge),
                            end = Mathf.lerp(endRadMin, endRadMax, charge);

                        shootOffset.trns(rot, baseRad);
                        recoilOffset.trns(rot, joint);

                        Draw.rect(jointBaseRegion, x + shootOffset.x, y + shootOffset.y, rot);
                        PMDrawf.stretch(armBaseRegion, x + shootOffset.x, y + shootOffset.y, x + recoilOffset.x, y + recoilOffset.y);
                        Draw.rect(jointRegion, x + recoilOffset.x, y + recoilOffset.y, rot);

                        shootOffset.trns(rot, end);

                        PMDrawf.stretch(armRegion, x + recoilOffset.x, y + recoilOffset.y, x + shootOffset.x, y + shootOffset.y);
                        Draw.rect(endRegion, x + shootOffset.x, y + shootOffset.y, rot);
                    }
                    Draw.mixcol();
                }

                @Override
                public void drawDeploy(ModularTurretBuild parent, BaseMount mount){
                    Draw.draw(Draw.z(), () -> {
                        float progress = mount.progress / deployTime;
                        PMDrawf.materialize(mount.x - elevation, mount.y - elevation, region, parent.team.color, 0f, 0.1f, progress, true);
                        PMDrawf.materialize(mount.x, mount.y, region, parent.team.color, 0f, 0.1f, progress);
                        for(int i = 0; i < 4; i++){
                            float rot = i * 90 - 45f;
                            PMDrawf.materialize(mount.x, mount.y, fullArm, parent.team.color, rot, 0.1f, progress);
                        }
                    });
                }

                @Override
                public void drawPayload(ModulePayloadBuild payload){
                    float x = payload.x,
                        y = payload.y;

                    Draw.rect(region, x, y);
                    for(int i = 0; i < 4; i++){
                        float rot = i * 90 - 45f;
                        Draw.rect(fullArm, x, y, rot);
                    }
                }
            };
        }};
        //endregion
    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
