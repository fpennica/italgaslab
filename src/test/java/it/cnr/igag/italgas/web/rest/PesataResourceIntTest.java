package it.cnr.igag.italgas.web.rest;

import it.cnr.igag.italgas.ItalgaslabApp;
import it.cnr.igag.italgas.domain.Pesata;
import it.cnr.igag.italgas.repository.PesataRepository;
import it.cnr.igag.italgas.service.PesataService;
import it.cnr.igag.italgas.repository.search.PesataSearchRepository;

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
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the PesataResource REST controller.
 *
 * @see PesataResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ItalgaslabApp.class)
@WebAppConfiguration
@IntegrationTest
public class PesataResourceIntTest {


    private static final Integer DEFAULT_NUM_PESATA = 1;
    private static final Integer UPDATED_NUM_PESATA = 2;

    private static final BigDecimal DEFAULT_PESO_NETTO = new BigDecimal(1);
    private static final BigDecimal UPDATED_PESO_NETTO = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_125_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_125_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_63_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_63_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_31_V_5_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_31_V_5_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_16_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_16_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_8_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_8_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_6_V_3_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_6_V_3_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_4_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_4_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_2_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_2_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_1_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_1_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_0_V_5_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_0_V_5_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_0_V_25_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_0_V_25_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_0_V_125_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_0_V_125_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_0_V_075_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_0_V_075_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_B_100_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_B_100_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_B_6_V_3_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_B_6_V_3_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_B_2_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_B_2_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_TRATT_B_0_V_5_MM = new BigDecimal(1);
    private static final BigDecimal UPDATED_TRATT_B_0_V_5_MM = new BigDecimal(2);

    private static final BigDecimal DEFAULT_FONDO = new BigDecimal(1);
    private static final BigDecimal UPDATED_FONDO = new BigDecimal(2);

    @Inject
    private PesataRepository pesataRepository;

    @Inject
    private PesataService pesataService;

    @Inject
    private PesataSearchRepository pesataSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restPesataMockMvc;

    private Pesata pesata;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PesataResource pesataResource = new PesataResource();
        ReflectionTestUtils.setField(pesataResource, "pesataService", pesataService);
        this.restPesataMockMvc = MockMvcBuilders.standaloneSetup(pesataResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        pesataSearchRepository.deleteAll();
        pesata = new Pesata();
        pesata.setNumPesata(DEFAULT_NUM_PESATA);
        pesata.setPesoNetto(DEFAULT_PESO_NETTO);
        pesata.setTratt125Mm(DEFAULT_TRATT_125_MM);
        pesata.setTratt63Mm(DEFAULT_TRATT_63_MM);
        pesata.setTratt31V5Mm(DEFAULT_TRATT_31_V_5_MM);
        pesata.setTratt16Mm(DEFAULT_TRATT_16_MM);
        pesata.setTratt8Mm(DEFAULT_TRATT_8_MM);
        pesata.setTratt6V3Mm(DEFAULT_TRATT_6_V_3_MM);
        pesata.setTratt4Mm(DEFAULT_TRATT_4_MM);
        pesata.setTratt2Mm(DEFAULT_TRATT_2_MM);
        pesata.setTratt1Mm(DEFAULT_TRATT_1_MM);
        pesata.setTratt0V5Mm(DEFAULT_TRATT_0_V_5_MM);
        pesata.setTratt0V25Mm(DEFAULT_TRATT_0_V_25_MM);
        pesata.setTratt0V125Mm(DEFAULT_TRATT_0_V_125_MM);
        pesata.setTratt0V075Mm(DEFAULT_TRATT_0_V_075_MM);
        pesata.setTrattB100Mm(DEFAULT_TRATT_B_100_MM);
        pesata.setTrattB6V3Mm(DEFAULT_TRATT_B_6_V_3_MM);
        pesata.setTrattB2Mm(DEFAULT_TRATT_B_2_MM);
        pesata.setTrattB0V5Mm(DEFAULT_TRATT_B_0_V_5_MM);
        pesata.setFondo(DEFAULT_FONDO);
    }

