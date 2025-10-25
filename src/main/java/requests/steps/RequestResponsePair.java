package requests.steps;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RequestResponsePair<REQ,RES> {
    private REQ request;
    private RES response;

}
