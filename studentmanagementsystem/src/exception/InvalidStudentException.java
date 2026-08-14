package exception;

public class InvalidStudentException extends RuntimeException {
    public InvalidStudentException(String message){
        super(message);
    }
}
