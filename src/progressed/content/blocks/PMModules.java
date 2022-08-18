package progressed.content.blocks;

public class PMModules{
    public static float maxClip = 0;

    public static void load(){

    }

    public static void setClip(float newClip){
        maxClip = Math.max(maxClip, newClip);
    }
}
