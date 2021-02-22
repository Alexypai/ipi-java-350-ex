package com.ipiecoles.java.java350.repository;
import com.ipiecoles.java.java350.Java350Application;
import com.ipiecoles.java.java350.model.Employe;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = {Java350Application.class})
//@DataJpaTest
@SpringBootTest
class EmployeRepositoryTest {

    @Autowired
    EmployeRepository employeRepository;

    @Test
    public void testFindLastMatricule0Employe(){
        //Given
        //Insérer des données en base
        //When
        //Exécuter des requêtes en base
        String lastMatricule = employeRepository.findLastMatricule();
        //Then
        Assertions.assertThat(lastMatricule).isNull();
    }

    @Test
    public void testFindLastMatricule1Employe() {
        //Given
        //Insérer des données en base
        employeRepository.save(new Employe("Doe", "John", "T12345", LocalDate.now(), 1500d, 1, 1.0));
        //When
        //Exécuter des requêtes en base
        String lastMatricule = employeRepository.findLastMatricule();
        //Then
        Assertions.assertThat(lastMatricule).isEqualTo("12345");
    }

    @Test
    public void testFindLastMatriculeNEmploye() {
        //Given
        //Insérer des données en base
        employeRepository.save(new Employe("Doe", "John", "T12345", LocalDate.now(), 1500d, 1, 1.0));
        employeRepository.save(new Employe("Doe", "John", "M40325", LocalDate.now(), 1500d, 1, 1.0));
        employeRepository.save(new Employe("Doe", "John", "C06432", LocalDate.now(), 1500d, 1, 1.0));
        //When
        //Exécuter des requêtes en base
        String lastMatricule = employeRepository.findLastMatricule();
        //Then
        Assertions.assertThat(lastMatricule).isEqualTo("40325");
    }

    @Test
    public void avgPerformanceWhereMatriculeStartsWithNull(){
        //GIVEN
        employeRepository.save(new Employe("Doe", "John", "T12345", LocalDate.now(), 1500d, null, 1.0));
        employeRepository.save(new Employe("Doe", "John", "T40325", LocalDate.now(), 1500d, null, 1.0));
        employeRepository.save(new Employe("Doe", "John", "T06432", LocalDate.now(), 1500d, null, 1.0));        //WHEN
        Double moyenne = employeRepository.avgPerformanceWhereMatriculeStartsWith("T");
        //THEN
        Assertions.assertThat(moyenne).isNull();
    }

    @Test
    public void avgPerformanceWhereMatriculeStartsWithCalcul(){
        //GIVEN
        employeRepository.save(new Employe("Doe", "John", "M12345", LocalDate.now(), 1500d, 100, 1.0));
        employeRepository.save(new Employe("Doe", "John", "M40325", LocalDate.now(), 1500d, 200, 1.0));
        employeRepository.save(new Employe("Doe", "John", "M06432", LocalDate.now(), 1500d, 0, 1.0));        //WHEN
        Double moyenne = employeRepository.avgPerformanceWhereMatriculeStartsWith("M");
        //THEN
        Assertions.assertThat(moyenne).isEqualTo(100);
    }

    @Test
    public void avgPerformanceWhereMatriculeStartsWith0(){
        //GIVEN
        employeRepository.save(new Employe("Doe", "John", "C12345", LocalDate.now(), 1500d, 0, 1.0));
        employeRepository.save(new Employe("Doe", "John", "C40325", LocalDate.now(), 1500d, 0, 1.0));
        employeRepository.save(new Employe("Doe", "John", "C06432", LocalDate.now(), 1500d, 0, 1.0));        //WHEN
        Double moyenne = employeRepository.avgPerformanceWhereMatriculeStartsWith("C");
        //THEN
        Assertions.assertThat(moyenne).isZero();
    }
    @Test
    public void avgPerformanceWhereMatriculeStartsWithOneNull(){
        //GIVEN
        employeRepository.save(new Employe("Doe", "John", "Z12345", LocalDate.now(), 1500d, 100, 1.0));
        employeRepository.save(new Employe("Doe", "John", "Z40325", LocalDate.now(), 1500d, null, 1.0));
        employeRepository.save(new Employe("Doe", "John", "Z06432", LocalDate.now(), 1500d, 200, 1.0));        //WHEN
        Double moyenne = employeRepository.avgPerformanceWhereMatriculeStartsWith("Z");
        //THEN
        // Si un employé possede une performance null il n'est pas compter dans la moyenne
        Assertions.assertThat(moyenne).isEqualTo(150);
    }

    @BeforeEach
    @AfterEach
    public void purgeBDD(){
        employeRepository.deleteAll();
    }

}