package it.cnr.igag.italgas.web.rest;

import it.cnr.igag.italgas.ItalgaslabApp;
import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.repository.LaboratorioRepository;
import it.cnr.igag.italgas.service.LaboratorioService;
import it.cnr.igag.italgas.repository.search.LaboratorioSearchRepository;

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
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the LaboratorioResource REST controller.
 *
 * @see LaboratorioResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ItalgaslabApp.class)
@WebAppConfiguration
@IntegrationTest
public class LaboratorioResourceIntTest {

    private static final String DEFAULT_NOME = "AAAAA";
    private static final String UPDATED_NOME = "BBBBB";
    private static final String DEFAULT_ISTITUTO = "AAAAA";
    private static final String UPDATED_ISTITUTO = "BBBBB";
    private static final String DEFAULT_RESPONSABILE = "AAAAA";
    private static final String UPDATED_RESPONSABILE = "BBBBB";
    private static final String DEFAULT_INDIRIZZO = "AAAAA";
    private static final String UPDATED_INDIRIZZO = "BBBBB";

    private static final byte[] DEFAULT_LOGO = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_LOGO = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_LOGO_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_LOGO_CONTENT_TYPE = "image/png";

    @Inject
    private LaboratorioRepository laboratorioRepository;

    @Inject
    private LaboratorioService laboratorioService;

    @Inject
    private LaboratorioSearchRepository laboratorioSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restLaboratorioMockMvc;

