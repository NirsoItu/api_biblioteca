package com.rogerio.libraryapi.service;

import com.rogerio.libraryapi.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class ScheduleService {

    // Agendamento de tarefa ( Segundos, Minutos, Horas, Dia, Mes, Ano), utilizar o site CronMaker.com, copiar o cronformat
    public static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    private final LoanService loanService;
    private final EmailService emailService;

    @Value("{application.mail.lateloans.message}")
    private String message;


    @Scheduled(cron = CRON_LATE_LOANS)
    public void SendMailToLateLoans(){
        List<Loan> allLateLoans = loanService.getAllLateLoans();
        List<String> mailsList = allLateLoans
                .stream()
                .map(loan -> loan.getCustomerEmail())
                .collect(Collectors.toList());

        emailService.sendMails(message, mailsList);
    }

}
