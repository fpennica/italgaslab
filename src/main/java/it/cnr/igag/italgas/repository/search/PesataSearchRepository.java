package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.Pesata;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Pesata entity.
 */
public interface PesataSearchRepository extends ElasticsearchRepository<Pesata, Long> {
}
