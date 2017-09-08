package TubeLineStatus.Controllers;

import TubeLineStatus.POJOs.TFLResponsePOJOs.LineStatus;
import TubeLineStatus.POJOs.TFLResponsePOJOs.TFLResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Dashboard {

    private ObjectMapper objectMapper;
    private List<LineStatus> lineStatusList;

    public Dashboard() {
        objectMapper = new ObjectMapper();
        lineStatusList = new ArrayList<>();
    }

    @RequestMapping("/")
    public List<LineStatus> appStatus() throws IOException {
        callTFLAPI();
        return lineStatusList;
    }

    private void callTFLAPI() {
        try {
            URL url = new URL("https://api.tfl.gov.uk/line/mode/tube/status");
            List<TFLResponse> tflResponse = objectMapper.readValue(url, new TypeReference<List<TFLResponse>>() {
            });

            for (TFLResponse t : tflResponse) {

                System.out.println(t.getName());
                System.out.println(t.getStatus() + "\n");

                LineStatus lineStatus = new LineStatus(t.getName(), t.getStatus());
                lineStatusList.add(lineStatus);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
