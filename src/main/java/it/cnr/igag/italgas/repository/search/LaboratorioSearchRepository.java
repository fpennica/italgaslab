package it.cnr.igag.italgas.repository.search;

import it.cnr.igag.italgas.domain.Laboratorio;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Laboratorio entity.
 */
public interface LaboratorioSearchRepository extends ElasticsearchRepository<Laboratorio, Long> {
}
