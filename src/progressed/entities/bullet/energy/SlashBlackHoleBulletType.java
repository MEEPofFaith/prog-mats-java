package progressed.entities.bullet.energy;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import blackhole.entities.bullet.*;
import blackhole.graphics.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import progressed.graphics.renders.*;

import static mindustry.Vars.*;

public class SlashBlackHoleBulletType extends BlackHoleBulletType{
    public float slashTime;
    public float slashOffsetStart, slashOffsetEnd;
    public float slashLength = 128f * tilesize;
    public float slashWidthFrom = tilesize, slashWidthTo;
    public Color slashColor;
    public Effect slashEffect = Fx.none;
    public Sound slashSound = Sounds.laserblast;
    public float slashSoundVolume = 2f;

    public SlashBlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        layer = BHLayer.end + 2f;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(slashColor == null) slashColor = starOut;
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(b.fdata == 0 && b.time > b.lifetime - slashTime){
            b.fdata = 1;
            slashEffect.at(b.x, b.y, 0f, BHDrawf.teamColor(b, slashColor), b);
            slashSound.at(b.x, b.y, 1f, slashSoundVolume);
        }
    }

    @Override
    public void draw(Bullet b){
        float fout = this.fout(b);
        BlackHoleRenderer.addBlackHole(b.x, b.y, this.horizonRadius * fout, this.lensingRadius * fout, BHDrawf.teamColor(b, this.color));

        if(b.time > b.lifetime - slashTime){
            float ang = Mathf.randomSeed(b.id, Mathf.PI2);
            float sfin = Interp.sineOut.apply(Mathf.curve(b.time, b.lifetime - slashTime, b.lifetime));
            float off = Mathf.lerp(slashOffsetStart, slashOffsetEnd, sfin) * fout;
            SlashRenderer.addSlash(b.x, b.y, ang, off);

            ang *= Mathf.radDeg;
            Tmp.v1.trns(ang, slashLength);
            Tmp.v2.trns(ang + 90f, Mathf.lerp(slashWidthFrom, slashWidthTo, sfin));

            Draw.color(BHDrawf.teamColor(b, slashColor));
            Fill.quad(
                b.x + Tmp.v1.x, b.y + Tmp.v1.y,
                b.x + Tmp.v2.x, b.y + Tmp.v2.y,
                b.x - Tmp.v1.x, b.y - Tmp.v1.y,
                b.x - Tmp.v2.x, b.y - Tmp.v2.y
            );
        }else if(this.starWidth > 0.0F){
            BlackHoleRenderer.addStar(b.x, b.y, this.starWidth * fout, this.starHeight * fout, this.starAngle, BHDrawf.teamColor(b, this.starIn), BHDrawf.teamColor(b, this.starOut));
        }
    }
}
