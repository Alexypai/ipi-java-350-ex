package com.ipiecoles.java.java350.service;

import com.ipiecoles.java.java350.exception.EmployeException;
import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.NiveauEtude;
import com.ipiecoles.java.java350.model.Poste;
import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class EmployeServiceMocksTests {

    @InjectMocks
    private EmployeService employeService;

    @Mock
    private EmployeRepository employeRepository;


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
}