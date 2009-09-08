/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Apr 18, 2009
 * Time: 1:08:16 PM
 */
package com.theoryinpractise.clojure;

import org.apache.commons.exec.Executor;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteException;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClojureCompilerMojo extends AbstractMojo {
    
    protected void callClojureWith(
            File[] sourceDirectory,
            File outputDirectory,
            List<String> compileClasspathElements,
            String mainClass,
            String[] clojureArgs) throws MojoExecutionException {
    
        outputDirectory.mkdirs();
                
        String cp = "";
        for (File directory : sourceDirectory) {
            cp = cp + directory.getPath() + File.pathSeparator;
        }
    
        cp = cp + outputDirectory.getPath() + File.pathSeparator;
    
        for (Object classpathElement : compileClasspathElements) {
            cp = cp + File.pathSeparator + classpathElement;
        }
    
        getLog().debug("Clojure classpath: " + cp);
        CommandLine cl = new CommandLine("java");
    
        cl.addArgument("-cp");
        cl.addArgument(cp);
        cl.addArgument("-Dclojure.compile.path=" + outputDirectory.getPath() + "");
        cl.addArgument(mainClass);
        
        if (clojureArgs != null) {
            cl.addArguments(clojureArgs);
        }
        
        Executor exec = new DefaultExecutor();
        Map<String,String> env = new HashMap<String,String>(System.getenv());
        env.put("path", ";");
        env.put("path", System.getProperty("java.home"));
        
        ExecuteStreamHandler handler = new CustomPumpStreamHandler(System.out, System.err, System.in);
        exec.setStreamHandler(handler);
        
        int status;
        try {
            status = exec.execute(cl, env);
        } catch (ExecuteException e) {
            status = e.getExitValue();
        } catch(IOException e) {
            status = 1;
        }
        
        if (status != 0) {
            throw new MojoExecutionException("Clojure failed.");
        }
    }
}
