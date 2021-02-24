package it.cnr.igag.italgas.config.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.igag.italgas.domain.AbstractAuditingEntity;
import it.cnr.igag.italgas.domain.EntityAuditEvent;
import it.cnr.igag.italgas.repository.EntityAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Async Entity Audit Event writer
 * This is invoked by Hibernate entity listeners to write audit event for entitities
 */
@Component
public class AsyncEntityAuditEventWriter {

    private final Logger log = LoggerFactory.getLogger(AsyncEntityAuditEventWriter.class);

    @Inject
    private EntityAuditEventRepository auditingEntityRepository;

    @Inject
    private ObjectMapper objectMapper; //Jackson object mapper

    /**
     * Writes audit events to DB asynchronously in a new thread
     */
    @Async
    public void writeAuditEvent(Object target, EntityAuditAction action) {
        log.debug("-------------- Post {} audit  --------------", action.value());
        try {
            EntityAuditEvent auditedEntity = prepareAuditEntity(target, action);
            if (auditedEntity != null) {
                auditingEntityRepository.save(auditedEntity);
            }
        } catch (Exception e) {
            log.error("Exception while persisting audit entity for {} error: {}", target, e);
        }
    }

    /**
     * Method to prepare auditing entity
     *
     * @param entity
     * @param action
     * @return
     */
    private EntityAuditEvent prepareAuditEntity(final Object entity, EntityAuditAction action) {
        EntityAuditEvent auditedEntity = new EntityAuditEvent();
        Class<?> entityClass = entity.getClass(); // Retrieve entity class with reflection
        
        // ============================================================================================
        // annullo tutti i blob: sia eternamente dannato:
        //  - chi li ha inventati,
        //  - chi ha deciso di supportarli in JHipster
        //  - chi ha creato Jhipster, Java ed Internet
        //  - chi ha creato generator-jhipster-entity-audit senza testarlo con i blob
        //  - chi ha deciso di usare JHipster (io)
        if(entity instanceof it.cnr.igag.italgas.domain.Consegna) {
        	log.trace("E' una consegna");
        	it.cnr.igag.italgas.domain.Consegna consegna = (it.cnr.igag.italgas.domain.Consegna) entity;
        	consegna.setProtocolloAccettazione(null);
        }
        if(entity instanceof it.cnr.igag.italgas.domain.Cassetta) {
        	log.trace("E' una cassetta");
        	it.cnr.igag.italgas.domain.Cassetta cassetta = (it.cnr.igag.italgas.domain.Cassetta) entity;
        	cassetta.setFotoContenitore(null);
        	cassetta.setFotoContenuto(null);
        	cassetta.setCertificatoPdf(null);
        	cassetta.getConsegna().setProtocolloAccettazione(null);
        }
        if(entity instanceof it.cnr.igag.italgas.domain.Campione) {
        	log.trace("E' un campione");
        	it.cnr.igag.italgas.domain.Campione campione = (it.cnr.igag.italgas.domain.Campione) entity;
        	campione.setFotoSacchetto(null);
        	campione.setFotoCampione(null);
        	campione.setFotoEtichetta(null);
        	campione.setCurva(null);
        	campione.setCurvaBinder(null);
        	campione.getCassetta().setFotoContenitore(null);
        	campione.getCassetta().setFotoContenuto(null);
        	campione.getCassetta().setCertificatoPdf(null);
        	campione.getCassetta().getConsegna().setProtocolloAccettazione(null);
        }
        if(entity instanceof it.cnr.igag.italgas.domain.Pesata) {
        	log.trace("E' una pesata");
        	it.cnr.igag.italgas.domain.Pesata pesata = (it.cnr.igag.italgas.domain.Pesata) entity;
        	pesata.getCampione().setFotoSacchetto(null);
        	pesata.getCampione().setFotoCampione(null);
        	pesata.getCampione().setFotoEtichetta(null);
        	pesata.getCampione().setCurva(null);
        	pesata.getCampione().setCurvaBinder(null);
        	pesata.getCampione().getCassetta().setFotoContenitore(null);
        	pesata.getCampione().getCassetta().setFotoContenuto(null);
        	pesata.getCampione().getCassetta().setCertificatoPdf(null);
        	pesata.getCampione().getCassetta().getConsegna().setProtocolloAccettazione(null);
        }
        // ==============================================================================================
        
        auditedEntity.setAction(action.value());
        auditedEntity.setEntityType(entityClass.getName());
        Long entityId;
        String entityData;
        log.trace("Getting Entity Id and Content");
        try {
            Field privateLongField = entityClass.getDeclaredField("id");
            privateLongField.setAccessible(true);
            entityId = (Long) privateLongField.get(entity);
            privateLongField.setAccessible(false);
            entityData = objectMapper.writeValueAsString(entity);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException |
            IOException e) {
            log.error("Exception while getting entity ID and content {}", e);
            // returning null as we dont want to raise an application exception here
            return null;
        }
        auditedEntity.setEntityId(entityId);
        auditedEntity.setEntityValue(entityData);
        final AbstractAuditingEntity abstractAuditEntity = (AbstractAuditingEntity) entity;
        if (EntityAuditAction.CREATE.equals(action)) {
            auditedEntity.setModifiedBy(abstractAuditEntity.getCreatedBy());
            auditedEntity.setModifiedDate(abstractAuditEntity.getCreatedDate());
            auditedEntity.setCommitVersion(1);
        } else {
            auditedEntity.setModifiedBy(abstractAuditEntity.getLastModifiedBy());
            auditedEntity.setModifiedDate(abstractAuditEntity.getLastModifiedDate());
            calculateVersion(auditedEntity);
        }
        log.trace("Audit Entity --> {} ", auditedEntity.toString());
        return auditedEntity;
    }

    private void calculateVersion(EntityAuditEvent auditedEntity) {
        log.trace("Version calculation. for update/remove");
        Integer lastCommitVersion = auditingEntityRepository.findMaxCommitVersion(auditedEntity
            .getEntityType(), auditedEntity.getEntityId());
        log.trace("Last commit version of entity => {}", lastCommitVersion);
        if(lastCommitVersion!=null && lastCommitVersion != 0){
            log.trace("Present. Adding version..");
            auditedEntity.setCommitVersion(lastCommitVersion + 1);
        } else {
            log.trace("No entities.. Adding new version 1");
            auditedEntity.setCommitVersion(1);
        }
    }
}
