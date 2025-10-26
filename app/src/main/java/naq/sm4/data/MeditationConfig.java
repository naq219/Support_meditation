package naq.sm4.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable value object describing a meditation session configuration made up of one or more
 * {@link MeditationStage} entries. The configuration is referenced throughout the app when
 * scheduling or editing sessions.
 */
public class MeditationConfig {

    private final String name;
    private final int totalMinutes;
    private final List<MeditationStage> stages;

    /**
     * Creates a new configuration with the supplied metadata and stage list.
     *
     * @param name          user facing name of the configuration
     * @param totalMinutes  total duration in minutes across all stages
     * @param stages        ordered stages that compose the session
     */
    public MeditationConfig(String name, int totalMinutes, List<MeditationStage> stages) {
        this.name = name;
        this.totalMinutes = totalMinutes;
        this.stages = stages == null ? Collections.emptyList() : Collections.unmodifiableList(stages);
    }

    /**
     * @return display name chosen by the user.
     */
    public String getName() {
        return name;
    }

    /**
     * @return total duration in minutes, cached when the config was saved.
     */
    public int getTotalMinutes() {
        return totalMinutes;
    }

    /**
     * @return immutable list of stages in execution order.
     */
    public List<MeditationStage> getStages() {
        return stages;
    }

    /**
     * @return number of stages contained within the configuration.
     */
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
