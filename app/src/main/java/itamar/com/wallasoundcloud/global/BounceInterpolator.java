package itamar.com.wallasoundcloud.global;


public class BounceInterpolator implements android.view.animation.Interpolator {
    /**
     * The amplitude of the bounces. The higher value (10, for example) produces more pronounced bounces.
     * The lower values (0.1, for example) produce less noticeable wobbles.
     */
    private double mAmplitude = 1;

    /**
     * The frequency of the bounces. The higher value produces more wobbles during the animation time period.
     */
    private double mFrequency = 10;

    /**
     * Initialize a new interpolator.
     *
     * @param amplitude The amplitude of the bounces. The higher value produces more pronounced bounces. The lower values (0.1, for example) produce less noticeable wobbles.
     * @param frequency The frequency of the bounces. The higher value produces more wobbles during the animation time period.
     */
    BounceInterpolator(double amplitude, double frequency) {
        this.mAmplitude = amplitude;
        this.mFrequency = frequency;
    }
    public BounceInterpolator() {
        this(0.1, 10);
    }

    @Override
    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) * Math.cos(mFrequency * time) + 1);
    }
}