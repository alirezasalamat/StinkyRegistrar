package domain;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.*;

import domain.exceptions.EnrollmentRulesViolationException;
import org.junit.Before;
import org.junit.Test;

public class EnrollCtrlTest {
	private Student bebe;
	private Course prog;
	private Course ap;
	private Course dm;
	private Course math1;
	private Course math2;
	private Course phys1;
	private Course phys2;
	private Course maaref;
	private Course farsi;
	private Course english;
	private Course akhlagh;
	private Course economy;
	private Course karafarini;

	@Before
	public void setup() {
		math1 = new Course("4", "MATH1", 3);
		phys1 = new Course("8", "PHYS1", 3);
		prog = new Course("7", "PROG", 4);
		math2 = new Course("6", "MATH2", 3).withPre(math1);
		phys2 = new Course("9", "PHYS2", 3).withPre(math1, phys1);
		ap = new Course("2", "AP", 3).withPre(prog);
		dm = new Course("3", "DM", 3).withPre(prog);
		economy = new Course("1", "ECO", 3);
		maaref = new Course("5", "MAAREF", 2);
		farsi = new Course("12", "FA", 2);
		english = new Course("10", "EN", 2);
		akhlagh = new Course("11", "AKHLAGH", 2);
		karafarini = new Course("13", "KAR", 3);

		bebe = new Student("1", "Bebe");
	}

	private ArrayList<Offering> requestedOfferings(Course...courses) {
		Calendar cal = Calendar.getInstance();
		ArrayList<Offering> result = new ArrayList<>();
		for (Course course : courses) {
			cal.add(Calendar.DATE, 1);
			result.add(new Offering(course, cal.getTime()));
		}
		return result;
	}

	private boolean hasTaken(Student s, Course...courses) {
	    Set<Course> coursesTaken = new HashSet<>();
		for (Offering cs : s.getCurrentTerm())
				coursesTaken.add(cs.getCourse());
		for (Course course : courses) {
			if (!coursesTaken.contains(course))
				return false;
		}
		return true;
	}

	@Test
	public void canTakeBasicCoursesInFirstTerm() {
		new EnrollCtrl().enroll(bebe, requestedOfferings(math1, phys1, prog));
		assertTrue(hasTaken(bebe, math1, phys1, prog));
	}

	@Test
	public void canTakeNoOfferings() {
		new EnrollCtrl().enroll(bebe, new ArrayList<>());
		assertTrue(hasTaken(bebe));
	}

