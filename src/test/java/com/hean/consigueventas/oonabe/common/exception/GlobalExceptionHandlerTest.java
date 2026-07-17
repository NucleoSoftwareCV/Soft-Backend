package com.hean.consigueventas.oonabe.common.exception;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void optimisticLockConflictReturnsProblemDetail() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest(
                "PUT",
                "/api/v1/one-to-one-services/1");

        ProblemDetail detail = handler.handleOptimisticLock(
                new OptimisticLockException(),
                new ServletWebRequest(request));

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(detail.getTitle()).isEqualTo("Conflicto de actualizacion");
        assertThat(detail.getType().toString()).endsWith("/concurrent-update");
        assertThat(detail.getProperties()).containsEntry("path", "/api/v1/one-to-one-services/1");
    }
}
