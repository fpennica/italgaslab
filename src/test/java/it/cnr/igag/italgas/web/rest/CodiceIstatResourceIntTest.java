package it.cnr.igag.italgas.web.rest;

import it.cnr.igag.italgas.ItalgaslabApp;
import it.cnr.igag.italgas.domain.CodiceIstat;
import it.cnr.igag.italgas.repository.CodiceIstatRepository;
import it.cnr.igag.italgas.service.CodiceIstatService;
import it.cnr.igag.italgas.repository.search.CodiceIstatSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the CodiceIstatResource REST controller.
 *
 * @see CodiceIstatResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ItalgaslabApp.class)
@WebAppConfiguration
@IntegrationTest
public class CodiceIstatResourceIntTest {


    private static final Integer DEFAULT_CODICE_ISTAT = 1;
    private static final Integer UPDATED_CODICE_ISTAT = 2;
    private static final String DEFAULT_COMUNE = "AAAAA";
    private static final String UPDATED_COMUNE = "BBBBB";
    private static final String DEFAULT_PROVINCIA = "AAAAA";
    private static final String UPDATED_PROVINCIA = "BBBBB";
    private static final String DEFAULT_REGIONE = "AAAAA";
    private static final String UPDATED_REGIONE = "BBBBB";

    @Inject
    private CodiceIstatRepository codiceIstatRepository;

    @Inject
    private CodiceIstatService codiceIstatService;

    @Inject
    private CodiceIstatSearchRepository codiceIstatSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restCodiceIstatMockMvc;

    private CodiceIstat codiceIstat;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CodiceIstatResource codiceIstatResource = new CodiceIstatResource();
        ReflectionTestUtils.setField(codiceIstatResource, "codiceIstatService", codiceIstatService);
        this.restCodiceIstatMockMvc = MockMvcBuilders.standaloneSetup(codiceIstatResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        codiceIstatSearchRepository.deleteAll();
        codiceIstat = new CodiceIstat();
        codiceIstat.setCodiceIstat(DEFAULT_CODICE_ISTAT);
        codiceIstat.setComune(DEFAULT_COMUNE);
        codiceIstat.setProvincia(DEFAULT_PROVINCIA);
        codiceIstat.setRegione(DEFAULT_REGIONE);
    }

    @Test
    @Transactional
    public void createCodiceIstat() throws Exception {
        int databaseSizeBeforeCreate = codiceIstatRepository.findAll().size();

        // Create the CodiceIstat

        restCodiceIstatMockMvc.perform(post("/api/codice-istats")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(codiceIstat)))
                .andExpect(status().isCreated());

        // Validate the CodiceIstat in the database
        List<CodiceIstat> codiceIstats = codiceIstatRepository.findAll();
        assertThat(codiceIstats).hasSize(databaseSizeBeforeCreate + 1);
        CodiceIstat testCodiceIstat = codiceIstats.get(codiceIstats.size() - 1);
        assertThat(testCodiceIstat.getCodiceIstat()).isEqualTo(DEFAULT_CODICE_ISTAT);
        assertThat(testCodiceIstat.getComune()).isEqualTo(DEFAULT_COMUNE);
        assertThat(testCodiceIstat.getProvincia()).isEqualTo(DEFAULT_PROVINCIA);
        assertThat(testCodiceIstat.getRegione()).isEqualTo(DEFAULT_REGIONE);

