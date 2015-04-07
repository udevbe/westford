package org.westmalle.wayland.output;


import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OutputGeometry {

    public static Builder builder() {
        return new AutoValue_OutputGeometry.Builder();
    }

    /**
     *
     * @return x position within the global compositor space
     */
    public abstract int getX();

    /**
     *
     * @return y position within the global compositor space
     */
    public abstract int getY();

    /**
     *
     * @return width in millimeters of the output
     */
    public abstract int getPhysicalWidth();

    /**
     *
     * @return height in millimeters of the output
     */
    public abstract int getPhysicalHeight();

    /**
     *
     * @return subpixel orientation of the output
     */
    public abstract int getSubpixel();

    /**
     *
     * @return textual description of the manufacturer
     */
    public abstract String getMake();

    /**
     *
     * @return textual description of the model
     */
    public abstract String getModel();

    /**
     *
     * @return transform that maps framebuffer to output
     */
    public abstract int getTransform();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public interface Builder{
        Builder x(int x);
        Builder y(int y);
        Builder physicalWidth(int width);
        Builder physicalHeight(int height);
        Builder subpixel(int subpixel);
        Builder make(String make);
        Builder model(String model);
        Builder transform(int transform);
        OutputGeometry build();
    }
}
