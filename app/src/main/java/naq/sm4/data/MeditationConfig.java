package naq.sm4.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MeditationConfig {

    private final String name;
    private final int totalMinutes;
    private final List<MeditationStage> stages;

    public MeditationConfig(String name, int totalMinutes, List<MeditationStage> stages) {
        this.name = name;
        this.totalMinutes = totalMinutes;
        this.stages = stages == null ? Collections.emptyList() : Collections.unmodifiableList(stages);
    }

    public String getName() {
        return name;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public List<MeditationStage> getStages() {
        return stages;
    }

    public int getStageCount() {
        return stages.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeditationConfig)) {
            return false;
        }
        MeditationConfig that = (MeditationConfig) o;
        return totalMinutes == that.totalMinutes
                && Objects.equals(name, that.name)
                && Objects.equals(stages, that.stages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, totalMinutes, stages);
    }
}
