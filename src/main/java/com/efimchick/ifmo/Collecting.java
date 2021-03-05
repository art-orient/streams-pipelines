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

    public long sum(IntStream stream) {
        long sum = stream.mapToLong(num -> num).sum();
        return sum;
    }

    public long production(IntStream stream) {
        long prod = stream.reduce(1, (a, b) -> a * b);
        return prod;
    }

    public long oddSum(IntStream stream) {
        long sum = stream.filter(s -> s % 2 != 0).mapToLong(num -> num).sum();
        return sum;
    }

    public Map<Integer, Integer> sumByRemainder(int i, IntStream stream) {
        Map<Integer, Integer> sum = stream.boxed()
                .collect(Collectors.groupingBy(s -> s % i, Collectors.summingInt(s -> s)));
        return sum;
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        long countTasks = courseResults.stream().flatMap(r -> r.getTaskResults().keySet().stream()).distinct().count();
        Map<Person, Double> total = courseResults.stream()
                .collect(Collectors.toMap(CourseResult::getPerson, r -> r.getTaskResults()
                .values().stream()
                .mapToInt(v -> v)
                .sum() / (double) countTasks));
        return total;
    }

    public Double averageTotalScore(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        long countPeople = courseResults.stream().map(CourseResult::getPerson).distinct().count();
        long countTasks = courseResults.stream().flatMap(r -> r.getTaskResults().keySet().stream()).distinct().count();
        Double average = courseResults.stream()
                        .map(CourseResult::getTaskResults)
                        .flatMapToDouble(r -> r.values().stream().mapToDouble(s -> s))
                        .sum() / (countPeople * countTasks);
        return average;
    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        long countPeople = courseResults.stream().map(CourseResult::getPerson).distinct().count();
        Map<String, Double> average = courseResults.stream()
                .flatMap(r -> r.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingDouble(e -> e.getValue() / (double) countPeople)));
        return average;
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        long countTasks = courseResults.stream().flatMap(r -> r.getTaskResults().keySet().stream()).distinct().count();
        Map<Person, String> defineMarks = courseResults.stream()
                .collect(Collectors.toMap(CourseResult::getPerson,
                    s -> {double score = s.getTaskResults().values().stream()
                        .mapToDouble(v -> v)
                        .sum() /  countTasks;
                    return score>90?"A":score>=83?"B":score>=75?"C":score>=68?"D":score>=60?"E":"F";
                }));
        return defineMarks;
    }

    public String easiestTask(Stream<CourseResult> stream) {
        List<CourseResult> courseResults = stream.collect(Collectors.toList());
        String task = courseResults.stream()
                .flatMap(r -> r.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nothing found.");
        return task;
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
                return builder.toString();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
        return collector;
    }
}
