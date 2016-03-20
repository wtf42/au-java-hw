import org.junit.Test;

import static org.junit.Assert.*;


public class PredicateTest {

    @Test
    public void testOr() throws Exception {
        assertFalse(Predicate.ALWAYS_FALSE.or(Predicate.ALWAYS_FALSE).apply(null));
        assertTrue(Predicate.ALWAYS_FALSE.or(Predicate.ALWAYS_TRUE).apply(null));
        assertTrue(Predicate.ALWAYS_TRUE.or(Predicate.ALWAYS_FALSE).apply(null));
        assertTrue(Predicate.ALWAYS_TRUE.or(Predicate.ALWAYS_TRUE).apply(null));
        final boolean[] fail = {false};
        Predicate.ALWAYS_TRUE.or(arg -> {
            fail[0] = true;
            return false;
        }).apply(null);
        assertFalse(fail[0]);
    }

    @Test
    public void testAnd() throws Exception {
        assertFalse(Predicate.ALWAYS_FALSE.and(Predicate.ALWAYS_FALSE).apply(null));
        assertFalse(Predicate.ALWAYS_FALSE.and(Predicate.ALWAYS_TRUE).apply(null));
        assertFalse(Predicate.ALWAYS_TRUE.and(Predicate.ALWAYS_FALSE).apply(null));
        assertTrue(Predicate.ALWAYS_TRUE.and(Predicate.ALWAYS_TRUE).apply(null));
        final boolean[] fail = {false};
        Predicate.ALWAYS_FALSE.and(arg -> {
            fail[0] = true;
            return false;
        }).apply(null);
        assertFalse(fail[0]);
    }

    @Test
    public void testNot() throws Exception {
        assertFalse(Predicate.ALWAYS_TRUE.not().apply(null));
        assertTrue(Predicate.ALWAYS_FALSE.not().apply(null));
    }
}
