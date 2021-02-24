package it.cnr.igag.italgas.service;

import com.codahale.metrics.annotation.Timed;
import it.cnr.igag.italgas.domain.*;
import it.cnr.igag.italgas.repository.*;
import it.cnr.igag.italgas.repository.search.*;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

@Service
public class ElasticsearchIndexService {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexService.class);

    @Inject
    private CampioneRepository campioneRepository;

    @Inject
    private CampioneSearchRepository campioneSearchRepository;

    @Inject
    private CassettaRepository cassettaRepository;

    @Inject
    private CassettaSearchRepository cassettaSearchRepository;

    @Inject
    private CodiceIstatRepository codiceIstatRepository;

    @Inject
    private CodiceIstatSearchRepository codiceIstatSearchRepository;

    @Inject
    private ConsegnaRepository consegnaRepository;

    @Inject
    private ConsegnaSearchRepository consegnaSearchRepository;

    @Inject
    private LaboratorioRepository laboratorioRepository;

    @Inject
    private LaboratorioSearchRepository laboratorioSearchRepository;

    @Inject
    private OperatoreRepository operatoreRepository;

    @Inject
    private OperatoreSearchRepository operatoreSearchRepository;

    @Inject
    private PesataRepository pesataRepository;

    @Inject
    private PesataSearchRepository pesataSearchRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private ElasticsearchTemplate elasticsearchTemplate;

    @Async
    @Timed
    public void reindexAll() {
    	reindexForClass(CodiceIstat.class, codiceIstatRepository, codiceIstatSearchRepository);
    	reindexForClass(Consegna.class, consegnaRepository, consegnaSearchRepository);
        reindexForClass(Cassetta.class, cassettaRepository, cassettaSearchRepository);
        //reindexForClass(Campione.class, campioneRepository, campioneSearchRepository);
        //reindexForClass(Laboratorio.class, laboratorioRepository, laboratorioSearchRepository);
        //reindexForClass(Operatore.class, operatoreRepository, operatoreSearchRepository);
        //reindexForClass(Pesata.class, pesataRepository, pesataSearchRepository);
        //reindexForClass(User.class, userRepository, userSearchRepository);

        log.info("Elasticsearch: Successfully performed reindexing");
    }

    @Transactional
    @SuppressWarnings("unchecked")
    private <T> void reindexForClass(Class<T> entityClass, JpaRepository<T, Long> jpaRepository,
                                                          ElasticsearchRepository<T, Long> elasticsearchRepository) {
        elasticsearchTemplate.deleteIndex(entityClass);
        try {
            elasticsearchTemplate.createIndex(entityClass);
        } catch (IndexAlreadyExistsException e) {
            // Do nothing. Index was already concurrently recreated by some other service.
        }
        elasticsearchTemplate.putMapping(entityClass);
        if (jpaRepository.count() > 0) {
            try {
                Method m = jpaRepository.getClass().getMethod("findAllWithEagerRelationships");
                elasticsearchRepository.save((List<T>) m.invoke(jpaRepository));
            } catch (Exception e) {
                elasticsearchRepository.save(jpaRepository.findAll());
            }
        }
        log.info("Elasticsearch: Indexed all rows for " + entityClass.getSimpleName());
    }
}
