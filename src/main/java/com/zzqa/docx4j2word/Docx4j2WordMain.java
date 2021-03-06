package com.zzqa.docx4j2word;

import com.zzqa.pojo.UnitInfo;
import com.zzqa.utils.Docx4jUtil;
import com.zzqa.utils.LoadDataUtils;
import com.zzqa.utils.LoadExeclUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.toc.Toc;
import org.docx4j.toc.TocGenerator;
import org.docx4j.toc.TocHelper;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ClassName: Docx4j2WordMain
 * Description:
 *
 * @author 张文豪
 * @date 2020/8/3 11:17
 */
public class Docx4j2WordMain {
    public static void main(String[] args) {
        try {
            WordprocessingMLPackage wpMLPackage = WordprocessingMLPackage.createPackage();

            //数据准备
            String reportName = "风电场风电机组";  //项目名称
            long startTime = 1468166400000L;    //报告开始时间毫秒值
            long endTime = System.currentTimeMillis();  //报告结束时间毫秒值
            String logoPath = "D:\\01_ideaspace\\Docx4jStudy\\src\\main\\resources\\images\\logo.png"; //封面logo路径
            String linePath = "src\\main\\resources\\images\\横线.png";   //封面横线路径

            int normalPart = 106;   //正常机组数量
            int warningPart = 20;   //预警机组数量
            int alarmPart = 12; //报警机组数量
            //最后保存的文件名
            String fileName = reportName + getDate(new Date(startTime)) + getDate(new Date(endTime)) + "检测报告.docx";
            //保存的文件路径
            String targetFilePath = "D:/AutoExport";
            String targetFile = targetFilePath + "/" + fileName;
            //PageContent2的数据准备
            List<List<List<List<UnitInfo>>>> unitInfos = createData();
            //TODO 删除下面输出
//            unitInfos.stream().forEach(l->{
//                l.stream().forEach(s->{
//                    s.stream().forEach(p->{
//                        System.out.println(p.toString());
//                    });
//                });
//            });


            Cover cover = new Cover();
            //创建封面
            wpMLPackage = cover.createCover(wpMLPackage, reportName, startTime, endTime, logoPath, linePath);
            //在这个前面添加目录
            Docx4jUtil.addNextSection(wpMLPackage);
            //文件内容1:项目概述
            PageContent1 pageContent1 = new PageContent1();
            pageContent1.createPageContent1(wpMLPackage, reportName, normalPart, warningPart, alarmPart);
            //文件内容2:运行状况
            PageContent2 pageContent2 = new PageContent2();
            pageContent2.createPageContent2(wpMLPackage, unitInfos);
            //文件内容3：震动图谱
            PageContent3 pageContent3 = new PageContent3();
            pageContent3.createPageContent3(wpMLPackage, unitInfos, 0);
            //文件内容4：补充说明
            PageContent4 pageContent4 = new PageContent4();
            pageContent4.createPageContent4(wpMLPackage);

            //保存文件
            File docxFile = new File(targetFilePath);
            if (!docxFile.exists() && !docxFile.isDirectory()) {
                docxFile.mkdirs();
            }

            //生成目录
            TocGenerator tocGenerator = new TocGenerator(wpMLPackage);
            Toc.setTocHeadingText("目录");
            tocGenerator.generateToc(15, TocHelper.DEFAULT_TOC_INSTRUCTION, true);

            wpMLPackage.save(new File(targetFile));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据准备
     *
     * @return
     */
    public static List<List<List<List<UnitInfo>>>> createData() {
        //趋势图
        String dataX1 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/趋势图X.txt");
        String[] colKeys = dataX1.split(",");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        simpleDateFormat.applyPattern("yyyy/MM/dd HH:mm");
        long [] colKeys1 = new long[colKeys.length];
        for (int i=0; i<colKeys.length; i++){
            try {
                Date parse = simpleDateFormat.parse(colKeys[i]);
                colKeys1[i] = parse.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String dataY1 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/趋势图Y.txt");
        double[][] data1 = string2DoubleArray(dataY1);


        //波形图
        String dataX2 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/波形图X.txt");
        String[] colKeys2 = dataX2.split(",");
        String dataY2 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/波形图Y.txt");
        double[][] data2 = string2DoubleArray(dataY2);
        //频谱图
        String dataX3 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/频谱图X.txt");
        String[] colKeys3 = dataX3.split(",");
        String dataY3 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/频谱图Y.txt");
        double[][] data3 = string2DoubleArray(dataY3);
        //包络图
        String dataX4 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/包络图X.txt");
        String[] colKeys4 = dataX4.split(",");
        String dataY4 = LoadDataUtils.ReadFile("C:/Users/Mi_dad/Desktop/包络图Y.txt");
        double[][] data4 = string2DoubleArray(dataY4);

        List<List<List<List<UnitInfo>>>> lists = LoadExeclUtil.loadTest();
        lists.forEach(list1->list1.forEach(list2->list2.forEach(list3->list3.forEach(unitInfo -> {
            unitInfo.setColKeys1(colKeys1);
            unitInfo.setColKeys2(string2Double(colKeys2));
            unitInfo.setColKeys3(string2Double(colKeys3));
            unitInfo.setColKeys4(string2Double(colKeys4));

            unitInfo.setData1(data1);
            unitInfo.setData2(data2);
            unitInfo.setData3(data3);
            unitInfo.setData4(data4);
        }))));

        return lists;
    }

    /**
     * 将数据转换为double数组
     *
     * @param dataY
     * @return
     */
    private static double[][] string2DoubleArray(String dataY) {
        String[] split = dataY.split(",");
        double[] dataTemp = new double[split.length];
        for (int i = 0; i < split.length; i++) {
            if (split[i] != null) {
                dataTemp[i] = Double.parseDouble(split[i]);
            }
        }
        double[][] data = {dataTemp};
        return data;
    }

    /**
     * String类型转换为Double类型
     * @param colKeys
     * @return
     */
    public static double[] string2Double(String[] colKeys){
        double [] colKeys1 = new double[colKeys.length];
        for (int i=0; i<colKeys.length; i++){
            colKeys1[i] = Double.parseDouble(colKeys[i]);
        }
        return colKeys1;
    }

    public static String getDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
        sdf.applyPattern("yyyy年M月d日");
        //TODO 删除输出语句
        System.out.println("转换时间：" + sdf.format(date)); // 输出已经格式化的现在时间（24小时制）
        return sdf.format(date);
    }
}
