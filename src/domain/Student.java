package domain;
import domain.exceptions.EnrollmentRulesViolationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {
	private String id;
	private String name;
	private Map<Term, Map<Course, Double>> transcript;
	private List<Offering> currentTerm;

	public Student(String id, String name) {
		this.id = id;
		this.name = name;
		this.transcript = new HashMap<>();
		this.currentTerm = new ArrayList<>();
	}
	
	public void takeCourse(Offering cse) {
		currentTerm.add(cse);
	}

	public Map<Term, Map<Course, Double>> getTranscript() {
		return transcript;
	}

	public void addTranscriptRecord(Course course, Term term, double grade) {
	    if (!transcript.containsKey(term))
	        transcript.put(term, new HashMap<>());
	    transcript.get(term).put(course, grade);
    }

	public double getGPA() {
		double points = 0;
		int totalUnits = 0;
		for (Map.Entry<Term, Map<Course, Double>> termTranscript : transcript.entrySet()) {
			for (Map.Entry<Course, Double> gradedCourse : termTranscript.getValue().entrySet()) {
				points += gradedCourse.getValue() * gradedCourse.getKey().getUnits();
				totalUnits += gradedCourse.getKey().getUnits();
			}
		}
		return points / totalUnits;
	}

    public List<Offering> getCurrentTerm() {
        return currentTerm;
    }

    public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}

	public boolean hasPassed(Course course) {
		for (Map.Entry<Term, Map<Course, Double>> termTranscript : transcript.entrySet()) {
			for (Map.Entry<Course, Double> gradedCourse : termTranscript.getValue().entrySet()) {
				if (gradedCourse.getKey().equals(course) && gradedCourse.getValue() >= 10)
					return true;
			}
		}
		return false;
	}
}
