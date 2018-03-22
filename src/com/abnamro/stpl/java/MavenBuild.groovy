package com.abnamro.stpl.java

import com.abnamro.stpl.java.model.MavenBuildInfo
import com.abnamro.stpl.util.Maven;

/**
 * Class for executing build tasks for Maven applications.
 */
class MavenBuild implements Serializable {

    MavenBuildInfo info;
    Maven maven;
    def steps

    /**
     * Creates a new MavenBuild.
     * @param steps the groovy dsl context
     * @param maven the maven util class
     * @param mavenBuildInfo all the information required for executing the different maven build actions
     */
    MavenBuild(steps, Maven maven, MavenBuildInfo mavenBuildInfo) {
        this.steps = steps
        this.maven = maven
        this.info = mavenBuildInfo
    }

    /**
     * Create a build tag number based upon the convention, pom and <b>currentBuild</b> param from the context.
     * <br>
     * The convention is that the build version should be Major.Minor.Patch-${build number}. <br>
     * The buildTag will then be v${buildVersion}. <br>
     * Example: <br>
     * <ul>
     *     <li>current version in pom.xml is 1.2.0</li>
     *     <li>current jenkins build number is 43</li>
     *     <li>buildTag=<b>v1.2.0-43</b></li>
     * <ul>
     *
     *
     * @param pom the maven pom object, the outcome of readMavenPom()
     * @return the string to use as build tag
     */
    String createBuildTag(pom) {
        def version = pom.version.replace("-SNAPSHOT", "-${steps.currentBuild.number}")
        return "v${version}"
    }

    /**
     * The default way to build a branch (i.e. not Master or Trunk).
     * <br>
     * Assumption: current version is a snapshot. <br>
     * Outcome: current version ends up as snapshot in ${SystemLetterCode}-snapshots.
     */
    void mavenBranchBuild() {
        String buildCommand = "clean deploy -B -s settings.xml ${info.buildTargets}"
        maven.mvn(buildCommand)

        if (info.publishJUnit) {
            steps.step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
        }

    }

    /**
     * The default maven build (assuming mainline: Trunk or Master)
     *
     * @param pom the maven pom, outcome of readMavenPom()
     * @return the version that is published to nexus/tag
     */
    String mavenBuild(def pom) {
        assert pom: 'No POM data was supplied!'

        /**
         * See: SOLO-2374
         * Either we do the JavaDoc separately or we do it in the release.
         * If done in the release, we do not fail on error.
         *
         * This way everyone can always get feedback on this
         */
        def javaDocAddition = '-Dmaven.javadoc.skip=true'
        if (info.publishJavaDoc) {
            maven.mvn("clean install javadoc:aggregate -s settings.xml ${info.javaDocBuildTargets}")
            steps.step([$class: 'JavadocArchiver', javadocDir: info.javaDocFolder, keepAll: false])
        } else {
            javaDocAddition = '-Dmaven.javadoc.failOnError=false'
        }

        def version = pom.version.replace("-SNAPSHOT", "-${steps.currentBuild.number}")
        String buildCommand = "-DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true clean release:clean release:prepare release:perform -B -s settings.xml ${javaDocAddition} ${info.buildTargets}"
        maven.mvn(buildCommand)

        if (info.publishJUnit) {
            steps.step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
        }

        return version
    }

    /**
     * Makes sure there is a settings.xml to work with. <br>
     * Every maven action assumes there is a settings.xml in the root of the workspace. <br>
     * <br>
     * If not overridden by <i>settingsXmlInWorkspace</i> or <i>copySettingsXmlFromJob</i> params, a settings.xml is generated.<br>
     * It is generated based upon the following assumptions:<br>
     * <ul>
     *     <li>you have provided the <b>systemLetterCode</b></li>
     *     <li>you are using the <b>systemLetterCode</b> in your pom.xml</li>
     *     <li>you are using the master-pom as parent pom</li>
     *     <li>you have a technical user ${systemLetterCode}_BUILDER</li>
     *     <li>you have the default maven repositories ${systemLetterCode}-releases,-snapshots and -releases-virtual</li>
     *     <li>your technical user has write rights to the ${systemLetterCode}-releases and -snapshot repositories</li>
     * </ul>
     * <br>
     */
    void prepareSettingsXml() {
        def settingsXmlExists = steps.fileExists 'settings.xml'
        if (info.settingsXmlInWorkspace && settingsXmlExists) {
            return // we don't have to do anything, there's a settings.xml
        }

        if (info.copySettingsXmlFromJob != 'false') {
            steps.step(
                    [$class              : 'CopyArtifact',
                     filter              : 'settings.xml',
                     fingerprintArtifacts: true,
                     projectName         : "${info.copySettingsXmlFromJob}",
                     selector            : [$class: 'StatusBuildSelector', stable: false]
                    ]
            )
        } else {
            String credentialsId = "${info.applicationCode}_BUILDER"
            maven.writeSettingsXml(info.applicationCode, info.mavenMirrorUrl, credentialsId, false )
        }
    }
}
