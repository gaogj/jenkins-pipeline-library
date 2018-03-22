package com.abnamro.stpl.java

import com.abnamro.stpl.java.model.MavenBuildInfo
import com.abnamro.stpl.util.Maven

/**
 * Class for executing NexusIQ tasks.
 */
class NexusIQ implements Serializable {

    def steps
    MavenBuildInfo buildInfo
    Maven maven
    String nexusIqServer

    /**
     * Creates a new instance.
     * @param steps the groovy dsl context
     * @param buildInfo the MavenBuildInfo class instance containing all the build information
     * @param maven the Maven core class instance
     * @param nexusIqServer the URL of the nexus IQ server to use
     */
    NexusIQ(steps, MavenBuildInfo buildInfo, Maven maven, String nexusIqServer) {
        this.steps = steps
        this.buildInfo = buildInfo
        this.maven = maven
        this.nexusIqServer = nexusIqServer
    }

    /**
     * Return the Application ID for NexusIQ calls.
     * @return the application id
     */
    private String getNexusIQApplicationId() {
        assert buildInfo.oarId: 'No oarId present'
        assert buildInfo.systemLetterCode: 'No oarId present'
        assert buildInfo.pom.artifactId: 'No artifactId present'

        return "${buildInfo.oarId}_${buildInfo.systemLetterCode}_${buildInfo.pom.artifactId}"
    }

    /**
     * Execute the Nexus IQ Analysis for this maven build.
     */
    void nexusIQAnalysis() {
        assert buildInfo.oarId: 'No oarId present'
        assert buildInfo.pom: 'No POM data was supplied!'

        if (buildInfo.skipNexusIQanalysis) {
            println "WARNING: Nexus IQ analysis skipped, please visit " +
                    "***REMOVED*** " +
                    "to get onboarded!"
        } else {
            println "Perform nexus IQ scan"
            try {
                    String evaluateCommand = "com.sonatype.clm:clm-maven-plugin:evaluate"
                    String additionalScopesCommand = "clm.additionalScopes=test,provided,system"

                    //construct applicationID and command for nexusIQ
                    String nexusIQApplicationId = getNexusIQApplicationId()
                    String applicationIdCommand = "clm.applicationId=${nexusIQApplicationId}"

                    println "Perform nexus IQ analysis"
                    //do a mvn clean package first to get all dependencies (based on effective pom, not just the project pom file)
                    maven.mvn('package -s settings.xml')
                    //perform nexusIQ analysis
                    String nexusIqCommand = "${evaluateCommand} -Dclm.serverUrl=${nexusIqServer} -D${additionalScopesCommand} -D${applicationIdCommand} -s settings.xml"
                    maven.mvn(nexusIqCommand)
            } catch (err) {
                steps.echo "Caught: ${err}"
                steps.error 'Failed to perform nexusIQ scan. Please check your oarID and other properties.'
            }
        }
    }
}
