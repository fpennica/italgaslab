package it.cnr.igag.italgas.web.rest;

import it.cnr.igag.italgas.ItalgaslabApp;
import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.repository.ConsegnaRepository;
import it.cnr.igag.italgas.service.ConsegnaService;
import it.cnr.igag.italgas.repository.search.ConsegnaSearchRepository;

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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the ConsegnaResource REST controller.
 *
 * @see ConsegnaResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ItalgaslabApp.class)
@WebAppConfiguration
@IntegrationTest
public class ConsegnaResourceIntTest {


    private static final LocalDate DEFAULT_DATA_CONSEGNA = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA_CONSEGNA = LocalDate.now(ZoneId.systemDefault());
    private static final String DEFAULT_TRASPORTATORE = "AAAAA";
    private static final String UPDATED_TRASPORTATORE = "BBBBB";

    private static final Integer DEFAULT_NUM_CASSETTE = 1;
    private static final Integer UPDATED_NUM_CASSETTE = 2;
    private static final String DEFAULT_NUM_PROTOCOLLO_ACCETTAZIONE = "AAAAA";
    private static final String UPDATED_NUM_PROTOCOLLO_ACCETTAZIONE = "BBBBB";

    private static final byte[] DEFAULT_PROTOCOLLO_ACCETTAZIONE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_PROTOCOLLO_ACCETTAZIONE = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE = "image/png";

    @Inject
    private ConsegnaRepository consegnaRepository;

    @Inject
    private ConsegnaService consegnaService;

    @Inject
    private ConsegnaSearchRepository consegnaSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restConsegnaMockMvc;

    private Consegna consegna;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ConsegnaResource consegnaResource = new ConsegnaResource();
        ReflectionTestUtils.setField(consegnaResource, "consegnaService", consegnaService);
        this.restConsegnaMockMvc = MockMvcBuilders.standaloneSetup(consegnaResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        consegnaSearchRepository.deleteAll();
        consegna = new Consegna();
        consegna.setDataConsegna(DEFAULT_DATA_CONSEGNA);
        consegna.setTrasportatore(DEFAULT_TRASPORTATORE);
        consegna.setNumCassette(DEFAULT_NUM_CASSETTE);
        consegna.setNumProtocolloAccettazione(DEFAULT_NUM_PROTOCOLLO_ACCETTAZIONE);
        consegna.setProtocolloAccettazione(DEFAULT_PROTOCOLLO_ACCETTAZIONE);
        consegna.setProtocolloAccettazioneContentType(DEFAULT_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE);
    }

