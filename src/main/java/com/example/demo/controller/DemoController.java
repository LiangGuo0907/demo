package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.example.demo.entity.Demo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wl
 * @date 2021/5/23 11:03
 */
@Controller
public class DemoController {

    @RequestMapping("index")
    public String index(Model model){
        model.addAttribute("name","jack");
        model.addAttribute("age","30");
        model.addAttribute("info","我是一个爱学习的好孩子");
        return "index";
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    @ResponseBody
    public void upload(@RequestParam(value="file",required = true) MultipartFile file) throws Exception {
        InputStream inputStream = null;
        Map map = new HashMap<>();//用来存储每个sheet页
        String fileName = "";
        try {
            inputStream =file.getInputStream();//获取前端传递过来的文件对象，存储在“inputStream”中
            fileName = file.getOriginalFilename();//获取文件名

            Workbook workbook =null; //用于存储解析后的Excel文件

            //判断文件扩展名为“.xls还是xlsx的Excel文件”,因为不同扩展名的Excel所用到的解析方法不同
            String fileType = fileName.substring(fileName.lastIndexOf("."));
            if(".xls".equals(fileType)){
                workbook= new HSSFWorkbook(inputStream);//HSSFWorkbook专门解析.xls文件
            }else if(".xlsx".equals(fileType)){
                workbook = new XSSFWorkbook(inputStream);//XSSFWorkbook专门解析.xlsx文件
            }

            Sheet sheet; //工作表
            Row row;      //行
            Cell cell;    //单元格

            //循环遍历，获取数据
            for(int i=0;i<workbook.getNumberOfSheets();i++){
                ArrayList<ArrayList<Object>>list =new ArrayList<>();
                sheet=workbook.getSheetAt(i);//获取sheet
                for(int j=4;j<=sheet.getLastRowNum();j++){//从有数据的第行开始遍历
                    row=sheet.getRow(j);
                    if(row!=null&&row.getFirstCellNum()!=j){ //row.getFirstCellNum()!=j的作用是去除首行，即标题行，如果无标题行可将该条件去掉
                        ArrayList tempList =new ArrayList();
                        for(int k=row.getFirstCellNum();k<row.getLastCellNum();k++){//这里需要注意的是getLastCellNum()的返回值为“下标+1”
                            cell =row.getCell(k);
                            tempList.add(cell);
                        }
                        list.add(tempList);
                    }
                }
                map.put(sheet.getSheetName(),list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            inputStream.close();
        }
        String reg = "[^\\d]";
        String[] newStrArr = fileName.split(reg);
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        EasyExcel.write(fileName, Demo.class).sheet("模板").doWrite((List) map.get("月度汇总"));
    }

    /**
     * xxxx年x月考勤表汇总表头
     * @return
     */
    private List<List<String>> timeSheetSummaryHead(String year,String month) {
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> head0 = new ArrayList<String>();
        head0.add("字符串" + System.currentTimeMillis());
        List<String> head1 = new ArrayList<String>();
        head1.add("数字" + System.currentTimeMillis());
        List<String> head2 = new ArrayList<String>();
        head2.add("日期" + System.currentTimeMillis());
        list.add(head0);
        list.add(head1);
        list.add(head2);
        return list;
    }

}
