package hudson.plugins.git.extensions.impl;

import hudson.Extension;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Force Workspace-less polling via "git ls-remote". Only needed for jobs that can't have a persistant workspace
 *
 * @author Isaac Johnson, based on DisableRemotePoll by Kohsuke Kawaguchi
 */
public class RequireRemotePoll extends GitSCMExtension {

    @DataBoundConstructor
    public RequireRemotePoll() {
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Extension
    public static class DescriptorImpl extends GitSCMExtensionDescriptor {
        @Override
        public String getDisplayName() {
            return "Force polling using remote";
        }
    }
}
