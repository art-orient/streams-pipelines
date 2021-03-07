package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Collecting {
    protected static Map<Double, String> scoreAndMarks;

    static {
        scoreAndMarks = new LinkedHashMap<>();
        scoreAndMarks.put(90.01, "A");
        scoreAndMarks.put(83.0, "B");
        scoreAndMarks.put(75.0, "C");
        scoreAndMarks.put(68.0, "D");
        scoreAndMarks.put(60.0, "E");
        scoreAndMarks.put(0.0, "F");
    }

    public long sum(IntStream stream) {
        return stream.mapToLong(num -> num).sum();
    }

    public long production(IntStream stream) {
        return stream.reduce(1, (a, b) -> a * b);
    }

    public long oddSum(IntStream stream) {
        return stream.filter(s -> s % 2 != 0).mapToLong(num -> num).sum();
    }

    public Map<Integer, Integer> sumByRemainder(int i, IntStream stream) {
        return stream.boxed()
                .collect(Collectors.groupingBy(s -> s % i, Collectors.summingInt(s -> s)));
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        return courseResults.stream()
                .collect(Collectors.toMap(CourseResult::getPerson, r -> r.getTaskResults()
                .values().stream()
                .mapToInt(v -> v)
                .sum() / (double) getCountTasks(courseResults)));
    }

    private long getCountTasks (List<CourseResult> courseResults) {
        return courseResults.stream()
                .flatMap(r -> r.getTaskResults()
                .keySet().stream())
                .distinct().count();
    }

    private long getCountPeople (List<CourseResult> courseResults) {
        return courseResults.stream()
                .map(CourseResult::getPerson)
                .distinct().count();
    }

    public Double averageTotalScore(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        return courseResults.stream()
                .map(CourseResult::getTaskResults)
                .flatMapToDouble(r -> r.values().stream().mapToDouble(s -> s))
                .sum() / (getCountPeople(courseResults) * getCountTasks(courseResults));
    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        return courseResults.stream()
                .flatMap(r -> r.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                    Collectors.summingDouble(e -> e.getValue() / (double) getCountPeople(courseResults))));
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        return courseResults.stream()
                    .collect(Collectors.toMap(CourseResult::getPerson,
                        s -> {double score = s.getTaskResults().values().stream()
                            .mapToDouble(v -> v)
                            .sum() / getCountTasks(courseResults);
                            return scoreAndMarks.entrySet().stream()
                                .filter(v -> v.getKey() <= score)
                                .map(v -> v.getValue())
                                .findFirst().orElse("F");}));
    }

    public String easiestTask(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        return courseResults.stream()
                .flatMap(r -> r.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nothing found.");
    }

    public Collector<CourseResult, SpecialPrint, String> printableStringCollector() {
        Collector collector =new Collector() {
            @Override
            public Supplier supplier() {
                return SpecialPrint::new;
            }

            @Override
            public BiConsumer<SpecialPrint, CourseResult> accumulator() {
                return SpecialPrint::addCourseResult;
            }

            @Override
            public BinaryOperator combiner() {
                return null;
            }

            @Override
            public Function<SpecialPrint, String> finisher() {
                return specialPrint -> {StringBuilder builder = new StringBuilder();
                            specialPrint.buildResult(builder);
                            return builder.toString();};
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
        return collector;
    }
}
