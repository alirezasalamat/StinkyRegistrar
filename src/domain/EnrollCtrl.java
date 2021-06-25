package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
	public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();
        CheckForAlreadyPassedCourses(s, courses);
        checkForPrerequisites(s, courses);
        for (CSE o : courses) {
            for (CSE o2 : courses) {
                if (o == o2)
                    continue;
                if (o.getExamTime().equals(o2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time", o, o2));
                if (o.getCourse().equals(o2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice", o.getCourse().getName()));
            }
		}
        checkForGPALimit(s, courses);
        for (CSE o : courses)
			s.takeCourse(o);
	}

    public void checkForPrerequisites(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            for (Course pre : o.getCourse().getPrerequisites()) {
                if (!s.hasPassed(pre)) {
                    throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s", pre.getName(), o.getCourse().getName()));
                }
            }
        }
    }

    public void CheckForAlreadyPassedCourses(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE o : courses) {
            if(s.hasPassed(o.getCourse())){
                throw new EnrollmentRulesViolationException(String.format("The student has already passed %s", o.getCourse().getName()));
            }
        }
    }

    public void checkForGPALimit(Student s, List<CSE> offerings) throws EnrollmentRulesViolationException {
        int unitsRequested = offerings.stream().mapToInt(o -> o.getCourse().getUnits()).sum();
        if ((s.getGPA() < 12 && unitsRequested > 14) ||
				(s.getGPA() < 16 && unitsRequested > 16) ||
				(unitsRequested > 20))
			throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f", unitsRequested, s.getGPA()));
    }


}
