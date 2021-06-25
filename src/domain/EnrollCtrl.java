package domain;

import domain.exceptions.EnrollmentRulesViolationException;

import java.util.List;

public class EnrollCtrl {
	public void enroll(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        CheckForAlreadyPassedCourses(student, offerings);
        checkForPrerequisites(student, offerings);
        checkForExamTimeConflict(offerings);
        checkForDuplicateTakenCourse(offerings);
        checkForGPALimit(student, offerings);
        offerings.forEach(student::takeCourse);
	}

    private void checkForDuplicateTakenCourse(List<Offering> offerings) throws EnrollmentRulesViolationException {
        for (Offering offering1 : offerings) {
            for (Offering offering2 : offerings) {
                if (offering1 == offering2)
                    continue;
                if (offering1.getCourse().equals(offering2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", offering1.getCourse().getName()));
            }
        }
    }

    private void checkForExamTimeConflict(List<Offering> offerings) throws EnrollmentRulesViolationException {
        for (Offering offering1 : offerings) {
            for (Offering offering2 : offerings) {
                if (offering1 == offering2)
                    continue;
                if (offering1.getExamTime().equals(offering2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", offering1, offering2));
            }
		}
    }

    public void checkForPrerequisites(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        for (Offering offering : offerings) {
            for (Course pre : offering.getCourse().getPrerequisites()) {
                if (!student.hasPassed(pre)) {
                    throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), offering.getCourse().getName()));
                }
            }
        }
    }

    public void CheckForAlreadyPassedCourses(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        for (Offering offering : offerings) {
            if(student.hasPassed(offering.getCourse())){
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", offering.getCourse().getName()));
            }
        }
    }

    public void checkForGPALimit(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        int unitsRequested = offerings.stream().mapToInt(offering -> offering.getCourse().getUnits()).sum();
        if ((student.getGPA() < 12 && unitsRequested > 14) ||
				(student.getGPA() < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, student.getGPA()));
    }
}
