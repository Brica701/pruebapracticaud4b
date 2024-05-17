package org.iesvdm.appointment.repository;

import org.iesvdm.appointment.entity.Appointment;
import org.iesvdm.appointment.entity.AppointmentStatus;
import org.iesvdm.appointment.entity.User;
import org.iesvdm.appointment.repository.impl.AppointmentRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AppointmentRepositoryImplTest {

    private Set<Appointment> appointments;

    private AppointmentRepository appointmentRepository;

    @BeforeEach
    public void setup() {
        appointments = new HashSet<>();
        appointmentRepository = new AppointmentRepositoryImpl(appointments);
    }

    /**
     * Crea 2 citas (Appointment) una con id 1 y otra con id 2,
     * resto de valores inventados.
     * Agrégalas a las citas (appointments) con la que
     * construyes el objeto appointmentRepository bajo test.
     * Comprueba que cuando invocas appointmentRepository.getOne con uno
     * de los id's anteriores recuperas obtienes el objeto.
     * Pero si lo invocas con otro id diferente recuperas null
     */
    @Test
    void getOneTest() {
        Appointment appointment1 = new Appointment();
        appointment1.setId(1);

        Appointment appointment2 = new Appointment();
        appointment2.setId(2);

        appointments.add(appointment1);
        appointments.add(appointment2);

        assertEquals(appointment1, appointmentRepository.getOne(1));
        assertEquals(appointment2, appointmentRepository.getOne(2));

        assertNull(appointmentRepository.getOne(3));
    }


    /**
     * Crea 2 citas (Appointment) y guárdalas mediante
     * appointmentRepository.save.
     * Comprueba que la colección appointments
     * contiene sólo esas 2 citas.
     */
    @Test
    void saveTest() {
        Appointment appointment1 = new Appointment();
        appointment1.setId(1);

        Appointment appointment2 = new Appointment();
        appointment2.setId(2);

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);

        assertTrue(appointments.contains(appointment1));
        assertTrue(appointments.contains(appointment2));
        assertEquals(2, appointments.size());
    }


    /**
     * Crea 2 citas (Appointment) una cancelada por un usuario y otra no,
     * (atención al estado de la cita, lee el código) y agrégalas mediante
     * appointmentRepository.save a la colección de appointments
     * Comprueba que mediante appointmentRepository.findCanceledByUser
     * obtienes la cita cancelada.
     */
    @Test
    void findCanceledByUserTest() {
        Appointment appointment1 = new Appointment();
        User canceler1 = new User();
        canceler1.setId(1);
        appointment1.setCanceler(1);

        Appointment appointment2 = new Appointment();
        User canceler2 = new User();
        canceler2.setId(2);
        appointment2.setCanceler(2);

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);

        List<Appointment> canceledAppointments = appointmentRepository.findCanceledByUser(1);
        assertTrue(canceledAppointments.contains(appointment1));
        assertFalse(canceledAppointments.contains(appointment2));
    }


    /**
     * Crea 3 citas (Appointment), 2 para un mismo cliente (Customer)
     * con sólo una cita de ellas presentando fecha de inicio (start)
     * y fin (end) dentro del periodo de búsqueda (startPeriod,endPeriod).
     * Guárdalas mediante appointmentRepository.save.
     * Comprueba que appointmentRepository.findByCustomerIdWithStartInPeroid
     * encuentra la cita en cuestión.
     * Nota: utiliza LocalDateTime.of(...) para crear los LocalDateTime
     */
    @Test
    void findByCustomerIdWithStartInPeroidTest() {
        Appointment appointment1 = new Appointment();
        User customer1 = new User();
        customer1.setId(1);
        appointment1.setCustomer(1);
        appointment1.setStart(LocalDateTime.of(2024, 5, 17, 9, 0));
        appointment1.setEnd(LocalDateTime.of(2024, 5, 17, 10, 0));

        Appointment appointment2 = new Appointment();
        appointment2.setCustomer(1);
        appointment2.setStart(LocalDateTime.of(2024, 5, 17, 11, 0));
        appointment2.setEnd(LocalDateTime.of(2024, 5, 17, 12, 0));

        Appointment appointment3 = new Appointment();
        User customer2 = new User();
        customer2.setId(2);
        appointment3.setCustomer(2);
        appointment3.setStart(LocalDateTime.of(2024, 5, 17, 13, 0));
        appointment3.setEnd(LocalDateTime.of(2024, 5, 17, 14, 0));

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);
        appointmentRepository.save(appointment3);

        List<Appointment> appointments = appointmentRepository.findByCustomerIdWithStartInPeroid(1, LocalDateTime.of(2024, 5, 17, 8, 0), LocalDateTime.of(2024, 5, 17, 10, 30));
        assertTrue(appointments.contains(appointment1));
        assertFalse(appointments.contains(appointment2));
        assertFalse(appointments.contains(appointment3));
    /**Fallo por los números de las  ids, yo esperaba que no fallara
    */
     }


    /**
     * Crea 2 citas (Appointment) una planificada (SCHEDULED) con tiempo fin
     * anterior a la tiempo buscado por appointmentRepository.findScheduledWithEndBeforeDate
     * guardándolas mediante appointmentRepository.save para la prueba de findScheduledWithEndBeforeDate
     *
     */
    @Test
    void findScheduledWithEndBeforeDateTest() {
        Appointment appointment1 = new Appointment();
        appointment1.setStatus(AppointmentStatus.SCHEDULED);
        appointment1.setEnd(LocalDateTime.now().minusHours(1));

        Appointment appointment2 = new Appointment();
        appointment2.setStatus(AppointmentStatus.SCHEDULED);
        appointment2.setEnd(LocalDateTime.now().plusHours(1));
        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);

        List<Appointment> appointments = appointmentRepository.findScheduledWithEndBeforeDate(LocalDateTime.now());
        assertTrue(appointments.contains(appointment1));
        assertFalse(appointments.contains(appointment2));
    }


    /**
     * Crea 3 citas (Appointment) planificadas (SCHEDULED)
     * , 2 para un mismo cliente, con una elegible para cambio (con fecha de inicio, start, adecuada)
     * y otra no.
     * La tercera ha de ser de otro cliente.
     * Guárdalas mediante appointmentRepository.save
     * Comprueba que getEligibleAppointmentsForExchange encuentra la correcta.
     */
    @Test
    void getEligibleAppointmentsForExchangeTest() {
        Appointment appointment1 = new Appointment();
        appointment1.setStatus(AppointmentStatus.SCHEDULED);
        appointment1.setCustomer(new User(1));
        appointment1.setStart(LocalDateTime.now().plusDays(1));

        Appointment appointment2 = new Appointment();
        appointment2.setStatus(AppointmentStatus.SCHEDULED);
        appointment2.setCustomer(new User(1));
        appointment2.setStart(LocalDateTime.now().minusDays(1));

        Appointment appointment3 = new Appointment();
        appointment3.setStatus(AppointmentStatus.SCHEDULED);
        appointment3.setCustomer(new User(2));
        appointment3.setStart(LocalDateTime.now().plusDays(1));

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);
        appointmentRepository.save(appointment3);

        List<Appointment> eligibleAppointments = appointmentRepository.getEligibleAppointmentsForExchange(LocalDateTime.now(), 1);

        assertEquals(1, eligibleAppointments.size());
        assertTrue(eligibleAppointments.contains(appointment3));
    }


    /**
     * Igual que antes, pero ahora las 3 citas tienen que tener
     * clientes diferentes y 2 de ellas con fecha de inicio (start)
     * antes de la especificada en el método de búsqueda para
     * findExchangeRequestedWithStartBefore
     */
    @Test
    void findExchangeRequestedWithStartBeforeTest() {
        Appointment appointment1 = new Appointment();
        appointment1.setStatus(AppointmentStatus.EXCHANGE_REQUESTED);
        appointment1.setCustomer(new User(1));
        appointment1.setStart(LocalDateTime.now().minusDays(1));

        Appointment appointment2 = new Appointment();
        appointment2.setStatus(AppointmentStatus.EXCHANGE_REQUESTED);
        appointment2.setCustomer(new User(2));
        appointment2.setStart(LocalDateTime.now().minusDays(2));

        Appointment appointment3 = new Appointment();
        appointment3.setStatus(AppointmentStatus.EXCHANGE_REQUESTED);
        appointment3.setCustomer(new User(3));
        appointment3.setStart(LocalDateTime.now().plusDays(1));

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);
        appointmentRepository.save(appointment3);

        List<Appointment> exchangeRequestedAppointments = appointmentRepository.findExchangeRequestedWithStartBefore(LocalDateTime.now());

        assertEquals(2, exchangeRequestedAppointments.size());
        assertTrue(exchangeRequestedAppointments.contains(appointment1));
        assertTrue(exchangeRequestedAppointments.contains(appointment2));
    }
}
