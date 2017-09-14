package org.reactome.server.analysis.core.importer;

import org.reactome.server.analysis.core.Main;
import org.reactome.server.analysis.core.importer.query.InteractorsTargetQueryResult;
import org.reactome.server.analysis.core.model.*;
import org.reactome.server.analysis.core.model.identifier.MainIdentifier;
import org.reactome.server.analysis.core.model.resource.MainResource;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.model.resource.ResourceFactory;
import org.reactome.server.analysis.core.util.MapSet;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.interactors.database.InteractorsDatabase;
import org.reactome.server.interactors.exception.InvalidInteractionResourceException;
import org.reactome.server.interactors.model.Interaction;
import org.reactome.server.interactors.model.Interactor;
import org.reactome.server.interactors.model.InteractorResource;
import org.reactome.server.interactors.service.InteractionService;
import org.reactome.server.interactors.service.InteractorResourceService;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Component
public class InteractorsBuilder {

    private static final String splitter = ":";

    private static final String STATIC = "static";

    private AdvancedDatabaseObjectService ados = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);

    private InteractionService interactionService;

    //Will contain the RADIX-TREE with the map (identifiers -> [InteractorNode])
    private IdentifiersMap<InteractorNode> interactorsMap = new IdentifiersMap<>();

    //Keeps track of the number of interactors that have been included
    private int n = 0;

    public void build(Set<SpeciesNode> speciesNodes, EntitiesContainer entities, InteractorsDatabase interactorsDatabase) {
        String msgPrefix = "\rCreating the interactors container ";
        if (Main.VERBOSE) System.out.print(msgPrefix);

        this.interactionService = new InteractionService(interactorsDatabase);
        InteractorResourceService interactorResourceService = new InteractorResourceService(interactorsDatabase);

        Map<Long, InteractorResource> resourceMap;
        try {
            resourceMap = interactorResourceService.getAllMappedById();
        } catch (SQLException e) {
            System.err.println("Interactor Resource Map couldn't be loaded");
            return;
        }


        String query;
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("splitter", splitter);
        int s = 0;
        int st = speciesNodes.size();
        for (SpeciesNode species : speciesNodes) {
            if (Main.TEST_MAIN_SPECIES && !species.getTaxID().equals(Main.MAIN_SPECIES_TAX_ID)) continue;

            paramsMap.put("taxId", species.getTaxID());

            String speciesPrefix = "for '" + species.getName() + "' (" + (s++) + "/" + st + ")";
            if (Main.VERBOSE) System.out.print(msgPrefix + speciesPrefix + " >> retrieving targets for interactors...");

            query = "MATCH (:Species{taxId:{taxId}})<-[:species]-(p:Pathway)-[:hasEvent]->(rle:ReactionLikeEvent), " +
                    "      (rle)-[:input|output|catalystActivity|entityFunctionalStatus|physicalEntity|regulatedBy|regulator*]->(pe:PhysicalEntity)-[:referenceEntity]->(re:ReferenceEntity) " +
                    //"WHERE NOT (pe)-[:hasModifiedResidue]->(:TranslationalModification) " +
                    "WITH DISTINCT p, re, COLLECT(DISTINCT rle.dbId + {splitter} + rle.stId) AS rles " +
                    "RETURN DISTINCT re.databaseName AS databaseName, " +
                    "                CASE WHEN re.variantIdentifier IS NOT NULL THEN re.variantIdentifier ELSE re.identifier END AS identifier, " +
                    "                p.dbId AS pathway, " +
                    "                rles AS reactions";

            Collection<InteractorsTargetQueryResult> its;
            try {
                its = ados.customQueryForObjects(InteractorsTargetQueryResult.class, query, paramsMap);
            } catch (CustomQueryException e) {
                throw new RuntimeException(e);
            }

            int i = 0, tot = its.size();
            for (InteractorsTargetQueryResult target : its) {
                if (Main.VERBOSE && ++i % 25 == 0) {
                    System.out.print("\rRetrieving interactors for targets >> " + i + "/" + tot);
                }

                MainResource mr = (MainResource) ResourceFactory.getResource(target.getDatabaseName());
                AnalysisIdentifier ai = new AnalysisIdentifier(target.getIdentifier());
                MainIdentifier interactsWith = new MainIdentifier(mr, ai);

                String acc = target.getIdentifier();
                for (Interactor interactor : getInteractors(acc)) {
                    InteractorResource aux = resourceMap.get(interactor.getInteractorResourceId());
                    Resource resource = ResourceFactory.getResource(aux.getName());
                    InteractorNode interactorNode = getOrCreate(resource, interactor.getAcc());
                    interactorNode.addPathwayReactions(target.getPathwayReactions());
                    interactorNode.addInteractsWith(interactsWith);
                    interactorsMap.add(interactor.getAlias(), resource, interactorNode);
                    interactorsMap.add(interactor.getAliasWithoutSpecies(false), resource, interactorNode);
                    //for (String synonym : interactor.getSynonyms().split("\\$")) interactorsMap.add(synonym, resource, interactorNode);
                }
            }
        }
        if (Main.VERBOSE) System.out.println(msgPrefix + " >> " + n + " interactors successfully added to Reactome");
    }

    public IdentifiersMap<InteractorNode> getInteractorsMap() {
        return interactorsMap;
    }

    private List<Interactor> getInteractors(String acc) {
        List<Interactor> rtn = new ArrayList<>();
        List<Interaction> interactions;
        try {
            interactions = interactionService.getInteractions(acc, STATIC);
        } catch (InvalidInteractionResourceException | SQLException e) {
            return rtn;
        }
        for (Interaction interaction : interactions) {
            rtn.add(interaction.getInteractorB());
        }
        return rtn;
    }



    private InteractorNode getOrCreate(Resource resource, String identifier) {
        MapSet<Resource, InteractorNode> map = interactorsMap.get(identifier);
        Set<InteractorNode> interactors = map.getElements(resource);
        if (interactors == null || interactors.isEmpty()) {
            InteractorNode interactorNode = new InteractorNode(identifier);
            interactorsMap.add(identifier, resource, interactorNode);
            n++;
            return interactorNode;
        } else {
            //Using IdentifiersMap causes this "oddity" here, but is a minor inconvenient
            if (interactors.size() > 1)
                System.err.println("Interactors duplication. There should not be more than one interactor for " + identifier + " [" + resource.getName() + "]");
            return interactors.iterator().next();
        }
    }
}
