package com.hean.consigueventas.oonabe.masterdata.controller;

import com.hean.consigueventas.oonabe.masterdata.dto.request.CityInterestRequest;
import com.hean.consigueventas.oonabe.masterdata.dto.response.CityInterestResponse;
import com.hean.consigueventas.oonabe.masterdata.service.CityInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/city-interests")
@Tag(name = "Interes por ciudad", description = "Registro publico de interes por ciudades donde Oona aun no esta disponible.")
public class CityInterestController {

    private final CityInterestService cityInterestService;

    public CityInterestController(CityInterestService cityInterestService) {
        this.cityInterestService = cityInterestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar interes por una ciudad",
            description = "Guarda el email de un usuario interesado en que Oona llegue a una ciudad determinada.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Interes registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, ciudad inactiva o interes duplicado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ciudad no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CityInterestResponse register(@Valid @RequestBody CityInterestRequest request) {
        return cityInterestService.register(request);
    }
}
