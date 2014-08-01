package com.shazam.dataengineering.pipelinebuilder;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.Serializable;

/**
 * Created by daniil.osipov on 7/29/14.
 */
public class Environment implements Describable<Environment>, Serializable {
    private String name;
    private String properties;

    public Environment(String name) {
        this.name = name;
        this.properties = "key: value";
    }

    @DataBoundConstructor
    public Environment(String name, String configParam) {
        this.name = name;
        this.properties = configParam;
    }

    public String getName() {
        return name;
    }

    public String getConfigParam() {
        return properties;
    }

    public EnvironmentDescriptor getDescriptor() {
        return (EnvironmentDescriptor) Jenkins.getInstance().getDescriptor(getClass());
    }

}