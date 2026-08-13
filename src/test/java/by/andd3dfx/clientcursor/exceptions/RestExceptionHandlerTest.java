package by.andd3dfx.clientcursor.exceptions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class RestExceptionHandlerTest {

    @Test
    void handleBadRequestReturns400WithMessage() {
        RestExceptionHandler handler = new RestExceptionHandler();
        String message = "Do not pass query parameter 'sort' together with 'cursor'; sort is already encoded in the cursor";

        ProblemDetail problem = handler.handleBadRequest(new BadRequestException(message));

        assertThat(problem.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(problem.getDetail(), is(message));
    }
}
