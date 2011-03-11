package org.coode.owlapi.rdfxml.parser;

import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 08-Dec-2006<br><br>
 * <p>
 * Handles rdfs:subClassOf triples.  If handling is set to strict then the triple is only consumed if
 * the subject and object are typed as classes.
 */
public class TPSubClassOfHandler extends TriplePredicateHandler {


    public TPSubClassOfHandler(OWLRDFConsumer consumer) {
        super(consumer, OWLRDFVocabulary.RDFS_SUBCLASS_OF.getIRI());
    }

    @Override
    public boolean canHandle(IRI subject, IRI predicate, IRI object) {
        return super.canHandle(subject, predicate, object) && isTyped(subject, predicate, object);
    }

    private boolean isTyped(IRI subject, IRI predicate, IRI object) {
        return getConsumer().isClassExpression(subject) && getConsumer().isClassExpression(object);
    }

    @Override
    public boolean canHandleStreaming(IRI subject, IRI predicate, IRI object) {
        getConsumer().addClassExpression(subject, false);
        getConsumer().addClassExpression(object, false);
        return !isStrict() && !isSubjectOrObjectAnonymous(subject, object);
    }


    @Override
    public void handleTriple(IRI subject, IRI predicate, IRI object) throws UnloadableImportException {
        if(isStrict()) {
            if(isClassExpressionStrict(subject) && isClassExpressionStrict(object)) {
                translate(subject, predicate, object);
            }
        }
        else {
            if(isClassExpressionLax(subject) && isClassExpressionLax(object)) {
                translate(subject, predicate, object);
            }
        }
    }

    private void translate(IRI subject, IRI predicate, IRI object) {
        OWLClassExpression subClass = translateClassExpression(subject);
        OWLClassExpression supClass = translateClassExpression(object);
        Set<OWLAnnotation> pendingAnnotations = getConsumer().getPendingAnnotations();
        OWLAxiom ax = getDataFactory().getOWLSubClassOfAxiom(subClass, supClass, pendingAnnotations);
        addAxiom(ax);
        consumeTriple(subject, predicate, object);
    }
}
