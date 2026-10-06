package pt.up.fe.els2026.animation;

public class TranslationMovement extends Movement {
    float targetX;
    float targetY;

    public TranslationMovement(float duration, float targetX,float targetY){
        super(duration);
        this.targetX;
        this.targetY=targetY;
    }
}
