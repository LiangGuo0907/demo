package com.example.controller;

import com.example.service.IAttendanceSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wl
 * @date 2021/7/22 15:51
 */
@RestController
@RequestMapping("/api")
public class DataController {

    @Autowired
    private IAttendanceSheetService attendanceSheetService;

    @RequestMapping(value = "/getdata")
    public Map<String,Object> dataList(@RequestParam(required=false,defaultValue="1") int page,
                                       @RequestParam(required=false,defaultValue="20") int limit,
                                       @RequestParam(required=false) String name,
                                       @RequestParam(required=false) String attendanceTime){
        return attendanceSheetService.queryMonthDataPage(page,limit,name,attendanceTime);
    }
}
