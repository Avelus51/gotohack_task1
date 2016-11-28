import gnu.trove.map.hash.TIntIntHashMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    enum StructureIndices {
        COURSE_ID,
        MODULE_ID,
        MODULE_POSITION,
        LESSON_ID,
        LESSON_POSITION,
        STEP_ID,
        STEP_POSITION,
        STEP_TYPE,
        STEP_COST
    }

    enum EventIndices {
        USER_ID,
        ACTION,
        STEP_ID,
        TIME
    }

    private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    //TODO: O(n)
    public static void main(String[] args) throws Exception {
        Path structurePath = Paths.get("res/course-217-structure.csv");
        Scanner scanner = new Scanner(structurePath);
        TIntIntHashMap stepsWithNonZeroCost = new TIntIntHashMap();
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String[] structureValues = scanner.nextLine().split(",");
            int stepCost = Integer.parseInt(structureValues[StructureIndices.STEP_COST.ordinal()]);
            if (stepCost > 0) {
                int stepId = Integer.parseInt(structureValues[StructureIndices.STEP_ID.ordinal()]);
                stepsWithNonZeroCost.put(stepId, stepCost);
            }
        }
        Path eventsPath = Paths.get("res/course-217-events.csv");
        scanner = new Scanner(eventsPath);
        scanner.nextLine();
        List<String[]> events = new ArrayList<>();
        while (scanner.hasNextLine()) {
            events.add(scanner.nextLine().split(","));
        }
        TIntIntHashMap userScores = new TIntIntHashMap();
        Map<Integer, Long> trainingDurations = new HashMap<>();
        List<Integer> usersWhoCompletedTheCourse = new ArrayList<>();
        events.stream()
                .sorted((e1, e2) -> {
                    int time1 = Integer.parseInt(e1[EventIndices.TIME.ordinal()]);
                    int time2 = Integer.parseInt(e2[EventIndices.TIME.ordinal()]);
                    return Integer.compare(time1, time2);
                })
                .forEach(e -> {
                    int userId = Integer.parseInt(e[EventIndices.USER_ID.ordinal()]);
                    int stepId = Integer.parseInt(e[EventIndices.STEP_ID.ordinal()]);
                    long time = Long.parseLong(e[EventIndices.TIME.ordinal()]);
                    if (!trainingDurations.containsKey(userId)) {
                        trainingDurations.put(userId, time);
                    }
                    if (e[EventIndices.ACTION.ordinal()].equals("passed") && stepsWithNonZeroCost.containsKey(stepId)) {
                        int stepCost = stepsWithNonZeroCost.get(stepId);
                        userScores.adjustOrPutValue(userId, stepCost, stepCost);
                    }
                    if (userScores.get(userId) > 23 && !usersWhoCompletedTheCourse.contains(userId)) {
                        usersWhoCompletedTheCourse.add(userId);
                        long startTime = trainingDurations.get(userId);
                        trainingDurations.put(userId, time - startTime);
                    }
                });
        List<Long> elapsedTimes = new ArrayList<>();
        for (long time : trainingDurations.values()) {
            if (time > 0) {
                elapsedTimes.add(time);
            }
        }
        Collections.sort(elapsedTimes);

        System.out.println(elapsedTimes.stream()
                .flatMap(time -> getKeysByValue(trainingDurations, time).stream())
                .limit(10)
                .map(Object::toString)
                .collect(Collectors.joining(",")));
    }
}
