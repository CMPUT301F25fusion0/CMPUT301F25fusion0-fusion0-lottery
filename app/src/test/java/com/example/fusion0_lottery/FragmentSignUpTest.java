import org.junit.Test;
import static org.junit.Assert.*;

import com.example.fusion0_lottery.FragmentSignUp;

public class FragmentSignUpTest {

    private final FragmentSignUp fragment = new FragmentSignUp();

    @Test
    public void testEmptyInputs() {
        boolean noName = fragment.validSignUp("", "test@gmail.com", "password", "password");
        boolean noEmail = fragment.validSignUp("Test", "", "password", "password");
        boolean noPassword = fragment.validSignUp("Test", "test@gmail.com", "", "password");
        boolean noConfirmPassword = fragment.validSignUp("Test", "test@gmail.com", "password", "");
        assertFalse(noName);
        assertFalse(noEmail);
        assertFalse(noPassword);
        assertFalse(noConfirmPassword);


    }

    @Test
    public void testMatchingPasswords() {
        boolean testNoMatch = fragment.validSignUp("Test", "test@gmail.com", "password1", "password2");
        boolean testMatch = fragment.validSignUp("Test", "test@gmail.com", "password1", "password1");
        assertFalse(testNoMatch);
        assertTrue(testMatch);
    }

    @Test
    public void testSignUp() {
        boolean result = fragment.validSignUp("Test", "test@gmail.com", "password", "password");
        assertTrue(result);
    }
}
