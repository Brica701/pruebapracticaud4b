package org.iesvdm.appointment.service.impl;

import net.bytebuddy.asm.Advice;
import org.iesvdm.appointment.entity.*;
import org.iesvdm.appointment.repository.AppointmentRepository;
import org.iesvdm.appointment.repository.ExchangeRequestRepository;
import org.iesvdm.appointment.repository.impl.AppointmentRepositoryImpl;
import org.iesvdm.appointment.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;

public class ExchangeServiceImplTest {

    @Spy
    private AppointmentRepository appointmentRepository = new AppointmentRepositoryImpl(new HashSet<>());

    @Mock
    private NotificationService notificationService;

    @Mock
    private  ExchangeRequestRepository exchangeRequestRepository;

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    private Customer customer1 = new Customer(1
            ,"paco"
            , "1234"
            , new ArrayList<>());
    private Customer customer2 = new Customer(2
            ,"pepe"
            , "1111"
            , new ArrayList<>());

    @Captor
    private ArgumentCaptor<Integer> appointmentIdCaptor;

    @Spy
    private Appointment appointment1 = new Appointment(LocalDateTime.of(2024, 6, 10,6, 0)
            , LocalDateTime.of(2024, 6, 16,18, 0)
            , null
            , null
            , AppointmentStatus.SCHEDULED
            , customer1
            , null
                                );

    @Spy
    private Appointment appointment2 = new Appointment(LocalDateTime.of(2024, 5, 18,8, 15)
            , LocalDateTime.of(2024, 5, 18,10, 15)
            , null
            , null
            , AppointmentStatus.SCHEDULED
            , customer2
            , null
    );

    @BeforeEach
    public void setup() {

        MockitoAnnotations.initMocks(this);

    }

    /**
     * Crea un stub para appointmentRepository.getOne
     * que devuelva una cita (Appointment) que
     * cumple que la fecha/tiempo de inicio (start) es
     * al menos un día después de la fecha/tiempo de búsqueda (actual)
     * , junto con los parámetros de estar planificada (SCHEDULED) y
     * pertenecer al cliente con userId 3.
     * De este modo que al invocar exchangeServiceImpl.checkIfEligibleForExchange
     * se debe obtener true.
     */
    @Test
    void checkIfEligibleForExchange() {
        int userId = 3;
        int appointmentId = 1;

        Appointment appointment = new Appointment(
                LocalDateTime.of(2024, 6, 10,6, 0),
                LocalDateTime.of(2024, 6, 16,18, 0),
                null,
                null,
                AppointmentStatus.SCHEDULED,
                new Customer(userId, "nombre", "telefono", new ArrayList<>()),
                null
        );
        Mockito.when(appointmentRepository.getOne(appointmentId)).thenReturn(appointment);

        boolean result = exchangeService.checkIfEligibleForExchange(userId, appointmentId);

        assertTrue(result);
    }


    /**
     * Añade mediante appointementRepository.save
     * 2 citas (Appointment) de modo que la eligible
     * la 2a empieza más de 24 horas más tarde
     * y pertenece a un cliente (Customer) con id diferente del
     * cliente de la primera que será appointmentToExchange.
     * Se debe verificar la invocación de los métodos appointmentRepository.getOne
     * con el appointmentId pasado a capturar mediante el captor de id
     */
    @Test
    void getEligibleAppointmentsForExchangeTest() {
        int appointmentIdToExchange = 1;
        int otherCustomerId = 2;

        Appointment appointmentToExchange = new Appointment(
                LocalDateTime.of(2024, 6, 10,6, 0),
                LocalDateTime.of(2024, 6, 16,18, 0),
                null,
                null,
                AppointmentStatus.SCHEDULED,
                new Customer(1, "nombre", "telefono", new ArrayList<>()),
                null
        );

        Appointment eligibleAppointment = new Appointment(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                null,
                null,
                AppointmentStatus.SCHEDULED,
                new Customer(otherCustomerId, "nombre", "telefono", new ArrayList<>()),
                null
        );

        appointmentRepository.save(appointmentToExchange);
        appointmentRepository.save(eligibleAppointment);


        List<Appointment> result = exchangeService.getEligibleAppointmentsForExchange(appointmentIdToExchange);


        assertEquals(1, result.size());
        assertEquals(eligibleAppointment, result.get(0));
    }

