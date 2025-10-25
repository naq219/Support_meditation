package naq.sm4.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MeditationStage {

    private final String name;
    private final int minutes;
    private final int repeatMinutes;
    private final List<String> sounds;

    public MeditationStage(String name, int minutes, int repeatMinutes, List<String> sounds) {
        this.name = name;
        this.minutes = minutes;
        this.repeatMinutes = repeatMinutes;
        this.sounds = sounds == null ? Collections.emptyList() : Collections.unmodifiableList(sounds);
    }

    public String getName() {
        return name;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getRepeatMinutes() {
        return repeatMinutes;
    }

    public List<String> getSounds() {
        return sounds;
    }

    public boolean hasRepeat() {
        return repeatMinutes > 0;
    }

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
