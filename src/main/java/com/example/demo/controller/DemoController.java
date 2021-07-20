package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.demo.Tools.ExcelListener;
import com.example.demo.entity.MonthTotalDto;
import com.example.demo.entity.MonthTotalExport2Dto;
import com.example.demo.entity.MonthTotalExportDto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wl
 * @date 2021/5/23 11:03
 */
@Controller
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private static String fileName = "";//上传的文件名
    private Map<String,Object> totalData = new HashMap<>();//钉钉月度汇总数据

    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

    /**
     * 导入Excel，解析
     *
     * @param file
     * @throws Exception
     */
    @PostMapping(value = "/importExcel")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam(value = "file") MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "导入失败");
       /* //获取选中的column
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
        String[] newStrArr = fileName.split(reg);*/
        fileName = run(file.getOriginalFilename());//获取文件名
        InputStream in = null;
        ExcelReader excelReader = null;
        try {
            in = file.getInputStream();
            ExcelListener boxServerListener1 = new ExcelListener();
            ExcelListener boxServerListener2 = new ExcelListener();
            ExcelListener boxServerListener3 = new ExcelListener();
            excelReader = EasyExcel.read(in).build();
            //第一个sheet
            ReadSheet readBoxServerSheet1 =
                    EasyExcel.readSheet(0).head(MonthTotalDto.class).registerReadListener(boxServerListener1).build();
            //第五个sheet
            ReadSheet readBoxServerSheet2 =
                    EasyExcel.readSheet(4).head(MonthTotalDto.class).registerReadListener(boxServerListener2).build();
            //第六个sheet
            ReadSheet readBoxServerSheet3 =
                    EasyExcel.readSheet(5).head(MonthTotalDto.class).registerReadListener(boxServerListener3).build();
            //读取数据，从第一个sheet读
            excelReader.read(readBoxServerSheet1,readBoxServerSheet2,readBoxServerSheet3);
            totalData.put("月汇总数据",boxServerListener1.getData());

            totalData.put("九二五",boxServerListener2.getData().stream().map(m -> m.getCheckGroup()).collect(Collectors.toList()));

            totalData.put("发布站",boxServerListener3.getData().stream().map(m -> m.getCheckGroup()).collect(Collectors.toList()));

        } catch (Exception ex) {
            logger.error("import excel to db fail", ex);
        } finally {
            in.close();
            if (excelReader != null) {
                excelReader.finish();
            }
        }
        result.put("msg", "导入成功");
        return result;
    }

    /**
     * 导出
     *
     * @param response
     */
    //timeSheetSummaryHead(newStrArr[0],newStrArr[1])
    @RequestMapping(value = "/export")
    public void export(HttpServletResponse response) throws IOException {
        String excelName = URLEncoder.encode(fileName.substring(0, 4) + "年" + fileName.substring(4, 6) + "月份考勤表", "UTF-8").replaceAll("\\+", "%20");
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + excelName + ".xlsx");
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        try {
            //EasyExcel.write(response.getOutputStream()).head(MonthTotalExportDto.class).sheet(0,"925G考勤表").doWrite(data());
            Map<String, List<MonthTotalExportDto>> datas = data();
            WriteSheet writeSheet = EasyExcel.writerSheet(0, "九二五G"+getChineseMonth(fileName.substring(4,6))+"考勤表").head(MonthTotalExportDto.class).build();
            excelWriter.write(datas.get("jew"), writeSheet);
            writeSheet = EasyExcel.writerSheet(1, "发布站"+getChineseMonth(fileName.substring(4,6))+"考勤表").head(MonthTotalExport2Dto.class).build();
            excelWriter.write(datas.get("fbz"), writeSheet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            excelWriter.finish();
        }
    }

    private Map<String,List<MonthTotalExportDto>> data() {
        Map<String,List<MonthTotalExportDto>> datas = new HashMap<>();//返回九二五和发布站两个sheet的数据
        List<MonthTotalExportDto> jew = new ArrayList<>();
        List<MonthTotalExportDto> fbz = new ArrayList<>();
        List<MonthTotalDto> monthTotal = (List<MonthTotalDto>) totalData.get("月汇总数据");
        List<String> jewNames = (List<String>) totalData.get("九二五");
        jewNames.removeIf(Objects::isNull);
        List<String> fbzNames = (List<String>) totalData.get("发布站");
        fbzNames.removeIf(Objects::isNull);
        int jewNum = 1;//九二五sheet页序号
        int fbzNum = 1;//发布站sheet页序号
        //九二五
        List<MonthTotalDto> list1 = monthTotal.stream().filter(m -> m.getName() !=null && jewNames.contains(m.getName().replace("（离职）",""))).collect(Collectors.toList());
        //发布站
        List<MonthTotalDto> list2 = monthTotal.stream().filter(m -> m.getName() !=null && fbzNames.contains(m.getName().replace("（离职）",""))).collect(Collectors.toList());
        /*for (String str : jewNames){

        }
        for (MonthTotalDto mtd : monthTotal) {
            MonthTotalExportDto mt = new MonthTotalExportDto();
            if (StringUtils.isBlank(mtd.getUserId()) || "UserId".equals(mtd.getUserId()) || StringUtils.isBlank(mtd.getName()) || "姓名".equals(mtd.getName())) {
                continue;
            }
            mt.setXh(num);
            mt.setName(mtd.getNeedName());
            mt.setDept(mtd.getDept());
            mt.setSjcq(mtd.getCqts());
            mt.setJiab("");
            mt.setY(StringUtils.isBlank(mtd.getYi()) ? "" : "正常".equals(mtd.getYi()) ? "√" : "休息".equals(mtd.getYi()) ? "休" : "休息并打卡".equals(mtd.getYi()) ? "√" : (mtd.getYi().lastIndexOf("年假")) != -1 ?
                    "年假" : (mtd.getYi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getYi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getYi()) ? "旷工" : "√");
            mt.setE(StringUtils.isBlank(mtd.getEr()) ? "" : "正常".equals(mtd.getEr()) ? "√" : "休息".equals(mtd.getEr()) ? "休" : "休息并打卡".equals(mtd.getEr()) ? "√" : (mtd.getEr().lastIndexOf("年假")) != -1 ?
                    "年假" : (mtd.getEr().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getEr().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getEr()) ? "旷工" : "√");
            mt.setSan(StringUtils.isBlank(mtd.getSan()) ? "" : "正常".equals(mtd.getSan()) ? "√" : "休息".equals(mtd.getSan()) ? "休" : "休息并打卡".equals(mtd.getSan()) ? "√" : (mtd.getSan().lastIndexOf("年假")) != -1 ?
                    "年假" : (mtd.getSan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSan()) ? "旷工" : "√");
            mt.setS(StringUtils.isBlank(mtd.getSi()) ? "" : "正常".equals(mtd.getSi()) ? "√" : "休息".equals(mtd.getSi()) ? "休" : "休息并打卡".equals(mtd.getSi()) ? "√"
                    : (mtd.getSi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSi()) ? "旷工" : "√");
            mt.setW(StringUtils.isBlank(mtd.getWu()) ? "" : "正常".equals(mtd.getWu()) ? "√" : "休息".equals(mtd.getWu()) ? "休" : "休息并打卡".equals(mtd.getWu()) ? "√"
                    : (mtd.getWu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getWu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getWu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getWu()) ? "旷工" : "√");
            mt.setL(StringUtils.isBlank(mtd.getLiu()) ? "" : "正常".equals(mtd.getLiu()) ? "√" : "休息".equals(mtd.getLiu()) ? "休" : "休息并打卡".equals(mtd.getLiu()) ? "√"
                    : (mtd.getLiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getLiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getLiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getLiu()) ? "旷工" : "√");
            mt.setQ(StringUtils.isBlank(mtd.getQi()) ? "" : "正常".equals(mtd.getQi()) ? "√" : "休息".equals(mtd.getQi()) ? "休" : "休息并打卡".equals(mtd.getQi()) ? "√"
                    : (mtd.getQi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getQi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getQi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getQi()) ? "旷工" : "√");
            mt.setB(StringUtils.isBlank(mtd.getBa()) ? "" : "正常".equals(mtd.getBa()) ? "√" : "休息".equals(mtd.getBa()) ? "休" : "休息并打卡".equals(mtd.getBa()) ? "√"
                    : (mtd.getBa().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getBa().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getBa().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getBa()) ? "旷工" : "√");
            mt.setJ(StringUtils.isBlank(mtd.getJiu()) ? "" : "正常".equals(mtd.getJiu()) ? "√" : "休息".equals(mtd.getJiu()) ? "休" : "休息并打卡".equals(mtd.getJiu()) ? "√"
                    : (mtd.getJiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getJiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getJiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getJiu()) ? "旷工" : "√");
            mt.setShi(StringUtils.isBlank(mtd.getShi()) ? "" : "正常".equals(mtd.getShi()) ? "√" : "休息".equals(mtd.getShi()) ? "休" : "休息并打卡".equals(mtd.getShi()) ? "√"
                    : (mtd.getShi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShi()) ? "旷工" : "√");
            mt.setSy(StringUtils.isBlank(mtd.getShiyi()) ? "" : "正常".equals(mtd.getShiyi()) ? "√" : "休息".equals(mtd.getShiyi()) ? "休" : "休息并打卡".equals(mtd.getShiyi()) ? "√"
                    : (mtd.getShiyi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiyi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiyi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiyi()) ? "旷工" : "√");
            mt.setSe(StringUtils.isBlank(mtd.getShier()) ? "" : "正常".equals(mtd.getShier()) ? "√" : "休息".equals(mtd.getShier()) ? "休" : "休息并打卡".equals(mtd.getShier()) ? "√"
                    : (mtd.getShier().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShier().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShier().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShier()) ? "旷工" : "√");
            mt.setSsan(StringUtils.isBlank(mtd.getShisan()) ? "" : "正常".equals(mtd.getShisan()) ? "√" : "休息".equals(mtd.getShisan()) ? "休" : "休息并打卡".equals(mtd.getShisan()) ? "√"
                    : (mtd.getShisan().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShisan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShisan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShisan()) ? "旷工" : "√");
            mt.setSs(StringUtils.isBlank(mtd.getShisi()) ? "" : "正常".equals(mtd.getShisi()) ? "√" : "休息".equals(mtd.getShisi()) ? "休" : "休息并打卡".equals(mtd.getShisi()) ? "√"
                    : (mtd.getShisi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShisi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShisi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShisi()) ? "旷工" : "√");
            mt.setSw(StringUtils.isBlank(mtd.getShiwu()) ? "" : "正常".equals(mtd.getShiwu()) ? "√" : "休息".equals(mtd.getShiwu()) ? "休" : "休息并打卡".equals(mtd.getShiwu()) ? "√"
                    : (mtd.getShiwu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiwu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiwu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiwu()) ? "旷工" : "√");
            mt.setSl(StringUtils.isBlank(mtd.getShiliu()) ? "" : "正常".equals(mtd.getShiliu()) ? "√" : "休息".equals(mtd.getShiliu()) ? "休" : "休息并打卡".equals(mtd.getShiliu()) ? "√"
                    : (mtd.getShiliu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiliu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiliu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiliu()) ? "旷工" : "√");
            mt.setSq(StringUtils.isBlank(mtd.getShiqi()) ? "" : "正常".equals(mtd.getShiqi()) ? "√" : "休息".equals(mtd.getShiqi()) ? "休" : "休息并打卡".equals(mtd.getShiqi()) ? "√"
                    : (mtd.getShiqi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiqi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiqi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiqi()) ? "旷工" : "√");
            mt.setSb(StringUtils.isBlank(mtd.getShiba()) ? "" : "正常".equals(mtd.getShiba()) ? "√" : "休息".equals(mtd.getShiba()) ? "休" : "休息并打卡".equals(mtd.getShiba()) ? "√"
                    : (mtd.getShiba().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiba().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiba().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiba()) ? "旷工" : "√");
            mt.setSj(StringUtils.isBlank(mtd.getShijiu()) ? "" : "正常".equals(mtd.getShijiu()) ? "√" : "休息".equals(mtd.getShijiu()) ? "休" : "休息并打卡".equals(mtd.getShijiu()) ? "√"
                    : (mtd.getShijiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShijiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShijiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShijiu()) ? "旷工" : "√");
            mt.setEs(StringUtils.isBlank(mtd.getErshi()) ? "" : "正常".equals(mtd.getErshi()) ? "√" : "休息".equals(mtd.getErshi()) ? "休" : "休息并打卡".equals(mtd.getErshi()) ? "√"
                    : (mtd.getErshi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErshi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErshi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErshi()) ? "旷工" : "√");
            mt.setEy(StringUtils.isBlank(mtd.getEryi()) ? "" : "正常".equals(mtd.getEryi()) ? "√" : "休息".equals(mtd.getEryi()) ? "休" : "休息并打卡".equals(mtd.getEryi()) ? "√"
                    : (mtd.getEryi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getEryi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getEryi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getEryi()) ? "旷工" : "√");
            mt.setEe(StringUtils.isBlank(mtd.getErer()) ? "" : "正常".equals(mtd.getErer()) ? "√" : "休息".equals(mtd.getErer()) ? "休" : "休息并打卡".equals(mtd.getErer()) ? "√"
                    : (mtd.getErer().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErer().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErer().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErer()) ? "旷工" : "√");
            mt.setEsa(StringUtils.isBlank(mtd.getErsan()) ? "" : "正常".equals(mtd.getErsan()) ? "√" : "休息".equals(mtd.getErsan()) ? "休" : "休息并打卡".equals(mtd.getErsan()) ? "√"
                    : (mtd.getErsan().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErsan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErsan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErsan()) ? "旷工" : "√");
            mt.setEsi(StringUtils.isBlank(mtd.getErsi()) ? "" : "正常".equals(mtd.getErsi()) ? "√" : "休息".equals(mtd.getErsi()) ? "休" : "休息并打卡".equals(mtd.getErsi()) ? "√"
                    : (mtd.getErsi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErsi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErsi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErsi()) ? "旷工" : "√");
            mt.setEw(StringUtils.isBlank(mtd.getErwu()) ? "" : "正常".equals(mtd.getErwu()) ? "√" : "休息".equals(mtd.getErwu()) ? "休" : "休息并打卡".equals(mtd.getErwu()) ? "√"
                    : (mtd.getErwu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErwu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErwu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErwu()) ? "旷工" : "√");
            mt.setEliu(StringUtils.isBlank(mtd.getErliu()) ? "" : "正常".equals(mtd.getErliu()) ? "√" : "休息".equals(mtd.getErliu()) ? "休" : "休息并打卡".equals(mtd.getErliu()) ? "√"
                    : (mtd.getErliu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErliu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErliu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErliu()) ? "旷工" : "√");
            mt.setEq(StringUtils.isBlank(mtd.getErqi()) ? "" : "正常".equals(mtd.getErqi()) ? "√" : "休息".equals(mtd.getErqi()) ? "休" : "休息并打卡".equals(mtd.getErqi()) ? "√"
                    : (mtd.getErqi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErqi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErqi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErqi()) ? "旷工" : "√");
            mt.setEb(StringUtils.isBlank(mtd.getErba()) ? "" : "正常".equals(mtd.getErba()) ? "√" : "休息".equals(mtd.getErba()) ? "休" : "休息并打卡".equals(mtd.getErba()) ? "√"
                    : (mtd.getErba().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErba().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErba().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErba()) ? "旷工" : "√");
            mt.setEj(StringUtils.isBlank(mtd.getErjiu()) ? "" : "正常".equals(mtd.getErjiu()) ? "√" : "休息".equals(mtd.getErjiu()) ? "休" : "休息并打卡".equals(mtd.getErjiu()) ? "√"
                    : (mtd.getErjiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErjiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErjiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErjiu()) ? "旷工" : "√");
            mt.setSans(StringUtils.isBlank(mtd.getSanshi()) ? "" : "正常".equals(mtd.getSanshi()) ? "√" : "休息".equals(mtd.getSanshi()) ? "休" : "休息并打卡".equals(mtd.getSanshi()) ? "√"
                    : (mtd.getSanshi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSanshi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSanshi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSanshi()) ? "旷工" : "√");
            mt.setSany(StringUtils.isBlank(mtd.getSanyi()) ? "" : "正常".equals(mtd.getSanyi()) ? "√" : "休息".equals(mtd.getSanyi()) ? "休" : "休息并打卡".equals(mtd.getSanyi()) ? "√"
                    : (mtd.getSanyi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSanyi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSanyi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSanyi()) ? "旷工" : "√");
            mt.setYcqts("");
            mt.setBj(mtd.getBj());
            mt.setShij(mtd.getSj());
            mt.setNj(mtd.getNj());
            mt.setKg(mtd.getKgts());
            mt.setJiab("");
            mt.setFdj("");
            mt.setZmj("");
            mt.setTx("");
            mt.setHj("");
            mt.setJxcqts("");
            mt.setFgzr("");
            mt.setSynj("");
            if (jewNames.contains(mt.getName())){

            }

            num++;
        }*/

        //九二五，按照给定的人名排序
        for (String str : jewNames){
            MonthTotalExportDto mt = new MonthTotalExportDto();
            for (MonthTotalDto mtd : list1){
                if (str.equals(mtd.getName())){
                    if (StringUtils.isBlank(mtd.getUserId()) || "UserId".equals(mtd.getUserId()) || StringUtils.isBlank(mtd.getName()) || "姓名".equals(mtd.getName())) {
                        continue;
                    }
                    mt.setXh(jewNum);
                    mt.setName(mtd.getName());
                    mt.setDept(mtd.getDept());
                    mt.setSjcq(mtd.getCqts());
                    mt.setJiab("");
                    mt.setY(StringUtils.isBlank(mtd.getYi()) ? "" : "正常".equals(mtd.getYi()) ? "√" : "休息".equals(mtd.getYi()) ? "休" : "休息并打卡".equals(mtd.getYi()) ? "√" : (mtd.getYi().lastIndexOf("年假")) != -1 ?
                            "年假" : (mtd.getYi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getYi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getYi()) ? "旷工" : "√");
                    mt.setE(StringUtils.isBlank(mtd.getEr()) ? "" : "正常".equals(mtd.getEr()) ? "√" : "休息".equals(mtd.getEr()) ? "休" : "休息并打卡".equals(mtd.getEr()) ? "√" : (mtd.getEr().lastIndexOf("年假")) != -1 ?
                            "年假" : (mtd.getEr().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getEr().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getEr()) ? "旷工" : "√");
                    mt.setSan(StringUtils.isBlank(mtd.getSan()) ? "" : "正常".equals(mtd.getSan()) ? "√" : "休息".equals(mtd.getSan()) ? "休" : "休息并打卡".equals(mtd.getSan()) ? "√" : (mtd.getSan().lastIndexOf("年假")) != -1 ?
                            "年假" : (mtd.getSan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSan()) ? "旷工" : "√");
                    mt.setS(StringUtils.isBlank(mtd.getSi()) ? "" : "正常".equals(mtd.getSi()) ? "√" : "休息".equals(mtd.getSi()) ? "休" : "休息并打卡".equals(mtd.getSi()) ? "√"
                            : (mtd.getSi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSi()) ? "旷工" : "√");
                    mt.setW(StringUtils.isBlank(mtd.getWu()) ? "" : "正常".equals(mtd.getWu()) ? "√" : "休息".equals(mtd.getWu()) ? "休" : "休息并打卡".equals(mtd.getWu()) ? "√"
                            : (mtd.getWu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getWu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getWu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getWu()) ? "旷工" : "√");
                    mt.setL(StringUtils.isBlank(mtd.getLiu()) ? "" : "正常".equals(mtd.getLiu()) ? "√" : "休息".equals(mtd.getLiu()) ? "休" : "休息并打卡".equals(mtd.getLiu()) ? "√"
                            : (mtd.getLiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getLiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getLiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getLiu()) ? "旷工" : "√");
                    mt.setQ(StringUtils.isBlank(mtd.getQi()) ? "" : "正常".equals(mtd.getQi()) ? "√" : "休息".equals(mtd.getQi()) ? "休" : "休息并打卡".equals(mtd.getQi()) ? "√"
                            : (mtd.getQi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getQi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getQi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getQi()) ? "旷工" : "√");
                    mt.setB(StringUtils.isBlank(mtd.getBa()) ? "" : "正常".equals(mtd.getBa()) ? "√" : "休息".equals(mtd.getBa()) ? "休" : "休息并打卡".equals(mtd.getBa()) ? "√"
                            : (mtd.getBa().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getBa().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getBa().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getBa()) ? "旷工" : "√");
                    mt.setJ(StringUtils.isBlank(mtd.getJiu()) ? "" : "正常".equals(mtd.getJiu()) ? "√" : "休息".equals(mtd.getJiu()) ? "休" : "休息并打卡".equals(mtd.getJiu()) ? "√"
                            : (mtd.getJiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getJiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getJiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getJiu()) ? "旷工" : "√");
                    mt.setShi(StringUtils.isBlank(mtd.getShi()) ? "" : "正常".equals(mtd.getShi()) ? "√" : "休息".equals(mtd.getShi()) ? "休" : "休息并打卡".equals(mtd.getShi()) ? "√"
                            : (mtd.getShi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShi()) ? "旷工" : "√");
                    mt.setSy(StringUtils.isBlank(mtd.getShiyi()) ? "" : "正常".equals(mtd.getShiyi()) ? "√" : "休息".equals(mtd.getShiyi()) ? "休" : "休息并打卡".equals(mtd.getShiyi()) ? "√"
                            : (mtd.getShiyi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiyi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiyi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiyi()) ? "旷工" : "√");
                    mt.setSe(StringUtils.isBlank(mtd.getShier()) ? "" : "正常".equals(mtd.getShier()) ? "√" : "休息".equals(mtd.getShier()) ? "休" : "休息并打卡".equals(mtd.getShier()) ? "√"
                            : (mtd.getShier().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShier().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShier().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShier()) ? "旷工" : "√");
                    mt.setSsan(StringUtils.isBlank(mtd.getShisan()) ? "" : "正常".equals(mtd.getShisan()) ? "√" : "休息".equals(mtd.getShisan()) ? "休" : "休息并打卡".equals(mtd.getShisan()) ? "√"
                            : (mtd.getShisan().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShisan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShisan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShisan()) ? "旷工" : "√");
                    mt.setSs(StringUtils.isBlank(mtd.getShisi()) ? "" : "正常".equals(mtd.getShisi()) ? "√" : "休息".equals(mtd.getShisi()) ? "休" : "休息并打卡".equals(mtd.getShisi()) ? "√"
                            : (mtd.getShisi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShisi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShisi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShisi()) ? "旷工" : "√");
                    mt.setSw(StringUtils.isBlank(mtd.getShiwu()) ? "" : "正常".equals(mtd.getShiwu()) ? "√" : "休息".equals(mtd.getShiwu()) ? "休" : "休息并打卡".equals(mtd.getShiwu()) ? "√"
                            : (mtd.getShiwu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiwu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiwu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiwu()) ? "旷工" : "√");
                    mt.setSl(StringUtils.isBlank(mtd.getShiliu()) ? "" : "正常".equals(mtd.getShiliu()) ? "√" : "休息".equals(mtd.getShiliu()) ? "休" : "休息并打卡".equals(mtd.getShiliu()) ? "√"
                            : (mtd.getShiliu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiliu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiliu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiliu()) ? "旷工" : "√");
                    mt.setSq(StringUtils.isBlank(mtd.getShiqi()) ? "" : "正常".equals(mtd.getShiqi()) ? "√" : "休息".equals(mtd.getShiqi()) ? "休" : "休息并打卡".equals(mtd.getShiqi()) ? "√"
                            : (mtd.getShiqi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiqi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiqi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiqi()) ? "旷工" : "√");
                    mt.setSb(StringUtils.isBlank(mtd.getShiba()) ? "" : "正常".equals(mtd.getShiba()) ? "√" : "休息".equals(mtd.getShiba()) ? "休" : "休息并打卡".equals(mtd.getShiba()) ? "√"
                            : (mtd.getShiba().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiba().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiba().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiba()) ? "旷工" : "√");
                    mt.setSj(StringUtils.isBlank(mtd.getShijiu()) ? "" : "正常".equals(mtd.getShijiu()) ? "√" : "休息".equals(mtd.getShijiu()) ? "休" : "休息并打卡".equals(mtd.getShijiu()) ? "√"
                            : (mtd.getShijiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShijiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShijiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShijiu()) ? "旷工" : "√");
                    mt.setEs(StringUtils.isBlank(mtd.getErshi()) ? "" : "正常".equals(mtd.getErshi()) ? "√" : "休息".equals(mtd.getErshi()) ? "休" : "休息并打卡".equals(mtd.getErshi()) ? "√"
                            : (mtd.getErshi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErshi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErshi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErshi()) ? "旷工" : "√");
                    mt.setEy(StringUtils.isBlank(mtd.getEryi()) ? "" : "正常".equals(mtd.getEryi()) ? "√" : "休息".equals(mtd.getEryi()) ? "休" : "休息并打卡".equals(mtd.getEryi()) ? "√"
                            : (mtd.getEryi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getEryi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getEryi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getEryi()) ? "旷工" : "√");
                    mt.setEe(StringUtils.isBlank(mtd.getErer()) ? "" : "正常".equals(mtd.getErer()) ? "√" : "休息".equals(mtd.getErer()) ? "休" : "休息并打卡".equals(mtd.getErer()) ? "√"
                            : (mtd.getErer().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErer().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErer().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErer()) ? "旷工" : "√");
                    mt.setEsa(StringUtils.isBlank(mtd.getErsan()) ? "" : "正常".equals(mtd.getErsan()) ? "√" : "休息".equals(mtd.getErsan()) ? "休" : "休息并打卡".equals(mtd.getErsan()) ? "√"
                            : (mtd.getErsan().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErsan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErsan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErsan()) ? "旷工" : "√");
                    mt.setEsi(StringUtils.isBlank(mtd.getErsi()) ? "" : "正常".equals(mtd.getErsi()) ? "√" : "休息".equals(mtd.getErsi()) ? "休" : "休息并打卡".equals(mtd.getErsi()) ? "√"
                            : (mtd.getErsi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErsi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErsi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErsi()) ? "旷工" : "√");
                    mt.setEw(StringUtils.isBlank(mtd.getErwu()) ? "" : "正常".equals(mtd.getErwu()) ? "√" : "休息".equals(mtd.getErwu()) ? "休" : "休息并打卡".equals(mtd.getErwu()) ? "√"
                            : (mtd.getErwu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErwu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErwu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErwu()) ? "旷工" : "√");
                    mt.setEliu(StringUtils.isBlank(mtd.getErliu()) ? "" : "正常".equals(mtd.getErliu()) ? "√" : "休息".equals(mtd.getErliu()) ? "休" : "休息并打卡".equals(mtd.getErliu()) ? "√"
                            : (mtd.getErliu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErliu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErliu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErliu()) ? "旷工" : "√");
                    mt.setEq(StringUtils.isBlank(mtd.getErqi()) ? "" : "正常".equals(mtd.getErqi()) ? "√" : "休息".equals(mtd.getErqi()) ? "休" : "休息并打卡".equals(mtd.getErqi()) ? "√"
                            : (mtd.getErqi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErqi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErqi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErqi()) ? "旷工" : "√");
                    mt.setEb(StringUtils.isBlank(mtd.getErba()) ? "" : "正常".equals(mtd.getErba()) ? "√" : "休息".equals(mtd.getErba()) ? "休" : "休息并打卡".equals(mtd.getErba()) ? "√"
                            : (mtd.getErba().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErba().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErba().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErba()) ? "旷工" : "√");
                    mt.setEj(StringUtils.isBlank(mtd.getErjiu()) ? "" : "正常".equals(mtd.getErjiu()) ? "√" : "休息".equals(mtd.getErjiu()) ? "休" : "休息并打卡".equals(mtd.getErjiu()) ? "√"
                            : (mtd.getErjiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErjiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErjiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErjiu()) ? "旷工" : "√");
                    mt.setSans(StringUtils.isBlank(mtd.getSanshi()) ? "" : "正常".equals(mtd.getSanshi()) ? "√" : "休息".equals(mtd.getSanshi()) ? "休" : "休息并打卡".equals(mtd.getSanshi()) ? "√"
                            : (mtd.getSanshi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSanshi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSanshi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSanshi()) ? "旷工" : "√");
                    mt.setSany(StringUtils.isBlank(mtd.getSanyi()) ? "" : "正常".equals(mtd.getSanyi()) ? "√" : "休息".equals(mtd.getSanyi()) ? "休" : "休息并打卡".equals(mtd.getSanyi()) ? "√"
                            : (mtd.getSanyi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSanyi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSanyi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSanyi()) ? "旷工" : "√");
                    mt.setYcqts("");
                    mt.setBj(mtd.getBj());
                    mt.setShij(mtd.getSj());
                    mt.setNj(mtd.getNj());
                    mt.setKg(mtd.getKgts());
                    mt.setJiab("");
                    mt.setFdj("");
                    mt.setZmj("");
                    mt.setTx("");
                    mt.setHj("");
                    mt.setJxcqts("");
                    mt.setFgzr("");
                    mt.setSynj("");
                    jew.add(mt);
                    jewNum++;
                    continue;
                }
            }
        }

        //发布站，按照给定的人名排序
        for (String str : fbzNames){
            MonthTotalExportDto mt = new MonthTotalExportDto();
            for (MonthTotalDto mtd : list2){
                if (str.equals(mtd.getName())){
                    if (StringUtils.isBlank(mtd.getUserId()) || "UserId".equals(mtd.getUserId()) || StringUtils.isBlank(mtd.getName()) || "姓名".equals(mtd.getName())) {
                        continue;
                    }
                    mt.setXh(fbzNum);
                    mt.setName(mtd.getName());
                    mt.setDept(mtd.getDept());
                    mt.setSjcq(mtd.getCqts());
                    mt.setJiab("");
                    mt.setY(StringUtils.isBlank(mtd.getYi()) ? "" : "正常".equals(mtd.getYi()) ? "√" : "休息".equals(mtd.getYi()) ? "休" : "休息并打卡".equals(mtd.getYi()) ? "√" : (mtd.getYi().lastIndexOf("年假")) != -1 ?
                            "年假" : (mtd.getYi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getYi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getYi()) ? "旷工" : "√");
                    mt.setE(StringUtils.isBlank(mtd.getEr()) ? "" : "正常".equals(mtd.getEr()) ? "√" : "休息".equals(mtd.getEr()) ? "休" : "休息并打卡".equals(mtd.getEr()) ? "√" : (mtd.getEr().lastIndexOf("年假")) != -1 ?
                            "年假" : (mtd.getEr().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getEr().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getEr()) ? "旷工" : "√");
                    mt.setSan(StringUtils.isBlank(mtd.getSan()) ? "" : "正常".equals(mtd.getSan()) ? "√" : "休息".equals(mtd.getSan()) ? "休" : "休息并打卡".equals(mtd.getSan()) ? "√" : (mtd.getSan().lastIndexOf("年假")) != -1 ?
                            "年假" : (mtd.getSan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSan()) ? "旷工" : "√");
                    mt.setS(StringUtils.isBlank(mtd.getSi()) ? "" : "正常".equals(mtd.getSi()) ? "√" : "休息".equals(mtd.getSi()) ? "休" : "休息并打卡".equals(mtd.getSi()) ? "√"
                            : (mtd.getSi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSi()) ? "旷工" : "√");
                    mt.setW(StringUtils.isBlank(mtd.getWu()) ? "" : "正常".equals(mtd.getWu()) ? "√" : "休息".equals(mtd.getWu()) ? "休" : "休息并打卡".equals(mtd.getWu()) ? "√"
                            : (mtd.getWu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getWu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getWu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getWu()) ? "旷工" : "√");
                    mt.setL(StringUtils.isBlank(mtd.getLiu()) ? "" : "正常".equals(mtd.getLiu()) ? "√" : "休息".equals(mtd.getLiu()) ? "休" : "休息并打卡".equals(mtd.getLiu()) ? "√"
                            : (mtd.getLiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getLiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getLiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getLiu()) ? "旷工" : "√");
                    mt.setQ(StringUtils.isBlank(mtd.getQi()) ? "" : "正常".equals(mtd.getQi()) ? "√" : "休息".equals(mtd.getQi()) ? "休" : "休息并打卡".equals(mtd.getQi()) ? "√"
                            : (mtd.getQi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getQi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getQi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getQi()) ? "旷工" : "√");
                    mt.setB(StringUtils.isBlank(mtd.getBa()) ? "" : "正常".equals(mtd.getBa()) ? "√" : "休息".equals(mtd.getBa()) ? "休" : "休息并打卡".equals(mtd.getBa()) ? "√"
                            : (mtd.getBa().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getBa().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getBa().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getBa()) ? "旷工" : "√");
                    mt.setJ(StringUtils.isBlank(mtd.getJiu()) ? "" : "正常".equals(mtd.getJiu()) ? "√" : "休息".equals(mtd.getJiu()) ? "休" : "休息并打卡".equals(mtd.getJiu()) ? "√"
                            : (mtd.getJiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getJiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getJiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getJiu()) ? "旷工" : "√");
                    mt.setShi(StringUtils.isBlank(mtd.getShi()) ? "" : "正常".equals(mtd.getShi()) ? "√" : "休息".equals(mtd.getShi()) ? "休" : "休息并打卡".equals(mtd.getShi()) ? "√"
                            : (mtd.getShi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShi()) ? "旷工" : "√");
                    mt.setSy(StringUtils.isBlank(mtd.getShiyi()) ? "" : "正常".equals(mtd.getShiyi()) ? "√" : "休息".equals(mtd.getShiyi()) ? "休" : "休息并打卡".equals(mtd.getShiyi()) ? "√"
                            : (mtd.getShiyi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiyi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiyi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiyi()) ? "旷工" : "√");
                    mt.setSe(StringUtils.isBlank(mtd.getShier()) ? "" : "正常".equals(mtd.getShier()) ? "√" : "休息".equals(mtd.getShier()) ? "休" : "休息并打卡".equals(mtd.getShier()) ? "√"
                            : (mtd.getShier().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShier().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShier().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShier()) ? "旷工" : "√");
                    mt.setSsan(StringUtils.isBlank(mtd.getShisan()) ? "" : "正常".equals(mtd.getShisan()) ? "√" : "休息".equals(mtd.getShisan()) ? "休" : "休息并打卡".equals(mtd.getShisan()) ? "√"
                            : (mtd.getShisan().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShisan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShisan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShisan()) ? "旷工" : "√");
                    mt.setSs(StringUtils.isBlank(mtd.getShisi()) ? "" : "正常".equals(mtd.getShisi()) ? "√" : "休息".equals(mtd.getShisi()) ? "休" : "休息并打卡".equals(mtd.getShisi()) ? "√"
                            : (mtd.getShisi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShisi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShisi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShisi()) ? "旷工" : "√");
                    mt.setSw(StringUtils.isBlank(mtd.getShiwu()) ? "" : "正常".equals(mtd.getShiwu()) ? "√" : "休息".equals(mtd.getShiwu()) ? "休" : "休息并打卡".equals(mtd.getShiwu()) ? "√"
                            : (mtd.getShiwu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiwu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiwu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiwu()) ? "旷工" : "√");
                    mt.setSl(StringUtils.isBlank(mtd.getShiliu()) ? "" : "正常".equals(mtd.getShiliu()) ? "√" : "休息".equals(mtd.getShiliu()) ? "休" : "休息并打卡".equals(mtd.getShiliu()) ? "√"
                            : (mtd.getShiliu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiliu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiliu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiliu()) ? "旷工" : "√");
                    mt.setSq(StringUtils.isBlank(mtd.getShiqi()) ? "" : "正常".equals(mtd.getShiqi()) ? "√" : "休息".equals(mtd.getShiqi()) ? "休" : "休息并打卡".equals(mtd.getShiqi()) ? "√"
                            : (mtd.getShiqi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiqi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiqi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiqi()) ? "旷工" : "√");
                    mt.setSb(StringUtils.isBlank(mtd.getShiba()) ? "" : "正常".equals(mtd.getShiba()) ? "√" : "休息".equals(mtd.getShiba()) ? "休" : "休息并打卡".equals(mtd.getShiba()) ? "√"
                            : (mtd.getShiba().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShiba().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShiba().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShiba()) ? "旷工" : "√");
                    mt.setSj(StringUtils.isBlank(mtd.getShijiu()) ? "" : "正常".equals(mtd.getShijiu()) ? "√" : "休息".equals(mtd.getShijiu()) ? "休" : "休息并打卡".equals(mtd.getShijiu()) ? "√"
                            : (mtd.getShijiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getShijiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getShijiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getShijiu()) ? "旷工" : "√");
                    mt.setEs(StringUtils.isBlank(mtd.getErshi()) ? "" : "正常".equals(mtd.getErshi()) ? "√" : "休息".equals(mtd.getErshi()) ? "休" : "休息并打卡".equals(mtd.getErshi()) ? "√"
                            : (mtd.getErshi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErshi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErshi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErshi()) ? "旷工" : "√");
                    mt.setEy(StringUtils.isBlank(mtd.getEryi()) ? "" : "正常".equals(mtd.getEryi()) ? "√" : "休息".equals(mtd.getEryi()) ? "休" : "休息并打卡".equals(mtd.getEryi()) ? "√"
                            : (mtd.getEryi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getEryi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getEryi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getEryi()) ? "旷工" : "√");
                    mt.setEe(StringUtils.isBlank(mtd.getErer()) ? "" : "正常".equals(mtd.getErer()) ? "√" : "休息".equals(mtd.getErer()) ? "休" : "休息并打卡".equals(mtd.getErer()) ? "√"
                            : (mtd.getErer().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErer().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErer().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErer()) ? "旷工" : "√");
                    mt.setEsa(StringUtils.isBlank(mtd.getErsan()) ? "" : "正常".equals(mtd.getErsan()) ? "√" : "休息".equals(mtd.getErsan()) ? "休" : "休息并打卡".equals(mtd.getErsan()) ? "√"
                            : (mtd.getErsan().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErsan().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErsan().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErsan()) ? "旷工" : "√");
                    mt.setEsi(StringUtils.isBlank(mtd.getErsi()) ? "" : "正常".equals(mtd.getErsi()) ? "√" : "休息".equals(mtd.getErsi()) ? "休" : "休息并打卡".equals(mtd.getErsi()) ? "√"
                            : (mtd.getErsi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErsi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErsi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErsi()) ? "旷工" : "√");
                    mt.setEw(StringUtils.isBlank(mtd.getErwu()) ? "" : "正常".equals(mtd.getErwu()) ? "√" : "休息".equals(mtd.getErwu()) ? "休" : "休息并打卡".equals(mtd.getErwu()) ? "√"
                            : (mtd.getErwu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErwu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErwu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErwu()) ? "旷工" : "√");
                    mt.setEliu(StringUtils.isBlank(mtd.getErliu()) ? "" : "正常".equals(mtd.getErliu()) ? "√" : "休息".equals(mtd.getErliu()) ? "休" : "休息并打卡".equals(mtd.getErliu()) ? "√"
                            : (mtd.getErliu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErliu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErliu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErliu()) ? "旷工" : "√");
                    mt.setEq(StringUtils.isBlank(mtd.getErqi()) ? "" : "正常".equals(mtd.getErqi()) ? "√" : "休息".equals(mtd.getErqi()) ? "休" : "休息并打卡".equals(mtd.getErqi()) ? "√"
                            : (mtd.getErqi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErqi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErqi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErqi()) ? "旷工" : "√");
                    mt.setEb(StringUtils.isBlank(mtd.getErba()) ? "" : "正常".equals(mtd.getErba()) ? "√" : "休息".equals(mtd.getErba()) ? "休" : "休息并打卡".equals(mtd.getErba()) ? "√"
                            : (mtd.getErba().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErba().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErba().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErba()) ? "旷工" : "√");
                    mt.setEj(StringUtils.isBlank(mtd.getErjiu()) ? "" : "正常".equals(mtd.getErjiu()) ? "√" : "休息".equals(mtd.getErjiu()) ? "休" : "休息并打卡".equals(mtd.getErjiu()) ? "√"
                            : (mtd.getErjiu().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getErjiu().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getErjiu().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getErjiu()) ? "旷工" : "√");
                    mt.setSans(StringUtils.isBlank(mtd.getSanshi()) ? "" : "正常".equals(mtd.getSanshi()) ? "√" : "休息".equals(mtd.getSanshi()) ? "休" : "休息并打卡".equals(mtd.getSanshi()) ? "√"
                            : (mtd.getSanshi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSanshi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSanshi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSanshi()) ? "旷工" : "√");
                    mt.setSany(StringUtils.isBlank(mtd.getSanyi()) ? "" : "正常".equals(mtd.getSanyi()) ? "√" : "休息".equals(mtd.getSanyi()) ? "休" : "休息并打卡".equals(mtd.getSanyi()) ? "√"
                            : (mtd.getSanyi().lastIndexOf("年假")) != -1 ? "年假" : (mtd.getSanyi().lastIndexOf("事假")) != -1 ? "事假" : (mtd.getSanyi().lastIndexOf("出差")) != -1 ? "出差" : "旷工".equals(mtd.getSanyi()) ? "旷工" : "√");
                    mt.setYcqts("");
                    mt.setBj(mtd.getBj());
                    mt.setShij(mtd.getSj());
                    mt.setNj(mtd.getNj());
                    mt.setKg(mtd.getKgts());
                    mt.setJiab("");
                    mt.setFdj("");
                    mt.setZmj("");
                    mt.setTx("");
                    mt.setHj("");
                    mt.setJxcqts("");
                    mt.setFgzr("");
                    mt.setSynj("");
                    fbz.add(mt);
                    fbzNum++;
                    continue;
                }
            }
        }
        datas.put("jew",jew);
        datas.put("fbz",fbz);
        return datas;
    }

    /**
     * xxxx年x月考勤表汇总表头
     *
     * @return
     */
    private List<List<String>> timeSheetSummaryHead(String year, String month) {
        List<List<String>> list = new ArrayList<List<String>>();
        List<String> head0 = new ArrayList<String>();
        head0.add(year + "年" + month + "月考勤及扣款确认表");
        head0.add("序号");
        List<String> head1 = new ArrayList<>();
        head1.add(year + "年" + month + "月考勤及扣款确认表");
        head1.add("部门");
        List<String> head2 = new ArrayList<>();
        head2.add(year + "年" + month + "月考勤及扣款确认表");
        head2.add("姓名");
        List<String> head3 = new ArrayList<>();
        head3.add(year + "年" + month + "月考勤及扣款确认表");
        head3.add("应出勤天数");
        List<String> head4 = new ArrayList<>();
        head4.add(year + "年" + month + "月考勤及扣款确认表");
        head4.add("实际出勤天数");
        List<String> head5 = new ArrayList<>();
        head5.add(year + "年" + month + "月考勤及扣款确认表");
        head5.add("事假天数");
        List<String> head6 = new ArrayList<>();
        head6.add(year + "年" + month + "月考勤及扣款确认表");
        head6.add("病假天数");
        List<String> head7 = new ArrayList<>();
        head7.add(year + "年" + month + "月考勤及扣款确认表");
        head7.add("年假天数");
        List<String> head8 = new ArrayList<>();
        head8.add(year + "年" + month + "月考勤及扣款确认表");
        head8.add("丧假天数");
        List<String> head9 = new ArrayList<>();
        head9.add(year + "年" + month + "月考勤及扣款确认表");
        head9.add("婚假天数");
        List<String> head10 = new ArrayList<>();
        head10.add(year + "年" + month + "月考勤及扣款确认表");
        head10.add("旷工");
        List<String> head11 = new ArrayList<>();
        head11.add(year + "年" + month + "月考勤及扣款确认表");
        head11.add("迟到  2-10分（10）");
        List<String> head12 = new ArrayList<>();
        head12.add(year + "年" + month + "月考勤及扣款确认表");
        head12.add("迟到 11-20分（30）");
        List<String> head13 = new ArrayList<>();
        head13.add(year + "年" + month + "月考勤及扣款确认表");
        head13.add("迟到 21-30分（50）");
        List<String> head14 = new ArrayList<>();
        head14.add(year + "年" + month + "月考勤及扣款确认表");
        head14.add("迟到  30以上（100）");
        List<String> head15 = new ArrayList<>();
        head15.add(year + "年" + month + "月考勤及扣款确认表");
        head15.add("早退  5-10分");
        List<String> head16 = new ArrayList<>();
        head16.add(year + "年" + month + "月考勤及扣款确认表");
        head16.add("早退 11-20分");
        List<String> head17 = new ArrayList<>();
        head17.add(year + "年" + month + "月考勤及扣款确认表");
        head17.add("早退 21-30分");
        List<String> head18 = new ArrayList<>();
        head18.add(year + "年" + month + "月考勤及扣款确认表");
        head18.add("未打卡30");
        List<String> head19 = new ArrayList<>();
        head19.add(year + "年" + month + "月考勤及扣款确认表");
        head19.add("考勤扣款金额合计");
        List<String> head20 = new ArrayList<>();
        head20.add(year + "年" + month + "月考勤及扣款确认表");
        head20.add("扣款说明（免责指是2次10分钟内的迟到）");
        List<String> head21 = new ArrayList<>();
        head21.add(year + "年" + month + "月考勤及扣款确认表");
        head21.add("绩效分数");
        List<String> head22 = new ArrayList<>();
        head22.add(year + "年" + month + "月考勤及扣款确认表");
        head22.add("扣分次数");

        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);
        list.add(head4);
        list.add(head5);
        list.add(head6);
        list.add(head7);
        list.add(head8);
        list.add(head9);
        list.add(head10);
        list.add(head11);
        list.add(head12);
        list.add(head13);
        list.add(head14);
        list.add(head15);
        list.add(head16);
        list.add(head17);
        list.add(head18);
        list.add(head19);
        list.add(head20);
        list.add(head21);
        list.add(head22);
        return list;
    }


    public static String run(String text) {
        String dateStr = text.replaceAll("r?n", " ");
        try {
            List matches = null;
            Pattern p = Pattern.compile("(\\d{1,4}\\d{1,2}\\d{1,2}\\d{1,2}\\d{1,2}\\d{1,2})", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher matcher = p.matcher(dateStr);
            if (matcher.find() && matcher.groupCount() >= 1) {
                matches = new ArrayList();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String temp = matcher.group(i);
                    matches.add(temp);
                }
            } else {
                matches = Collections.EMPTY_LIST;
            }
            if (matches.size() > 0) {
                return ((String) matches.get(0)).trim();
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String getChineseMonth(String month) {
        switch (month) {
            case "01":
                return "一月";
            case "02":
                return "二月";
            case "03":
                return "三月";
            case "04":
                return "四月";
            case "05":
                return "五月";
            case "06":
                return "六月";
            case "07":
                return "七月";
            case "08":
                return "八月";
            case "09":
                return "九月";
            case "10":
                return "十月";
            case "11":
                return "十一月";
            case "12":
                return "十二月";
            default:
                return "";
        }
    }

}