    /**
     * Realiza una prueba que mediante stubs apropiados demuestre
     * que cuando el userID es igual al userId del oldAppointment
     * se lanza una RuntimeException con mensaje Unauthorized
     */
    @Test
    void checkIfExchangeIsPossibleTest() {
        // Arrange
        int oldAppointmentId = 1;
        int newAppointmentId = 2;
        int userId = 1;
        int unauthorizedUserId = 2;

        Appointment oldAppointment = new Appointment(
                LocalDateTime.of(2024, 6, 10,6, 0),
                LocalDateTime.of(2024, 6, 16,18, 0),
                null,
                null,
                AppointmentStatus.SCHEDULED,
                new Customer(userId, "nombre", "telefono", new ArrayList<>()),
                null
        );

        Appointment newAppointment = new Appointment(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                null,
                null,
                AppointmentStatus.SCHEDULED,
                new Customer(userId, "nombre", "telefono", new ArrayList<>()),
                null
        );

        Mockito.when(appointmentRepository.getOne(oldAppointmentId)).thenReturn(oldAppointment);
        Mockito.when(appointmentRepository.getOne(newAppointmentId)).thenReturn(newAppointment);

        assertThrows(RuntimeException.class, () -> {
            exchangeService.checkIfExchangeIsPossible(oldAppointmentId, newAppointmentId, unauthorizedUserId);
        });
    }


    /**
     * Crea un stub para exchangeRequestRepository.getOne
     * que devuelva un exchangeRequest que contiene una cita (Appointment)
     * en el método getRequestor.
     * Verifica que se invoca exchangeRequestRepository.save capturando
     * al exchangeRequest y comprobando que se le ha establecido un status
     * rechazado (REJECTED).
     * Verfifica se invoca al método con el exchangeRequest del stub.
     */
     void rejectExchangeTest() {
         int oldAppointmentId = 1;
         int newAppointmentId = 2;
         int userId = 1;

         Appointment oldAppointment = new Appointment(
                 LocalDateTime.of(2024, 6, 10,6, 0),
                 LocalDateTime.of(2024, 6, 16,18, 0),
                 null,
                 null,
                 AppointmentStatus.SCHEDULED,
                 new Customer(userId, "nombre", "telefono", new ArrayList<>()),
                 null
         );

         Appointment newAppointment = new Appointment(
                 LocalDateTime.now().plusDays(2),
                 LocalDateTime.now().plusDays(2).plusHours(2),
                 null,
                 null,
                 AppointmentStatus.SCHEDULED,
                 new Customer(userId, "nombre", "telefono", new ArrayList<>()),
                 null
         );

         ExchangeRequest exchangeRequest = new ExchangeRequest(oldAppointment, newAppointment, ExchangeStatus.PENDING);

         Mockito.when(appointmentRepository.getOne(oldAppointmentId)).thenReturn(oldAppointment);
         Mockito.when(appointmentRepository.getOne(newAppointmentId)).thenReturn(newAppointment);
         Mockito.when(exchangeRequestRepository.getOne(anyInt())).thenReturn(exchangeRequest);


         exchangeService.rejectExchange(exchangeRequest.getId());


         ArgumentCaptor<ExchangeRequest> exchangeRequestCaptor = ArgumentCaptor.forClass(ExchangeRequest.class);
         Mockito.verify(exchangeRequestRepository).save(exchangeRequestCaptor.capture());
         assertEquals(ExchangeStatus.REJECTED, exchangeRequestCaptor.getValue().getStatus());
     }

}
