package by.andd3dfx.templateapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArticleNotFoundException extends NotFoundException {

    public ArticleNotFoundException(Long id) {
        super("an article", id);
    }
}
