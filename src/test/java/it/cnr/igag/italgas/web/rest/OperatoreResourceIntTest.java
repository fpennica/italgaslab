package it.cnr.igag.italgas.web.rest;

import it.cnr.igag.italgas.ItalgaslabApp;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.service.OperatoreService;
import it.cnr.igag.italgas.repository.search.OperatoreSearchRepository;

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
 * Test class for the OperatoreResource REST controller.
 *
 * @see OperatoreResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ItalgaslabApp.class)
@WebAppConfiguration
@IntegrationTest
public class OperatoreResourceIntTest {


    @Inject
    private OperatoreRepository operatoreRepository;

    @Inject
    private OperatoreService operatoreService;

    @Inject
    private OperatoreSearchRepository operatoreSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restOperatoreMockMvc;

    private Operatore operatore;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        OperatoreResource operatoreResource = new OperatoreResource();
        ReflectionTestUtils.setField(operatoreResource, "operatoreService", operatoreService);
        this.restOperatoreMockMvc = MockMvcBuilders.standaloneSetup(operatoreResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        operatoreSearchRepository.deleteAll();
        operatore = new Operatore();
    }

    @Test
    @Transactional
    public void createOperatore() throws Exception {
        int databaseSizeBeforeCreate = operatoreRepository.findAll().size();

        // Create the Operatore

        restOperatoreMockMvc.perform(post("/api/operatores")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(operatore)))
                .andExpect(status().isCreated());

        // Validate the Operatore in the database
        List<Operatore> operatores = operatoreRepository.findAll();
        assertThat(operatores).hasSize(databaseSizeBeforeCreate + 1);
        Operatore testOperatore = operatores.get(operatores.size() - 1);

        // Validate the Operatore in ElasticSearch
        Operatore operatoreEs = operatoreSearchRepository.findOne(testOperatore.getId());
        assertThat(operatoreEs).isEqualToComparingFieldByField(testOperatore);
    }

    @Test
    @Transactional
    public void getAllOperatores() throws Exception {
        // Initialize the database
        operatoreRepository.saveAndFlush(operatore);

        // Get all the operatores
        restOperatoreMockMvc.perform(get("/api/operatores?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(operatore.getId().intValue())));
    }

    @Test
    @Transactional
    public void getOperatore() throws Exception {
        // Initialize the database
        operatoreRepository.saveAndFlush(operatore);

        // Get the operatore
        restOperatoreMockMvc.perform(get("/api/operatores/{id}", operatore.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(operatore.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingOperatore() throws Exception {
        // Get the operatore
        restOperatoreMockMvc.perform(get("/api/operatores/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateOperatore() throws Exception {
        // Initialize the database
        operatoreService.save(operatore);

        int databaseSizeBeforeUpdate = operatoreRepository.findAll().size();

        // Update the operatore
        Operatore updatedOperatore = new Operatore();
        updatedOperatore.setId(operatore.getId());

        restOperatoreMockMvc.perform(put("/api/operatores")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedOperatore)))
                .andExpect(status().isOk());

        // Validate the Operatore in the database
        List<Operatore> operatores = operatoreRepository.findAll();
        assertThat(operatores).hasSize(databaseSizeBeforeUpdate);
        Operatore testOperatore = operatores.get(operatores.size() - 1);

        // Validate the Operatore in ElasticSearch
        Operatore operatoreEs = operatoreSearchRepository.findOne(testOperatore.getId());
        assertThat(operatoreEs).isEqualToComparingFieldByField(testOperatore);
    }

    @Test
    @Transactional
    public void deleteOperatore() throws Exception {
        // Initialize the database
        operatoreService.save(operatore);

        int databaseSizeBeforeDelete = operatoreRepository.findAll().size();

        // Get the operatore
        restOperatoreMockMvc.perform(delete("/api/operatores/{id}", operatore.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean operatoreExistsInEs = operatoreSearchRepository.exists(operatore.getId());
        assertThat(operatoreExistsInEs).isFalse();

        // Validate the database is empty
        List<Operatore> operatores = operatoreRepository.findAll();
        assertThat(operatores).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchOperatore() throws Exception {
        // Initialize the database
        operatoreService.save(operatore);

        // Search the operatore
        restOperatoreMockMvc.perform(get("/api/_search/operatores?query=id:" + operatore.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.[*].id").value(hasItem(operatore.getId().intValue())));
    }
}
