package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.Cassetta;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Cassetta entity.
 */
public interface CassettaSearchRepository extends ElasticsearchRepository<Cassetta, Long> {
}
