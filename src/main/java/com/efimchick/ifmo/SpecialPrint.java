package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpecialPrint {
    private static final String STUDENT = "Student";
    private static final String TOTAL = "Total";
    private static final String MARK = "Mark";
    private static final String AVERAGE = "Average";
    private static final String SEPARATOR = " | ";
    private static final String END = " |";
    private int wightFirstColumn = STUDENT.length() + 1;
    List<CourseResult> courseResults = new ArrayList<>();
    List<String> taskNames = new ArrayList<>();
    List<String> students = new ArrayList<>();
    Collecting collecting = new Collecting();

    public StringBuilder buildResult(StringBuilder builder) {
        buildHeader(builder);
        buildStudentRows(builder);
        buildLastRow(builder);
        return builder;
    }

    public StringBuilder buildHeader(StringBuilder builder) {
        students = getStudents(courseResults);
        wightFirstColumn = getLengthName(students);
        builder.append(String.format("%-" + wightFirstColumn + "s", STUDENT) + SEPARATOR);
        taskNames = getTaskNames(courseResults);
        builder.append(taskNames.stream().collect(Collectors.joining(SEPARATOR)))
                .append(SEPARATOR + TOTAL + SEPARATOR + MARK + END)
                .append("\n");
        return builder;
    }

    public StringBuilder buildStudentRows(StringBuilder builder) {
        Map<Person, Double> totalScores = collecting.totalScores(courseResults.stream());
        Map<Person, String> marks = collecting.defineMarks(courseResults.stream());
        builder.append(students.stream()
            .map(s -> { StringBuilder results = new StringBuilder()
                .append(String.format("%-" + wightFirstColumn + "s" + SEPARATOR, s))
                .append(taskNames.stream()
                        .map(t -> String.format("%" + t.length() + "s", findTaskResult(s, t)))
                        .collect(Collectors.joining(SEPARATOR))).append(SEPARATOR)
                .append(String.format("%.2f", getTotalOfStudent(s, totalScores)).replace(",", ".") + SEPARATOR)
                .append("   " + getMarkOfStudent(s, marks))
                .append(END);
                return results.toString();
                })
            .collect(Collectors.joining("\n"))).append("\n");
        return builder;
    }

    public StringBuilder buildLastRow(StringBuilder builder) {
        Map<String, Double> averageScores = collecting.averageScoresPerTask(courseResults.stream());
        double sum = collecting.averageTotalScore(courseResults.stream());
        builder.append(String.format("%-" + wightFirstColumn + "s", AVERAGE) + SEPARATOR)
                .append(taskNames.stream()
                    .map(s -> String.format("%" + s.length() + ".2f", averageScores.get(s)).replace(",", "."))
                    .collect(Collectors.joining(SEPARATOR)))
                .append(SEPARATOR)
                .append(String.format("%.2f", sum).replace(",", ".") + SEPARATOR)
                .append("   " + (sum > 90? "A": sum>=83?"B":sum>=75?"C":sum>=68?"D":sum>=60?"E":"F"))
                .append(END);
        return builder;
    }

    private List<String> getTaskNames(List<CourseResult> courseResults) {
        List<String> taskNames = courseResults.stream()
                .flatMap(r -> r.getTaskResults().keySet().stream())
                .distinct().sorted()
                .collect(Collectors.toList());
        return taskNames;
    }

    private List<String> getStudents(List<CourseResult> courseResults) {
        List<String> students = courseResults.stream()
                .map(CourseResult::getPerson)
                .distinct().sorted(Comparator.comparing(Person::getLastName))
                .map(s -> s.getLastName() + " " + s.getFirstName())
                .collect(Collectors.toList());
        return students;
    }

    private int getLengthName (List<String> students) {
        return Math.max(wightFirstColumn, students.stream().mapToInt(s -> s.length())
                .max().orElse(wightFirstColumn));
    }

    public void addCourseResult(CourseResult courseResult) {
        courseResults.add(courseResult);
    }

    private int findTaskResult(String student, String taskName) {
        return courseResults.stream()
                    .filter(s -> (s.getPerson().getLastName() + " " + s.getPerson().getFirstName()).equals(student))
                    .flatMap(s -> s.getTaskResults().entrySet().stream())
                    .filter(t -> t.getKey().equals(taskName))
                    .mapToInt(Map.Entry::getValue)
                    .findAny().orElse(0);
    }

    private double getTotalOfStudent(String student, Map<Person, Double> totalscores) {
        return totalscores.entrySet().stream()
                .filter(s -> (s.getKey().getLastName() + " " + s.getKey().getFirstName()).equals(student))
                .mapToDouble(Map.Entry::getValue).findAny().orElse(0);
    }

    private String getMarkOfStudent(String student, Map<Person, String> marks) {
        return marks.entrySet().stream()
                .filter(s -> (s.getKey().getLastName() + " " + s.getKey().getFirstName()).equals(student))
                .map(Map.Entry::getValue).findAny().orElse("F");
    }
}
