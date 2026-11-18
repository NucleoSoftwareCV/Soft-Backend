package com.hean.consigueventas.oonabe.rewards.Config;

import com.hean.consigueventas.oonabe.rewards.Entity.RewardLegalBases;
import com.hean.consigueventas.oonabe.rewards.Repository.RewardLegalBasesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
public class RewardLegalBasesDataSeeder implements CommandLineRunner {

    private final RewardLegalBasesRepository rewardLegalBasesRepository;

    @Override
    @Transactional
    public void run(String... args) {

        if (rewardLegalBasesRepository.count() > 0) {
            return;
        }

        List<RewardLegalBases> legalBases = List.of(

                RewardLegalBases.builder()
                        .question("¿Qué es Oona Rewards?")
                        .description("Oona Rewards es un programa de fidelización que recompensa a los usuarios por participar en las diferentes experiencias y eventos disponibles en Oona. A través de este programa, los usuarios acumulan puntos que posteriormente pueden utilizar para obtener descuentos en sus próximas reservas.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Cómo se acumulan los puntos?")
                        .description("Los puntos se acumulan al asistir a los eventos y experiencias reservados en Oona. La cantidad de puntos obtenidos depende del tipo de experiencia y de las reglas de acumulación establecidas para cada actividad. Los puntos se asignan al usuario que realizó la reserva, una vez confirmada la asistencia de los participantes.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Qué actividades permiten obtener puntos?")
                        .description("Puedes obtener puntos participando en las diferentes experiencias disponibles en Oona, como retiros, talleres, ceremonias, encuentros grupales, formaciones y clases. Cada tipo de experiencia tiene una cantidad de puntos asignada.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Cómo se utilizan los puntos?")
                        .description("Puedes utilizar tus puntos al momento de realizar una reserva. Durante el proceso de pago, podrás seleccionar la cantidad de puntos que deseas canjear y obtener un descuento sobre el importe de tu reserva. La equivalencia es de 100 puntos por cada dólar de descuento.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Cuál es el máximo de puntos que se pueden canjear?")
                        .description("Actualmente, no existe un límite máximo establecido para el canje de puntos. Puedes utilizar los puntos disponibles en tu cuenta para obtener descuentos en tus reservas, de acuerdo con el importe de la compra y las condiciones aplicables.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Los puntos tienen fecha de vencimiento?")
                        .description("No. Actualmente, los puntos acumulados en Oona Rewards no tienen fecha de vencimiento, por lo que puedes conservarlos y utilizarlos cuando desees.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Qué sucede si se cancela una reserva?")
                        .description("Si una reserva es cancelada, no se otorgarán puntos por esa experiencia. Los puntos se generan únicamente cuando los participantes asisten efectivamente al evento.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Los puntos pueden transferirse a otro usuario?")
                        .description("No. Los puntos son personales e intransferibles. Solo pueden ser utilizados por el usuario titular de la cuenta en la que fueron acumulados.")
                        .build(),

                RewardLegalBases.builder()
                        .question("¿Los puntos pueden convertirse en dinero?")
                        .description("Los puntos no pueden retirarse ni convertirse en dinero en efectivo. Sin embargo, tienen un valor de descuento dentro de la plataforma Oona: cada 100 puntos equivalen a 1 dólar de descuento que puedes aplicar al reservar una experiencia.")
                        .build()
        );

        rewardLegalBasesRepository.saveAll(legalBases);
    }
}

