package progressed.world.meta;

import mindustry.world.meta.*;

public class PMStat{
    public static final Stat
    mounts = new Stat("pm-mounts"),
    sentry = new Stat("pm-sentry"),
    sentryLifetime = new Stat("pm-sentry-lifetime"),

    fuel = new Stat("pm-fuel", StatCat.crafting),

    recipes = new Stat("pm-recipes"),
    producer = new Stat("pm-producer"),
    produce = new Stat("pm-produce"),
    used = new Stat("pm-used");
}
