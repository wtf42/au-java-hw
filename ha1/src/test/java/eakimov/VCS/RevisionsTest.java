package eakimov.VCS;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class RevisionsTest {
    @Test
    public void manageRevisions() throws Exception {
        final Branch firstBranch = new Branch("first branch", null);
        final Revision rev1 = new Revision(firstBranch, null, "first commit", new HashMap<>());
        final Revision rev2 = new Revision(firstBranch, rev1, "second commit", new HashMap<>());

        assertEquals(1, rev1.getId());
        assertEquals(2, rev2.getId());
        assertEquals(3, firstBranch.nextRevisionId());
        assertEquals(rev1, rev2.getParent());
        assertNull(rev2.getMergeParent());

        final Branch secondBranch = new Branch("second branch", rev2);
        final Revision rev4 = new Revision(secondBranch, rev2, "alternative commit", new HashMap<>());

        assertEquals(1, rev4.getId());
        assertEquals(rev2, rev4.getParent());
        assertNull(rev4.getMergeParent());

        final Branch mergedBranch = new Branch("merged branch", null);
        final Revision mergedRevision = new Revision(mergedBranch,
                rev1, rev4, "merged rev1 and rev4 to new branch", new HashMap<>());

        assertEquals(rev1, mergedRevision.getParent());
        assertEquals(rev4, mergedRevision.getMergeParent());
        assertEquals(1, mergedRevision.getId());

        final Revision mergedToHeadRevision = new Revision(firstBranch,
                rev1, rev4, "merged rev1 and rev4 to first branch", new HashMap<>());

        assertEquals(rev1, mergedToHeadRevision.getParent());
        assertEquals(rev4, mergedToHeadRevision.getMergeParent());
        assertEquals(4, mergedToHeadRevision.getId());
    }
}