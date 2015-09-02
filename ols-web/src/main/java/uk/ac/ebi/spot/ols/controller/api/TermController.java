package uk.ac.ebi.spot.ols.controller.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import springfox.documentation.annotations.ApiIgnore;
import uk.ac.ebi.spot.ols.model.OntologyDocument;
import uk.ac.ebi.spot.ols.neo4j.model.Term;
import uk.ac.ebi.spot.ols.neo4j.service.OntologyTermGraphService;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author Simon Jupp
 * @date 23/06/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Controller
@Api(value = "Terms", description = "Ontology terms API", position = 2)
@RequestMapping("/api/ontology")
public class TermController {

    @Autowired
    private OntologyTermGraphService ontologyTermGraphService;

    @Autowired TermAssembler termAssembler;

    @ApiOperation(value = "Find ontology term", notes = "Returns a term from the specified ontology with specified ID")
    @RequestMapping(path = "{onto}/terms", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> terms(
             @PathVariable("onto") String ontologyId,
             @ApiParam(value = "iri", name = "iri") @RequestParam(value = "iri", required = false) String iri,
            @RequestParam(value = "short_form", required = false) String shortForm,
            @RequestParam(value = "obo_id", required = false) String oboId,
            @ApiIgnore()Pageable pageable,
             @ApiIgnore() PagedResourcesAssembler assembler) {

        Page<Term> terms = null;

        ontologyId = ontologyId.toLowerCase();
        if (iri != null) {
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, iri);
            if (term != null) {
                terms =  new PageImpl<Term>(Arrays.asList(term));
            }
        }
        else if (shortForm != null) {
            Term term = ontologyTermGraphService.findByOntologyAndShortForm(ontologyId, shortForm);
            if (term != null) {
                terms =  new PageImpl<Term>(Arrays.asList(term));
            }
        }
        else if (oboId != null) {
            Term term = ontologyTermGraphService.findByOntologyAndOboId(ontologyId, oboId);
            if (term != null) {
                terms =  new PageImpl<Term>(Arrays.asList(term));
            }
        }
        else {
            terms = ontologyTermGraphService.findAllByOntology(ontologyId, pageable);
        }

        return new ResponseEntity<>( assembler.toResource(terms, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/roots", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getRoots(
            @PathVariable("onto") String ontologyId,
            Pageable pageable,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        Page<Term> roots = ontologyTermGraphService.getRoots(ontologyId, pageable);
        return new ResponseEntity<>( assembler.toResource(roots, termAssembler), HttpStatus.OK);
    }

    @RequestMapping(path = "/{onto}/terms/{id}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<Resource<Term>> getTerm(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId) throws ResourceNotFoundException {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Term term = ontologyTermGraphService.findByOntologyAndIri(ontologyId, decoded);
            return new ResponseEntity<>( termAssembler.toResource(term), HttpStatus.OK);
        } catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/parents", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> getParents(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> parents = ontologyTermGraphService.getParents(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(parents, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/children", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> children(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                              PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> children = ontologyTermGraphService.getChildren(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(children, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/descendants", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> descendants(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                                 PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> descendants = ontologyTermGraphService.getDescendants(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(descendants, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/ancestors", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> ancestors(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, Pageable pageable,
                                               PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");
            Page<Term> ancestors = ontologyTermGraphService.getAncestors(ontologyId, decoded, pageable);
            return new ResponseEntity<>( assembler.toResource(ancestors, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/{onto}/terms/{id}/jstree", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<String> graphJsTree(
            @PathVariable("onto") String ontologyId,
            @PathVariable("id") String termId,
            @RequestParam(value = "siblings", defaultValue = "false", required = false) boolean siblings) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decoded = UriUtils.decode(termId, "UTF-8");

            Object object= ontologyTermGraphService.getJsTree(ontologyId, decoded, siblings);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return new HttpEntity<String>(ow.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(path = "/{onto}/terms/{id}/{relation}", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<Term>> related(@PathVariable("onto") String ontologyId, @PathVariable("id") String termId, @PathVariable("relation") String relation, Pageable pageable,
                                             PagedResourcesAssembler assembler) {
        ontologyId = ontologyId.toLowerCase();

        try {
            String decodedTerm = UriUtils.decode(termId, "UTF-8");
            String decodedRelation = UriUtils.decode(relation, "UTF-8");
            Page<Term> related = ontologyTermGraphService.getRelated(ontologyId, decodedTerm, decodedRelation, pageable);

            return new ResponseEntity<>( assembler.toResource(related, termAssembler), HttpStatus.OK);
        }
        catch (UnsupportedEncodingException e) {
            throw new ResourceNotFoundException();
        }
    }

    private String decode (String iri) throws UnsupportedEncodingException {
        return UriUtils.decode(iri, "UTF-8");
    }


}