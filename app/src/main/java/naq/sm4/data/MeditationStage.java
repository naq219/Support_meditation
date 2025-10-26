package naq.sm4.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable description for a single stage within a meditation configuration. A stage controls
 * timing, repetition cadence, and the list of audio cues played during the stage.
 */
public class MeditationStage {

    private final String name;
    private final int minutes;
    private final int repeatMinutes;
    private final List<String> sounds;

    /**
     * Builds a stage definition.
     *
     * @param name          label presented to the user
     * @param minutes       duration of the stage in minutes
     * @param repeatMinutes interval between repeated sound cues (0 = no repeat)
     * @param sounds        audio file names to play when the stage begins or repeats
     */
    public MeditationStage(String name, int minutes, int repeatMinutes, List<String> sounds) {
        this.name = name;
        this.minutes = minutes;
        this.repeatMinutes = repeatMinutes;
        this.sounds = sounds == null ? Collections.emptyList() : Collections.unmodifiableList(sounds);
    }

    /**
     * @return user facing name for the stage.
     */
    public String getName() {
        return name;
    }

    /**
     * @return duration in minutes that the stage lasts.
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * @return repeat interval in minutes, or 0 when sounds should not repeat.
     */
    public int getRepeatMinutes() {
        return repeatMinutes;
    }

    /**
     * @return immutable list of sound file names associated with this stage.
     */
    public List<String> getSounds() {
        return sounds;
    }

    /**
     * @return {@code true} if a repeat interval greater than zero has been set.
     */
    public boolean hasRepeat() {
        return repeatMinutes > 0;
    }

    /**
     * @return human readable description of the repeat cadence for UI display.
     */
    public String getRepeatDescription() {
        if (repeatMinutes <= 0) {
            return "Chỉ phát 1 lần";
        }
        return "Lặp mỗi " + repeatMinutes + " phút";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeditationStage)) {
            return false;
        }
        MeditationStage that = (MeditationStage) o;
        return minutes == that.minutes
                && repeatMinutes == that.repeatMinutes
                && Objects.equals(name, that.name)
                && Objects.equals(sounds, that.sounds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, minutes, repeatMinutes, sounds);
    }
}
