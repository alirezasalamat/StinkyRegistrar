package domain;

import domain.exceptions.EnrollmentRulesViolationException;

import java.util.ArrayList;
import java.util.List;

public class EnrollCtrl {
	public List<Exception> enroll(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
	    List<Exception> violations = new ArrayList<>();

        violations.addAll(checkForPrerequisites(student, offerings));
        violations.addAll(checkForAlreadyPassedCourses(student, offerings));
        violations.addAll(checkForExamTimeConflict(offerings));
        violations.addAll(checkForDuplicateTakenCourse(offerings));
        violations.addAll(checkForGPALimit(student, offerings));

        offerings.forEach(student::takeCourse);
        return violations;
	}

    private List<Exception> checkForDuplicateTakenCourse(List<Offering> offerings) throws EnrollmentRulesViolationException {
        List<Exception> violations = new ArrayList<>();
        for (Offering offering1 : offerings) {
            for (Offering offering2 : offerings) {
                if (offering1 == offering2)
                    continue;
                if (offering1.getCourse().equals(offering2.getCourse()))
                    violations.add(new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", offering1.getCourse().getName())));
            }
        }

        return violations;
    }

    private List<Exception> checkForExamTimeConflict(List<Offering> offerings) throws EnrollmentRulesViolationException {
        List<Exception> violations = new ArrayList<>();
	    for (Offering offering1 : offerings) {
            for (Offering offering2 : offerings) {
                if (offering1 == offering2)
                    continue;
                if (offering1.getExamTime().equals(offering2.getExamTime()))
                    violations.add(new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", offering1, offering2)));
            }
		}

        return violations;
    }

    public List<Exception> checkForPrerequisites(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        List<Exception> violations = new ArrayList<>();
	    for (Offering offering : offerings) {
            for (Course pre : offering.getCourse().getPrerequisites()) {
                if (!student.hasPassed(pre)) {
                    violations.add(new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), offering.getCourse().getName())));
                }
            }
        }

        return violations;
    }

    public List<Exception> checkForAlreadyPassedCourses(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        List<Exception> violations = new ArrayList<>();
	    for (Offering offering : offerings) {
            if(student.hasPassed(offering.getCourse())){
                violations.add(new EnrollmentRulesViolationException(String.format("The student has already passed %s", offering.getCourse().getName())));
            }
        }

        return violations;
    }

    public List<Exception> checkForGPALimit(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        List<Exception> violations = new ArrayList<>();
	    int unitsRequested = offerings.stream().mapToInt(offering -> offering.getCourse().getUnits()).sum();
        if ((student.getGPA() < 12 && unitsRequested > 14) ||
				(student.getGPA() < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			violations.add(new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, student.getGPA())));

        return violations;
	}
}