    @Test
    @Transactional
    public void createConsegna() throws Exception {
        int databaseSizeBeforeCreate = consegnaRepository.findAll().size();

        // Create the Consegna

        restConsegnaMockMvc.perform(post("/api/consegnas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(consegna)))
                .andExpect(status().isCreated());

        // Validate the Consegna in the database
        List<Consegna> consegnas = consegnaRepository.findAll();
        assertThat(consegnas).hasSize(databaseSizeBeforeCreate + 1);
        Consegna testConsegna = consegnas.get(consegnas.size() - 1);
        assertThat(testConsegna.getDataConsegna()).isEqualTo(DEFAULT_DATA_CONSEGNA);
        assertThat(testConsegna.getTrasportatore()).isEqualTo(DEFAULT_TRASPORTATORE);
        assertThat(testConsegna.getNumCassette()).isEqualTo(DEFAULT_NUM_CASSETTE);
        assertThat(testConsegna.getNumProtocolloAccettazione()).isEqualTo(DEFAULT_NUM_PROTOCOLLO_ACCETTAZIONE);
        assertThat(testConsegna.getProtocolloAccettazione()).isEqualTo(DEFAULT_PROTOCOLLO_ACCETTAZIONE);
        assertThat(testConsegna.getProtocolloAccettazioneContentType()).isEqualTo(DEFAULT_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE);

        // Validate the Consegna in ElasticSearch
        Consegna consegnaEs = consegnaSearchRepository.findOne(testConsegna.getId());
        assertThat(consegnaEs).isEqualToComparingFieldByField(testConsegna);
    }

    @Test
    @Transactional
    public void checkDataConsegnaIsRequired() throws Exception {
        int databaseSizeBeforeTest = consegnaRepository.findAll().size();
        // set the field null
        consegna.setDataConsegna(null);

        // Create the Consegna, which fails.

        restConsegnaMockMvc.perform(post("/api/consegnas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(consegna)))
                .andExpect(status().isBadRequest());

        List<Consegna> consegnas = consegnaRepository.findAll();
        assertThat(consegnas).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllConsegnas() throws Exception {
        // Initialize the database
        consegnaRepository.saveAndFlush(consegna);

        // Get all the consegnas
        restConsegnaMockMvc.perform(get("/api/consegnas?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(consegna.getId().intValue())))
                .andExpect(jsonPath("$.[*].dataConsegna").value(hasItem(DEFAULT_DATA_CONSEGNA.toString())))
                .andExpect(jsonPath("$.[*].trasportatore").value(hasItem(DEFAULT_TRASPORTATORE.toString())))
                .andExpect(jsonPath("$.[*].numCassette").value(hasItem(DEFAULT_NUM_CASSETTE)))
                .andExpect(jsonPath("$.[*].numProtocolloAccettazione").value(hasItem(DEFAULT_NUM_PROTOCOLLO_ACCETTAZIONE.toString())))
                .andExpect(jsonPath("$.[*].protocolloAccettazioneContentType").value(hasItem(DEFAULT_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].protocolloAccettazione").value(hasItem(Base64Utils.encodeToString(DEFAULT_PROTOCOLLO_ACCETTAZIONE))));
    }

    @Test
    @Transactional
    public void getConsegna() throws Exception {
        // Initialize the database
        consegnaRepository.saveAndFlush(consegna);

        // Get the consegna
        restConsegnaMockMvc.perform(get("/api/consegnas/{id}", consegna.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(consegna.getId().intValue()))
            .andExpect(jsonPath("$.dataConsegna").value(DEFAULT_DATA_CONSEGNA.toString()))
            .andExpect(jsonPath("$.trasportatore").value(DEFAULT_TRASPORTATORE.toString()))
            .andExpect(jsonPath("$.numCassette").value(DEFAULT_NUM_CASSETTE))
            .andExpect(jsonPath("$.numProtocolloAccettazione").value(DEFAULT_NUM_PROTOCOLLO_ACCETTAZIONE.toString()))
            .andExpect(jsonPath("$.protocolloAccettazioneContentType").value(DEFAULT_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE))
            .andExpect(jsonPath("$.protocolloAccettazione").value(Base64Utils.encodeToString(DEFAULT_PROTOCOLLO_ACCETTAZIONE)));
    }

    @Test
    @Transactional
    public void getNonExistingConsegna() throws Exception {
        // Get the consegna
        restConsegnaMockMvc.perform(get("/api/consegnas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateConsegna() throws Exception {
        // Initialize the database
        consegnaService.save(consegna);

        int databaseSizeBeforeUpdate = consegnaRepository.findAll().size();

        // Update the consegna
        Consegna updatedConsegna = new Consegna();
        updatedConsegna.setId(consegna.getId());
        updatedConsegna.setDataConsegna(UPDATED_DATA_CONSEGNA);
        updatedConsegna.setTrasportatore(UPDATED_TRASPORTATORE);
        updatedConsegna.setNumCassette(UPDATED_NUM_CASSETTE);
        updatedConsegna.setNumProtocolloAccettazione(UPDATED_NUM_PROTOCOLLO_ACCETTAZIONE);
        updatedConsegna.setProtocolloAccettazione(UPDATED_PROTOCOLLO_ACCETTAZIONE);
        updatedConsegna.setProtocolloAccettazioneContentType(UPDATED_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE);

        restConsegnaMockMvc.perform(put("/api/consegnas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedConsegna)))
                .andExpect(status().isOk());

        // Validate the Consegna in the database
        List<Consegna> consegnas = consegnaRepository.findAll();
        assertThat(consegnas).hasSize(databaseSizeBeforeUpdate);
        Consegna testConsegna = consegnas.get(consegnas.size() - 1);
        assertThat(testConsegna.getDataConsegna()).isEqualTo(UPDATED_DATA_CONSEGNA);
        assertThat(testConsegna.getTrasportatore()).isEqualTo(UPDATED_TRASPORTATORE);
        assertThat(testConsegna.getNumCassette()).isEqualTo(UPDATED_NUM_CASSETTE);
        assertThat(testConsegna.getNumProtocolloAccettazione()).isEqualTo(UPDATED_NUM_PROTOCOLLO_ACCETTAZIONE);
        assertThat(testConsegna.getProtocolloAccettazione()).isEqualTo(UPDATED_PROTOCOLLO_ACCETTAZIONE);
        assertThat(testConsegna.getProtocolloAccettazioneContentType()).isEqualTo(UPDATED_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE);

        // Validate the Consegna in ElasticSearch
        Consegna consegnaEs = consegnaSearchRepository.findOne(testConsegna.getId());
        assertThat(consegnaEs).isEqualToComparingFieldByField(testConsegna);
    }

    @Test
    @Transactional
    public void deleteConsegna() throws Exception {
        // Initialize the database
        consegnaService.save(consegna);

        int databaseSizeBeforeDelete = consegnaRepository.findAll().size();

        // Get the consegna
        restConsegnaMockMvc.perform(delete("/api/consegnas/{id}", consegna.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean consegnaExistsInEs = consegnaSearchRepository.exists(consegna.getId());
        assertThat(consegnaExistsInEs).isFalse();

        // Validate the database is empty
        List<Consegna> consegnas = consegnaRepository.findAll();
        assertThat(consegnas).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchConsegna() throws Exception {
        // Initialize the database
        consegnaService.save(consegna);

        // Search the consegna
        restConsegnaMockMvc.perform(get("/api/_search/consegnas?query=id:" + consegna.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(consegna.getId().intValue())))
            .andExpect(jsonPath("$.[*].dataConsegna").value(hasItem(DEFAULT_DATA_CONSEGNA.toString())))
            .andExpect(jsonPath("$.[*].trasportatore").value(hasItem(DEFAULT_TRASPORTATORE.toString())))
            .andExpect(jsonPath("$.[*].numCassette").value(hasItem(DEFAULT_NUM_CASSETTE)))
            .andExpect(jsonPath("$.[*].numProtocolloAccettazione").value(hasItem(DEFAULT_NUM_PROTOCOLLO_ACCETTAZIONE.toString())))
            .andExpect(jsonPath("$.[*].protocolloAccettazioneContentType").value(hasItem(DEFAULT_PROTOCOLLO_ACCETTAZIONE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].protocolloAccettazione").value(hasItem(Base64Utils.encodeToString(DEFAULT_PROTOCOLLO_ACCETTAZIONE))));
    }
}
