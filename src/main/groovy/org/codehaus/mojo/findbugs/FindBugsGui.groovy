package org.codehaus.mojo.findbugs

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.project.MavenProject
import org.apache.maven.plugin.AbstractMojo

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope


/**
 * Launch the Findbugs GUI.
 * It will use all the parameters in the POM fle.
 *
 * @since 2.0
 *
 * @description Launch the Findbugs GUI using the parameters in the POM fle.
 *
 * @author <a href="mailto:gleclaire@codehaus.org">Garvin LeClaire</a>
 * @version $Id: FindBugsGui.groovy gleclaire $
 */

@Mojo( name = "gui", requiresDependencyResolution = ResolutionScope.TEST, requiresProject = true )
class FindBugsGui extends AbstractMojo {


    /**
     * locale to use for Resource bundle.
     *
     *
     */
    static Locale locale = Locale.ENGLISH

    /**
     * Directory containing the class files for FindBugs to analyze.
     *
     * @required
     */
    @Parameter( defaultValue = '${project.build.outputDirectory}', required = true )
    File classFilesDirectory

    /**
     * turn on Findbugs debugging
     *
     */
    @Parameter( defaultValue = "false", property="findbugs.debug" )
    Boolean debug

    /**
     * List of artifacts this plugin depends on. Used for resolving the Findbugs coreplugin.
     *
     */
    @Parameter( property="plugin.artifacts", required = true, readonly = true )
    List pluginArtifacts

    /**
     * Effort of the bug finders. Valid values are Min, Default and Max.
     *
     */
    @Parameter( defaultValue = "Default", property="findbugs.effort" )
    String effort


    /**
     * The plugin list to include in the report. This is a FindBugsInfo.COMMA-delimited list.
     *
     */
    @Parameter( property="findbugs.pluginList" )
    String pluginList

    /**
     * Maven Project
     *
     */
    @Parameter( property="project", required = true, readonly = true )
    MavenProject project

    /**
     * Resource bundle for a specific locale.
     *
     */
    @Parameter( readonly = true )
    ResourceBundle bundle

    /**
     * Specifies the directory where the findbugs native xml output will be generated.
     *
     */
    @Parameter( defaultValue = '${project.build.directory}', required = true )
    File findbugsXmlOutputDirectory

    /**
     * The file encoding to use when reading the source files. If the property <code>project.build.sourceEncoding</code>
     * is not set, the platform default encoding is used. <strong>Note:</strong> This parameter always overrides the
     * property <code>charset</code> from Checkstyle's <code>TreeWalker</code> module.
     *
     * @since 2.2
     */
    @Parameter( property="encoding", defaultValue = '${project.build.sourceEncoding}' )
    String encoding

    /**
     * Maximum Java heap size in megabytes  (default=512).
     *
     * @since 2.2
     */
    @Parameter( property="findbugs.maxHeap", defaultValue = "512" )
    int maxHeap

    void execute() {

        def ant = new AntBuilder()

        def auxClasspathElements = project.compileClasspathElements

        if ( debug ) {
            log.debug("  Plugin Artifacts to be added ->" + pluginArtifacts.toString())
        }




        ant.project.setProperty('basedir', findbugsXmlOutputDirectory.getAbsolutePath())
        ant.project.setProperty('verbose', "true")


        ant.java(classname: "edu.umd.cs.findbugs.LaunchAppropriateUI", fork: "true", failonerror: "true", clonevm: "true", maxmemory: "${maxHeap}m")
        {

            def effectiveEncoding = System.getProperty( "file.encoding", "UTF-8" )

            if ( encoding ) { effectiveEncoding = encoding }

            log.info("File Encoding is " + effectiveEncoding)

            sysproperty(key: "file.encoding" , value: effectiveEncoding)
            def findbugsXmlName = findbugsXmlOutputDirectory.toString() + "/findbugsXml.xml"
            def findbugsXml = new File(findbugsXmlName)

            if ( findbugsXml.exists() ) {
                log.debug("  Found an FindBugs XML at ->" + findbugsXml.toString())
                arg(value: findbugsXml)
            }


            classpath()
            {

                pluginArtifacts.each() {pluginArtifact ->
                    if ( debug ) {
                        log.debug("  Trying to Add to pluginArtifact ->" + pluginArtifact.file.toString())
                    }

                    pathelement(location: pluginArtifact.file)
                }
            }
        }
    }
}
