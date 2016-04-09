import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<Integer> digits = Arrays.asList(1, 2, 3, 4, 5);
        Function2<Integer, Integer, Integer> digitsToInt = (sum, elem) -> sum * 10 + elem;
        assertEquals(12345, (int)Collections.foldl(digitsToInt, 0, digits));

        List<Derived> arr = Stream.of(1, 2, 3, 4, 5)
                .map(Derived::new)
                .collect(Collectors.toList());
        Function2<Integer, Base, Integer> add = (sum, elem) -> sum + elem.getX();
        assertEquals(15, (int)Collections.foldl(add, 0, arr));
    }

    @Test
    public void testFoldr() throws Exception {
        List<Integer> digits = Arrays.asList(1, 2, 3, 4, 5);
        Function2<Integer, Integer, Integer> digitsToReversedInt = (elem, sum) -> sum * 10 + elem;
        assertEquals(54321, (int)Collections.foldr(digitsToReversedInt, 0, digits));

        List<Derived> arr = Stream.of(1, 2, 3, 4, 5)
                .map(Derived::new)
                .collect(Collectors.toList());
        Function2<Base, Integer, Integer> add = (elem, sum) -> sum + elem.getX();
        assertEquals(15, (int)Collections.foldr(add, 0, arr));
    }
}