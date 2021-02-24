package it.cnr.igag.italgas.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.cnr.igag.italgas.domain.Cassetta;
import it.cnr.igag.italgas.domain.Consegna;
import it.cnr.igag.italgas.domain.Laboratorio;
import it.cnr.igag.italgas.domain.Operatore;
import it.cnr.igag.italgas.domain.enumeration.StatoAttualeCassetta;
import it.cnr.igag.italgas.repository.CampioneRepository;
import it.cnr.igag.italgas.repository.CassettaRepository;
import it.cnr.igag.italgas.repository.ConsegnaRepository;
import it.cnr.igag.italgas.repository.LaboratorioRepository;
import it.cnr.igag.italgas.repository.OperatoreRepository;
import it.cnr.igag.italgas.web.rest.dto.ConsegnaStatoCassetteDTO;
import it.cnr.igag.italgas.web.rest.dto.LaboratorioStatoCassetteDTO;
import it.cnr.igag.italgas.web.rest.dto.ProgettoBasicStatsDTO;

@Service
@Transactional
public class StatService {
	
	private final Logger log = LoggerFactory.getLogger(StatService.class);
	
	@Inject
    private CassettaService cassettaService;
	
	@Inject
    private OperatoreRepository operatoreRepository;
	
	@Inject
    private CassettaRepository cassettaRepository;
	
	@Inject
    private ConsegnaRepository consegnaRepository;
	
	@Inject
    private CampioneRepository campioneRepository;
	
	@Inject
    private LaboratorioRepository laboratorioRepository;
	
	@Transactional(readOnly = true) 
	public ConsegnaStatoCassetteDTO getConsegnaStatoCassette(Consegna consegna) {
		
		List<Cassetta> cassettaList = cassettaService.findByConsegna(consegna.getId());
		
		ConsegnaStatoCassetteDTO dto = new ConsegnaStatoCassetteDTO();
		
		dto.setConsegna(consegna);
		dto.setNumCassette(consegna.getNumCassette());
		dto.setNumCassetteInserite(cassettaList.size());
		
		long numCassetteInLavorazione = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.IN_LAVORAZIONE))
				.count();
		
		dto.setNumCassetteInLavorazione((int) numCassetteInLavorazione);
		
		long numCassetteRifiutate = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.RIFIUTATA))
				.count();
		
		dto.setNumCassetteRifiutate((int) numCassetteRifiutate);
		
		long numCassetteInTrattamento = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.TRATTAMENTO_INQUINAMENTO))
				.count();
		
		dto.setNumCassetteInTrattamento((int) numCassetteInTrattamento);
		
		long numCassetteLavorazioneTerminata = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.LAVORAZIONE_TERMINATA))
				.count();
		
		dto.setNumCassetteLavorazioneTerminata((int) numCassetteLavorazioneTerminata);
		
		long numCassetteRestituite = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.RESTITUITA))
				.count();
		
		dto.setNumCassetteRestituite((int) numCassetteRestituite);
		
		long numCassetteStatoNullo = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() == null)
				.count();
		
		long numCassetteStatoSconosciuto = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> !StatoAttualeCassetta.isInEnum(c.getStatoAttualeCassetta().toString()))
				//.filter(c -> c.getStatoAttualeCassetta() == null)
				.count();
		
		dto.setNumCassetteStatoSconosciuto((int) (numCassetteStatoSconosciuto + numCassetteStatoNullo));
		
		
		
		return dto;
		
	}
	
	@Transactional(readOnly = true) 
	public LaboratorioStatoCassetteDTO getLaboratorioStatoCassette() {
		
		List<Cassetta> cassettaList = cassettaService.findByLaboratorio();
		Operatore op = operatoreRepository.findByLogin();
		
		List<Consegna> consegnaList = consegnaRepository.findByLaboratorio(op.getLaboratorio());
		
		LaboratorioStatoCassetteDTO dto = new LaboratorioStatoCassetteDTO();
		
		dto.setLaboratorio(op.getLaboratorio());
		dto.setNumConsegneInserite(consegnaList.size());
		dto.setNumCassetteInserite(cassettaList.size());
		
		long numCassetteInLavorazione = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.IN_LAVORAZIONE))
				.count();
		
		dto.setNumCassetteInLavorazione((int) numCassetteInLavorazione);
		
		long numCassetteRifiutate = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.RIFIUTATA))
				.count();
		
		dto.setNumCassetteRifiutate((int) numCassetteRifiutate);
		
		long numCassetteInTrattamento = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.TRATTAMENTO_INQUINAMENTO))
				.count();
		
		dto.setNumCassetteInTrattamento((int) numCassetteInTrattamento);
		
		long numCassetteLavorazioneTerminata = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.LAVORAZIONE_TERMINATA))
				.count();
		
		dto.setNumCassetteLavorazioneTerminata((int) numCassetteLavorazioneTerminata);
		
		long numCassetteRestituite = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> c.getStatoAttualeCassetta().equals(StatoAttualeCassetta.RESTITUITA))
				.count();
		
		dto.setNumCassetteRestituite((int) numCassetteRestituite);
		
		long numCassetteStatoNullo = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() == null)
				.count();
		
		long numCassetteStatoSconosciuto = cassettaList.stream()
				.filter(c -> c.getStatoAttualeCassetta() != null)
				.filter(c -> !StatoAttualeCassetta.isInEnum(c.getStatoAttualeCassetta().toString()))
				//.filter(c -> c.getStatoAttualeCassetta() == null)
				.count();
		
		dto.setNumCassetteStatoSconosciuto((int) (numCassetteStatoSconosciuto + numCassetteStatoNullo));
		
		
		
		return dto;
		
	}
	
	@Transactional(readOnly = true) 
	public ProgettoBasicStatsDTO getProgettoBasicStats() {
		
		ProgettoBasicStatsDTO dto = new ProgettoBasicStatsDTO();
		
		long numTotConsegne = consegnaRepository.count();
		long numTotCassette = cassettaRepository.count();
		long numTotCampioni = campioneRepository.count();
		
		dto.setNumTotConsegneInserite(numTotConsegne);
		dto.setNumTotCassetteInserite(numTotCassette);
		dto.setNumTotCampioniInseriti(numTotCampioni);
		
		List<Laboratorio> labList = laboratorioRepository.findAllByOrderByIstituto();
		
		HashMap<String, LaboratorioStatoCassetteDTO> labMap = new HashMap<String, LaboratorioStatoCassetteDTO>();
		
		for(Laboratorio l : labList) {
			LaboratorioStatoCassetteDTO labStatsDto = getLabStats(l);
			labMap.put(l.getIstituto(), labStatsDto);
		}
		
		dto.setLabMap(labMap);
		
		return dto;
		
	}
	
	private LaboratorioStatoCassetteDTO getLabStats(Laboratorio laboratorio) {
		
		LaboratorioStatoCassetteDTO dto = new LaboratorioStatoCassetteDTO();
		
		dto.setLaboratorio(laboratorio);
		
		long numConsegne = consegnaRepository.countByLaboratorio(laboratorio);
		long numCassette = cassettaRepository.countByLaboratorio(laboratorio);
		long numCampioni = campioneRepository.countByLaboratorio(laboratorio);
		
		dto.setLaboratorio(dto.getLaboratorio());
		dto.setNumConsegneInserite((int) numConsegne);
		dto.setNumCassetteInserite((int) numCassette);
		dto.setNumCampioniInseriti((int) numCampioni);
		
		return dto;
	}
	
}