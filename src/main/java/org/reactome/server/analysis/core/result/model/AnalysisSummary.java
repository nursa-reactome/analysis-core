package org.reactome.server.analysis.core.result.model;

import org.reactome.server.analysis.core.model.AnalysisType;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class AnalysisSummary {
    private String token;
    private Boolean projection;
    private Boolean interactors;
    private String type;
    private String sampleName;
    private Long species;
    private boolean text = false;
    private String fileName;

    AnalysisSummary(String token, Boolean projection, Boolean interactors, String sampleName, AnalysisType type) {
        this.token = token;
        this.type = type.toString();
        this.sampleName = sampleName;
        this.projection = projection;
        this.interactors = interactors;
    }

    public AnalysisSummary(String token, Boolean projection, Boolean interactors, String sampleName, AnalysisType type, String fileName) {
        this(token, projection, interactors, sampleName, type);
        this.fileName = fileName;
    }

    public AnalysisSummary(String token, Boolean projection, Boolean interactors, String sampleName, AnalysisType type, Long species) {
        this(token, projection, interactors, sampleName, type);
        this.species = species;
    }

    public AnalysisSummary(String token, Boolean projection, Boolean interactors, String sampleName, AnalysisType type, boolean text) {
        this(token, projection, interactors, sampleName, type);
        this.text = text;
    }

    public String getToken() {
        return token;
    }

    public Boolean isProjection() {
        return projection;
    }

    public Boolean isInteractors() {
        return interactors;
    }

    public String getType() {
        return type;
    }

    public String getSampleName() {
        return sampleName;
    }

    public Long getSpecies() {
        return species;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isText() {
        return text;
    }
}
