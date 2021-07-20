package com.example.demo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

/**
 * @author wl
 * @date 2021/5/27 15:01
 */
@Data
public class MonthTotalExport2Dto extends BaseRowModel {
    @ExcelProperty({"发布站 月考勤表", "序号"})
    private Integer xh;
    @ExcelProperty({"发布站 月考勤表", "姓名"})
    private String name;
    @ExcelProperty({"发布站 月考勤表", "部门"})
    private String dept;
    @ExcelProperty({"发布站 月考勤表", "实际出勤/天"})
    private String sjcq;
    @ExcelProperty({"发布站 月考勤表", "加班/天"})
    private String jb;
    @ExcelProperty({"发布站 月考勤表", "1"})
    private String y;
    @ExcelProperty({"发布站 月考勤表", "2"})
    private String e;
    @ExcelProperty({"发布站 月考勤表", "3"})
    private String san;
    @ExcelProperty({"发布站 月考勤表", "4"})
    private String s;
    @ExcelProperty({"发布站 月考勤表", "5"})
    private String w;
    @ExcelProperty({"发布站 月考勤表", "6"})
    private String l;
    @ExcelProperty({"发布站 月考勤表", "7"})
    private String q;
    @ExcelProperty({"发布站 月考勤表", "8"})
    private String b;
    @ExcelProperty({"发布站 月考勤表", "9"})
    private String j;
    @ExcelProperty({"发布站 月考勤表", "10"})
    private String shi;
    @ExcelProperty({"发布站 月考勤表", "11"})
    private String sy;
    @ExcelProperty({"发布站 月考勤表", "12"})
    private String se;
    @ExcelProperty({"发布站 月考勤表", "13"})
    private String ssan;
    @ExcelProperty({"发布站 月考勤表", "14"})
    private String ss;
    @ExcelProperty({"发布站 月考勤表", "15"})
    private String sw;
    @ExcelProperty({"发布站 月考勤表", "16"})
    private String sl;
    @ExcelProperty({"发布站 月考勤表", "17"})
    private String sq;
    @ExcelProperty({"发布站 月考勤表", "18"})
    private String sb;
    @ExcelProperty({"发布站 月考勤表", "19"})
    private String sj;
    @ExcelProperty({"发布站 月考勤表", "20"})
    private String es;
    @ExcelProperty({"发布站 月考勤表", "21"})
    private String ey;
    @ExcelProperty({"发布站 月考勤表", "22"})
    private String ee;
    @ExcelProperty({"发布站 月考勤表", "23"})
    private String esa;
    @ExcelProperty({"发布站 月考勤表", "24"})
    private String esi;
    @ExcelProperty({"发布站 月考勤表", "25"})
    private String ew;
    @ExcelProperty({"发布站 月考勤表", "26"})
    private String eliu;
    @ExcelProperty({"发布站 月考勤表", "27"})
    private String eq;
    @ExcelProperty({"发布站 月考勤表", "28"})
    private String eb;
    @ExcelProperty({"发布站 月考勤表", "29"})
    private String ej;
    @ExcelProperty({"发布站 月考勤表", "30"})
    private String sans;
    @ExcelProperty({"发布站 月考勤表", "31"})
    private String sany;
    @ExcelProperty({"发布站 月考勤表", "应出勤天数"})
    private String ycqts;
    @ExcelProperty({"发布站 月考勤表", "病假"})
    private String bj;
    @ExcelProperty({"发布站 月考勤表", "事假"})
    private String shij;
    @ExcelProperty({"发布站 月考勤表", "年假"})
    private String nj;
    @ExcelProperty({"发布站 月考勤表", "旷工"})
    private String kg;
    @ExcelProperty({"发布站 月考勤表", "加班/时"})
    private String jiab;
    @ExcelProperty({"发布站 月考勤表", "法定加"})
    private String fdj;
    @ExcelProperty({"发布站 月考勤表", "周末加/天"})
    private String zmj;
    @ExcelProperty({"发布站 月考勤表", "调休"})
    private String tx;
    @ExcelProperty({"发布站 月考勤表", "合计"})
    private String hj;
    @ExcelProperty({"发布站 月考勤表", "计薪出勤天数"})
    private String jxcqts;
    @ExcelProperty({"发布站 月考勤表", "非√日"})
    private String fgzr;
    @ExcelProperty({"发布站 月考勤表", "剩年假剩余/天"})
    private String synj;
}
