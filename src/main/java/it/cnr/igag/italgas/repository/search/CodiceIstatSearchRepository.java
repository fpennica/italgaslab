package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.CodiceIstat;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the CodiceIstat entity.
 */
public interface CodiceIstatSearchRepository extends ElasticsearchRepository<CodiceIstat, Long> {
}
