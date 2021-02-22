package com.ipiecoles.java.java350.service;

import com.ipiecoles.java.java350.exception.EmployeException;
import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.NiveauEtude;
import com.ipiecoles.java.java350.model.Poste;
import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class EmployeServiceTest {

    @InjectMocks
    private EmployeService employeService;

    @Mock
    private EmployeRepository employeRepository;

    @Test
    public void testEmbauchePremierEmploye() throws EmployeException {
        //Given Pas d'employés en base
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        //Simuler qu'aucun employé n'est présent (ou du moins aucun matricule)
        Mockito.when(employeRepository.findLastMatricule()).thenReturn(null);
        //Simuler que la recherche par matricule ne renvoie pas de résultats
        Mockito.when(employeRepository.findByMatricule("T00001")).thenReturn(null);
        //When
        employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);
        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
//        Mockito.verify(employeRepository, Mockito.times(1)).save(employeArgumentCaptor.capture());
        Mockito.verify(employeRepository).save(employeArgumentCaptor.capture());
        Employe employe = employeArgumentCaptor.getValue();
        Assertions.assertThat(employe).isNotNull();
        Assertions.assertThat(employe.getNom()).isEqualTo(nom);
        Assertions.assertThat(employe.getPrenom()).isEqualTo(prenom);
        Assertions.assertThat(employe.getSalaire()).isEqualTo(1825.464);
        Assertions.assertThat(employe.getTempsPartiel()).isEqualTo(1.0);
        Assertions.assertThat(employe.getDateEmbauche()).isEqualTo(LocalDate.now());
        Assertions.assertThat(employe.getMatricule()).isEqualTo("T00001");
    }

    @Test
    public void testEmbaucheLimiteMatricule() {
        //Given Pas d'employés en base
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        //Simuler qu'il y a 99999 employés en base (ou du moins que le matricule le plus haut
        //est X99999
        Mockito.when(employeRepository.findLastMatricule()).thenReturn("99999");
        //When
        try {
            employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);
            Assertions.fail("embaucheEmploye aurait dû lancer une exception");
        } catch (EmployeException e) {
            //Then
            Assertions.assertThat(e.getMessage()).isEqualTo("Limite des 100000 matricules atteinte !");
        }
    }

    @Test
    public void testEmbaucheEmployeExisteDeja() throws EmployeException {
        //Given Pas d'employés en base
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        Employe employeExistant = new Employe("Doe", "Jane", "T00001", LocalDate.now(), 1500d, 1, 1.0);
        //Simuler qu'aucun employé n'est présent (ou du moins aucun matricule)
        Mockito.when(employeRepository.findLastMatricule()).thenReturn(null);
        //Simuler que la recherche par matricule renvoie un employé (un employé a été embauché entre temps)
        Mockito.when(employeRepository.findByMatricule("T00001")).thenReturn(employeExistant);
        //When
        try {
            employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel);
            Assertions.fail("embaucheEmploye aurait dû lancer une exception");
        } catch (Exception e){
            //Then
            Assertions.assertThat(e).isInstanceOf(EntityExistsException.class);
            Assertions.assertThat(e.getMessage()).isEqualTo("L'employé de matricule T00001 existe déjà en BDD");
        }
    }

    @ParameterizedTest(name = "Performance{0}, matricule {1} objectifCa{2}, Catraite {3}, majPerformance {4}")
    @CsvSource({"1,'C12345',1000,0,1",
            "1,'C12345',1000,900,1",
            "2,'C12345',1000,900,1",
            "3,'C12345',1000,900,1",
            "4,'C12345',1000,900,2",
            "1,'C12345',1000,999,1",
            "4,'C12345',1000,1001,4",
            "1,'C12345',1000,1051,2",
            "100,'C12345',1000,1200,101",
            "1,'C12345',1000,1201,5",
            "100,'C12345',1000,10000,104",
            "5,'C12345',1000,0,1"})
    public void testCalculPerfCommercialParametre(Integer performance, String matricule, Long objectifCa, Long caTraite, Integer majPerformance) throws EmployeException {
        //Given
        Employe commercial = new Employe("Doe", "Jane", matricule, LocalDate.now(), 1500d, performance, 1.0);
        Mockito.when(employeRepository.findByMatricule(matricule)).thenReturn(commercial);
        Mockito.when(employeRepository.avgPerformanceWhereMatriculeStartsWith("C")).thenReturn(null);
        //When
        employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
        //Then
        Assertions.assertThat(commercial.getPerformance()).isEqualTo(majPerformance);
    }

    @ParameterizedTest(name = "Perf{0}, matricule {1} objectifCa{2}, Catraite {3}")
    @CsvSource({"1,'M12345',0,10000",
            "1,'',0,10000",
            "1, ,0,10000",
            "1,'T12345',0,2000"})
    public void testCalculPerfCommercialMatriculeNotCorNull(Integer performance,String matricule,Long objectifCa,Long caTraite) {
        //Given
        //When
        try {
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("calculPerformanceCommercial aurait dû lancer une exception");
        } catch (EmployeException e) {
            //Then
            Assertions.assertThat(e.getMessage()).isEqualTo("Le matricule ne peut être null et doit commencer par un C !");
        }
    }

    @ParameterizedTest(name = "Perf{0}, matricule {1} objectifCa{2}, Catraite {3}")
    @CsvSource({"1,'C12345',,10000",
            "1,'C12345',-1000,10000"})
    public void testCalculPerfCommercialObjectifCa(Integer performance,String matricule,Long objectifCa,Long caTraite) {
        //Given

        //When
        try {
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("calculPerformanceCommercial aurait dû lancer une exception");
        } catch (EmployeException e) {
            //Then
            Assertions.assertThat(e.getMessage()).isEqualTo("L'objectif de chiffre d'affaire ne peut être négatif ou null !");
        }
    }


    @ParameterizedTest(name = "Perf{0}, matricule {1} objectifCa{2}, Catraite {3}")
    @CsvSource({"1,'C12345',10000,",
            "1,'C12345',10000,-10000"})
    public void testCalculPerfCommercialCatraite(Integer performance,String matricule,Long objectifCa,Long caTraite)  {
        //Given
        //When
        try {
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("calculPerformanceCommercial aurait dû lancer une exception");
        } catch (EmployeException e) {
            //Then
            Assertions.assertThat(e.getMessage()).isEqualTo("Le chiffre d'affaire traité ne peut être négatif ou null !");
        }
    }

    @Test
    public void testCalculPerfCommercialEmployeNull() {
        //Given
        String matricule = "C06432";
        Long caTraite = 100000L;
        Long objectifCa = 100000L;
        //When
        try {
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("calculPerformanceCommercial aurait dû lancer une exception");
        } catch (EmployeException e) {
            //Then
            Assertions.assertThat(e.getMessage()).isEqualTo("Le matricule C06432 n'existe pas !");
        }
    }

    @ParameterizedTest(name = "Perf{0}, matricule {1} objectifCa{2}, Catraite {3}")
    @CsvSource({"0,'C12345',0,10000",
            "-1,'C12345',0,10000",
            "-100,'C12345' ,0,10000",
            ",'C12345',0,2000"})
    public void testCalculPerfCommercialPerfomanceNullOrInferieur1(Integer performance,String matricule,Long objectifCa,Long caTraite) {
        //Given
        Employe commercial = new Employe("Doe", "Jane", matricule, LocalDate.now(), 1500d, performance, 1.0);
        Mockito.when(employeRepository.findByMatricule(matricule)).thenReturn(commercial);
        //When
        try {
            employeService.calculPerformanceCommercial(matricule,caTraite,objectifCa);
            Assertions.fail("calculPerformanceCommercial aurait dû lancer une exception");
        } catch (EmployeException e) {
            //Then
            Assertions.assertThat(e.getMessage()).isEqualTo("La performance ne peut être null ou inférieur a 1 !");
        }
    }

}