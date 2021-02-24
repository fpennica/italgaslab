package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.Campione;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Campione entity.
 */
public interface CampioneSearchRepository extends ElasticsearchRepository<Campione, Long> {
}
