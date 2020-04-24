package it.polito.tdp.meteo.model;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	private boolean primaEccezione = true;
	private Integer costoTotale = 0;
	private List <String> soluzione;
	private Map <String, Citta> tuttecitta;
	private MeteoDAO meteo ;
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	private boolean primo = true;

	public Model() {
		meteo = new MeteoDAO();
		tuttecitta = new HashMap<>();
	}

	// of course you can change the String output with what you think works best
	public List<Citta> getUmiditaMedia(int mese) {
		Citta milano = new Citta ("Milano", meteo.getAllRilevamentiLocalitaMese(mese, "Milano"));
		milano.calcolaUmiditaMedia();
		Citta torino = new Citta ("Torino", meteo.getAllRilevamentiLocalitaMese(mese, "Torino"));
		torino.calcolaUmiditaMedia();
		Citta genova = new Citta ("Genova", meteo.getAllRilevamentiLocalitaMese(mese, "Genova"));
		genova.calcolaUmiditaMedia();
		List<Citta> citta = new ArrayList<>();
		citta.add(milano);
		citta.add(torino);
		citta.add(genova);
		return citta;
	}
	
	// of course you can change the String output with what you think works best
	public List<String> trovaSequenza(int mese) {
		tuttecitta.clear();
		tuttecitta.put("Milano", new Citta ("Milano", meteo.getAllRilevamentiLocalitaMese(mese, "Milano")));
		tuttecitta.put("Torino", new Citta ("Torino", meteo.getAllRilevamentiLocalitaMese(mese, "Torino")));
		tuttecitta.put("Genova", new Citta ("Genova", meteo.getAllRilevamentiLocalitaMese(mese, "Genova")));
		List <String> parziale = new ArrayList<>();
		soluzione = new ArrayList<>();
		ricorsione (parziale, 0);
		costoTotale = calcolaCosto(soluzione);
		return soluzione;
	}
	
	private void ricorsione (List<String> parziale, int livello){
		
		
		if(livello == NUMERO_GIORNI_TOTALI ) {
			
			if(parziale.contains("Genova") && parziale.contains("Torino") && 
					parziale.contains("Milano") && isCorretta(parziale) ) {
				if(primo) {
				soluzione = new ArrayList<>(parziale);
				primo = false;
				} else if(calcolaCosto(soluzione) > calcolaCosto(parziale))
					soluzione = new ArrayList<>(parziale);
			}//è il minimo
			
		} else {
			
			if(parziale.size()>0 && !isCorretta(parziale))
				return ;
			
			for(String c: tuttecitta.keySet()) {
				parziale.add(c);
				ricorsione(parziale, livello + 1);
				parziale.remove(parziale.size()-1);
			}
			
		}
	}
	
	private boolean isCorretta(List<String> daControllare) {
		int cont = 0;
		List<String> altra = new ArrayList<>(daControllare);
		if(altra.size()==15)
			altra.add("");
		for(Citta c: tuttecitta.values())
			c.setCounter(0);
		//mi permette di controllare anche l'ultima sequenza di stringhe che sicuramente saranno diverse da vuoto
		String attuale = daControllare.get(0);
		
		for(String s: altra) {
			if(s != "")
				tuttecitta.get(s).increaseCounter();
			
			if(s.equals(attuale)) {
					cont++;
			} else {
				if(cont < NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN) 
					return false;
				else {
					attuale = s;
					cont = 1 ;
				}
			}
		}
		for(Citta c: tuttecitta.values()) 
			if(c.getCounter() > NUMERO_GIORNI_CITTA_MAX) 
				return false;

		return true;
		
	}
	
	private int calcolaCosto (List<String> altra) {
		
		Integer rilevamento = 0;
		String precedente = "";
		int contSpostamenti = 0;
		int contatore=0;
		
		for(String s: altra) {
			contatore++;
			if(!precedente.equals(s)) {
				contSpostamenti++;
				precedente = s;
			}
			Citta c = tuttecitta.get(s);
			try{
				rilevamento += c.getRilevamentoUmidita(contatore).getUmidita();
			}catch(NullPointerException npe) {
				if(primaEccezione)
					System.err.println("Mancano i dati del giorno "+contatore+" della località "+s+"\n"
							+ "La sequenza potrebbe essere quindi errata per mancanza di dati");
				primaEccezione=false;
			}
		}
		
		return (contSpostamenti*COST)+rilevamento;
		
	}
	public Integer getCosto() {
		return costoTotale;
	}
	

}
