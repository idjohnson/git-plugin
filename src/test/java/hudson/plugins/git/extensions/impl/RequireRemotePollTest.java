package hudson.plugins.git.extensions.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.TestGitRepo;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionTest;
import hudson.scm.PollingResult;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.jvnet.hudson.test.CaptureEnvironmentBuilder;
import org.mockito.Mockito;

public class RequireRemotePollTest extends GitSCMExtensionTest {
    public class RecordingPrintStream extends PrintStream {
        private final List<String> list = new ArrayList<String>();

        public RecordingPrintStream() {
            super(System.out);
        }

        @Override
        public void println(String x) {
            super.println(x);
            list.add(x);
        }

        @Override
        public void print(String x) {
            super.print(x);
            list.add(x);
        }

        public List<String> getList() {
            return list;
        }
    }

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("main");
    private TestGitRepo repo;

    @Test
    public void testRemotePolling() throws Exception {
        repo = new TestGitRepo("repo", tmp.newFolder(), listener);

        TaskListener listener = Mockito.mock(TaskListener.class);
        RecordingPrintStream recordingPrintStream = new RecordingPrintStream();
        when(listener.getLogger()).thenReturn(recordingPrintStream);

        FreeStyleProject project = setupBasicProject(repo, new String[] { "*" });

        assertTrue("Project should have >0 changes", project.poll(listener).hasChanges());
        repo.commit("firstFile", "bluh bluh 01", repo.johnDoe, "initial commit");
        // System.err.println("1 Workspace: " + project.getWorkspace());
        build(project, Result.SUCCESS);
        project.doDoWipeOutWorkspace();
        PollingResult poll = project.poll(listener);

        assertFalse("Project should have 0 changes", poll.hasChanges());

        for (String logItem : recordingPrintStream.getList()) {
            assertThat("Should not find message about not getting remote.", logItem,
                    not(equalTo("No workspace is available, so can’t check for updates.")));
        }
    }

    @Override
    protected void before() throws Exception {
        // nothing on purpose
    }

    @Override
    protected GitSCMExtension getExtension() {
        GitSCMExtension rrp = new RequireRemotePoll();
        return rrp;
    }

    protected FreeStyleProject setupBasicProject(TestGitRepo repo, String[] branchNames) throws Exception {
        GitSCMExtension extension = getExtension();
        FreeStyleProject project = j.createFreeStyleProject(extension.getClass() + "Project");
        List<BranchSpec> branches = new ArrayList<BranchSpec>();

        for (String branchName : branchNames) {
            branches.add(new BranchSpec(branchName));
        }

        GitSCM scm = new GitSCM(repo.remoteConfigs(), branches, false, Collections.<SubmoduleConfig> emptyList(), null,
                null, Collections.<GitSCMExtension> emptyList());
        System.err.println("###\t" + extension.requiresWorkspaceForPolling());
        scm.getExtensions().add(extension);
        project.setScm(scm);
        project.getBuildersList().add(new CaptureEnvironmentBuilder());

        return project;
    }
}
