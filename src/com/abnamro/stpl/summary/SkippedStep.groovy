package com.abnamro.stpl.summary

/**
 * Class for keeping tabs on skipped steps in the pipeline.
 */
class SkippedStep implements Serializable {
    private String name
    private String message

    /**
     * Creates a new SkippedStep.
     * @param name the name of the step that was skipped
     * @param message the reasoning why the step was skipped
     */
    SkippedStep(String name, String message) {
        this.name = name
        this.message = message
    }


    @Override
    String toString() {
        return "<li>" +
                "<b>" + name + '</b>: ' +
                message + '</li>'
    }
}