    private Laboratorio laboratorio;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        LaboratorioResource laboratorioResource = new LaboratorioResource();
        ReflectionTestUtils.setField(laboratorioResource, "laboratorioService", laboratorioService);
        this.restLaboratorioMockMvc = MockMvcBuilders.standaloneSetup(laboratorioResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        laboratorioSearchRepository.deleteAll();
        laboratorio = new Laboratorio();
        laboratorio.setNome(DEFAULT_NOME);
        laboratorio.setIstituto(DEFAULT_ISTITUTO);
        laboratorio.setResponsabile(DEFAULT_RESPONSABILE);
        laboratorio.setIndirizzo(DEFAULT_INDIRIZZO);
        laboratorio.setLogo(DEFAULT_LOGO);
        laboratorio.setLogoContentType(DEFAULT_LOGO_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void createLaboratorio() throws Exception {
        int databaseSizeBeforeCreate = laboratorioRepository.findAll().size();

        // Create the Laboratorio

        restLaboratorioMockMvc.perform(post("/api/laboratorios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(laboratorio)))
                .andExpect(status().isCreated());

        // Validate the Laboratorio in the database
        List<Laboratorio> laboratorios = laboratorioRepository.findAll();
        assertThat(laboratorios).hasSize(databaseSizeBeforeCreate + 1);
        Laboratorio testLaboratorio = laboratorios.get(laboratorios.size() - 1);
        assertThat(testLaboratorio.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testLaboratorio.getIstituto()).isEqualTo(DEFAULT_ISTITUTO);
        assertThat(testLaboratorio.getResponsabile()).isEqualTo(DEFAULT_RESPONSABILE);
        assertThat(testLaboratorio.getIndirizzo()).isEqualTo(DEFAULT_INDIRIZZO);
        assertThat(testLaboratorio.getLogo()).isEqualTo(DEFAULT_LOGO);
        assertThat(testLaboratorio.getLogoContentType()).isEqualTo(DEFAULT_LOGO_CONTENT_TYPE);

        // Validate the Laboratorio in ElasticSearch
        Laboratorio laboratorioEs = laboratorioSearchRepository.findOne(testLaboratorio.getId());
        assertThat(laboratorioEs).isEqualToComparingFieldByField(testLaboratorio);
    }

    @Test
    @Transactional
    public void checkIstitutoIsRequired() throws Exception {
        int databaseSizeBeforeTest = laboratorioRepository.findAll().size();
        // set the field null
        laboratorio.setIstituto(null);

        // Create the Laboratorio, which fails.

        restLaboratorioMockMvc.perform(post("/api/laboratorios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(laboratorio)))
                .andExpect(status().isBadRequest());

        List<Laboratorio> laboratorios = laboratorioRepository.findAll();
        assertThat(laboratorios).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkResponsabileIsRequired() throws Exception {
        int databaseSizeBeforeTest = laboratorioRepository.findAll().size();
        // set the field null
        laboratorio.setResponsabile(null);

        // Create the Laboratorio, which fails.

        restLaboratorioMockMvc.perform(post("/api/laboratorios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(laboratorio)))
                .andExpect(status().isBadRequest());

        List<Laboratorio> laboratorios = laboratorioRepository.findAll();
        assertThat(laboratorios).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllLaboratorios() throws Exception {
        // Initialize the database
        laboratorioRepository.saveAndFlush(laboratorio);

        // Get all the laboratorios
        restLaboratorioMockMvc.perform(get("/api/laboratorios?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(laboratorio.getId().intValue())))
                .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME.toString())))
                .andExpect(jsonPath("$.[*].istituto").value(hasItem(DEFAULT_ISTITUTO.toString())))
                .andExpect(jsonPath("$.[*].responsabile").value(hasItem(DEFAULT_RESPONSABILE.toString())))
                .andExpect(jsonPath("$.[*].indirizzo").value(hasItem(DEFAULT_INDIRIZZO.toString())))
                .andExpect(jsonPath("$.[*].logoContentType").value(hasItem(DEFAULT_LOGO_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].logo").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO))));
    }

    @Test
    @Transactional
    public void getLaboratorio() throws Exception {
        // Initialize the database
        laboratorioRepository.saveAndFlush(laboratorio);

        // Get the laboratorio
        restLaboratorioMockMvc.perform(get("/api/laboratorios/{id}", laboratorio.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(laboratorio.getId().intValue()))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME.toString()))
            .andExpect(jsonPath("$.istituto").value(DEFAULT_ISTITUTO.toString()))
            .andExpect(jsonPath("$.responsabile").value(DEFAULT_RESPONSABILE.toString()))
            .andExpect(jsonPath("$.indirizzo").value(DEFAULT_INDIRIZZO.toString()))
            .andExpect(jsonPath("$.logoContentType").value(DEFAULT_LOGO_CONTENT_TYPE))
            .andExpect(jsonPath("$.logo").value(Base64Utils.encodeToString(DEFAULT_LOGO)));
    }

    @Test
    @Transactional
    public void getNonExistingLaboratorio() throws Exception {
        // Get the laboratorio
        restLaboratorioMockMvc.perform(get("/api/laboratorios/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateLaboratorio() throws Exception {
        // Initialize the database
        laboratorioService.save(laboratorio);

        int databaseSizeBeforeUpdate = laboratorioRepository.findAll().size();

        // Update the laboratorio
        Laboratorio updatedLaboratorio = new Laboratorio();
        updatedLaboratorio.setId(laboratorio.getId());
        updatedLaboratorio.setNome(UPDATED_NOME);
        updatedLaboratorio.setIstituto(UPDATED_ISTITUTO);
        updatedLaboratorio.setResponsabile(UPDATED_RESPONSABILE);
        updatedLaboratorio.setIndirizzo(UPDATED_INDIRIZZO);
        updatedLaboratorio.setLogo(UPDATED_LOGO);
        updatedLaboratorio.setLogoContentType(UPDATED_LOGO_CONTENT_TYPE);

        restLaboratorioMockMvc.perform(put("/api/laboratorios")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedLaboratorio)))
                .andExpect(status().isOk());

        // Validate the Laboratorio in the database
        List<Laboratorio> laboratorios = laboratorioRepository.findAll();
        assertThat(laboratorios).hasSize(databaseSizeBeforeUpdate);
        Laboratorio testLaboratorio = laboratorios.get(laboratorios.size() - 1);
        assertThat(testLaboratorio.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testLaboratorio.getIstituto()).isEqualTo(UPDATED_ISTITUTO);
        assertThat(testLaboratorio.getResponsabile()).isEqualTo(UPDATED_RESPONSABILE);
        assertThat(testLaboratorio.getIndirizzo()).isEqualTo(UPDATED_INDIRIZZO);
        assertThat(testLaboratorio.getLogo()).isEqualTo(UPDATED_LOGO);
        assertThat(testLaboratorio.getLogoContentType()).isEqualTo(UPDATED_LOGO_CONTENT_TYPE);

        // Validate the Laboratorio in ElasticSearch
        Laboratorio laboratorioEs = laboratorioSearchRepository.findOne(testLaboratorio.getId());
        assertThat(laboratorioEs).isEqualToComparingFieldByField(testLaboratorio);
    }

    @Test
    @Transactional
    public void deleteLaboratorio() throws Exception {
        // Initialize the database
        laboratorioService.save(laboratorio);

        int databaseSizeBeforeDelete = laboratorioRepository.findAll().size();

        // Get the laboratorio
        restLaboratorioMockMvc.perform(delete("/api/laboratorios/{id}", laboratorio.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean laboratorioExistsInEs = laboratorioSearchRepository.exists(laboratorio.getId());
        assertThat(laboratorioExistsInEs).isFalse();

        // Validate the database is empty
        List<Laboratorio> laboratorios = laboratorioRepository.findAll();
        assertThat(laboratorios).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchLaboratorio() throws Exception {
        // Initialize the database
        laboratorioService.save(laboratorio);

        // Search the laboratorio
        restLaboratorioMockMvc.perform(get("/api/_search/laboratorios?query=id:" + laboratorio.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(laboratorio.getId().intValue())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME.toString())))
            .andExpect(jsonPath("$.[*].istituto").value(hasItem(DEFAULT_ISTITUTO.toString())))
            .andExpect(jsonPath("$.[*].responsabile").value(hasItem(DEFAULT_RESPONSABILE.toString())))
            .andExpect(jsonPath("$.[*].indirizzo").value(hasItem(DEFAULT_INDIRIZZO.toString())))
            .andExpect(jsonPath("$.[*].logoContentType").value(hasItem(DEFAULT_LOGO_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].logo").value(hasItem(Base64Utils.encodeToString(DEFAULT_LOGO))));
    }
}
