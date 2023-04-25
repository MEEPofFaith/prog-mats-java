package progressed.entities.bullet.energy;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import progressed.entities.*;
import progressed.graphics.*;

public class BeamBulletType extends ContinuousBulletType{
    public float growTime = 16f, fadeTime = 16f;
    public float length = 160f;
    public float width = 9f, oscScl = 0.8f, oscMag = 1.5f;
    public Interp lengthInterp = Interp.pow3In;
    public Color laserColor;

    public String beamSprite;
    public TextureRegion beam, beamStart, beamEnd;

    public BeamBulletType(float damage, String beamSprite){
        super();
        this.damage = damage;
        this.beamSprite = beamSprite;
    }

    {
        lifetime = 32f;
        optimalLifeFract = 0.5f;
        laserAbsorb = false; //Apparently laser and pierce aren't applied at the same time.
        pierceCap = 4;
    }

    @Override
    public void load(){
        super.load();

        beam = Core.atlas.find(beamSprite);
        beamStart = Core.atlas.find(beamSprite + "-start");
        beamEnd = Core.atlas.find(beamSprite + "-end");
    }

    @Override
    public void draw(Bullet b){
        float lenScl = Mathf.clamp(b.time < growTime ? lengthInterp.apply(b.time / growTime) : 1f);
        float fout = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);
        float realLength = (pierceCap <= 0 ? length : PMDamage.findPierceLength(b, pierceCap, length * lenScl));
        Color c = laserColor == null ? b.team.color : laserColor;
        Tmp.v1.trns(b.rotation(), realLength * 1.1f);
        Draw.color(c);
        PMDrawf.laser(beam, beamStart, beamEnd, b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, (width + Mathf.absin(Time.time, oscScl, oscMag)) * fout / 12f);
        Draw.color();
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.4f * b.fout(), c, 0.6f);
    }

    @Override
    public void drawLight(Bullet b){
        //no light drawn here
    }

    @Override
    public float currentLength(Bullet b){
        float lenScl = Mathf.clamp(b.time < growTime ? lengthInterp.apply(b.time / growTime) : 1f);
        return length * lenScl;
    }
}
