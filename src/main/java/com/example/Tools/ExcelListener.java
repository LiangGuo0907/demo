package com.example.Tools;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.example.entity.MonthTotalDto;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wl
 * @date 2021/5/27 8:24
 */
public class ExcelListener extends AnalysisEventListener<MonthTotalDto> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelListener.class);

    private List<MonthTotalDto> data = new ArrayList<>();

    @Override
    public void invoke(MonthTotalDto o, AnalysisContext analysisContext) {
        LOGGER.info("解析到一条数据:{}", JSON.toJSONString(o));
        data.add(o);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        LOGGER.info("所有数据解析完成！");
    }

    /**
     * 入库
     */
    private void saveData() {
        LOGGER.info("{}条数据，开始存储数据库！", data.size());
        //这个方法自己实现  能完成保存数据入库即可
        LOGGER.info("存储数据库成功！");
    }

    public List<MonthTotalDto> getData() {
        return data;
    }

    public void setData(List<MonthTotalDto> data) {
        this.data = data;
    }
}
