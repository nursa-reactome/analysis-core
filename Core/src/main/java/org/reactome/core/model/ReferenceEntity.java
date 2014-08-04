package org.reactome.core.model;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * ReferenceEntity generated by hbm2java
 */
@XmlRootElement
public class ReferenceEntity extends DatabaseObject {
    
    private List<DatabaseIdentifier> crossReference;
    private String identifier;
    private List<String> name;
    private List<String> otherIdentifier;
    private ReferenceDatabase referenceDatabase;
    
    public ReferenceEntity() {
        
    }

    public List<DatabaseIdentifier> getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(List<DatabaseIdentifier> crossReference) {
        this.crossReference = crossReference;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<String> getOtherIdentifier() {
        return otherIdentifier;
    }

    public void setOtherIdentifier(List<String> otherIdentifier) {
        this.otherIdentifier = otherIdentifier;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }


}