    @Test
    @Transactional
    public void createPesata() throws Exception {
        int databaseSizeBeforeCreate = pesataRepository.findAll().size();

        // Create the Pesata

        restPesataMockMvc.perform(post("/api/pesatas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(pesata)))
                .andExpect(status().isCreated());

        // Validate the Pesata in the database
        List<Pesata> pesatas = pesataRepository.findAll();
        assertThat(pesatas).hasSize(databaseSizeBeforeCreate + 1);
        Pesata testPesata = pesatas.get(pesatas.size() - 1);
        assertThat(testPesata.getNumPesata()).isEqualTo(DEFAULT_NUM_PESATA);
        assertThat(testPesata.getPesoNetto()).isEqualTo(DEFAULT_PESO_NETTO);
        assertThat(testPesata.getTratt125Mm()).isEqualTo(DEFAULT_TRATT_125_MM);
        assertThat(testPesata.getTratt63Mm()).isEqualTo(DEFAULT_TRATT_63_MM);
        assertThat(testPesata.getTratt31V5Mm()).isEqualTo(DEFAULT_TRATT_31_V_5_MM);
        assertThat(testPesata.getTratt16Mm()).isEqualTo(DEFAULT_TRATT_16_MM);
        assertThat(testPesata.getTratt8Mm()).isEqualTo(DEFAULT_TRATT_8_MM);
        assertThat(testPesata.getTratt6V3Mm()).isEqualTo(DEFAULT_TRATT_6_V_3_MM);
        assertThat(testPesata.getTratt4Mm()).isEqualTo(DEFAULT_TRATT_4_MM);
        assertThat(testPesata.getTratt2Mm()).isEqualTo(DEFAULT_TRATT_2_MM);
        assertThat(testPesata.getTratt1Mm()).isEqualTo(DEFAULT_TRATT_1_MM);
        assertThat(testPesata.getTratt0V5Mm()).isEqualTo(DEFAULT_TRATT_0_V_5_MM);
        assertThat(testPesata.getTratt0V25Mm()).isEqualTo(DEFAULT_TRATT_0_V_25_MM);
        assertThat(testPesata.getTratt0V125Mm()).isEqualTo(DEFAULT_TRATT_0_V_125_MM);
        assertThat(testPesata.getTratt0V075Mm()).isEqualTo(DEFAULT_TRATT_0_V_075_MM);
        assertThat(testPesata.getTrattB100Mm()).isEqualTo(DEFAULT_TRATT_B_100_MM);
        assertThat(testPesata.getTrattB6V3Mm()).isEqualTo(DEFAULT_TRATT_B_6_V_3_MM);
        assertThat(testPesata.getTrattB2Mm()).isEqualTo(DEFAULT_TRATT_B_2_MM);
        assertThat(testPesata.getTrattB0V5Mm()).isEqualTo(DEFAULT_TRATT_B_0_V_5_MM);
        assertThat(testPesata.getFondo()).isEqualTo(DEFAULT_FONDO);

        // Validate the Pesata in ElasticSearch
        Pesata pesataEs = pesataSearchRepository.findOne(testPesata.getId());
        assertThat(pesataEs).isEqualToComparingFieldByField(testPesata);
    }

    @Test
    @Transactional
    public void getAllPesatas() throws Exception {
        // Initialize the database
        pesataRepository.saveAndFlush(pesata);

        // Get all the pesatas
        restPesataMockMvc.perform(get("/api/pesatas?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(pesata.getId().intValue())))
                .andExpect(jsonPath("$.[*].numPesata").value(hasItem(DEFAULT_NUM_PESATA)))
                .andExpect(jsonPath("$.[*].pesoNetto").value(hasItem(DEFAULT_PESO_NETTO.intValue())))
                .andExpect(jsonPath("$.[*].tratt125Mm").value(hasItem(DEFAULT_TRATT_125_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt63Mm").value(hasItem(DEFAULT_TRATT_63_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt31V5Mm").value(hasItem(DEFAULT_TRATT_31_V_5_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt16Mm").value(hasItem(DEFAULT_TRATT_16_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt8Mm").value(hasItem(DEFAULT_TRATT_8_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt6V3Mm").value(hasItem(DEFAULT_TRATT_6_V_3_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt4Mm").value(hasItem(DEFAULT_TRATT_4_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt2Mm").value(hasItem(DEFAULT_TRATT_2_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt1Mm").value(hasItem(DEFAULT_TRATT_1_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt0V5Mm").value(hasItem(DEFAULT_TRATT_0_V_5_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt0V25Mm").value(hasItem(DEFAULT_TRATT_0_V_25_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt0V125Mm").value(hasItem(DEFAULT_TRATT_0_V_125_MM.intValue())))
                .andExpect(jsonPath("$.[*].tratt0V075Mm").value(hasItem(DEFAULT_TRATT_0_V_075_MM.intValue())))
                .andExpect(jsonPath("$.[*].trattB100Mm").value(hasItem(DEFAULT_TRATT_B_100_MM.intValue())))
                .andExpect(jsonPath("$.[*].trattB6V3Mm").value(hasItem(DEFAULT_TRATT_B_6_V_3_MM.intValue())))
                .andExpect(jsonPath("$.[*].trattB2Mm").value(hasItem(DEFAULT_TRATT_B_2_MM.intValue())))
                .andExpect(jsonPath("$.[*].trattB0V5Mm").value(hasItem(DEFAULT_TRATT_B_0_V_5_MM.intValue())))
                .andExpect(jsonPath("$.[*].fondo").value(hasItem(DEFAULT_FONDO.intValue())));
    }

    @Test
    @Transactional
    public void getPesata() throws Exception {
        // Initialize the database
        pesataRepository.saveAndFlush(pesata);

        // Get the pesata
        restPesataMockMvc.perform(get("/api/pesatas/{id}", pesata.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(pesata.getId().intValue()))
            .andExpect(jsonPath("$.numPesata").value(DEFAULT_NUM_PESATA))
            .andExpect(jsonPath("$.pesoNetto").value(DEFAULT_PESO_NETTO.intValue()))
            .andExpect(jsonPath("$.tratt125Mm").value(DEFAULT_TRATT_125_MM.intValue()))
            .andExpect(jsonPath("$.tratt63Mm").value(DEFAULT_TRATT_63_MM.intValue()))
            .andExpect(jsonPath("$.tratt31V5Mm").value(DEFAULT_TRATT_31_V_5_MM.intValue()))
            .andExpect(jsonPath("$.tratt16Mm").value(DEFAULT_TRATT_16_MM.intValue()))
            .andExpect(jsonPath("$.tratt8Mm").value(DEFAULT_TRATT_8_MM.intValue()))
            .andExpect(jsonPath("$.tratt6V3Mm").value(DEFAULT_TRATT_6_V_3_MM.intValue()))
            .andExpect(jsonPath("$.tratt4Mm").value(DEFAULT_TRATT_4_MM.intValue()))
            .andExpect(jsonPath("$.tratt2Mm").value(DEFAULT_TRATT_2_MM.intValue()))
            .andExpect(jsonPath("$.tratt1Mm").value(DEFAULT_TRATT_1_MM.intValue()))
            .andExpect(jsonPath("$.tratt0V5Mm").value(DEFAULT_TRATT_0_V_5_MM.intValue()))
            .andExpect(jsonPath("$.tratt0V25Mm").value(DEFAULT_TRATT_0_V_25_MM.intValue()))
            .andExpect(jsonPath("$.tratt0V125Mm").value(DEFAULT_TRATT_0_V_125_MM.intValue()))
            .andExpect(jsonPath("$.tratt0V075Mm").value(DEFAULT_TRATT_0_V_075_MM.intValue()))
            .andExpect(jsonPath("$.trattB100Mm").value(DEFAULT_TRATT_B_100_MM.intValue()))
            .andExpect(jsonPath("$.trattB6V3Mm").value(DEFAULT_TRATT_B_6_V_3_MM.intValue()))
            .andExpect(jsonPath("$.trattB2Mm").value(DEFAULT_TRATT_B_2_MM.intValue()))
            .andExpect(jsonPath("$.trattB0V5Mm").value(DEFAULT_TRATT_B_0_V_5_MM.intValue()))
            .andExpect(jsonPath("$.fondo").value(DEFAULT_FONDO.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingPesata() throws Exception {
        // Get the pesata
        restPesataMockMvc.perform(get("/api/pesatas/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePesata() throws Exception {
        // Initialize the database
        pesataService.save(pesata);

        int databaseSizeBeforeUpdate = pesataRepository.findAll().size();

        // Update the pesata
        Pesata updatedPesata = new Pesata();
        updatedPesata.setId(pesata.getId());
        updatedPesata.setNumPesata(UPDATED_NUM_PESATA);
        updatedPesata.setPesoNetto(UPDATED_PESO_NETTO);
        updatedPesata.setTratt125Mm(UPDATED_TRATT_125_MM);
        updatedPesata.setTratt63Mm(UPDATED_TRATT_63_MM);
        updatedPesata.setTratt31V5Mm(UPDATED_TRATT_31_V_5_MM);
        updatedPesata.setTratt16Mm(UPDATED_TRATT_16_MM);
        updatedPesata.setTratt8Mm(UPDATED_TRATT_8_MM);
        updatedPesata.setTratt6V3Mm(UPDATED_TRATT_6_V_3_MM);
        updatedPesata.setTratt4Mm(UPDATED_TRATT_4_MM);
        updatedPesata.setTratt2Mm(UPDATED_TRATT_2_MM);
        updatedPesata.setTratt1Mm(UPDATED_TRATT_1_MM);
        updatedPesata.setTratt0V5Mm(UPDATED_TRATT_0_V_5_MM);
        updatedPesata.setTratt0V25Mm(UPDATED_TRATT_0_V_25_MM);
        updatedPesata.setTratt0V125Mm(UPDATED_TRATT_0_V_125_MM);
        updatedPesata.setTratt0V075Mm(UPDATED_TRATT_0_V_075_MM);
        updatedPesata.setTrattB100Mm(UPDATED_TRATT_B_100_MM);
        updatedPesata.setTrattB6V3Mm(UPDATED_TRATT_B_6_V_3_MM);
        updatedPesata.setTrattB2Mm(UPDATED_TRATT_B_2_MM);
        updatedPesata.setTrattB0V5Mm(UPDATED_TRATT_B_0_V_5_MM);
        updatedPesata.setFondo(UPDATED_FONDO);

        restPesataMockMvc.perform(put("/api/pesatas")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedPesata)))
                .andExpect(status().isOk());

        // Validate the Pesata in the database
        List<Pesata> pesatas = pesataRepository.findAll();
        assertThat(pesatas).hasSize(databaseSizeBeforeUpdate);
        Pesata testPesata = pesatas.get(pesatas.size() - 1);
        assertThat(testPesata.getNumPesata()).isEqualTo(UPDATED_NUM_PESATA);
        assertThat(testPesata.getPesoNetto()).isEqualTo(UPDATED_PESO_NETTO);
        assertThat(testPesata.getTratt125Mm()).isEqualTo(UPDATED_TRATT_125_MM);
        assertThat(testPesata.getTratt63Mm()).isEqualTo(UPDATED_TRATT_63_MM);
        assertThat(testPesata.getTratt31V5Mm()).isEqualTo(UPDATED_TRATT_31_V_5_MM);
        assertThat(testPesata.getTratt16Mm()).isEqualTo(UPDATED_TRATT_16_MM);
        assertThat(testPesata.getTratt8Mm()).isEqualTo(UPDATED_TRATT_8_MM);
        assertThat(testPesata.getTratt6V3Mm()).isEqualTo(UPDATED_TRATT_6_V_3_MM);
        assertThat(testPesata.getTratt4Mm()).isEqualTo(UPDATED_TRATT_4_MM);
        assertThat(testPesata.getTratt2Mm()).isEqualTo(UPDATED_TRATT_2_MM);
        assertThat(testPesata.getTratt1Mm()).isEqualTo(UPDATED_TRATT_1_MM);
        assertThat(testPesata.getTratt0V5Mm()).isEqualTo(UPDATED_TRATT_0_V_5_MM);
        assertThat(testPesata.getTratt0V25Mm()).isEqualTo(UPDATED_TRATT_0_V_25_MM);
        assertThat(testPesata.getTratt0V125Mm()).isEqualTo(UPDATED_TRATT_0_V_125_MM);
        assertThat(testPesata.getTratt0V075Mm()).isEqualTo(UPDATED_TRATT_0_V_075_MM);
        assertThat(testPesata.getTrattB100Mm()).isEqualTo(UPDATED_TRATT_B_100_MM);
        assertThat(testPesata.getTrattB6V3Mm()).isEqualTo(UPDATED_TRATT_B_6_V_3_MM);
        assertThat(testPesata.getTrattB2Mm()).isEqualTo(UPDATED_TRATT_B_2_MM);
        assertThat(testPesata.getTrattB0V5Mm()).isEqualTo(UPDATED_TRATT_B_0_V_5_MM);
        assertThat(testPesata.getFondo()).isEqualTo(UPDATED_FONDO);

        // Validate the Pesata in ElasticSearch
        Pesata pesataEs = pesataSearchRepository.findOne(testPesata.getId());
        assertThat(pesataEs).isEqualToComparingFieldByField(testPesata);
    }

    @Test
    @Transactional
    public void deletePesata() throws Exception {
        // Initialize the database
        pesataService.save(pesata);

        int databaseSizeBeforeDelete = pesataRepository.findAll().size();

        // Get the pesata
        restPesataMockMvc.perform(delete("/api/pesatas/{id}", pesata.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean pesataExistsInEs = pesataSearchRepository.exists(pesata.getId());
        assertThat(pesataExistsInEs).isFalse();

        // Validate the database is empty
        List<Pesata> pesatas = pesataRepository.findAll();
        assertThat(pesatas).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchPesata() throws Exception {
        // Initialize the database
        pesataService.save(pesata);

        // Search the pesata
        restPesataMockMvc.perform(get("/api/_search/pesatas?query=id:" + pesata.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(pesata.getId().intValue())))
            .andExpect(jsonPath("$.[*].numPesata").value(hasItem(DEFAULT_NUM_PESATA)))
            .andExpect(jsonPath("$.[*].pesoNetto").value(hasItem(DEFAULT_PESO_NETTO.intValue())))
            .andExpect(jsonPath("$.[*].tratt125Mm").value(hasItem(DEFAULT_TRATT_125_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt63Mm").value(hasItem(DEFAULT_TRATT_63_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt31V5Mm").value(hasItem(DEFAULT_TRATT_31_V_5_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt16Mm").value(hasItem(DEFAULT_TRATT_16_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt8Mm").value(hasItem(DEFAULT_TRATT_8_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt6V3Mm").value(hasItem(DEFAULT_TRATT_6_V_3_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt4Mm").value(hasItem(DEFAULT_TRATT_4_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt2Mm").value(hasItem(DEFAULT_TRATT_2_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt1Mm").value(hasItem(DEFAULT_TRATT_1_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt0V5Mm").value(hasItem(DEFAULT_TRATT_0_V_5_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt0V25Mm").value(hasItem(DEFAULT_TRATT_0_V_25_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt0V125Mm").value(hasItem(DEFAULT_TRATT_0_V_125_MM.intValue())))
            .andExpect(jsonPath("$.[*].tratt0V075Mm").value(hasItem(DEFAULT_TRATT_0_V_075_MM.intValue())))
            .andExpect(jsonPath("$.[*].trattB100Mm").value(hasItem(DEFAULT_TRATT_B_100_MM.intValue())))
            .andExpect(jsonPath("$.[*].trattB6V3Mm").value(hasItem(DEFAULT_TRATT_B_6_V_3_MM.intValue())))
            .andExpect(jsonPath("$.[*].trattB2Mm").value(hasItem(DEFAULT_TRATT_B_2_MM.intValue())))
            .andExpect(jsonPath("$.[*].trattB0V5Mm").value(hasItem(DEFAULT_TRATT_B_0_V_5_MM.intValue())))
            .andExpect(jsonPath("$.[*].fondo").value(hasItem(DEFAULT_FONDO.intValue())));
    }
}
