package com.grain.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.grain.teacher.entity.EduSubject;
import com.grain.teacher.mapper.EduSubjectMapper;
import com.grain.teacher.service.EduSubjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程科目 服务实现类
 * </p>
 *
 * @author Dragon Wen
 * @since 2020-03-01
 */
@Service
public class EduSubjectServiceImpl extends ServiceImpl<EduSubjectMapper, EduSubject> implements EduSubjectService {

    @Override
    public List<String> importExcel(MultipartFile file) {

        //存储错误信息集合
        List<String> meg = new ArrayList<>();
        try {
            //1、获取文件流
            InputStream inputStream = file.getInputStream();
            //2、根据流创建workBook
            Workbook workbook = new HSSFWorkbook(inputStream);
            //3、获取sheet.getSheetAt(0)
            Sheet sheet = workbook.getSheetAt(0);
            //4、根据sheet获取行数
            int lastRowNum = sheet.getLastRowNum();
            if(lastRowNum <= 1){
                meg.add("请填写数据");
                return meg;
            }
            //5、遍历行
            for (int rowNum = 1; rowNum < lastRowNum; rowNum++) {
                Row row = sheet.getRow(rowNum);
                Cell cell = row.getCell(0);
                //6、获取每一行的第一列：一级分类
                if(cell == null ){
                    meg.add("第" + rowNum + "行第1列为空");
                    continue;
                }
                String cellValue = cell.getStringCellValue();
                if(StringUtils.isEmpty(cellValue)){
                    meg.add("第" + rowNum + "行第1列为数据空");
                    continue;
                }

                //7、判断列是否存在，存在获取的数据
                EduSubject subject = this.selectSubjectByName(cellValue);
                String pid = "";
                //8、把这一列中的数据（一级分类）保存到数据库中
                if(subject == null){
                    //9、在保存之前判断此一级分类是否存在，如果在就不再添加；如果不存在就保存数据
                    EduSubject su = new EduSubject();
                    su.setTitle(cellValue);
                    su.setParentId("0");
                    su.setSort(0);
                    baseMapper.insert(su);
                    pid = su.getId();
                } else {
                    pid = subject.getId();
                }

                //10、再获取每一行的第二列
                Cell cell_1 = row.getCell(1);
                //11、获取第二列中的数据（二级分类）
                if(cell_1 == null){
                    meg.add("第" + rowNum + "行第2列为空");
                    continue;
                }
                String stringCellValue = cell_1.getStringCellValue();
                if(StringUtils.isEmpty(stringCellValue)){
                    meg.add("第" + rowNum + "行第2列为数据空");
                    continue;
                }
                //12、判断此一级分类中是否存在此二级分类
                EduSubject subject_1 = this.selectSubjectByNameAndParentId(stringCellValue,pid);
                //13、如果此一级分类中有此二级分类：不保存
                if(subject_1 == null){
                    EduSubject su = new EduSubject();
                    su.setTitle(stringCellValue);
                    su.setParentId(pid);
                    su.setSort(0);
                    baseMapper.insert(su);
                }
                //14、如果没有则保存
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return meg;
    }

    /**
     * 根据课程分类的名字和父类ID查询分类是否存在
     * @param stringCellValue
     * @param pid
     * @return
     */
    private EduSubject selectSubjectByNameAndParentId(String stringCellValue, String pid) {
        QueryWrapper<EduSubject> subjectQueryWrapper = new QueryWrapper<>();
        subjectQueryWrapper.eq("title", stringCellValue);
        subjectQueryWrapper.eq("parent_id", pid);
        EduSubject subject = baseMapper.selectOne(subjectQueryWrapper);
        return subject;
    }

    /**
     * 根据课程分类的名字查询分类是否存在
     * @param cellValue
     * @return
     */
    private EduSubject selectSubjectByName(String cellValue) {
        QueryWrapper<EduSubject> subjectQueryWrapper = new QueryWrapper<>();
        subjectQueryWrapper.eq("title", cellValue);
        subjectQueryWrapper.eq("parent_id", 0);
        EduSubject subject = baseMapper.selectOne(subjectQueryWrapper);
        return subject;
    }
}