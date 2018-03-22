package com.abnamro.stpl.util

/**
 * Class for providing some generic utilities that can be used by any pipeline.
 */
class Utilities implements Serializable {
    private def jdkLinuxMap = [
            '1.7': 'Linux IBM JDK 1.7',
            '1.8': 'Linux IBM JDK 1.8'
    ]
    private def jdkWindowsMap = [
            '1.7': 'SUN JDK 1.7',
            '1.8':'SUN JDK 1.8',
            '1.7-ibm': 'IBM JDK 1.7',
    ]

    private def mavenLinuxMap = [
            '2.2.1': 'Linux Maven 2.2.1',
            '3.2.1': 'Linux Maven 3.2.1',
            '3.3.3': 'Linux Maven 3.3.3',
    ]

    private def mavenWindowsMap = [
            '2.2.1': 'Maven 2.2.1',
            '3.2.1': 'Maven 3.2.1',
            '3.3.3': 'Maven 3.3.3',
    ]

    def steps

    /**
     *
     * @param steps pipeline dsl context
     */
    Utilities(steps) {
        this.steps = steps

    }

    /**
     * Returns either the property of the value from the property container or the supplied default.
     * The propertiesContainer can be object received from readProperties dsl method.
     *
     * @param propertiesContainer object containing the properties
     * @param propertyKey key of the property you want to retrieve
     * @param defaultValue the default value in case the container doesn't contain the key
     * @return the property if it exists, or the defaultValue
     */
    String getPropertyOrDefault(propertiesContainer, String propertyKey, String defaultValue) {
        assert propertiesContainer: 'there is no propertiesContainer present'
        assert propertyKey: 'there is no propertyKey present'

        if (propertiesContainer[propertyKey]) {
            return propertiesContainer[propertyKey]
        }
        return defaultValue
    }

    /**
     * Returns wether or not this is a mainline branch.
     * In case of SVN, it is mainline if the branch is trunk.
     * In case of Git, it is mainline if the branch is master.
     * Else returns false.
     *
     * @param scmType scn type 'git', or 'svn'
     * @param branchName
     * @return
     */
    boolean isMainLineBranch(String scmType, String branchName) {
        if (scmType == 'git') {
            return 'master' == branchName
        }
        return 'trunk' == branchName
    }

    /**
     * Retrieve the JDK tool definition based upon desired version and platform.
     * For example, if you're on Linux and desiredVersion is 1.7, you get 'Linux IBM JDK 1.7'.
     *
     * @param desiredVersion the desiredVersion of the JDK (1.7, 1.8)
     * @return
     */
    void getJDKTool(String desiredVersion) {
        def jdk
        if (steps.isUnix()) {
            jdk = jdkLinuxMap.get(desiredVersion)
        } else {
            jdk = jdkWindowsMap.get(desiredVersion)
        }

        if (jdk) {
            def jdkTool = steps.tool name: jdk, type: 'jdk'
            return jdkTool
        } else {
            throw new IllegalArgumentException(String.format("No such jdk known: %s", desiredVersion))
        }
    }

    /**
     * Retrieve the Maven tool definition based upon desired version and platform.
     * For example, if you're on Linux and desiredVersion is 3.3.3, you get 'Linux Maven 3.3.3'.
     *
     * @param desiredVersion the desiredVersion of Maven (2.2.1, 3.2.1, 3.3.3)
     * @return the maven home for this version
     */
    String getMavenTool(String desiredVersion) {
        def maven
        if (steps.isUnix()) {
            maven = mavenLinuxMap.get(desiredVersion)
        } else {
            maven = mavenWindowsMap.get(desiredVersion)
        }

        if (maven) {
            def mavenTool = steps.tool name: maven, type: 'maven'
            return mavenTool
        } else {
            throw new IllegalArgumentException(String.format("No such maven known: %s", desiredVersion))
        }
    }

    /**
     * Uses the pipeline DSL to archive the given (Ant) file set in Jenkins.
     *
     * @param fileSet the Ant style list of files
     */
    void archiveFiles(String fileSet){
        assert fileSet: 'There is not fileSet present!'
        def fileSetArray = fileSet.split(';')

        /* for (String fileToArchive : fileSetArray){
         * have to use classic for loop, see: https://issues.jenkins-ci.org/browse/JENKINS-27421
         */
        for (int i =0; i <fileSetArray.size(); i++ ) {
            def fileToArchive = fileSetArray[i]
            println "fileToArchive=$fileToArchive"
            steps.archive fileToArchive
        }
        fileSetArray = null
    }

    /**
     * Checks if the value giving is among the split segments of the original string, split based on splitValue.
     *
     * @param stringToSplit the original string to split
     * @param splitValue the value with which to split the original string
     * @param valueToCheck the value to check for among the split segments
     * @return true if the splitted string contains a segment that is equal to the valueToCheck
     */
    @NonCPS
    boolean splittedStringContainsValue(String stringToSplit, String splitValue, String valueToCheck){
        assert stringToSplit: 'stringToSplit data is not present!'
        assert splitValue: 'splitValue data is not present!'
        assert valueToCheck: 'valueToCheck data is not present!'

        def list = ((String)stringToSplit).split(splitValue)
        for (int i =0; i <list.size(); i++ ) {
            if (list[i].equals(valueToCheck) ){
                return true
            }
        }
        return false
    }
}