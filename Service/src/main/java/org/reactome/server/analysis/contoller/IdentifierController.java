package org.reactome.server.analysis.contoller;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.reactome.server.analysis.helper.AnalysisHelper;
import org.reactome.server.analysis.model.AnalysisResult;
import org.reactome.server.components.analysis.model.UserData;
import org.reactome.server.components.analysis.util.InputUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(value = "Queries for only one identifier")
@RequestMapping(value = "/identifier")
public class IdentifierController {

    @Autowired
    private AnalysisHelper controller;

    @ApiOperation(value = "Analise the identifier over the different species in the database and projects the result to Homo Sapiens", notes = "")
    @RequestMapping(value = "/{id}/projection", method = RequestMethod.GET , produces = "application/json")
    @ResponseBody
    public AnalysisResult getIdentifierToHuman( @ApiParam(name = "id", required = true, value = "The identifier of the element to be retrieved")
                                              @PathVariable String id,
                                               @ApiParam(name = "pageSize", value = "pathways per page", defaultValue = "20")
                                              @RequestParam(required = false) Integer pageSize,
                                               @ApiParam(name = "page", value = "page number", defaultValue = "1")
                                              @RequestParam(required = false) Integer page,
                                               @ApiParam(name = "sortBy", value = "how to sort the result", required = false, defaultValue = "ENTITIES_PVALUE", allowableValues = "NAME,TOTAL_ENTITIES,TOTAL_REACTIONS,FOUND_ENTITIES,FOUND_REACTIONS,ENTITIES_RATIO,ENTITIES_PVALUE,ENTITIES_FDR,REACTIONS_RATIO")
                                              @RequestParam(required = false) String sortBy,
                                               @ApiParam(name = "order", value = "specifies the order", required = false, defaultValue = "ASC", allowableValues = "ASC,DESC")
                                              @RequestParam(required = false) String order,
                                               @ApiParam(name = "resource", value = "resource to sort", required = false, defaultValue = "TOTAL", allowableValues = "TOTAL,UNIPROT,ENSEMBL,CHEBI,NCBI_PROTEIN,EMBL,COMPOUND")
                                              @RequestParam(required = false, defaultValue = "TOTAL") String resource) {
        UserData ud = InputUtils.getUserData(id);
        return controller.analyse(ud, true).getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Analise the identifier over the different species in the database")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET , produces = "application/json")
    @ResponseBody
    public AnalysisResult getIdentifier( @ApiParam(name = "id" , required = true, value = "The identifier of the element to be retrieved")
                                       @PathVariable String id,
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
        UserData ud = InputUtils.getUserData(id);
        return controller.analyse(ud, false).getResultSummary(sortBy, order, resource, pageSize, page);
    }
}
