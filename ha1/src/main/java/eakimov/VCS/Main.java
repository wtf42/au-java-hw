package eakimov.VCS;

import eakimov.VCS.commands.*;
import io.airlift.airline.*;

public class Main {
    public static void main(String[] args)
    {
        Cli.CliBuilder<Runnable> builder = Cli.<Runnable>builder("vcs")
                .withDescription("HA1 VCS")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        Commit.class,
                        Checkout.class,
                        Log.class,
                        Merge.class,
                        Init.class,
                        AddFile.class,
                        Status.class,
                        ResetFile.class,
                        RemoveFile.class,
                        CleanFiles.class);

        builder.withGroup("branch")
                .withDescription("Manage branches")
                .withDefaultCommand(Help.class)
                .withCommands(Help.class,
                        NewBranch.class,
                        CloneBranch.class,
                        DeleteBranch.class,
                        ListBranches.class);

        Cli<Runnable> vcsArgumentsParser = builder.build();

        try {
            vcsArgumentsParser.parse(args).run();
        } catch (ParseException e) {
            System.err.println("failed to parse arguments: " + e.getMessage());
        }
    }
}
