package org.reactome.server.analysis.core.result;

import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.model.identifier.Identifier;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.result.exception.DataFormatException;
import org.reactome.server.analysis.core.result.model.*;
import org.reactome.server.analysis.core.util.MapSet;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class AnalysisStoredResult {
    private static final Integer PAGE_SIZE = 20;

    private AnalysisSummary summary;
    private List<String> warnings;
    private Set<AnalysisIdentifier> notFound;
    private List<PathwayNodeSummary> pathways;
    private List<ResourceSummary> resourceSummary;
    private ExpressionSummary expressionSummary;

    public AnalysisStoredResult(UserData userData, HierarchiesData data){
        this.warnings = userData.getWarningMessages();
        this.notFound = data.getNotFound();
        this.pathways = new LinkedList<>();
        this.expressionSummary = new ExpressionSummary(userData);
    }

    public void setHitPathways(List<PathwayNode> pathwayNodes){
        //At the time we set the hit pathways, we also initialize resource summary
        Map<String, Integer> aux = new HashMap<>();
        Integer total = 0;
        for (PathwayNode pathwayNode : pathwayNodes) {
            total++;
            for (MainResource mainResource : pathwayNode.getPathwayNodeData().getResources()) {
                PathwayNodeData data = pathwayNode.getPathwayNodeData();
                if (data.getEntitiesFound(mainResource) > 0 || data.getInteractorsFound(mainResource) > 0) {
                    Integer n = aux.get(mainResource.getName());
                    aux.put(mainResource.getName(), n == null ? 1 : n + 1 );
                }
            }
            this.pathways.add(new PathwayNodeSummary(pathwayNode));
        }
        resourceSummary = new LinkedList<>();
        for (String resource : aux.keySet()) {
            resourceSummary.add(new ResourceSummary(resource, aux.get(resource)));
        }
        Collections.sort(resourceSummary, Collections.reverseOrder());
        //Total is always inserted on top
        resourceSummary.add(0, new ResourceSummary("TOTAL", total));
    }

    public void setSummary(AnalysisSummary summary) {
        this.summary = summary;
    }

    public ExpressionSummary getExpressionSummary() {
        return expressionSummary;
    }

    public FoundElements getFoundElmentsForPathway(String pathway, String resource){
        PathwayNodeSummary aux = getPathway(pathway);
        if (aux != null) {
            List<String> columnNames = getExpressionSummary().getColumnNames();
            FoundEntities identifiers = (new FoundEntities(aux, columnNames)).filter(resource);
            FoundInteractors interactors =(new FoundInteractors(aux, columnNames)).filter(resource);
            if (identifiers != null ) {
                return new FoundElements(pathway, identifiers, interactors, columnNames);
            }
        }
        return null;
    }

    public List<FoundElements> getFoundElmentsForPathways(List<String> pathways, String resource){
        List<FoundElements> rtn = new ArrayList<>();
        for (String pathway : pathways) {
            PathwayNodeSummary aux = getPathway(pathway);
            if (aux != null) {
                List<String> columnNames = getExpressionSummary().getColumnNames();
                FoundEntities identifiers = (new FoundEntities(aux, columnNames)).filter(resource);
                FoundInteractors interactors = (new FoundInteractors(aux, columnNames)).filter(resource);
                if (identifiers != null) {
                    rtn.add(new FoundElements(pathway, identifiers, interactors, columnNames));
                }
            }
        }
        return rtn;
    }

    public FoundEntities getFoundEntities(String pathway){
        FoundEntities pi = null;
        PathwayNodeSummary aux = getPathway(pathway);
        if (aux != null) {
            List<String> columnNames = getExpressionSummary().getColumnNames();
            pi = (new FoundEntities(aux, columnNames));
        }
        return pi;
    }

    public FoundInteractors getFoundInteractors(String pathway){
        FoundInteractors pi = null;
        PathwayNodeSummary aux = getPathway(pathway);
        if (aux != null) {
            List<String> columnNames = getExpressionSummary().getColumnNames();
            pi = new FoundInteractors(aux, columnNames);
        }
        return pi;
    }

    public Set<AnalysisIdentifier> getAnalysisIdentifiers(){
        Set<AnalysisIdentifier> rtn = new HashSet<>();
        for (Identifier identifier : getFoundEntitiesMap().keySet()) {
            rtn.add(identifier.getValue());
        }
        return rtn;
    }

    public MapSet<Identifier, MainIdentifier> getFoundEntitiesMap() {
        MapSet<Identifier, MainIdentifier> rtn = new MapSet<Identifier, MainIdentifier>();
        for (PathwayNodeSummary pathway : this.pathways) {
            rtn.addAll(pathway.getData().getIdentifierMap());
        }
        return rtn;
    }

    public MapSet<Identifier, MainIdentifier> getFoundEntitiesMap(MainResource mainResource) {
        MapSet<Identifier, MainIdentifier> rtn = new MapSet<>();

        MapSet<Identifier, MainIdentifier> aux = getFoundEntitiesMap();
        for (Identifier identifier : aux.keySet()) {
            for (MainIdentifier mainIdentifier : aux.getElements(identifier)) {
                if(mainIdentifier.getResource().equals(mainResource)){
                    rtn.add(identifier, mainIdentifier);
                }
            }
        }
        return rtn;
    }

    public Set<Long> getFoundReactions(String pathwayId, String resource){
        Set<Long> rtn = new HashSet<>();
        if(resource.toUpperCase().equals("TOTAL")){
            for (PathwayNodeSummary pathway : this.pathways) {
                if(pathway.is(pathwayId)){
                    for (AnalysisReaction reaction : pathway.getData().getReactions()) {
                        rtn.add(reaction.getDbId());
                    }
                }
            }
        }else{
            Resource r = ResourceFactory.getResource(resource);
            if(r instanceof MainResource){
                MainResource mainResource = (MainResource) r;
                for (PathwayNodeSummary pathway : this.pathways) {
                    if(pathway.is(pathwayId)){
                        for (AnalysisReaction reaction : pathway.getData().getReactions(mainResource)) {
                            rtn.add(reaction.getDbId());
                        }
                    }
                }
            }
        }
        return rtn;
    }

    public Set<Long> getFoundReactions(List<String> pathwayIds, String resource){
        Set<Long> rtn = new HashSet<>();
        if(resource.toUpperCase().equals("TOTAL")){
            for (PathwayNodeSummary pathway : this.pathways) {
                if(pathway.in(pathwayIds)){
                    for (AnalysisReaction reaction : pathway.getData().getReactions()) {
                        rtn.add(reaction.getDbId());
                    }
                }
            }
        }else{
            Resource r = ResourceFactory.getResource(resource);
            if(r instanceof MainResource){
                MainResource mainResource = (MainResource) r;
                for (PathwayNodeSummary pathway : this.pathways) {
                    if(pathway.in(pathwayIds)){
                        for (AnalysisReaction reaction : pathway.getData().getReactions(mainResource)) {
                            rtn.add(reaction.getDbId());
                        }
                    }
                }
            }
        }
        return rtn;
    }

    public Set<AnalysisIdentifier> getNotFound() {
        return notFound;
    }

    public List<IdentifierSummary> getNotFoundIdentifiers(){
        List<IdentifierSummary> notFound = new LinkedList<>();
        for (AnalysisIdentifier identifier : getNotFound()) {
            notFound.add(new IdentifierSummary(identifier));
        }
        return notFound;
    }

    public PathwayNodeSummary getPathway(String identifier){
        for (PathwayNodeSummary nodeSummary : this.pathways) {
            if(nodeSummary.is(identifier)){
                return nodeSummary;
            }
        }
        return null;
    }

    public List<PathwayNodeSummary> getPathways() {
        return pathways;
    }

    public int getPage(String pathwayId, String sortBy, String order, String resource, Integer pageSize){
        this.filterPathwaysByResource(resource);
        Collections.sort(this.pathways, getComparator(sortBy, order, resource));
        if(pageSize==null) pageSize = PAGE_SIZE;
        for (int i = 0; i < this.pathways.size(); i++) {
            PathwayNodeSummary pathway = this.pathways.get(i);
            if(pathway.is(pathwayId)){
                return ((int) Math.floor(i/pageSize)) + 1;
            }
        }
        return -1;
    }

    public List<ResourceSummary> getResourceSummary() {
        return resourceSummary;
    }

    public AnalysisResult getResultSummary(String resource) {
        return getResultSummary(null, "ASC", resource, null, null);
    }

    public AnalysisResult getResultSummary(String sortBy, String order, String resource, Integer pageSize, Integer page){
        this.filterPathwaysByResource(resource);
        Collections.sort(this.pathways, getComparator(sortBy, order, resource));
        if(pageSize==null) pageSize = PAGE_SIZE;
        List<PathwaySummary> rtn = new LinkedList<>();
        if(page!=null && page>0){ // && this.pathways.size()>(pageSize*(page-1))){
            int end = (pageSize * page) > this.pathways.size()? this.pathways.size() : (pageSize * page);
            for(int i = pageSize*(page-1); i<end; ++i){
                PathwayNodeSummary pathwayNodeSummary = this.pathways.get(i);
                rtn.add(new PathwaySummary(pathwayNodeSummary, resource.toUpperCase(), summary.isInteractors()));
            }
        }else{
            for (PathwayNodeSummary pathway : this.pathways) {
                rtn.add(new PathwaySummary(pathway, resource.toUpperCase(), summary.isInteractors()));
            }
        }
        return new AnalysisResult(this, rtn);
    }

    public AnalysisSummary getSummary() {
        return summary;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<PathwaySummary> filterByPathways(List<String> pathwayIds, String resource){
        this.filterPathwaysByResource(resource);
        List<PathwaySummary> rtn = new LinkedList<>();
        for (PathwayNodeSummary pathway : this.pathways) {
            if(pathway.in(pathwayIds)){
                rtn.add(new PathwaySummary(pathway, resource.toUpperCase(), summary.isInteractors()));
            }
        }
        return rtn;
    }

    public SpeciesFilteredResult filterBySpecies(Long speciesId, String resource) {
        return filterBySpecies(speciesId, resource, null, "ASC");
    }

    public SpeciesFilteredResult filterBySpecies(Long speciesId, String resource, String sortBy, String order){
        if (resource != null) {
            Resource r = ResourceFactory.getResource(resource);

            this.filterPathwaysByResource(resource);
            Collections.sort(this.pathways, getComparator(sortBy, order, resource));
            List<PathwayBase> rtn = new LinkedList<>();
            Double min = null, max = null;

            for (PathwayNodeSummary pathway : this.pathways) {
                if (pathway.getSpecies().getSpeciesID().equals(speciesId)) {
                    PathwaySummary aux = new PathwaySummary(pathway, resource.toUpperCase(), summary.isInteractors());
                    rtn.add(new PathwayBase(aux));

                    List<Double> exps = new LinkedList<>();
                    if (r instanceof MainResource) {
                        exps = pathway.getData().getExpressionValuesAvg((MainResource) r);
                    } else if (resource.equals("TOTAL")) {
                        exps = pathway.getData().getExpressionValuesAvg();
                    }

                    for (Double exp : exps) {
                        if (min == null || exp < min) {
                            min = exp;
                        } else if (max == null || exp > max) {
                            max = exp;
                        }
                    }
                }
            }
            ExpressionSummary summary;
            if (this.expressionSummary == null) {
                summary = null;
            } else {
                summary = new ExpressionSummary(this.expressionSummary.getColumnNames(), min, max);
            }
            return new SpeciesFilteredResult(this.summary.getType(), summary, rtn);
        }
        throw new DataFormatException("Resource is null");
    }

    private Comparator<PathwayNodeSummary> getComparator(String sortBy, String order, String resource){
        AnalysisSortType sortType = AnalysisSortType.getSortType(sortBy);
        if (resource != null) {
            Resource r = ResourceFactory.getResource(resource);
            if (r != null && r instanceof MainResource) {
                MainResource mr = (MainResource) r;
                if (order != null && order.toUpperCase().equals("DESC")) {
                    return Collections.reverseOrder(ComparatorFactory.getComparator(sortType, mr));
                } else {
                    return ComparatorFactory.getComparator(sortType, mr);
                }
            }
        }
        if(order!=null && order.toUpperCase().equals("DESC")){
            return Collections.reverseOrder(ComparatorFactory.getComparator(sortType));
        }else{
            return ComparatorFactory.getComparator(sortType);
        }
    }

    private void filterPathwaysByResource(String resource){
        if(!resource.toUpperCase().equals("TOTAL")){
            List<PathwayNodeSummary> rtn = new LinkedList<>();
            Resource r = ResourceFactory.getResource(resource);
            if(r instanceof MainResource){
                MainResource mr = (MainResource) r;
                for (PathwayNodeSummary pathway : this.pathways) {
                    if (pathway.getData().getEntitiesFound(mr) > 0 || pathway.getData().getInteractorsFound(mr) > 0) {
                        rtn.add(pathway);
                    }
                }
            }
            this.pathways = rtn;
        }
    }
}
