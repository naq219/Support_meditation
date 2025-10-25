package naq.sm4.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MockData {

    private MockData() {
    }

    public static List<MeditationConfig> getMeditationConfigs() {
        List<MeditationConfig> configs = new ArrayList<>();
        configs.add(new MeditationConfig(
                "Thiền buổi sáng",
                30,
                Arrays.asList(
                        new MeditationStage("Khởi động", 5, 0, Arrays.asList("bell_start.mp3")),
                        new MeditationStage("Tập trung", 20, 5, Arrays.asList("chime.mp3", "rain.wav")),
                        new MeditationStage("Thư giãn", 5, 0, Arrays.asList("bell_end.mp3"))
                )));
        configs.add(new MeditationConfig(
                "Thiền tập trung",
                25,
                Arrays.asList(
                        new MeditationStage("Đặt tâm", 5, 0, Arrays.asList("bowl.wav")),
                        new MeditationStage("Quan sát", 15, 4, Arrays.asList("wind.mp3", "birds.wav")),
                        new MeditationStage("Kết thúc", 5, 0, Arrays.asList("bell_end.mp3"))
                )));
        configs.add(new MeditationConfig(
                "Thiền ngủ ngon",
                40,
                Arrays.asList(
                        new MeditationStage("Thả lỏng", 10, 0, Arrays.asList("soft_bell.mp3")),
                        new MeditationStage("Ngủ sâu", 25, 6, Arrays.asList("ocean.wav", "forest.mp3")),
                        new MeditationStage("Thức dậy", 5, 0, Arrays.asList("bell_end.mp3"))
                )));
        return configs;
    }

    public static List<String> getSoundFiles() {
        return Arrays.asList(
                "Tieng mưa nhẹ.mp3",
                "Tiếng suối rì rào.wav",
                "Tiếng chuông thiền.mp3",
                "Giai điệu bình yên.m4a",
                "Tiếng chuông gió.ogg",
                "Tiếng biển đêm.wav",
                "Chậu nước róc rách.mp3",
                "Tiếng đàn tranh.mp3",
                "Tiếng chim sớm mai.wav"
        );
    }

    public static List<MeditationStage> getTemplateStages() {
        return Arrays.asList(
                new MeditationStage("Khởi động", 5, 0, Arrays.asList("bell_start.mp3")),
                new MeditationStage("Thiền sâu", 15, 5, Arrays.asList("bowl.wav", "chime.mp3")),
                new MeditationStage("Thư giãn", 10, 0, Arrays.asList("rain.wav"))
        );
    }
}
