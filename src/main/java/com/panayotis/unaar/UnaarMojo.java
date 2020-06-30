/*
 * (c) 2020 by Panayotis Katsaloulis
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.panayotis.unaar;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.panayotis.unaar.Template.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;


@Mojo(name = "unaar", defaultPhase = LifecyclePhase.INSTALL)
public class UnaarMojo extends AbstractMojo {

    @Parameter(property = "artifacts")
    private String artifacts;

    @Parameter(property = "shadowGroup")
    private String shadowGroup;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    private static final String[] REPOS = {"https://repo.maven.apache.org/maven2", "https://dl.google.com/dl/android/maven2", "https://jcenter.bintray.com"};

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (artifacts == null)
            return;

        Collection<Dependency> deps = new ArrayList<>();
        for (String a : artifacts.split(";")) {
            String[] parts = a.split(":");
            if (parts.length == 3)
                deps.add(new Dependency(parts[0], parts[1], parts[2]));
            else if (parts.length == 4)
                deps.add(new Dependency(parts[0], parts[1], parts[2], parts[3]));
            else
                throw new IllegalArgumentException("Unable to parse artifact " + a);
        }
        for (Dependency dep : deps)
            fetch(dep);
    }

    private void fetch(Dependency dep) throws MojoExecutionException {
        for (String repo : REPOS)
            if (fetch(dep, repo))
                return;
        getLog().error("Unable to find artifact " + dep);
        System.exit(1);
    }

    private boolean fetch(Dependency orig, String repo) throws MojoExecutionException {
        Dependency shadowed = orig.getFlattened(shadowGroup);
        File base = new File(mavenProject.getBuild().getDirectory(), "unaar");
        base.mkdirs();

        File jar = new File(base, shadowed.getFilename());
        File pom = new File(base, shadowed.getPom());
        jar.delete();
        pom.delete();
        String url = repo + "/" + orig.groupId.replace('.', '/') + "/" + orig.artifactId + "/"
                + orig.version + "/" + orig.artifactId + "-" + orig.version + "." + orig.packaging;
        if (!fetchJar(url, jar) || !createPom(pom, shadowed))
            return false;
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-install-plugin"),
                        version("2.5.2")
                ),
                goal("install-file"),
                configuration(
                        element(name("file"), jar.getAbsolutePath()),
                        element(name("groupId"), shadowed.groupId),
                        element(name("artifactId"), shadowed.artifactId),
                        element(name("version"), shadowed.version),
                        element(name("packaging"), shadowed.packaging),
                        element(name("pomFile"), pom.getAbsolutePath())
                ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                )
        );
        return true;
    }

    private boolean createPom(File pom, Dependency dep) throws MojoExecutionException {
        String pomData = POM_XML.
                replace(POM_GROUPID, dep.groupId).
                replace(POM_ARTIFACTID, dep.artifactId).
                replace(POM_VERSION, dep.version).
                replace(POM_PACKAGING, dep.packaging);
        try {
            Files.write(pom.toPath(), pomData.getBytes());
            return true;
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to save POM file", e);
        }
    }

    private boolean fetchJar(String url, File output) throws MojoExecutionException {
        try (ZipInputStream in = new ZipInputStream(new URL(url).openStream()); BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output))) {
            getLog().info("Fetching " + url);
            ZipEntry entry = in.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory() && entry.getName().equals("classes.jar")) {
                    byte[] bytesIn = new byte[10000];
                    int read;
                    while ((read = in.read(bytesIn)) >= 0)
                        out.write(bytesIn, 0, read);
                    return true;
                }
                in.closeEntry();
                entry = in.getNextEntry();
            }
            throw new MojoExecutionException("Unable to find classes.jar");
        } catch (IOException e) {
            return false;
        }
    }
}
