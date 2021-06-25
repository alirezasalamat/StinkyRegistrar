package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student student, List<Offering> offerings) throws EnrollmentRulesViolationException {
        CheckForAlreadyPassedCourses(student, offerings);
        checkForPrerequisites(student, offerings);
        checkForExamTimeConflict(offerings);
        checkForDuplicateTakenCourse(offerings);
        checkForGPALimit(student, offerings);
        offerings.forEach(student::takeCourse);
	}

    private void checkForDuplicateTakenCourse(List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            for (Offering o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getCourse().equals(o2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
            }
        }
    }

    private void checkForExamTimeConflict(List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            for (Offering o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getExamTime().equals(o2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
            }
		}
    }

    public void checkForPrerequisites(Student s, List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            for (Course pre : o.getCourse().getPrerequisites()) {
                if (!s.hasPassed(pre)) {
                    throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
                }
            }
        }
    }

    public void CheckForAlreadyPassedCourses(Student s, List<Offering> courses) throws EnrollmentRulesViolationException {
        for (Offering o : courses) {
            if(s.hasPassed(o.getCourse())){
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
            }
        }
    }

    public void checkForGPALimit(Student s, List<Offering> offerings) throws EnrollmentRulesViolationException {
        int unitsRequested = offerings.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((s.getGPA() < 12 && unitsRequested > 14) ||
				(s.getGPA() < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, s.getGPA()));
    }


}
