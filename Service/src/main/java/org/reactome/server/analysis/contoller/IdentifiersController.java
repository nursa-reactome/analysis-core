package org.reactome.server.analysis.contoller;

import com.wordnik.swagger.annotations.*;
import org.reactome.server.analysis.helper.AnalysisHelper;
import org.reactome.server.analysis.model.AnalysisResult;
import org.reactome.server.components.analysis.model.UserData;
import org.reactome.server.components.analysis.util.InputUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
@Controller
@Api(value = "Queries for multiple identifiers")
@RequestMapping(value = "/identifiers")
public class IdentifiersController {

    @Autowired
    private AnalysisHelper controller;

    @ApiOperation(value = "Analise the post identifiers over the different species and projects the result to Homo Sapiens")
    @RequestMapping(value = "/projection", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostTextToHuman( @ApiParam(name = "input", required = true, value = "Identifiers to analyse followed by their expression (when applies)")
                                            @RequestBody String input,
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
        UserData ud = InputUtils.getUserData(input);
        return controller.analyse(ud, true).getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Analise the post identifiers over the different species")
    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostText( @ApiParam(name = "input", required = true, value = "Identifiers to analyse followed by their expression (when applies)")
                                     @RequestBody String input,
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
        UserData ud = InputUtils.getUserData(input);
        return controller.analyse(ud, false).getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Analise the identifiers in the file over the different species and projects the result to Homo Sapiens") //, consumes = "multipart/form-data")
    @ApiErrors(value = {@ApiError(code = 415, reason = "Unsupported Media Type" )})
//    @ApiResponses({@ApiResponse( code = 415, message = "Unsupported Media Type" ) })
    @RequestMapping(value = "/form/projection", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostFileToHuman( @ApiParam(name = "file", required = true, value = "A file with the data to be analysed")
                                            @RequestParam(required = true) MultipartFile file,
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
        UserData ud = controller.getUserData(file);
        return controller.analyse(ud, true, file.getOriginalFilename()).getResultSummary(sortBy, order, resource, pageSize, page);
    }

    @ApiOperation(value = "Analise the identifiers in the file over the different species") //, consumes = "multipart/form-data")
    @ApiErrors(value = {@ApiError(code = 415, reason = "Unsupported Media Type" )})
//    @ApiResponses({@ApiResponse( code = 415, message = "Unsupported Media Type" ) })
    @RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public AnalysisResult getPostFile( @ApiParam(name = "file", required = true, value = "A file with the data to be analysed")
                                     @RequestParam(required = true) MultipartFile file,
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
        UserData ud = controller.getUserData(file);
        return controller.analyse(ud, false, file.getOriginalFilename()).getResultSummary(sortBy, order, resource, pageSize, page);
    }
}
