package lt.creditco.cupa.remote;

import java.util.Map;
import lombok.Data;

@Data
public class QueryResponse {

    private Map<String, String> reply;
}
