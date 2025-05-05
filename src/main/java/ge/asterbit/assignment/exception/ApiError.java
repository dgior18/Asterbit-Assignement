package ge.asterbit.assignment.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String message;
    private String debugMessage;
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public ApiError(HttpStatus status) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(HttpStatus status, String message) {
        this(status);
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, Throwable ex) {
        this(status, message);
        this.debugMessage = ex.getLocalizedMessage();
    }

    public void addError(String error) {
        this.errors.add(error);
    }
} 