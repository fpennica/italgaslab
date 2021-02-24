package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.Consegna;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Consegna entity.
 */
public interface ConsegnaSearchRepository extends ElasticsearchRepository<Consegna, Long> {
}
