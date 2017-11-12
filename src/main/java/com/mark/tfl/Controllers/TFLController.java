package com.mark.tfl.Controllers;

import com.mark.tfl.Models.TFLLineHistoryObject;
import com.mark.tfl.Services.TFLStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mark.tfl.Utils.MathUtils.getPercentage;

@Controller
public class TFLController {

    private static final Logger log = LoggerFactory.getLogger(TFLController.class);
    private long historyCount, goodHistoryCount, notGoodHistoryCount;
    private double percentageUptime;
    private List<String> statuses;
    private List<String> distinctStatuses;

    @Autowired
    private TFLStatusService tflStatusService;

    public void lastScheduledRuntime(String time) {
        log.info("TFLController - " + time);
        tflStatusService.scheduleAPICall();
    }

    @RequestMapping("/")
    public String homeController(Model model) {
        model.addAttribute("title", "Home");
        model.addAttribute("tablecontent", tflStatusService.getLineStatuses());
        model.addAttribute("dropdowncontent", tflStatusService.getLineStatuses());
        return "index";
    }

    @RequestMapping("/issues")
    public String linesWithIssues(Model model) {
        model.addAttribute("title", "Issues");
        model.addAttribute("tablecontent", tflStatusService.getLineIssues());
        model.addAttribute("dropdowncontent", tflStatusService.getLineStatuses());
        return "index";
    }

    @RequestMapping("/linehistory")
    public String lineHistory(@RequestParam("linename") String lineName, Model model) {
        List<TFLLineHistoryObject> lineHistory = tflStatusService.getLineStatusHistoryFromMongo(lineName);
        updateVariables(lineName);
        model.addAttribute("dropdowncontent", tflStatusService.getLineStatuses());
        model.addAttribute("heading", "Status history of the " + lineName + " line");
        model.addAttribute("history", lineHistory);
        model.addAttribute("total_count", "Total number of searches: " + historyCount);
        model.addAttribute("good_count", "Good Service: " + goodHistoryCount);
        model.addAttribute("not_good_count", "Other: " + notGoodHistoryCount);
        model.addAttribute("percentage_uptime", "Percentage uptime: " + percentageUptime + "%");
        model.addAttribute("lineName", lineName);
        model.addAttribute("mapsList", populateChart());
        return "line_history";
    }

    private void updateVariables(String lineName) {
        historyCount = tflStatusService.getHistoryCount();
        goodHistoryCount = tflStatusService.getGoodHistoryCount();
        notGoodHistoryCount = tflStatusService.getNotGoodHistoryCount();
        percentageUptime = getPercentage(historyCount, goodHistoryCount);
        statuses = tflStatusService.getLineStatusesForLine(lineName);
    }

    private Object[][] populateChart() {
        listDistinctStatuses();
        Object[][] result = new Object[distinctStatuses.size() + 1][2];

        result[0][0] = "Status";
        result[0][1] = "Frequency";

        for (int row = 1; row <= distinctStatuses.size(); row++) {
            for (int column = 0; column < 2; column++) {
                if (column == 0) {
                    result[row][column] = distinctStatuses.get(row - 1);
                } else {
                    result[row][column] = findFrequencyOfStatus(distinctStatuses.get(row - 1));
                }
            }
        }
        return result;
    }

    private int findFrequencyOfStatus(String statusType) {
        int count = 0;
        for (String status : statuses) {
            if (Objects.equals(statusType, status)) {
                count++;
            }
        }
        return count;
    }

    private void listDistinctStatuses() {
        distinctStatuses = new ArrayList<>();
        for (String s : statuses) {
            if (!distinctStatuses.contains(s)) {
                System.out.println("adding to list: " + s);
                distinctStatuses.add(s);
            }
        }
    }
}