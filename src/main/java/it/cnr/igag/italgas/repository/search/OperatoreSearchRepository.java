package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.Operatore;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Operatore entity.
 */
public interface OperatoreSearchRepository extends ElasticsearchRepository<Operatore, Long> {
}
