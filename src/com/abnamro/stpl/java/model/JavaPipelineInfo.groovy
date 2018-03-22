package com.abnamro.stpl.java.model

import com.abnamro.stpl.util.Utilities

/**
 * Container class for information of the pipeline.
 */
class JavaPipelineInfo implements Serializable {
    String scmType
    String builderCredentialsId
    boolean isMainlineBuild
    Utilities utilities
    def steps

    /**
     * Creates a new java pipeline info class.
     * @param steps the groovy dsl context
     * @param utilities the core Utilities class instance
     * @param scmType the scmType
     */
    JavaPipelineInfo(steps, Utilities utilities, String scmType){
        this.steps = steps
        this.utilities = utilities
        this.scmType = scmType
    }
}
