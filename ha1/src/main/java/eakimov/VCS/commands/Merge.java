package eakimov.VCS.commands;

import com.google.common.collect.Lists;
import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.StageException;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Command(name = "merge", description = "Merge two branches")
public class Merge extends VCSCommand
{
    @Option(name = "-with", description = "branch to merge with", required = true)
    public String fromBranch;
    @Option(name = "-to", description = "branch to store result", required = false)
    public String toBranch;

    @Override
    protected void actualRun() throws RepositoryException
    {
        final Branch currentBranch = state.getCurrentBranch();
        if (currentBranch == null) {
            throw new BranchManagementException("current", "not set");
        }
        final Revision currentRevision = state.getCurrentRevision();

        final Branch mergeBranch = state.findBranchByName(fromBranch);
        if (mergeBranch == null) {
            throw new BranchManagementException(fromBranch, "not found");
        }

        Branch resultBranch = currentBranch;
        if (toBranch != null) {
            resultBranch = state.findBranchByName(toBranch);
            if (resultBranch == null) {
                throw new BranchManagementException(toBranch, "not found");
            }
        }

        if (!state.getStageFiles().isEmpty()) {
            throw new StageException("staged, but uncommited changes exists");
        }

        final Revision mergeRevision = mergeBranch.getHeadRevision();
        final Revision commonParent = findCommonParent(currentRevision, mergeRevision);

        // actual merge

        final Path stageDirectoryPath = Paths.get(repositoryWorkingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STAGE_DIRECTORY);
        VCSFileUtils.copyAllFiles(getRevisionPath(currentRevision), stageDirectoryPath);
        VCSMerge.merge(repositoryWorkingDirectory, commonParent, mergeRevision, stageDirectoryPath);

        // actual merge completed

        final int mergeRevisionId = mergeRevision == null ? 0 : mergeRevision.getId();
        final String commitMessage = String.format("merged from %s (revision %d)",
                fromBranch,
                mergeRevisionId);
        final Map<String, Revision> revisionFiles = new HashMap<>();
        VCSFileUtils.getWorkDirFiles(stageDirectoryPath)
                .forEach(f -> revisionFiles.put(f, null));
        final Revision commitRevision = new Revision(resultBranch,
                currentRevision,
                mergeRevision,
                commitMessage,
                revisionFiles);

        VCSFileUtils.copyAllFiles(stageDirectoryPath, getRevisionPath(commitRevision));
        VCSFileUtils.cleanUpDirectory(stageDirectoryPath);

        resultBranch.setHeadRevision(commitRevision);
        state.setCurrentRevision(commitRevision);
    }

    private static Revision findCommonParent(Revision first, Revision second) {
        final List<Revision> firstHistory = getRevisionHistory(first);
        final List<Revision> secondHistory = getRevisionHistory(second);
        final int commonSize = Math.min(firstHistory.size(), secondHistory.size());
        for (int i = commonSize - 1; i >= 0; i--) {
            if (firstHistory.get(i) == secondHistory.get(i)) {
                return firstHistory.get(i);
            }
        }
        return null;
    }

    private static List<Revision> getRevisionHistory(Revision revision) {
        final List<Revision> result = new ArrayList<>();
        while (revision != null) {
            result.add(revision);
            revision = revision.getParent();
        }
        return Lists.reverse(result);
    }
}
