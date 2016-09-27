package eakimov.VCS;

import org.junit.Test;

import static org.junit.Assert.*;

public class RevisionsTest {
    @Test
    public void manageRevisions() throws Exception {
        final Branch firstBranch = new Branch("first branch", null);
        final Revision rev1 = new Revision(firstBranch, null, "first commit");
        final Revision rev2 = new Revision(firstBranch, rev1, "second commit");

        assertEquals(1, rev1.getId());
        assertEquals(2, rev2.getId());
        assertEquals(3, firstBranch.nextRevisionId());

        final Branch secondBranch = new Branch("second branch", null);
        final Revision rev3 = new Revision(secondBranch, rev2);
        final Revision rev4 = new Revision(secondBranch, rev3, "alternative commit");

        assertEquals(1, rev3.getId());
        assertEquals(rev1, rev2.getParent());
        assertNull(rev2.getMergeParent());

        assertEquals(2, rev4.getId());
        assertEquals(rev3, rev4.getParent());
        assertNull(rev4.getMergeParent());

        final Branch mergedBranch = new Branch("merged branch", null);
        final Revision mergedRevision = new Revision(mergedBranch,
                rev1, rev4, "merged rev1 and rev4 to new branch");

        assertEquals(rev1, mergedRevision.getParent());
        assertEquals(rev4, mergedRevision.getMergeParent());
        assertEquals(1, mergedRevision.getId());

        final Revision mergedToHeadRevision = new Revision(firstBranch,
                rev1, rev4, "merged rev1 and rev4 to first branch");

        assertEquals(rev1, mergedToHeadRevision.getParent());
        assertEquals(rev4, mergedToHeadRevision.getMergeParent());
        assertEquals(4, mergedToHeadRevision.getId());
    }
}