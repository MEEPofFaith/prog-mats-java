package progressed.entities.bullet.energy;

import arc.audio.*;
import arc.graphics.*;
import arc.math.*;
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
        BlackHoleRenderer.addBlackHole(
            b.x, b.y,
            this.horizonRadius * fout, this.lensingRadius * fout,
            BHDrawf.teamColor(b, this.color)
        );

        if(b.time > b.lifetime - slashTime){
            float ang = Mathf.randomSeed(b.id, Mathf.PI2);
            float sfin = Interp.sineOut.apply(Mathf.curve(b.time, b.lifetime - slashTime, b.lifetime));
            float off = Mathf.lerp(slashOffsetStart, slashOffsetEnd, sfin) * fout;
            SlashRenderer.addSlash(
                b.x, b.y, ang, off,
                slashLength * fout, Mathf.lerp(slashWidthFrom, slashWidthTo, sfin) * fout,
                BHDrawf.teamColor(b, slashColor).toFloatBits()
            );
        }else if(this.starWidth > 0.0F){
            BlackHoleRenderer.addStar(
                b.x, b.y,
                this.starWidth * fout, this.starHeight * fout, this.starAngle,
                BHDrawf.teamColor(b, this.starIn), BHDrawf.teamColor(b, this.starOut)
            );
        }
    }
}
