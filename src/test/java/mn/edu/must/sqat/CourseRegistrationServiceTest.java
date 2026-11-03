package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "B230001";
    private static final String STUDENT_EMAIL = "student@example.com";
    private static final String COURSE_ID = "F.CSA313";
    private static final String PREREQUISITE = "F.CSA211";

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    // A1
    @Test
    void register_activeStudent_returnsOkAndSavesAndSendsEmail() {
        stubEligibleStudent();

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(STUDENT_EMAIL), eq("Бүртгэл баталгаажлаа"),
                contains(COURSE_ID));
    }

    // A2
    @Test
    void register_missingStudent_returnsNoStudentErrorWithoutSavingOrSendingEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3
    @Test
    void register_inactiveStudent_returnsInactiveErrorWithoutSavingOrSendingEmail() {
        Student student = new Student(STUDENT_ID, STUDENT_EMAIL, false, List.of());
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);

        assertEquals("ERROR_INACTIVE_STUDENT", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4
    @Test
    void register_missingPrerequisite_returnsPrerequisitesErrorWithoutSavingOrSendingEmail() {
        Student student = new Student(STUDENT_ID, STUDENT_EMAIL, true, List.of());
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of(PREREQUISITE));

        assertEquals("ERROR_PREREQUISITES", service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A5
    @Test
    void register_emailSenderThrows_returnsOkAndSavesRegistration() {
        stubEligibleStudent();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(eq(STUDENT_EMAIL), anyString(), anyString());

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(STUDENT_EMAIL), anyString(), anyString());
    }

    // A6: хоёр аргументын null-ийг нэг тестийн метод дотор тус тусад нь шалгана.
    @Test
    void register_nullArgument_throwsIllegalArgumentExceptionWithoutInteractions() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, COURSE_ID));
        assertThrows(IllegalArgumentException.class,
                () -> service.register(STUDENT_ID, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.register(null, null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7
    @Test
    void register_successfulRegistration_emailBodyContainsCourseId() {
        stubEligibleStudent();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register(STUDENT_ID, COURSE_ID));

        verify(emailSender).send(eq(STUDENT_EMAIL), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
    }

    // A8: оюутан байхгүй, идэвхгүй, хичээл байхгүй гэсэн гурван алдааны ангийг шалгана.
    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource({
            "false, true,  true,  ERROR_NO_STUDENT",
            "true,  false, true,  ERROR_INACTIVE_STUDENT",
            "true,  true,  false, ERROR_NO_COURSE"
    })
    void register_invalidStudentOrCourse_returnsExpectedErrorWithoutSavingOrSendingEmail(
            boolean studentExists, boolean active, boolean courseExists, String expectedError) {
        Student student = studentExists
                ? new Student(STUDENT_ID, STUDENT_EMAIL, active, List.of(PREREQUISITE))
                : null;
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        // Эхний хоёр мөр хичээл шалгахаас өмнө буцдаг тул зөвхөн энэ stub lenient байна.
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(courseExists);

        assertEquals(expectedError, service.register(STUDENT_ID, COURSE_ID));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    private void stubEligibleStudent() {
        Student student = new Student(STUDENT_ID, STUDENT_EMAIL, true, List.of(PREREQUISITE));
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of(PREREQUISITE));
    }
}
