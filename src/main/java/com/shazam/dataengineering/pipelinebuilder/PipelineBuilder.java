package com.shazam.dataengineering.pipelinebuilder;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

public class PipelineBuilder extends Builder {
    private Environment[] configParams;
    private String file;

    private static ProductionEnvironment productionEnvironment = new ProductionEnvironment("Production");
    private static DevelopmentEnvironment developmentEnvironment = new DevelopmentEnvironment("Development");

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public PipelineBuilder(String filePath, Environment[] environment) {
        this.configParams = environment;
        this.file = filePath;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        FilePath ws = build.getWorkspace();
        FilePath input = ws.child(file);

        PipelineProcessor processor = new PipelineProcessor(build, listener);
        processor.setEnvironments(configParams);

        boolean result = processor.process(input);
        if (result) {
            // TODO: Add files for release
            //build.pickArtifactManager().archive(ws, launcher, listener, files);
        }

        return result;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public Environment[] getConfigParams() {
        return configParams;
    }

    public String getFilePath() {
        return file;
    }

    public String getDisplayName() {
        return "AWS Data Pipeline";
    }

    public List<Environment> getEnvironmentList() {
        ArrayList<Environment> env = new ArrayList<Environment>();
        if (configParams != null) {
            for (Environment environment : configParams) {
                env.add(environment);
            }
        }
        return env;
    }

    /**
     * Descriptor for {@link PipelineBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * TODO: For global parameters
     *
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String filePath;
        private Environment[] configParams;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         *      <p>
         *      Note that returning {@link hudson.util.FormValidation#error(String)} does not
         *      prevent the form from being saved. It just means that a message
         *      will be displayed to the user.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error("Please set a name");
//            if (value.length() < 4)
//                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        // TODO: No path to workspace, can't verify
//        public FormValidation doCheckFilePath(@QueryParameter String value)
//                throws IOException, ServletException, InterruptedException {
//            FilePath path = new FilePath(new File(value));
//            if (!path.exists()) {
//                return FormValidation.error("Defined file doesn't exist");
//            } else {
//                return FormValidation.ok();
//            }
//        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "AWS Data Pipeline";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            filePath = formData.getString("filePath");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }


        /**
         * Populates the "Add configuration" button with choices for environments
         *
         * @param project   Instance
         * @return
         */
        public static List<Descriptor<Environment>> getEnvironmentDescriptions(AbstractProject<?,?> project) {
            ArrayList<Descriptor<Environment>> descriptors = new ArrayList<Descriptor<Environment>>();
            descriptors.add(productionEnvironment.getDescriptor());
            descriptors.add(developmentEnvironment.getDescriptor());
            return descriptors;
        }



        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public String getFilePath() {
            return filePath;
        }
    }
}