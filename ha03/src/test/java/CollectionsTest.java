import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CollectionsTest {

    @Test
    public void testMap() throws Exception {
        List<Integer> srcList = Arrays.asList(1, 2, 3, 4, 5);
        List<Base> destList = Collections.map(arg -> new Base(arg * 2), srcList);
        assertEquals(srcList.size(), destList.size());
        for (int i = 0; i < srcList.size(); i++) {
            assertEquals(srcList.get(i) * 2, (int)destList.get(i).getX());
        }
        assertTrue(Collections.map(a->null, new ArrayList<>()).isEmpty());
    }

    @Test
    public void testFilter() throws Exception {
        List<Integer> srcList = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(srcList, Collections.filter(Predicate.ALWAYS_TRUE, srcList));
        assertTrue(Collections.filter(Predicate.ALWAYS_FALSE, srcList).isEmpty());
        List<Integer> destList = Arrays.asList(2, 4);
        assertEquals(destList, Collections.filter(a -> a % 2==0, srcList));
        assertTrue(Collections.filter(Predicate.ALWAYS_TRUE, new ArrayList<>()).isEmpty());
    }

    @Test
    public void testTakeWhile() throws Exception {
        List<Integer> srcList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> destList = Arrays.asList(1, 2, 3);
        assertEquals(destList, Collections.takeWhile(a -> a < 4, srcList));
        assertTrue(Collections.takeWhile(a -> a > 42, srcList).isEmpty());
        assertEquals(srcList, Collections.takeWhile(Predicate.ALWAYS_TRUE, srcList));
        assertTrue(Collections.takeWhile(Predicate.ALWAYS_FALSE, srcList).isEmpty());
    }

    @Test
    public void testTakeUnless() throws Exception {
        List<Integer> srcList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> destList = Arrays.asList(1, 2, 3);
        assertEquals(destList, Collections.takeUnless(a -> a >= 4, srcList));
        assertTrue(Collections.takeUnless(a -> a < 42, srcList).isEmpty());
        assertEquals(srcList, Collections.takeUnless(Predicate.ALWAYS_FALSE, srcList));
        assertTrue(Collections.takeUnless(Predicate.ALWAYS_TRUE, srcList).isEmpty());
    }

    @Test
    public void testFoldl() throws Exception {
        List<Integer> arr = Arrays.asList(1, 2, 3, 4, 5);
        Function2<Base, Integer, Derived> add = (a, b) -> new Derived(a.getX() + b);
        Derived zero = new Derived(0);
        Derived sum = Collections.foldl(add, zero, arr);
        assertEquals(15, (int)sum.getX());
        assertEquals(zero, Collections.foldl(add, zero, new ArrayList<>()));
    }

    @Test
    public void testFoldr() throws Exception {
        List<Integer> arr = Arrays.asList(1, 2, 3, 4, 5);
        Function2<Integer, Base, Derived> add = (a, b) -> new Derived(a + b.getX());
        Derived zero = new Derived(0);
        Derived sum = Collections.foldr(add, new Derived(0), arr);
        assertEquals(15, (int)sum.getX());
        assertEquals(zero, Collections.foldr(add, zero, new ArrayList<>()));
    }
}