	@Test
	public void cannotTakeWithoutPreTaken() {
		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(math2, phys1, prog));
		assertEquals(violations.size(), 1);
		assertTrue(violations.get(0) instanceof EnrollmentRulesViolationException);
	}

	@Test
	public void cannotTakeWithoutPrePassed() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 18);
		bebe.addTranscriptRecord(prog, new Term("t1"), 12);
		bebe.addTranscriptRecord(math1, new Term("t1"), 8.4);
		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(math2, ap));

		assertEquals(violations.size(), 1);
		assertTrue(violations.get(0) instanceof EnrollmentRulesViolationException);
	}

	@Test
	public void canTakeWithPreFinallyPassed() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 18);
		bebe.addTranscriptRecord(prog, new Term("t1"), 12);
		bebe.addTranscriptRecord(math1, new Term("t1"), 8.4);

		bebe.addTranscriptRecord(phys2, new Term("t2"), 10);
		bebe.addTranscriptRecord(ap, new Term("t2"), 16);
		bebe.addTranscriptRecord(math1, new Term("t2"), 10.5);

		new EnrollCtrl().enroll(bebe, requestedOfferings(math2, dm));
		assertTrue(hasTaken(bebe, math2, dm));
	}

	@Test
	public void cannotTakeAlreadyPassed1() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 18);
		bebe.addTranscriptRecord(prog, new Term("t1"), 12);
		bebe.addTranscriptRecord(math1, new Term("t1"), 8.4);

		bebe.addTranscriptRecord(phys2, new Term("t2"), 10);
		bebe.addTranscriptRecord(ap, new Term("t2"), 16);
		bebe.addTranscriptRecord(math1, new Term("t2"), 10.5);

		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(math1, dm));

		assertEquals(violations.size(), 1);
		assertTrue(violations.get(0) instanceof EnrollmentRulesViolationException);
	}

	@Test
	public void cannotTakeAlreadyPassed2() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 18);
		bebe.addTranscriptRecord(prog, new Term("t1"), 12);
		bebe.addTranscriptRecord(math1, new Term("t1"), 8.4);

		bebe.addTranscriptRecord(phys2, new Term("t2"), 10);
		bebe.addTranscriptRecord(ap, new Term("t2"), 16);
		bebe.addTranscriptRecord(math1, new Term("t2"), 10.5);

		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(phys1, dm));

		assertEquals(violations.size(), 1);
		assertTrue(violations.get(0) instanceof EnrollmentRulesViolationException);
	}

	@Test
	public void cannotTakeOfferingsWithSameExamTime() {
		Calendar cal = Calendar.getInstance();
		List<Exception> violations = new EnrollCtrl().enroll(bebe,
				List.of(
					new Offering(phys1, cal.getTime()),
					new Offering(math1, cal.getTime()),
					new Offering(phys1, cal.getTime())
				));

		assertEquals(violations.size(), 8);
		violations.forEach(violation -> assertTrue(violation instanceof EnrollmentRulesViolationException));
	}

	@Test
	public void cannotTakeACourseTwice() {
		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(phys1, dm, phys1));

		assertEquals(violations.size(), 3);
		violations.forEach(violation -> assertTrue(violation instanceof EnrollmentRulesViolationException));
	}

	@Test
	public void canTake14WithGPA11() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 13);
		bebe.addTranscriptRecord(prog, new Term("t1"), 11);
		bebe.addTranscriptRecord(math1, new Term("t1"), 9);

		new EnrollCtrl().enroll(bebe, requestedOfferings(dm, math1, farsi, akhlagh, english, maaref));
		assertTrue(hasTaken(bebe, dm, math1, farsi, akhlagh, english, maaref));
	}

	@Test
	public void cannotTake15WithGPA11() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 13);
		bebe.addTranscriptRecord(prog, new Term("t1"), 11);
		bebe.addTranscriptRecord(math1, new Term("t1"), 9);

		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(dm, math1, farsi, akhlagh, english, ap));
		assertFalse(hasTaken(bebe, dm, math1, farsi, akhlagh, english, ap));

		assertEquals(violations.size(), 1);
		assertTrue(violations.get(0) instanceof EnrollmentRulesViolationException);
	}

	@Test
	public void canTake15WithGPA12() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 15);
		bebe.addTranscriptRecord(prog, new Term("t1"), 12);
		bebe.addTranscriptRecord(math1, new Term("t1"), 9);

		new EnrollCtrl().enroll(bebe, requestedOfferings(dm, math1, farsi, akhlagh, english, maaref));
		assertTrue(hasTaken(bebe, dm, math1, farsi, akhlagh, english, maaref));
	}

	@Test
	public void canTake15WithGPA15() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 15);
		bebe.addTranscriptRecord(prog, new Term("t1"), 15);
		bebe.addTranscriptRecord(math1, new Term("t1"), 15);

		new EnrollCtrl().enroll(bebe, requestedOfferings(dm, math2, farsi, akhlagh, english, maaref));
		assertTrue(hasTaken(bebe, dm, math2, farsi, akhlagh, english, maaref));
	}

	@Test
	public void cannotTake18WithGPA15() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 15);
		bebe.addTranscriptRecord(prog, new Term("t1"), 15);
		bebe.addTranscriptRecord(math1, new Term("t1"), 15);

		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(ap, dm, math2, farsi, akhlagh, english, ap));
		assertFalse(hasTaken(bebe, ap, dm, math2, farsi, akhlagh, english, ap));

		assertEquals(violations.size(), 3);
		violations.forEach(violation -> assertTrue(violation instanceof EnrollmentRulesViolationException));
	}

	@Test
	public void canTake20WithGPA16() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 16);
		bebe.addTranscriptRecord(prog, new Term("t1"), 16);
		bebe.addTranscriptRecord(math1, new Term("t1"), 16);

		new EnrollCtrl().enroll(bebe, requestedOfferings(
				ap, dm, math2, phys2, economy, karafarini, farsi));
		assertTrue(hasTaken(bebe, ap, dm, math2, phys2, economy, karafarini, farsi));
	}

	@Test
	public void cannotTake24() {
		bebe.addTranscriptRecord(phys1, new Term("t1"), 16);
		bebe.addTranscriptRecord(prog, new Term("t1"), 16);
		bebe.addTranscriptRecord(math1, new Term("t1"), 16);

		List<Exception> violations = new EnrollCtrl().enroll(bebe, requestedOfferings(
				ap, dm, math2, phys2, economy, karafarini, farsi, akhlagh, english));
		assertFalse(hasTaken(bebe, ap, dm, math2, phys2, economy, karafarini, farsi, akhlagh, english));

		assertEquals(violations.size(), 1);
		assertTrue(violations.get(0) instanceof EnrollmentRulesViolationException);
	}
}