        // Validate the CodiceIstat in ElasticSearch
        CodiceIstat codiceIstatEs = codiceIstatSearchRepository.findOne(testCodiceIstat.getId());
        assertThat(codiceIstatEs).isEqualToComparingFieldByField(testCodiceIstat);
    }

    @Test
    @Transactional
    public void getAllCodiceIstats() throws Exception {
        // Initialize the database
        codiceIstatRepository.saveAndFlush(codiceIstat);

        // Get all the codiceIstats
        restCodiceIstatMockMvc.perform(get("/api/codice-istats?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(codiceIstat.getId().intValue())))
                .andExpect(jsonPath("$.[*].codiceIstat").value(hasItem(DEFAULT_CODICE_ISTAT)))
                .andExpect(jsonPath("$.[*].comune").value(hasItem(DEFAULT_COMUNE.toString())))
                .andExpect(jsonPath("$.[*].provincia").value(hasItem(DEFAULT_PROVINCIA.toString())))
                .andExpect(jsonPath("$.[*].regione").value(hasItem(DEFAULT_REGIONE.toString())));
    }

    @Test
    @Transactional
    public void getCodiceIstat() throws Exception {
        // Initialize the database
        codiceIstatRepository.saveAndFlush(codiceIstat);

        // Get the codiceIstat
        restCodiceIstatMockMvc.perform(get("/api/codice-istats/{id}", codiceIstat.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(codiceIstat.getId().intValue()))
            .andExpect(jsonPath("$.codiceIstat").value(DEFAULT_CODICE_ISTAT))
            .andExpect(jsonPath("$.comune").value(DEFAULT_COMUNE.toString()))
            .andExpect(jsonPath("$.provincia").value(DEFAULT_PROVINCIA.toString()))
            .andExpect(jsonPath("$.regione").value(DEFAULT_REGIONE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCodiceIstat() throws Exception {
        // Get the codiceIstat
        restCodiceIstatMockMvc.perform(get("/api/codice-istats/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCodiceIstat() throws Exception {
        // Initialize the database
        codiceIstatService.save(codiceIstat);

        int databaseSizeBeforeUpdate = codiceIstatRepository.findAll().size();

        // Update the codiceIstat
        CodiceIstat updatedCodiceIstat = new CodiceIstat();
        updatedCodiceIstat.setId(codiceIstat.getId());
        updatedCodiceIstat.setCodiceIstat(UPDATED_CODICE_ISTAT);
        updatedCodiceIstat.setComune(UPDATED_COMUNE);
        updatedCodiceIstat.setProvincia(UPDATED_PROVINCIA);
        updatedCodiceIstat.setRegione(UPDATED_REGIONE);

        restCodiceIstatMockMvc.perform(put("/api/codice-istats")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedCodiceIstat)))
                .andExpect(status().isOk());

        // Validate the CodiceIstat in the database
        List<CodiceIstat> codiceIstats = codiceIstatRepository.findAll();
        assertThat(codiceIstats).hasSize(databaseSizeBeforeUpdate);
        CodiceIstat testCodiceIstat = codiceIstats.get(codiceIstats.size() - 1);
        assertThat(testCodiceIstat.getCodiceIstat()).isEqualTo(UPDATED_CODICE_ISTAT);
        assertThat(testCodiceIstat.getComune()).isEqualTo(UPDATED_COMUNE);
        assertThat(testCodiceIstat.getProvincia()).isEqualTo(UPDATED_PROVINCIA);
        assertThat(testCodiceIstat.getRegione()).isEqualTo(UPDATED_REGIONE);

        // Validate the CodiceIstat in ElasticSearch
        CodiceIstat codiceIstatEs = codiceIstatSearchRepository.findOne(testCodiceIstat.getId());
        assertThat(codiceIstatEs).isEqualToComparingFieldByField(testCodiceIstat);
    }

    @Test
    @Transactional
    public void deleteCodiceIstat() throws Exception {
        // Initialize the database
        codiceIstatService.save(codiceIstat);

        int databaseSizeBeforeDelete = codiceIstatRepository.findAll().size();

        // Get the codiceIstat
        restCodiceIstatMockMvc.perform(delete("/api/codice-istats/{id}", codiceIstat.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean codiceIstatExistsInEs = codiceIstatSearchRepository.exists(codiceIstat.getId());
        assertThat(codiceIstatExistsInEs).isFalse();

        // Validate the database is empty
        List<CodiceIstat> codiceIstats = codiceIstatRepository.findAll();
        assertThat(codiceIstats).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchCodiceIstat() throws Exception {
        // Initialize the database
        codiceIstatService.save(codiceIstat);

        // Search the codiceIstat
        restCodiceIstatMockMvc.perform(get("/api/_search/codice-istats?query=id:" + codiceIstat.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(codiceIstat.getId().intValue())))
            .andExpect(jsonPath("$.[*].codiceIstat").value(hasItem(DEFAULT_CODICE_ISTAT)))
            .andExpect(jsonPath("$.[*].comune").value(hasItem(DEFAULT_COMUNE.toString())))
            .andExpect(jsonPath("$.[*].provincia").value(hasItem(DEFAULT_PROVINCIA.toString())))
            .andExpect(jsonPath("$.[*].regione").value(hasItem(DEFAULT_REGIONE.toString())));
    }
}
