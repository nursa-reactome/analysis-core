package org.reactome.server.analysis.contoller;

import com.wordnik.swagger.annotations.*;
import org.reactome.server.analysis.exception.ResourceNotFoundException;
import org.reactome.server.analysis.helper.AnalysisHelper;
import org.reactome.server.analysis.model.*;
import org.reactome.server.analysis.result.AnalysisStoredResult;
import org.reactome.server.analysis.result.PathwayNodeSummary;
import org.reactome.server.components.analysis.model.AnalysisIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(value = "Previous queries filter")
@RequestMapping(value = "/token")
public class TokenController {

    @Autowired
    private AnalysisHelper controller;

    @ApiOperation(value = "Returns the result associated with the token")
    @ApiErrors(value = {@ApiError(code = 404, reason = "No result corresponding to the token was found" )})
//    @ApiResponses({@ApiResponse( code = 404, message = "No result corresponding to the token was found" ) })
    @RequestMapping(value = "/{token}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public AnalysisResult getToken( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                  @PathVariable String token,
//                                   @ApiParam(name = "resource", value = "focused resource", defaultValue = "TOTAL")
//                                 @PathVariable String resource,
                                   @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                  @RequestParam(required = false) Integer pageSize,
                                   @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                  @RequestParam(required = false) Integer page,
                                    @ApiParam(name = "sortBy", value = "how to sort the result", required = false, defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                  @RequestParam(required = false) String sortBy,
                                    @ApiParam(name = "order", value = "specifies the order", required = false, defaultValue = "ASC", allowableValues = "ASC,DESC")
                                  @RequestParam(required = false) String order,
                                    @ApiParam(name = "resource", value = "the resource to sort", required = false, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                  @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return controller.getFromToken(token).getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Returns the result for the pathway ids sent by post (when they are present in the original result)")
    @ApiErrors(value = {@ApiError(code = 404, reason = "No result corresponding to the token was found" )})
//    @ApiResponses({@ApiResponse( code = 404, message = "No result corresponding to the token was found" ) })
    @RequestMapping(value = "/{token}/filter/pathways", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<PathwaySummary> getTokenFilterPathways( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                @PathVariable String token,
                                                 @ApiParam(name = "input", required = true, value = "A comma separated list with the identifiers of the pathways of interest (NOTE: is plain text, not json)")
                                                @RequestBody(required = true) String input,
                                                 @ApiParam(name = "resource", value = "the resource to sort", required = false, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                                @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        List<Long> pathwayIds = controller.getInputIds(input);
        return controller.getFromToken(token).filterByPathways(pathwayIds, resource);
    }

    @ApiOperation(value = "Returns the page where the corresponding pathway is taking into account the passed parameters")
    @ApiErrors(value = {@ApiError(code = 404, reason = "No result corresponding to the token was found" )})
//    @ApiResponses({@ApiResponse( code = 404, message = "No result corresponding to the token was found" ) })
    @RequestMapping(value = "/{token}/page/{pathway}", method = RequestMethod.GET , produces = "application/json")
    @ResponseBody
    public int getPageOfPathway( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                @PathVariable String token,
                                 @ApiParam(name = "pathway", required = true, value = "The database identifier of the pathway of interest")
                                @PathVariable Long pathway,
                                 @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                @RequestParam(required = false) Integer pageSize,
                                 @ApiParam(name = "sortBy", value = "how to sort the result", required = false, defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                @RequestParam(required = false) String sortBy,
                                 @ApiParam(name = "order", value = "specifies the order", required = false, defaultValue = "ASC", allowableValues = "ASC,DESC")
                                @RequestParam(required = false) String order,
                                 @ApiParam(name = "resource", value = "the resource to sort", required = false, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                 @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        return controller.getFromToken(token).getPage(pathway, sortBy, order, resource, pageSize);
    }

    @ApiOperation(value = "Returns a summary of the contained identifiers for a pathway in the result for a given token")
    @ApiErrors(value = {@ApiError(code = 404, reason = "No result corresponding to the token was found" )})
//    @ApiResponses({@ApiResponse( code = 404, message = "No result corresponding to the token was found" ) })
    @RequestMapping(value = "/{token}/summary/{pathway}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public PathwayIdentifiers getTokenSummaryPathway( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                     @PathVariable String token,
                                                      @ApiParam(name = "pathway", required = true, value = "The dbId of the pathway of interest")
                                                     @PathVariable Long pathway,
                                                      @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                                     @RequestParam(required = false) Integer page,
                                                      @ApiParam(name = "pageSize", value = "identifiers per page", defaultValue = "20")
                                                     @RequestParam(required = false) Integer pageSize,
                                                      @ApiParam(name = "resource", value = "the resource to sort", required = false, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                                     @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        AnalysisStoredResult result = controller.getFromToken(token);
        PathwayNodeSummary aux = result.getPathway(pathway);
        if(aux!=null){
            List<String> columnNames = result.getExpressionSummary().getColumnNames();
            PathwayIdentifiers pi = (new PathwayIdentifiers(aux, columnNames)).filter(resource, pageSize, page);
            if(pi!=null){
                return pi;
            }
        }
        throw new ResourceNotFoundException();
    }

    @ApiOperation(value = "Returns a list of the identifiers not found for a given token")
    @ApiErrors(value = {@ApiError(code = 404, reason = "No result corresponding to the token was found" )})
//    @ApiResponses({@ApiResponse( code = 404, message = "No result corresponding to the token was found" ) })
    @RequestMapping(value = "/{token}/notFound", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<IdentifierSummary> getNotFoundIdentifiers( @ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                                          @PathVariable String token,
                                                           @ApiParam(name = "pageSize", value = "identifiers per page")
                                                          @RequestParam(required = false) Integer pageSize,
                                                           @ApiParam(name = "page", value = "page number")
                                                          @RequestParam(required = false) Integer page) {
        List<IdentifierSummary> notFound = new LinkedList<IdentifierSummary>();
        for (AnalysisIdentifier identifier : controller.getFromToken(token).getNotFound()) {
            notFound.add(new IdentifierSummary(identifier));
        }
        if(pageSize!=null && page!=null){
            pageSize = pageSize < 0 ? 0 : pageSize;
            page = page < 0 ? 0 : page;
            int from = pageSize * (page - 1);
            if(from < notFound.size() && from > -1){
                int to = from + pageSize;
                to = to > notFound.size() ? notFound.size() : to;
                return notFound.subList(from, to);
            }else{
                return new LinkedList<IdentifierSummary>();
            }
        }else{
            return notFound;
        }
    }

    @ApiOperation(value = "Returns the resources summary associated with the token")
    @ApiErrors(value = {@ApiError(code = 404, reason = "No result corresponding to the token was found" )})
//    @ApiResponses({@ApiResponse( code = 404, message = "No result corresponding to the token was found" ) })
    @RequestMapping(value = "/{token}/resources", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ResourceSummary> getResources(@ApiParam(name = "token", required = true, value = "The token associated with the data to query")
                                              @PathVariable String token) {
        return this.controller.getFromToken(token).getResourceSummary();
    }
}
